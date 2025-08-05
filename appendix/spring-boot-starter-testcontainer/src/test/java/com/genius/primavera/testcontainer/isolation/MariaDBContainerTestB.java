package com.genius.primavera.testcontainer.isolation;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MariaDBContainer;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MariaDB 컨테이너 격리 테스트 B
 * 다른 테스트 클래스와 독립적인 컨테이너를 사용하는지 확인
 */
@Slf4j
@SpringBootTest(properties = "primavera.test.isolation=TestB")
@EnableTestContainers(containers = ContainerType.MARIADB)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MariaDB 컨테이너 격리 테스트 B")
class MariaDBContainerTestB {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private MariaDBContainer<?> mariaDBContainer;

    private static final AtomicInteger testCounter = new AtomicInteger(0);
    private String containerUrl;
    private String containerHostPort;

    @BeforeAll
    void beforeAllTests() {
        log.info("=== MariaDBContainerTestB 시작 ===");
        if (mariaDBContainer != null) {
            containerUrl = mariaDBContainer.getJdbcUrl();
            containerHostPort = mariaDBContainer.getHost() + ":" + mariaDBContainer.getMappedPort(3306);
            log.info("테스트 B - MariaDB 컨테이너 URL: {}", containerUrl);
            log.info("테스트 B - MariaDB 호스트:포트: {}", containerHostPort);
        }
    }

    @BeforeEach
    void beforeEachTest() {
        int testNum = testCounter.incrementAndGet();
        log.info("테스트 B - {} 번째 테스트 시작", testNum);
        
        // 현재 컨테이너 정보 로깅
        if (mariaDBContainer != null) {
            log.info("테스트 B - 현재 컨테이너 상태: running={}, host:port={}", 
                     mariaDBContainer.isRunning(), containerHostPort);
        }
    }

    @Test
    @Order(1)
    @DisplayName("테스트 B-1: 고유 데이터 삽입 및 확인")
    void testB1_insertUniqueData() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 B-1 시작 - 고유 데이터 삽입", threadName);
        
        // 의도적인 지연으로 동시 실행 확인
        Thread.sleep(250);
        
        // 초기 데이터 수 확인
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("테스트 B-1 - 초기 사용자 수: {}", initialCount);
        
        // 고유한 테스트 B 데이터 삽입
        for (int i = 1; i <= 7; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('TestB User %d', 'testB_%d@containerB.com')", 
                i, i
            ));
        }
        
        // 테스트 B 데이터만 조회
        Integer testBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        assertThat(testBCount).isEqualTo(7);
        
        // 테스트 A 데이터가 없는지 확인 (격리 확인)
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(0);
        
        log.info("테스트 B-1 완료 - TestB 데이터: {} 건, TestA 데이터: {} 건", testBCount, testACount);
    }

    @Test
    @Order(2)
    @DisplayName("테스트 B-2: 컨테이너 격리 상태 지속 확인")
    void testB2_verifyContainerIsolation() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 B-2 시작 - 격리 상태 지속 확인", threadName);
        
        Thread.sleep(180);
        
        // 이전 테스트의 데이터가 유지되는지 확인 (같은 컨테이너 사용)
        Integer testBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        assertThat(testBCount).isEqualTo(7);
        
        // 추가 데이터 삽입 (TestB만의 특별한 데이터)
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('TestB Special %d', 'testB_special_%d@containerB.com')", 
                i, i
            ));
        }
        
        // 전체 테스트 B 데이터 확인
        Integer totalTestBCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        assertThat(totalTestBCount).isEqualTo(10);
        
        // 여전히 테스트 A 데이터는 없어야 함
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(0);
        
        log.info("테스트 B-2 완료 - 총 TestB 데이터: {} 건, TestA 데이터: {} 건", totalTestBCount, testACount);
    }

    @Test
    @Order(3)
    @DisplayName("테스트 B-3: 대량 데이터 처리 및 성능 테스트")
    void testB3_bulkDataProcessing() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 B-3 시작 - 대량 데이터 처리", threadName);
        
        Thread.sleep(100);
        
        // 대량 데이터 삽입 (TestB의 독립성 강조)
        long startTime = System.currentTimeMillis();
        for (int i = 1; i <= 50; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('TestB Bulk %d', 'testB_bulk_%d@containerB.com')", 
                i, i
            ));
        }
        long insertTime = System.currentTimeMillis() - startTime;
        
        // 복잡한 집계 쿼리 수행
        startTime = System.currentTimeMillis();
        var stats = jdbcTemplate.queryForMap("""
            SELECT 
                COUNT(*) as total_count,
                COUNT(DISTINCT SUBSTRING_INDEX(email, '@', -1)) as unique_domains,
                AVG(LENGTH(name)) as avg_name_length,
                MIN(created_at) as first_created,
                MAX(created_at) as last_created
            FROM test_users 
            WHERE email LIKE '%@containerB.com'
        """);
        long queryTime = System.currentTimeMillis() - startTime;
        
        // 결과 검증
        assertThat(((Number) stats.get("total_count")).intValue()).isEqualTo(60); // 10 + 50
        assertThat(((Number) stats.get("unique_domains")).intValue()).isEqualTo(1); // containerB.com만
        
        log.info("테스트 B-3 - 성능 결과: 삽입 시간 {}ms, 집계 쿼리 시간 {}ms", insertTime, queryTime);
        log.info("테스트 B-3 - 통계: {}", stats);
        
        // 최종 격리 확인
        Integer testACount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerA.com'", 
            Integer.class
        );
        assertThat(testACount).isEqualTo(0);
        
        log.info("테스트 B-3 완료 - 격리 상태 유지 확인");
    }

    @Test
    @Order(4)
    @DisplayName("테스트 B-4: 컨테이너 정보 및 최종 상태 확인")
    void testB4_finalContainerVerification() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 B-4 시작 - 최종 상태 확인", threadName);
        
        Thread.sleep(120);
        
        // 컨테이너 상세 정보 로깅
        if (mariaDBContainer != null) {
            log.info("테스트 B-4 - 컨테이너 상세 정보:");
            log.info("  - JDBC URL: {}", mariaDBContainer.getJdbcUrl());
            log.info("  - Host: {}", mariaDBContainer.getHost());
            log.info("  - Port: {}", mariaDBContainer.getMappedPort(3306));
            log.info("  - Container ID: {}", mariaDBContainer.getContainerId());
            log.info("  - Running: {}", mariaDBContainer.isRunning());
        }
        
        // 최종 데이터 상태 확인
        Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        Integer testBUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE '%@containerB.com'", 
            Integer.class
        );
        
        log.info("테스트 B-4 - 최종 상태: 전체 사용자 {} 명, TestB 사용자 {} 명", totalUsers, testBUsers);
        
        // 테스트 B만의 고유한 태그 데이터 삽입 (격리 확인용)
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('FINAL_TEST_B_MARKER', 'final_marker@containerB.com')");
        
        Integer finalMarkerCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email = 'final_marker@containerB.com'", 
            Integer.class
        );
        assertThat(finalMarkerCount).isEqualTo(1);
        
        log.info("테스트 B-4 완료 - 최종 마커 삽입 완료");
    }

    @AfterAll
    void afterAllTests() {
        log.info("=== MariaDBContainerTestB 종료 ===");
        log.info("총 실행된 테스트 수: {}", testCounter.get());
        
        if (mariaDBContainer != null) {
            log.info("최종 컨테이너 정보 - URL: {}, Host:Port: {}", 
                     containerUrl, containerHostPort);
        }
    }
}