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
@DisplayName("HikariCP translated_text_2 translated_text_3 translated_text_2 test")
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
    @DisplayName("translated_text_2 translated_text_3 translated_text_2 translated_text_1 translated_text_2 verification")
    void verifyPerformancePoolConfiguration() {
        log.info("=== HikariCP translated_text_2 translated_text_3 translated_text_2 translated_text_1 translated_text_2 ===");
        log.info("translated_text_2 translated_text_1 translated_text_2: {}", hikariDataSource.getMaximumPoolSize());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}", hikariDataSource.getMinimumIdle());
        log.info("translated_text_2 translated_text_4: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("translated_text_2 translated_text_4: {}ms", hikariDataSource.getIdleTimeout());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", hikariDataSource.getMaxLifetime());
        log.info("translated_text_2 translated_text_2 translated_text_3: {}ms (0=translated_text_4)", hikariDataSource.getLeakDetectionThreshold());

        Assertions.assertEquals(20, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(10, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(20000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(0, hikariDataSource.getLeakDetectionThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 processing test")
    void measureHighVolumeQueryPerformance() {
        final int QUERY_COUNT = 1000;
        Instant start = Instant.now();
        log.info("=== translated_text_2 translated_text_2 translated_text_2 test translated_text_2: {}translated_text_1 translated_text_2 ===", QUERY_COUNT);
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
        log.info("=== translated_text_2 translated_text_2 translated_text_2 test result ===");
        log.info("{}translated_text_1 translated_text_2 execution translated_text_2: {}ms", QUERY_COUNT, elapsed.toMillis());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", elapsed.toMillis() / (double) QUERY_COUNT);
        log.info("processing: {} queries/sec", String.format("%.2f", queriesPerSecond));
        logPoolStatistics("translated_text_2 translated_text_2 test translated_text_1");
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 translated_text_3 translated_text_4 test")
    void measureMaximumConcurrencyStressTest() {
        final int THREAD_COUNT = 30;
        final int QUERIES_PER_THREAD = 20;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Instant start = Instant.now();
        log.info("=== translated_text_2 translated_text_3 translated_text_4 test translated_text_2: {}translated_text_1 translated_text_3 × {}translated_text_1 translated_text_2 ===", THREAD_COUNT, QUERIES_PER_THREAD);
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
        log.info("=== translated_text_2 translated_text_3 translated_text_4 test result ===");
        log.info("translated_text_1 execution translated_text_2: {}ms", totalElapsed.toMillis());
        log.info("translated_text_2 translated_text_3 execution translated_text_2: {}ms", String.format("%.2f", avgThreadTime));
        log.info("translated_text_8 translated_text_2: {}translated_text_1", totalSuccess);
        log.info("translated_text_8 translated_text_2: {}translated_text_1", totalErrors);
        log.info("translated_text_8: {}%", String.format("%.2f", (totalSuccess * 100.0) / (totalSuccess + totalErrors)));
        log.info("translated_text_2 processing: {} queries/sec", String.format("%.2f", totalQueriesPerSecond));
        logPoolStatistics("translated_text_2 translated_text_3 test translated_text_1");
        executor.shutdown();
        Assertions.assertTrue(totalQueriesPerSecond > 50, "translated_text_2 translated_text_3 translated_text_2 50 queries/sec translated_text_2 processing translated_text_2");
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 creation translated_text_4 translated_text_2 verification")
    void verifyMinimalConnectionCreationOverhead() {
        log.info("=== translated_text_2 creation translated_text_4 translated_text_2 test ===");
        logPoolStatistics("test translated_text_2 translated_text_1 (translated_text_1 creation translated_text_2 verification)");
        long[] queryTimes = new long[10];
        for (int i = 0; i < 10; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());
            queryTimes[i] = queryTime.toMillis();
            Assertions.assertNotNull(user);
            log.debug("translated_text_2 translated_text_2 {}: {}ms", i + 1, queryTime.toMillis());
        }

        double avgQueryTime = java.util.Arrays.stream(queryTimes).average().orElse(0.0);
        long maxQueryTime = java.util.Arrays.stream(queryTimes).max().orElse(0L);
        long minQueryTime = java.util.Arrays.stream(queryTimes).min().orElse(0L);
        log.info("=== translated_text_2 creation translated_text_4 test result ===");
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", String.format("%.2f", avgQueryTime));
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", maxQueryTime);
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", minQueryTime);
        log.info("translated_text_2 translated_text_2: {}ms", maxQueryTime - minQueryTime);
        logPoolStatistics("translated_text_2 translated_text_4 test translated_text_1");
        Assertions.assertTrue(avgQueryTime < 50, "translated_text_1 creation translated_text_2 translated_text_2 translated_text_2 50ms translated_text_2 translated_text_2 translated_text_2 translated_text_2");
    }

    private void logPoolStatistics(String phase) {
        try {
            log.info("=== {} translated_text_1 translated_text_2 ===", phase);
            log.info("translated_text_2 translated_text_2 translated_text_1: {}", poolMXBean.getActiveConnections());
            log.info("translated_text_2 translated_text_2 translated_text_1: {}", poolMXBean.getIdleConnections());
            log.info("translated_text_1 translated_text_2 translated_text_1: {}", poolMXBean.getTotalConnections());
            log.info("translated_text_2 translated_text_2 translated_text_3 translated_text_1: {}", poolMXBean.getThreadsAwaitingConnection());
        } catch (Exception e) {
            log.warn("translated_text_1 translated_text_2 inquiry failure: {}", e.getMessage());
        }
    }

    private record ThreadResult(int threadId, Duration duration, int successCount, int errorCount) {
    }
}