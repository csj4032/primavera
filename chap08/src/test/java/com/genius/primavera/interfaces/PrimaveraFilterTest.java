package com.genius.primavera.interfaces;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Primavera Filter translated_text_2 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PrimaveraFilterTest {

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

    @Test
    @Order(1)
    @DisplayName("Primavera Filter translated_text_2 verification")
    void shouldAddPrimaveraHeaderToLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("primavera"));
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_3 translated_text_6 translated_text_3 success")
    void shouldAuthenticateWithValidCredentials() throws Exception {
        HttpSession httpSession = mockMvc.perform(post("/login")
                        .param("email", "genius@gmail.com")
                        .param("password", "Secret0!"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("principal", "genius@gmail.com"))
                .andExpect(model().attribute("credentials", "Secret0!"))
                .andReturn().getRequest().getSession();

        Assertions.assertNotNull(httpSession);
        Assertions.assertNotNull(httpSession.getAttribute(SPRING_SECURITY_CONTEXT_KEY));
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_4 translated_text_1 translated_text_3 translated_text_4 translated_text_5")
    void shouldRedirectToLoginAfterLogout() throws Exception {
        HttpSession httpSession = mockMvc.perform(get("/logout"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn().getRequest().getSession();

        Assertions.assertNotNull(httpSession);
        Assertions.assertNull(httpSession.getAttribute(SPRING_SECURITY_CONTEXT_KEY));
    }
}
