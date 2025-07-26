package com.genius.primavera.persistence;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static final String URL = "jdbc:mariadb://localhost:3306/primavera";

    public static Connection getConnection() {
        try {
            DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
            return DriverManager.getConnection(URL, "primavera", "primavera");
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}