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
@DisplayName("error processing should test")
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

        log.info("error processing test connection completed");
    }

    @Test
    @Order(1)
    @DisplayName("connection SQL test error processing")
    void testInvalidSqlErrorHandling() {
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.execute("SELCT * FROM error_test");
        }, "connection SQL file successfully file should");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.queryForObject("SELECT COUNT(*) FROM non_existent_table", Integer.class);
        }, "file test connection test successfully file should");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", "invalid_id", "test data");
        }, "data test file successfully file should");

        Integer count = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertNotNull(count, "file test connection file should");
        assertTrue(count > 0, "test data file should");

        log.info("connection SQL test error processing test completed");
    }

    @Test
    @Order(2)
    @DisplayName("test test error processing")
    void testConstraintViolationErrorHandling() {
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", 1, "Duplicate key test");
        }, "PRIMARY KEY file successfully file should");

        resilientJdbc.execute("ALTER TABLE error_test MODIFY data VARCHAR(500) NOT NULL");
        
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (data) VALUES (NULL)");
        }, "NOT NULL test test successfully file should");

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
        }, "test should test test successfully file should");

        int result = resilientJdbc.update("INSERT INTO child_table (parent_id, data) VALUES (?, ?)", 1, "Valid FK");
        assertEquals(1, result, "file test should connection should not should");

        log.info("test test error processing test completed");
    }

    @Test
    @Order(3)
    @DisplayName("test file should test")
    void testConnectionTimeoutAndRecovery() throws InterruptedException {
        Integer initialCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertNotNull(initialCount, "test successfully file should");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.queryForObject("SELECT SLEEP(31)", Integer.class);
        }, "test file successfully file should");

        Thread.sleep(1000);

        assertDoesNotThrow(() -> {
            Integer count = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
            assertNotNull(count, "file connection should test file should");
        }, "file should test Endpoint should");

        log.info("test file should test completed");
    }

    @Test
    @Order(4) 
    @DisplayName("file test should error processing")
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
        }, "file should test test successfully file should");

        Integer afterCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeCount, afterCount, "file test data file connection should");

        resilientJdbc.execute("START TRANSACTION");
        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "After rollback test");
        resilientJdbc.execute("COMMIT");

        Integer finalCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeCount + 1, finalCount, "test should file file should");

        log.info("file test error processing test completed");
    }

    @Test
    @Order(5)
    @DisplayName("data test needs to be added test")
    void testDatabaseConnectionInterruptionAndRecovery() throws InterruptedException {
        ContainerInfo containerInfo = containerManager.getContainer("resilientDb");
        assertNotNull(containerInfo, "file operation file should");
        
        GenericContainer<?> container = containerInfo.container();
        assertTrue(container.isRunning(), "file execution needs to be added");

        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Before container stop");
        
        Integer beforeStopCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);

        log.info("file test should processing test");
        
        container.getDockerClient().pauseContainerCmd(container.getContainerId()).exec();

        assertThrows(Exception.class, () -> {
            resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        }, "file needs to be added test should not should");

        container.getDockerClient().unpauseContainerCmd(container.getContainerId()).exec();
        
        Thread.sleep(2000);

        log.info("file test should test connection");

        boolean recovered = false;
        for (int i = 0; i < 10; i++) {
            try {
                Integer afterResumeCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
                assertEquals(beforeStopCount, afterResumeCount, "data Endpoint should");
                recovered = true;
                break;
            } catch (Exception e) {
                log.warn("test {}/10 failure: {}", i + 1, e.getMessage());
                Thread.sleep(1000);
            }
        }

        assertTrue(recovered, "file test should test Endpoint should");

        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "After container resume");
        
        Integer finalCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeStopCount + 1, finalCount, "test needs to be added connection file should");

        log.info("data test needs to be added test test completed");
    }

    @Test
    @Order(6)
    @DisplayName("connection test Endpoint error processing")
    void testOutOfMemoryErrorHandling() {
        String largeData = "X".repeat(1000000);

        assertThrows(DataAccessException.class, () -> {
            for (int i = 0; i < 100; i++) {
                resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", largeData + i);
            }
        }, "connection data file test error connection should test");

        assertDoesNotThrow(() -> {
            resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Small data after memory test");
        }, "connection error connection file connection file should");

        log.info("connection test error processing test completed");
    }

    @Test
    @Order(7)
    @DisplayName("connection error test processing")
    void testConcurrencyErrorHandling() throws InterruptedException {
        resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", 1000, "Concurrency test");

        Thread thread1 = new Thread(() -> {
            try {
                resilientJdbc.execute("START TRANSACTION");
                resilientJdbc.update("UPDATE error_test SET data = ? WHERE id = ?", "Updated by thread 1", 1000);
                Thread.sleep(2000);
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
                Thread.sleep(500);
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
        
        assertTrue(finalData.contains("Updated by thread"), "connection test data logging should");

        log.info("connection error test processing test completed: test data = {}", finalData);
    }

    @Test
    @Order(8)
    @DisplayName("test should test processing test")
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
            }, "test should test needs to be added test should not should");

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
                assertNotNull(conn, "test should test needs to be added test should connection should");
            }
        }, "test should file test file should");

        for (Connection conn : connections) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }

        log.info("test should test completed");
    }
}