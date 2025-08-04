package com.genius.primavera.interfaces;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("로그인 컨트롤러 통합 테스트")
public class LoginControllerTest {

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
	private MockMvc mockMvc;

	@Test
	@Order(1)
	@DisplayName("로그인 화면 접근")
	public void loginView() throws Exception {
		mockMvc.perform(get("/login"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("login"));
	}

	@Test
	@Order(2)
	@DisplayName("로그인 시도 : 성공")
	public void loginIn() throws Exception {
		mockMvc.perform(post("/login").param("email", "genius@primavera.com").param("password", "password123"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"));
	}

	@Test
	@Order(3)
	@DisplayName("로그인 시도 : 실패")
	public void loginInFalse() throws Exception {
		mockMvc.perform(post("/login").param("email", "genius@primavera.com").param("password", "wrongpassword"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(model().attribute("message", "failure"));
	}

	@Test
	@Order(4)
	@DisplayName("로그아웃 시도")
	public void loginOut() throws Exception {
		mockMvc.perform(get("/logout"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}
}
