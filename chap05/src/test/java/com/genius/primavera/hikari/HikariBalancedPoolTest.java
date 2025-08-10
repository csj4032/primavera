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
@DisplayName("HikariCP translated_text_4 translated_text_2 translated_text_2 test")
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
    @DisplayName("translated_text_2 translated_text_1 translated_text_2 translated_text_2 verification")
    void verifyBalancedPoolConfiguration() {
        log.info("=== HikariCP translated_text_4 translated_text_2 translated_text_1 translated_text_2 ===");
        log.info("translated_text_2 translated_text_1 translated_text_2: {}", hikariDataSource.getMaximumPoolSize());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}", hikariDataSource.getMinimumIdle());
        log.info("translated_text_2 translated_text_4: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("translated_text_2 translated_text_4: {}ms", hikariDataSource.getIdleTimeout());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", hikariDataSource.getMaxLifetime());
        log.info("translated_text_2 translated_text_2 translated_text_3: {}ms", hikariDataSource.getLeakDetectionThreshold());

        Assertions.assertEquals(10, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(5, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(30000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(60000, hikariDataSource.getLeakDetectionThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_4 translated_text_2 translated_text_1 translated_text_3 translated_text_4 test")
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
        log.info("=== translated_text_3 translated_text_4 test result ===");
        log.info("200translated_text_1 translated_text_2 execution translated_text_2: {}ms", elapsed.toMillis());
        log.info("translated_text_2 translated_text_2 translated_text_2: {}ms", elapsed.toMillis() / 200.0);

        logPoolStatistics("translated_text_3 translated_text_4 test translated_text_1");
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 translated_text_2 translated_text_3 test")
    void measureModerateConcurrencyPerformance() {
        ExecutorService executor = Executors.newFixedThreadPool(15);
        Instant start = Instant.now();

        log.info("=== translated_text_2 translated_text_2 translated_text_3 test (15translated_text_1 translated_text_3 vs 10translated_text_1 translated_text_2 translated_text_2) ===");

        CompletableFuture<Duration>[] futures = IntStream.range(0, 15)
                .mapToObj(threadId -> CompletableFuture.supplyAsync(() -> {
                    Instant threadStart = Instant.now();

                    for (int i = 0; i < 10; i++) {
                        User user = userMapper.findById(1L);
                        Assertions.assertNotNull(user);
                    }
                    Duration threadElapsed = Duration.between(threadStart, Instant.now());
                    log.debug("Thread-{}: 10translated_text_1 translated_text_2 completed translated_text_2: {}ms", threadId, threadElapsed.toMillis());
                    return threadElapsed;
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());

        double avgThreadTime = java.util.Arrays.stream(futures)
                .mapToLong(f -> f.join().toMillis())
                .average()
                .orElse(0.0);

        log.info("=== translated_text_2 translated_text_2 translated_text_3 test result ===");
        log.info("translated_text_1 execution translated_text_2: {}ms", totalElapsed.toMillis());
        log.info("translated_text_2 translated_text_3 execution translated_text_2: {}ms", avgThreadTime);
        log.info("translated_text_1 translated_text_2 translated_text_1: 150translated_text_1 (15 translated_text_3 × 10 translated_text_2)");
        log.info("translated_text_2 translated_text_11: {} queries/sec", 150.0 / (totalElapsed.toMillis() / 1000.0));

        logPoolStatistics("translated_text_2 translated_text_3 test translated_text_1");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 validation translated_text_1 translated_text_2 test")
    void testConnectionValidationAndRecovery() {
        log.info("=== translated_text_2 validation translated_text_1 translated_text_2 test ===");

        logPoolStatistics("validation test translated_text_2 translated_text_1");

        for (int i = 1; i <= 5; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());

            Assertions.assertNotNull(user);
            log.info("validation translated_text_2 {}: execution translated_text_2 {}ms", i, queryTime.toMillis());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logPoolStatistics("validation test completed translated_text_1");
        log.info("translated_text_2 validation translated_text_2(SELECT 1) translated_text_2 verification completed");
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