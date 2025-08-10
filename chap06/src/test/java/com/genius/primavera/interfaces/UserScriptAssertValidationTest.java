package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.UserWithScriptAssert;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

@Order(5)
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("@ScriptAssert Groovy file validation test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserScriptAssertValidationTest {

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
    private Validator validator;

    @Test
    @Order(1)
    @DisplayName("@ScriptAssertshould file connection validation")
    public void validatePasswordMismatchWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("testuser")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty(), "file connection should validation with file should");
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "file connection shouldneeds to be added should");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(2)
    @DisplayName("@ScriptAssertshould file test validation")
    public void validatePasswordMatchWithScriptAssert() {
        String password = "Secret0!";
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(1L)
                .email("test@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("testuser")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);

        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertFalse(hasPasswordMatchError, "file test should validation with connection should");

        if (!violations.isEmpty()) {
            System.out.println("validation error should:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
        } else {
            System.out.println("validation test: file test");
        }
    }

    @Test
    @Order(3)
    @DisplayName("@ScriptAssertshould null file validation")
    public void validateNullPasswordWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(1L)
                .email("test@gmail.com")
                .password(null)
                .passwordConfirm("Secret0!")
                .nickname("testuser")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "passwordshould nulltest validation should not should");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("@ScriptAssertshould file test validation")
    public void validateCaseSensitiveWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("secret0!")
                .nickname("testuser")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "file test file connectionshould validation should");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(5)
    @DisplayName("@ScriptAsserttest should validation")
    public void validateSpaceIncludedWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Secret0! ")
                .nickname("testuser")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "test should test file connectionshould validation should");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(6)
    @DisplayName("@ScriptAssertshould test validation - test error")
    public void validateMultipleErrorsWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(-1L)
                .email("invalid-email")
                .password("weak")
                .passwordConfirm("different")
                .nickname("")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);

        Assertions.assertFalse(violations.isEmpty(), "test validation with file should");
        Assertions.assertTrue(violations.size() >= 4, "test 4should connection validation with connection should");

        System.out.println("validation error should (" + violations.size() + "should):");
        violations.forEach(v -> System.out.println("- " + v.getPropertyPath() + ": " + v.getMessage()));

        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "file connection error needs to be added");
    }
}