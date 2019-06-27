package com.genius.primavera.persistence;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName(value = "데이터베이스 접속")
public class DatabaseConnectionTest {

    public static final String URL = "jdbc:mariadb://localhost:3306/primavera";

    @Test
    @Order(1)
    @DisplayName(value = "Socket 데이터베이스에 접속해보자")
    public void socketTest() {
        try {
            SocketChannel client = SocketChannel.open();
            client.connect(new InetSocketAddress("localhost", 3306));
            assertTrue(client.isConnected());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(2)
    @DisplayName(value = "데이터베이스에 접속해보자")
    public void connectionTest() {
        try {
            DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
            Connection connection = DriverManager.getConnection(URL, "primavera", "primavera");
            assertEquals("primavera", connection.getCatalog());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(3)
    @DisplayName(value = "DatabaseConnection 클래스를 만들어서 데이터베이스에 접속해보자")
    public void DatabaseConnectionTest() throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        assertEquals("primavera", connection.getCatalog());
    }
}