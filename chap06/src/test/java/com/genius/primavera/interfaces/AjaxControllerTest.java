package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.User;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

@Order(1)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Ajax controller test - REST API test validation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AjaxControllerTest {

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
    private TestRestTemplate testRestTemplate;

    @Test
    @Order(1)
    @DisplayName("Ajax HTML connection test")
    public void ajaxTest() {
        String ajaxHtml = testRestTemplate.getForObject("/ajax", String.class);
        org.assertj.core.api.Assertions.assertThat(ajaxHtml).contains("ajax");
    }

    @Test
    @Order(2)
    @DisplayName("HTML connection test")
    public void htmlTest() {
        Assertions.assertEquals(testRestTemplate.getForObject("/ajax/html", String.class), "<div>html</div>");
    }

    @Test
    @Order(3)
    @DisplayName("JSON test test")
    public void htmlFormTest() {
        User user = testRestTemplate.getForObject("/ajax/form", User.class);
        Assertions.assertEquals(1, user.getId());
    }

    @Test
    @Order(4)
    @DisplayName("processing test data test")
    public void formDataTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("email", "email");
        User user = testRestTemplate.getForObject("/ajax/form/data?id={id}&email={email}", User.class, params);
        Assertions.assertEquals(1, user.getId());
        Assertions.assertEquals("email", user.getEmail());
    }
}

