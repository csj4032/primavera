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
@ActiveProfiles("hikari-balanced")
@DisplayName("HikariCP file test test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HikariBalancedPoolTest {

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
        ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (BalancedHikariPool)");
        poolMXBean = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
    }

    @Test
    @Order(1)
    @DisplayName("test should test verification")
    void verifyBalancedPoolConfiguration() {
        log.info("=== HikariCP file test should test ===");
        log.info("test should test: {}", hikariDataSource.getMaximumPoolSize());
        log.info("test test: {}", hikariDataSource.getMinimumIdle());
        log.info("test file: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("test file: {}ms", hikariDataSource.getIdleTimeout());
        log.info("test test: {}ms", hikariDataSource.getMaxLifetime());
        log.info("test connection: {}ms", hikariDataSource.getLeakDetectionThreshold());

        Assertions.assertEquals(10, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(5, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(30000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(60000, hikariDataSource.getLeakDetectionThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("file test should connection file test")
    void measureTypicalWorkloadPerformance() {
        Instant start = Instant.now();

        IntStream.rangeClosed(1, 200).forEach(i -> {
            User user = userMapper.findById(1L);
            Assertions.assertNotNull(user);
            if (i % 50 == 0) {
                log.debug("Progress: {}/200 queries completed", i);
            }
        });

        Duration elapsed = Duration.between(start, Instant.now());
        log.info("=== connection file test result ===");
        log.info("200should test execution test: {}ms", elapsed.toMillis());
        log.info("test test: {}ms", elapsed.toMillis() / 200.0);

        logPoolStatistics("connection file test should");
    }

    @Test
    @Order(3)
    @DisplayName("test connection test")
    void measureModerateConcurrencyPerformance() {
        ExecutorService executor = Executors.newFixedThreadPool(15);
        Instant start = Instant.now();

        log.info("=== test connection test (15should connection vs 10should test) ===");

        CompletableFuture<Duration>[] futures = IntStream.range(0, 15)
                .mapToObj(threadId -> CompletableFuture.supplyAsync(() -> {
                    Instant threadStart = Instant.now();

                    for (int i = 0; i < 10; i++) {
                        User user = userMapper.findById(1L);
                        Assertions.assertNotNull(user);
                    }
                    Duration threadElapsed = Duration.between(threadStart, Instant.now());
                    log.debug("Thread-{}: 10should test completed test: {}ms", threadId, threadElapsed.toMillis());
                    return threadElapsed;
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());

        double avgThreadTime = java.util.Arrays.stream(futures)
                .mapToLong(f -> f.join().toMillis())
                .average()
                .orElse(0.0);

        log.info("=== test connection test result ===");
        log.info("should execution test: {}ms", totalElapsed.toMillis());
        log.info("test connection execution test: {}ms", avgThreadTime);
        log.info("test should: 150should (15 connection × 10 test)");
        log.info("test processing: {} queries/sec", 150.0 / (totalElapsed.toMillis() / 1000.0));

        logPoolStatistics("test connection test should");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("test validation should test")
    void testConnectionValidationAndRecovery() {
        log.info("=== test validation should test ===");

        logPoolStatistics("validation test should");

        for (int i = 1; i <= 5; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());

            Assertions.assertNotNull(user);
            log.info("validation test {}: execution test {}ms", i, queryTime.toMillis());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logPoolStatistics("validation test completed should");
        log.info("test validation test(SELECT 1) test verification completed");
    }

    private void logPoolStatistics(String phase) {
        try {
            log.info("=== {} should test ===", phase);
            log.info("test should: {}", poolMXBean.getActiveConnections());
            log.info("test should: {}", poolMXBean.getIdleConnections());
            log.info("test should: {}", poolMXBean.getTotalConnections());
            log.info("test connection should: {}", poolMXBean.getThreadsAwaitingConnection());
        } catch (Exception e) {
            log.warn("test should failure: {}", e.getMessage());
        }
    }
}