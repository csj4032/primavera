package com.genius.primavera.dataSource;

import com.genius.primavera.testcontainer.EnablePrimaveraTestcontainers;
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
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnablePrimaveraTestcontainers
public class HikariDataSourceTest {

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