package com.genius.primavera.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableAutoConfiguration(exclude = {

    org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@DisplayName("PostController translated_text_2 test - Mixin translated_text_2 translated_text_2")
public class PostControllerMigrationTest extends WebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("translated_text_3 translated_text_2 inquiry - GET /posts")
    void shouldReturnPostList() throws Exception {

        mockMvc.perform(get("/posts"))
            .andExpect(status().isOk())
            .andExpect(view().name("posting/list"))
            .andExpect(model().attributeExists("posts"))
            .andDo(result -> {
                log.info(" translated_text_3 translated_text_2 inquiry success: {}", 
                    result.getResponse().getContentAsString());
            });
    }

    @Test
    @Order(2) 
    @DisplayName("translated_text_3 translated_text_2 translated_text_1 - GET /posts/new")
    void shouldShowPostForm() throws Exception {

        mockMvc.perform(get("/posts/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("posting/form"))
            .andExpect(model().attributeExists("post"))
            .andDo(result -> {
                log.info(" translated_text_3 translated_text_2 translated_text_1 translated_text_2 success");
            });
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_4 translated_text_2 translated_text_3 inquiry translated_text_1 404 translated_text_2")
    void shouldReturn404ForNonExistentPost() throws Exception {

        long nonExistentId = 999999L;

        mockMvc.perform(get("/posts/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andDo(result -> {
                log.info(" translated_text_4 translated_text_2 translated_text_3 translated_text_2 translated_text_1 404 translated_text_2 translated_text_13");
            });
    }

    @AfterAll
    static void tearDown() {
        log.info(" PostController translated_text_2 test completed - TestContainer translated_text_2 translated_text_3");
    }
}