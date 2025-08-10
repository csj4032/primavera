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
@ActiveProfiles("hikari-minimal")
@DisplayName("HikariCP test test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HikariMinimalPoolTest {

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
        ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (MinimalHikariPool)");
        poolMXBean = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
    }

    @Test
    @Order(1)
    @DisplayName("test should test verification")
    void verifyPoolConfiguration() {
        log.info("=== HikariCP test should test ===");
        log.info("test should test: {}", hikariDataSource.getMaximumPoolSize());
        log.info("test test: {}", hikariDataSource.getMinimumIdle());
        log.info("test file: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("test file: {}ms", hikariDataSource.getIdleTimeout());
        log.info("test test: {}ms", hikariDataSource.getMaxLifetime());

        Assertions.assertEquals(3, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(1, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(5000, hikariDataSource.getConnectionTimeout());
    }

    @Test
    @Order(2)
    @DisplayName("test test")
    void measureSingleConnectionPerformance() {
        Instant start = Instant.now();

        IntStream.rangeClosed(1, 100)
            .forEach(i -> {
                User user = userMapper.findById(1L);
                Assertions.assertNotNull(user);
            });

        Duration elapsed = Duration.between(start, Instant.now());
        log.info("test 100should test execution test: {}ms", elapsed.toMillis());

        logPoolStatistics("test test should");
    }

    @Test
    @Order(3)
    @DisplayName("test test")
    void measureConcurrentConnectionContentionTest() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Instant start = Instant.now();

        CompletableFuture<Void>[] futures = IntStream.range(0, 10)
            .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                for (int i = 0; i < 5; i++) {
                    User user = userMapper.findById(1L);
                    Assertions.assertNotNull(user);
                    log.debug("Thread-{}: Query-{} completed", threadId, i + 1);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration elapsed = Duration.between(start, Instant.now());

        log.info("=== test test result ===");
        log.info("10should connection × 5should test (should 50should) execution test: {}ms", elapsed.toMillis());
        log.info("test test: {}ms", elapsed.toMillis() / 50.0);

        logPoolStatistics("test test should");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("test should test test")
    void measurePoolSaturationBehavior() {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        Instant start = Instant.now();

        log.info("=== test should test test (20should connection vs 3should test) ===");

        CompletableFuture<Void>[] futures = IntStream.range(0, 20)
            .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                try {
                    Instant threadStart = Instant.now();
                    User user = userMapper.findById(1L);
                    Duration threadElapsed = Duration.between(threadStart, Instant.now());

                    log.info("Thread-{}: test should test completed test: {}ms",
                        threadId, threadElapsed.toMillis());
                    Assertions.assertNotNull(user);
                } catch (Exception e) {
                    log.error("Thread-{}: test failure - {}", threadId, e.getMessage());
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());

        log.info("=== test should test completed ===");
        log.info("should execution test: {}ms", totalElapsed.toMillis());
        log.info("test should file test test");

        logPoolStatistics("should test should");
        executor.shutdown();
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