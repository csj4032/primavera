package com.genius.primavera.interfaces;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class SecurityLoginPageTest {

	@Container
	protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4.6")
			.withDatabaseName("primavera")
			.withUsername("primavera")
			.withPassword("primavera");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
		registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Order(1)
	@DisplayName("권한이 없는 경우 로그인 화면으로 이동")
	public void loginPage() throws Exception {
		mockMvc.perform(get("/"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}

	@Test
	@Order(2)
	@DisplayName("로그인 시도 성공 후 메인 페이지 이동")
	public void signInFail() throws Exception {
		mockMvc.perform(post("/signin")
				.with(csrf())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("email", "Genius")
				.param("password", "password"))
				.andExpect(status().is3xxRedirection())
				.andDo(print());
	}

	@Test
	@Order(3)
	@DisplayName("USER 권한으로 메인 페이지 접근")
	@WithMockUser(username = "Genius")
	public void index() throws Exception {
		mockMvc.perform(get("/index"))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	@Order(4)
	@DisplayName("USER 권한으로  Manager 페이지 접근")
	@WithMockUser(username = "Genius", roles = "USER")
	public void manager() throws Exception {
		mockMvc.perform(get("/manager"))
				.andDo(print())
				.andExpect(status().isOk());
	}
}
