package com.genius.primavera.transaction;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.testcontainer.EnablePrimaveraTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@EnablePrimaveraTestcontainers
@DisplayName("Spring Transaction Propagation 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpringPropagationTest {

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
                .email("propagation-test-" + System.currentTimeMillis() + "@example.com")
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("PROPAGATION_TESTER")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // TransactionTemplate 초기화
        transactionTemplate = new TransactionTemplate(transactionManager);

        // 테스트 전에 Winner 테이블 정리
        winnerMapper.truncate();
    }

    @Test
    @Order(1)
    @DisplayName("REQUIRED - 진행 중인 트랜잭션이 있으면 참여, 없으면 새로 시작")
    void testRequired() {
        // Case 1: 트랜잭션 없이 호출
        log.info("=== REQUIRED 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        requiredMethod(userId, "Case1_NoTransaction");

        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("Case1_NoTransaction");
        log.info("Case 1 통과 - 트랜잭션 없이 호출 시 새 트랜잭션 시작");

        // Case 2: 기존 트랜잭션 내에서 호출
        transactionalMethodWithRequired(userId);

        User updatedUser = userMapper.findById(userId);
        assertThat(updatedUser.getNickname()).isEqualTo("Case2_WithTransaction");
        log.info("Case 2 통과 - 기존 트랜잭션에 참여");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void requiredMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("REQUIRED 메서드 실행 - {}", nickname);
    }

    @Transactional
    void transactionalMethodWithRequired(Long userId) {
        requiredMethod(userId, "Case2_WithTransaction");
        log.info("기존 트랜잭션 내에서 REQUIRED 메서드 호출");
    }

    @Test
    @DisplayName("REQUIRES_NEW - 항상 새 트랜잭션 시작, 기존 트랜잭션은 일시 중단")
    void testRequiresNew() {
        log.info("=== REQUIRES_NEW 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        // 기존 트랜잭션 내에서 REQUIRES_NEW 호출 (프로그래밍 방식)
        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                // 외부 트랜잭션에서 Winner 생성
                Winner winner = Winner.builder()
                        .name("REQUIRES_NEW Winner")
                        .year(2023)
                        .sport("Test Sport")
                        .prize("Test Prize")
                        .amount(new BigDecimal("100000.00"))
                        .build();
                winnerMapper.save(winner);
                log.info("외부 트랜잭션 - Winner 생성: {}", winner.getId());

                // REQUIRES_NEW로 새 트랜잭션 시작
                TransactionTemplate requiresNewTemplate = new TransactionTemplate(transactionManager);
                requiresNewTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());

                requiresNewTemplate.execute(innerStatus -> {
                    User user = userMapper.findById(userId);
                    user.setNickname("REQUIRES_NEW_COMMITTED");
                    userMapper.update(user);
                    log.info("REQUIRES_NEW 메서드 실행 - 새 트랜잭션에서 {}", "REQUIRES_NEW_COMMITTED");
                    return null;
                });

                // 외부 트랜잭션에서 예외 발생
                throw new RuntimeException("외부 트랜잭션에서 예외 발생");
            });
        }).isInstanceOf(RuntimeException.class)
                .hasMessage("외부 트랜잭션에서 예외 발생");

        // REQUIRES_NEW로 실행된 내부 트랜잭션은 커밋되어야 함
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("REQUIRES_NEW_COMMITTED");

        // 외부 트랜잭션에서 생성된 Winner는 롤백되어야 함
        long winnerCount = winnerMapper.countByName("REQUIRES_NEW Winner");
        assertThat(winnerCount).isEqualTo(0);

        log.info("REQUIRES_NEW 테스트 통과 - 내부 트랜잭션은 커밋, 외부 트랜잭션은 롤백");
    }

    @Transactional
    void transactionalMethodWithRequiresNew(Long userId) {
        // 외부 트랜잭션에서 Winner 생성
        Winner winner = Winner.builder()
                .name("REQUIRES_NEW Winner")
                .year(2023)
                .sport("Test Sport")
                .prize("Test Prize")
                .amount(new BigDecimal("100000.00"))
                .build();
        winnerMapper.save(winner);
        log.info("외부 트랜잭션 - Winner 생성: {}", winner.getId());

        // REQUIRES_NEW로 새 트랜잭션 시작
        requiresNewMethod(userId, "REQUIRES_NEW_COMMITTED");

        // 외부 트랜잭션에서 예외 발생
        throw new RuntimeException("외부 트랜잭션에서 예외 발생");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void requiresNewMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("REQUIRES_NEW 메서드 실행 - 새 트랜잭션에서 {}", nickname);
    }

    @Test
    @DisplayName("SUPPORTS - 트랜잭션이 있으면 참여, 없으면 트랜잭션 없이 실행")
    void testSupports() {
        log.info("=== SUPPORTS 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        // Case 1: 트랜잭션 없이 호출
        supportsMethod(userId, "SUPPORTS_NO_TX");

        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("SUPPORTS_NO_TX");
        log.info("Case 1 통과 - 트랜잭션 없이 실행");

        // Case 2: 트랜잭션 내에서 호출
        transactionalMethodWithSupports(userId);

        User updatedUser = userMapper.findById(userId);
        assertThat(updatedUser.getNickname()).isEqualTo("SUPPORTS_WITH_TX");
        log.info("Case 2 통과 - 기존 트랜잭션에 참여");
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    void supportsMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("SUPPORTS 메서드 실행 - {}", nickname);
    }

    @Transactional
    void transactionalMethodWithSupports(Long userId) {
        supportsMethod(userId, "SUPPORTS_WITH_TX");
        log.info("트랜잭션 내에서 SUPPORTS 메서드 호출");
    }

    @Test
    @DisplayName("NOT_SUPPORTED - 트랜잭션 없이 실행, 기존 트랜잭션은 일시 중단")
    void testNotSupported() {
        log.info("=== NOT_SUPPORTED 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        // 트랜잭션 내에서 NOT_SUPPORTED 호출 (프로그래밍 방식)
        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                // 외부 트랜잭션에서 Winner 생성
                Winner winner = Winner.builder()
                        .name("NOT_SUPPORTED Winner")
                        .year(2023)
                        .sport("Test Sport 2")
                        .prize("Test Prize 2")
                        .amount(new BigDecimal("200000.00"))
                        .build();
                winnerMapper.save(winner);
                log.info("외부 트랜잭션 - Winner 생성: {}", winner.getId());

                // NOT_SUPPORTED로 트랜잭션 없이 실행
                TransactionTemplate notSupportedTemplate = new TransactionTemplate(transactionManager);
                notSupportedTemplate.setPropagationBehavior(Propagation.NOT_SUPPORTED.value());

                notSupportedTemplate.execute(innerStatus -> {
                    User user = userMapper.findById(userId);
                    user.setNickname("NOT_SUPPORTED_COMMITTED");
                    userMapper.update(user);
                    log.info("NOT_SUPPORTED 메서드 실행 - 트랜잭션 없이 {}", "NOT_SUPPORTED_COMMITTED");
                    return null;
                });

                // 외부 트랜잭션에서 예외 발생
                throw new RuntimeException("외부 트랜잭션에서 예외 발생");
            });
        }).isInstanceOf(RuntimeException.class)
                .hasMessage("외부 트랜잭션에서 예외 발생");

        // NOT_SUPPORTED 메서드의 변경사항은 커밋되어야 함 (트랜잭션 없이 실행)
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("NOT_SUPPORTED_COMMITTED");

        // 외부 트랜잭션의 Winner는 롤백되어야 함
        long winnerCount = winnerMapper.countByName("NOT_SUPPORTED Winner");
        assertThat(winnerCount).isEqualTo(0);

        log.info("NOT_SUPPORTED 테스트 통과 - 트랜잭션 없이 실행된 변경사항은 커밋");
    }

    @Transactional
    void transactionalMethodWithNotSupported(Long userId) {
        // 외부 트랜잭션에서 Winner 생성
        Winner winner = Winner.builder()
                .name("NOT_SUPPORTED Winner")
                .year(2023)
                .sport("Test Sport 2")
                .prize("Test Prize 2")
                .amount(new BigDecimal("200000.00"))
                .build();
        winnerMapper.save(winner);
        log.info("외부 트랜잭션 - Winner 생성: {}", winner.getId());

        // NOT_SUPPORTED로 트랜잭션 없이 실행
        notSupportedMethod(userId, "NOT_SUPPORTED_COMMITTED");

        // 외부 트랜잭션에서 예외 발생
        throw new RuntimeException("외부 트랜잭션에서 예외 발생");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void notSupportedMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("NOT_SUPPORTED 메서드 실행 - 트랜잭션 없이 {}", nickname);
    }

    @Test
    @DisplayName("MANDATORY - 반드시 기존 트랜잭션 내에서 실행, 없으면 예외")
    void testMandatory() {
        log.info("=== MANDATORY 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        // Case 1: 트랜잭션 없이 호출 시 예외 발생 (프로그래밍 방식)
        assertThatThrownBy(() -> {
            TransactionTemplate mandatoryTemplate = new TransactionTemplate(transactionManager);
            mandatoryTemplate.setPropagationBehavior(Propagation.MANDATORY.value());

            mandatoryTemplate.execute(status -> {
                User user = userMapper.findById(userId);
                user.setNickname("MANDATORY_FAIL");
                userMapper.update(user);
                log.info("MANDATORY 메서드 실행 - {}", "MANDATORY_FAIL");
                return null;
            });
        }).isInstanceOf(IllegalTransactionStateException.class);
        log.info("Case 1 통과 - 트랜잭션 없이 호출 시 예외 발생");

        // Case 2: 트랜잭션 내에서 호출 시 정상 실행 (프로그래밍 방식)
        transactionTemplate.execute(status -> {
            TransactionTemplate mandatoryTemplate = new TransactionTemplate(transactionManager);
            mandatoryTemplate.setPropagationBehavior(Propagation.MANDATORY.value());

            return mandatoryTemplate.execute(innerStatus -> {
                User user = userMapper.findById(userId);
                user.setNickname("MANDATORY_SUCCESS");
                userMapper.update(user);
                log.info("MANDATORY 메서드 실행 - {}", "MANDATORY_SUCCESS");
                log.info("트랜잭션 내에서 MANDATORY 메서드 호출");
                return null;
            });
        });

        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("MANDATORY_SUCCESS");
        log.info("Case 2 통과 - 트랜잭션 내에서 정상 실행");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    void mandatoryMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("MANDATORY 메서드 실행 - {}", nickname);
    }

    @Transactional
    void transactionalMethodWithMandatory(Long userId) {
        mandatoryMethod(userId, "MANDATORY_SUCCESS");
        log.info("트랜잭션 내에서 MANDATORY 메서드 호출");
    }

    @Test
    @DisplayName("NEVER - 반드시 트랜잭션 없이 실행, 트랜잭션이 있으면 예외")
    void testNever() {
        log.info("=== NEVER 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        // Case 1: 트랜잭션 없이 호출 시 정상 실행
        neverMethod(userId, "NEVER_SUCCESS");

        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("NEVER_SUCCESS");
        log.info("Case 1 통과 - 트랜잭션 없이 정상 실행");

        // Case 2: 트랜잭션 내에서 NEVER 호출 시 예외 발생 (프로그래밍 방식)
        assertThatThrownBy(() -> {
            // 기존 트랜잭션을 시작하고 그 안에서 NEVER 호출
            transactionTemplate.execute(status -> {
                log.info("외부 트랜잭션 시작됨");

                // NEVER 전파 설정으로 TransactionTemplate 생성
                TransactionTemplate neverTransactionTemplate = new TransactionTemplate(transactionManager);
                neverTransactionTemplate.setPropagationBehavior(Propagation.NEVER.value());

                // NEVER 전파로 실행 시도 - 예외 발생해야 함
                return neverTransactionTemplate.execute(neverStatus -> {
                    User neverUser = userMapper.findById(userId);
                    neverUser.setNickname("NEVER_FAIL");
                    userMapper.update(neverUser);
                    log.info("이 로그는 출력되지 않아야 함");
                    return null;
                });
            });
        }).isInstanceOf(IllegalTransactionStateException.class);
        log.info("Case 2 통과 - 트랜잭션 내에서 NEVER 호출 시 예외 발생");
    }

    @Transactional(propagation = Propagation.NEVER)
    void neverMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("NEVER 메서드 실행 - {}", nickname);
    }


    @Test
    @DisplayName("NESTED - 중첩 트랜잭션, 내부 트랜잭션 롤백이 외부에 영향 없음")
    void testNested() {
        log.info("=== NESTED 테스트 시작 ===");
        Long userId = userMapper.save(testUser);

        // 외부 트랜잭션은 성공, 내부 중첩 트랜잭션은 실패 (프로그래밍 방식)
        transactionTemplate.execute(status -> {
            // 외부 트랜잭션에서 User 업데이트
            User user = userMapper.findById(userId);
            user.setNickname("OUTER_TRANSACTION");
            userMapper.update(user);
            log.info("외부 트랜잭션 - User 업데이트: {}", user.getNickname());

            // 중첩 트랜잭션 실행 (예외 발생하지만 외부 트랜잭션에 영향 없음)
            try {
                TransactionTemplate nestedTemplate = new TransactionTemplate(transactionManager);
                nestedTemplate.setPropagationBehavior(Propagation.NESTED.value());

                nestedTemplate.execute(nestedStatus -> {
                    // 중첩 트랜잭션에서 Winner 생성
                    Winner winner = Winner.builder()
                            .name("NESTED Winner")
                            .year(2023)
                            .sport("Test Sport 3")
                            .prize("Test Prize 3")
                            .amount(new BigDecimal("300000.00"))
                            .build();
                    winnerMapper.save(winner);
                    log.info("중첩 트랜잭션 - Winner 생성: {}", winner.getId());

                    // 중첩 트랜잭션에서 예외 발생
                    throw new RuntimeException("중첩 트랜잭션에서 예외 발생");
                });
            } catch (RuntimeException e) {
                log.info("중첩 트랜잭션 예외 처리: {}", e.getMessage());
            }

            log.info("외부 트랜잭션 계속 진행");
            return null;
        });

        // 외부 트랜잭션의 변경사항은 커밋되어야 함
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("OUTER_TRANSACTION");

        // 내부 중첩 트랜잭션의 Winner는 롤백되어야 함
        long winnerCount = winnerMapper.countByName("NESTED Winner");
        assertThat(winnerCount).isEqualTo(0);

        log.info("NESTED 테스트 통과 - 외부 트랜잭션 커밋, 내부 중첩 트랜잭션 롤백");
    }

    @Transactional
    void transactionalMethodWithNested(Long userId) {
        // 외부 트랜잭션에서 User 업데이트
        User user = userMapper.findById(userId);
        user.setNickname("OUTER_TRANSACTION");
        userMapper.update(user);
        log.info("외부 트랜잭션 - User 업데이트: {}", user.getNickname());

        // 중첩 트랜잭션 실행 (예외 발생하지만 외부 트랜잭션에 영향 없음)
        try {
            nestedMethod(userId);
        } catch (RuntimeException e) {
            log.info("중첩 트랜잭션 예외 처리: {}", e.getMessage());
        }

        log.info("외부 트랜잭션 계속 진행");
    }

    @Transactional(propagation = Propagation.NESTED)
    void nestedMethod(Long userId) {
        // 중첩 트랜잭션에서 Winner 생성
        Winner winner = Winner.builder()
                .name("NESTED Winner")
                .year(2023)
                .sport("Test Sport 3")
                .prize("Test Prize 3")
                .amount(new BigDecimal("300000.00"))
                .build();
        winnerMapper.save(winner);
        log.info("중첩 트랜잭션 - Winner 생성: {}", winner.getId());

        // 중첩 트랜잭션에서 예외 발생
        throw new RuntimeException("중첩 트랜잭션에서 예외 발생");
    }
}