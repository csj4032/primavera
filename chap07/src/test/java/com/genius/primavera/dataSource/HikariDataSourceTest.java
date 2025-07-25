package com.genius.primavera.dataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;

@Testcontainers
public class HikariDataSourceTest {

	public static final String USER_NAME = "primavera";
	public static final String PASS_WORLD = "primavera";
	public static final String CATALOG = "primavera";
	private static HikariConfig configuration;

	@Container
	protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4.6")
			.withDatabaseName("primavera")
			.withUsername("primavera")
			.withPassword("primavera");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
	}

	@BeforeAll
	@DisplayName(value = "히카리 설정 초기화")
	public static void init() {
		configuration = new HikariConfig();
		configuration.setDriverClassName("com.mysql.cj.jdbc.Driver");
		configuration.setJdbcUrl(mysqlContainer.getJdbcUrl());
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