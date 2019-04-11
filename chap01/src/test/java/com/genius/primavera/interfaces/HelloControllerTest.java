package com.genius.primavera.interfaces;

import com.genius.primavera.application.HelloService;
import org.hamcrest.core.IsAnything;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private HelloService helloService;

	@Test
	@DisplayName(value = "index 접근하면 반환값으로 hello world")
	public void indexPage() throws Exception {
		mockMvc.perform(get("/").accept(MediaType.TEXT_PLAIN))
				.andExpect(status().isOk())
				.andExpect(content().string(IsAnything.anything("hello world")));
	}

	@Test
	@DisplayName(value = "articles 접근하면 반환값으로 hello world")
	public void indexPageArticle() throws Exception {
		given(this.helloService.getArticles()).willReturn(List.of("게시글1", "게시글2"));
		mockMvc.perform(get("/articles").accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isOk()).andExpect(content().string(("[\"게시글1\",\"게시글2\"]")));
	}
}