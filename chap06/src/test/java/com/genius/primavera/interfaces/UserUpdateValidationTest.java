package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ActiveProfiles("test")
@EnablePrimaveraTestcontainers
@DisplayName("사용자 수정 유효성 검증 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserUpdateValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    @DisplayName("사용자 ID 누락 검증")
    public void updateAndUserIllegalId() {
        User source = User.builder().email("genius@gmail.com").password("Secret0!").passwordConfirm("Secret0!").nickname("genius").status(UserStatus.INACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(2)
    @DisplayName("사용자 상태 누락 검증")
    public void saveAndReturnUserIllegalStatus() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").status(null).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(3)
    @DisplayName("닉네임 최소 길이 미달 검증")
    public void saveAndReturnUserIllegalNickname() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("g").status(UserStatus.INACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(4)
    @DisplayName("사용자 ID가 0일 때 400 Bad Request 반환")
    public void updateAndUserIdZero() {
        User source = User.builder().id(0L).email("idZero@gmail.com").password("Secret0!").nickname("idZero").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(5)
    @DisplayName("사용자 ID가 음수일 때 400 Bad Request 반환")
    public void updateAndUserIdNegative() {
        User source = User.builder().id(-1L).email("idNegative@gmail.com").password("Secret0!").nickname("idNegative").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(6)
    @DisplayName("이메일 형식 오류 검증")
    public void updateAndUserInvalidEmail() {
        User source = User.builder().id(1L).email("invalid-email").password("Secret0!").nickname("invalidemail").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(7)
    @DisplayName("비밀번호 복잡성 규칙 위반 검증")
    public void updateAndUserInvalidPassword() {
        User source = User.builder().id(1L).email("invalidpw@gmail.com").password("simple").nickname("invalidpw").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(8)
    @DisplayName("닉네임 최대 길이 초과 검증")
    public void updateAndUserNicknameTooLong() {
        User source = User.builder().id(1L).email("toolong@gmail.com").password("Secret0!").nickname("가".repeat(21)).status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(9)
    @DisplayName("권한 리스트가 비어있을 때 400 Bad Request 반환")
    public void updateAndUserEmptyRoles() {
        User source = User.builder().id(1L).email("emptyroles@gmail.com").password("Secret0!").nickname("emptyroles").status(UserStatus.ACTIVE).roles(List.of()).build();
        updateUser(source);
    }

    @Test
    @Order(10)
    @DisplayName("권한이 null일 때 400 Bad Request 반환")
    public void updateAndUserNullRoles() {
        User source = User.builder().id(1L).email("nullroles@gmail.com").password("Secret0!").nickname("nullroles").status(UserStatus.ACTIVE).roles(null).build();
        updateUser(source);
    }

    @Test
    @Order(12)
    @DisplayName("이메일이 빈 문자열일 때 400 Bad Request 반환")
    public void updateAndUserEmptyEmail() {
        User source = User.builder().id(1L).email("").password("Secret0!").nickname("emptyemail").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(13)
    @DisplayName("비밀번호가 빈 문자열일 때 400 Bad Request 반환")
    public void updateAndUserEmptyPassword() {
        User source = User.builder().id(1L).email("emptypass@gmail.com").password("").nickname("emptypass").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(14)
    @DisplayName("닉네임이 빈 문자열일 때 400 Bad Request 반환")
    public void updateAndUserEmptyNickname() {
        User source = User.builder().id(1L).email("emptynick@gmail.com").password("Secret0!").nickname("").status(UserStatus.ACTIVE).roles(List.of(new Role(1, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(15)
    @DisplayName("닉네임이 한글만 포함된 경우 정상 처리")
    public void updateAndUserKoreanOnlyNickname() {
        String password = "Secret0!";
        User source = User.builder()
                .id(2L)
                .email("korean" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("한글닉네임")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "한글 닉네임은 정상 처리되어야 함");
    }

    @Test
    @Order(16)
    @DisplayName("닉네임이 숫자만 포함된 경우 정상 처리")
    public void updateAndUserNumberOnlyNickname() {
        String password = "Secret0!";
        User source = User.builder()
                .id(3L)
                .email("numbers" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("12345")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "숫자 닉네임은 정상 처리되어야 함");
    }

    @Test
    @Order(17)
    @DisplayName("닉네임이 영어만 포함된 경우 정상 처리")
    public void updateAndUserEnglishOnlyNickname() {
        String password = "Secret0!";
        User source = User.builder()
                .id(4L)
                .email("english" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("englishonly")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "영어 닉네임은 정상 처리되어야 함");
    }

    @Test
    @Order(18)
    @DisplayName("사용자 상태를 DORMANT로 변경 시 정상 처리")
    public void updateAndUserStatusDormant() {
        String password = "Secret0!";
        User source = User.builder()
                .id(5L)
                .email("dormant" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("dormant")
                .status(UserStatus.DORMANT)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "DORMANT 상태 변경은 정상 처리되어야 함");
    }

    @Test
    @Order(19)
    @DisplayName("사용자 상태를 INACTIVE로 변경 시 정상 처리")
    public void updateAndUserStatusInactive() {
        String password = "Secret0!";
        User source = User.builder()
                .id(6L)
                .email("inactive" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("inactive")
                .status(UserStatus.INACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "INACTIVE 상태 변경은 정상 처리되어야 함");
    }

    @Test
    @Order(20)
    @DisplayName("다중 권한을 가진 사용자 수정 시 정상 처리")
    public void updateAndUserMultipleRoles() {
        String password = "Secret0!";
        User source = User.builder()
                .id(7L)
                .email("multipleroles" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("multipleroles")
                .status(UserStatus.ACTIVE)
                .roles(List.of(
                        new Role(1, RoleType.USER),
                        new Role(2, RoleType.MANAGER)
                ))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "다중 권한은 정상 처리되어야 함");
    }

    @Test
    @Order(21)
    @DisplayName("권한에 null 타입이 포함된 경우 400 Bad Request 반환")
    public void updateAndUserRoleWithNullType() {
        User source = User.builder().id(1L).email("nullroletype@gmail.com").password("Secret0!").nickname("nullroletype").status(UserStatus.ACTIVE).roles(List.of(new Role(1, null))).build();
        updateUser(source);
    }

    @Test
    @Order(22)
    @DisplayName("권한 ID가 음수인 경우 400 Bad Request 반환")
    public void updateAndUserRoleNegativeId() {
        User source = User.builder().id(1L).email("negroleid@gmail.com").password("Secret0!").nickname("negroleid").status(UserStatus.ACTIVE).roles(List.of(new Role(-5, RoleType.USER))).build();
        updateUser(source);
    }

    @Test
    @Order(23)
    @DisplayName("여러 유효성 검증 오류가 동시에 발생한 경우 400 Bad Request 반환")
    public void updateAndUserMultipleValidationErrors() {
        User source = User.builder()
                .id(-1L)  // 음수 ID
                .email("invalid-email")  // 잘못된 이메일 형식
                .password("simple")  // 약한 비밀번호
                .nickname("a")  // 너무 짧은 닉네임
                .status(null)  // null 상태
                .roles(List.of())  // 빈 권한 리스트
                .build();
        updateUser(source);
    }

    @Test
    @Order(24)
    @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않을 때 400 Bad Request 반환")
    public void updateAndUserPasswordMismatch() {
        User source = User.builder()
                .id(1L)
                .email("mismatch@gmail.com")
                .password("Secret0!")
                .passwordConfirm("Different1!")
                .nickname("mismatch")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(25)
    @DisplayName("비밀번호 확인이 null일 때 400 Bad Request 반환")
    public void updateAndUserPasswordConfirmNull() {
        User source = User.builder()
                .id(1L)
                .email("confirmnull@gmail.com")
                .password("Secret0!")
                .passwordConfirm(null)
                .nickname("confirmnull")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(26)
    @DisplayName("비밀번호 확인이 빈 문자열일 때 400 Bad Request 반환")
    public void updateAndUserPasswordConfirmEmpty() {
        User source = User.builder()
                .id(1L)
                .email("confirmempty@gmail.com")
                .password("Secret0!")
                .passwordConfirm("")
                .nickname("confirmempty")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(27)
    @DisplayName("비밀번호가 null이고 비밀번호 확인이 있을 때 400 Bad Request 반환")
    public void updateAndUserPasswordNullConfirmExists() {
        User source = User.builder()
                .id(1L)
                .email("passnull@gmail.com")
                .password(null)
                .passwordConfirm("Secret0!")
                .nickname("passnull")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(28)
    @DisplayName("비밀번호와 비밀번호 확인이 모두 null일 때 400 Bad Request 반환")
    public void updateAndUserBothPasswordsNull() {
        User source = User.builder()
                .id(1L)
                .email("bothpassnull@gmail.com")
                .password(null)
                .passwordConfirm(null)
                .nickname("bothpassnull")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        updateUser(source);
    }

    @Test
    @Order(29)
    @DisplayName("정상 사용자 수정 요청 - 비밀번호 일치하고 모든 값 유효할 때 200 OK 반환")
    public void updateAndReturnUserValidWithMatchingPasswords() {
        String password = "ValidPass1!";
        User source = User.builder()
                .id(2L)
                .email("validupdate" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("validupdate")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "비밀번호 일치 시 정상 수정되어야 함");
    }

    @Test
    @Order(30)
    @DisplayName("정상 사용자 수정 요청 - 모든 값이 유효할 때 200 OK 반환")
    public void updateAndReturnUserValid() {
        String password = "ValidPass1!";
        User source = User.builder()
                .id(3L)
                .email("validupdate2" + System.currentTimeMillis() + "@gmail.com")
                .password(password)
                .passwordConfirm(password)
                .nickname("validupdate2")
                .status(UserStatus.ACTIVE)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "정상 수정 시 200 또는 201 반환");
    }

    private void updateUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, String.class, source);
        Assertions.assertEquals(400, destination.getStatusCode().value());
    }
}