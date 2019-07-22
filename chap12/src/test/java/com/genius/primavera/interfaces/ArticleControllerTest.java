package com.genius.primavera.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Calendar;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Order(1)
	@DisplayName("게시판 리스트 화면 접근")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void articles() throws Exception {
		mockMvc.perform(get("/articles").accept(MediaType.TEXT_HTML))
				.andDo(print())
				.andExpect(view().name("article/list"))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	@Order(2)
	@DisplayName("게시글 작성 접근")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void save() throws Exception {
		MultiValueMap<String, String> param = new LinkedMultiValueMap();
		param.set("pId", "0");
		param.set("subject", "제목");
		param.set("contents", "내용");
		mockMvc.perform(post("/articles/save").params(param).contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andDo(print())
				.andExpect(status().is3xxRedirection());
	}

	@Test
	@Order(3)
	@DisplayName("게시글 첨부 작성 접근")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void saveAndAttachment() throws Exception {
		mockMvc.perform(multipart("/articles/save")
				.file(new MockMultipartFile("file", "genius.txt", "text/plain", "Hello Wrold".getBytes()))
				.param("pId", "0")
				.param("subject", "첨부파일 제목")
				.param("contents", "첨부파일 내영")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andDo(print())
				.andExpect(status().is3xxRedirection());
	}

	@Test
	@Order(4)
	@DisplayName("1번 게시글 상세 접근")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void detail() throws Exception {
		mockMvc.perform(get("/articles/1").accept(MediaType.TEXT_HTML))
				.andDo(print())
				.andExpect(view().name("article/detail"))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	@Order(5)
	@DisplayName("1번 게시글 댓글 등록")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void comment() throws Exception {
		MultiValueMap<String, String> param = new LinkedMultiValueMap();
		param.set("article", "1");
		param.set("comment", "댓글");
		mockMvc.perform(post("/articles/comment").params(param).contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andDo(print())
				.andExpect(status().is3xxRedirection());
	}
}