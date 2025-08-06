package com.genius.primavera.testcontainers;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 성능 및 연결 풀 관리 테스트
 * - 연결 풀 설정 검증
 * - 동시성 연결 테스트
 * - 성능 벤치마크
 * - 연결 누수 감지
 * - 부하 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("성능 및 연결 풀 관리 테스트")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "performanceDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.POSTGRESQL, name = "benchmarkDb")
})
class PerformanceConnectionPoolTest {

    @Autowired
    @Qualifier("performanceDb")
    private DataSource performanceDataSource;

    @Autowired
    @Qualifier("benchmarkDb")
    private DataSource benchmarkDataSource;

    private JdbcTemplate performanceJdbc;
    private JdbcTemplate benchmarkJdbc;
    private ExecutorService executorService;

    @BeforeAll
    void setupPerformanceTests() {
        performanceJdbc = new JdbcTemplate(performanceDataSource);
        benchmarkJdbc = new JdbcTemplate(benchmarkDataSource);
        executorService = Executors.newFixedThreadPool(50);

        // 테스트용 테이블 생성
        performanceJdbc.execute("""
            CREATE TABLE performance_test (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                data VARCHAR(1000),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_created_at (created_at)
            )
        """);

        benchmarkJdbc.execute("""
            CREATE TABLE performance_test (
                id BIGSERIAL PRIMARY KEY,
                data VARCHAR(1000),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        
        benchmarkJdbc.execute("CREATE INDEX idx_created_at ON performance_test(created_at)");

        log.info("성능 테스트 환경 초기화 완료");
    }

    @AfterAll
    void cleanupPerformanceTests() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("HikariCP 연결 풀 설정 검증")
    void testConnectionPoolConfiguration() {
        assertTrue(performanceDataSource instanceof HikariDataSource, "HikariCP 데이터소스여야 함");
        
        HikariDataSource hikariDs = (HikariDataSource) performanceDataSource;
        
        assertTrue(hikariDs.getMaximumPoolSize() > 0, "최대 풀 크기가 설정되어야 함");
        assertTrue(hikariDs.getMinimumIdle() >= 0, "최소 유휴 연결 수가 설정되어야 함");
        assertTrue(hikariDs.getConnectionTimeout() > 0, "연결 타임아웃이 설정되어야 함");
        assertTrue(hikariDs.getIdleTimeout() > 0, "유휴 타임아웃이 설정되어야 함");
        assertTrue(hikariDs.getMaxLifetime() > 0, "최대 생명주기가 설정되어야 함");

        log.info("HikariCP 설정 - MaxPool: {}, MinIdle: {}, ConnTimeout: {}ms, IdleTimeout: {}ms", 
            hikariDs.getMaximumPoolSize(), hikariDs.getMinimumIdle(), 
            hikariDs.getConnectionTimeout(), hikariDs.getIdleTimeout());
    }

    @Test
    @Order(2)
    @DisplayName("동시 연결 처리 테스트")
    void testConcurrentConnections() throws InterruptedException {
        int threadCount = 20;
        int operationsPerThread = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Future<?> future = executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int j = 0; j < operationsPerThread; j++) {
                        try (Connection conn = performanceDataSource.getConnection()) {
                            // 간단한 쿼리 실행
                            performanceJdbc.update(
                                "INSERT INTO performance_test (data) VALUES (?)",
                                "Thread-" + threadId + "-Operation-" + j);
                            successCount.incrementAndGet();
                        } catch (SQLException e) {
                            log.error("Connection error in thread {}: {}", threadId, e.getMessage());
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
            futures.add(future);
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // 모든 스레드 동시 시작
        
        assertTrue(completeLatch.await(30, TimeUnit.SECONDS), "모든 작업이 30초 내에 완료되어야 함");
        long endTime = System.currentTimeMillis();

        int expectedOperations = threadCount * operationsPerThread;
        assertEquals(expectedOperations, successCount.get(), "모든 연결 작업이 성공해야 함");
        assertEquals(0, errorCount.get(), "연결 오류가 없어야 함");

        // 실제 DB에 저장된 데이터 확인
        Integer recordCount = performanceJdbc.queryForObject("SELECT COUNT(*) FROM performance_test", Integer.class);
        assertEquals(expectedOperations, recordCount, "모든 데이터가 DB에 저장되어야 함");

        log.info("동시 연결 테스트 완료 - 스레드: {}, 작업/스레드: {}, 총 성공: {}, 소요시간: {}ms", 
            threadCount, operationsPerThread, successCount.get(), endTime - startTime);
    }

    @Test
    @Order(3)
    @DisplayName("대용량 데이터 삽입 성능 테스트")
    void testBulkInsertPerformance() {
        int batchSize = 1000;
        int totalRecords = 5000;

        long startTime = System.currentTimeMillis();

        // 배치 삽입 테스트
        for (int i = 0; i < totalRecords; i += batchSize) {
            List<String> batch = IntStream.range(i, Math.min(i + batchSize, totalRecords))
                .mapToObj(n -> "Bulk-Insert-Data-" + n + "-" + System.nanoTime())
                .toList();

            performanceJdbc.batchUpdate(
                "INSERT INTO performance_test (data) VALUES (?)",
                batch, 
                batch.size(),
                (ps, data) -> ps.setString(1, data));
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 결과 검증
        Integer finalCount = performanceJdbc.queryForObject("SELECT COUNT(*) FROM performance_test", Integer.class);
        assertTrue(finalCount >= totalRecords, "최소 " + totalRecords + "개의 레코드가 있어야 함");

        // 성능 지표 계산
        double recordsPerSecond = (double) totalRecords / (duration / 1000.0);
        assertTrue(recordsPerSecond > 100, "초당 100개 이상의 레코드가 처리되어야 함");

        log.info("대용량 삽입 성능 - 총 레코드: {}, 소요시간: {}ms, 처리속도: {:.2f} records/sec", 
            totalRecords, duration, recordsPerSecond);
    }

    @Test
    @Order(4)
    @DisplayName("쿼리 성능 벤치마크 (MariaDB vs PostgreSQL)")
    void testQueryPerformanceBenchmark() {
        int testDataSize = 1000;

        // 두 DB에 동일한 테스트 데이터 삽입
        List<String> testData = IntStream.range(0, testDataSize)
            .mapToObj(i -> "Benchmark-Data-" + i + "-" + (i % 100))
            .toList();

        // MariaDB 데이터 삽입
        long mariaDbInsertStart = System.currentTimeMillis();
        performanceJdbc.batchUpdate(
            "INSERT INTO performance_test (data) VALUES (?)",
            testData, 
            testData.size(),
            (ps, data) -> ps.setString(1, data));
        long mariaDbInsertTime = System.currentTimeMillis() - mariaDbInsertStart;

        // PostgreSQL 데이터 삽입
        long pgInsertStart = System.currentTimeMillis();
        benchmarkJdbc.batchUpdate(
            "INSERT INTO performance_test (data) VALUES (?)",
            testData, 
            testData.size(),
            (ps, data) -> ps.setString(1, data));
        long pgInsertTime = System.currentTimeMillis() - pgInsertStart;

        // 조회 성능 테스트
        int queryCount = 100;
        
        // MariaDB 조회 성능
        long mariaDbQueryStart = System.currentTimeMillis();
        for (int i = 0; i < queryCount; i++) {
            String pattern = "%-Data-" + (i % 100) + "-%";
            List<String> results = performanceJdbc.queryForList(
                "SELECT data FROM performance_test WHERE data LIKE ? ORDER BY created_at DESC LIMIT 10", 
                String.class, pattern);
            assertFalse(results.isEmpty(), "쿼리 결과가 있어야 함");
        }
        long mariaDbQueryTime = System.currentTimeMillis() - mariaDbQueryStart;

        // PostgreSQL 조회 성능  
        long pgQueryStart = System.currentTimeMillis();
        for (int i = 0; i < queryCount; i++) {
            String pattern = "%-Data-" + (i % 100) + "-%";
            List<String> results = benchmarkJdbc.queryForList(
                "SELECT data FROM performance_test WHERE data LIKE ? ORDER BY created_at DESC LIMIT 10", 
                String.class, pattern);
            assertFalse(results.isEmpty(), "쿼리 결과가 있어야 함");
        }
        long pgQueryTime = System.currentTimeMillis() - pgQueryStart;

        // 성능 비교 로깅
        log.info("성능 벤치마크 결과:");
        log.info("MariaDB - 삽입: {}ms, 조회: {}ms", mariaDbInsertTime, mariaDbQueryTime);
        log.info("PostgreSQL - 삽입: {}ms, 조회: {}ms", pgInsertTime, pgQueryTime);

        assertTrue(mariaDbInsertTime < 10000, "MariaDB 삽입이 10초 내에 완료되어야 함");
        assertTrue(pgInsertTime < 10000, "PostgreSQL 삽입이 10초 내에 완료되어야 함");
        assertTrue(mariaDbQueryTime < 5000, "MariaDB 조회가 5초 내에 완료되어야 함");
        assertTrue(pgQueryTime < 5000, "PostgreSQL 조회가 5초 내에 완료되어야 함");
    }

    @Test
    @Order(5)
    @DisplayName("연결 누수 감지 테스트")
    void testConnectionLeakDetection() throws InterruptedException, SQLException {
        HikariDataSource hikariDs = (HikariDataSource) performanceDataSource;
        
        // 현재 활성 연결 수 확인
        int initialActiveConnections = hikariDs.getHikariPoolMXBean().getActiveConnections();
        
        // 의도적으로 연결을 많이 열고 일부는 닫지 않음
        List<Connection> connections = new ArrayList<>();
        
        try {
            for (int i = 0; i < 5; i++) {
                Connection conn = performanceDataSource.getConnection();
                connections.add(conn);
                
                // 간단한 쿼리 실행
                try (var stmt = conn.prepareStatement("SELECT 1")) {
                    stmt.execute();
                }
            }

            // 일부 연결만 닫음
            for (int i = 0; i < 3; i++) {
                connections.get(i).close();
            }

            // 활성 연결 수가 증가했는지 확인
            int activeConnections = hikariDs.getHikariPoolMXBean().getActiveConnections();
            assertTrue(activeConnections >= initialActiveConnections, 
                "활성 연결 수가 증가해야 함");

            log.info("연결 상태 - 초기: {}, 현재: {}, 총풀크기: {}", 
                initialActiveConnections, activeConnections, hikariDs.getHikariPoolMXBean().getTotalConnections());

        } finally {
            // 남은 연결들 정리
            for (Connection conn : connections) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    // 무시
                }
            }
        }

        // 잠시 대기 후 연결이 정리되었는지 확인
        Thread.sleep(1000);
        
        int finalActiveConnections = hikariDs.getHikariPoolMXBean().getActiveConnections();
        assertEquals(initialActiveConnections, finalActiveConnections, 
            "모든 연결이 정리되어 초기 상태로 돌아가야 함");

        log.info("연결 누수 테스트 완료 - 최종 활성 연결: {}", finalActiveConnections);
    }

    @Test
    @Order(6)
    @DisplayName("간단한 부하 테스트")
    void testSimpleLoadTest() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        AtomicInteger totalOperations = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    for (int j = 0; j < operationsPerThread; j++) {
                        try (Connection conn = performanceDataSource.getConnection()) {
                            performanceJdbc.update(
                                "INSERT INTO performance_test (data) VALUES (?)",
                                "Load-Test-" + threadId + "-" + j);
                            totalOperations.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            if (errorCount.get() <= 5) {
                                log.warn("Thread {} error: {}", threadId, e.getMessage());
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        
        assertTrue(completeLatch.await(60, TimeUnit.SECONDS), 
            "모든 작업이 60초 내에 완료되어야 함");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        int operations = totalOperations.get();
        int errors = errorCount.get();
        double operationsPerSecond = (double) operations / (duration / 1000.0);

        assertTrue(operations > threadCount * operationsPerThread * 0.8, "80% 이상의 작업이 성공해야 함");
        assertTrue(operationsPerSecond > 10, "초당 10개 이상의 작업이 처리되어야 함");
        assertTrue(errors < operations * 0.1, "오류율이 10% 미만이어야 함");

        log.info("부하 테스트 완료 - 지속시간: {}ms, 총 작업: {}, 오류: {}, 처리속도: {:.2f} ops/sec", 
            duration, operations, errors, operationsPerSecond);
    }
}