package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers
@DisplayName("Groovy 스크립트 검증 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserGroovyScriptValidationTest {

    @Autowired
    private Validator validator;
    
    @MockBean
    private UserService userService;

    @Test
    @Order(1)
    @DisplayName("커스텀 @PasswordMatch 어노테이션으로 비밀번호 불일치 검증")
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
        
        Assertions.assertFalse(violations.isEmpty(), "비밀번호 불일치 시 검증 오류가 발생해야 함");
        boolean hasPasswordMatchError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "비밀번호 불일치 메시지가 포함되어야 함");
    }

    @Test
    @Order(2)
    @DisplayName("커스텀 @PasswordMatch 어노테이션으로 비밀번호 일치 검증")
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
        
        boolean hasPasswordMatchError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertFalse(hasPasswordMatchError, "비밀번호 일치 시 검증 오류가 없어야 함");
    }

    @Test
    @Order(3)
    @DisplayName("비밀번호 확인이 null인 경우 검증 통과 (기존 호환성)")
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
        
        boolean hasPasswordMatchError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertFalse(hasPasswordMatchError, "passwordConfirm이 null인 경우 검증 통과해야 함");
    }

    @Test
    @Order(4)
    @DisplayName("비밀번호가 null이고 비밀번호 확인이 있는 경우 검증 실패")
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
        
        boolean hasPasswordMatchError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "password가 null이고 passwordConfirm이 있으면 검증 실패해야 함");
    }

    @Test
    @Order(5)
    @DisplayName("대소문자 구분 비밀번호 불일치 검증")
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
        
        boolean hasPasswordMatchError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "대소문자 다른 비밀번호는 불일치로 검증되어야 함");
    }

    @Test
    @Order(6)
    @DisplayName("공백 포함 비밀번호 불일치 검증")
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
        
        boolean hasPasswordMatchError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "공백 차이가 있는 비밀번호는 불일치로 검증되어야 함");
    }

    @Test
    @Order(7)
    @DisplayName("UpdateGroup 검증 그룹에서 비밀번호 매치 검증")
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
        
        boolean hasPasswordMatchError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "UpdateGroup에서도 비밀번호 불일치 검증이 작동해야 함");
    }

    @Test
    @Order(8)
    @DisplayName("SaveGroup 검증 그룹에서 비밀번호 매치 검증")
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
        
        boolean hasPasswordMatchError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "SaveGroup에서도 비밀번호 불일치 검증이 작동해야 함");
    }
}