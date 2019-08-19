package com.genius.primavera.interfaces;

import org.junit.jupiter.api.Disabled;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("포스팅 등록 화면 접근")
    @WithUserDetails(value = "csj4032@gmail.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void form() throws Exception {
        mockMvc.perform(get("/posts/form")).andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("포스팅 저장 후 목록 화면")
    @WithUserDetails(value = "csj4032@gmail.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void save() throws Exception {
        MultiValueMap params = new LinkedMultiValueMap();
        params.set("subject", "승자의 혼미");
        params.set("contents", "카르타고의 멸망에서부터 카이사르가 역사적 무대로 등장하기 전까지를 그리고 있는 <로마인 이야기> 그 세번째 이야기.");
        params.set("writerId", "1");
        mockMvc.perform(post("/posts/save").params(params)).andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(3)
    @DisplayName("포스팅 상세 화면 접근")
    @WithUserDetails(value = "csj4032@gmail.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void detail() throws Exception {
        mockMvc.perform(get("/posts/1").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("카르타고의")))
                .andExpect(content().string(containsString("멸망에서부터")))
                .andExpect(content().string(containsString("Genius")));
    }

    @Test
    @Order(4)
    @DisplayName("포스팅 리스트 화면 접근")
    @WithUserDetails(value = "csj4032@gmail.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void listForPageable() throws Exception {
        mockMvc.perform(get("/posts")
                .param("page", "1")
                .param("size", "10")
                .param("keyword", "승자의")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }
}