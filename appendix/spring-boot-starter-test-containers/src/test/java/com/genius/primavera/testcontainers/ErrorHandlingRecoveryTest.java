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
@DisplayName("error processing translated_text_1 translated_text_2 test")
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

        log.info("error processing test translated_text_2 translated_text_3 completed");
    }

    @Test
    @Order(1)
    @DisplayName("translated_text_3 SQL translated_text_2 error processing")
    void testInvalidSqlErrorHandling() {
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.execute("SELCT * FROM error_test");
        }, "translated_text_3 SQL translated_text_4 translated_text_10 translated_text_4 translated_text_1");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.queryForObject("SELECT COUNT(*) FROM non_existent_table", Integer.class);
        }, "translated_text_4 translated_text_2 translated_text_3 translated_text_2 translated_text_10 translated_text_4 translated_text_1");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", "invalid_id", "test data");
        }, "data translated_text_2 translated_text_4 translated_text_10 translated_text_4 translated_text_1");

        Integer count = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertNotNull(count, "translated_text_4 translated_text_2 translated_text_3 translated_text_4 translated_text_1");
        assertTrue(count > 0, "test data translated_text_4 translated_text_1");

        log.info("translated_text_3 SQL translated_text_2 error processing test completed");
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 error processing")
    void testConstraintViolationErrorHandling() {
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (id, data) VALUES (?, ?)", 1, "Duplicate key test");
        }, "PRIMARY KEY translated_text_4 translated_text_10 translated_text_4 translated_text_1");

        resilientJdbc.execute("ALTER TABLE error_test MODIFY data VARCHAR(500) NOT NULL");
        
        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.update("INSERT INTO error_test (data) VALUES (NULL)");
        }, "NOT NULL translated_text_2 translated_text_2 translated_text_2 translated_text_10 translated_text_4 translated_text_1");

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
        }, "translated_text_2 translated_text_1 translated_text_2 translated_text_2 translated_text_2 translated_text_10 translated_text_4 translated_text_1");

        int result = resilientJdbc.update("INSERT INTO child_table (parent_id, data) VALUES (?, ?)", 1, "Valid FK");
        assertEquals(1, result, "translated_text_4 translated_text_2 translated_text_1 translated_text_3 translated_text_9 translated_text_1");

        log.info("translated_text_2 translated_text_2 translated_text_2 error processing test completed");
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 translated_text_4 translated_text_1 translated_text_2 test")
    void testConnectionTimeoutAndRecovery() throws InterruptedException {
        Integer initialCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertNotNull(initialCount, "translated_text_2 translated_text_2 successfully translated_text_4 translated_text_1");

        assertThrows(DataAccessException.class, () -> {
            resilientJdbc.queryForObject("SELECT SLEEP(31)", Integer.class);
        }, "translated_text_2 translated_text_4 translated_text_10 translated_text_4 translated_text_1");

        Thread.sleep(1000);

        assertDoesNotThrow(() -> {
            Integer count = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
            assertNotNull(count, "translated_text_4 translated_text_3 translated_text_1 translated_text_2 translated_text_2 translated_text_4 translated_text_1");
        }, "translated_text_4 translated_text_1 translated_text_2 translated_text_2 translated_text_5 translated_text_1");

        log.info("translated_text_2 translated_text_4 translated_text_1 translated_text_2 test completed");
    }

    @Test
    @Order(4) 
    @DisplayName("translated_text_4 translated_text_2 translated_text_1 error processing")
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
        }, "translated_text_4 translated_text_1 translated_text_2 translated_text_2 translated_text_2 translated_text_10 translated_text_4 translated_text_1");

        Integer afterCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeCount, afterCount, "translated_text_4 translated_text_2 data translated_text_4 translated_text_3 translated_text_1");

        resilientJdbc.execute("START TRANSACTION");
        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "After rollback test");
        resilientJdbc.execute("COMMIT");

        Integer finalCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeCount + 1, finalCount, "translated_text_2 translated_text_1 translated_text_4 translated_text_4 translated_text_4 translated_text_1");

        log.info("translated_text_4 translated_text_2 error processing test completed");
    }

    @Test
    @Order(5)
    @DisplayName("data translated_text_2 translated_text_1 translated_text_1 translated_text_2 translated_text_2")
    void testDatabaseConnectionInterruptionAndRecovery() throws InterruptedException {
        ContainerInfo containerInfo = containerManager.getContainer("resilientDb");
        assertNotNull(containerInfo, "translated_text_4 translated_text_12 translated_text_4 translated_text_1");
        
        GenericContainer<?> container = containerInfo.container();
        assertTrue(container.isRunning(), "translated_text_4 execution translated_text_1 translated_text_1");

        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Before container stop");
        
        Integer beforeStopCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);

        log.info("translated_text_4 translated_text_2 translated_text_1 translated_text_5 translated_text_2");
        
        container.getDockerClient().pauseContainerCmd(container.getContainerId()).exec();

        assertThrows(Exception.class, () -> {
            resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        }, "translated_text_4 translated_text_1 translated_text_1 translated_text_2 translated_text_9 translated_text_1");

        container.getDockerClient().unpauseContainerCmd(container.getContainerId()).exec();
        
        Thread.sleep(2000);

        log.info("translated_text_4 translated_text_2 translated_text_1 translated_text_2 translated_text_3");

        boolean recovered = false;
        for (int i = 0; i < 10; i++) {
            try {
                Integer afterResumeCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
                assertEquals(beforeStopCount, afterResumeCount, "data translated_text_5 translated_text_1");
                recovered = true;
                break;
            } catch (Exception e) {
                log.warn("translated_text_2 translated_text_2 {}/10 failure: {}", i + 1, e.getMessage());
                Thread.sleep(1000);
            }
        }

        assertTrue(recovered, "translated_text_4 translated_text_2 translated_text_1 translated_text_2 translated_text_5 translated_text_1");

        resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "After container resume");
        
        Integer finalCount = resilientJdbc.queryForObject("SELECT COUNT(*) FROM error_test", Integer.class);
        assertEquals(beforeStopCount + 1, finalCount, "translated_text_2 translated_text_1 translated_text_1 translated_text_3 translated_text_4 translated_text_1");

        log.info("data translated_text_2 translated_text_1 translated_text_1 translated_text_2 translated_text_2 test completed");
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_3 translated_text_2 translated_text_5 error processing")
    void testOutOfMemoryErrorHandling() {
        String largeData = "X".repeat(1000000);

        assertThrows(DataAccessException.class, () -> {
            for (int i = 0; i < 100; i++) {
                resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", largeData + i);
            }
        }, "translated_text_3 data translated_text_4 translated_text_2 error translated_text_3 translated_text_1 translated_text_2");

        assertDoesNotThrow(() -> {
            resilientJdbc.update("INSERT INTO error_test (data) VALUES (?)", "Small data after memory test");
        }, "translated_text_3 error translated_text_3 translated_text_4 translated_text_3 translated_text_4 translated_text_1");

        log.info("translated_text_3 translated_text_2 translated_text_2 error processing test completed");
    }

    @Test
    @Order(7)
    @DisplayName("translated_text_3 error translated_text_2 processing")
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
        
        assertTrue(finalData.contains("Updated by thread"), "translated_text_3 translated_text_2 translated_text_2 data translated_text_7 translated_text_1");

        log.info("translated_text_3 error translated_text_2 processing test completed: translated_text_2 data = {}", finalData);
    }

    @Test
    @Order(8)
    @DisplayName("translated_text_2 translated_text_1 translated_text_2 translated_text_5 translated_text_2")
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
            }, "translated_text_2 translated_text_1 translated_text_2 translated_text_1 translated_text_1 translated_text_2 translated_text_9 translated_text_1");

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
                assertNotNull(conn, "translated_text_2 translated_text_1 translated_text_2 translated_text_1 translated_text_1 translated_text_2 translated_text_2 translated_text_1 translated_text_3 translated_text_1");
            }
        }, "translated_text_2 translated_text_1 translated_text_2 translated_text_1 translated_text_4 translated_text_2 translated_text_4 translated_text_1");

        for (Connection conn : connections) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }

        log.info("translated_text_2 translated_text_1 translated_text_2 translated_text_1 translated_text_2 test completed");
    }
}