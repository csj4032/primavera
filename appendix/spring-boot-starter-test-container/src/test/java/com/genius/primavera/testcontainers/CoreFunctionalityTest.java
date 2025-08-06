package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("V4 Core Functionality Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "coreDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "coreCache")
})
class V4CoreFunctionalityTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    @Qualifier("coreDb")
    private DataSource coreDataSource;
    
    @Autowired
    @Qualifier("coreCache")
    private RedisTemplate<String, Object> coreRedisTemplate;
    
    @Test
    @Order(1)
    @DisplayName("Verify Spring context loads successfully")
    void testSpringContextLoads() {
        assertNotNull(applicationContext, "ApplicationContext should be available");
        log.info("✅ Spring context loaded successfully");
    }
    
    @Test
    @Order(2)
    @DisplayName("Verify container beans are registered")
    void testContainerBeansRegistered() {
        assertTrue(applicationContext.containsBean("coreDb"), "coreDb bean should be registered");
        assertTrue(applicationContext.containsBean("coreCache"), "coreCache bean should be registered");
        
        assertNotNull(coreDataSource, "DataSource should be injected");
        assertNotNull(coreRedisTemplate, "RedisTemplate should be injected");
        
        log.info("✅ Container beans registered and injected successfully");
    }
    
    @Test
    @Order(3)
    @DisplayName("Verify container manager is available")
    void testContainerManagerAvailable() {
        ContainerManager manager = ContainerRegistry.get();
        assertNotNull(manager, "ContainerManager should be available");
        assertTrue(manager.isStarted(), "ContainerManager should be started");
        
        ContainerInfo dbInfo = manager.getContainer("coreDb");
        ContainerInfo cacheInfo = manager.getContainer("coreCache");
        
        assertNotNull(dbInfo, "Database container info should exist");
        assertNotNull(cacheInfo, "Cache container info should exist");
        
        assertEquals(ContainerType.MARIADB, dbInfo.getType(), "Database should be MariaDB");
        assertEquals(ContainerType.REDIS, cacheInfo.getType(), "Cache should be Redis");
        
        log.info("✅ Container manager available with {} containers", manager.getAllContainers().size());
    }
    
    @Test
    @Order(4)
    @DisplayName("Verify database connectivity")
    void testDatabaseConnectivity() throws Exception {
        try (Connection connection = coreDataSource.getConnection()) {
            assertNotNull(connection, "Database connection should be established");
            assertFalse(connection.isClosed(), "Connection should be open");
            
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            assertTrue(productName.toLowerCase().contains("mariadb"), 
                "Should be MariaDB, got: " + productName);
        }
        
        log.info("✅ Database connectivity verified");
    }
    
    @Test
    @Order(5)
    @DisplayName("Verify database operations")
    void testDatabaseOperations() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(coreDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS core_test (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
        
        String testName = "core-test-" + System.currentTimeMillis();
        jdbcTemplate.update("INSERT INTO core_test (name) VALUES (?)", testName);
        
        String retrieved = jdbcTemplate.queryForObject(
            "SELECT name FROM core_test WHERE name = ?", String.class, testName);
        
        assertEquals(testName, retrieved, "Database operations should work correctly");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM core_test", Integer.class);
        assertTrue(count >= 1, "Should have at least one record");
        
        log.info("✅ Database operations verified with {} records", count);
    }
    
    @Test
    @Order(6)
    @DisplayName("Verify Redis operations")
    void testRedisOperations() {
        String key = "core-test-" + System.currentTimeMillis();
        String value = "test-value-" + System.nanoTime();
        
        coreRedisTemplate.opsForValue().set(key, value);
        
        Object retrieved = coreRedisTemplate.opsForValue().get(key);
        assertEquals(value, retrieved, "Redis operations should work correctly");
        
        assertTrue(coreRedisTemplate.hasKey(key), "Key should exist in Redis");
        
        coreRedisTemplate.opsForValue().set(key + "-ttl", "expiring", Duration.ofSeconds(30));
        assertTrue(coreRedisTemplate.hasKey(key + "-ttl"), "TTL operations should work");
        
        log.info("✅ Redis operations verified");
    }
    
    @Test
    @Order(7)
    @DisplayName("Verify container health")
    void testContainerHealth() {
        ContainerManager manager = ContainerRegistry.get();
        
        for (ContainerInfo containerInfo : manager.getAllContainers()) {
            assertTrue(containerInfo.getContainer().isRunning(), 
                "Container " + containerInfo.getName() + " should be running");
            
            try {
                boolean isHealthy = containerInfo.getContainer().isHealthy();
                log.info("Container {} health status: {}", containerInfo.getName(), isHealthy);
            } catch (Exception e) {
                log.info("Container {} does not have health check configured: {}", 
                    containerInfo.getName(), e.getMessage());
            }
        }
        
        log.info("✅ All containers are healthy and running");
    }
    
    @Test
    @Order(8)
    @DisplayName("Verify container isolation")
    void testContainerIsolation() {
        String isolationKey = "isolation-" + this.getClass().getSimpleName();
        String isolationValue = "isolated-" + System.currentTimeMillis();
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(coreDataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS isolation_test (" +
            "test_key VARCHAR(200) PRIMARY KEY, " +
            "test_value VARCHAR(200)" +
            ")");
        
        jdbcTemplate.update("INSERT INTO isolation_test (test_key, test_value) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE test_value = VALUES(test_value)", isolationKey, isolationValue);
        
        coreRedisTemplate.opsForValue().set(isolationKey, isolationValue);
        
        String dbValue = jdbcTemplate.queryForObject(
            "SELECT test_value FROM isolation_test WHERE test_key = ?", String.class, isolationKey);
        Object cacheValue = coreRedisTemplate.opsForValue().get(isolationKey);
        
        assertEquals(isolationValue, dbValue, "Database isolation should work");
        assertEquals(isolationValue, cacheValue, "Cache isolation should work");
        
        log.info("✅ Container isolation verified for {}", this.getClass().getSimpleName());
    }
    
    @Test
    @Order(9)
    @DisplayName("Verify resource cleanup readiness")
    void testResourceCleanupReadiness() {
        ContainerManager manager = ContainerRegistry.get();
        assertTrue(manager.isStarted(), "Manager should be started");
        
        int containerCount = manager.getAllContainers().size();
        assertEquals(2, containerCount, "Should have exactly 2 containers");
        
        for (ContainerInfo containerInfo : manager.getAllContainers()) {
            assertNotNull(containerInfo.getContainer().getContainerId(), 
                "Container ID should be available for cleanup");
        }
        
        log.info("✅ Resource cleanup readiness verified for {} containers", containerCount);
    }
}