package com.genius.primavera.interfaces;

import com.genius.primavera.testingsupport.annotation.TestWebWithDB;
import com.genius.primavera.testingsupport.annotation.WithTestUser;
import com.genius.primavera.testingsupport.security.TestDataConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@TestWebWithDB(initScript = "sql/init.sql")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@DisplayName("ArticleController translated_text_2 translated_text_2 translated_text_2 test")
public class ArticleControllerSecureTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void verifyTestEnvironment() {

        TestDataConstants.Security.ensureTestEnvironment();
        log.info(" test translated_text_2 validation completed - translated_text_3 test execution");
    }

    @Test
    @Order(1)
    @WithTestUser(role = WithTestUser.Role.GENIUS)
    @DisplayName("translated_text_3 translated_text_4 translated_text_3 translated_text_2 inquiry")
    void shouldAllowAdminToViewArticles() throws Exception {

        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andExpect(view().name("article/list"))
            .andExpect(model().attributeExists("articles"))
            .andDo(result -> {
                log.info(" translated_text_3({}) translated_text_4 translated_text_3 translated_text_2 inquiry success", 
                    TestDataConstants.TestUsers.GENIUS_EMAIL);
            });
    }

    @Test
    @Order(2)
    @WithTestUser(role = WithTestUser.Role.USER)
    @DisplayName("translated_text_2 user translated_text_4 translated_text_3 inquiry")
    void shouldAllowUserToViewArticles() throws Exception {

        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andExpect(view().name("article/list"))
            .andDo(result -> {
                log.info(" translated_text_2 user({}) translated_text_4 translated_text_3 inquiry success", 
                    TestDataConstants.TestUsers.USER_EMAIL);
            });
    }

    @Test
    @Order(3)
    @WithTestUser(role = WithTestUser.Role.GENIUS)
    @DisplayName("translated_text_3 translated_text_4 translated_text_3 translated_text_2")
    void shouldAllowAdminToCreateArticle() throws Exception {

        String title = "translated_text_2 translated_text_2 test translated_text_3";
        String content = "translated_text_1 translated_text_3 translated_text_2translated_text_1 translated_text_2 test translated_text_2.";

        mockMvc.perform(post("/articles")
                .param("title", title)
                .param("content", content)
                .param("type", "GENERAL"))
            .andExpect(status().is3xxRedirection())
            .andExpected(redirectedUrl("/articles"))
            .andDo(result -> {
                log.info(" translated_text_3({}) translated_text_4 translated_text_3 translated_text_2 success: {}", 
                    TestDataConstants.TestUsers.GENIUS_EMAIL, title);
            });
    }

    @Test
    @Order(4)
    @WithTestUser(role = WithTestUser.Role.USER)
    @DisplayName("translated_text_2 user translated_text_3 translated_text_2 test")
    void shouldRestrictUserPermissions() throws Exception {

        mockMvc.perform(get("/admin/articles"))
            .andExpect(status().isForbidden())
            .andDo(result -> {
                log.info(" translated_text_2 user({}) translated_text_3 translated_text_2 translated_text_2 translated_text_3", 
                    TestDataConstants.TestUsers.USER_EMAIL);
            });
    }

    @Test
    @Order(5)
    @WithTestUser(email = "custom@test.primavera.local")
    @DisplayName("translated_text_3 test user translated_text_2")
    void shouldWorkWithCustomTestUser() throws Exception {

        String customEmail = "custom@test.primavera.local";

        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info(" translated_text_3 test user({}) translated_text_2 success", customEmail);
            });
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_4 translated_text_2 user translated_text_2 translated_text_2")
    void shouldRestrictUnauthenticatedAccess() throws Exception {

        mockMvc.perform(post("/articles")
                .param("title", "Unauthorized Article")
                .param("content", "This should fail"))
            .andExpect(status().is3xxRedirection())
            .andDo(result -> {
                log.info(" translated_text_4 translated_text_2 translated_text_2 translated_text_3 - translated_text_3 translated_text_1 translated_text_1");
            });
    }

    @AfterAll
    static void securityReport() {
        log.info(" translated_text_2 translated_text_2 test completed translated_text_2:");
        log.info("   test translated_text_2 translated_text_3 translated_text_2: *.test.primavera.local");
        log.info("   translated_text_5 user information translated_text_2");
        log.info("   translated_text_2 translated_text_2 translated_text_2 validation");
        log.info("   test translated_text_2 translated_text_2 validation");
        log.info("   TestContainer translated_text_2 translated_text_2");
    }
}