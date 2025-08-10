package com.genius.primavera.transaction;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@SpringBootTest
@EnableTestContainers
@ActiveProfiles({"test"})
@DisplayName("translated_text_4 Read Phenomena test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReadPhenomenaTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("phenomena-test-" + System.currentTimeMillis() + "@example.com")
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("PHENOMENA_TESTER")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Dirty Read - T1translated_text_1 T2translated_text_1 translated_text_4 translated_text_2 translated_text_5 translated_text_2 translated_text_2")
    void testDirtyRead() throws InterruptedException {
        log.info("=== Dirty Read test translated_text_2 ===");
        Long userId = userMapper.save(testUser);
        CountDownLatch writerStartLatch = new CountDownLatch(1);
        CountDownLatch readerDoneLatch = new CountDownLatch(1);
        CountDownLatch writerRollbackLatch = new CountDownLatch(1);
        AtomicReference<String> dirtyReadValue = new AtomicReference<>();
        AtomicReference<String> cleanReadValue = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return dirtyReadWriter(userId, writerStartLatch, readerDoneLatch, writerRollbackLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return dirtyReadReader(userId, writerStartLatch, readerDoneLatch,
                        writerRollbackLatch, dirtyReadValue, cleanReadValue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);
        String writerResult;
        try {
            writerResult = writerFuture.join();
        } catch (Exception e) {
            writerResult = "Writer rollback completed (expected)";
            log.info("Writer translated_text_2 completed (translated_text_3 translated_text_2): {}", e.getCause().getMessage());
        }

        String readerResult = readerFuture.join();
        log.info("Dirty Read test result");
        log.info("- Writer: {}", writerResult);
        log.info("- Reader: {}", readerResult);
        log.info("- Dirty Read translated_text_1: {}", dirtyReadValue.get());
        log.info("- Clean Read translated_text_1: {}", cleanReadValue.get());
        if (dirtyReadValue.get() != null && !dirtyReadValue.get().equals(cleanReadValue.get())) {
            log.info(" Dirty Read translated_text_2 translated_text_13: translated_text_2 translated_text_1 translated_text_3");
        }

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String dirtyReadWriter(Long userId, CountDownLatch writerStartLatch, CountDownLatch readerDoneLatch, CountDownLatch writerRollbackLatch) throws InterruptedException {
        log.info("Dirty Read Writer translated_text_2 - translated_text_1 translated_text_2 translated_text_1 translated_text_2 translated_text_2");
        User user = userMapper.findById(userId);
        user.setNickname("DIRTY_VALUE_WILL_ROLLBACK");
        userMapper.update(user);
        log.info("translated_text_1 translated_text_2 completed: {}", user.getNickname());
        writerStartLatch.countDown();
        readerDoneLatch.await();
        log.info("translated_text_1 exception translated_text_4 translated_text_2 translated_text_2");
        writerRollbackLatch.countDown();
        throw new RuntimeException("translated_text_1 translated_text_2 - Dirty Read test");
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String dirtyReadReader(Long userId, CountDownLatch writerStartLatch, CountDownLatch readerDoneLatch, CountDownLatch writerRollbackLatch, AtomicReference<String> dirtyReadValue, AtomicReference<String> cleanReadValue) throws InterruptedException {
        writerStartLatch.await();
        log.info("Dirty Read Reader translated_text_2 - translated_text_4 translated_text_2 translated_text_1 translated_text_2");
        User dirtyUser = userMapper.findById(userId);
        dirtyReadValue.set(dirtyUser.getNickname());
        log.info("Dirty Read translated_text_2: {}", dirtyUser.getNickname());
        readerDoneLatch.countDown();
        writerRollbackLatch.await();
        Thread.sleep(100);
        User cleanUser = userMapper.findById(userId);
        cleanReadValue.set(cleanUser.getNickname());
        log.info("Clean Read translated_text_2: {}", cleanUser.getNickname());
        return "Dirty Read Reader completed";
    }

    @Test
    @Order(2)
    @DisplayName("Non-repeatable Read - T1translated_text_1 translated_text_2 translated_text_1 translated_text_1 translated_text_1 translated_text_2 translated_text_1 translated_text_2 translated_text_1 translated_text_2 translated_text_2")
    void testNonRepeatableRead() throws InterruptedException {
        log.info("=== Non-repeatable Read test translated_text_2 ===");
        Long userId = userMapper.save(testUser);
        CountDownLatch readerFirstReadLatch = new CountDownLatch(1);
        CountDownLatch writerDoneLatch = new CountDownLatch(1);
        AtomicReference<String> firstReadValue = new AtomicReference<>();
        AtomicReference<String> secondReadValue = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return nonRepeatableReadReader(userId, readerFirstReadLatch, writerDoneLatch, firstReadValue, secondReadValue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return nonRepeatableReadWriter(userId, readerFirstReadLatch, writerDoneLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);
        String readerResult = readerFuture.join();
        String writerResult = writerFuture.join();
        log.info("Non-repeatable Read test result");
        log.info("- Reader: {}", readerResult);
        log.info("- Writer: {}", writerResult);
        log.info("- translated_text_1 translated_text_1 translated_text_2: {}", firstReadValue.get());
        log.info("- translated_text_1 translated_text_1 translated_text_2: {}", secondReadValue.get());
        if (!firstReadValue.get().equals(secondReadValue.get())) log.info(" Non-repeatable Read translated_text_2 translated_text_13: translated_text_2 translated_text_4translated_text_1 translated_text_2 translated_text_1 translated_text_2");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String nonRepeatableReadReader(Long userId, CountDownLatch readerFirstReadLatch, CountDownLatch writerDoneLatch, AtomicReference<String> firstReadValue, AtomicReference<String> secondReadValue) throws InterruptedException {
        log.info("Non-repeatable Read Reader translated_text_2");
        User firstRead = userMapper.findById(userId);
        firstReadValue.set(firstRead.getNickname());
        log.info("translated_text_1 translated_text_1 translated_text_2: {}", firstRead.getNickname());
        readerFirstReadLatch.countDown();
        writerDoneLatch.await();
        User secondRead = userMapper.findById(userId);
        secondReadValue.set(secondRead.getNickname());
        log.info("translated_text_1 translated_text_1 translated_text_2: {}", secondRead.getNickname());
        return "Non-repeatable Read Reader completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String nonRepeatableReadWriter(Long userId, CountDownLatch readerFirstReadLatch, CountDownLatch writerDoneLatch) throws InterruptedException {
        readerFirstReadLatch.await();
        log.info("Non-repeatable Read Writer translated_text_2");
        User user = userMapper.findById(userId);
        user.setNickname("CHANGED_VALUE");
        userMapper.update(user);
        log.info("translated_text_1 translated_text_2 completed: {}", user.getNickname());
        writerDoneLatch.countDown();
        return "Non-repeatable Read Writer completed";
    }

    @Test
    @Order(3)
    @DisplayName("Phantom Read - T1translated_text_1 translated_text_2 translated_text_4 translated_text_8 translated_text_1 translated_text_3 translated_text_3 translated_text_4 translated_text_2")
    void testPhantomRead() throws InterruptedException {
        log.info("=== Phantom Read test translated_text_2 ===");
        User user1 = User.builder()
                .email("phantom1-" + System.currentTimeMillis() + "@example.com")
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("PHANTOM_USER_1")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userMapper.save(user1);
        CountDownLatch readerFirstQueryLatch = new CountDownLatch(1);
        CountDownLatch writerDoneLatch = new CountDownLatch(1);
        AtomicReference<Integer> firstQueryCount = new AtomicReference<>();
        AtomicReference<Integer> secondQueryCount = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return phantomReadReader(readerFirstQueryLatch, writerDoneLatch, firstQueryCount, secondQueryCount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return phantomReadWriter(readerFirstQueryLatch, writerDoneLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);
        String readerResult = readerFuture.join();
        String writerResult = writerFuture.join();
        log.info("Phantom Read test result");
        log.info("- Reader: {}", readerResult);
        log.info("- Writer: {}", writerResult);
        log.info("- translated_text_1 translated_text_1 translated_text_2 result translated_text_1: {}", firstQueryCount.get());
        log.info("- translated_text_1 translated_text_1 translated_text_2 result translated_text_1: {}", secondQueryCount.get());
        if (!firstQueryCount.get().equals(secondQueryCount.get())) {
            log.info(" Phantom Read translated_text_2 translated_text_13: translated_text_3 translated_text_3 translated_text_3");
        }
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    String phantomReadReader(CountDownLatch readerFirstQueryLatch, CountDownLatch writerDoneLatch, AtomicReference<Integer> firstQueryCount, AtomicReference<Integer> secondQueryCount) throws InterruptedException {
        log.info("Phantom Read Reader translated_text_2");
        List<User> firstQuery = userMapper.findByStatus(UserStatus.ACTIVE);
        firstQueryCount.set(firstQuery.size());
        log.info("translated_text_1 translated_text_1 translated_text_2 result: {} translated_text_1", firstQuery.size());
        readerFirstQueryLatch.countDown();
        writerDoneLatch.await();
        List<User> secondQuery = userMapper.findByStatus(UserStatus.ACTIVE);
        secondQueryCount.set(secondQuery.size());
        log.info("translated_text_1 translated_text_1 translated_text_2 result: {} translated_text_1", secondQuery.size());
        return "Phantom Read Reader completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String phantomReadWriter(CountDownLatch readerFirstQueryLatch, CountDownLatch writerDoneLatch) throws InterruptedException {
        readerFirstQueryLatch.await();
        log.info("Phantom Read Writer translated_text_2");
        User phantomUser = User.builder()
                .email("phantom2-" + System.currentTimeMillis() + "@example.com")
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("PHANTOM_USER_2")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userMapper.save(phantomUser);
        log.info("translated_text_3 User translated_text_2 completed: {}", phantomUser.getNickname());
        writerDoneLatch.countDown();
        return "Phantom Read Writer completed";
    }

    @Test
    @Order(4)
    @DisplayName("Lost Update - translated_text_1 translated_text_4translated_text_1 translated_text_2 translated_text_1 translated_text_3 translated_text_1 translated_text_1 translated_text_1 translated_text_1 translated_text_2translated_text_1 translated_text_4 translated_text_2")
    void testLostUpdate() throws InterruptedException {
        log.info("=== Lost Update test translated_text_2 ===");
        Long userId = userMapper.save(testUser);
        CountDownLatch bothReadLatch = new CountDownLatch(2);
        CountDownLatch firstUpdateLatch = new CountDownLatch(1);
        AtomicReference<String> transaction1Result = new AtomicReference<>();
        AtomicReference<String> transaction2Result = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<String> transaction1Future = CompletableFuture.supplyAsync(() -> {
            try {
                return lostUpdateTransaction1(userId, bothReadLatch, firstUpdateLatch, transaction1Result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "T1 interrupted";
            }
        }, executor);

        CompletableFuture<String> transaction2Future = CompletableFuture.supplyAsync(() -> {
            try {
                return lostUpdateTransaction2(userId, bothReadLatch, firstUpdateLatch, transaction2Result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "T2 interrupted";
            }
        }, executor);

        String result1 = transaction1Future.join();
        String result2 = transaction2Future.join();
        User finalUser = userMapper.findById(userId);
        log.info("Lost Update test result");
        log.info("- Transaction1: {}", result1);
        log.info("- Transaction2: {}", result2);
        log.info("- T1translated_text_1 translated_text_1 translated_text_1: {}", transaction1Result.get());
        log.info("- T2translated_text_1 translated_text_1 translated_text_1: {}", transaction2Result.get());
        log.info("- translated_text_2 translated_text_1translated_text_1 translated_text_1: {}", finalUser.getNickname());
        if (!finalUser.getNickname().equals(transaction1Result.get()) && !finalUser.getNickname().equals(transaction2Result.get())) {
            log.info(" Lost Update translated_text_2 translated_text_13: translated_text_1 translated_text_1 translated_text_2translated_text_1 translated_text_3");
        } else if (finalUser.getNickname().equals(transaction2Result.get()) && !finalUser.getNickname().equals(transaction1Result.get())) {
            log.info(" Lost Update translated_text_2 translated_text_13: T1translated_text_1 translated_text_2translated_text_1 T2translated_text_1 translated_text_1 translated_text_4");
        }
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String lostUpdateTransaction1(Long userId, CountDownLatch bothReadLatch, CountDownLatch firstUpdateLatch, AtomicReference<String> transaction1Result) throws InterruptedException {
        log.info("Lost Update T1 translated_text_2");
        User user = userMapper.findById(userId);
        String originalNickname = user.getNickname();
        log.info("T1translated_text_1 translated_text_2 translated_text_2 translated_text_1: {}", originalNickname);
        bothReadLatch.countDown();
        bothReadLatch.await();
        String newNickname = originalNickname + "_MODIFIED_BY_T1";
        user.setNickname(newNickname);
        userMapper.update(user);
        transaction1Result.set(newNickname);
        log.info("T1 translated_text_1 completed: {}", newNickname);
        firstUpdateLatch.countDown();
        Thread.sleep(100);
        return "Lost Update T1 completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String lostUpdateTransaction2(Long userId, CountDownLatch bothReadLatch, CountDownLatch firstUpdateLatch, AtomicReference<String> transaction2Result) throws InterruptedException {
        log.info("Lost Update T2 translated_text_2");
        User user = userMapper.findById(userId);
        String originalNickname = user.getNickname();
        log.info("T2translated_text_1 translated_text_2 translated_text_2 translated_text_1: {}", originalNickname);
        bothReadLatch.countDown();
        bothReadLatch.await();
        firstUpdateLatch.await();
        Thread.sleep(50);
        String newNickname = originalNickname + "_MODIFIED_BY_T2";
        user.setNickname(newNickname);
        userMapper.update(user);
        transaction2Result.set(newNickname);
        log.info("T2 translated_text_1 completed: {}", newNickname);
        return "Lost Update T2 completed";
    }
}