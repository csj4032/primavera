package com.genius.primavera.persistence;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@Disabled("Database integration tests disabled - using mock tests only")
public class DatabaseConnectionComponentTest {

	@Autowired
	private DatabaseConnectionComponent databaseConnectionComponent;

	@Test
	@DisplayName(value = "스프링 빈을 이용해서 데이터베이스에 접속해보자")
	public void connectionTest() throws SQLException {
		Connection connection = databaseConnectionComponent.getConnection();
		assertEquals("primavera", connection.getCatalog());
	}
}