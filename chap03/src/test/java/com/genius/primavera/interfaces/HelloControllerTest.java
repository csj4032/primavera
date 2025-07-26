package com.genius.primavera.interfaces;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.domain.User;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HelloService helloService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public HelloService mockHelloService() {
            return Mockito.mock(HelloService.class);
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(helloService);
    }

    @Test
    @Order(1)
    @DisplayName("HelloController hello endpoint test")
    public void helloTest() throws Exception {
        List<User> users = Collections.emptyList();
        given(helloService.getUsers()).willReturn(users);

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("world"))
                .andExpect(model().attributeExists("hello"))
                .andExpect(model().attribute("hello", users));
    }

    @Test
    @Order(2)
    @DisplayName("HelloController hello by id endpoint test")
    public void helloByIdTest() throws Exception {
        Long userId = 1L;
        User user = User.builder().id(userId).name("testUser").build();
        given(helloService.getUserById(userId)).willReturn(user);

        mockMvc.perform(get("/hello/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("hello"))
                .andExpect(model().attributeExists("hello"))
                .andExpect(model().attribute("hello", user));
    }

    @Test
    @Order(3)
    @DisplayName("HelloController oops endpoint exception test")
    public void oopsTest() throws Exception {
        mockMvc.perform(get("/oops"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(4)
    @DisplayName("HelloController order endpoint test")
    public void orderTest() throws Exception {
        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(view().name("hello"));
    }
}