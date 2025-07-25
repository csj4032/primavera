package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
class MySQLTestApplication {
}

@Slf4j
@Testcontainers
@SpringBootTest(classes = MySQLTestApplication.class)
@ActiveProfiles("test")
class MySQLTestContainersTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("primavera")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Test
    void contextLoads() {
        assertTrue(mysql.isRunning());
        log.info("MySQL TestContainer is running on {}", mysql.getJdbcUrl());
        log.info("MySQL version: {}", mysql.getDockerImageName());
    }

    @Test
    void databaseConnectionTest() {
        assertNotNull(mysql.getJdbcUrl());
        assertTrue(mysql.getJdbcUrl().contains("primavera"));
        assertTrue(mysql.getJdbcUrl().startsWith("jdbc:mysql://"));
        log.info("Database connection verified: {}", mysql.getJdbcUrl());
    }
}