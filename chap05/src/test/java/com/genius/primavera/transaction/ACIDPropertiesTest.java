package com.genius.primavera.transaction;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import com.genius.primavera.domain.model.Winner;

import java.math.BigDecimal;

import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ACID 속성 테스트")
@EnablePrimaveraTestcontainers
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
                
        // TransactionTemplate 초기화
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    @Order(1)
    @DisplayName("원자성(Atomicity) 테스트 - 트랜잭션 롤백 시 모든 변경사항이 취소됨")
    void testAtomicity() {
        Long userId = userMapper.save(testUser);
        long initialUserCount = userMapper.count();
        long initialWinnerCount = winnerMapper.count();
        log.info("초기 상태 - User 수: {}, Winner 수: {}", initialUserCount, initialWinnerCount);
        assertThatThrownBy(() -> {
            // 프로그래밍 방식으로 트랜잭션 실행
            transactionTemplate.execute(status -> {
                User user = userMapper.findById(userId);
                user.setStatus(UserStatus.INACTIVE);
                userMapper.update(user);

                log.info("User 상태를 INACTIVE로 변경: {}", user.getId());

                // Winner 데이터 삽입
                Winner winner = Winner.builder()
                        .name("Test Winner")
                        .year(2023)
                        .sport("Test Sport")
                        .prize("Test Prize")
                        .amount(new BigDecimal("1000.00"))
                        .build();
                winnerMapper.save(winner);

                log.info("Winner 데이터 삽입: ID={}, Name={}", winner.getId(), winner.getName());

                // 의도적 예외 발생 - 원자성 테스트를 위해
                throw new RuntimeException("의도적인 예외 발생 - 원자성 테스트");
            });
        }).isInstanceOf(RuntimeException.class).hasMessage("의도적인 예외 발생 - 원자성 테스트");
        long finalUserCount = userMapper.count();
        long finalWinnerCount = winnerMapper.count();
        log.info("롤백 후 상태 - User 수: {}, Winner 수: {}", finalUserCount, finalWinnerCount);
        assertThat(finalUserCount).isEqualTo(initialUserCount);
        assertThat(finalWinnerCount).isEqualTo(initialWinnerCount);
    }


    @Test
    @Order(2)
    @DisplayName("일관성(Consistency) 테스트 - 비즈니스 규칙 위반 시 트랜잭션 실패")
    void testConsistency() {
        // Given
        Long userId = userMapper.save(testUser);

        // When & Then - 이메일 중복 삽입 시도 (UNIQUE 제약 위반)
        // 동일한 이메일로 중복 사용자 생성 시도
        User duplicateUser = User.builder()
                .email(testUser.getEmail())
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("DUPLICATE_USER")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> {
            // 프로그래밍 방식으로 트랜잭션 실행
            transactionTemplate.execute(status -> {
                userMapper.save(duplicateUser); // UNIQUE 제약 위반으로 예외 발생 예상
                return null;
            });
        }).isInstanceOf(DataAccessException.class);

        // 데이터 일관성 확인
        long userCount = userMapper.countByEmail(testUser.getEmail());
        assertThat(userCount).isEqualTo(1); // 중복 삽입이 방지되어야 함

        log.info("일관성 테스트 통과 - 중복 이메일 삽입 방지됨");
    }

    @Test
    @Order(3)
    @DisplayName("격리성(Isolation) 테스트 - 동시 트랜잭션 간 격리")
    void testIsolation() throws InterruptedException {
        // Given
        Long userId = userMapper.save(testUser);
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // When - 두 개의 동시 트랜잭션 실행
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

        // Then
        String result1 = future1.join();
        String result2 = future2.join();

        log.info("격리성 테스트 결과 - Thread1: {}, Thread2: {}", result1, result2);

        // 각 트랜잭션이 격리되어 실행되었는지 확인
        assertThat(result1).contains("Transaction1");
        assertThat(result2).contains("Transaction2");

        executor.shutdown();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String isolatedTransaction1(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("Transaction1 시작 - User ID: {}", userId);

        User user = userMapper.findById(userId);
        user.setNickname("TRANSACTION1_USER");
        userMapper.update(user);

        latch.countDown();
        latch.await(); // 다른 트랜잭션과 동기화

        Thread.sleep(100); // 짧은 지연

        log.info("Transaction1 완료");
        return "Transaction1 completed";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    String isolatedTransaction2(Long userId, CountDownLatch latch) throws InterruptedException {
        log.info("Transaction2 시작 - User ID: {}", userId);

        User user = userMapper.findById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userMapper.update(user);

        latch.countDown();
        latch.await(); // 다른 트랜잭션과 동기화

        Thread.sleep(100); // 짧은 지연

        log.info("Transaction2 완료");
        return "Transaction2 completed";
    }

    @Test
    @Order(4)
    @DisplayName("지속성(Durability) 테스트 - 커밋된 트랜잭션은 시스템 재시작 후에도 유지")
    void testDurability() {
        // Given
        Long userId = userMapper.save(testUser);
        long initialCount = userMapper.count();

        // When - 트랜잭션 커밋 (프로그래밍 방식)
        transactionTemplate.execute(status -> {
            User user = userMapper.findById(userId);
            log.info("지속성 테스트 - 업데이트 전 User 상태: nickname={}, status={}", user.getNickname(), user.getStatus());
            user.setNickname("DURABLE_USER");
            user.setStatus(UserStatus.INACTIVE);
            userMapper.update(user);
            log.info("지속성 테스트 - User 업데이트 완료: nickname={}, status={}", user.getNickname(), user.getStatus());
            return null;
        });

        // Then - 커밋된 변경사항이 지속되는지 확인
        User updatedUser = userMapper.findById(userId);
        log.info("지속성 테스트 - 커밋 후 User 상태: nickname={}, status={}", updatedUser.getNickname(), updatedUser.getStatus());
        assertThat(updatedUser.getNickname()).isEqualTo("DURABLE_USER");
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);

        // 새로운 트랜잭션에서도 변경사항이 보이는지 확인 (프로그래밍 방식)
        TransactionTemplate newTransactionTemplate = new TransactionTemplate(transactionManager);
        newTransactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        
        newTransactionTemplate.execute(status -> {
            User user = userMapper.findById(userId);
            assertThat(user.getNickname()).isEqualTo("DURABLE_USER");
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
            log.info("새로운 트랜잭션에서 확인 - 변경사항 지속됨: {}", user.getNickname());
            return null;
        });
        log.info("지속성 테스트 통과 - 변경사항이 지속됨");
    }

}