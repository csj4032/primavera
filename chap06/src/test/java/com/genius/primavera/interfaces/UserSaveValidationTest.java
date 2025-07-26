package com.genius.primavera.interfaces;

import com.genius.primavera.domain.AbstractContainerTest;
import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("사용자 등록 유효성 검증 테스트")
public class UserSaveValidationTest extends AbstractContainerTest {

    @Autowired
    private TestRestTemplate restTemplate;

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
    public void saveAndRegDateModDate() {
        User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").createdAt(LocalDateTime.now().plusDays(1)).updatedAt(LocalDateTime.now()).nickname("genius").roles(List.of(new Role(1, null))).build();
        saveUser(source);
    }

    private void saveUser(User source) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> httpEntity = new HttpEntity(source, headers);
        ResponseEntity<User> destination = restTemplate.exchange("/users/save", HttpMethod.POST, httpEntity, User.class, source);
        Assertions.assertEquals(400, destination.getStatusCodeValue());
    }
}