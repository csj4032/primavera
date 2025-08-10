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
@DisplayName("ArticleController test test")
public class ArticleControllerSecureTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void verifyTestEnvironment() {

        TestDataConstants.Security.ensureTestEnvironment();
        log.info(" test validation completed - connection test execution");
    }

    @Test
    @Order(1)
    @WithTestUser(role = WithTestUser.Role.GENIUS)
    @DisplayName("connection file connection test inquiry")
    void shouldAllowAdminToViewArticles() throws Exception {

        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andExpect(view().name("article/list"))
            .andExpect(model().attributeExists("articles"))
            .andDo(result -> {
                log.info(" connection({}) file connection test inquiry success", 
                    TestDataConstants.TestUsers.GENIUS_EMAIL);
            });
    }

    @Test
    @Order(2)
    @WithTestUser(role = WithTestUser.Role.USER)
    @DisplayName("test user file connection inquiry")
    void shouldAllowUserToViewArticles() throws Exception {

        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andExpect(view().name("article/list"))
            .andDo(result -> {
                log.info(" test user({}) file connection inquiry success", 
                    TestDataConstants.TestUsers.USER_EMAIL);
            });
    }

    @Test
    @Order(3)
    @WithTestUser(role = WithTestUser.Role.GENIUS)
    @DisplayName("connection file connection test")
    void shouldAllowAdminToCreateArticle() throws Exception {

        String title = "test test connection";
        String content = "should connection testshould test test.";

        mockMvc.perform(post("/articles")
                .param("title", title)
                .param("content", content)
                .param("type", "GENERAL"))
            .andExpect(status().is3xxRedirection())
            .andExpected(redirectedUrl("/articles"))
            .andDo(result -> {
                log.info(" connection({}) file connection test success: {}", 
                    TestDataConstants.TestUsers.GENIUS_EMAIL, title);
            });
    }

    @Test
    @Order(4)
    @WithTestUser(role = WithTestUser.Role.USER)
    @DisplayName("test user connection test")
    void shouldRestrictUserPermissions() throws Exception {

        mockMvc.perform(get("/admin/articles"))
            .andExpect(status().isForbidden())
            .andDo(result -> {
                log.info(" test user({}) connection test connection", 
                    TestDataConstants.TestUsers.USER_EMAIL);
            });
    }

    @Test
    @Order(5)
    @WithTestUser(email = "custom@test.primavera.local")
    @DisplayName("connection test user test")
    void shouldWorkWithCustomTestUser() throws Exception {

        String customEmail = "custom@test.primavera.local";

        mockMvc.perform(get("/articles"))
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info(" connection test user({}) test success", customEmail);
            });
    }

    @Test
    @Order(6)
    @DisplayName("file test user test")
    void shouldRestrictUnauthenticatedAccess() throws Exception {

        mockMvc.perform(post("/articles")
                .param("title", "Unauthorized Article")
                .param("content", "This should fail"))
            .andExpect(status().is3xxRedirection())
            .andDo(result -> {
                log.info(" file test connection - connection needs to be added");
            });
    }

    @AfterAll
    static void securityReport() {
        log.info(" test test completed test:");
        log.info("   test connection test: *.test.primavera.local");
        log.info("   Endpoint user information test");
        log.info("   test test validation");
        log.info("   test test validation");
        log.info("   TestContainer test");
    }
}