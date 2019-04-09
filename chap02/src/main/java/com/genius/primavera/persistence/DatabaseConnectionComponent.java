package com.genius.primavera.persistence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnectionComponent {

	@Value(value = "${mariadb.url}")
	public String url;
	@Value(value = "${mariadb.user}")
	public String user;
	@Value(value = "${mariadb.password}")
	public String password;

	public Connection getConnection() {
		try {
			DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}