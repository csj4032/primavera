package com.genius.primavera.interfaces;

import com.genius.primavera.application.post.PostService;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PostService postService;

	@Test
	@Order(1)
	@DisplayName("포스팅 목록 화면 접근")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void postList() throws Exception {
		given(this.postService.findAll()).willReturn(List.of(
				Post.builder().id(1).subject("로마는 하루아침에 이루어지지 않았다.").contents("제1권 로마는 하루아침에 이루어지지 않았다.").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
				Post.builder().id(2).subject("한니발 전쟁").contents("제2권 한니발 전쟁").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build()));
		mockMvc.perform(get("/posts").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("로마는 하루아침에 이루어지지 않았다.")))
				.andExpect(content().string(containsString("Genius")));
	}

	@Test
	@Order(2)
	@DisplayName("포스팅 상세 화면 접근")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void postDetail() throws Exception {
		mockMvc.perform(get("/posts/1")).andExpect(status().isOk());
	}

	@Test
	@Order(3)
	@DisplayName("포스팅 등록 화면 접근")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void postForm() throws Exception {
		mockMvc.perform(get("/post/form")).andExpect(status().isOk());
	}

	@Test
	@Order(4)
	@DisplayName("포스팅 저장 후 목록 화면")
	@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void postSave() throws Exception {
		mockMvc.perform(post("/post/save")).andExpect(status().is3xxRedirection());
	}
}
