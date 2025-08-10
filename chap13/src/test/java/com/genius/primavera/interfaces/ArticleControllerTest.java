package com.genius.primavera.interfaces;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleControllerTest {

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
    @DisplayName("connection test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void articles() throws Exception {
        mockMvc.perform(get("/articles").accept(MediaType.TEXT_HTML))
                .andDo(print())
                .andExpect(view().name("article/list"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @Order(2)
    @DisplayName("connection test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void save() throws Exception {
        MultiValueMap<String, String> param = new LinkedMultiValueMap();
        param.set("pId", "0");
        param.set("subject", "test");
        param.set("contents", "test");
        mockMvc.perform(post("/articles/save").params(param).contentType(MediaType.APPLICATION_FORM_URLENCODED).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(3)
    @DisplayName("connection test test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void saveAndAttachment() throws Exception {
        mockMvc.perform(multipart("/articles/save")
                        .file(new MockMultipartFile("file", "genius.txt", "text/plain", "Hello Wrold".getBytes()))
                        .param("pId", "0")
                        .param("subject", "test")
                        .param("contents", "test")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(4)
    @DisplayName("1should connection test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void detail() throws Exception {
        mockMvc.perform(get("/articles/1").accept(MediaType.TEXT_HTML))
                .andDo(print())
                .andExpect(view().name("article/detail"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @Order(5)
    @DisplayName("1should connection test registration")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void comment() throws Exception {
        MultiValueMap<String, String> param = new LinkedMultiValueMap();
        param.set("article", "1");
        param.set("comment", "test");
        mockMvc.perform(post("/articles/comment").params(param).contentType(MediaType.APPLICATION_FORM_URLENCODED).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }
}