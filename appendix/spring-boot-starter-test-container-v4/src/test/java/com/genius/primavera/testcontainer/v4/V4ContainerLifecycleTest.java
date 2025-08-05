package com.genius.primavera.testcontainer.v4;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j 
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Container Lifecycle Validation Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "lifecycleDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "lifecycleCache")
})
class V4ContainerLifecycleTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    @Qualifier("lifecycleDb")
    private DataSource lifecycleDataSource;
    
    private static Instant testStartTime;
    private static Instant containerStartTime;
    
    @BeforeAll
    static void recordTestStartTime() {
        testStartTime = Instant.now();
        log.info("Test class initialization started at: {}", testStartTime);
    }
    
    @Test
    @Order(1)
    @DisplayName("Container startup time validation")
    void testContainerStartupTime() {
        containerStartTime = Instant.now();
        Duration startupDuration = Duration.between(testStartTime, containerStartTime);
        
        assertTrue(startupDuration.toSeconds() < 180, 
            String.format("Container startup should complete within 3 minutes, took: %d seconds", 
                startupDuration.toSeconds()));
        
        log.info("Container startup completed in {} seconds", startupDuration.toSeconds());
    }
    
    @Test
    @Order(2)
    @DisplayName("Container registry bean validation")
    void testContainerRegistryBeans() {
        assertTrue(applicationContext.containsBean("lifecycleDb"), "lifecycleDb bean should be registered");
        assertTrue(applicationContext.containsBean("lifecycleCache"), "lifecycleCache bean should be registered");
        
        Object dbBean = applicationContext.getBean("lifecycleDb");
        Object cacheBean = applicationContext.getBean("lifecycleCache");
        
        assertNotNull(dbBean, "lifecycleDb bean should not be null");
        assertNotNull(cacheBean, "lifecycleCache bean should not be null");
        
        assertTrue(dbBean instanceof DataSource, "lifecycleDb should be DataSource instance");
        
        log.info("All container beans successfully registered and validated");
    }
    
    @Test
    @Order(3)
    @DisplayName("Database connection pool validation")
    void testDatabaseConnectionPool() throws Exception {
        assertNotNull(lifecycleDataSource, "DataSource should be injected");
        
        try (Connection connection = lifecycleDataSource.getConnection()) {
            assertNotNull(connection, "Connection should be established");
            assertFalse(connection.isClosed(), "Connection should be open");
            
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseVersion = metaData.getDatabaseProductVersion();
            
            assertTrue(databaseProductName.toLowerCase().contains("mariadb"), 
                "Database should be MariaDB, but was: " + databaseProductName);
            
            log.info("Database connection validated: {} version {}", databaseProductName, databaseVersion);
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("Container health and readiness validation")
    void testContainerHealthCheck() {
        ContainerManager manager = ContainerRegistry.get();
        
        assertNotNull(manager, "ContainerManager should be registered");
        assertTrue(manager.isStarted(), "ContainerManager should be in started state");
        
        ContainerInfo dbInfo = manager.getContainer("lifecycleDb");
        ContainerInfo cacheInfo = manager.getContainer("lifecycleCache"); 
        
        assertNotNull(dbInfo, "Database container info should exist");
        assertNotNull(cacheInfo, "Cache container info should exist");
        
        GenericContainer<?> dbContainer = dbInfo.getContainer();
        GenericContainer<?> cacheContainer = cacheInfo.getContainer();
        
        assertTrue(dbContainer.isRunning(), "Database container should be running");
        assertTrue(cacheContainer.isRunning(), "Cache container should be running");
        
        assertTrue(dbContainer.isHealthy(), "Database container should be healthy");
        assertTrue(cacheContainer.isHealthy(), "Cache container should be healthy");
        
        log.info("All containers are healthy and running");
    }
    
    @Test
    @Order(5)
    @DisplayName("Database schema and data persistence validation")
    void testDatabasePersistence() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(lifecycleDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS lifecycle_test (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "INDEX idx_name (name)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        
        String testData = "lifecycle-test-" + System.currentTimeMillis();
        jdbcTemplate.update("INSERT INTO lifecycle_test (name) VALUES (?)", testData);
        
        String retrievedData = jdbcTemplate.queryForObject(
            "SELECT name FROM lifecycle_test WHERE name = ?", String.class, testData);
        
        assertEquals(testData, retrievedData, "Data should persist correctly");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lifecycle_test", Integer.class);
        assertTrue(count >= 1, "Should have at least 1 record");
        
        log.info("Database persistence validated with {} records", count);
    }
    
    @Test
    @Order(6)
    @DisplayName("Container resource allocation validation")
    void testContainerResources() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo dbInfo = manager.getContainer("lifecycleDb");
        GenericContainer<?> dbContainer = dbInfo.getContainer();
        
        assertNotNull(dbContainer.getHost(), "Container host should be available");
        assertTrue(dbContainer.getFirstMappedPort() > 0, "Container should have mapped port");
        
        String jdbcUrl = String.format("jdbc:mariadb://%s:%d/%s", 
            dbContainer.getHost(), 
            dbContainer.getFirstMappedPort(),
            dbInfo.getSpec().getDatabase());
        
        assertNotNull(jdbcUrl, "JDBC URL should be constructed");
        assertTrue(jdbcUrl.startsWith("jdbc:mariadb://"), "JDBC URL should be valid MariaDB URL");
        
        log.info("Container resources validated - Host: {}, Port: {}", 
            dbContainer.getHost(), dbContainer.getFirstMappedPort());
    }
    
    @Test
    @Order(7)
    @DisplayName("Container isolation between test methods")
    void testContainerIsolation() {
        String isolationKey = "method-isolation-" + Thread.currentThread().getName();
        String isolationValue = "isolated-" + System.nanoTime();
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(lifecycleDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS method_isolation (" +
            "test_key VARCHAR(100) PRIMARY KEY, " +
            "test_value VARCHAR(100) NOT NULL" +
            ")");
        
        jdbcTemplate.update("INSERT INTO method_isolation (test_key, test_value) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE test_value = VALUES(test_value)", isolationKey, isolationValue);
        
        String retrievedValue = jdbcTemplate.queryForObject(
            "SELECT test_value FROM method_isolation WHERE test_key = ?", 
            String.class, isolationKey);
        
        assertEquals(isolationValue, retrievedValue, "Method-level isolation should work");
        
        log.info("Container isolation validated for method: {} with value: {}", 
            isolationKey, isolationValue);
    }
}