package com.genius.primavera.application;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class DoSomethingImplTest {

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

    @Test
    @DisplayName("DoSomethingImpl 테스트")
    public void testDoSomething() {
        DoSomething doSomething = new DoSomethingImpl();
        String result1 = doSomething.doSomething("Hello");
        assertEquals("Hello Something Something", result1, "Single argument should return the same string");
        String result2 = doSomething.doSomething("Hello", "World");
        assertEquals("Hello World Something", result2, "Two arguments should concatenate with a space");
        String result3 = doSomething.doSomething("Hello", "World", "!");
        assertEquals("Hello World ! Something", result3, "Three arguments should concatenate with spaces");
    }
}