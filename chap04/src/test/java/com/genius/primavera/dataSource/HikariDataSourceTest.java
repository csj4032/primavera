package com.genius.primavera.dataSource;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @ActiveProfiles("test")만 존재할 경우 Vault 연결 정보을 이용
 * @EnablePrimaveraTestcontainers 사용할 경우 testcontainers를 이용하여 MariaDB 컨테이너를 실행
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnablePrimaveraTestcontainers({ContainerType.MARIADB})
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
}