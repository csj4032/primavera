package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("file with test")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "lifecycleDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "lifecycleCache")
})
public class ContainerLifecycleTest {

    @Autowired
    @Qualifier("lifecycleDb")
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private ContainerManager containerManager;

    @BeforeAll
    void setupLifecycleTests() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        containerManager = ContainerRegistry.get();
        
        jdbcTemplate.execute("""
            CREATE TABLE lifecycle_test (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                test_name VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(50) DEFAULT 'ACTIVE'
            )
        """);

        log.info("file with test connection completed");
    }

    @Test
    @Order(1)
    @DisplayName("file test should test validation")
    void testContainerInitialState() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        assertNotNull(dbInfo, "DB file operation file should");
        assertNotNull(cacheInfo, "test file operation file should");

        assertTrue(dbInfo.container().isRunning(), "DB fileshould execution file should");
        assertTrue(cacheInfo.container().isRunning(), "test fileshould execution file should");

        assertTrue(dbInfo.container().isHealthy(), "DB fileshould file should");
        assertNotNull(dbInfo.container().getContainerId(), "file IDshould file should");
        assertTrue(dbInfo.container().getContainerId().length() > 10, "file IDshould file should");

        String dbHost = dbInfo.container().getHost();
        Integer dbPort = dbInfo.container().getFirstMappedPort();
        
        assertNotNull(dbHost, "DB connectiontest should");
        assertNotNull(dbPort, "DB should Endpoint should");
        assertTrue(dbPort > 0, "DB should file should");

        log.info("file test validation completed - DB: {}:{}, Cache: {}:{}", 
            dbHost, dbPort, cacheInfo.container().getHost(), cacheInfo.container().getFirstMappedPort());
    }

    @Test
    @Order(2)
    @DisplayName("logging test should test validation")
    void testDatabaseConnectivityAndOperations() {
        assertDoesNotThrow(() -> {
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            assertNotNull(version, "logging test operation needs to be added connection should");
            assertTrue(version.toLowerCase().contains("mariadb"), "MariaDB test created successfully should");
            log.info("logging test: {}", version);
        }, "logging test successfully file should");

        int insertResult = jdbcTemplate.update(
            "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
            "connectivity_test", "RUNNING");
        assertEquals(1, insertResult, "data connection should not should");

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name = ?", 
            Integer.class, "connectivity_test");
        assertEquals(1, count, "connection datashould configuration should connection should");

        int updateResult = jdbcTemplate.update(
            "UPDATE lifecycle_test SET status = ? WHERE test_name = ?",
            "COMPLETED", "connectivity_test");
        assertEquals(1, updateResult, "data should should not should");

        String updatedStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM lifecycle_test WHERE test_name = ?", 
            String.class, "connectivity_test");
        assertEquals("COMPLETED", updatedStatus, "datashould file needs to be added");

        log.info("logging test validation completed");
    }

    @Test
    @Order(3)
    @DisplayName("file test should file validation")
    void testContainerIsolationAndNetwork() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        String dbHost = dbInfo.container().getHost();
        Integer dbPort = dbInfo.container().getFirstMappedPort();
        String cacheHost = cacheInfo.container().getHost();
        Integer cachePort = cacheInfo.container().getFirstMappedPort();

        assertNotNull(dbHost, "DB connectiontest should");
        assertNotNull(dbPort, "DB test should");
        assertNotNull(cacheHost, "test connectiontest should");
        assertNotNull(cachePort, "test should");

        assertNotEquals(dbPort, cachePort, "DBshould test connection file should");

        String dbContainerId = dbInfo.container().getContainerId();
        String cacheContainerId = cacheInfo.container().getContainerId();
        
        assertNotNull(dbContainerId, "DB file IDshould file should");
        assertNotNull(cacheContainerId, "test file IDshould file should");
        assertNotEquals(dbContainerId, cacheContainerId, "file IDshould connection should");

        jdbcTemplate.update(
            "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
            "isolation_test_db", "ISOLATED");

        Integer dbTestCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'isolation_test_%'", 
            Integer.class);
        assertEquals(1, dbTestCount, "DB file test needs to be added");

        log.info("file test validation completed - DB: {} ({}), Cache: {} ({})", 
            dbHost + ":" + dbPort, dbContainerId.substring(0, 12),
            cacheHost + ":" + cachePort, cacheContainerId.substring(0, 12));
    }

    @Test
    @Order(4)
    @DisplayName("file connection test should test validation")
    void testContainerHealthAndStatus() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        assertTrue(dbInfo.container().isHealthy(), "DB fileshould file should");
        assertTrue(cacheInfo.container().isHealthy(), "test fileshould file should");

        assertNotNull(dbInfo.container().getHost(), "DB connectiontest should");
        assertNotNull(dbInfo.container().getFirstMappedPort(), "DB should Endpoint should");
        
        assertDoesNotThrow(() -> {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            assertEquals(1, result, "connection test should test execution should");
        }, "logging connection testshould should not should");

        log.info("file connection test completed");
    }

    @Test
    @Order(5)
    @DisplayName("connection should connection test")
    void testStabilityAndPersistence() throws InterruptedException {
        int iterations = 20;
        int successCount = 0;
        int errorCount = 0;

        log.info("connection test - {}should test", iterations);

        for (int i = 0; i < iterations; i++) {
            try {
                jdbcTemplate.update(
                    "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
                    "stability_test_" + i, "ITERATION_" + i);

                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'stability_test_%'", 
                    Integer.class);

                assertTrue(count > 0, "data shouldshould successfully file should");

                if (i % 5 == 0) {
                    jdbcTemplate.update(
                        "UPDATE lifecycle_test SET status = 'UPDATED' WHERE test_name = ?",
                        "stability_test_" + i);
                }

                successCount++;

                if (i % 5 == 0) {
                    Thread.sleep(50);
                }

            } catch (Exception e) {
                errorCount++;
                log.warn("test {} failed with error: {}", i, e.getMessage());
                
                if (errorCount > iterations * 0.1) {
                    fail("error 10%should return: " + errorCount + "/" + (i + 1));
                }
            }
        }

        assertTrue(successCount >= iterations * 0.9, "90% connection test should not should");
        assertTrue(errorCount < iterations * 0.1, "error 10% Endpoint should");

        Integer finalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'stability_test_%'", 
            Integer.class);
        assertTrue(finalCount >= successCount * 0.9, "file datashould successfully Endpoint should");

        log.info("connection test completed - success: {}, failure: {}, connection data: {}", 
            successCount, errorCount, finalCount);
    }

    @Test
    @Order(6)
    @DisplayName("connection should test validation")
    void testResourceUsageAndCleanup() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        GenericContainer<?> container = dbInfo.container();

        assertNotNull(container.getContainerId(), "file IDshould file should");
        assertTrue(container.isRunning(), "fileshould execution file should");

        String containerId = container.getContainerId();
        assertTrue(containerId.length() > 10, "connection file IDtest should");

        Integer mappedPort = container.getFirstMappedPort();
        assertNotNull(mappedPort, "should Endpoint should");
        assertTrue(mappedPort > 1024, "connection should connection test should");

        String host = container.getHost();
        assertTrue("localhost".equals(host) || "127.0.0.1".equals(host) || host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"), 
            "connection test should");

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            }
        }, "should test successfully file should");

        log.info("connection validation completed - Container: {}, Host: {}, Port: {}", 
            containerId.substring(0, 12), host, mappedPort);
    }
}