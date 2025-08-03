package com.genius.primavera.interfaces;

import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
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
@EnablePrimaveraTestcontainers
@DisplayName("Primavera Filter 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PrimaveraFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("Primavera Filter 헤더 확인")
    void shouldAddPrimaveraHeaderToLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("primavera"));
    }

    @Test
    @Order(2)
    @DisplayName("유효한 자격증명으로 로그인 성공")
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
    @DisplayName("로그아웃 후 로그인 페이지로 리다이렉트")
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
