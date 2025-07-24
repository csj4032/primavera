package com.genius.primavera.dataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
public class SpringDataSourceTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/schema.sql");

    // Spring 환경에 MySQL 컨테이너 속성 등록
    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName(value = "스프링 빈을 이용한 데이터베이스 접속")
    public void dataSourceTest() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            // 왜 hikari 인가?
            Assertions.assertEquals("com.zaxxer.hikari.pool.HikariProxyConnection", connection.getClass().getName());
            Assertions.assertEquals("primavera", connection.getCatalog());
        }
    }

    @Test
    @DisplayName(value = "데이터소스 모킹 테스트")
    public void dataSourceMockTest() throws SQLException {
        // Mock DataSource and Connection
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);

        // Configure mock behavior
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.getCatalog()).thenReturn("primavera");

        // Test
        try (Connection connection = mockDataSource.getConnection()) {
            String catalog = connection.getCatalog();

            // Verify
            Assertions.assertEquals("primavera", catalog);
            verify(mockDataSource, times(1)).getConnection();
            verify(mockConnection, times(1)).getCatalog();
        }
    }

    @Test
    @DisplayName("Docker MySQL 컨테이너 연결 테스트")
    public void dockerMySQLDataSourceTest() throws SQLException {
        System.out.println("MySQL 컨테이너 JDBC URL: " + mysqlContainer.getJdbcUrl());
        System.out.println("MySQL 컨테이너 포트: " + mysqlContainer.getFirstMappedPort());

        try (Connection connection = mysqlContainer.createConnection("")) {
            Assertions.assertEquals("primavera", connection.getCatalog());
            Assertions.assertFalse(connection.isClosed());

            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT 1");
                Assertions.assertTrue(resultSet.next());
                Assertions.assertEquals(1, resultSet.getInt(1));

                resultSet = statement.executeQuery("SELECT COUNT(*) FROM USERS");
                Assertions.assertTrue(resultSet.next());
                Assertions.assertEquals(4, resultSet.getInt(1));
            }
        }
    }
}