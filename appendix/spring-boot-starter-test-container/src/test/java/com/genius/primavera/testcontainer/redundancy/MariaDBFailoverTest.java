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
 * MariaDB 페일오버 시나리오 테스트
 * Primary 서버 장애 상황에서 Secondary 서버로 전환하는 시나리오 테스트
 */
@SpringBootTest
@EnableTestContainers(containers = {
    @ContainerSpec(
        type = ContainerType.MARIADB, 
        name = "primary", 
        initScript = "sql/init.sql",
        databaseName = "primavera_primary",
        username = "primary_user",
        password = "primary_pass",
        labels = {"role=primary", "cluster=failover-test"}
    ),
    @ContainerSpec(
        type = ContainerType.MARIADB, 
        name = "secondary", 
        initScript = "sql/init.sql",
        databaseName = "primavera_secondary", 
        username = "secondary_user",
        password = "secondary_pass",
        labels = {"role=secondary", "cluster=failover-test"}
    )
})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MariaDB 페일오버 시나리오 테스트")
@Slf4j
class MariaDBFailoverTest {

    private static final String TEST_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS failover_test (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            message VARCHAR(200) NOT NULL,
            server_role VARCHAR(20) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;

    @Test
    @Order(1)
    @DisplayName("페일오버 환경 초기 설정")
    void testInitialSetup() throws SQLException {
        // 컨테이너 확인
        MariaDBContainer<?> primary = MariaDBContainerConfiguration.getContainer("primary");  
        MariaDBContainer<?> secondary = MariaDBContainerConfiguration.getContainer("secondary");

        Assertions.assertNotNull(primary, "Primary 컨테이너가 존재해야 함");
        Assertions.assertNotNull(secondary, "Secondary 컨테이너가 존재해야 함");
        
        Assertions.assertTrue(primary.isRunning(), "Primary 컨테이너가 실행 중이어야 함");
        Assertions.assertTrue(secondary.isRunning(), "Secondary 컨테이너가 실행 중이어야 함");

        // 양쪽 데이터베이스에 테이블 생성
        setupDatabase(primary, "PRIMARY");
        setupDatabase(secondary, "SECONDARY");
        
        log.info("페일오버 테스트 환경 초기화 완료");
    }

    @Test
    @Order(2)
    @DisplayName("정상 상태에서 Primary 서버 사용")
    void testNormalOperationWithPrimary() throws SQLException {
        MariaDBContainer<?> primary = MariaDBContainerConfiguration.getContainer("primary");
        
        // Primary 서버에 데이터 삽입
        try (Connection conn = DriverManager.getConnection(
                primary.getJdbcUrl(), primary.getUsername(), primary.getPassword())) {
            
            insertTestData(conn, "PRIMARY", "정상 운영 중 데이터 1");
            insertTestData(conn, "PRIMARY", "정상 운영 중 데이터 2");
            insertTestData(conn, "PRIMARY", "정상 운영 중 데이터 3");
            
            log.info("Primary 서버에 정상 운영 데이터 3건 삽입 완료");
        }
        
        // 데이터 확인
        int primaryCount = getRecordCount(primary);
        Assertions.assertEquals(3, primaryCount, "Primary 서버에 3건의 데이터가 있어야 함");
        
        log.info("정상 상태 Primary 서버 운영 테스트 완료");
    }

    @Test
    @Order(3)
    @DisplayName("Primary 서버 장애 시뮬레이션")
    void testPrimaryServerFailure() throws SQLException {
        MariaDBContainer<?> primary = MariaDBContainerConfiguration.getContainer("primary");
        MariaDBContainer<?> secondary = MariaDBContainerConfiguration.getContainer("secondary");
        
        // Primary 서버 장애 전 상태 확인
        int primaryCountBeforeFailure = getRecordCount(primary);
        int secondaryCountBeforeFailure = getRecordCount(secondary);
        
        log.info("장애 전 상태 - Primary: {}건, Secondary: {}건", 
                primaryCountBeforeFailure, secondaryCountBeforeFailure);
        
        // Primary 서버 장애 시뮬레이션 (컨테이너 중지)
        log.info("Primary 서버 장애 시뮬레이션 시작...");
        primary.stop();
        
        // Primary 서버가 중지되었는지 확인
        Assertions.assertFalse(primary.isRunning(), "Primary 서버가 중지되어야 함");
        
        // Primary 서버 연결 시도 시 실패하는지 확인
        Assertions.assertThrows(SQLException.class, () -> {
            try (Connection conn = DriverManager.getConnection(
                    primary.getJdbcUrl(), primary.getUsername(), primary.getPassword())) {
                conn.createStatement().executeQuery("SELECT 1");
            }
        }, "Primary 서버 장애 시 연결이 실패해야 함");
        
        log.info("Primary 서버 장애 시뮬레이션 완료");
    }

    @Test
    @Order(4)
    @DisplayName("Secondary 서버로 페일오버")
    void testFailoverToSecondary() throws SQLException {
        MariaDBContainer<?> secondary = MariaDBContainerConfiguration.getContainer("secondary");
        
        // Secondary 서버가 여전히 작동하는지 확인
        Assertions.assertTrue(secondary.isRunning(), "Secondary 서버는 여전히 실행 중이어야 함");
        
        // Secondary 서버로 페일오버하여 데이터 작업 수행
        try (Connection conn = DriverManager.getConnection(
                secondary.getJdbcUrl(), secondary.getUsername(), secondary.getPassword())) {
            
            insertTestData(conn, "SECONDARY", "페일오버 후 데이터 1");
            insertTestData(conn, "SECONDARY", "페일오버 후 데이터 2");
            insertTestData(conn, "SECONDARY", "페일오버 후 데이터 3"); 
            insertTestData(conn, "SECONDARY", "페일오버 후 데이터 4");
            
            log.info("페일오버 후 Secondary 서버에 데이터 4건 삽입 완료");
        }
        
        // Secondary 서버 데이터 확인
        int secondaryCount = getRecordCount(secondary);
        Assertions.assertEquals(4, secondaryCount, "Secondary 서버에 4건의 데이터가 있어야 함");
        
        log.info("Secondary 서버로 페일오버 테스트 완료");
    }

    @Test
    @Order(5)
    @DisplayName("Primary 서버 복구 시나리오")
    void testPrimaryServerRecovery() throws SQLException, InterruptedException {
        MariaDBContainer<?> primary = MariaDBContainerConfiguration.getContainer("primary");
        MariaDBContainer<?> secondary = MariaDBContainerConfiguration.getContainer("secondary");
        
        // Primary 서버 복구 (컨테이너 재시작)
        log.info("Primary 서버 복구 시작...");
        primary.start();
        
        // 복구 대기 시간
        Thread.sleep(2000);
        
        // Primary 서버가 다시 실행되는지 확인
        Assertions.assertTrue(primary.isRunning(), "Primary 서버가 복구되어야 함");
        
        // Primary 서버 연결 확인
        try (Connection conn = DriverManager.getConnection(
                primary.getJdbcUrl(), primary.getUsername(), primary.getPassword())) {
            
            Assertions.assertNotNull(conn, "복구된 Primary 서버에 연결할 수 있어야 함");
            
            // 복구 후 데이터 상태 확인 (장애 전 데이터가 유지되는지)
            int recoveredCount = getRecordCount(primary);
            log.info("Primary 서버 복구 후 데이터 개수: {}", recoveredCount);
            
            // 복구 확인 데이터 삽입
            insertTestData(conn, "PRIMARY", "복구 후 테스트 데이터");
            
            log.info("Primary 서버 복구 완료");
        }
        
        // 최종 상태 확인
        int finalPrimaryCount = getRecordCount(primary);
        int finalSecondaryCount = getRecordCount(secondary);
        
        log.info("최종 상태 - Primary: {}건, Secondary: {}건", 
                finalPrimaryCount, finalSecondaryCount);
        
        Assertions.assertTrue(finalPrimaryCount > 0, "복구된 Primary 서버에 데이터가 있어야 함");
        Assertions.assertEquals(4, finalSecondaryCount, "Secondary 서버 데이터는 유지되어야 함");
    }

    @Test
    @Order(6)
    @DisplayName("양방향 동시 작업 테스트")
    void testBidirectionalOperations() throws SQLException, InterruptedException {
        MariaDBContainer<?> primary = MariaDBContainerConfiguration.getContainer("primary");
        MariaDBContainer<?> secondary = MariaDBContainerConfiguration.getContainer("secondary");
        
        // 양쪽 서버에 동시에 데이터 작업 수행
        Thread primaryWorker = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(
                    primary.getJdbcUrl(), primary.getUsername(), primary.getPassword())) {
                
                for (int i = 1; i <= 5; i++) {
                    insertTestData(conn, "PRIMARY", "동시작업 Primary " + i);
                    Thread.sleep(50);
                }
                log.info("Primary 서버 동시 작업 완료");
                
            } catch (Exception e) {
                log.error("Primary 서버 동시 작업 실패", e);
            }
        });
        
        Thread secondaryWorker = new Thread(() -> {
            try (Connection conn = DriverManager.getConnection(
                    secondary.getJdbcUrl(), secondary.getUsername(), secondary.getPassword())) {
                
                for (int i = 1; i <= 7; i++) {
                    insertTestData(conn, "SECONDARY", "동시작업 Secondary " + i);
                    Thread.sleep(40);
                }
                log.info("Secondary 서버 동시 작업 완료");
                
            } catch (Exception e) {
                log.error("Secondary 서버 동시 작업 실패", e);
            }
        });
        
        // 동시 실행
        long startTime = System.currentTimeMillis();
        primaryWorker.start();
        secondaryWorker.start();
        
        primaryWorker.join();
        secondaryWorker.join();
        long endTime = System.currentTimeMillis();
        
        log.info("양방향 동시 작업 완료 - 소요시간: {}ms", endTime - startTime);
        
        // 최종 결과 확인
        int finalPrimaryCount = getRecordCount(primary);
        int finalSecondaryCount = getRecordCount(secondary);
        
        log.info("동시 작업 후 최종 상태 - Primary: {}건, Secondary: {}건", 
                finalPrimaryCount, finalSecondaryCount);
        
        // Primary는 기존 데이터 + 5건, Secondary는 기존 4건 + 7건
        Assertions.assertTrue(finalPrimaryCount >= 5, "Primary 서버에 추가 데이터가 있어야 함");
        Assertions.assertTrue(finalSecondaryCount >= 11, "Secondary 서버에 충분한 데이터가 있어야 함");
    }

    private void setupDatabase(MariaDBContainer<?> container, String role) throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword());
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(TEST_TABLE_SQL);
            log.info("{} 서버에 failover_test 테이블 생성 완료", role);
        }
    }

    private void insertTestData(Connection conn, String serverRole, String message) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO failover_test (message, server_role) VALUES (?, ?)")) {
            pstmt.setString(1, message);
            pstmt.setString(2, serverRole);
            pstmt.executeUpdate();
        }
    }

    private int getRecordCount(MariaDBContainer<?> container) throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                container.getJdbcUrl(), container.getUsername(), container.getPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM failover_test")) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    @AfterAll
    static void cleanup() {
        log.info("MariaDB 페일오버 테스트 완료 - 정리 작업 실행");
        
        // 모든 컨테이너가 실행 중인 상태로 정리
        MariaDBContainer<?> primary = MariaDBContainerConfiguration.getContainer("primary");
        if (primary != null && !primary.isRunning()) {
            try {
                primary.start();
                log.info("Primary 컨테이너 복구하여 정리");
            } catch (Exception e) {
                log.warn("Primary 컨테이너 복구 실패", e);
            }
        }
        
        MariaDBContainerConfiguration.clearCache();
    }
}