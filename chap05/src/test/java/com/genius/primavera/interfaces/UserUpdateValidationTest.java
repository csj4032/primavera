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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
public class UserUpdateValidationTest {

	@Container
	private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4.0")
			.withDatabaseName("primavera")
			.withUsername("primavera")
			.withPassword("primavera")
			.withInitScript("sql/schema.sql")
			.withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

	@DynamicPropertySource
	static void mysqlProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		System.out.println("MySQL 컨테이너 JDBC URL: " + mysqlContainer.getJdbcUrl());
		System.out.println("MySQL 컨테이너 포트: " + mysqlContainer.getFirstMappedPort());
	}

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
		ResponseEntity<User> destination = restTemplate.exchange("/users/update", HttpMethod.POST, httpEntity, User.class, source);
		Assertions.assertEquals(400, destination.getStatusCodeValue());
	}
}