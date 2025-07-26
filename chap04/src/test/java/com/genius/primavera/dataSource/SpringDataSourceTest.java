package com.genius.primavera.dataSource;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@PrimaveraTestContainer
public class SpringDataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName(value = "스프링 빈을 이용한 데이터베이스 접속")
    public void dataSourceTest() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            Assertions.assertEquals("com.zaxxer.hikari.pool.HikariProxyConnection", connection.getClass().getName());
            Assertions.assertEquals("primavera", connection.getCatalog());
        }
    }

    @Test
    @DisplayName(value = "데이터소스 모킹 테스트")
    public void dataSourceMockTest() throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.getCatalog()).thenReturn("primavera");

        try (Connection connection = mockDataSource.getConnection()) {
            String catalog = connection.getCatalog();
            Assertions.assertEquals("primavera", catalog);
            verify(mockDataSource, times(1)).getConnection();
            verify(mockConnection, times(1)).getCatalog();
        }
    }
}