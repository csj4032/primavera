package com.genius.primavera.interfaces;

import com.genius.primavera.application.user.UserService;
import com.genius.primavera.domain.model.user.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION;
import static org.springframework.security.test.context.support.TestExecutionEvent.TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Slf4j
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    @Order(1)
    @DisplayName("유저 정보 확인")
    //@WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService", setupBefore = TEST_METHOD)
    void findById() throws Exception {
//        mockMvc.perform(get("/user/1").accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
//                .andDo(print())
//                .andExpect(status().is2xxSuccessful());

        userService.findByEmail("Genius Choi");
    }
}
