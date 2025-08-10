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

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("translated_text_2 translated_text_1 translated_text_2 translated_text_1 translated_text_2 test")
@EnableTestContainers({
        @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "performanceDb"),
        @EnableTestContainers.TestContainer(type = ContainerType.POSTGRESQL, name = "benchmarkDb")
})
public class PerformanceConnectionPoolTest {

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

        log.info("translated_text_2 test translated_text_2 translated_text_3 completed");
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
    @DisplayName("HikariCP translated_text_2 translated_text_1 translated_text_2 validation")
    void testConnectionPoolConfiguration() {
        assertTrue(performanceDataSource instanceof HikariDataSource, "HikariCP translated_text_8 translated_text_1");

        HikariDataSource hikariDs = (HikariDataSource) performanceDataSource;

        assertTrue(hikariDs.getMaximumPoolSize() > 0, "translated_text_2 translated_text_1 translated_text_3 translated_text_2 translated_text_1");
        assertTrue(hikariDs.getMinimumIdle() >= 0, "translated_text_2 translated_text_2 translated_text_2 translated_text_2 translated_text_2 translated_text_1");
        assertTrue(hikariDs.getConnectionTimeout() > 0, "translated_text_2 translated_text_5 translated_text_2 translated_text_1");
        assertTrue(hikariDs.getIdleTimeout() > 0, "translated_text_2 translated_text_5 translated_text_2 translated_text_1");
        assertTrue(hikariDs.getMaxLifetime() > 0, "translated_text_2 translated_text_5 translated_text_2 translated_text_1");

        log.info("HikariCP translated_text_2 - MaxPool: {}, MinIdle: {}, ConnTimeout: {}ms, IdleTimeout: {}ms",
                hikariDs.getMaximumPoolSize(), hikariDs.getMinimumIdle(),
                hikariDs.getConnectionTimeout(), hikariDs.getIdleTimeout());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_2 processing test")
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
        startLatch.countDown();

        assertTrue(completeLatch.await(30, TimeUnit.SECONDS), "all translated_text_3 30translated_text_1 translated_text_2 completed translated_text_1");
        long endTime = System.currentTimeMillis();

        int expectedOperations = threadCount * operationsPerThread;
        assertEquals(expectedOperations, successCount.get(), "all translated_text_2 translated_text_3 translated_text_9 translated_text_1");
        assertEquals(0, errorCount.get(), "translated_text_2 translated_text_6 translated_text_3 translated_text_1");

        Integer recordCount = performanceJdbc.queryForObject("SELECT COUNT(*) FROM performance_test", Integer.class);
        assertEquals(expectedOperations, recordCount, "all translated_text_5 DBtranslated_text_1 translated_text_5 translated_text_1");

        log.info("translated_text_2 translated_text_2 test completed - translated_text_3: {}, translated_text_2/translated_text_3: {}, translated_text_1 success: {}, translated_text_4: {}ms",
                threadCount, operationsPerThread, successCount.get(), endTime - startTime);
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_3 data translated_text_2 translated_text_2 test")
    void testBulkInsertPerformance() {
        int batchSize = 1000;
        int totalRecords = 5000;

        long startTime = System.currentTimeMillis();

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

        Integer finalCount = performanceJdbc.queryForObject("SELECT COUNT(*) FROM performance_test", Integer.class);
        assertTrue(finalCount >= totalRecords, "translated_text_2 " + totalRecords + "translated_text_2 translated_text_4 translated_text_3 translated_text_1");

        double recordsPerSecond = (double) totalRecords / (duration / 1000.0);
        assertTrue(recordsPerSecond > 100, "translated_text_1 100translated_text_1 translated_text_3 translated_text_4 processing translated_text_1");

        log.info("translated_text_3 translated_text_2 translated_text_2 - translated_text_1 translated_text_3: {}, translated_text_4: {}ms, processing: {:.2f} records/sec",
                totalRecords, duration, recordsPerSecond);
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 translated_text_2 translated_text_4 (MariaDB vs PostgreSQL)")
    void testQueryPerformanceBenchmark() {
        int testDataSize = 1000;

        List<String> testData = IntStream.range(0, testDataSize)
                .mapToObj(i -> "Benchmark-Data-" + i + "-" + (i % 100))
                .toList();

        long mariaDbInsertStart = System.currentTimeMillis();
        performanceJdbc.batchUpdate(
                "INSERT INTO performance_test (data) VALUES (?)",
                testData,
                testData.size(),
                (ps, data) -> ps.setString(1, data));
        long mariaDbInsertTime = System.currentTimeMillis() - mariaDbInsertStart;

        long pgInsertStart = System.currentTimeMillis();
        benchmarkJdbc.batchUpdate(
                "INSERT INTO performance_test (data) VALUES (?)",
                testData,
                testData.size(),
                (ps, data) -> ps.setString(1, data));
        long pgInsertTime = System.currentTimeMillis() - pgInsertStart;

        int queryCount = 100;

        long mariaDbQueryStart = System.currentTimeMillis();
        for (int i = 0; i < queryCount; i++) {
            String pattern = "%-Data-" + (i % 100) + "-%";
            List<String> results = performanceJdbc.queryForList(
                    "SELECT data FROM performance_test WHERE data LIKE ? ORDER BY created_at DESC LIMIT 10",
                    String.class, pattern);
            assertFalse(results.isEmpty(), "translated_text_2 translated_text_7 translated_text_3 translated_text_1");
        }
        long mariaDbQueryTime = System.currentTimeMillis() - mariaDbQueryStart;

        long pgQueryStart = System.currentTimeMillis();
        for (int i = 0; i < queryCount; i++) {
            String pattern = "%-Data-" + (i % 100) + "-%";
            List<String> results = benchmarkJdbc.queryForList(
                    "SELECT data FROM performance_test WHERE data LIKE ? ORDER BY created_at DESC LIMIT 10",
                    String.class, pattern);
            assertFalse(results.isEmpty(), "translated_text_2 translated_text_7 translated_text_3 translated_text_1");
        }
        long pgQueryTime = System.currentTimeMillis() - pgQueryStart;

        log.info("translated_text_2 translated_text_4 result:");
        log.info("MariaDB - translated_text_2: {}ms, inquiry: {}ms", mariaDbInsertTime, mariaDbQueryTime);
        log.info("PostgreSQL - translated_text_2: {}ms, inquiry: {}ms", pgInsertTime, pgQueryTime);

        assertTrue(mariaDbInsertTime < 10000, "MariaDB translated_text_2 10translated_text_1 translated_text_2 completed translated_text_1");
        assertTrue(pgInsertTime < 10000, "PostgreSQL translated_text_2 10translated_text_1 translated_text_2 completed translated_text_1");
        assertTrue(mariaDbQueryTime < 5000, "MariaDB inquiry 5translated_text_1 translated_text_2 completed translated_text_1");
        assertTrue(pgQueryTime < 5000, "PostgreSQL inquiry 5translated_text_1 translated_text_2 completed translated_text_1");
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 test")
    void testConnectionLeakDetection() throws InterruptedException, SQLException {
        HikariDataSource hikariDs = (HikariDataSource) performanceDataSource;

        int initialActiveConnections = hikariDs.getHikariPoolMXBean().getActiveConnections();

        List<Connection> connections = new ArrayList<>();

        try {
            for (int i = 0; i < 5; i++) {
                Connection conn = performanceDataSource.getConnection();
                connections.add(conn);

                try (var stmt = conn.prepareStatement("SELECT 1")) {
                    stmt.execute();
                }
            }

            for (int i = 0; i < 3; i++) {
                connections.get(i).close();
            }

            int activeConnections = hikariDs.getHikariPoolMXBean().getActiveConnections();
            assertTrue(activeConnections >= initialActiveConnections,
                    "translated_text_2 translated_text_2 translated_text_2 translated_text_4 translated_text_1");

            log.info("translated_text_2 translated_text_2 - translated_text_1: {}, translated_text_2: {}, translated_text_1translated_text_1: {}",
                    initialActiveConnections, activeConnections, hikariDs.getHikariPoolMXBean().getTotalConnections());

        } finally {
            for (Connection conn : connections) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                }
            }
        }

        Thread.sleep(1000);

        int finalActiveConnections = hikariDs.getHikariPoolMXBean().getActiveConnections();
        assertEquals(initialActiveConnections, finalActiveConnections,
                "all translated_text_2 translated_text_4 translated_text_1 translated_text_2 translated_text_4 translated_text_1");

        log.info("translated_text_2 translated_text_2 test completed - translated_text_2 translated_text_2 translated_text_2: {}", finalActiveConnections);
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_3 translated_text_2 test")
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
                "all translated_text_3 60translated_text_1 translated_text_2 completed translated_text_1");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        int operations = totalOperations.get();
        int errors = errorCount.get();
        double operationsPerSecond = (double) operations / (duration / 1000.0);

        assertTrue(operations > threadCount * operationsPerThread * 0.8, "80% translated_text_3 translated_text_3 translated_text_9 translated_text_1");
        assertTrue(operationsPerSecond > 10, "translated_text_1 10translated_text_1 translated_text_3 translated_text_3 processing translated_text_1");
        assertTrue(errors < operations * 0.1, "translated_text_7 10% translated_text_5 translated_text_1");

        log.info("translated_text_2 test completed - translated_text_4: {}ms, translated_text_1 translated_text_2: {}, error: {}, processing: {:.2f} ops/sec",
                duration, operations, errors, operationsPerSecond);
    }
}