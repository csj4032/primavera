package com.genius.primavera.transaction;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@EnableTestContainers
@ActiveProfiles({"test"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Spring Transaction Isolation Level test")
public class IsolationLevelTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("isolation-test-" + System.currentTimeMillis() + "@example.com")
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("ISOLATION_TESTER")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("DEFAULT - translated_text_7 translated_text_2 translated_text_2 translated_text_2 translated_text_2 (translated_text_5 READ_COMMITTED)")
    void testDefaultIsolation() throws InterruptedException {
        log.info("=== DEFAULT Isolation test translated_text_2 ===");
        Long userId = userMapper.save(testUser);
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return defaultIsolationTransaction1(userId, latch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Thread1 interrupted";
            }
        }, executor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return defaultIsolationTransaction2(userId, latch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Thread2 interrupted";
            }
        }, executor);
        String result1 = future1.join();
        String result2 = future2.join();
        log.info("DEFAULT translated_text_2 translated_text_2 test result - T1: {}, T2: {}", result1, result2);
        assertThat(result1).contains("DEFAULT_T1");
        assertThat(result2).contains("DEFAULT_T2");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.DEFAULT)
    String defaultIsolationTransaction1(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("DEFAULT T1 translated_text_2 - User ID: {}", userId);
        User user = userMapper.findById(userId);
        user.setNickname("DEFAULT_T1_USER");
        userMapper.update(user);
        latch.countDown();
        latch.await();
        Thread.sleep(50);
        log.info("DEFAULT T1 completed");
        return "DEFAULT_T1 completed";
    }

    @Transactional(isolation = Isolation.DEFAULT)
    String defaultIsolationTransaction2(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("DEFAULT T2 translated_text_2 - User ID: {}", userId);
        User user = userMapper.findById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userMapper.update(user);
        latch.countDown();
        latch.await();
        Thread.sleep(50);
        log.info("DEFAULT T2 completed");
        return "DEFAULT_T2 completed";
    }

    @Test
    @DisplayName("READ_UNCOMMITTED - translated_text_2 translated_text_5 translated_text_4 translated_text_2 translated_text_5 translated_text_2 translated_text_1 translated_text_2 (Dirty Read translated_text_2 translated_text_2)")
    void testReadUncommittedIsolation() throws InterruptedException {
        log.info("=== READ_UNCOMMITTED Isolation test translated_text_2 ===");
        Long userId = userMapper.save(testUser);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return readUncommittedWriter(userId, startLatch, readLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return readUncommittedReader(userId, startLatch, readLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);
        String writerResult = writerFuture.join();
        String readerResult = readerFuture.join();
        log.info("READ_UNCOMMITTED test result - Writer: {}, Reader: {}", writerResult, readerResult);
        assertThat(writerResult).contains("WRITER");
        assertThat(readerResult).contains("READER");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String readUncommittedWriter(Long userId, CountDownLatch startLatch, CountDownLatch readLatch) throws InterruptedException {
        log.info("READ_UNCOMMITTED Writer translated_text_2");
        User user = userMapper.findById(userId);
        user.setNickname("UNCOMMITTED_CHANGE");
        userMapper.update(user);
        log.info("data translated_text_2 completed, Readertranslated_text_2 translated_text_2 translated_text_2");
        startLatch.countDown();
        readLatch.await();
        Thread.sleep(100);
        log.info("READ_UNCOMMITTED Writer completed");
        return "WRITER completed";
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String readUncommittedReader(Long userId, CountDownLatch startLatch, CountDownLatch readLatch) throws InterruptedException {
        log.info("READ_UNCOMMITTED Reader translated_text_2 translated_text_1");
        startLatch.await();
        log.info("READ_UNCOMMITTED Reader translated_text_2");
        User user = userMapper.findById(userId);
        log.info("translated_text_2 data: nickname = {}", user.getNickname());
        readLatch.countDown();
        log.info("READ_UNCOMMITTED Reader completed");
        return "READER completed - read: " + user.getNickname();
    }

    @Test
    @DisplayName("READ_COMMITTED - translated_text_3 data translated_text_2 translated_text_1 translated_text_2 (Dirty Read translated_text_2)")
    void testReadCommittedIsolation() throws InterruptedException {
        log.info("=== READ_COMMITTED Isolation test translated_text_2 ===");
        Long userId = userMapper.save(testUser);
        CountDownLatch writerStartLatch = new CountDownLatch(1);
        CountDownLatch readerDoneLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return readCommittedWriter(userId, writerStartLatch, readerDoneLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return readCommittedReader(userId, writerStartLatch, readerDoneLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);
        String writerResult = writerFuture.join();
        String readerResult = readerFuture.join();
        log.info("READ_COMMITTED test result - Writer: {}, Reader: {}", writerResult, readerResult);
        assertThat(writerResult).contains("COMMITTED_WRITER");
        assertThat(readerResult).contains("COMMITTED_READER");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String readCommittedWriter(Long userId, CountDownLatch writerStartLatch, CountDownLatch readerDoneLatch) throws InterruptedException {
        log.info("READ_COMMITTED Writer translated_text_2");
        User user = userMapper.findById(userId);
        user.setNickname("WILL_BE_COMMITTED");
        userMapper.update(user);
        log.info("data translated_text_2 completed, Reader translated_text_2 translated_text_2");
        writerStartLatch.countDown();
        Thread.sleep(200);
        log.info("READ_COMMITTED Writer translated_text_2 translated_text_2");
        return "COMMITTED_WRITER completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String readCommittedReader(Long userId, CountDownLatch writerStartLatch, CountDownLatch readerDoneLatch) throws InterruptedException {
        writerStartLatch.await();
        log.info("READ_COMMITTED Reader translated_text_2");
        Thread.sleep(50);
        User user = userMapper.findById(userId);
        log.info("READ_COMMITTEDtranslated_text_1 translated_text_2 data: nickname = {}", user.getNickname());
        readerDoneLatch.countDown();
        log.info("READ_COMMITTED Reader completed");
        return "COMMITTED_READER completed - read: " + user.getNickname();
    }

    @Test
    @DisplayName("REPEATABLE_READ - translated_text_4 translated_text_2 translated_text_2 data translated_text_2 translated_text_2 translated_text_1 translated_text_3 translated_text_1 translated_text_2")
    void testRepeatableReadIsolation() throws InterruptedException {
        log.info("=== REPEATABLE_READ Isolation test translated_text_2 ===");
        Long userId = userMapper.save(testUser);
        CountDownLatch readerStartLatch = new CountDownLatch(1);
        CountDownLatch writerDoneLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return repeatableReadReader(userId, readerStartLatch, writerDoneLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return repeatableReadWriter(userId, readerStartLatch, writerDoneLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);
        String readerResult = readerFuture.join();
        String writerResult = writerFuture.join();
        log.info("REPEATABLE_READ test result - Reader: {}, Writer: {}", readerResult, writerResult);
        assertThat(readerResult).contains("REPEATABLE_READER");
        assertThat(writerResult).contains("REPEATABLE_WRITER");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    String repeatableReadReader(Long userId, CountDownLatch readerStartLatch, CountDownLatch writerDoneLatch) throws InterruptedException {
        log.info("REPEATABLE_READ Reader translated_text_2");
        User user1 = userMapper.findById(userId);
        log.info("translated_text_1 translated_text_2 translated_text_2: nickname = {}", user1.getNickname());
        readerStartLatch.countDown();
        writerDoneLatch.await();
        User user2 = userMapper.findById(userId);
        log.info("translated_text_1 translated_text_2 translated_text_2: nickname = {}", user2.getNickname());
        log.info("REPEATABLE_READ Reader completed - translated_text_1translated_text_2: {}, translated_text_1translated_text_2: {}", user1.getNickname(), user2.getNickname());
        return String.format("REPEATABLE_READER completed - first: %s, second: %s", user1.getNickname(), user2.getNickname());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String repeatableReadWriter(Long userId, CountDownLatch readerStartLatch, CountDownLatch writerDoneLatch) throws InterruptedException {
        readerStartLatch.await();
        log.info("REPEATABLE_READ Writer translated_text_2");
        User user = userMapper.findById(userId);
        user.setNickname("CHANGED_BY_WRITER");
        userMapper.update(user);
        log.info("REPEATABLE_READ Writer data translated_text_2 completed");
        writerDoneLatch.countDown();
        log.info("REPEATABLE_READ Writer completed");
        return "REPEATABLE_WRITER completed";
    }

    @Test
    @DisplayName("SERIALIZABLE - translated_text_2 translated_text_2 translated_text_2 translated_text_2, all translated_text_3 translated_text_2 translated_text_5 translated_text_2 translated_text_2")
    void testSerializableIsolation() throws InterruptedException {
        log.info("=== SERIALIZABLE Isolation test translated_text_2 ===");
        Long userId = userMapper.save(testUser);
        CountDownLatch startLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return serializableTransaction1(userId, startLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "T1 interrupted";
            }
        }, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return serializableTransaction2(userId, startLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "T2 interrupted";
            }
        }, executor);
        String result1 = future1.join();
        String result2 = future2.join();
        log.info("SERIALIZABLE test result - T1: {}, T2: {}", result1, result2);
        assertThat(result1).contains("SERIALIZABLE_T1");
        assertThat(result2).contains("SERIALIZABLE_T2");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    String serializableTransaction1(Long userId, CountDownLatch startLatch) throws InterruptedException {
        log.info("SERIALIZABLE T1 translated_text_2");
        startLatch.countDown();
        startLatch.await();
        User user = userMapper.findById(userId);
        user.setNickname("SERIALIZABLE_T1_USER");
        userMapper.update(user);
        Thread.sleep(100);
        log.info("SERIALIZABLE T1 completed");
        return "SERIALIZABLE_T1 completed";
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    String serializableTransaction2(Long userId, CountDownLatch startLatch) throws InterruptedException {
        log.info("SERIALIZABLE T2 translated_text_2");
        startLatch.countDown();
        startLatch.await();
        User user = userMapper.findById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userMapper.update(user);
        Thread.sleep(100);
        log.info("SERIALIZABLE T2 completed");
        return "SERIALIZABLE_T2 completed";
    }
}