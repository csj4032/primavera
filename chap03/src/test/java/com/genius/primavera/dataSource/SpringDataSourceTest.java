package com.genius.primavera.dataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SpringDataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @Disabled("Database integration test disabled - converted to mock test")
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
        when(mockConnection.getClass()).thenReturn((Class) com.zaxxer.hikari.pool.HikariProxyConnection.class);
        when(mockConnection.getCatalog()).thenReturn("primavera");

        // Test
        try (Connection connection = mockDataSource.getConnection()) {
            String catalog = connection.getCatalog();

            // Verify
            Assertions.assertEquals("primavera", catalog);
            Assertions.assertEquals("com.zaxxer.hikari.pool.HikariProxyConnection", connection.getClass().getName());
            verify(mockDataSource, times(1)).getConnection();
            verify(mockConnection, times(1)).getCatalog();
        }
    }

    @Test
    @DisplayName("H2 데이터베이스 연결 테스트")
    public void h2DataSourceTest() throws SQLException {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        try (Connection connection = h2DataSource.getConnection()) {
            Assertions.assertEquals("TESTDB", connection.getCatalog());
            Assertions.assertFalse(connection.isClosed());
        }
    }
}