package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class UserUpdateValidationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	@Order(1)
	@DisplayName("유저 정보 수정 아이디 누락")
	public void updateAndUserIllegalId() {
		User source = User.builder().email("genius@gmail.com").password("Secret0!").nickname("genius").status(UserStatus.BLOCK).roles(List.of(new Role(1, RoleType.USER))).build();
		updateUser(source);
	}

	@Test
	@Order(2)
	@DisplayName("유저 정보 수정 상태 누락")
	public void saveAndReturnUserIllegalStatus() {
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("genius").status(null).roles(List.of(new Role(1, RoleType.USER))).build();
		updateUser(source);
	}

	@Test
	@Order(3)
	@DisplayName("유저 정보 수정 상태 누락")
	public void saveAndReturnUserIllegalNickname() {
		User source = User.builder().id(1L).email("genius@gmail.com").password("Secret0!").nickname("g").status(UserStatus.BLOCK).roles(List.of(new Role(1, RoleType.USER))).build();
		updateUser(source);
	}

	private void updateUser(User source) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<User> httpEntity = new HttpEntity(source, headers);
		ResponseEntity<User> destination = restTemplate.exchange("/user/update", HttpMethod.POST, httpEntity, User.class, source);
		Assertions.assertEquals(400, destination.getStatusCodeValue());
	}
}