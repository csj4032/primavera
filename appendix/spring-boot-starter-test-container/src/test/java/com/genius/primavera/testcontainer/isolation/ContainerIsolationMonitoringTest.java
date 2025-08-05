package com.genius.primavera.testcontainer.isolation;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MariaDBContainer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 컨테이너 격리 모니터링 테스트
 * 여러 테스트 클래스가 동시에 실행될 때 각각 독립적인 컨테이너를 사용하는지 모니터링
 */
@Slf4j
@SpringBootTest
@EnableTestContainers(containers = ContainerType.MARIADB)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("컨테이너 격리 모니터링 테스트")
class ContainerIsolationMonitoringTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private MariaDBContainer<?> mariaDBContainer;

    // 컨테이너 정보를 저장할 정적 맵 (여러 테스트 클래스 간 공유)
    private static final ConcurrentHashMap<String, String> containerRegistry = new ConcurrentHashMap<>();

    @BeforeAll
    void setupMonitoring() {
        log.info("=== 컨테이너 격리 모니터링 테스트 시작 ===");
        
        if (mariaDBContainer != null) {
            String containerInfo = String.format("URL=%s, Host:Port=%s:%d, ContainerID=%s", 
                mariaDBContainer.getJdbcUrl(),
                mariaDBContainer.getHost(),
                mariaDBContainer.getMappedPort(3306),
                mariaDBContainer.getContainerId()
            );
            
            String testClassName = this.getClass().getSimpleName();
            containerRegistry.put(testClassName, containerInfo);
            
            log.info("컨테이너 등록 - {}: {}", testClassName, containerInfo);
        }
        
        // 격리 모니터링 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS container_isolation_log (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                test_class VARCHAR(200),
                container_url VARCHAR(500),
                container_id VARCHAR(100),
                host_port VARCHAR(50),
                test_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                isolation_marker VARCHAR(100),
                data_count INT DEFAULT 0
            )
        """);
        
        log.info("격리 모니터링 테이블 준비 완료");
    }

    @Test
    @DisplayName("모니터링 - 컨테이너 고유성 확인")
    void testContainerUniqueness() throws InterruptedException {
        log.info("컨테이너 고유성 확인 테스트 시작");
        
        Thread.sleep(100); // 다른 테스트와의 실행 시차
        
        String testClassName = this.getClass().getSimpleName();
        String isolationMarker = "MONITOR_" + System.currentTimeMillis();
        
        // 현재 컨테이너 정보 기록
        if (mariaDBContainer != null) {
            jdbcTemplate.update("""
                INSERT INTO container_isolation_log 
                (test_class, container_url, container_id, host_port, isolation_marker) 
                VALUES (?, ?, ?, ?, ?)
                """,
                testClassName,
                mariaDBContainer.getJdbcUrl(),
                mariaDBContainer.getContainerId(),
                mariaDBContainer.getHost() + ":" + mariaDBContainer.getMappedPort(3306),
                isolationMarker
            );
        }
        
        // 고유한 모니터링 데이터 삽입
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('Monitor User %d', 'monitor_%d@%s.com')", 
                i, i, testClassName.toLowerCase()
            ));
        }
        
        // 다른 테스트 클래스의 데이터가 없는지 확인
        Integer otherTestData = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email NOT LIKE ? AND email NOT LIKE '%@example.com'",
            Integer.class,
            "%@" + testClassName.toLowerCase() + ".com"
        );
        
        log.info("모니터링 결과 - 현재 테스트 데이터: 3건, 다른 테스트 데이터: {}건", otherTestData);
        
        // 격리 상태 업데이트
        jdbcTemplate.update(
            "UPDATE container_isolation_log SET data_count = ? WHERE isolation_marker = ?",
            3, isolationMarker
        );
        
        log.info("컨테이너 고유성 확인 완료");
    }

    @Test
    @DisplayName("모니터링 - 동시 실행 감지 및 분석")
    void testConcurrentExecutionDetection() throws InterruptedException {
        log.info("동시 실행 감지 테스트 시작");
        
        Thread.sleep(200);
        
        // 현재 시간 기준으로 최근 1분 내 실행된 테스트들 조회
        List<Map<String, Object>> recentTests = jdbcTemplate.queryForList("""
            SELECT 
                test_class,
                container_id,
                host_port,
                test_timestamp,
                COUNT(*) as execution_count
            FROM container_isolation_log 
            WHERE test_timestamp >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
            GROUP BY test_class, container_id
            ORDER BY test_timestamp DESC
        """);
        
        log.info("최근 1분 내 실행된 테스트 수: {}", recentTests.size());
        
        for (Map<String, Object> test : recentTests) {
            log.info("동시 실행 감지 - 테스트: {}, 컨테이너 ID: {}, 호스트:포트: {}, 실행 시간: {}", 
                test.get("test_class"),
                test.get("container_id"), 
                test.get("host_port"),
                test.get("test_timestamp")
            );
        }
        
        // 컨테이너 고유성 분석
        Long uniqueContainerCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT container_id) FROM container_isolation_log WHERE test_timestamp >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)",
            Long.class
        );
        
        Long uniqueTestClassCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT test_class) FROM container_isolation_log WHERE test_timestamp >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)",
            Long.class
        );
        
        log.info("격리 분석 결과 - 고유 컨테이너 수: {}, 고유 테스트 클래스 수: {}", 
                 uniqueContainerCount, uniqueTestClassCount);
        
        // 격리가 올바르게 작동하면 컨테이너 수 >= 테스트 클래스 수
        assertThat(uniqueContainerCount).isGreaterThanOrEqualTo(uniqueTestClassCount);
        
        log.info("동시 실행 감지 및 분석 완료");
    }

    @AfterAll
    void generateIsolationReport() {
        log.info("=== 컨테이너 격리 최종 리포트 ===");
        
        try {
            // 전체 격리 로그 통계
            List<Map<String, Object>> isolationStats = jdbcTemplate.queryForList("""
                SELECT 
                    test_class,
                    container_id,
                    host_port,
                    MIN(test_timestamp) as first_execution,
                    MAX(test_timestamp) as last_execution,
                    COUNT(*) as total_executions,
                    SUM(data_count) as total_data_inserted
                FROM container_isolation_log 
                GROUP BY test_class, container_id
                ORDER BY first_execution
            """);
            
            log.info("전체 격리 통계:");
            for (Map<String, Object> stat : isolationStats) {
                log.info("  테스트 클래스: {} | 컨테이너 ID: {} | 호스트:포트: {} | 실행 횟수: {} | 삽입 데이터: {}",
                    stat.get("test_class"),
                    stat.get("container_id"),
                    stat.get("host_port"),
                    stat.get("total_executions"),
                    stat.get("total_data_inserted")
                );
            }
            
            // 컨테이너 재사용 분석
            Long totalContainers = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT container_id) FROM container_isolation_log", Long.class
            );
            Long totalTestClasses = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT test_class) FROM container_isolation_log", Long.class
            );
            
            log.info("=== 격리 결과 분석 ===");
            log.info("총 사용된 컨테이너 수: {}", totalContainers);
            log.info("총 테스트 클래스 수: {}", totalTestClasses);
            
            if (totalContainers.equals(totalTestClasses)) {
                log.info("✅ 완벽한 격리: 각 테스트 클래스가 독립적인 컨테이너 사용");
            } else if (totalContainers > totalTestClasses) {
                log.info("⚠️  과도한 컨테이너 생성: 일부 테스트에서 추가 컨테이너 생성됨");
            } else {
                log.info("❌ 격리 실패: 일부 테스트 클래스가 컨테이너를 공유함");
            }
            
            // 등록된 컨테이너 정보 출력
            log.info("=== 등록된 컨테이너 정보 ===");
            containerRegistry.forEach((testClass, containerInfo) -> {
                log.info("  {}: {}", testClass, containerInfo);
            });
            
        } catch (Exception e) {
            log.error("격리 리포트 생성 중 오류 발생", e);
        }
        
        log.info("=== 컨테이너 격리 모니터링 테스트 완료 ===");
    }
}