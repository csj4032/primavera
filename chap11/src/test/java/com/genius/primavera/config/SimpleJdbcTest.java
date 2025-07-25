package com.genius.primavera.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Testcontainers
class SimpleJdbcTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.4.0"))
            .withDatabaseName("primavera_test")
            .withUsername("test")
            .withPassword("test");

    @Test
    void testMySQLContainer() {
        assertTrue(mysql.isRunning());
        log.info("MySQL Container is running");
        log.info("JDBC URL: {}", mysql.getJdbcUrl());
        log.info("Username: {}", mysql.getUsername());
        log.info("Password: {}", mysql.getPassword());
    }

    @Test
    void testDirectConnection() throws SQLException {
        try (Connection connection = mysql.createConnection("")) {
            assertTrue(connection.isValid(1));
            log.info("Direct connection successful");
            
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