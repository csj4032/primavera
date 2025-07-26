package com.genius.primavera.dataSource;

import com.genius.primavera.test.TestContainerService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MariaDBContainer;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class HikariDataSourceTest {

    private static MariaDBContainer<?> mariaDBContainer;
    private static HikariConfig configuration;

    @BeforeAll
    @DisplayName(value = "히카리 설정 초기화")
    public static void init() {
        mariaDBContainer = new TestContainerService().getMariaDBContainer();
        log.info("MariaDB 컨테이너 실행 상태: {}", mariaDBContainer.isRunning());
        log.info("MariaDB 컨테이너 JDBC URL: {}", mariaDBContainer.getJdbcUrl());
        log.info("MariaDB 컨테이너 포트: {}", mariaDBContainer.getFirstMappedPort());
        configuration = new HikariConfig();
        configuration.setDriverClassName("org.mariadb.jdbc.Driver");
        configuration.setJdbcUrl(mariaDBContainer.getJdbcUrl() + "?allowPublicKeyRetrieval=true");
        configuration.setUsername(mariaDBContainer.getUsername());
        configuration.setPassword(mariaDBContainer.getPassword());
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
            Assertions.assertEquals(mariaDBContainer.getDatabaseName(), connection.getCatalog());
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
        when(mockConnection.getCatalog()).thenReturn(mariaDBContainer.getDatabaseName());
        Connection connection = mockDataSource.getConnection();
        String catalog = connection.getCatalog();
        Assertions.assertEquals(mariaDBContainer.getDatabaseName(), catalog);
        verify(mockDataSource).getConnection();
        verify(mockConnection).getCatalog();
    }
}