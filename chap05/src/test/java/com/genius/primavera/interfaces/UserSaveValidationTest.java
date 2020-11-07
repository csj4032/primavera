package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class UserSaveValidationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	@Order(1)
	@DisplayName("유저 아이디를 이용한 조회")
	public void getUserById() {
		long body = restTemplate.getForObject("/users/1", long.class);
		Assertions.assertEquals(1L, body);
	}

	@Test
	@Order(2)
	@DisplayName("유저 저장 후 결과 유저 값 받기 이메일 이상하게 넣음")
	public void saveAndReturnUserIllegalEmail() {
		User source = User.builder().id(1L).email("genius@").password("Secret0!").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
		saveUser(source);
	}

	@Test
	@Order(3)
	@DisplayName("유저 저장 후 결과 유저 값 받기 비밀번호 이상하게 넣음")
	public void saveAndReturnUserIllegalPassword() {
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0").nickname("genius").roles(List.of(new Role(1, RoleType.USER))).build();
		saveUser(source);
	}

	@Test
	@Order(4)
	@DisplayName("유저 저장 후 결과 유저 값 받기 별명 길게 넣음")
	public void saveAndReturnUserIllegalLongNickname() {
		String nickname = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname(nickname).roles(List.of(new Role(1, RoleType.USER))).build();
		saveUser(source);
	}

	@Test
	@Order(5)
	@DisplayName("유저 저장 후 결과 유저 값 받기 별명 짧게 넣음")
	public void saveAndReturnUserIllegalShortNickname() {
		String nickname = "1";
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname(nickname).roles(List.of(new Role(1, RoleType.USER))).build();
		saveUser(source);
	}

	@Test
	@Order(6)
	@DisplayName("유저 저장 후 결과 유저 값 받기 권한 안 넣음")
	public void saveAndReturnUserNotRole() {
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(null).build();
		saveUser(source);
	}

	@Test
	@Order(7)
	@DisplayName("유저 저장 후 결과 유저 값 받기 권한 아이디 0 넣음")
	public void saveAndReturnUserIllegalRoleId() {
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(List.of(new Role(0, RoleType.USER))).build();
		saveUser(source);
	}

	@Test
	@Order(8)
	@DisplayName("유저 저장 후 결과 유저 값 받기 권한 아이디 타입 안 넣음")
	public void saveAndReturnUserIllegalRoleType() {
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").roles(List.of(new Role(1, null))).build();
		saveUser(source);
	}

	@Test
	@Order(9)
	@DisplayName("유저 등록일자가 수정일자보다 뒤일 경")
	public void saveAndRegDateModDate() {
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").regDate(LocalDateTime.now().plusDays(1)).modDate(LocalDateTime.now()).nickname("genius").roles(List.of(new Role(1, null))).build();
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