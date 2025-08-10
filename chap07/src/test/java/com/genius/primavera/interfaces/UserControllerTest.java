package com.genius.primavera.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.RoleType;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DisplayName("user controller translated_text_2 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

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
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("user IDtranslated_text_1 inquiry")
    public void findByIdTest() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 registration")
    public void saveTest() throws Exception {
        User source = User.builder()
                .email("newuser@gmail.com")
                .password("Secret0!")
                .nickname("newuser")
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        
        mockMvc.perform(post("/users/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(source)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(source.getEmail()))
                .andExpect(jsonPath("$.nickname").value(source.getNickname()));
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 modification")
    public void updateTest() throws Exception {
        User source = User.builder()
                .id(1L)
                .email("genius@primavera.com")
                .password("Secret0!")
                .nickname("updatedNickname")
                .status(UserStatus.ON)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        
        mockMvc.perform(post("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(source)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nickname").value(source.getNickname()));
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_3 translated_text_2 modification")
    public void updateNotFoundUserTest() throws Exception {
        User source = User.builder()
                .id(1000000L)
                .email("notfound@gmail.com")
                .password("Secret0!")
                .nickname("notfound")
                .status(UserStatus.ON)
                .roles(List.of(new Role(1, RoleType.USER)))
                .build();
        
        mockMvc.perform(post("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(source)))
                .andExpect(status().isNotFound());
    }
}