package com.genius.primavera.transaction;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
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
@ActiveProfiles({"test"})
@EnablePrimaveraTestcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Spring Transaction Isolation Level 테스트")
public class IsolationLevelTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WinnerMapper winnerMapper;

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
    @DisplayName("DEFAULT - 데이터베이스 기본 격리 수준 사용 (일반적으로 READ_COMMITTED)")
    void testDefaultIsolation() throws InterruptedException {
        log.info("=== DEFAULT Isolation 테스트 시작 ===");
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
        log.info("DEFAULT 격리 수준 테스트 결과 - T1: {}, T2: {}", result1, result2);
        assertThat(result1).contains("DEFAULT_T1");
        assertThat(result2).contains("DEFAULT_T2");
        executor.shutdown();
    }

    @Transactional(isolation = Isolation.DEFAULT)
    String defaultIsolationTransaction1(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("DEFAULT T1 시작 - User ID: {}", userId);
        User user = userMapper.findById(userId);
        user.setNickname("DEFAULT_T1_USER");
        userMapper.update(user);
        latch.countDown();
        latch.await();
        Thread.sleep(50);
        log.info("DEFAULT T1 완료");
        return "DEFAULT_T1 completed";
    }

    @Transactional(isolation = Isolation.DEFAULT)
    String defaultIsolationTransaction2(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("DEFAULT T2 시작 - User ID: {}", userId);
        User user = userMapper.findById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userMapper.update(user);
        latch.countDown();
        latch.await();
        Thread.sleep(50);
        log.info("DEFAULT T2 완료");
        return "DEFAULT_T2 completed";
    }

    @Test
    @DisplayName("READ_UNCOMMITTED - 다른 트랜잭션의 커밋되지 않은 변경사항도 읽을 수 있음 (Dirty Read 발생 가능)")
    void testReadUncommittedIsolation() throws InterruptedException {
        log.info("=== READ_UNCOMMITTED Isolation 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // T1: 데이터를 변경하고 잠시 대기 (커밋하지 않음)
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return readUncommittedWriter(userId, startLatch, readLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);

        // T2: READ_UNCOMMITTED로 변경 중인 데이터 읽기 시도
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

        log.info("READ_UNCOMMITTED 테스트 결과 - Writer: {}, Reader: {}", writerResult, readerResult);

        assertThat(writerResult).contains("WRITER");
        assertThat(readerResult).contains("READER");

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String readUncommittedWriter(Long userId, CountDownLatch startLatch, CountDownLatch readLatch)
            throws InterruptedException {
        log.info("READ_UNCOMMITTED Writer 시작");

        User user = userMapper.findById(userId);
        user.setNickname("UNCOMMITTED_CHANGE");
        userMapper.update(user);

        log.info("데이터 변경 완료, Reader에게 신호 전송");
        startLatch.countDown();

        // Reader가 읽을 때까지 대기
        readLatch.await();

        // 잠시 대기 후 롤백 (의도적으로 예외 발생시키지 않고 정상 커밋)
        Thread.sleep(100);

        log.info("READ_UNCOMMITTED Writer 완료");
        return "WRITER completed";
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String readUncommittedReader(Long userId, CountDownLatch startLatch, CountDownLatch readLatch)
            throws InterruptedException {
        log.info("READ_UNCOMMITTED Reader 대기 중");

        // Writer가 데이터를 변경할 때까지 대기
        startLatch.await();

        log.info("READ_UNCOMMITTED Reader 시작");
        User user = userMapper.findById(userId);
        log.info("읽은 데이터: nickname = {}", user.getNickname());

        readLatch.countDown();

        log.info("READ_UNCOMMITTED Reader 완료");
        return "READER completed - read: " + user.getNickname();
    }

    @Test
    @DisplayName("READ_COMMITTED - 커밋된 데이터만 읽을 수 있음 (Dirty Read 방지)")
    void testReadCommittedIsolation() throws InterruptedException {
        log.info("=== READ_COMMITTED Isolation 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        CountDownLatch writerStartLatch = new CountDownLatch(1);
        CountDownLatch readerDoneLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // T1: 데이터 변경 후 잠시 대기
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return readCommittedWriter(userId, writerStartLatch, readerDoneLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);

        // T2: READ_COMMITTED로 데이터 읽기 (커밋된 데이터만 읽음)
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

        log.info("READ_COMMITTED 테스트 결과 - Writer: {}, Reader: {}", writerResult, readerResult);

        assertThat(writerResult).contains("COMMITTED_WRITER");
        assertThat(readerResult).contains("COMMITTED_READER");

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String readCommittedWriter(Long userId, CountDownLatch writerStartLatch, CountDownLatch readerDoneLatch)
            throws InterruptedException {
        log.info("READ_COMMITTED Writer 시작");

        User user = userMapper.findById(userId);
        user.setNickname("WILL_BE_COMMITTED");
        userMapper.update(user);

        log.info("데이터 변경 완료, Reader 시작 허용");
        writerStartLatch.countDown();

        // Reader가 완료될 때까지 잠시 대기
        Thread.sleep(200);

        log.info("READ_COMMITTED Writer 커밋 예정");
        return "COMMITTED_WRITER completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String readCommittedReader(Long userId, CountDownLatch writerStartLatch, CountDownLatch readerDoneLatch)
            throws InterruptedException {
        // Writer가 시작할 때까지 대기
        writerStartLatch.await();

        log.info("READ_COMMITTED Reader 시작");

        // 짧은 대기 후 읽기 (Writer가 아직 커밋하지 않은 상태)
        Thread.sleep(50);

        User user = userMapper.findById(userId);
        log.info("READ_COMMITTED로 읽은 데이터: nickname = {}", user.getNickname());

        readerDoneLatch.countDown();

        log.info("READ_COMMITTED Reader 완료");
        return "COMMITTED_READER completed - read: " + user.getNickname();
    }

    @Test
    @DisplayName("REPEATABLE_READ - 트랜잭션 동안 같은 데이터를 반복 읽을 때 동일한 값 보장")
    void testRepeatableReadIsolation() throws InterruptedException {
        log.info("=== REPEATABLE_READ Isolation 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        CountDownLatch readerStartLatch = new CountDownLatch(1);
        CountDownLatch writerDoneLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // T1: REPEATABLE_READ로 데이터를 두 번 읽기
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return repeatableReadReader(userId, readerStartLatch, writerDoneLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);

        // T2: 중간에 데이터 변경
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

        log.info("REPEATABLE_READ 테스트 결과 - Reader: {}, Writer: {}", readerResult, writerResult);

        assertThat(readerResult).contains("REPEATABLE_READER");
        assertThat(writerResult).contains("REPEATABLE_WRITER");

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    String repeatableReadReader(Long userId, CountDownLatch readerStartLatch, CountDownLatch writerDoneLatch)
            throws InterruptedException {
        log.info("REPEATABLE_READ Reader 시작");

        // 첫 번째 읽기
        User user1 = userMapper.findById(userId);
        log.info("첫 번째 읽기: nickname = {}", user1.getNickname());

        // Writer에게 실행 허용 신호
        readerStartLatch.countDown();

        // Writer가 완료될 때까지 대기
        writerDoneLatch.await();

        // 두 번째 읽기 (REPEATABLE_READ에서는 같은 값이 나와야 함)
        User user2 = userMapper.findById(userId);
        log.info("두 번째 읽기: nickname = {}", user2.getNickname());

        log.info("REPEATABLE_READ Reader 완료 - 첫번째: {}, 두번째: {}",
                user1.getNickname(), user2.getNickname());

        return String.format("REPEATABLE_READER completed - first: %s, second: %s",
                user1.getNickname(), user2.getNickname());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String repeatableReadWriter(Long userId, CountDownLatch readerStartLatch, CountDownLatch writerDoneLatch)
            throws InterruptedException {
        // Reader가 첫 번째 읽기를 완료할 때까지 대기
        readerStartLatch.await();

        log.info("REPEATABLE_READ Writer 시작");

        User user = userMapper.findById(userId);
        user.setNickname("CHANGED_BY_WRITER");
        userMapper.update(user);

        log.info("REPEATABLE_READ Writer 데이터 변경 완료");
        writerDoneLatch.countDown();

        log.info("REPEATABLE_READ Writer 완료");
        return "REPEATABLE_WRITER completed";
    }

    @Test
    @DisplayName("SERIALIZABLE - 가장 높은 격리 수준, 모든 동시성 문제 해결하지만 성능 저하")
    void testSerializableIsolation() throws InterruptedException {
        log.info("=== SERIALIZABLE Isolation 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        CountDownLatch startLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // T1: SERIALIZABLE로 데이터 처리
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return serializableTransaction1(userId, startLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "T1 interrupted";
            }
        }, executor);

        // T2: SERIALIZABLE로 같은 데이터에 접근
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

        log.info("SERIALIZABLE 테스트 결과 - T1: {}, T2: {}", result1, result2);

        assertThat(result1).contains("SERIALIZABLE_T1");
        assertThat(result2).contains("SERIALIZABLE_T2");

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    String serializableTransaction1(Long userId, CountDownLatch startLatch) throws InterruptedException {
        log.info("SERIALIZABLE T1 시작");

        startLatch.countDown();
        startLatch.await(); // 모든 트랜잭션이 시작될 때까지 대기

        User user = userMapper.findById(userId);
        user.setNickname("SERIALIZABLE_T1_USER");
        userMapper.update(user);

        Thread.sleep(100);

        log.info("SERIALIZABLE T1 완료");
        return "SERIALIZABLE_T1 completed";
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    String serializableTransaction2(Long userId, CountDownLatch startLatch) throws InterruptedException {
        log.info("SERIALIZABLE T2 시작");

        startLatch.countDown();
        startLatch.await(); // 모든 트랜잭션이 시작될 때까지 대기

        User user = userMapper.findById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userMapper.update(user);

        Thread.sleep(100);

        log.info("SERIALIZABLE T2 완료");
        return "SERIALIZABLE_T2 completed";
    }
}