package com.genius.primavera.hikari;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.model.User;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles({"hikari-resource-constrained"})
@DisplayName("HikariCP connection test test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HikariResourceConstrainedPoolTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserMapper userMapper;

    private HikariDataSource hikariDataSource;
    private HikariPoolMXBean poolMXBean;

    @BeforeEach
    void setUp() throws Exception {
        hikariDataSource = (HikariDataSource) dataSource;
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (ResourceConstrainedPool)");
        poolMXBean = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
    }

    @Test
    @Order(1)
    @DisplayName("connection test test should test verification")
    void verifyResourceConstrainedPoolConfiguration() {
        log.info("=== HikariCP connection test test should test ===");
        log.info("test should test: {}", hikariDataSource.getMaximumPoolSize());
        log.info("test test: {}", hikariDataSource.getMinimumIdle());
        log.info("test file: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("test file: {}ms", hikariDataSource.getIdleTimeout());
        log.info("test test: {}ms", hikariDataSource.getMaxLifetime());
        log.info("test connection: {}ms", hikariDataSource.getLeakDetectionThreshold());
        log.info("JMX file: {}", hikariDataSource.isRegisterMbeans());

        Assertions.assertEquals(5, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(2, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(10000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(60000, hikariDataSource.getIdleTimeout());
        Assertions.assertEquals(30000, hikariDataSource.getLeakDetectionThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("connection test processing test")
    void measureBasicProcessingCapabilityUnderConstraints() {
        final int QUERY_COUNT = 100;
        Instant start = Instant.now();

        log.info("=== connection test processing test: {}should test ===", QUERY_COUNT);

        for (int i = 1; i <= QUERY_COUNT; i++) {
            User user = userMapper.findById(1L);
            Assertions.assertNotNull(user);

            if (i % 25 == 0) {
                log.debug("Progress: {}/{} queries completed", i, QUERY_COUNT);
                logPoolStatistics(String.format("connection %d%%", (i * 100) / QUERY_COUNT));
            }
        }

        Duration elapsed = Duration.between(start, Instant.now());
        double queriesPerSecond = QUERY_COUNT / (elapsed.toMillis() / 1000.0);

        log.info("=== connection test processing result ===");
        log.info("{}should test execution test: {}ms", QUERY_COUNT, elapsed.toMillis());
        log.info("test test: {}ms", elapsed.toMillis() / (double) QUERY_COUNT);
        log.info("processing: {} queries/sec", String.format("%.2f", queriesPerSecond));

        logPoolStatistics("test processing test completed should");
    }

    @Test
    @Order(3)
    @DisplayName("test file test test")
    void observeConnectionWaitingBehavior() {
        final int THREAD_COUNT = 10;
        final int QUERIES_PER_THREAD = 5;

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Instant start = Instant.now();

        log.info("=== test file test test: {}should connection vs {}should test ===",
                THREAD_COUNT, hikariDataSource.getMaximumPoolSize());

        CompletableFuture<ThreadWaitResult>[] futures = IntStream.range(0, THREAD_COUNT)
                .mapToObj(threadId -> CompletableFuture.supplyAsync(() -> {
                    Instant threadStart = Instant.now();
                    long totalWaitTime = 0;
                    int successCount = 0;

                    for (int i = 0; i < QUERIES_PER_THREAD; i++) {
                        try {
                            Instant queryStart = Instant.now();
                            User user = userMapper.findById(1L);
                            Duration waitTime = Duration.between(queryStart, Instant.now());

                            Assertions.assertNotNull(user);
                            totalWaitTime += waitTime.toMillis();
                            successCount++;

                            log.debug("Thread-{} Query-{}: testtest {}ms", threadId, i + 1, waitTime.toMillis());
                        } catch (Exception e) {
                            log.warn("Thread-{} Query-{} failed: {}", threadId, i + 1, e.getMessage());
                        }
                    }

                    Duration threadElapsed = Duration.between(threadStart, Instant.now());
                    return new ThreadWaitResult(threadId, threadElapsed, totalWaitTime, successCount);
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());

        ThreadWaitResult[] results = java.util.Arrays.stream(futures)
                .map(CompletableFuture::join)
                .toArray(ThreadWaitResult[]::new);

        int totalSuccess = java.util.Arrays.stream(results).mapToInt(r -> r.successCount).sum();
        double avgWaitTimePerQuery = java.util.Arrays.stream(results)
                .mapToLong(r -> r.totalWaitTime)
                .average().orElse(0.0) / QUERIES_PER_THREAD;
        double avgThreadTime = java.util.Arrays.stream(results)
                .mapToLong(r -> r.threadDuration.toMillis())
                .average().orElse(0.0);

        log.info("=== test test test result ===");
        log.info("should execution test: {}ms", totalElapsed.toMillis());
        log.info("test connection execution test: {}ms", String.format("%.2f", avgThreadTime));
        log.info("test test: {}ms", String.format("%.2f", avgWaitTimePerQuery));
        log.info("configuration test: {}should", totalSuccess);
        log.info("test file test test");

        logPoolStatistics("test test should");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("test test should connection test")
    void testConnectionLeakDetectionAndCleanup() {
        log.info("=== test test should connection test ===");
        log.info("test connection: {}ms", hikariDataSource.getLeakDetectionThreshold());

        logPoolStatistics("test test should");

        for (int i = 1; i <= 5; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());

            Assertions.assertNotNull(user);
            log.debug("test {}: executiontest {}ms", i, queryTime.toMillis());
        }

        logPoolStatistics("test execution should");

        log.info("test test verification test should... (test file: {}ms)",
                hikariDataSource.getIdleTimeout());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logPoolStatistics("test should test");

        User user = userMapper.findById(1L);
        Assertions.assertNotNull(user);

        logPoolStatistics("test should not should test");

        log.info("connection test connection test verification completed");
    }

    @Test
    @Order(5)
    @DisplayName("connection test should connection verification")
    void verifyMemoryEfficiencyAndConnectionOptimization() {
        log.info("=== connection test should connection verification ===");

        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        logPoolStatistics("connection test");

        final int MODERATE_WORKLOAD = 50;
        Instant start = Instant.now();

        for (int i = 1; i <= MODERATE_WORKLOAD; i++) {
            User user = userMapper.findById(1L);
            Assertions.assertNotNull(user);

            if (i % 10 == 0) {
                logPoolStatistics(String.format("file test: %d/%d", i, MODERATE_WORKLOAD));
            }
        }

        Duration elapsed = Duration.between(start, Instant.now());
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        log.info("=== connection test result ===");
        log.info("file: {}should test", MODERATE_WORKLOAD);
        log.info("execution test: {}ms", elapsed.toMillis());
        log.info("connection test: {}KB", memoryUsed / 1024);
        log.info("test should test: {}should connection test", hikariDataSource.getMaximumPoolSize());

        logPoolStatistics("connection test completed");

        try {
            int totalConnections = poolMXBean.getTotalConnections();
            Assertions.assertTrue(totalConnections <= hikariDataSource.getMaximumPoolSize(),
                    "test should test should test file connection should");
            log.info("test should test verification: should {}should test (test {}should)",
                    totalConnections, hikariDataSource.getMaximumPoolSize());
        } catch (Exception e) {
            log.warn("test should verification failure: {}", e.getMessage());
        }
    }

    private void logPoolStatistics(String phase) {
        try {
            log.info("=== {} should test ===", phase);
            log.info("test should: {}", poolMXBean.getActiveConnections());
            log.info("test should: {}", poolMXBean.getIdleConnections());
            log.info("test should: {}", poolMXBean.getTotalConnections());
            log.info("test should connection should: {}", poolMXBean.getThreadsAwaitingConnection());
        } catch (Exception e) {
            log.warn("should test inquiry failure: {}", e.getMessage());
        }
    }

    private record ThreadWaitResult(int threadId, Duration threadDuration, long totalWaitTime, int successCount) {
    }
}