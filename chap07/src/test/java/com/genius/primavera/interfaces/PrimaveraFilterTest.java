package com.genius.primavera.interfaces;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpSession;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraFilterTest {

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
