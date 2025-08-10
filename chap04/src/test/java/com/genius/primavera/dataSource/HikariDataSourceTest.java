package com.genius.primavera.dataSource;

import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@SpringBootTest
@EnableTestContainers
@ActiveProfiles("test")
@DisplayName("HikariDataSource translated_text_2 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HikariDataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("DataSource test")
    public void testDataSource() throws SQLException {
        log.info("DataSource class: {}", dataSource.getClass().getName());
        log.info("Database Catalog: {}", dataSource.getConnection().getCatalog());
    }

    @Test
    @Order(2)
    @DisplayName("HikariDataSource test")
    public void testJdbcTemplate() throws SQLException {
        log.info("HikariDataSource class: {}", jdbcTemplate.getClass().getName());
        log.info("Database Product Name: {}", jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName());
        log.info("Database Product Version: {}", jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductVersion());
        log.info("Database Driver Name: {}", jdbcTemplate.getDataSource().getConnection().getMetaData().getDriverName());
        log.info("Database Driver Version: {}", jdbcTemplate.getDataSource().getConnection());
    }

    @Test
    @Order(3)
    @DisplayName("HikariDataSource translated_text_3 test")
    public void testConnection() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            log.info("translated_text_3 translated_text_10 translated_text_13.");
            log.info("translated_text_3 URL: {}", connection.getMetaData().getURL());
            log.info("translated_text_3 user translated_text_2: {}", connection.getMetaData().getUserName());
        } catch (SQLException e) {
            log.error("translated_text_3 creation translated_text_1 error translated_text_2: {}", e.getMessage());
            throw e;
        }
    }
}