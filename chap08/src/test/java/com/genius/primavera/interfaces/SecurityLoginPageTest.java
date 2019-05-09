package com.genius.primavera.interfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityLoginPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("로그인 화면으로 이동")
    public void loginPage() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(2)
    @DisplayName("로그인 시도 실패")
    public void signInFail() throws Exception {
        mockMvc.perform(post("/signin"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(2)
    @DisplayName("로그인 시도")
    public void signIn() throws Exception {
        mockMvc.perform(post("/signin").param("email", "genius").param("password", "password"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }
}
