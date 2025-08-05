package com.genius.primavera.testcontainer.redundancy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MariaDBContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 직접적인 MariaDB 이중화 테스트 - Spring Context 없이
 * TestContainers를 직접 사용하여 mariadb1, mariadb2 구성 테스트
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("직접 MariaDB 이중화 테스트")
@Slf4j
class DirectContainerTest {

    private static MariaDBContainer<?> mariadb1;
    private static MariaDBContainer<?> mariadb2;

    @BeforeAll
    static void setupContainers() {
        log.info("MariaDB 이중화 컨테이너 설정 시작");
        
        // MariaDB1 컨테이너 생성 및 시작
        mariadb1 = new MariaDBContainer<>("mariadb:11.4.7")
                .withDatabaseName("primavera_master")
                .withUsername("master_user") 
                .withPassword("master_pass")
                .withInitScript("sql/init.sql");
        
        mariadb1.start();
        log.info("MariaDB1 컨테이너 시작 완료 - URL: {}", mariadb1.getJdbcUrl());
        
        // MariaDB2 컨테이너 생성 및 시작
        mariadb2 = new MariaDBContainer<>("mariadb:11.4.7")
                .withDatabaseName("primavera_slave")
                .withUsername("slave_user")
                .withPassword("slave_pass") 
                .withInitScript("sql/init.sql");
        
        mariadb2.start();
        log.info("MariaDB2 컨테이너 시작 완료 - URL: {}", mariadb2.getJdbcUrl());
    }

    @Test
    @Order(1)
    @DisplayName("컨테이너 기본 설정 확인")
    void testContainerSetup() {
        // 컨테이너 상태 확인
        Assertions.assertTrue(mariadb1.isRunning(), "MariaDB1이 실행 중이어야 함");
        Assertions.assertTrue(mariadb2.isRunning(), "MariaDB2가 실행 중이어야 함");
        
        // 포트가 다른지 확인
        int port1 = mariadb1.getMappedPort(3306);
        int port2 = mariadb2.getMappedPort(3306);
        Assertions.assertNotEquals(port1, port2, "두 컨테이너는 다른 포트를 사용해야 함");
        
        log.info("MariaDB1 포트: {}, MariaDB2 포트: {}", port1, port2);
        
        // 데이터베이스명 확인
        Assertions.assertEquals("primavera_master", mariadb1.getDatabaseName());
        Assertions.assertEquals("primavera_slave", mariadb2.getDatabaseName());
        
        // 사용자명 확인
        Assertions.assertEquals("master_user", mariadb1.getUsername());
        Assertions.assertEquals("slave_user", mariadb2.getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("각 데이터베이스 연결 테스트")
    void testDatabaseConnections() throws SQLException {
        // MariaDB1 연결 테스트
        try (Connection conn1 = DriverManager.getConnection(
                mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword())) {
            
            Assertions.assertNotNull(conn1);
            Assertions.assertFalse(conn1.isClosed());
            
            // 데이터베이스 정보 확인
            try (Statement stmt = conn1.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE(), USER()")) {
                
                if (rs.next()) {
                    String database = rs.getString(1);
                    String user = rs.getString(2);
                    log.info("MariaDB1 - 데이터베이스: {}, 사용자: {}", database, user);
                    Assertions.assertEquals("primavera_master", database);
                    Assertions.assertTrue(user.contains("master_user"));
                }
            }
        }

        // MariaDB2 연결 테스트
        try (Connection conn2 = DriverManager.getConnection(
                mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword())) {
            
            Assertions.assertNotNull(conn2);
            Assertions.assertFalse(conn2.isClosed());
            
            // 데이터베이스 정보 확인
            try (Statement stmt = conn2.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE(), USER()")) {
                
                if (rs.next()) {
                    String database = rs.getString(1);
                    String user = rs.getString(2);
                    log.info("MariaDB2 - 데이터베이스: {}, 사용자: {}", database, user);
                    Assertions.assertEquals("primavera_slave", database);
                    Assertions.assertTrue(user.contains("slave_user"));
                }
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("데이터 독립성 테스트")
    void testDataIndependence() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS test_redundancy (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                server_info VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        // MariaDB1에 테이블 생성 및 데이터 삽입
        try (Connection conn1 = DriverManager.getConnection(
                mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword())) {
            
            // 테이블 생성
            try (Statement stmt = conn1.createStatement()) {
                stmt.execute(createTableSQL);
            }
            
            // 데이터 삽입
            try (PreparedStatement pstmt = conn1.prepareStatement(
                    "INSERT INTO test_redundancy (name, server_info) VALUES (?, ?)")) {
                pstmt.setString(1, "Master Data 1");
                pstmt.setString(2, "mariadb1-master");
                pstmt.executeUpdate();
                
                pstmt.setString(1, "Master Data 2");
                pstmt.setString(2, "mariadb1-master");
                pstmt.executeUpdate();
            }
            
            log.info("MariaDB1에 마스터 데이터 2건 삽입 완료");
        }

        // MariaDB2에 테이블 생성 및 다른 데이터 삽입
        try (Connection conn2 = DriverManager.getConnection(
                mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword())) {
            
            // 테이블 생성
            try (Statement stmt = conn2.createStatement()) {
                stmt.execute(createTableSQL);
            }
            
            // 데이터 삽입
            try (PreparedStatement pstmt = conn2.prepareStatement(
                    "INSERT INTO test_redundancy (name, server_info) VALUES (?, ?)")) {
                pstmt.setString(1, "Slave Data 1");
                pstmt.setString(2, "mariadb2-slave");
                pstmt.executeUpdate();
                
                pstmt.setString(1, "Slave Data 2");
                pstmt.setString(2, "mariadb2-slave");
                pstmt.executeUpdate();
                
                pstmt.setString(1, "Slave Data 3");
                pstmt.setString(2, "mariadb2-slave");
                pstmt.executeUpdate();
            }
            
            log.info("MariaDB2에 슬레이브 데이터 3건 삽입 완료");
        }

        // 데이터 독립성 확인
        verifyDataCounts();
    }

    @Test
    @Order(4)
    @DisplayName("동시 접속 테스트")
    void testConcurrentAccess() throws SQLException, InterruptedException {
        // 동시 접속 테스트
        Thread thread1 = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(
                    mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword())) {
                
                for (int i = 1; i <= 5; i++) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO test_redundancy (name, server_info) VALUES (?, ?)")) {
                        pstmt.setString(1, "Concurrent Master " + i);
                        pstmt.setString(2, "mariadb1-concurrent");
                        pstmt.executeUpdate();
                        Thread.sleep(10);
                    }
                }
                log.info("MariaDB1 동시 접속 테스트 완료");
                
            } catch (Exception e) {
                log.error("MariaDB1 동시 접속 테스트 실패", e);
            }
        });

        Thread thread2 = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(
                    mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword())) {
                
                for (int i = 1; i <= 7; i++) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO test_redundancy (name, server_info) VALUES (?, ?)")) {
                        pstmt.setString(1, "Concurrent Slave " + i);
                        pstmt.setString(2, "mariadb2-concurrent");
                        pstmt.executeUpdate();
                        Thread.sleep(8);
                    }
                }
                log.info("MariaDB2 동시 접속 테스트 완료");
                
            } catch (Exception e) {
                log.error("MariaDB2 동시 접속 테스트 실패", e);
            }
        });

        // 동시 실행
        long startTime = System.currentTimeMillis();
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
        long endTime = System.currentTimeMillis();

        log.info("동시 접속 테스트 완료 - 소요시간: {}ms", endTime - startTime);
        
        // 최종 결과 확인
        verifyFinalCounts();
    }

    private void verifyDataCounts() throws SQLException {
        // MariaDB1 데이터 확인
        try (Connection conn1 = DriverManager.getConnection(
                mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword());
             Statement stmt1 = conn1.createStatement();
             ResultSet rs1 = stmt1.executeQuery("SELECT COUNT(*) FROM test_redundancy WHERE server_info = 'mariadb1-master'")) {
            
            if (rs1.next()) {
                int count = rs1.getInt(1);
                log.info("MariaDB1 마스터 데이터 개수: {}", count);
                Assertions.assertEquals(2, count);
            }
        }

        // MariaDB2 데이터 확인
        try (Connection conn2 = DriverManager.getConnection(
                mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword());
             Statement stmt2 = conn2.createStatement();
             ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM test_redundancy WHERE server_info = 'mariadb2-slave'")) {
            
            if (rs2.next()) {
                int count = rs2.getInt(1);
                log.info("MariaDB2 슬레이브 데이터 개수: {}", count);
                Assertions.assertEquals(3, count);
            }
        }
    }

    private void verifyFinalCounts() throws SQLException {
        // MariaDB1 총 데이터 확인
        try (Connection conn1 = DriverManager.getConnection(
                mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword());
             Statement stmt1 = conn1.createStatement();
             ResultSet rs1 = stmt1.executeQuery("SELECT COUNT(*) FROM test_redundancy")) {
            
            if (rs1.next()) {
                int count = rs1.getInt(1);
                log.info("MariaDB1 최종 데이터 개수: {}", count);
                Assertions.assertTrue(count >= 7, "MariaDB1에 최소 7개 데이터가 있어야 함"); // 2 + 5
            }
        }

        // MariaDB2 총 데이터 확인
        try (Connection conn2 = DriverManager.getConnection(
                mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword());
             Statement stmt2 = conn2.createStatement();
             ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM test_redundancy")) {
            
            if (rs2.next()) {
                int count = rs2.getInt(1);
                log.info("MariaDB2 최종 데이터 개수: {}", count);
                Assertions.assertTrue(count >= 10, "MariaDB2에 최소 10개 데이터가 있어야 함"); // 3 + 7
            }
        }
    }

    @AfterAll
    static void cleanup() {
        log.info("MariaDB 이중화 테스트 정리 시작");
        
        if (mariadb1 != null && mariadb1.isRunning()) {
            mariadb1.stop();
            log.info("MariaDB1 컨테이너 중지 완료");
        }
        
        if (mariadb2 != null && mariadb2.isRunning()) {
            mariadb2.stop();
            log.info("MariaDB2 컨테이너 중지 완료");
        }
        
        log.info("MariaDB 이중화 테스트 정리 완료");
    }
}