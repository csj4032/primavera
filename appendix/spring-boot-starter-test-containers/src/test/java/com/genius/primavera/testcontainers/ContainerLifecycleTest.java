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
@DisplayName("translated_text_4 translated_text_6 translated_text_2 test")
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

        log.info("translated_text_4 translated_text_6 test translated_text_2 translated_text_3 completed");
    }

    @Test
    @Order(1)
    @DisplayName("translated_text_4 translated_text_2 translated_text_2 translated_text_1 translated_text_2 translated_text_2 validation")
    void testContainerInitialState() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        assertNotNull(dbInfo, "DB translated_text_4 translated_text_12 translated_text_4 translated_text_1");
        assertNotNull(cacheInfo, "translated_text_2 translated_text_4 translated_text_12 translated_text_4 translated_text_1");

        assertTrue(dbInfo.container().isRunning(), "DB translated_text_4translated_text_1 execution translated_text_4 translated_text_1");
        assertTrue(cacheInfo.container().isRunning(), "translated_text_2 translated_text_4translated_text_1 execution translated_text_4 translated_text_1");

        assertTrue(dbInfo.container().isHealthy(), "DB translated_text_4translated_text_1 translated_text_4 translated_text_1");
        assertNotNull(dbInfo.container().getContainerId(), "translated_text_4 IDtranslated_text_1 translated_text_4 translated_text_1");
        assertTrue(dbInfo.container().getContainerId().length() > 10, "translated_text_4 IDtranslated_text_1 translated_text_4 translated_text_1");

        String dbHost = dbInfo.container().getHost();
        Integer dbPort = dbInfo.container().getFirstMappedPort();
        
        assertNotNull(dbHost, "DB translated_text_3translated_text_1 translated_text_2 translated_text_1");
        assertNotNull(dbPort, "DB translated_text_1 translated_text_5 translated_text_1");
        assertTrue(dbPort > 0, "DB translated_text_1 translated_text_4 translated_text_1");

        log.info("translated_text_4 translated_text_2 translated_text_2 validation completed - DB: {}:{}, Cache: {}:{}", 
            dbHost, dbPort, cacheInfo.container().getHost(), cacheInfo.container().getFirstMappedPort());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_7 translated_text_2 translated_text_1 translated_text_2 translated_text_2 validation")
    void testDatabaseConnectivityAndOperations() {
        assertDoesNotThrow(() -> {
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            assertNotNull(version, "translated_text_7 translated_text_2 translated_text_12 translated_text_1 translated_text_1 translated_text_3 translated_text_1");
            assertTrue(version.toLowerCase().contains("mariadb"), "MariaDB translated_text_2 translated_text_13 translated_text_1");
            log.info("translated_text_7 translated_text_2: {}", version);
        }, "translated_text_7 translated_text_2 successfully translated_text_4 translated_text_1");

        int insertResult = jdbcTemplate.update(
            "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
            "connectivity_test", "RUNNING");
        assertEquals(1, insertResult, "data translated_text_3 translated_text_9 translated_text_1");

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name = ?", 
            Integer.class, "connectivity_test");
        assertEquals(1, count, "translated_text_3 datatranslated_text_1 translated_text_8 translated_text_1 translated_text_3 translated_text_1");

        int updateResult = jdbcTemplate.update(
            "UPDATE lifecycle_test SET status = ? WHERE test_name = ?",
            "COMPLETED", "connectivity_test");
        assertEquals(1, updateResult, "data translated_text_1 translated_text_9 translated_text_1");

        String updatedStatus = jdbcTemplate.queryForObject(
            "SELECT status FROM lifecycle_test WHERE test_name = ?", 
            String.class, "connectivity_test");
        assertEquals("COMPLETED", updatedStatus, "datatranslated_text_1 translated_text_4 translated_text_1 translated_text_1");

        log.info("translated_text_7 translated_text_2 translated_text_2 validation completed");
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_4 translated_text_2 translated_text_1 translated_text_4 validation")
    void testContainerIsolationAndNetwork() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        String dbHost = dbInfo.container().getHost();
        Integer dbPort = dbInfo.container().getFirstMappedPort();
        String cacheHost = cacheInfo.container().getHost();
        Integer cachePort = cacheInfo.container().getFirstMappedPort();

        assertNotNull(dbHost, "DB translated_text_3translated_text_1 translated_text_2 translated_text_1");
        assertNotNull(dbPort, "DB translated_text_1 translated_text_2 translated_text_1");
        assertNotNull(cacheHost, "translated_text_2 translated_text_3translated_text_1 translated_text_2 translated_text_1");
        assertNotNull(cachePort, "translated_text_2 translated_text_1 translated_text_2 translated_text_1");

        assertNotEquals(dbPort, cachePort, "DBtranslated_text_1 translated_text_2 translated_text_2 translated_text_3 translated_text_4 translated_text_1");

        String dbContainerId = dbInfo.container().getContainerId();
        String cacheContainerId = cacheInfo.container().getContainerId();
        
        assertNotNull(dbContainerId, "DB translated_text_4 IDtranslated_text_1 translated_text_4 translated_text_1");
        assertNotNull(cacheContainerId, "translated_text_2 translated_text_4 IDtranslated_text_1 translated_text_4 translated_text_1");
        assertNotEquals(dbContainerId, cacheContainerId, "translated_text_4 IDtranslated_text_1 translated_text_3 translated_text_1");

        jdbcTemplate.update(
            "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
            "isolation_test_db", "ISOLATED");

        Integer dbTestCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'isolation_test_%'", 
            Integer.class);
        assertEquals(1, dbTestCount, "DB translated_text_4 translated_text_4 translated_text_2 translated_text_1 translated_text_1");

        log.info("translated_text_4 translated_text_2 validation completed - DB: {} ({}), Cache: {} ({})", 
            dbHost + ":" + dbPort, dbContainerId.substring(0, 12),
            cacheHost + ":" + cachePort, cacheContainerId.substring(0, 12));
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_4 translated_text_3 translated_text_2 translated_text_1 translated_text_2 validation")
    void testContainerHealthAndStatus() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = containerManager.getContainer("lifecycleCache");

        assertTrue(dbInfo.container().isHealthy(), "DB translated_text_4translated_text_1 translated_text_4 translated_text_1");
        assertTrue(cacheInfo.container().isHealthy(), "translated_text_2 translated_text_4translated_text_1 translated_text_4 translated_text_1");

        assertNotNull(dbInfo.container().getHost(), "DB translated_text_3translated_text_1 translated_text_2 translated_text_1");
        assertNotNull(dbInfo.container().getFirstMappedPort(), "DB translated_text_1 translated_text_5 translated_text_1");
        
        assertDoesNotThrow(() -> {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            assertEquals(1, result, "translated_text_3 translated_text_2 translated_text_1 translated_text_2 execution translated_text_1");
        }, "translated_text_7 translated_text_3 translated_text_2translated_text_1 translated_text_9 translated_text_1");

        log.info("translated_text_4 translated_text_3 translated_text_2 completed");
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_3 translated_text_1 translated_text_3 test")
    void testStabilityAndPersistence() throws InterruptedException {
        int iterations = 20;
        int successCount = 0;
        int errorCount = 0;

        log.info("translated_text_3 test translated_text_2 - {}translated_text_1 translated_text_2", iterations);

        for (int i = 0; i < iterations; i++) {
            try {
                jdbcTemplate.update(
                    "INSERT INTO lifecycle_test (test_name, status) VALUES (?, ?)",
                    "stability_test_" + i, "ITERATION_" + i);

                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'stability_test_%'", 
                    Integer.class);

                assertTrue(count > 0, "data translated_text_1translated_text_1 successfully translated_text_4 translated_text_1");

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
                log.warn("translated_text_2 {} translated_text_1 error translated_text_2: {}", i, e.getMessage());
                
                if (errorCount > iterations * 0.1) {
                    fail("error 10%translated_text_1 translated_text_6: " + errorCount + "/" + (i + 1));
                }
            }
        }

        assertTrue(successCount >= iterations * 0.9, "90% translated_text_3 translated_text_2 translated_text_9 translated_text_1");
        assertTrue(errorCount < iterations * 0.1, "error 10% translated_text_5 translated_text_1");

        Integer finalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM lifecycle_test WHERE test_name LIKE 'stability_test_%'", 
            Integer.class);
        assertTrue(finalCount >= successCount * 0.9, "translated_text_4 datatranslated_text_1 successfully translated_text_5 translated_text_1");

        log.info("translated_text_3 test completed - success: {}, failure: {}, translated_text_3 data: {}", 
            successCount, errorCount, finalCount);
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_3 translated_text_3 translated_text_1 translated_text_2 validation")
    void testResourceUsageAndCleanup() {
        ContainerInfo dbInfo = containerManager.getContainer("lifecycleDb");
        GenericContainer<?> container = dbInfo.container();

        assertNotNull(container.getContainerId(), "translated_text_4 IDtranslated_text_1 translated_text_4 translated_text_1");
        assertTrue(container.isRunning(), "translated_text_4translated_text_1 execution translated_text_4 translated_text_1");

        String containerId = container.getContainerId();
        assertTrue(containerId.length() > 10, "translated_text_3 translated_text_4 IDtranslated_text_2 translated_text_1");

        Integer mappedPort = container.getFirstMappedPort();
        assertNotNull(mappedPort, "translated_text_1 translated_text_5 translated_text_1");
        assertTrue(mappedPort > 1024, "translated_text_3 translated_text_1 translated_text_3 translated_text_2 translated_text_1");

        String host = container.getHost();
        assertTrue("localhost".equals(host) || "127.0.0.1".equals(host) || host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"), 
            "translated_text_3 translated_text_3 translated_text_2 translated_text_1");

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            }
        }, "translated_text_1 translated_text_2 successfully translated_text_4 translated_text_1");

        log.info("translated_text_3 translated_text_3 validation completed - Container: {}, Host: {}, Port: {}", 
            containerId.substring(0, 12), host, mappedPort);
    }
}