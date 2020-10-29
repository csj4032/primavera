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
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpSession;

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
public class SecurityLoginPageTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MockHttpServletRequest request;

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
		HttpSession session = mockMvc.perform(post("/signin")
				.with(csrf())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("email", "Genius")
				.param("password", "password"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/index"))
				.andReturn()
				.getRequest()
				.getSession();

		request.setSession(session);
		SecurityContext securityContext = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		SecurityContextHolder.setContext(securityContext);
		Assertions.assertTrue(securityContext.getAuthentication().isAuthenticated());
		Assertions.assertEquals("Genius", securityContext.getAuthentication().getName());
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
				.andExpect(status().is4xxClientError());
	}
}
