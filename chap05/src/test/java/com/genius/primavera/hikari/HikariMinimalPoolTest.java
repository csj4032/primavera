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
@DisplayName("HikariCP translated_text_2 translated_text_2 translated_text_2 test")
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
    @DisplayName("translated_text_2 translated_text_1 translated_text_2 translated_text_2 verification")
    void verifyPoolConfiguration() {
        log.info("=== HikariCP translated_text_2 translated_text_2 translated_text_1 translated_text_2 ===");
        log.info("translated_text_2 translated_text_1 translated_text_2: {}", hikariDataSource.getMaximumPoolSize());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}", hikariDataSource.getMinimumIdle());
        log.info("translated_text_2 translated_text_4: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("translated_text_2 translated_text_4: {}ms", hikariDataSource.getIdleTimeout());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", hikariDataSource.getMaxLifetime());

        Assertions.assertEquals(3, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(1, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(5000, hikariDataSource.getConnectionTimeout());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 translated_text_2")
    void measureSingleConnectionPerformance() {
        Instant start = Instant.now();

        IntStream.rangeClosed(1, 100)
            .forEach(i -> {
                User user = userMapper.findById(1L);
                Assertions.assertNotNull(user);
            });

        Duration elapsed = Duration.between(start, Instant.now());
        log.info("translated_text_2 translated_text_2 100translated_text_1 translated_text_2 execution translated_text_2: {}ms", elapsed.toMillis());

        logPoolStatistics("translated_text_2 translated_text_2 test translated_text_1");
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 test")
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

        log.info("=== translated_text_2 translated_text_2 translated_text_2 test result ===");
        log.info("10translated_text_1 translated_text_3 × 5translated_text_1 translated_text_2 (translated_text_1 50translated_text_1) execution translated_text_2: {}ms", elapsed.toMillis());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", elapsed.toMillis() / 50.0);

        logPoolStatistics("translated_text_2 translated_text_2 translated_text_2 test translated_text_1");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 translated_text_1 translated_text_2 translated_text_2 test")
    void measurePoolSaturationBehavior() {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        Instant start = Instant.now();

        log.info("=== translated_text_2 translated_text_1 translated_text_2 test translated_text_2 (20translated_text_1 translated_text_3 vs 3translated_text_1 translated_text_2 translated_text_2) ===");

        CompletableFuture<Void>[] futures = IntStream.range(0, 20)
            .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                try {
                    Instant threadStart = Instant.now();
                    User user = userMapper.findById(1L);
                    Duration threadElapsed = Duration.between(threadStart, Instant.now());

                    log.info("Thread-{}: translated_text_2 translated_text_2 translated_text_1 translated_text_2 completed translated_text_2: {}ms",
                        threadId, threadElapsed.toMillis());
                    Assertions.assertNotNull(user);
                } catch (Exception e) {
                    log.error("Thread-{}: translated_text_2 failure - {}", threadId, e.getMessage());
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());

        log.info("=== translated_text_2 translated_text_1 translated_text_2 test completed ===");
        log.info("translated_text_1 execution translated_text_2: {}ms", totalElapsed.toMillis());
        log.info("translated_text_2 translated_text_2 translated_text_1 translated_text_4 translated_text_2 translated_text_2 translated_text_2 translated_text_2");

        logPoolStatistics("translated_text_1 translated_text_2 test translated_text_1");
        executor.shutdown();
    }

    private void logPoolStatistics(String phase) {
        try {
            log.info("=== {} translated_text_1 translated_text_2 ===", phase);
            log.info("translated_text_2 translated_text_2 translated_text_1: {}", poolMXBean.getActiveConnections());
            log.info("translated_text_2 translated_text_2 translated_text_1: {}", poolMXBean.getIdleConnections());
            log.info("translated_text_1 translated_text_2 translated_text_1: {}", poolMXBean.getTotalConnections());
            log.info("translated_text_2 translated_text_2 translated_text_3 translated_text_1: {}", poolMXBean.getThreadsAwaitingConnection());
        } catch (Exception e) {
            log.warn("translated_text_1 translated_text_2 translated_text_1 failure: {}", e.getMessage());
        }
    }
}