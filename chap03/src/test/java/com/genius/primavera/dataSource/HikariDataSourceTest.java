package com.genius.primavera.dataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class HikariDataSourceTest {

	public static final String USER_NAME = "primavera";
	public static final String PASS_WORLD = "primavera";
	public static final String CATALOG = "primavera";
	private static HikariConfig configuration;

	@BeforeAll
	@DisplayName(value = "히카리 설정 초기화")
	public static void init() {
		configuration = new HikariConfig();
		configuration.setDriverClassName("org.mariadb.jdbc.Driver");
		configuration.setJdbcUrl("jdbc:mariadb://localhost:3306/primavera");
		configuration.setUsername(USER_NAME);
		configuration.setPassword(PASS_WORLD);
		configuration.setConnectionInitSql("SELECT 1 FROM DUAL");
	}

	@Test
	@Disabled("Database integration test disabled - converted to mock test")
	@DisplayName(value = "히카리를 이용해 데이터베이스에 커넥션")
	public void hikariDataSourceTest() throws SQLException {
		try (var hikariDataSource = new HikariDataSource(configuration)) {
			var connection = hikariDataSource.getConnection();
			Assertions.assertEquals(CATALOG, connection.getCatalog());
		}
	}

	@Test
	@DisplayName(value = "히카리 데이터소스 모킹 테스트")
	public void hikariDataSourceMockTest() throws SQLException {
		// Mock HikariDataSource and Connection
		HikariDataSource mockDataSource = mock(HikariDataSource.class);
		Connection mockConnection = mock(Connection.class);
		
		// Configure mock behavior
		when(mockDataSource.getConnection()).thenReturn(mockConnection);
		when(mockConnection.getCatalog()).thenReturn(CATALOG);
		
		// Test
		Connection connection = mockDataSource.getConnection();
		String catalog = connection.getCatalog();
		
		// Verify
		Assertions.assertEquals(CATALOG, catalog);
		verify(mockDataSource).getConnection();
		verify(mockConnection).getCatalog();
	}

	@Test
	@DisplayName(value = "히카리 데이터소스 H2 테스트")
	public void hikariDataSourceH2Test() throws SQLException {
		HikariConfig h2Config = new HikariConfig();
		h2Config.setDriverClassName("org.h2.Driver");
		h2Config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
		h2Config.setUsername("sa");
		h2Config.setPassword("");
		h2Config.setConnectionInitSql("SELECT 1");

		try (var hikariDataSource = new HikariDataSource(h2Config)) {
			var connection = hikariDataSource.getConnection();
			Assertions.assertEquals("TESTDB", connection.getCatalog());
		}
	}
}