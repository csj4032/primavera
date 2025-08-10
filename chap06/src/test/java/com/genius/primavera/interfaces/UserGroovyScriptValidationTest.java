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
@DisplayName("Groovy file validation test")
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
    @DisplayName("connection @PasswordMatch annotationtest file connection validation")
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
        Assertions.assertFalse(violations.isEmpty(), "file connection should validation with file should");
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("file created successfully file."));
        Assertions.assertTrue(hasPasswordMatchError, "file connection shouldneeds to be added should");
    }

    @Test
    @Order(2)
    @DisplayName("connection @PasswordMatch annotationtest file test validation")
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
        Assertions.assertFalse(hasPasswordMatchError, "file test should validation with connection should");
    }

    @Test
    @Order(3)
    @DisplayName("file created successfully nullshould test validation test (test connection)")
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
        Assertions.assertFalse(hasPasswordMatchError, "passwordConfirmshould nullshould test validation test should");
    }

    @Test
    @Order(4)
    @DisplayName("fileshould nullshould file created successfully test validation failure")
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
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("file created successfully file."));
        Assertions.assertTrue(hasPasswordMatchError, "passwordshould nullshould passwordConfirmshould connection validation failure should");
    }

    @Test
    @Order(5)
    @DisplayName("file test file connection validation")
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
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("file created successfully file."));
        Assertions.assertTrue(hasPasswordMatchError, "file test file connection validation should");
    }

    @Test
    @Order(6)
    @DisplayName("test should file connection validation")
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
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("file created successfully file."));
        Assertions.assertTrue(hasPasswordMatchError, "test shouldshould test file connection validation should");
    }

    @Test
    @Order(7)
    @DisplayName("UpdateGroup validation file test validation")
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
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("file created successfully file."));
        Assertions.assertTrue(hasPasswordMatchError, "UpdateGroupconnection file connection validationshould file should");
    }

    @Test
    @Order(8)
    @DisplayName("SaveGroup validation file test validation")
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
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("file created successfully file."));
        Assertions.assertTrue(hasPasswordMatchError, "SaveGroupconnection file connection validationshould file should");
    }
}