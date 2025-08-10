package com.genius.primavera.transaction;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.testcontainers.EnableTestContainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@EnableTestContainers
@ActiveProfiles({"test"})
@DisplayName("ACID test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ACIDPropertiesTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WinnerMapper winnerMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test" + System.currentTimeMillis() + "@primavera.com")
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("TestUser")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    @Order(1)
    @DisplayName("connection(Atomicity) test - file test should all Endpoint connection")
    void testAtomicity() {
        Long userId = userMapper.save(testUser);
        long initialUserCount = userMapper.count();
        long initialWinnerCount = winnerMapper.count();
        log.info("test - User should: {}, Winner should: {}", initialUserCount, initialWinnerCount);
        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                User user = userMapper.findById(userId);
                user.setStatus(UserStatus.INACTIVE);
                userMapper.update(user);
                log.info("User test INACTIVEshould test: {}", user.getId());
                Winner winner = Winner.builder().name("Test Winner").year(2023).sport("Test Sport").prize("Test Prize").amount(new BigDecimal("1000.00")).build();
                winnerMapper.save(winner);
                log.info("Winner data test: ID={}, Name={}", winner.getId(), winner.getName());
                throw new RuntimeException("file exception test - connection test");
            });
        }).isInstanceOf(RuntimeException.class).hasMessage("file exception test - connection test");
        long finalUserCount = userMapper.count();
        long finalWinnerCount = winnerMapper.count();
        log.info("test should test - User should: {}, Winner should: {}", finalUserCount, finalWinnerCount);
        assertThat(finalUserCount).isEqualTo(initialUserCount);
        assertThat(finalWinnerCount).isEqualTo(initialWinnerCount);
    }

    @Test
    @Order(2)
    @DisplayName("connection(Consistency) test - file test should file failure")
    void testConsistency() {
        User duplicateUser = User.builder()
                .email(testUser.getEmail())
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("DUPLICATE_USER")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                userMapper.save(duplicateUser);
                return null;
            });
        }).isInstanceOf(DataAccessException.class);
        long userCount = userMapper.countByEmail(testUser.getEmail());
        assertThat(userCount).isEqualTo(1);
        log.info("connection test - test connection test connection");
    }

    @Test
    @Order(3)
    @DisplayName("connection(Isolation) test - should file should test")
    void testIsolation() throws InterruptedException {
        Long userId = userMapper.save(testUser);
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return isolatedTransaction1(userId, latch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Thread1 interrupted";
            }
        }, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return isolatedTransaction2(userId, latch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Thread2 interrupted";
            }
        }, executor);
        String result1 = future1.join();
        String result2 = future2.join();
        log.info("connection test result - Thread1: {}, Thread2: {}", result1, result2);
        assertThat(result1).contains("Transaction1");
        assertThat(result2).contains("Transaction2");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String isolatedTransaction1(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("Transaction1 should - User ID: {}", userId);
        User user = userMapper.findById(userId);
        user.setNickname("TRANSACTION1_USER");
        userMapper.update(user);
        latch.countDown();
        latch.await();
        Thread.sleep(100);
        log.info("Transaction1 completed");
        return "Transaction1 completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String isolatedTransaction2(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("Transaction2 should - User ID: {}", userId);
        User user = userMapper.findById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userMapper.update(user);
        latch.countDown();
        latch.await();
        Thread.sleep(100);
        log.info("Transaction2 completed");
        return "Transaction2 completed";
    }

    @Test
    @Order(4)
    @DisplayName("test(Durability) test - connection file needs to be added should test")
    void testDurability() {
        Long userId = userMapper.save(testUser);
        transactionTemplate.execute(status -> {
            User user = userMapper.findById(userId);
            log.info("test - file should User test: nickname={}, status={}", user.getNickname(), user.getStatus());
            user.setNickname("DURABLE_USER");
            user.setStatus(UserStatus.INACTIVE);
            userMapper.update(user);
            log.info("test - User file completed: nickname={}, status={}", user.getNickname(), user.getStatus());
            return null;
        });
        User updatedUser = userMapper.findById(userId);
        log.info("test - test should User test: nickname={}, status={}", updatedUser.getNickname(), updatedUser.getStatus());
        assertThat(updatedUser.getNickname()).isEqualTo("DURABLE_USER");
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        TransactionTemplate newTransactionTemplate = new TransactionTemplate(transactionManager);
        newTransactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        newTransactionTemplate.execute(status -> {
            User user = userMapper.findById(userId);
            assertThat(user.getNickname()).isEqualTo("DURABLE_USER");
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
            log.info("should file verification - test connection: {}", user.getNickname());
            return null;
        });
        log.info("test test - Endpoint connection");
    }
}