package com.genius.primavera.dataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

@Slf4j
@Testcontainers
@ExtendWith(SpringExtension.class)
public class HikariDataSourceTest {

    public static final String USER_NAME = "primavera";
    public static final String PASS_WORLD = "primavera";
    public static final String CATALOG = "primavera";

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName(CATALOG)
            .withUsername(USER_NAME)
            .withPassword(PASS_WORLD);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = mysqlContainer.getJdbcUrl() + "?allowPublicKeyRetrieval=true&useSSL=false";
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    private static HikariConfig configuration;

    @BeforeAll
    @DisplayName(value = "히카리 설정 초기화")
    public static void init() {
        // 컨테이너 상태 출력
        log.info("MySQL 컨테이너 실행 상태: {}", mysqlContainer.isRunning());
        log.info("MySQL 컨테이너 JDBC URL: {}", mysqlContainer.getJdbcUrl());
        log.info("MySQL 컨테이너 포트: {}", mysqlContainer.getFirstMappedPort());

        configuration = new HikariConfig();
        configuration.setDriverClassName("com.mysql.cj.jdbc.Driver");
        configuration.setJdbcUrl(mysqlContainer.getJdbcUrl() + "?allowPublicKeyRetrieval=true");
        configuration.setUsername(USER_NAME);
        configuration.setPassword(PASS_WORLD);
        configuration.setConnectionInitSql("SELECT 1");
        configuration.setMaximumPoolSize(5);
        configuration.setMinimumIdle(2);
        configuration.setConnectionTimeout(10000);
    }

    @Test
    @DisplayName(value = "도커 MySQL 컨테이너 연결 테스트")
    public void dockerMySQLHikariTest() throws SQLException {
        try (var hikariDataSource = new HikariDataSource(configuration)) {
            var connection = hikariDataSource.getConnection();

            // 데이터베이스 연결 검증
            Assertions.assertEquals(CATALOG, connection.getCatalog());
            Assertions.assertFalse(connection.isClosed());
            log.info("MySQL 컨테이너에 HikariCP로 연결 성공!");
        }
    }

    @Test
    @DisplayName(value = "히카리 데이터소스 모킹 테스트")
    public void hikariDataSourceMockTest() throws SQLException {
        HikariDataSource mockDataSource = mock(HikariDataSource.class);
        Connection mockConnection = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.getCatalog()).thenReturn(CATALOG);

        Connection connection = mockDataSource.getConnection();
        String catalog = connection.getCatalog();

        Assertions.assertEquals(CATALOG, catalog);
        verify(mockDataSource).getConnection();
        verify(mockConnection).getCatalog();
    }
}