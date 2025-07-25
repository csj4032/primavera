package com.genius.primavera.interfaces;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class PrimaveraFilterTest {

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
	@DisplayName(value = "Primavera Filter 사용 확인")
	public void loginView() throws Exception {
		mockMvc.perform(get("/login"))
				.andDo(print())
				.andExpect(status().isOk()).andExpect(header().exists("primavera"));
	}

	@Test
	@Order(2)
	@DisplayName(value = "Primavera Filter UsernamePasswordAuthenticationToken 로그인 시도")
	public void loginIn() throws Exception {
		HttpSession httpSession = mockMvc.perform(post("/login").param("email", "genius@gmail.com").param("password", "Secret0!"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("index"))
				.andExpect(model().attribute("principal", "genius@gmail.com"))
				.andExpect(model().attribute("credentials", "Secret0!")).andReturn().getRequest().getSession();
		Assertions.assertNotNull(httpSession.getAttribute(SPRING_SECURITY_CONTEXT_KEY));
	}

	@Test
	@Order(3)
	@DisplayName("로그아웃 시도")
	public void loginOut() throws Exception {
		HttpSession httpSession = mockMvc.perform(get("/logout"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login")).andReturn().getRequest().getSession();
		Assertions.assertNull(httpSession.getAttribute(SPRING_SECURITY_CONTEXT_KEY));
	}
}
