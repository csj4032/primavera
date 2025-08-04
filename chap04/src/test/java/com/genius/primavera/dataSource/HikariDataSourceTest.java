package com.genius.primavera.dataSource;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.SQLException;


@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("HikariDataSource 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HikariDataSourceTest {

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
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("DataSource 테스트")
    public void testDataSource() throws SQLException {
        log.info("DataSource class: {}", dataSource.getClass().getName());
        log.info("Database Catalog: {}", dataSource.getConnection().getCatalog());
    }

    @Test
    @Order(2)
    @DisplayName("HikariDataSource 테스트")
    public void testJdbcTemplate() throws SQLException {
        log.info("HikariDataSource class: {}", jdbcTemplate.getClass().getName());
        log.info("Database Product Name: {}", jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName());
        log.info("Database Product Version: {}", jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductVersion());
        log.info("Database Driver Name: {}", jdbcTemplate.getDataSource().getConnection().getMetaData().getDriverName());
        log.info("Database Driver Version: {}", jdbcTemplate.getDataSource().getConnection());
    }

    @Test
    @Order(3)
    @DisplayName("HikariDataSource 커넥션 테스트")
    public void testConnection() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            log.info("커넥션이 성공적으로 생성되었습니다.");
            log.info("커넥션 URL: {}", connection.getMetaData().getURL());
            log.info("커넥션 사용자 이름: {}", connection.getMetaData().getUserName());
        } catch (SQLException e) {
            log.error("커넥션 생성 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }
}