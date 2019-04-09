package com.genius.primavera.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DatabaseConnectionComponentTest {

	@Autowired
	private DatabaseConnectionComponent databaseConnectionComponent;

	@Test
	public void connectionTest() throws SQLException {
		Connection connection = databaseConnectionComponent.getConnection();
		assertEquals("study", connection.getCatalog());
	}
}
