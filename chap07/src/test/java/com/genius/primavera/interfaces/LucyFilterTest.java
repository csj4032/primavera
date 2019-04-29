package com.genius.primavera.interfaces;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LucyFilterTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Order(1)
	@DisplayName("XSS 필터 적용")
	public void message() throws Exception {
		mockMvc.perform(get("/lucy/filter").param("xss", "<ok>"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string("&lt;ok&gt;"));
	}

	@Test
	@Order(2)
	@DisplayName("XSS 필터 URL 예외 적용")
	public void filterParameterDisable() throws Exception {
		mockMvc.perform(get("/lucy/filter/parameter/disable").param("message", "<ok>").param( "xss", "<ignore>"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string("&lt;ok&gt;<ignore>"));
	}

	@Test
	@Order(3)
	@DisplayName("XSS 필터 파라미터 예외 적용")
	public void filterUrlDisable() throws Exception {
		mockMvc.perform(get("/lucy/filter/url/disable").param("message", "<ok>").param( "xss", "<ignore>"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string("<ok><ignore>"));
	}

	@Test
	@Order(4)
	@DisplayName("XSS 필터 글로벌 파라미터 예외 적용")
	public void filterGlobalParameterDisable() throws Exception {
		mockMvc.perform(get("/lucy/filter/global").param("message", "<ok>").param( "global", "<ignore>"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string("&lt;ok&gt;<ignore>"));
	}
}
