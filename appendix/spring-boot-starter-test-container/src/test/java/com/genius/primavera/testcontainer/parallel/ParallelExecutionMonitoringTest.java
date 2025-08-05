package com.genius.primavera.testcontainer.parallel;

import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 병렬 실행 모니터링 테스트
 * TestContainers의 병렬 실행 성능과 동작을 모니터링하고 분석
 */
@Slf4j
@SpringBootTest
@EnableTestContainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParallelExecutionMonitoringTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final AtomicInteger testExecutionCounter = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, Long> testStartTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> testEndTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> containerInfo = new ConcurrentHashMap<>();

    @BeforeAll
    void setupMonitoring() {
        log.info("=== 병렬 실행 모니터링 테스트 시작 ===");
        
        // 모니터링 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS test_execution_log (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                test_name VARCHAR(200),
                thread_name VARCHAR(100),
                container_info TEXT,
                start_time TIMESTAMP,
                end_time TIMESTAMP,
                duration_ms BIGINT,
                execution_order INT,
                status VARCHAR(20) DEFAULT 'RUNNING'
            )
        """);
        
        log.info("모니터링 테이블 준비 완료");
    }

    @BeforeEach
    void beforeEachTest(TestInfo testInfo) {
        String testName = testInfo.getDisplayName();
        String threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();
        
        testStartTimes.put(testName, startTime);
        
        // 컨테이너 정보 수집
        String containerDetails = collectContainerInfo();
        containerInfo.put(testName, containerDetails);
        
        // 실행 로그 기록
        jdbcTemplate.update("""
            INSERT INTO test_execution_log 
            (test_name, thread_name, container_info, start_time, execution_order, status) 
            VALUES (?, ?, ?, ?, ?, 'STARTED')
        """, 
        testName, threadName, containerDetails, 
        new java.sql.Timestamp(startTime), testExecutionCounter.incrementAndGet());
        
        log.info("[{}] 테스트 시작: {} (실행 순서: {})", threadName, testName, testExecutionCounter.get());
    }

    @AfterEach
    void afterEachTest(TestInfo testInfo) {
        String testName = testInfo.getDisplayName();
        String threadName = Thread.currentThread().getName();
        long endTime = System.currentTimeMillis();
        long startTime = testStartTimes.get(testName);
        long duration = endTime - startTime;
        
        testEndTimes.put(testName, endTime);
        
        // 실행 로그 업데이트
        jdbcTemplate.update("""
            UPDATE test_execution_log 
            SET end_time = ?, duration_ms = ?, status = 'COMPLETED'
            WHERE test_name = ? AND thread_name = ?
        """, 
        new java.sql.Timestamp(endTime), duration, testName, threadName);
        
        log.info("[{}] 테스트 완료: {} (소요시간: {}ms)", threadName, testName, duration);
    }

    @Test
    @Order(1)
    @DisplayName("모니터링 테스트 1: 기본 병렬 실행 측정")
    void monitoringTest1_basicParallelExecution() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 기본 병렬 실행 측정 시작", threadName);
        
        // 작업 시뮬레이션
        Thread.sleep(200);
        
        // 데이터베이스 작업 수행
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('Monitor1-%s-%d', 'monitor1_%s_%d@test.com')", 
                threadName, i, threadName.replaceAll("[^a-zA-Z0-9]", ""), i
            ));
        }
        
        Integer insertedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM test_users WHERE email LIKE 'monitor1_" + 
            threadName.replaceAll("[^a-zA-Z0-9]", "") + "_%'", 
            Integer.class
        );
        assertThat(insertedCount).isEqualTo(5);
        
        log.info("[{}] 기본 병렬 실행 측정 완료 - {} 건 처리", threadName, insertedCount);
    }

    @Test
    @Order(2)
    @DisplayName("모니터링 테스트 2: 중간 부하 병렬 실행")
    void monitoringTest2_mediumLoadParallel() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 중간 부하 병렬 실행 시작", threadName);
        
        Thread.sleep(350);
        
        // 더 많은 데이터베이스 작업
        for (int i = 1; i <= 10; i++) {
            jdbcTemplate.execute(String.format(
                "INSERT INTO test_users (name, email) VALUES ('Monitor2-%s-%d', 'monitor2_%s_%d@test.com')", 
                threadName, i, threadName.replaceAll("[^a-zA-Z0-9]", ""), i
            ));
            
            // 중간에 조회 작업도 수행
            if (i % 3 == 0) {
                Integer currentCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM test_users WHERE email LIKE 'monitor2_" + 
                    threadName.replaceAll("[^a-zA-Z0-9]", "") + "_%'", 
                    Integer.class
                );
                assertThat(currentCount).isEqualTo(i);
            }
        }
        
        log.info("[{}] 중간 부하 병렬 실행 완료", threadName);
    }

    @Test
    @Order(3)
    @DisplayName("모니터링 테스트 3: 고부하 병렬 실행")
    void monitoringTest3_highLoadParallel() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 고부하 병렬 실행 시작", threadName);
        
        Thread.sleep(500);
        
        // 복잡한 쿼리와 대량 작업
        String batchInsertSql = """
            INSERT INTO test_users (name, email) VALUES 
            ('Monitor3-Batch-1', 'monitor3_batch1_%s@test.com'),
            ('Monitor3-Batch-2', 'monitor3_batch2_%s@test.com'),
            ('Monitor3-Batch-3', 'monitor3_batch3_%s@test.com'),
            ('Monitor3-Batch-4', 'monitor3_batch4_%s@test.com'),
            ('Monitor3-Batch-5', 'monitor3_batch5_%s@test.com')
        """;
        
        String threadId = threadName.replaceAll("[^a-zA-Z0-9]", "");
        jdbcTemplate.execute(String.format(batchInsertSql, threadId, threadId, threadId, threadId, threadId));
        
        // 집계 쿼리 수행
        List<Map<String, Object>> results = jdbcTemplate.queryForList("""
            SELECT 
                COUNT(*) as total_count,
                COUNT(DISTINCT SUBSTRING_INDEX(email, '@', 1)) as unique_prefixes,
                MAX(created_at) as latest_created
            FROM test_users 
            WHERE email LIKE 'monitor3_%'
        """);
        
        assertThat(results).hasSize(1);
        assertThat((Long) results.get(0).get("total_count")).isGreaterThanOrEqualTo(5);
        
        log.info("[{}] 고부하 병렬 실행 완료 - 결과: {}", threadName, results.get(0));
    }

    @Test
    @Order(4)
    @DisplayName("모니터링 테스트 4: 동시성 충돌 테스트")
    void monitoringTest4_concurrencyConflict() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 동시성 충돌 테스트 시작", threadName);
        
        Thread.sleep(150);
        
        // 동일한 리소스에 대한 경쟁 상황 시뮬레이션
        String sharedResource = "shared_resource_counter";
        
        // 공유 카운터 테이블 생성 (존재하지 않을 경우)
        jdbcTemplate.execute(String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                counter_name VARCHAR(50) PRIMARY KEY,
                counter_value INT DEFAULT 0,
                last_updated_by VARCHAR(100),
                update_count INT DEFAULT 0
            )
        """, sharedResource));
        
        // 카운터 초기화 (이미 존재할 수 있으므로 IGNORE 사용)
        jdbcTemplate.execute(String.format(
            "INSERT IGNORE INTO %s (counter_name, counter_value) VALUES ('TEST_COUNTER', 0)", 
            sharedResource
        ));
        
        // 빠른 연속 업데이트로 경쟁 상황 생성
        for (int i = 0; i < 5; i++) {
            try {
                jdbcTemplate.update(String.format("""
                    UPDATE %s 
                    SET counter_value = counter_value + 1, 
                        last_updated_by = ?, 
                        update_count = update_count + 1 
                    WHERE counter_name = 'TEST_COUNTER'
                """, sharedResource), threadName);
                
                Thread.sleep(10); // 짧은 대기
            } catch (Exception e) {
                log.warn("[{}] 동시성 충돌 발생: {}", threadName, e.getMessage());
            }
        }
        
        // 최종 상태 확인
        Map<String, Object> finalState = jdbcTemplate.queryForMap(
            String.format("SELECT * FROM %s WHERE counter_name = 'TEST_COUNTER'", sharedResource)
        );
        
        log.info("[{}] 동시성 충돌 테스트 완료 - 최종 상태: {}", threadName, finalState);
    }

    @AfterAll
    void generateReport() {
        log.info("=== 병렬 실행 모니터링 리포트 생성 ===");
        
        try {
            // 실행 통계 조회
            List<Map<String, Object>> executionStats = jdbcTemplate.queryForList("""
                SELECT 
                    test_name,
                    thread_name,
                    duration_ms,
                    execution_order,
                    start_time,
                    end_time
                FROM test_execution_log 
                WHERE status = 'COMPLETED'
                ORDER BY execution_order
            """);
            
            log.info("총 완료된 테스트 수: {}", executionStats.size());
            
            // 통계 정보 출력
            executionStats.forEach(stat -> {
                log.info("테스트: {} | 스레드: {} | 소요시간: {}ms | 순서: {}", 
                    stat.get("test_name"), 
                    stat.get("thread_name"), 
                    stat.get("duration_ms"),
                    stat.get("execution_order")
                );
            });
            
            // 평균 실행 시간 계산
            Double avgDuration = jdbcTemplate.queryForObject(
                "SELECT AVG(duration_ms) FROM test_execution_log WHERE status = 'COMPLETED'", 
                Double.class
            );
            
            // 최대/최소 실행 시간
            Long maxDuration = jdbcTemplate.queryForObject(
                "SELECT MAX(duration_ms) FROM test_execution_log WHERE status = 'COMPLETED'", 
                Long.class
            );
            Long minDuration = jdbcTemplate.queryForObject(
                "SELECT MIN(duration_ms) FROM test_execution_log WHERE status = 'COMPLETED'", 
                Long.class
            );
            
            log.info("=== 실행 시간 통계 ===");
            log.info("평균 실행 시간: {:.2f}ms", avgDuration != null ? avgDuration : 0.0);
            log.info("최대 실행 시간: {}ms", maxDuration != null ? maxDuration : 0);
            log.info("최소 실행 시간: {}ms", minDuration != null ? minDuration : 0);
            
            // 동시 실행 분석
            List<Map<String, Object>> concurrencyAnalysis = jdbcTemplate.queryForList("""
                SELECT 
                    COUNT(DISTINCT thread_name) as concurrent_threads,
                    COUNT(*) as total_tests,
                    MIN(start_time) as first_start,
                    MAX(end_time) as last_end
                FROM test_execution_log 
                WHERE status = 'COMPLETED'
            """);
            
            if (!concurrencyAnalysis.isEmpty()) {
                Map<String, Object> analysis = concurrencyAnalysis.get(0);
                log.info("=== 동시성 분석 ===");
                log.info("동시 실행 스레드 수: {}", analysis.get("concurrent_threads"));
                log.info("총 테스트 수: {}", analysis.get("total_tests"));
                log.info("전체 실행 기간: {} ~ {}", analysis.get("first_start"), analysis.get("last_end"));
            }
            
        } catch (Exception e) {
            log.error("리포트 생성 중 오류 발생", e);
        }
        
        log.info("=== 병렬 실행 모니터링 테스트 완료 ===");
    }

    private String collectContainerInfo() {
        try {
            // 데이터소스 정보 수집
            String dataSourceInfo = jdbcTemplate.getDataSource().toString();
            
            // 데이터베이스 버전 정보
            String dbVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            
            // 현재 연결 정보
            String connectionInfo = jdbcTemplate.queryForObject("SELECT CONNECTION_ID()", String.class);
            
            return String.format("DataSource: %s | DB Version: %s | Connection ID: %s", 
                dataSourceInfo, dbVersion, connectionInfo);
                
        } catch (Exception e) {
            return "Container info collection failed: " + e.getMessage();
        }
    }
}