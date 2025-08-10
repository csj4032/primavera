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
@DisplayName("@ScriptAssert Groovy translated_text_4 validation test")
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
    @DisplayName("@ScriptAsserttranslated_text_1 translated_text_4 translated_text_3 validation")
    public void validatePasswordMismatchWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("testuser")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty(), "translated_text_4 translated_text_3 translated_text_1 validation translated_text_6 translated_text_4 translated_text_1");
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "translated_text_4 translated_text_3 translated_text_1translated_text_1 translated_text_1 translated_text_1");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(2)
    @DisplayName("@ScriptAsserttranslated_text_1 translated_text_4 translated_text_2 validation")
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
        Assertions.assertFalse(hasPasswordMatchError, "translated_text_4 translated_text_2 translated_text_1 validation translated_text_6 translated_text_3 translated_text_1");

        if (!violations.isEmpty()) {
            System.out.println("validation error translated_text_1:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
        } else {
            System.out.println("validation translated_text_2: translated_text_4 translated_text_2");
        }
    }

    @Test
    @Order(3)
    @DisplayName("@ScriptAsserttranslated_text_1 null translated_text_4 validation")
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
        Assertions.assertTrue(hasPasswordMatchError, "passwordtranslated_text_1 nulltranslated_text_2 validation translated_text_9 translated_text_1");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("@ScriptAsserttranslated_text_1 translated_text_4 translated_text_2 validation")
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
        Assertions.assertTrue(hasPasswordMatchError, "translated_text_4 translated_text_2 translated_text_4 translated_text_3translated_text_1 validation translated_text_1");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(5)
    @DisplayName("@ScriptAsserttranslated_text_1 translated_text_2 translated_text_1 validation")
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
        Assertions.assertTrue(hasPasswordMatchError, "translated_text_2 translated_text_1 translated_text_2 translated_text_4 translated_text_3translated_text_1 validation translated_text_1");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(6)
    @DisplayName("@ScriptAsserttranslated_text_1 translated_text_2 validation - translated_text_2 translated_text_2 error")
    public void validateMultipleErrorsWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(-1L)
                .email("invalid-email")
                .password("weak")
                .passwordConfirm("different")
                .nickname("")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);

        Assertions.assertFalse(violations.isEmpty(), "translated_text_2 validation translated_text_6 translated_text_4 translated_text_1");
        Assertions.assertTrue(violations.size() >= 4, "translated_text_2 4translated_text_1 translated_text_3 validation translated_text_6 translated_text_3 translated_text_1");

        System.out.println("validation error translated_text_1 (" + violations.size() + "translated_text_1):");
        violations.forEach(v -> System.out.println("- " + v.getPropertyPath() + ": " + v.getMessage()));

        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "translated_text_4 translated_text_3 error translated_text_1 translated_text_1");
    }
}