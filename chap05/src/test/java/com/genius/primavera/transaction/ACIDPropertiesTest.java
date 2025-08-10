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
@DisplayName("ACID translated_text_2 test")
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
    @DisplayName("translated_text_3(Atomicity) test - translated_text_4 translated_text_2 translated_text_1 all translated_text_5 translated_text_3")
    void testAtomicity() {
        Long userId = userMapper.save(testUser);
        long initialUserCount = userMapper.count();
        long initialWinnerCount = winnerMapper.count();
        log.info("translated_text_2 translated_text_2 - User translated_text_1: {}, Winner translated_text_1: {}", initialUserCount, initialWinnerCount);
        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                User user = userMapper.findById(userId);
                user.setStatus(UserStatus.INACTIVE);
                userMapper.update(user);
                log.info("User translated_text_2 INACTIVEtranslated_text_1 translated_text_2: {}", user.getId());
                Winner winner = Winner.builder().name("Test Winner").year(2023).sport("Test Sport").prize("Test Prize").amount(new BigDecimal("1000.00")).build();
                winnerMapper.save(winner);
                log.info("Winner data translated_text_2: ID={}, Name={}", winner.getId(), winner.getName());
                throw new RuntimeException("translated_text_4 exception translated_text_2 - translated_text_3 test");
            });
        }).isInstanceOf(RuntimeException.class).hasMessage("translated_text_4 exception translated_text_2 - translated_text_3 test");
        long finalUserCount = userMapper.count();
        long finalWinnerCount = winnerMapper.count();
        log.info("translated_text_2 translated_text_1 translated_text_2 - User translated_text_1: {}, Winner translated_text_1: {}", finalUserCount, finalWinnerCount);
        assertThat(finalUserCount).isEqualTo(initialUserCount);
        assertThat(finalWinnerCount).isEqualTo(initialWinnerCount);
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_3(Consistency) test - translated_text_4 translated_text_2 translated_text_2 translated_text_1 translated_text_4 failure")
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
        log.info("translated_text_3 test translated_text_2 - translated_text_2 translated_text_3 translated_text_2 translated_text_3");
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_3(Isolation) test - translated_text_1 translated_text_4 translated_text_1 translated_text_2")
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
        log.info("translated_text_3 test result - Thread1: {}, Thread2: {}", result1, result2);
        assertThat(result1).contains("Transaction1");
        assertThat(result2).contains("Transaction2");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String isolatedTransaction1(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("Transaction1 translated_text_1 - User ID: {}", userId);
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
        log.info("Transaction2 translated_text_1 - User ID: {}", userId);
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
    @DisplayName("translated_text_2(Durability) test - translated_text_3 translated_text_4 translated_text_1 translated_text_1 translated_text_1 translated_text_2")
    void testDurability() {
        Long userId = userMapper.save(testUser);
        transactionTemplate.execute(status -> {
            User user = userMapper.findById(userId);
            log.info("translated_text_2 test - translated_text_4 translated_text_1 User translated_text_2: nickname={}, status={}", user.getNickname(), user.getStatus());
            user.setNickname("DURABLE_USER");
            user.setStatus(UserStatus.INACTIVE);
            userMapper.update(user);
            log.info("translated_text_2 test - User translated_text_4 completed: nickname={}, status={}", user.getNickname(), user.getStatus());
            return null;
        });
        User updatedUser = userMapper.findById(userId);
        log.info("translated_text_2 test - translated_text_2 translated_text_1 User translated_text_2: nickname={}, status={}", updatedUser.getNickname(), updatedUser.getStatus());
        assertThat(updatedUser.getNickname()).isEqualTo("DURABLE_USER");
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        TransactionTemplate newTransactionTemplate = new TransactionTemplate(transactionManager);
        newTransactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        newTransactionTemplate.execute(status -> {
            User user = userMapper.findById(userId);
            assertThat(user.getNickname()).isEqualTo("DURABLE_USER");
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
            log.info("translated_text_1 translated_text_4 verification - translated_text_2 translated_text_3: {}", user.getNickname());
            return null;
        });
        log.info("translated_text_2 test translated_text_2 - translated_text_5 translated_text_3");
    }
}