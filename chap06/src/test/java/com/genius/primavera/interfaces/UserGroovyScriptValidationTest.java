package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Groovy translated_text_4 validation test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserGroovyScriptValidationTest {

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
    @DisplayName("translated_text_3 @PasswordMatch annotationtranslated_text_2 translated_text_4 translated_text_3 validation")
    public void validatePasswordMismatchWithCustomAnnotation() {
        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("testuser")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, Default.class);
        Assertions.assertFalse(violations.isEmpty(), "translated_text_4 translated_text_3 translated_text_1 validation translated_text_6 translated_text_4 translated_text_1");
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("translated_text_4 translated_text_4 translated_text_13 translated_text_4 translated_text_4."));
        Assertions.assertTrue(hasPasswordMatchError, "translated_text_4 translated_text_3 translated_text_1translated_text_1 translated_text_1 translated_text_1");
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_3 @PasswordMatch annotationtranslated_text_2 translated_text_4 translated_text_2 validation")
    public void validatePasswordMatchWithCustomAnnotation() {
        String password = "Secret0!";
        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("testuser")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, Default.class);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertFalse(hasPasswordMatchError, "translated_text_4 translated_text_2 translated_text_1 validation translated_text_6 translated_text_3 translated_text_1");
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_4 translated_text_13 nulltranslated_text_1 translated_text_2 validation translated_text_2 (translated_text_2 translated_text_3)")
    public void validatePasswordConfirmNullAllowed() {
        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm(null)
                .nickname("testuser")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, Default.class);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertFalse(hasPasswordMatchError, "passwordConfirmtranslated_text_1 nulltranslated_text_1 translated_text_2 validation translated_text_2 translated_text_1");
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_4translated_text_1 nulltranslated_text_1 translated_text_4 translated_text_13 translated_text_2 translated_text_2 validation failure")
    public void validatePasswordNullConfirmExists() {
        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password(null)
                .passwordConfirm("Secret0!")
                .nickname("testuser")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, Default.class);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("translated_text_4 translated_text_4 translated_text_13 translated_text_4 translated_text_4."));
        Assertions.assertTrue(hasPasswordMatchError, "passwordtranslated_text_1 nulltranslated_text_1 passwordConfirmtranslated_text_1 translated_text_3 validation failure translated_text_1");
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_4 translated_text_2 translated_text_4 translated_text_3 validation")
    public void validatePasswordCaseSensitiveMismatch() {
        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("secret0!")
                .nickname("testuser")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, Default.class);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("translated_text_4 translated_text_4 translated_text_13 translated_text_4 translated_text_4."));
        Assertions.assertTrue(hasPasswordMatchError, "translated_text_4 translated_text_2 translated_text_4 translated_text_3 validation translated_text_1");
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_2 translated_text_1 translated_text_4 translated_text_3 validation")
    public void validatePasswordWithSpaceMismatch() {
        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Secret0! ")
                .nickname("testuser")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, Default.class);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("translated_text_4 translated_text_4 translated_text_13 translated_text_4 translated_text_4."));
        Assertions.assertTrue(hasPasswordMatchError, "translated_text_2 translated_text_1translated_text_1 translated_text_2 translated_text_4 translated_text_3 validation translated_text_1");
    }

    @Test
    @Order(7)
    @DisplayName("UpdateGroup validation translated_text_4 translated_text_4 translated_text_2 validation")
    public void validatePasswordMatchWithUpdateGroup() {
        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("testuser")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, User.UpdateGroup.class);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("translated_text_4 translated_text_4 translated_text_13 translated_text_4 translated_text_4."));
        Assertions.assertTrue(hasPasswordMatchError, "UpdateGrouptranslated_text_3 translated_text_4 translated_text_3 validationtranslated_text_1 translated_text_4 translated_text_1");
    }

    @Test
    @Order(8)
    @DisplayName("SaveGroup validation translated_text_4 translated_text_4 translated_text_2 validation")
    public void validatePasswordMatchWithSaveGroup() {
        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("testuser")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, User.SaveGroup.class);
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("translated_text_4 translated_text_4 translated_text_13 translated_text_4 translated_text_4."));
        Assertions.assertTrue(hasPasswordMatchError, "SaveGrouptranslated_text_3 translated_text_4 translated_text_3 validationtranslated_text_1 translated_text_4 translated_text_1");
    }
}