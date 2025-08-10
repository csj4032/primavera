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
@DisplayName("PostController test - Mixin test")
public class PostControllerMigrationTest extends WebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("connection test inquiry - GET /posts")
    void shouldReturnPostList() throws Exception {

        mockMvc.perform(get("/posts"))
            .andExpect(status().isOk())
            .andExpect(view().name("posting/list"))
            .andExpect(model().attributeExists("posts"))
            .andDo(result -> {
                log.info(" connection test inquiry success: {}", 
                    result.getResponse().getContentAsString());
            });
    }

    @Test
    @Order(2) 
    @DisplayName("connection test should - GET /posts/new")
    void shouldShowPostForm() throws Exception {

        mockMvc.perform(get("/posts/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("posting/form"))
            .andExpect(model().attributeExists("post"))
            .andDo(result -> {
                log.info(" connection test should test success");
            });
    }

    @Test
    @Order(3)
    @DisplayName("file test connection inquiry should 404 test")
    void shouldReturn404ForNonExistentPost() throws Exception {

        long nonExistentId = 999999L;

        mockMvc.perform(get("/posts/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andDo(result -> {
                log.info(" file test connection test should 404 test created successfully");
            });
    }

    @AfterAll
    static void tearDown() {
        log.info(" PostController test completed - TestContainer test connection");
    }
}