package com.genius.primavera.persistence;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseConnectionTest {

	@Test
	public void connectionTest() throws SQLException {
		Connection connection = DatabaseConnection.getConnection();
		assertEquals("study", connection.getCatalog());
	}
}
