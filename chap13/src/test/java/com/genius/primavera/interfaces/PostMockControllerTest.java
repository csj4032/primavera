package com.genius.primavera.interfaces;

import com.genius.primavera.application.post.PostingService;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;
import com.genius.primavera.domain.model.user.User;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import org.mockito.ArgumentMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostMockControllerTest {

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

    @MockBean
    private PostingService postService;

    @Test
    @Order(1)
    @DisplayName("포스팅 목록 화면 접근")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postList() throws Exception {
        PageRequest pageRequest = PageRequest.of(1, 10);
        List<PostDto.ResponseForList> posts = List.of(
                PostDto.ResponseForList.builder().id(1).subject("로마는 하루아침에 이루어지지 않았다.").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(2).subject("한니발 전쟁").writerNickName("Genius").build());
        Paged<PostDto.ResponseForList> paged = new Paged<>(pageRequest, posts, 2);
        given(this.postService.findForPageable(ArgumentMatchers.any(PageRequest.class), ArgumentMatchers.anyString())).willReturn(paged);
        mockMvc.perform(get("/posts").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("로마는 하루아침에 이루어지지 않았다.")))
                .andExpect(content().string(containsString("Genius")));
    }

    @Test
    @Order(2)
    @DisplayName("포스팅 페이징 목록 화면 접근")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postListOfPagination() throws Exception {
        PageRequest pageable = PageRequest.of(1, 10);
        List<PostDto.ResponseForList> list = List.of(
                PostDto.ResponseForList.builder().id(1).subject("로마는 하루아침에 이루어지지 않았다.").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(2).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(3).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(4).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(5).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(6).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(7).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(8).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(9).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(10).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(11).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(12).subject("한니발 전쟁").writerNickName("Genius").build(),
                PostDto.ResponseForList.builder().id(13).subject("한니발 전쟁").writerNickName("Genius").build()
        );
        Paged<PostDto.ResponseForList> postPage = new Paged<>(pageable, list, list.size());
        given(this.postService.findForPageable(ArgumentMatchers.any(PageRequest.class), ArgumentMatchers.anyString())).willReturn(postPage);
        assertEquals(10, postPage.getPageSize());
        assertEquals(13, postPage.getTotalElements());
        assertEquals(2, postPage.getTotalPages());
        assertEquals(1, postPage.getPageNumber());
        assertEquals(13, postPage.getTotalElements());
    }

    @Test
    @Order(3)
    @DisplayName("포스팅 상세 화면 접근")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postDetail() throws Exception {
        given(this.postService.findById(1)).willReturn(Post.builder().id(1).subject("제1권 로마는 하루아침에 이루어지지 않았다.").contents("제1권 로마는 하루아침에 이루어지지 않았다.").writer(User.builder().id(1).nickname("Genius").build()).build());
        mockMvc.perform(get("/posts/1").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("하루아침에")))
                .andExpect(content().string(containsString("이루어지지")))
                .andExpect(content().string(containsString("Genius")));
    }

    @Test
    @Order(4)
    @DisplayName("포스팅 등록 화면 접근")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postForm() throws Exception {
        mockMvc.perform(get("/posts/form")).andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("포스팅 저장 후 목록 화면")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postSave() throws Exception {
        MultiValueMap params = new LinkedMultiValueMap();
        params.set("subject", "승자의 혼미");
        params.set("contents", "카르타고의 멸망에서부터 카이사르가 역사적 무대로 등장하기 전까지를 그리고 있는 <로마인 이야기> 그 세번째 이야기.");
        params.set("writerId", "4");
        mockMvc.perform(post("/posts/save").params(params).with(csrf())).andExpect(status().is3xxRedirection());
    }
}
