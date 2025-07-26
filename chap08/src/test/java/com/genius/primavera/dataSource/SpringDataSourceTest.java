package com.genius.primavera.dataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;

import javax.sql.DataSource;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
public class SpringDataSourceTest {

	@Container
	protected static final MariaDBContainer<?> mysqlContainer = new MariaDBContainer<>("mariadb:11.4.7")
			.withDatabaseName("primavera")
			.withUsername("primavera")
			.withPassword("primavera");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mysqlContainer::getUsername);
		registry.add("spring.datasource.password", mysqlContainer::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
		registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
	}

	@Autowired
	private DataSource dataSource;

	@Test
	@DisplayName(value = "스프링 빈을 이용한 데이터베이스 접속")
	public void dataSourceTest() throws SQLException {
		try(var connection = dataSource.getConnection()){
			// P6Spy가 적용되어 있어 ConnectionWrapper가 반환됨
			Assertions.assertTrue(connection.getClass().getName().contains("Connection"));
			Assertions.assertEquals("primavera", connection.getCatalog());
		}
	}
}