package com.genius.primavera.interfaces;

import com.genius.primavera.application.post.PostingService;
import com.genius.primavera.config.TestSecurityConfiguration;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.post.Post;
import com.genius.primavera.domain.model.post.PostDto;
import com.genius.primavera.domain.model.user.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestSecurityConfiguration.class)
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
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
    @Disabled
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 translated_text_2")
    @WithUserDetails(value = "board@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postList() throws Exception {
        given(this.postService.findAll()).willReturn(List.of(
                Post.builder().id(1).subject("translated_text_3 translated_text_5 translated_text_5 translated_text_3.").contents("translated_text_11translated_text_1 translated_text_3 translated_text_5 translated_text_5 translated_text_3.").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(2).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build()));
        mockMvc.perform(get("/posts/all").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("translated_text_3 translated_text_5 translated_text_5 translated_text_3.")))
                .andExpect(content().string(containsString("Genius")));
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_3 translated_text_3 translated_text_2 translated_text_2 translated_text_2")
    @WithUserDetails(value = "board@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postListOfPagination() throws Exception {
        PageRequest pageable = PageRequest.of(1, 10);
        List<Post> list = List.of(
                Post.builder().id(1).subject("translated_text_3 translated_text_5 translated_text_5 translated_text_3.").contents("translated_text_11translated_text_1 translated_text_3 translated_text_5 translated_text_5 translated_text_3.").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(2).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(3).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(3).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(4).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(5).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(6).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(7).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(8).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(9).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(10).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(11).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build(),
                Post.builder().id(12).subject("translated_text_3 translated_text_2").contents("translated_text_12translated_text_1 translated_text_3 translated_text_2").writer(User.builder().id(1).email("Genius Choi").nickname("Genius").build()).build()
        );
        Paged<PostDto.ResponseForList> postPage = new Paged(pageable, list, list.size());
        given(this.postService.findForPageable(pageable, "")).willReturn(postPage);
        Assertions.assertEquals(10, postPage.getPageSize());
        Assertions.assertEquals(13, postPage.getTotalElements());
        Assertions.assertEquals(2, postPage.getTotalPages());
        Assertions.assertEquals(1, postPage.getPageNumber());
        Assertions.assertEquals(13, postPage.getTotalElements());
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 translated_text_2")
    @WithUserDetails(value = "board@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postDetail() throws Exception {
        given(this.postService.findById(1)).willReturn(Post.builder().id(1).subject("translated_text_11translated_text_1 translated_text_3 translated_text_5 translated_text_5 translated_text_3.").contents("translated_text_11translated_text_1 translated_text_3 translated_text_5 translated_text_5 translated_text_3.").writer(User.builder().id(1).nickname("Genius").build()).build());
        mockMvc.perform(get("/posts/1").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("translated_text_5")))
                .andExpect(content().string(containsString("translated_text_5")))
                .andExpect(content().string(containsString("Genius")));
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_3 registration translated_text_2 translated_text_2")
    @WithUserDetails(value = "board@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postForm() throws Exception {
        mockMvc.perform(get("/post/form")).andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_3 translated_text_2 translated_text_1 translated_text_2 translated_text_2")
    @WithUserDetails(value = "board@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void postSave() throws Exception {
        MultiValueMap params = new LinkedMultiValueMap();
        params.set("subject", "translated_text_3 translated_text_2");
        params.set("contents", "translated_text_5 translated_text_6 translated_text_5 translated_text_3 translated_text_3 translated_text_4 translated_text_4 translated_text_3 translated_text_2 <translated_text_3 translated_text_3> translated_text_1 translated_text_3 translated_text_3.");
        params.set("writerId", "1");
        mockMvc.perform(post("/post/save").params(params).with(csrf())).andExpect(status().is3xxRedirection());
    }
}
