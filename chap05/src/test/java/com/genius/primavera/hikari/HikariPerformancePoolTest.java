package com.genius.primavera.hikari;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.model.User;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
@DisplayName("HikariCP test connection test")
@ActiveProfiles("hikari-performance-focused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HikariPerformancePoolTest {

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
        ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (PerformanceHikariPool)");
        poolMXBean = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
    }

    @Test
    @Order(1)
    @DisplayName("test connection test should test verification")
    void verifyPerformancePoolConfiguration() {
        log.info("=== HikariCP test connection test should test ===");
        log.info("test should test: {}", hikariDataSource.getMaximumPoolSize());
        log.info("test test: {}", hikariDataSource.getMinimumIdle());
        log.info("test file: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("test file: {}ms", hikariDataSource.getIdleTimeout());
        log.info("test test: {}ms", hikariDataSource.getMaxLifetime());
        log.info("test connection: {}ms (0=file)", hikariDataSource.getLeakDetectionThreshold());

        Assertions.assertEquals(20, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(10, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(20000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(0, hikariDataSource.getLeakDetectionThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("test test processing test")
    void measureHighVolumeQueryPerformance() {
        final int QUERY_COUNT = 1000;
        Instant start = Instant.now();
        log.info("=== test test test: {}should test ===", QUERY_COUNT);
        IntStream.rangeClosed(1, QUERY_COUNT)
                .parallel()
                .forEach(i -> {
                    User user = userMapper.findById(1L);
                    Assertions.assertNotNull(user);
                    if (i % 200 == 0) {
                        log.debug("Progress: {}/{} queries completed", i, QUERY_COUNT);
                    }
                });

        Duration elapsed = Duration.between(start, Instant.now());
        double queriesPerSecond = QUERY_COUNT / (elapsed.toMillis() / 1000.0);
        log.info("=== test test result ===");
        log.info("{}should test execution test: {}ms", QUERY_COUNT, elapsed.toMillis());
        log.info("test test: {}ms", elapsed.toMillis() / (double) QUERY_COUNT);
        log.info("processing: {} queries/sec", String.format("%.2f", queriesPerSecond));
        logPoolStatistics("test test should");
    }

    @Test
    @Order(3)
    @DisplayName("test connection file test")
    void measureMaximumConcurrencyStressTest() {
        final int THREAD_COUNT = 30;
        final int QUERIES_PER_THREAD = 20;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Instant start = Instant.now();
        log.info("=== test connection file test: {}should connection × {}should test ===", THREAD_COUNT, QUERIES_PER_THREAD);
        CompletableFuture<ThreadResult>[] futures = IntStream.range(0, THREAD_COUNT)
                .mapToObj(threadId -> CompletableFuture.supplyAsync(() -> {
                    Instant threadStart = Instant.now();
                    int successCount = 0;
                    int errorCount = 0;

                    for (int i = 0; i < QUERIES_PER_THREAD; i++) {
                        try {
                            User user = userMapper.findById(1L);
                            Assertions.assertNotNull(user);
                            successCount++;
                        } catch (Exception e) {
                            errorCount++;
                            log.warn("Thread-{} Query-{} failed: {}", threadId, i + 1, e.getMessage());
                        }
                    }

                    Duration threadElapsed = Duration.between(threadStart, Instant.now());
                    return new ThreadResult(threadId, threadElapsed, successCount, errorCount);
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());
        ThreadResult[] results = java.util.Arrays.stream(futures).map(CompletableFuture::join).toArray(ThreadResult[]::new);
        int totalSuccess = java.util.Arrays.stream(results).mapToInt(r -> r.successCount).sum();
        int totalErrors = java.util.Arrays.stream(results).mapToInt(r -> r.errorCount).sum();
        double avgThreadTime = java.util.Arrays.stream(results).mapToLong(r -> r.duration.toMillis()).average().orElse(0.0);
        double totalQueriesPerSecond = totalSuccess / (totalElapsed.toMillis() / 1000.0);
        log.info("=== test connection file test result ===");
        log.info("should execution test: {}ms", totalElapsed.toMillis());
        log.info("test connection execution test: {}ms", String.format("%.2f", avgThreadTime));
        log.info("configuration test: {}should", totalSuccess);
        log.info("configuration test: {}should", totalErrors);
        log.info("configuration: {}%", String.format("%.2f", (totalSuccess * 100.0) / (totalSuccess + totalErrors)));
        log.info("test processing: {} queries/sec", String.format("%.2f", totalQueriesPerSecond));
        logPoolStatistics("test connection test should");
        executor.shutdown();
        Assertions.assertTrue(totalQueriesPerSecond > 50, "test connection test 50 queries/sec test processing test");
    }

    @Test
    @Order(4)
    @DisplayName("test creation file test verification")
    void verifyMinimalConnectionCreationOverhead() {
        log.info("=== test creation file test ===");
        logPoolStatistics("test should (should creation test verification)");
        long[] queryTimes = new long[10];
        for (int i = 0; i < 10; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());
            queryTimes[i] = queryTime.toMillis();
            Assertions.assertNotNull(user);
            log.debug("test {}: {}ms", i + 1, queryTime.toMillis());
        }

        double avgQueryTime = java.util.Arrays.stream(queryTimes).average().orElse(0.0);
        long maxQueryTime = java.util.Arrays.stream(queryTimes).max().orElse(0L);
        long minQueryTime = java.util.Arrays.stream(queryTimes).min().orElse(0L);
        log.info("=== test creation file test result ===");
        log.info("test test: {}ms", String.format("%.2f", avgQueryTime));
        log.info("test test: {}ms", maxQueryTime);
        log.info("test test: {}ms", minQueryTime);
        log.info("test: {}ms", maxQueryTime - minQueryTime);
        logPoolStatistics("test file test should");
        Assertions.assertTrue(avgQueryTime < 50, "should creation test test 50ms test test");
    }

    private void logPoolStatistics(String phase) {
        try {
            log.info("=== {} should test ===", phase);
            log.info("test should: {}", poolMXBean.getActiveConnections());
            log.info("test should: {}", poolMXBean.getIdleConnections());
            log.info("test should: {}", poolMXBean.getTotalConnections());
            log.info("test connection should: {}", poolMXBean.getThreadsAwaitingConnection());
        } catch (Exception e) {
            log.warn("should test inquiry failure: {}", e.getMessage());
        }
    }

    private record ThreadResult(int threadId, Duration duration, int successCount, int errorCount) {
    }
}