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
@DisplayName("HikariCP translated_text_3 translated_text_2 translated_text_2 translated_text_2 test")
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
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 translated_text_2 translated_text_1 translated_text_2 verification")
    void verifyResourceConstrainedPoolConfiguration() {
        log.info("=== HikariCP translated_text_3 translated_text_2 translated_text_2 translated_text_2 translated_text_1 translated_text_2 ===");
        log.info("translated_text_2 translated_text_1 translated_text_2: {}", hikariDataSource.getMaximumPoolSize());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}", hikariDataSource.getMinimumIdle());
        log.info("translated_text_2 translated_text_4: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("translated_text_2 translated_text_4: {}ms", hikariDataSource.getIdleTimeout());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", hikariDataSource.getMaxLifetime());
        log.info("translated_text_2 translated_text_2 translated_text_3: {}ms", hikariDataSource.getLeakDetectionThreshold());
        log.info("JMX translated_text_4: {}", hikariDataSource.isRegisterMbeans());

        Assertions.assertEquals(5, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(2, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(10000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(60000, hikariDataSource.getIdleTimeout());
        Assertions.assertEquals(30000, hikariDataSource.getLeakDetectionThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_3 translated_text_3 translated_text_2 translated_text_2 processing translated_text_2 test")
    void measureBasicProcessingCapabilityUnderConstraints() {
        final int QUERY_COUNT = 100;
        Instant start = Instant.now();

        log.info("=== translated_text_3 translated_text_3 translated_text_2 translated_text_2 processing test: {}translated_text_1 translated_text_2 ===", QUERY_COUNT);

        for (int i = 1; i <= QUERY_COUNT; i++) {
            User user = userMapper.findById(1L);
            Assertions.assertNotNull(user);

            if (i % 25 == 0) {
                log.debug("Progress: {}/{} queries completed", i, QUERY_COUNT);
                logPoolStatistics(String.format("translated_text_3 %d%%", (i * 100) / QUERY_COUNT));
            }
        }

        Duration elapsed = Duration.between(start, Instant.now());
        double queriesPerSecond = QUERY_COUNT / (elapsed.toMillis() / 1000.0);

        log.info("=== translated_text_3 translated_text_3 translated_text_2 translated_text_2 processing result ===");
        log.info("{}translated_text_1 translated_text_2 execution translated_text_2: {}ms", QUERY_COUNT, elapsed.toMillis());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", elapsed.toMillis() / (double) QUERY_COUNT);
        log.info("processing: {} queries/sec", String.format("%.2f", queriesPerSecond));

        logPoolStatistics("translated_text_2 processing test completed translated_text_1");
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 translated_text_4 translated_text_2 translated_text_2 translated_text_2 translated_text_2")
    void observeConnectionWaitingBehavior() {
        final int THREAD_COUNT = 10;
        final int QUERIES_PER_THREAD = 5;

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Instant start = Instant.now();

        log.info("=== translated_text_2 translated_text_4 translated_text_2 translated_text_2 translated_text_2 translated_text_2: {}translated_text_1 translated_text_3 vs {}translated_text_1 translated_text_2 translated_text_2 ===",
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

                            log.debug("Thread-{} Query-{}: translated_text_2translated_text_2 {}ms", threadId, i + 1, waitTime.toMillis());
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

        log.info("=== translated_text_2 translated_text_2 translated_text_2 translated_text_2 translated_text_2 result ===");
        log.info("translated_text_1 execution translated_text_2: {}ms", totalElapsed.toMillis());
        log.info("translated_text_2 translated_text_3 execution translated_text_2: {}ms", String.format("%.2f", avgThreadTime));
        log.info("translated_text_2 translated_text_2 translated_text_2 translated_text_2: {}ms", String.format("%.2f", avgWaitTimePerQuery));
        log.info("translated_text_8 translated_text_2: {}translated_text_1", totalSuccess);
        log.info("translated_text_2 translated_text_4 translated_text_2 translated_text_2 translated_text_2 translated_text_2");

        logPoolStatistics("translated_text_2 translated_text_2 test translated_text_1");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 translated_text_1 translated_text_3 translated_text_2 test")
    void testConnectionLeakDetectionAndCleanup() {
        log.info("=== translated_text_2 translated_text_2 translated_text_2 translated_text_1 translated_text_3 translated_text_2 test ===");
        log.info("translated_text_2 translated_text_2 translated_text_3: {}ms", hikariDataSource.getLeakDetectionThreshold());

        logPoolStatistics("translated_text_2 translated_text_2 test translated_text_2 translated_text_1");

        for (int i = 1; i <= 5; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());

            Assertions.assertNotNull(user);
            log.debug("translated_text_2 translated_text_2 {}: executiontranslated_text_2 {}ms", i, queryTime.toMillis());
        }

        logPoolStatistics("translated_text_2 translated_text_2 execution translated_text_1");

        log.info("translated_text_2 translated_text_2 translated_text_2 translated_text_2 verification translated_text_2 translated_text_2 translated_text_1... (translated_text_2 translated_text_4: {}ms)",
                hikariDataSource.getIdleTimeout());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logPoolStatistics("translated_text_2 translated_text_1 translated_text_2");

        User user = userMapper.findById(1L);
        Assertions.assertNotNull(user);

        logPoolStatistics("translated_text_2 translated_text_9 translated_text_1 translated_text_2 translated_text_2");

        log.info("translated_text_3 translated_text_2 translated_text_2 translated_text_3 translated_text_2 translated_text_2 verification completed");
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_3 translated_text_3 translated_text_1 translated_text_2 translated_text_1 translated_text_3 verification")
    void verifyMemoryEfficiencyAndConnectionOptimization() {
        log.info("=== translated_text_3 translated_text_3 translated_text_1 translated_text_2 translated_text_1 translated_text_3 verification ===");

        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        logPoolStatistics("translated_text_3 translated_text_3 test translated_text_2");

        final int MODERATE_WORKLOAD = 50;
        Instant start = Instant.now();

        for (int i = 1; i <= MODERATE_WORKLOAD; i++) {
            User user = userMapper.findById(1L);
            Assertions.assertNotNull(user);

            if (i % 10 == 0) {
                logPoolStatistics(String.format("translated_text_4 translated_text_2: %d/%d", i, MODERATE_WORKLOAD));
            }
        }

        Duration elapsed = Duration.between(start, Instant.now());
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        log.info("=== translated_text_3 translated_text_3 test result ===");
        log.info("translated_text_4: {}translated_text_1 translated_text_2", MODERATE_WORKLOAD);
        log.info("execution translated_text_2: {}ms", elapsed.toMillis());
        log.info("translated_text_3 translated_text_3 translated_text_2: {}KB", memoryUsed / 1024);
        log.info("translated_text_2 translated_text_2 translated_text_1 translated_text_2: {}translated_text_1 translated_text_3 translated_text_3 translated_text_2", hikariDataSource.getMaximumPoolSize());

        logPoolStatistics("translated_text_3 translated_text_3 test completed");

        try {
            int totalConnections = poolMXBean.getTotalConnections();
            Assertions.assertTrue(totalConnections <= hikariDataSource.getMaximumPoolSize(),
                    "translated_text_1 translated_text_2 translated_text_1 translated_text_2 translated_text_1 translated_text_2 translated_text_4 translated_text_3 translated_text_1");
            log.info("translated_text_2 translated_text_1 translated_text_2 verification: translated_text_1 {}translated_text_1 translated_text_2 (translated_text_2 {}translated_text_1)",
                    totalConnections, hikariDataSource.getMaximumPoolSize());
        } catch (Exception e) {
            log.warn("translated_text_2 translated_text_1 verification failure: {}", e.getMessage());
        }
    }

    private void logPoolStatistics(String phase) {
        try {
            log.info("=== {} translated_text_1 translated_text_2 ===", phase);
            log.info("translated_text_2 translated_text_2 translated_text_1: {}", poolMXBean.getActiveConnections());
            log.info("translated_text_2 translated_text_2 translated_text_1: {}", poolMXBean.getIdleConnections());
            log.info("translated_text_1 translated_text_2 translated_text_1: {}", poolMXBean.getTotalConnections());
            log.info("translated_text_2 translated_text_1 translated_text_3 translated_text_1: {}", poolMXBean.getThreadsAwaitingConnection());
        } catch (Exception e) {
            log.warn("translated_text_1 translated_text_2 inquiry failure: {}", e.getMessage());
        }
    }

    private record ThreadWaitResult(int threadId, Duration threadDuration, long totalWaitTime, int successCount) {
    }
}