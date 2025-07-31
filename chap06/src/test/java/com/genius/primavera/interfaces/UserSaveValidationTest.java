package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;

@ActiveProfiles("test")
@DisplayName("사용자 등록 유효성 검증 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserSaveValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @MockBean
    private UserService userService;

    @Test
    @Order(1)
    @DisplayName("사용자 ID로 조회 테스트")
    public void getUserById() {
        long body = restTemplate.getForObject("/users/1", long.class);
        Assertions.assertEquals(1L, body);
    }

    @Test
    @Order(2)
    @DisplayName("잘못된 이메일 형식 유효성 검증")
    public void saveAndReturnUserIllegalEmail() {
        User source = User.builder().id(1L).email("genius@").password("Secret0!").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(3)
    @DisplayName("비밀번호 복잡성 규칙 위반 검증")
    public void saveAndReturnUserIllegalPassword() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(4)
    @DisplayName("닉네임 최대 길이 초과 검증")
    public void saveAndReturnUserIllegalLongNickname() {
        String nickname = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname(nickname).roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(5)
    @DisplayName("닉네임 최소 길이 미달 검증")
    public void saveAndReturnUserIllegalShortNickname() {
        String nickname = "1";
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname(nickname).roles(List.of(new Role(1, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(6)
    @DisplayName("사용자 권한 누락 검증")
    public void saveAndReturnUserNotRole() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(null).build();
        saveUser(source);
    }

    @Test
    @Order(7)
    @DisplayName("잘못된 권한 ID 검증")
    public void saveAndReturnUserIllegalRoleId() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(List.of(new Role(0, RoleType.USER))).build();
        saveUser(source);
    }

    @Test
    @Order(8)
    @DisplayName("권한 타입 누락 검증")
    public void saveAndReturnUserIllegalRoleType() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(List.of(new Role(1, null))).build();
        saveUser(source);
    }

    @Test
    @Order(9)
    @DisplayName("등록일자와 수정일자 순서 검증")
    public void saveAndCreatedDateUpdatedDate() {
        // createdAt이 updatedAt보다 미래일 때 유효성 검증
        User source = User.builder()
                .id(1L)
                .email("genius@gmail.com")
                .password("Secret0!")
                .createdAt(Instant.now().plusSeconds(60 * 60 * 24)) // 1일 후
                .updatedAt(Instant.now())
                .nickname("genius")
                .roles(List.of(new Role(1, null)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(10)
    @DisplayName("정상 사용자 등록 요청 - 모든 값이 유효할 때 200 OK 반환")
    public void saveAndReturnUserValid() {
        User source = User.builder()
                .id(2L)
                .email("validuser" + System.currentTimeMillis() + "@gmail.com")
                .password("ValidPass1!")
                .nickname("validnick")
                .roles(List.of(new Role(1, RoleType.USER)))
                .createdAt(Instant.now())
                .updatedAt(Instant.now().plusSeconds(10))
                .status(UserStatus.ACTIVE)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "정상 등록 시 200 또는 201 반환");
    }

    @Test
    @Order(12)
    @DisplayName("권한 리스트가 비어있을 때 400 Bad Request 반환")
    public void saveAndReturnUserEmptyRoles() {
        User source = User.builder()
                .id(4L)
                .email("emptyroles@gmail.com")
                .password("Secret0!")
                .nickname("emptyroles")
                .roles(List.of())
                .build();
        saveUser(source);
    }

    @Test
    @Order(13)
    @DisplayName("권한이 null일 때 400 Bad Request 반환")
    public void saveAndReturnUserNullRoles() {
        User source = User.builder()
                .id(5L)
                .email("nullroles@gmail.com")
                .password("Secret0!")
                .nickname("nullroles")
                .roles(null)
                .build();
        saveUser(source);
    }

    @Test
    @Order(14)
    @DisplayName("이메일이 null일 때 400 Bad Request 반환")
    public void saveAndReturnUserNullEmail() {
        User source = User.builder()
                .id(6L)
                .email(null)
                .password("Secret0!")
                .nickname("nullemail")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(15)
    @DisplayName("비밀번호가 null일 때 400 Bad Request 반환")
    public void saveAndReturnUserNullPassword() {
        User source = User.builder()
                .id(7L)
                .email("nullpass@gmail.com")
                .password(null)
                .nickname("nullpass")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(16)
    @DisplayName("닉네임이 null일 때 400 Bad Request 반환")
    public void saveAndReturnUserNullNickname() {
        User source = User.builder()
                .id(8L)
                .email("nullnick@gmail.com")
                .password("Secret0!")
                .nickname(null)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(17)
    @DisplayName("닉네임에 특수문자 포함 시 400 Bad Request 반환")
    public void saveAndReturnUserNicknameWithSpecialChars() {
        User source = User.builder()
                .id(9L)
                .email("special@gmail.com")
                .password("Secret0!")
                .nickname("nick@name!")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(18)
    @DisplayName("닉네임에 공백 포함 시 400 Bad Request 반환")
    public void saveAndReturnUserNicknameWithSpaces() {
        User source = User.builder()
                .id(10L)
                .email("spaces@gmail.com")
                .password("Secret0!")
                .nickname("nick name")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(19)
    @DisplayName("비밀번호가 짧을 때 (7자) 400 Bad Request 반환")
    public void saveAndReturnUserPasswordTooShort() {
        User source = User.builder()
                .id(11L)
                .email("shortpw@gmail.com")
                .password("Short1!")
                .nickname("shortpw")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(20)
    @DisplayName("비밀번호가 길 때 (21자) 400 Bad Request 반환")
    public void saveAndReturnUserPasswordTooLong() {
        User source = User.builder()
                .id(12L)
                .email("longpw@gmail.com")
                .password("VeryLongPassword123!@")
                .nickname("longpw")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(21)
    @DisplayName("비밀번호에 숫자 누락 시 400 Bad Request 반환")
    public void saveAndReturnUserPasswordNoDigit() {
        User source = User.builder()
                .id(13L)
                .email("nodigit@gmail.com")
                .password("NoDigitPass!")
                .nickname("nodigit")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(22)
    @DisplayName("비밀번호에 소문자 누락 시 400 Bad Request 반환")
    public void saveAndReturnUserPasswordNoLowercase() {
        User source = User.builder()
                .id(14L)
                .email("nolower@gmail.com")
                .password("NOLOWER1!")
                .nickname("nolower")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(23)
    @DisplayName("비밀번호에 대문자 누락 시 400 Bad Request 반환")
    public void saveAndReturnUserPasswordNoUppercase() {
        User source = User.builder()
                .id(15L)
                .email("noupper@gmail.com")
                .password("noupper1!")
                .nickname("noupper")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(24)
    @DisplayName("비밀번호에 특수문자 누락 시 400 Bad Request 반환")
    public void saveAndReturnUserPasswordNoSpecialChar() {
        User source = User.builder()
                .id(16L)
                .email("nospecial@gmail.com")
                .password("NoSpecial1")
                .nickname("nospecial")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(25)
    @DisplayName("비밀번호에 공백 포함 시 400 Bad Request 반환")
    public void saveAndReturnUserPasswordWithSpaces() {
        User source = User.builder()
                .id(17L)
                .email("spacepw@gmail.com")
                .password("Space Pass1!")
                .nickname("spacepw")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(26)
    @DisplayName("권한의 ID가 0일 때 400 Bad Request 반환")
    public void saveAndReturnUserRoleIdZero() {
        User source = User.builder()
                .id(18L)
                .email("roleid0@gmail.com")
                .password("Secret0!")
                .nickname("roleid0")
                .roles(List.of(new Role(0, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(27)
    @DisplayName("권한의 ID가 음수일 때 400 Bad Request 반환")
    public void saveAndReturnUserRoleIdNegative() {
        User source = User.builder()
                .id(19L)
                .email("roleidneg@gmail.com")
                .password("Secret0!")
                .nickname("roleidneg")
                .roles(List.of(new Role(-1, RoleType.USER)))
                .build();
        saveUser(source);
    }

    @Test
    @Order(28)
    @DisplayName("한글 닉네임 정상 처리")
    public void saveAndReturnUserKoreanNickname() {
        User source = User.builder()
                .id(20L)
                .email("korean" + System.currentTimeMillis() + "@gmail.com")
                .password("Secret0!")
                .nickname("한글닉네임")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        Assertions.assertTrue(actualStatus == 200 || actualStatus == 201, "한글 닉네임은 정상 처리되어야 함");
    }

    private void saveUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity<>(source, headers);
        ResponseEntity<String> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, String.class, source);
        int actualStatus = destination.getStatusCode().value();
        // @ScriptAssert 제거 후 유효성 검증이 제대로 작동하여 400 Bad Request 반환
        Assertions.assertEquals(400, actualStatus, "Expected 400 Bad Request for invalid email");
    }
}