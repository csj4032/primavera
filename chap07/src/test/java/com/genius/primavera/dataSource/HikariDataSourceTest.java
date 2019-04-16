package com.genius.primavera.dataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

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
	@DisplayName(value = "히카리를 이용해 데이터베이스에 커넥션")
	public void hikariDataSourceTest() throws SQLException {
		try (var hikariDataSource = new HikariDataSource(configuration)) {
			var connection = hikariDataSource.getConnection();
			Assertions.assertEquals(CATALOG, connection.getCatalog());
		}
	}
}