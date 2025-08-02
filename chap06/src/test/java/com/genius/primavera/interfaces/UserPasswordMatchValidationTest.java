package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Order(3)
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers
@DisplayName("비밀번호 매치 검증 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserPasswordMatchValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserService userService;

    @Test
    @Order(1)
    @DisplayName("사용자 등록 시 비밀번호와 비밀번호 확인이 일치하지 않을 때 400 Bad Request 반환")
    public void saveAndUserPasswordMismatch() {
        User source = User.builder()
                .id(1L)
                .email("mismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("mismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(2)
    @DisplayName("사용자 등록 시 비밀번호는 있지만 비밀번호 확인이 빈 문자열일 때 400 Bad Request 반환")
    public void saveAndUserPasswordConfirmEmpty() {
        User source = User.builder()
                .id(1L)
                .email("confirmempty@gmail.com")
                .password("Secret0!")
                .passwordConfirm("")
                .nickname("confirmempty")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(3)
    @DisplayName("사용자 등록 시 비밀번호는 null이고 비밀번호 확인이 있을 때 400 Bad Request 반환")
    public void saveAndUserPasswordNullConfirmExists() {
        User source = User.builder()
                .id(1L)
                .email("passnull@gmail.com")
                .password(null)
                .passwordConfirm("Secret0!")
                .nickname("passnull")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(4)
    @DisplayName("사용자 등록 시 대소문자가 다른 비밀번호 불일치 400 Bad Request 반환")
    public void saveAndUserPasswordCaseMismatch() {
        User source = User.builder()
                .id(1L)
                .email("casemismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("secret0!")
                .nickname("casemismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(5)
    @DisplayName("사용자 등록 시 공백 포함된 비밀번호 불일치 400 Bad Request 반환")
    public void saveAndUserPasswordSpaceMismatch() {
        User source = User.builder()
                .id(1L)
                .email("spacemismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Secret0! ")
                .nickname("spacemismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(6)
    @DisplayName("사용자 등록 시 특수문자 다른 비밀번호 불일치 400 Bad Request 반환")
    public void saveAndUserPasswordSpecialCharMismatch() {
        User source = User.builder()
                .id(1L)
                .email("specialmismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Secret0@")
                .nickname("specialmismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(7)
    @DisplayName("사용자 등록 시 비밀번호와 비밀번호 확인이 일치할 때 정상 처리")
    public void saveAndReturnUserValidWithMatchingPasswords() {
        String password = "ValidPass1!";
        User source = User.builder()
                .id(2L)
                .email("validmatch" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("validmatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        assertTrue(actualStatus == 200 || actualStatus == 201, "비밀번호 일치 시 정상 등록되어야 함");
    }

    @Test
    @Order(8)
    @DisplayName("사용자 수정 시 비밀번호와 비밀번호 확인이 일치하지 않을 때 400 Bad Request 반환")
    public void updateAndUserPasswordMismatch() {
        User source = User.builder()
                .id(1L)
                .email("updatemismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("updatemismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(9)
    @DisplayName("사용자 수정 시 비밀번호와 비밀번호 확인이 일치할 때 정상 처리")
    public void updateAndReturnUserValidWithMatchingPasswords() {
        String password = "UpdatePass1!";
        User source = User.builder()
                .id(3L)
                .email("updatevalid" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("updatevalid")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        assertTrue(actualStatus == 200 || actualStatus == 201, "비밀번호 일치 시 정상 수정되어야 함");
    }

    private void saveUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        assertEquals(400, actualStatus, "비밀번호 불일치 시 400 Bad Request 반환");
    }

    private void updateUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        assertEquals(400, actualStatus, "비밀번호 불일치 시 400 Bad Request 반환");
    }
}