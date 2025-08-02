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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers
@DisplayName("트랜잭션 Read Phenomena 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReadPhenomenaTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WinnerMapper winnerMapper;

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
    @DisplayName("Dirty Read - T1이 T2의 커밋되지 않은 변경사항을 읽는 현상")
    void testDirtyRead() throws InterruptedException {
        log.info("=== Dirty Read 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        CountDownLatch writerStartLatch = new CountDownLatch(1);
        CountDownLatch readerDoneLatch = new CountDownLatch(1);
        CountDownLatch writerRollbackLatch = new CountDownLatch(1);

        AtomicReference<String> dirtyReadValue = new AtomicReference<>();
        AtomicReference<String> cleanReadValue = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // T2: 데이터 변경 후 롤백 (커밋하지 않음)
        CompletableFuture<String> writerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return dirtyReadWriter(userId, writerStartLatch, readerDoneLatch, writerRollbackLatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Writer interrupted";
            }
        }, executor);

        // T1: READ_UNCOMMITTED로 변경 중인 데이터 읽기 시도
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return dirtyReadReader(userId, writerStartLatch, readerDoneLatch,
                        writerRollbackLatch, dirtyReadValue, cleanReadValue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);

        // Writer는 의도적으로 예외를 발생시키므로 예외 처리
        String writerResult;
        try {
            writerResult = writerFuture.join();
        } catch (Exception e) {
            writerResult = "Writer rollback completed (expected)";
            log.info("Writer 롤백 완료 (예상된 동작): {}", e.getCause().getMessage());
        }
        
        String readerResult = readerFuture.join();

        log.info("Dirty Read 테스트 결과");
        log.info("- Writer: {}", writerResult);
        log.info("- Reader: {}", readerResult);
        log.info("- Dirty Read 값: {}", dirtyReadValue.get());
        log.info("- Clean Read 값: {}", cleanReadValue.get());

        // Dirty Read가 발생했는지 확인
        if (dirtyReadValue.get() != null && !dirtyReadValue.get().equals(cleanReadValue.get())) {
            log.info("✓ Dirty Read 현상 확인됨: 롤백된 데이터를 읽었음");
        }

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String dirtyReadWriter(Long userId, CountDownLatch writerStartLatch, CountDownLatch readerDoneLatch,
                           CountDownLatch writerRollbackLatch) throws InterruptedException {
        log.info("Dirty Read Writer 시작 - 데이터 변경 후 롤백 예정");

        User user = userMapper.findById(userId);
        user.setNickname("DIRTY_VALUE_WILL_ROLLBACK");
        userMapper.update(user);

        log.info("데이터 변경 완료: {}", user.getNickname());
        writerStartLatch.countDown();

        // Reader가 dirty read를 할 때까지 대기
        readerDoneLatch.await();

        log.info("의도적 예외 발생으로 롤백 수행");
        writerRollbackLatch.countDown();

        // 롤백을 위한 예외 발생
        throw new RuntimeException("의도적 롤백 - Dirty Read 테스트");
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    String dirtyReadReader(Long userId, CountDownLatch writerStartLatch, CountDownLatch readerDoneLatch,
                           CountDownLatch writerRollbackLatch, AtomicReference<String> dirtyReadValue,
                           AtomicReference<String> cleanReadValue) throws InterruptedException {
        // Writer가 데이터를 변경할 때까지 대기
        writerStartLatch.await();

        log.info("Dirty Read Reader 시작 - 커밋되지 않은 데이터 읽기");

        // Dirty Read 수행
        User dirtyUser = userMapper.findById(userId);
        dirtyReadValue.set(dirtyUser.getNickname());
        log.info("Dirty Read 수행: {}", dirtyUser.getNickname());

        readerDoneLatch.countDown();

        // Writer가 롤백할 때까지 대기
        writerRollbackLatch.await();
        Thread.sleep(100); // 롤백 완료 대기

        // 롤백 후 Clean Read 수행
        User cleanUser = userMapper.findById(userId);
        cleanReadValue.set(cleanUser.getNickname());
        log.info("Clean Read 수행: {}", cleanUser.getNickname());

        return "Dirty Read Reader completed";
    }

    @Test
    @Order(2)
    @DisplayName("Non-repeatable Read - T1이 같은 데이터를 두 번 읽을 때 다른 값을 얻는 현상")
    void testNonRepeatableRead() throws InterruptedException {
        log.info("=== Non-repeatable Read 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        CountDownLatch readerFirstReadLatch = new CountDownLatch(1);
        CountDownLatch writerDoneLatch = new CountDownLatch(1);

        AtomicReference<String> firstReadValue = new AtomicReference<>();
        AtomicReference<String> secondReadValue = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // T1: READ_COMMITTED로 같은 데이터를 두 번 읽기
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return nonRepeatableReadReader(userId, readerFirstReadLatch, writerDoneLatch, firstReadValue, secondReadValue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);

        // T2: 중간에 데이터 변경
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

        log.info("Non-repeatable Read 테스트 결과");
        log.info("- Reader: {}", readerResult);
        log.info("- Writer: {}", writerResult);
        log.info("- 첫 번째 읽기: {}", firstReadValue.get());
        log.info("- 두 번째 읽기: {}", secondReadValue.get());

        // Non-repeatable Read가 발생했는지 확인
        if (!firstReadValue.get().equals(secondReadValue.get())) {
            log.info("✓ Non-repeatable Read 현상 확인됨: 같은 트랜잭션에서 다른 값을 읽음");
        }

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String nonRepeatableReadReader(Long userId, CountDownLatch readerFirstReadLatch, CountDownLatch writerDoneLatch,
                                   AtomicReference<String> firstReadValue, AtomicReference<String> secondReadValue)
            throws InterruptedException {
        log.info("Non-repeatable Read Reader 시작");

        // 첫 번째 읽기
        User firstRead = userMapper.findById(userId);
        firstReadValue.set(firstRead.getNickname());
        log.info("첫 번째 읽기: {}", firstRead.getNickname());

        // Writer에게 변경 허용 신호
        readerFirstReadLatch.countDown();

        // Writer가 변경을 완료할 때까지 대기
        writerDoneLatch.await();

        // 두 번째 읽기
        User secondRead = userMapper.findById(userId);
        secondReadValue.set(secondRead.getNickname());
        log.info("두 번째 읽기: {}", secondRead.getNickname());

        return "Non-repeatable Read Reader completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String nonRepeatableReadWriter(Long userId, CountDownLatch readerFirstReadLatch, CountDownLatch writerDoneLatch)
            throws InterruptedException {
        // Reader가 첫 번째 읽기를 완료할 때까지 대기
        readerFirstReadLatch.await();

        log.info("Non-repeatable Read Writer 시작");

        User user = userMapper.findById(userId);
        user.setNickname("CHANGED_VALUE");
        userMapper.update(user);

        log.info("데이터 변경 완료: {}", user.getNickname());
        writerDoneLatch.countDown();

        return "Non-repeatable Read Writer completed";
    }

    @Test
    @Order(3)
    @DisplayName("Phantom Read - T1이 같은 조건으로 조회할 때 새로운 로우가 나타나는 현상")
    void testPhantomRead() throws InterruptedException {
        log.info("=== Phantom Read 테스트 시작 ===");

        // 기본 데이터 삽입
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

        // T1: REPEATABLE_READ로 같은 조건의 쿼리를 두 번 실행
        CompletableFuture<String> readerFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return phantomReadReader(readerFirstQueryLatch, writerDoneLatch, firstQueryCount, secondQueryCount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Reader interrupted";
            }
        }, executor);

        // T2: 중간에 새로운 데이터 삽입
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

        log.info("Phantom Read 테스트 결과");
        log.info("- Reader: {}", readerResult);
        log.info("- Writer: {}", writerResult);
        log.info("- 첫 번째 쿼리 결과 수: {}", firstQueryCount.get());
        log.info("- 두 번째 쿼리 결과 수: {}", secondQueryCount.get());

        // Phantom Read가 발생했는지 확인
        if (!firstQueryCount.get().equals(secondQueryCount.get())) {
            log.info("✓ Phantom Read 현상 확인됨: 새로운 로우가 나타남");
        }

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    String phantomReadReader(CountDownLatch readerFirstQueryLatch, CountDownLatch writerDoneLatch,
                             AtomicReference<Integer> firstQueryCount, AtomicReference<Integer> secondQueryCount)
            throws InterruptedException {
        log.info("Phantom Read Reader 시작");

        // 첫 번째 쿼리: ACTIVE 상태의 User 수 조회
        List<User> firstQuery = userMapper.findByStatus(UserStatus.ACTIVE);
        firstQueryCount.set(firstQuery.size());
        log.info("첫 번째 쿼리 결과: {} 개", firstQuery.size());

        // Writer에게 삽입 허용 신호
        readerFirstQueryLatch.countDown();

        // Writer가 삽입을 완료할 때까지 대기
        writerDoneLatch.await();

        // 두 번째 쿼리: 동일한 조건으로 재조회
        List<User> secondQuery = userMapper.findByStatus(UserStatus.ACTIVE);
        secondQueryCount.set(secondQuery.size());
        log.info("두 번째 쿼리 결과: {} 개", secondQuery.size());

        return "Phantom Read Reader completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String phantomReadWriter(CountDownLatch readerFirstQueryLatch, CountDownLatch writerDoneLatch)
            throws InterruptedException {
        // Reader가 첫 번째 쿼리를 완료할 때까지 대기
        readerFirstQueryLatch.await();

        log.info("Phantom Read Writer 시작");

        // 새로운 ACTIVE 상태의 User 삽입
        User phantomUser = User.builder()
                .email("phantom2-" + System.currentTimeMillis() + "@example.com")
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("PHANTOM_USER_2")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userMapper.save(phantomUser);

        log.info("새로운 User 삽입 완료: {}", phantomUser.getNickname());
        writerDoneLatch.countDown();

        return "Phantom Read Writer completed";
    }

    @Test
    @Order(4)
    @DisplayName("Lost Update - 두 트랜잭션이 같은 데이터를 동시에 수정할 때 한 쪽 변경사항이 소실되는 현상")
    void testLostUpdate() throws InterruptedException {
        log.info("=== Lost Update 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        CountDownLatch bothReadLatch = new CountDownLatch(2);
        CountDownLatch firstUpdateLatch = new CountDownLatch(1);

        AtomicReference<String> transaction1Result = new AtomicReference<>();
        AtomicReference<String> transaction2Result = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // T1: 데이터를 읽고 수정
        CompletableFuture<String> transaction1Future = CompletableFuture.supplyAsync(() -> {
            try {
                return lostUpdateTransaction1(userId, bothReadLatch, firstUpdateLatch, transaction1Result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "T1 interrupted";
            }
        }, executor);

        // T2: 같은 데이터를 읽고 수정
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

        // 최종 결과 확인
        User finalUser = userMapper.findById(userId);

        log.info("Lost Update 테스트 결과");
        log.info("- Transaction1: {}", result1);
        log.info("- Transaction2: {}", result2);
        log.info("- T1이 설정한 값: {}", transaction1Result.get());
        log.info("- T2가 설정한 값: {}", transaction2Result.get());
        log.info("- 최종 데이터베이스 값: {}", finalUser.getNickname());

        // Lost Update 발생 여부 확인
        if (!finalUser.getNickname().equals(transaction1Result.get()) && !finalUser.getNickname().equals(transaction2Result.get())) {
            log.info("✓ Lost Update 현상 확인됨: 둘 다의 변경사항이 소실됨");
        } else if (finalUser.getNickname().equals(transaction2Result.get()) && !finalUser.getNickname().equals(transaction1Result.get())) {
            log.info("✓ Lost Update 현상 확인됨: T1의 변경사항이 T2에 의해 덮어써짐");
        }

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String lostUpdateTransaction1(Long userId, CountDownLatch bothReadLatch, CountDownLatch firstUpdateLatch, AtomicReference<String> transaction1Result) throws InterruptedException {
        log.info("Lost Update T1 시작");

        // 데이터 읽기
        User user = userMapper.findById(userId);
        String originalNickname = user.getNickname();
        log.info("T1이 읽은 원본 데이터: {}", originalNickname);

        bothReadLatch.countDown();
        bothReadLatch.await(); // 둘 다 읽을 때까지 대기

        // 데이터 수정
        String newNickname = originalNickname + "_MODIFIED_BY_T1";
        user.setNickname(newNickname);
        userMapper.update(user);
        transaction1Result.set(newNickname);

        log.info("T1 수정 완료: {}", newNickname);
        firstUpdateLatch.countDown();

        Thread.sleep(100); // T2가 수정할 시간 제공

        return "Lost Update T1 completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String lostUpdateTransaction2(Long userId, CountDownLatch bothReadLatch, CountDownLatch firstUpdateLatch, AtomicReference<String> transaction2Result) throws InterruptedException {
        log.info("Lost Update T2 시작");

        // 데이터 읽기 (T1과 동일한 원본 데이터 읽음)
        User user = userMapper.findById(userId);
        String originalNickname = user.getNickname();
        log.info("T2가 읽은 원본 데이터: {}", originalNickname);

        bothReadLatch.countDown();
        bothReadLatch.await(); // 둘 다 읽을 때까지 대기

        // T1이 먼저 수정하도록 잠시 대기
        firstUpdateLatch.await();
        Thread.sleep(50);

        // T1의 변경사항을 무시하고 원본 데이터를 기준으로 수정
        String newNickname = originalNickname + "_MODIFIED_BY_T2";
        user.setNickname(newNickname);
        userMapper.update(user);
        transaction2Result.set(newNickname);

        log.info("T2 수정 완료: {}", newNickname);

        return "Lost Update T2 completed";
    }
}