package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("오류 처리 및 복구 테스트")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "resilientDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "resilientCache")
})
class ErrorHandlingRecoveryTest {

    @Autowired
    @Qualifier("resilientDb")
    private DataSource resilientDataSource;

    private JdbcTemplate resilientJdbc;
    private ContainerManager containerManager;

    @BeforeAll
    void setupErrorTests() {
        resilientJdbc = new JdbcTemplate(resilientDataSource);
        containerManager = ContainerRegistry.get();

        resilientJdbc.execute("""
            CREATE TABLE error_test (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                data VARCHAR(500),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Initial test data");

        log.info("오류 처리 테스트 환경 초기화 완료");
    }

    @Test
    @Order(1)
    @DisplayName("잘못된 SQL 쿼리 오류 처리")
    void testInvalidSqlErrorHandling() {
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.execute("SELCT * FROM error_test"); // 의도적인 오타
        }, "잘못된 SQL 구문으로 예외가 발생해야 함");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.queryForObject("SELECT COUNT(*) FROM non_existent_table", Integer.class);
        }, "존재하지 않는 테이블 쿼리로 예외가 발생해야 함");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", "invalid_id", "test data");
        }, "데이터 타입 불일치로 예외가 발생해야 함");

        Integer count = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertNotNull(count, "정상적인 쿼리는 여전히 작동해야 함");
        assertTrue(count > 0, "테스트 데이터가 존재해야 함");

        log.info("잘못된 SQL 쿼리 오류 처리 테스트 완료");
    }

    @Test
    @Order(2)
    @DisplayName("제약 조건 위반 오류 처리")
    void testConstraintViolationErrorHandling() {
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", 1, "Duplicate key test");
        }, "PRIMARY KEY 중복으로 예외가 발생해야 함");

        resilientJdbc.execute("ALTER TABLE error_test MODIFY data VARCHAR(500) NOT NULL");
        
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (data) VALUES (NULL)");
        }, "NOT NULL 제약 조건 위반으로 예외가 발생해야 함");

        resilientJdbc.execute("""
            CREATE TABLE parent_table (
                id BIGINT PRIMARY KEY,
                name VARCHAR(100)
            )
        """);

        resilientJdbc.execute("""
            CREATE TABLE child_table (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                parent_id BIGINT,
                data VARCHAR(100),
                FOREIGN KEY (parent_id) REFERENCES parent_table(id)
            )
        """);

        resilientJdbc.update("INSERT INTO parent_table (id, name) VALUES (1, 'Test Parent')");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO child_table (parent_id, data) VALUES (?, ?)", 999, "Invalid FK");
        }, "외래 키 제약 조건 위반으로 예외가 발생해야 함");

        int result = resilientJdbc.update("INSERT INTO child_table (parent_id, data) VALUES (?, ?)", 1, "Valid FK");
        assertEquals(1, result, "정상적인 외래 키로 삽입이 성공해야 함");

        log.info("제약 조건 위반 오류 처리 테스트 완료");
    }

    @Test
    @Order(3)
    @DisplayName("연결 타임아웃 및 복구 테스트")
    void testConnectionTimeoutAndRecovery() throws InterruptedException {
        Integer initialCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertNotNull(initialCount, "초기 연결이 정상적으로 작동해야 함");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.queryForObject("SELECT SLEEP(31)", Integer.class); // 30초 이상 대기
        }, "쿼리 타임아웃으로 예외가 발생해야 함");

        Thread.sleep(1000);

        assertDoesNotThrow(() -> {
            Integer count = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
            assertNotNull(count, "타임아웃 후에도 새 연결로 쿼리가 가능해야 함");
        }, "타임아웃 후 연결 복구가 이루어져야 함");

        log.info("연결 타임아웃 및 복구 테스트 완료");
    }

    @Test
    @Order(4) 
    @DisplayName("트랜잭션 롤백 중 오류 처리")
    void testTransactionRollbackErrorHandling() {
        Integer beforeCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.execute("START TRANSACTION");
            try {
                resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Transaction test 1");
                
                resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", 1, "Duplicate PK");
                
                resilientJdbc.execute("COMMIT");
            } catch (Exception e) {
                resilientJdbc.execute("ROLLBACK");
                throw e;
            }
        }, "트랜잭션 중 제약 조건 위반으로 예외가 발생해야 함");

        Integer afterCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeCount, afterCount, "트랜잭션 롤백으로 데이터가 변경되지 않아야 함");

        resilientJdbc.execute("START TRANSACTION");
        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "After rollback test");
        resilientJdbc.execute("COMMIT");

        Integer finalCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeCount + 1, finalCount, "롤백 후 정상적인 트랜잭션이 작동해야 함");

        log.info("트랜잭션 롤백 오류 처리 테스트 완료");
    }

    @Test
    @Order(5)
    @DisplayName("데이터베이스 연결 중단 및 자동 복구")
    void testDatabaseConnectionInterruptionAndRecovery() throws InterruptedException {
        ContainerInfo containerInfo = containerManager.getContainer("resilientDb");
        assertNotNull(containerInfo, "컨테이너 정보가 존재해야 함");
        
        GenericContainer<?> container = containerInfo.container();
        assertTrue(container.isRunning(), "컨테이너가 실행 중이어야 함");

        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Before container stop");
        
        Integer beforeStopCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);

        log.info("컨테이너 일시 중지 시뮬레이션 시작");
        
        container.getDockerClient().pauseContainerCmd(container.getContainerId()).exec();

        assertThrows(Exception.class, () -> {
            resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        }, "컨테이너 중지 중에는 연결이 실패해야 함");

        container.getDockerClient().unpauseContainerCmd(container.getContainerId()).exec();
        
        Thread.sleep(2000);

        log.info("컨테이너 복구 후 연결 재시도");

        boolean recovered = false;
        for (int i = 0; i < 10; i++) {
            try {
                Integer afterResumeCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
                assertEquals(beforeStopCount, afterResumeCount, "데이터가 보존되어야 함");
                recovered = true;
                break;
            } catch (Exception e) {
                log.warn("복구 시도 {}/10 실패: {}", i + 1, e.getMessage());
                Thread.sleep(1000);
            }
        }

        assertTrue(recovered, "컨테이너 복구 후 연결이 재개되어야 함");

        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "After container resume");
        
        Integer finalCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeStopCount + 1, finalCount, "복구 후 새로운 작업이 가능해야 함");

        log.info("데이터베이스 연결 중단 및 자동 복구 테스트 완료");
    }

    @Test
    @Order(6)
    @DisplayName("메모리 부족 상황에서의 오류 처리")
    void testOutOfMemoryErrorHandling() {
        String largeData = "X".repeat(1000000); // 1MB 문자열

        assertThrows(DataAccessException.class, () -> {
            for (int i = 0; i < 100; i++) {
                resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", largeData + i);
            }
        }, "대용량 데이터 삽입으로 인한 오류가 발생할 수 있음");

        assertDoesNotThrow(() -> {
            resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Small data after memory test");
        }, "메모리 오류 후에도 정상적인 작업이 가능해야 함");

        log.info("메모리 부족 상황 오류 처리 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("동시성 오류 상황 처리")
    void testConcurrencyErrorHandling() throws InterruptedException {
        resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", 1000, "Concurrency test");

        Thread thread1 = new Thread(() -> {
            try {
                resilientJdbc.execute("START TRANSACTION");
                resilientJdbc.update("UPDATE error_test SET data = ? WHERE id = ?", "Updated by thread 1", 1000);
                Thread.sleep(2000); // 2초 대기
                resilientJdbc.execute("COMMIT");
                log.info("Thread 1 committed successfully");
            } catch (Exception e) {
                log.warn("Thread 1 failed: {}", e.getMessage());
                try {
                    resilientJdbc.execute("ROLLBACK");
                } catch (Exception rollbackEx) {
                    log.error("Thread 1 rollback failed", rollbackEx);
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(500); // 0.5초 후 시작
                resilientJdbc.execute("START TRANSACTION");
                resilientJdbc.update("UPDATE error_test SET data = ? WHERE id = ?", "Updated by thread 2", 1000);
                resilientJdbc.execute("COMMIT");
                log.info("Thread 2 committed successfully");
            } catch (Exception e) {
                log.warn("Thread 2 failed: {}", e.getMessage());
                try {
                    resilientJdbc.execute("ROLLBACK");
                } catch (Exception rollbackEx) {
                    log.error("Thread 2 rollback failed", rollbackEx);
                }
            }
        });

        thread1.start();
        thread2.start();

        thread1.join(10000);
        thread2.join(10000);

        String finalData = resilientJdbc.queryForObject(
            "SELECT data FROM error_test WHERE id = ?", String.class, 1000);
        
        assertTrue(finalData.contains("Updated by thread"), "동시성 상황에서도 최종 데이터가 업데이트되어야 함");

        log.info("동시성 오류 상황 처리 테스트 완료: 최종 데이터 = {}", finalData);
    }

    @Test
    @Order(8)
    @DisplayName("연결 풀 고갈 상황에서의 복구")
    void testConnectionPoolExhaustionRecovery() throws InterruptedException {
        java.util.List<Connection> connections = new java.util.ArrayList<>();
        
        try {
            for (int i = 0; i < 12; i++) {
                try {
                    Connection conn = resilientDataSource.getConnection();
                    connections.add(conn);
                    log.info("Connection {} acquired", i + 1);
                } catch (SQLException e) {
                    log.info("Connection pool exhausted at connection {}: {}", i + 1, e.getMessage());
                    break;
                }
            }

            assertThrows(SQLException.class, () -> {
                resilientDataSource.getConnection();
            }, "연결 풀 고갈 시 새 연결이 실패해야 함");

        } finally {
            for (int i = 0; i < connections.size() / 2; i++) {
                try {
                    connections.get(i).close();
                } catch (SQLException e) {
                    log.warn("Connection close failed", e);
                }
            }
        }

        Thread.sleep(1000);
        
        assertDoesNotThrow(() -> {
            try (Connection conn = resilientDataSource.getConnection()) {
                assertNotNull(conn, "연결 풀 복구 후 새 연결을 얻을 수 있어야 함");
            }
        }, "연결 풀 복구 후 정상적인 연결이 가능해야 함");

        for (Connection conn : connections) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }

        log.info("연결 풀 고갈 및 복구 테스트 완료");
    }
}