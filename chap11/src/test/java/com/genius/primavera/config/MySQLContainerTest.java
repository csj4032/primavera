package com.genius.primavera.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class MySQLContainerTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.4.0"))
            .withDatabaseName("primavera_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Test
    void contextLoads() {
        assertTrue(mysql.isRunning());
        log.info("MySQL Container is running on: {}", mysql.getJdbcUrl());
    }

    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection connection = mysql.createConnection("")) {
            assertTrue(connection.isValid(1));
            log.info("Successfully connected to MySQL 8.4.0 container");
            
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT VERSION()");
            if (resultSet.next()) {
                String version = resultSet.getString(1);
                log.info("MySQL Version: {}", version);
                assertTrue(version.startsWith("8.4.0"));
            }
        }
    }
}