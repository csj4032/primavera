package com.genius.primavera.testcontainer.redundancy;

import com.genius.primavera.testcontainer.ContainerSpec;
import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import com.genius.primavera.testcontainer.config.MariaDBContainerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MariaDB 이중화 구성 테스트
 * mariadb1, mariadb2 두 개의 독립적인 MariaDB 컨테이너를 구성하고
 * 각각의 데이터베이스 연결 및 데이터 동기화를 테스트
 */
@SpringBootTest
@EnableTestContainers(containers = {
    @ContainerSpec(
        type = ContainerType.MARIADB, 
        name = "mariadb1", 
        initScript = "sql/init.sql",
        databaseName = "primavera_master",
        username = "master_user",
        password = "master_pass",
        labels = {"role=master", "cluster=primavera"}
    ),
    @ContainerSpec(
        type = ContainerType.MARIADB, 
        name = "mariadb2", 
        initScript = "sql/init.sql",
        databaseName = "primavera_slave", 
        username = "slave_user",
        password = "slave_pass",
        labels = {"role=slave", "cluster=primavera"}
    )
})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MariaDB 이중화 구성 테스트")
@Slf4j
class MariaDBRedundancyTest {

    private static final String TEST_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS test_redundancy (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            server_info VARCHAR(50)
        )
        """;

    @Test
    @Order(1) 
    @DisplayName("이중화 컨테이너 생성 및 기본 설정 확인")
    void testRedundantContainerCreation() {
        // 컨테이너 개수 확인
        int containerCount = MariaDBContainerConfiguration.getCachedContainerCount();
        log.info("생성된 MariaDB 컨테이너 개수: {}", containerCount);
        Assertions.assertEquals(2, containerCount, "정확히 2개의 MariaDB 컨테이너가 생성되어야 함");

        // mariadb1 컨테이너 확인
        MariaDBContainer<?> mariadb1 = MariaDBContainerConfiguration.getContainer("mariadb1");
        Assertions.assertNotNull(mariadb1, "mariadb1 컨테이너가 존재해야 함");
        Assertions.assertTrue(mariadb1.isRunning(), "mariadb1 컨테이너가 실행 중이어야 함");
        
        log.info("MariaDB1 컨테이너 정보:");
        log.info("  - 데이터베이스명: {}", mariadb1.getDatabaseName());
        log.info("  - 사용자명: {}", mariadb1.getUsername());
        log.info("  - JDBC URL: {}", mariadb1.getJdbcUrl());
        log.info("  - 포트: {}", mariadb1.getMappedPort(3306));

        // mariadb2 컨테이너 확인  
        MariaDBContainer<?> mariadb2 = MariaDBContainerConfiguration.getContainer("mariadb2");
        Assertions.assertNotNull(mariadb2, "mariadb2 컨테이너가 존재해야 함");
        Assertions.assertTrue(mariadb2.isRunning(), "mariadb2 컨테이너가 실행 중이어야 함");

        log.info("MariaDB2 컨테이너 정보:");
        log.info("  - 데이터베이스명: {}", mariadb2.getDatabaseName());
        log.info("  - 사용자명: {}", mariadb2.getUsername());
        log.info("  - JDBC URL: {}", mariadb2.getJdbcUrl());
        log.info("  - 포트: {}", mariadb2.getMappedPort(3306));

        // 두 컨테이너가 다른 인스턴스인지 확인
        Assertions.assertNotSame(mariadb1, mariadb2, "mariadb1과 mariadb2는 다른 컨테이너 인스턴스여야 함");
        
        // 다른 포트를 사용하는지 확인
        int port1 = mariadb1.getMappedPort(3306);
        int port2 = mariadb2.getMappedPort(3306);
        Assertions.assertNotEquals(port1, port2, "두 컨테이너는 다른 포트를 사용해야 함");
    }

    @Test
    @Order(2)
    @DisplayName("각 데이터베이스 연결 테스트")
    void testDatabaseConnections() throws SQLException {
        MariaDBContainer<?> mariadb1 = MariaDBContainerConfiguration.getContainer("mariadb1");
        MariaDBContainer<?> mariadb2 = MariaDBContainerConfiguration.getContainer("mariadb2");

        // mariadb1 연결 테스트
        log.info("MariaDB1 연결 테스트 시작");
        try (Connection conn1 = DriverManager.getConnection(
                mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword())) {
            
            Assertions.assertNotNull(conn1, "mariadb1 연결이 성공해야 함");
            Assertions.assertFalse(conn1.isClosed(), "mariadb1 연결이 열려있어야 함");
            
            // 데이터베이스 정보 확인
            try (Statement stmt = conn1.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE(), USER(), VERSION()")) {
                
                if (rs.next()) {
                    String database = rs.getString(1);
                    String user = rs.getString(2);
                    String version = rs.getString(3);
                    
                    log.info("MariaDB1 - 데이터베이스: {}, 사용자: {}, 버전: {}", database, user, version);
                    Assertions.assertEquals("primavera_master", database);
                }
            }
        }

        // mariadb2 연결 테스트
        log.info("MariaDB2 연결 테스트 시작");
        try (Connection conn2 = DriverManager.getConnection(
                mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword())) {
            
            Assertions.assertNotNull(conn2, "mariadb2 연결이 성공해야 함");
            Assertions.assertFalse(conn2.isClosed(), "mariadb2 연결이 열려있어야 함");
            
            // 데이터베이스 정보 확인
            try (Statement stmt = conn2.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE(), USER(), VERSION()")) {
                
                if (rs.next()) {
                    String database = rs.getString(1);
                    String user = rs.getString(2);
                    String version = rs.getString(3);
                    
                    log.info("MariaDB2 - 데이터베이스: {}, 사용자: {}, 버전: {}", database, user, version);
                    Assertions.assertEquals("primavera_slave", database);
                }
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("테이블 생성 및 데이터 독립성 테스트")
    void testDataIndependence() throws SQLException {
        MariaDBContainer<?> mariadb1 = MariaDBContainerConfiguration.getContainer("mariadb1");
        MariaDBContainer<?> mariadb2 = MariaDBContainerConfiguration.getContainer("mariadb2");

        // mariadb1에 테이블 생성 및 데이터 삽입
        try (Connection conn1 = DriverManager.getConnection(
                mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword())) {
            
            // 테이블 생성
            try (Statement stmt = conn1.createStatement()) {
                stmt.execute(TEST_TABLE_SQL);
                log.info("MariaDB1에 test_redundancy 테이블 생성 완료");
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
                
                log.info("MariaDB1에 마스터 데이터 2건 삽입 완료");
            }
        }

        // mariadb2에 테이블 생성 및 다른 데이터 삽입
        try (Connection conn2 = DriverManager.getConnection(
                mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword())) {
            
            // 테이블 생성
            try (Statement stmt = conn2.createStatement()) {
                stmt.execute(TEST_TABLE_SQL);
                log.info("MariaDB2에 test_redundancy 테이블 생성 완료");
            }
            
            // 다른 데이터 삽입
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
                
                log.info("MariaDB2에 슬레이브 데이터 3건 삽입 완료");
            }
        }

        // 데이터 독립성 확인 - 각 DB의 데이터가 다른지 확인
        verifyDataIndependence(mariadb1, mariadb2);
    }

    @Test
    @Order(4)
    @DisplayName("동시 접속 및 성능 테스트")
    void testConcurrentAccess() throws SQLException, InterruptedException {
        MariaDBContainer<?> mariadb1 = MariaDBContainerConfiguration.getContainer("mariadb1");
        MariaDBContainer<?> mariadb2 = MariaDBContainerConfiguration.getContainer("mariadb2");

        // 동시 접속 테스트
        Thread thread1 = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(
                    mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword())) {
                
                for (int i = 0; i < 10; i++) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO test_redundancy (name, server_info) VALUES (?, ?)")) {
                        pstmt.setString(1, "Concurrent Test 1-" + i);
                        pstmt.setString(2, "mariadb1-thread1");
                        pstmt.executeUpdate();
                        Thread.sleep(10); // 짧은 대기
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
                
                for (int i = 0; i < 15; i++) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO test_redundancy (name, server_info) VALUES (?, ?)")) {
                        pstmt.setString(1, "Concurrent Test 2-" + i);
                        pstmt.setString(2, "mariadb2-thread2");
                        pstmt.executeUpdate();
                        Thread.sleep(8); // 짧은 대기
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

        // 완료 대기
        thread1.join();
        thread2.join();
        long endTime = System.currentTimeMillis();

        log.info("동시 접속 테스트 완료 - 소요시간: {}ms", endTime - startTime);
        
        // 결과 확인
        verifyFinalDataCount(mariadb1, mariadb2);
    }

    private void verifyDataIndependence(MariaDBContainer<?> mariadb1, MariaDBContainer<?> mariadb2) throws SQLException {
        // mariadb1 데이터 확인
        try (Connection conn1 = DriverManager.getConnection(
                mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword());
             Statement stmt1 = conn1.createStatement();
             ResultSet rs1 = stmt1.executeQuery("SELECT COUNT(*), server_info FROM test_redundancy GROUP BY server_info")) {
            
            while (rs1.next()) {
                int count = rs1.getInt(1);
                String serverInfo = rs1.getString(2);
                log.info("MariaDB1 데이터 - server_info: {}, count: {}", serverInfo, count);
                Assertions.assertEquals("mariadb1-master", serverInfo);
            }
        }

        // mariadb2 데이터 확인
        try (Connection conn2 = DriverManager.getConnection(
                mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword());
             Statement stmt2 = conn2.createStatement();
             ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*), server_info FROM test_redundancy GROUP BY server_info")) {
            
            while (rs2.next()) {
                int count = rs2.getInt(1);
                String serverInfo = rs2.getString(2);
                log.info("MariaDB2 데이터 - server_info: {}, count: {}", serverInfo, count);
                Assertions.assertEquals("mariadb2-slave", serverInfo);
            }
        }
    }

    private void verifyFinalDataCount(MariaDBContainer<?> mariadb1, MariaDBContainer<?> mariadb2) throws SQLException {
        // mariadb1 총 레코드 수 확인
        try (Connection conn1 = DriverManager.getConnection(
                mariadb1.getJdbcUrl(), mariadb1.getUsername(), mariadb1.getPassword());
             Statement stmt1 = conn1.createStatement();
             ResultSet rs1 = stmt1.executeQuery("SELECT COUNT(*) FROM test_redundancy")) {
            
            if (rs1.next()) {
                int count1 = rs1.getInt(1);
                log.info("MariaDB1 최종 레코드 수: {}", count1);
                Assertions.assertTrue(count1 >= 12, "mariadb1에 최소 12개 레코드가 있어야 함"); // 2 + 10
            }
        }

        // mariadb2 총 레코드 수 확인
        try (Connection conn2 = DriverManager.getConnection(
                mariadb2.getJdbcUrl(), mariadb2.getUsername(), mariadb2.getPassword());
             Statement stmt2 = conn2.createStatement();
             ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM test_redundancy")) {
            
            if (rs2.next()) {
                int count2 = rs2.getInt(1);
                log.info("MariaDB2 최종 레코드 수: {}", count2);
                Assertions.assertTrue(count2 >= 18, "mariadb2에 최소 18개 레코드가 있어야 함"); // 3 + 15
            }
        }
    }

    @AfterAll
    static void cleanup() {
        log.info("MariaDB 이중화 테스트 완료 - 정리 작업 실행");
        MariaDBContainerConfiguration.clearCache();
    }
}