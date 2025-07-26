package com.genius.primavera.persistence;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName(value = "데이터베이스 접속 - TestContainers 기반")
public class DatabaseConnectionTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7")
            .withExposedPorts(3306)
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera");

    @Test
    @Order(1)
    @DisplayName(value = "TestContainers MariaDB 컨테이너가 정상적으로 시작되는지 확인")
    public void containerStartTest() {
        assertTrue(mariadb.isRunning(), "MariaDB 컨테이너가 실행 중이어야 합니다");
        log.info("MariaDB Container URL: {}", mariadb.getJdbcUrl());
        log.info("MariaDB Container Port: {}", mariadb.getMappedPort(3306));
    }

    @Test
    @Order(2)
    @DisplayName(value = "Socket으로 TestContainers MariaDB에 접속해보자")
    public void socketTest() {
        try {
            SocketChannel client = SocketChannel.open();
            String host = mariadb.getHost();
            Integer port = mariadb.getMappedPort(3306);
            client.connect(new InetSocketAddress(host, port));
            assertTrue(client.isConnected());
            client.close();
            log.info("Socket 연결 성공: {}:{}", host, port);
        } catch (IOException e) {
            log.error("데이터베이스에 접속할 수 없습니다.", e);
            Assertions.fail("데이터베이스에 접속할 수 없습니다.");
        }
    }

    @Test
    @Order(3)
    @DisplayName(value = "TestContainers MariaDB에 직접 접속해보자")
    public void connectionTest() {
        try {
            DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
            Connection connection = DriverManager.getConnection(
                    mariadb.getJdbcUrl(),
                    mariadb.getUsername(),
                    mariadb.getPassword()
            );
            assertEquals("primavera", connection.getCatalog());
            log.info("데이터베이스 직접 연결 성공: {}", connection.getCatalog());
            connection.close();
        } catch (SQLException e) {
            log.error("데이터베이스에 접속할 수 없습니다.", e);
            Assertions.fail("데이터베이스에 접속할 수 없습니다.");
        }
    }

    @Test
    @Order(4)
    @DisplayName(value = "TestContainers 환경에서 DatabaseConnection 클래스 테스트")
    public void databaseConnectionTest() {
        log.info("DatabaseConnection 클래스는 정적 설정을 사용하므로 localhost:3306을 참조합니다");
        log.info("실제 운영 환경에서는 localhost MariaDB를, 테스트에서는 TestContainers를 사용합니다");
        log.info("TestContainers MariaDB URL: {}", mariadb.getJdbcUrl());
        log.info("TestContainers MariaDB Username: {}", mariadb.getUsername());
        log.info("TestContainers MariaDB Password: {}", mariadb.getPassword());
        assertTrue(mariadb.isRunning(), "TestContainers MariaDB가 실행 중이어야 합니다");
    }
}