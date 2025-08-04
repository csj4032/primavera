package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.UserWithScriptAssert;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

@Order(5)
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("@ScriptAssert Groovy 스크립트 검증 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserScriptAssertValidationTest {

    @Autowired
    private Validator validator;

    @Test
    @Order(1)
    @DisplayName("@ScriptAssert로 비밀번호 불일치 검증")
    public void validatePasswordMismatchWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("testuser")
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty(), "비밀번호 불일치 시 검증 오류가 발생해야 함");
        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "비밀번호 불일치 메시지가 포함되어야 함");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(2)
    @DisplayName("@ScriptAssert로 비밀번호 일치 검증")
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
        Assertions.assertFalse(hasPasswordMatchError, "비밀번호 일치 시 검증 오류가 없어야 함");

        if (!violations.isEmpty()) {
            System.out.println("검증 오류 메시지들:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
        } else {
            System.out.println("검증 통과: 비밀번호 일치");
        }
    }

    @Test
    @Order(3)
    @DisplayName("@ScriptAssert로 null 비밀번호 검증")
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
        Assertions.assertTrue(hasPasswordMatchError, "password가 null이면 검증 실패해야 함");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(4)
    @DisplayName("@ScriptAssert로 대소문자 구분 검증")
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
        Assertions.assertTrue(hasPasswordMatchError, "대소문자 다른 비밀번호는 불일치로 검증되어야 함");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(5)
    @DisplayName("@ScriptAssert로 공백 포함 검증")
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
        Assertions.assertTrue(hasPasswordMatchError, "공백 차이가 있는 비밀번호는 불일치로 검증되어야 함");
        violations.forEach(v -> System.out.println("- " + v.getMessage()));
    }

    @Test
    @Order(6)
    @DisplayName("@ScriptAssert로 복합 검증 - 여러 필드 오류")
    public void validateMultipleErrorsWithScriptAssert() {
        UserWithScriptAssert user = UserWithScriptAssert.builder()
                .id(-1L)  // Min 어노테이션 위반
                .email("invalid-email")  // Email 어노테이션 위반
                .password("weak")  // Pattern 어노테이션 위반
                .passwordConfirm("different")  // ScriptAssert 위반
                .nickname("")  // NotBlank 어노테이션 위반
                .build();

        Set<ConstraintViolation<UserWithScriptAssert>> violations = validator.validate(user);

        Assertions.assertFalse(violations.isEmpty(), "여러 검증 오류가 발생해야 함");
        Assertions.assertTrue(violations.size() >= 4, "최소 4개 이상의 검증 오류가 있어야 함");

        System.out.println("검증 오류 메시지들 (" + violations.size() + "개):");
        violations.forEach(v -> System.out.println("- " + v.getPropertyPath() + ": " + v.getMessage()));

        boolean hasPasswordMatchError = violations.stream().anyMatch(v -> v.getMessage().contains("Passwords do not match"));
        Assertions.assertTrue(hasPasswordMatchError, "비밀번호 불일치 오류도 포함되어야 함");
    }
}