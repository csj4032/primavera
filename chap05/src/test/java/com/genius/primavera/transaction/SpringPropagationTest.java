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
@EnableTestContainers
@DisplayName("Spring Transaction Propagation test")
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
    public void setUp() {
        testUser = User.builder()
                .email("propagation-test-" + System.currentTimeMillis() + "@example.com")
                .password("{bcrypt}$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e")
                .nickname("PROPAGATION_TESTER")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        transactionTemplate = new TransactionTemplate(transactionManager);
        winnerMapper.truncate();
    }

    @Test
    @Order(1)
    @DisplayName("REQUIRED - test Endpoint connection test, connection test")
    public void testRequired() {
        log.info("=== REQUIRED test ===");
        Long userId = userMapper.save(testUser);
        requiredMethod(userId, "Case1_NoTransaction");
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("Case1_NoTransaction");
        log.info("Case 1 test - file test called needs to be added file test");
        transactionalMethodWithRequired(userId);
        User updatedUser = userMapper.findById(userId);
        assertThat(updatedUser.getNickname()).isEqualTo("Case2_WithTransaction");
        log.info("Case 2 test - test file test");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("REQUIRED connection execution - {}", nickname);
    }

    @Transactional
    public void transactionalMethodWithRequired(Long userId) {
        requiredMethod(userId, "Case2_WithTransaction");
        log.info("test file connection REQUIRED connection called");
    }

    @Test
    @DisplayName("REQUIRES_NEW - test should file test, test file should test")
    public void testRequiresNew() {
        log.info("=== REQUIRES_NEW test ===");
        Long userId = userMapper.save(testUser);
        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                Winner winner = Winner.builder()
                        .name("REQUIRES_NEW Winner")
                        .year(2023)
                        .sport("Test Sport")
                        .prize("Test Prize")
                        .amount(new BigDecimal("100000.00"))
                        .build();
                winnerMapper.save(winner);
                log.info("test file - Winner creation: {}", winner.getId());
                TransactionTemplate requiresNewTemplate = new TransactionTemplate(transactionManager);
                requiresNewTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
                requiresNewTemplate.execute(innerStatus -> {
                    User user = userMapper.findById(userId);
                    user.setNickname("REQUIRES_NEW_COMMITTED");
                    userMapper.update(user);
                    log.info("REQUIRES_NEW connection execution - should file {}", "REQUIRES_NEW_COMMITTED");
                    return null;
                });
                throw new RuntimeException("test file exception test");
            });
        }).isInstanceOf(RuntimeException.class).hasMessage("test file exception test");
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("REQUIRES_NEW_COMMITTED");
        long winnerCount = winnerMapper.countByName("REQUIRES_NEW Winner");
        assertThat(winnerCount).isEqualTo(0);
        log.info("REQUIRES_NEW test - test file test, test file test");
    }

    @Transactional
    public void transactionalMethodWithRequiresNew(Long userId) {
        Winner winner = Winner.builder()
                .name("REQUIRES_NEW Winner")
                .year(2023)
                .sport("Test Sport")
                .prize("Test Prize")
                .amount(new BigDecimal("100000.00"))
                .build();
        winnerMapper.save(winner);
        log.info("test file - Winner creation: {}", winner.getId());
        requiresNewMethod(userId, "REQUIRES_NEW_COMMITTED");
        throw new RuntimeException("test file exception test");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("REQUIRES_NEW connection execution - should file {}", nickname);
    }

    @Test
    @DisplayName("SUPPORTS - Endpoint connection test, connection file test execution")
    public void testSupports() {
        log.info("=== SUPPORTS test ===");
        Long userId = userMapper.save(testUser);
        supportsMethod(userId, "SUPPORTS_NO_TX");
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("SUPPORTS_NO_TX");
        log.info("Case 1 test - file test execution");
        transactionalMethodWithSupports(userId);
        User updatedUser = userMapper.findById(userId);
        assertThat(updatedUser.getNickname()).isEqualTo("SUPPORTS_WITH_TX");
        log.info("Case 2 test - test file test");
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void supportsMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("SUPPORTS connection execution - {}", nickname);
    }

    @Transactional
    public void transactionalMethodWithSupports(Long userId) {
        supportsMethod(userId, "SUPPORTS_WITH_TX");
        log.info("file connection SUPPORTS connection called");
    }

    @Test
    @DisplayName("NOT_SUPPORTED - file test execution, test file should test")
    public void testNotSupported() {
        log.info("=== NOT_SUPPORTED test ===");
        Long userId = userMapper.save(testUser);
        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                Winner winner = Winner.builder()
                        .name("NOT_SUPPORTED Winner")
                        .year(2023)
                        .sport("Test Sport 2")
                        .prize("Test Prize 2")
                        .amount(new BigDecimal("200000.00"))
                        .build();
                winnerMapper.save(winner);
                log.info("test file - Winner creation: {}", winner.getId());
                TransactionTemplate notSupportedTemplate = new TransactionTemplate(transactionManager);
                notSupportedTemplate.setPropagationBehavior(Propagation.NOT_SUPPORTED.value());
                notSupportedTemplate.execute(innerStatus -> {
                    User user = userMapper.findById(userId);
                    user.setNickname("NOT_SUPPORTED_COMMITTED");
                    userMapper.update(user);
                    log.info("NOT_SUPPORTED connection execution - file test {}", "NOT_SUPPORTED_COMMITTED");
                    return null;
                });
                throw new RuntimeException("test file exception test");
            });
        }).isInstanceOf(RuntimeException.class).hasMessage("test file exception test");
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("NOT_SUPPORTED_COMMITTED");
        long winnerCount = winnerMapper.countByName("NOT_SUPPORTED Winner");
        assertThat(winnerCount).isEqualTo(0);
        log.info("NOT_SUPPORTED test - file test execution processing test");
    }

    @Transactional
    public void transactionalMethodWithNotSupported(Long userId) {
        Winner winner = Winner.builder()
                .name("NOT_SUPPORTED Winner")
                .year(2023)
                .sport("Test Sport 2")
                .prize("Test Prize 2")
                .amount(new BigDecimal("200000.00"))
                .build();
        winnerMapper.save(winner);
        log.info("test file - Winner creation: {}", winner.getId());
        notSupportedMethod(userId, "NOT_SUPPORTED_COMMITTED");
        throw new RuntimeException("test file exception test");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void notSupportedMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("NOT_SUPPORTED connection execution - file test {}", nickname);
    }

    @Test
    @DisplayName("MANDATORY - should test file connection execution, connection exception")
    public void testMandatory() {
        log.info("=== MANDATORY test ===");
        Long userId = userMapper.save(testUser);
        assertThatThrownBy(() -> {
            TransactionTemplate mandatoryTemplate = new TransactionTemplate(transactionManager);
            mandatoryTemplate.setPropagationBehavior(Propagation.MANDATORY.value());
            mandatoryTemplate.execute(status -> {
                User user = userMapper.findById(userId);
                user.setNickname("MANDATORY_FAIL");
                userMapper.update(user);
                log.info("MANDATORY connection execution - {}", "MANDATORY_FAIL");
                return null;
            });
        }).isInstanceOf(IllegalTransactionStateException.class);
        log.info("Case 1 test - file test called should exception test");
        transactionTemplate.execute(status -> {
            TransactionTemplate mandatoryTemplate = new TransactionTemplate(transactionManager);
            mandatoryTemplate.setPropagationBehavior(Propagation.MANDATORY.value());
            return mandatoryTemplate.execute(innerStatus -> {
                User user = userMapper.findById(userId);
                user.setNickname("MANDATORY_SUCCESS");
                userMapper.update(user);
                log.info("MANDATORY connection execution - {}", "MANDATORY_SUCCESS");
                log.info("file connection MANDATORY connection called");
                return null;
            });
        });
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("MANDATORY_SUCCESS");
        log.info("Case 2 test - file connection test execution");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void mandatoryMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("MANDATORY connection execution - {}", nickname);
    }

    @Transactional
    public void transactionalMethodWithMandatory(Long userId) {
        mandatoryMethod(userId, "MANDATORY_SUCCESS");
        log.info("file connection MANDATORY connection called");
    }

    @Test
    @DisplayName("NEVER - should file test execution, Endpoint connection exception")
    public void testNever() {
        log.info("=== NEVER test ===");
        Long userId = userMapper.save(testUser);
        neverMethod(userId, "NEVER_SUCCESS");
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("NEVER_SUCCESS");
        log.info("Case 1 test - file test execution");
        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                log.info("test file test");
                TransactionTemplate neverTransactionTemplate = new TransactionTemplate(transactionManager);
                neverTransactionTemplate.setPropagationBehavior(Propagation.NEVER.value());
                return neverTransactionTemplate.execute(neverStatus -> {
                    User neverUser = userMapper.findById(userId);
                    neverUser.setNickname("NEVER_FAIL");
                    userMapper.update(neverUser);
                    log.info("should connection file connection should");
                    return null;
                });
            });
        }).isInstanceOf(IllegalTransactionStateException.class);
        log.info("Case 2 test - file connection NEVER called should exception test");
    }

    @Transactional(propagation = Propagation.NEVER)
    public void neverMethod(Long userId, String nickname) {
        User user = userMapper.findById(userId);
        user.setNickname(nickname);
        userMapper.update(user);
        log.info("NEVER connection execution - {}", nickname);
    }

    @Test
    @DisplayName("NESTED - test file, test file testshould test test")
    public void testNested() {
        log.info("=== NESTED test ===");
        Long userId = userMapper.save(testUser);
        transactionTemplate.execute(status -> {

            User user = userMapper.findById(userId);
            user.setNickname("OUTER_TRANSACTION");
            userMapper.update(user);
            log.info("test file - User should: {}", user.getNickname());
            try {
                TransactionTemplate nestedTemplate = new TransactionTemplate(transactionManager);
                nestedTemplate.setPropagationBehavior(Propagation.NESTED.value());
                nestedTemplate.execute(nestedStatus -> {
                    Winner winner = Winner.builder()
                            .name("NESTED Winner")
                            .year(2023)
                            .sport("Test Sport 3")
                            .prize("Test Prize 3")
                            .amount(new BigDecimal("300000.00"))
                            .build();
                    winnerMapper.save(winner);
                    log.info("test file - Winner creation: {}", winner.getId());
                    throw new RuntimeException("test file exception test");
                });
            } catch (RuntimeException e) {
                log.info("test file exception processing: {}", e.getMessage());
            }
            log.info("test file test");
            return null;
        });
        User user = userMapper.findById(userId);
        assertThat(user.getNickname()).isEqualTo("OUTER_TRANSACTION");
        long winnerCount = winnerMapper.countByName("NESTED Winner");
        assertThat(winnerCount).isEqualTo(0);
        log.info("NESTED test - test file test, test file test");
    }

    @Transactional
    public void transactionalMethodWithNested(Long userId) {
        User user = userMapper.findById(userId);
        user.setNickname("OUTER_TRANSACTION");
        userMapper.update(user);
        log.info("test file - User should: {}", user.getNickname());
        try {
            nestedMethod(userId);
        } catch (RuntimeException e) {
            log.info("test file exception processing: {}", e.getMessage());
        }
        log.info("test file test");
    }

    @Transactional(propagation = Propagation.NESTED)
    public void nestedMethod(Long userId) {
        Winner winner = Winner.builder()
                .name("NESTED Winner")
                .year(2023)
                .sport("Test Sport 3")
                .prize("Test Prize 3")
                .amount(new BigDecimal("300000.00"))
                .build();
        winnerMapper.save(winner);
        log.info("test file - Winner creation: {}", winner.getId());
        throw new RuntimeException("test file exception test");
    }
}