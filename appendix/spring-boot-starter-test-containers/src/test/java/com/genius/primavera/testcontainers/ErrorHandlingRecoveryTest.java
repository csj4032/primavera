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

/**
 * 오류 처리 및 복구 테스트
 * - 연결 실패 처리
 * - 컨테이너 중지/재시작 시나리오
 * - 네트워크 장애 시뮬레이션
 * - 자동 복구 메커니즘
 * - 예외 상황 처리
 */
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

        // 테스트용 테이블 생성
        resilientJdbc.execute("""
            CREATE TABLE error_test (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                data VARCHAR(500),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // 초기 데이터 삽입
        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Initial test data");

        log.info("오류 처리 테스트 환경 초기화 완료");
    }

    @Test
    @Order(1)
    @DisplayName("잘못된 SQL 쿼리 오류 처리")
    void testInvalidSqlErrorHandling() {
        // 구문 오류가 있는 쿼리
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.execute("SELCT * FROM error_test"); // 의도적인 오타
        }, "잘못된 SQL 구문으로 예외가 발생해야 함");

        // 존재하지 않는 테이블 쿼리
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.queryForObject("SELECT COUNT(*) FROM non_existent_table", Integer.class);
        }, "존재하지 않는 테이블 쿼리로 예외가 발생해야 함");

        // 컬럼 타입 불일치
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", "invalid_id", "test data");
        }, "데이터 타입 불일치로 예외가 발생해야 함");

        // 연결이 여전히 정상적으로 작동하는지 확인
        Integer count = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertNotNull(count, "정상적인 쿼리는 여전히 작동해야 함");
        assertTrue(count > 0, "테스트 데이터가 존재해야 함");

        log.info("잘못된 SQL 쿼리 오류 처리 테스트 완료");
    }

    @Test
    @Order(2)
    @DisplayName("제약 조건 위반 오류 처리")
    void testConstraintViolationErrorHandling() {
        // PRIMARY KEY 중복 오류
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", 1, "Duplicate key test");
        }, "PRIMARY KEY 중복으로 예외가 발생해야 함");

        // NULL 제약 조건 위반 (data 컬럼에 NULL 삽입 시도)
        resilientJdbc.execute("ALTER TABLE error_test MODIFY data VARCHAR(500) NOT NULL");
        
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (data) VALUES (NULL)");
        }, "NOT NULL 제약 조건 위반으로 예외가 발생해야 함");

        // 외래 키 제약 조건 테스트
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

        // 존재하지 않는 부모 키로 삽입 시도
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO child_table (parent_id, data) VALUES (?, ?)", 999, "Invalid FK");
        }, "외래 키 제약 조건 위반으로 예외가 발생해야 함");

        // 정상적인 삽입은 성공해야 함
        int result = resilientJdbc.update("INSERT INTO child_table (parent_id, data) VALUES (?, ?)", 1, "Valid FK");
        assertEquals(1, result, "정상적인 외래 키로 삽입이 성공해야 함");

        log.info("제약 조건 위반 오류 처리 테스트 완료");
    }

    @Test
    @Order(3)
    @DisplayName("연결 타임아웃 및 복구 테스트")
    void testConnectionTimeoutAndRecovery() throws InterruptedException {
        // 현재 연결이 정상적으로 작동하는지 확인
        Integer initialCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertNotNull(initialCount, "초기 연결이 정상적으로 작동해야 함");

        // 긴 시간이 소요되는 쿼리로 타임아웃 시뮬레이션
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.queryForObject("SELECT SLEEP(31)", Integer.class); // 30초 이상 대기
        }, "쿼리 타임아웃으로 예외가 발생해야 함");

        // 짧은 대기 후 연결 복구 확인
        Thread.sleep(1000);

        // 새로운 연결로 정상 작업이 가능한지 확인
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

        // 트랜잭션 중간에 실패하는 시나리오
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.execute("START TRANSACTION");
            try {
                // 정상 삽입
                resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Transaction test 1");
                
                // 제약 조건 위반으로 실패
                resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", 1, "Duplicate PK");
                
                resilientJdbc.execute("COMMIT");
            } catch (Exception e) {
                resilientJdbc.execute("ROLLBACK");
                throw e;
            }
        }, "트랜잭션 중 제약 조건 위반으로 예외가 발생해야 함");

        // 롤백으로 인해 데이터가 변경되지 않았는지 확인
        Integer afterCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeCount, afterCount, "트랜잭션 롤백으로 데이터가 변경되지 않아야 함");

        // 이후 정상적인 트랜잭션이 작동하는지 확인
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
        // 현재 컨테이너 정보 확인
        ContainerInfo containerInfo = containerManager.getContainer("resilientDb");
        assertNotNull(containerInfo, "컨테이너 정보가 존재해야 함");
        
        GenericContainer<?> container = containerInfo.getContainer();
        assertTrue(container.isRunning(), "컨테이너가 실행 중이어야 함");

        // 정상적인 작업 수행
        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Before container stop");
        
        Integer beforeStopCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);

        log.info("컨테이너 일시 중지 시뮬레이션 시작");
        
        // 컨테이너 일시 중지
        container.getDockerClient().pauseContainerCmd(container.getContainerId()).exec();

        // 연결이 실패하는지 확인
        assertThrows(Exception.class, () -> {
            resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        }, "컨테이너 중지 중에는 연결이 실패해야 함");

        // 컨테이너 재개
        container.getDockerClient().unpauseContainerCmd(container.getContainerId()).exec();
        
        // 복구 대기
        Thread.sleep(2000);

        log.info("컨테이너 복구 후 연결 재시도");

        // 연결 복구 확인 (여러 번 재시도)
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

        // 복구 후 새로운 작업이 가능한지 확인
        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "After container resume");
        
        Integer finalCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeStopCount + 1, finalCount, "복구 후 새로운 작업이 가능해야 함");

        log.info("데이터베이스 연결 중단 및 자동 복구 테스트 완료");
    }

    @Test
    @Order(6)
    @DisplayName("메모리 부족 상황에서의 오류 처리")
    void testOutOfMemoryErrorHandling() {
        // 대용량 데이터 처리로 메모리 부족 상황 시뮬레이션
        String largeData = "X".repeat(1000000); // 1MB 문자열

        assertThrows(DataAccessException.class, () -> {
            for (int i = 0; i < 100; i++) {
                resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", largeData + i);
            }
        }, "대용량 데이터 삽입으로 인한 오류가 발생할 수 있음");

        // 작은 데이터로 정상 작업이 가능한지 확인
        assertDoesNotThrow(() -> {
            resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Small data after memory test");
        }, "메모리 오류 후에도 정상적인 작업이 가능해야 함");

        log.info("메모리 부족 상황 오류 처리 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("동시성 오류 상황 처리")
    void testConcurrencyErrorHandling() throws InterruptedException {
        // 동시에 같은 레코드를 수정하려는 상황 시뮬레이션
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

        // 최종 상태 확인 (어떤 스레드든 성공했다면 데이터가 업데이트되어야 함)
        String finalData = resilientJdbc.queryForObject(
            "SELECT data FROM error_test WHERE id = ?", String.class, 1000);
        
        assertTrue(finalData.contains("Updated by thread"), "동시성 상황에서도 최종 데이터가 업데이트되어야 함");

        log.info("동시성 오류 상황 처리 테스트 완료: 최종 데이터 = {}", finalData);
    }

    @Test
    @Order(8)
    @DisplayName("연결 풀 고갈 상황에서의 복구")
    void testConnectionPoolExhaustionRecovery() throws InterruptedException {
        // 연결 풀의 모든 연결을 점유
        java.util.List<Connection> connections = new java.util.ArrayList<>();
        
        try {
            // 최대 연결 수까지 연결 생성 (HikariCP 기본값은 10)
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

            // 새로운 연결 시도가 실패하는지 확인
            assertThrows(SQLException.class, () -> {
                resilientDataSource.getConnection();
            }, "연결 풀 고갈 시 새 연결이 실패해야 함");

        } finally {
            // 절반의 연결 해제
            for (int i = 0; i < connections.size() / 2; i++) {
                try {
                    connections.get(i).close();
                } catch (SQLException e) {
                    log.warn("Connection close failed", e);
                }
            }
        }

        // 연결이 복구되었는지 확인
        Thread.sleep(1000);
        
        assertDoesNotThrow(() -> {
            try (Connection conn = resilientDataSource.getConnection()) {
                assertNotNull(conn, "연결 풀 복구 후 새 연결을 얻을 수 있어야 함");
            }
        }, "연결 풀 복구 후 정상적인 연결이 가능해야 함");

        // 남은 연결들 정리
        for (Connection conn : connections) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                // 무시
            }
        }

        log.info("연결 풀 고갈 및 복구 테스트 완료");
    }
}