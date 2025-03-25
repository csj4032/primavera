package com.genius.primavera.interfaces;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginControllerTest {

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
		mockMvc.perform(post("/login").param("email", "genius_0@gmail.com").param("password", "secret"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().exists("auth"))
				.andExpect(view().name("redirect:/"));
	}

	@Test
	@Order(3)
	@DisplayName("로그인 시도 : 실패")
	public void loginInFalse() throws Exception {
		mockMvc.perform(post("/login").param("email", "genius_0@gmail.com").param("password", ""))
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
