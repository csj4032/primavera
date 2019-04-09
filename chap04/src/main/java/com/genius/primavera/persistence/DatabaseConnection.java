package com.genius.primavera.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

	private DatabaseConnection() {
	}

	public static final String URL = "jdbc:mariadb://localhost:3306/study";

	public static Connection getConnection() {
		try {
			DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
			return DriverManager.getConnection(URL, "study", "study");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}