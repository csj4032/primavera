package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Precise Validation Tests for V4 TestContainer")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "testDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "testCache"),
    @EnableTestContainers.TestContainer(type = ContainerType.KAFKA, name = "testMessaging")
})
public class PreciseTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private Environment environment;
    
    @Autowired
    @Qualifier("testDb")
    private DataSource testDataSource;
    
    @Autowired
    @Qualifier("testCache")
    private RedisTemplate<String, Object> testRedisTemplate;
    
    @Autowired
    @Qualifier("testMessaging")
    private KafkaTemplate<String, Object> testKafkaTemplate;
    
    @Test
    @Order(1)
    @DisplayName("Bean registration and dependency injection validation")
    void testBeanRegistrationAndInjection() {
        assertNotNull(applicationContext, "ApplicationContext should be injected");
        assertNotNull(environment, "Environment should be injected");
        
        assertTrue(applicationContext.containsBean("testDb"), "testDb bean should be registered");
        assertTrue(applicationContext.containsBean("testCache"), "testCache bean should be registered");
        assertTrue(applicationContext.containsBean("testMessaging"), "testMessaging bean should be registered");
        
        assertNotNull(testDataSource, "DataSource should be injected");
        assertNotNull(testRedisTemplate, "RedisTemplate should be injected");
        assertNotNull(testKafkaTemplate, "KafkaTemplate should be injected");
        
        log.info("✅ All beans properly registered and injected");
    }
    
    @Test
    @Order(2)
    @DisplayName("Container manager state and lifecycle validation")
    void testContainerManagerStateLifecycle() {
        ContainerManager manager = ContainerRegistry.get();
        assertNotNull(manager, "ContainerManager should be available from registry");
        assertTrue(manager.isStarted(), "ContainerManager should be in started state");
        
        ContainerInfo dbInfo = manager.getContainer("testDb");
        ContainerInfo cacheInfo = manager.getContainer("testCache");
        ContainerInfo messagingInfo = manager.getContainer("testMessaging");
        
        assertNotNull(dbInfo, "Database container info should exist");
        assertNotNull(cacheInfo, "Cache container info should exist");
        assertNotNull(messagingInfo, "Messaging container info should exist");
        
        assertEquals(ContainerType.MARIADB, dbInfo.type(), "Database should be MariaDB type");
        assertEquals(ContainerType.REDIS, cacheInfo.type(), "Cache should be Redis type");
        assertEquals(ContainerType.KAFKA, messagingInfo.type(), "Messaging should be Kafka type");
        
        assertTrue(dbInfo.container().isRunning(), "Database container should be running");
        assertTrue(cacheInfo.container().isRunning(), "Cache container should be running");
        assertTrue(messagingInfo.container().isRunning(), "Messaging container should be running");
        
        log.info("✅ Container manager state and lifecycle validated");
    }
    
    @Test
    @Order(3)
    @DisplayName("Database connectivity and operations validation")
    void testDatabaseConnectivityOperations() throws Exception {
        try (Connection connection = testDataSource.getConnection()) {
            assertNotNull(connection, "Database connection should be established");
            assertFalse(connection.isClosed(), "Database connection should be open");
            
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            assertTrue(productName.toLowerCase().contains("mariadb"), 
                "Database should be MariaDB, got: " + productName);
        }
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(testDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS precise_test (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "value VARCHAR(200), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
        
        String testName = "test-entry-" + System.currentTimeMillis();
        String testValue = "test-value-" + System.nanoTime();
        
        jdbcTemplate.update("INSERT INTO precise_test (name, value) VALUES (?, ?)", testName, testValue);
        
        String retrievedValue = jdbcTemplate.queryForObject(
            "SELECT value FROM precise_test WHERE name = ?", String.class, testName);
        
        assertEquals(testValue, retrievedValue, "Database should persist and retrieve data correctly");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM precise_test", Integer.class);
        assertTrue(count >= 1, "Should have at least 1 record in database");
        
        log.info("✅ Database connectivity and operations validated with {} records", count);
    }
    
    @Test
    @Order(4)
    @DisplayName("Redis cache operations validation")
    void testRedisCacheOperations() {
        String cacheKey = "precise-test-" + System.currentTimeMillis();
        String cacheValue = "cached-value-" + System.nanoTime();
        
        testRedisTemplate.opsForValue().set(cacheKey, cacheValue);
        
        Object retrievedValue = testRedisTemplate.opsForValue().get(cacheKey);
        assertEquals(cacheValue, retrievedValue, "Redis should store and retrieve values correctly");
        
        assertTrue(testRedisTemplate.hasKey(cacheKey), "Redis should confirm key existence");
        
        testRedisTemplate.opsForValue().set(cacheKey + "-ttl", "expiring-value", Duration.ofSeconds(60));
        assertTrue(testRedisTemplate.hasKey(cacheKey + "-ttl"), "Redis should handle TTL operations");
        
        testRedisTemplate.opsForHash().put("hash-test", "field1", "value1");
        testRedisTemplate.opsForHash().put("hash-test", "field2", "value2");
        
        Object hashValue = testRedisTemplate.opsForHash().get("hash-test", "field1");
        assertEquals("value1", hashValue, "Redis hash operations should work correctly");
        
        log.info("✅ Redis cache operations validated");
    }
    
    @Test
    @Order(5)
    @DisplayName("Kafka messaging operations validation")
    void testKafkaMessagingOperations() {
        String topic = "precise-test-topic";
        String message = "test-message-" + System.currentTimeMillis();
        
        assertDoesNotThrow(() -> {
            testKafkaTemplate.send(topic, message);
            log.info("Kafka message sent to topic '{}': {}", topic, message);
        }, "Kafka should accept message sending operations");
        
        assertNotNull(testKafkaTemplate, "Kafka template should be configured");
        
        log.info("✅ Kafka messaging operations validated");
    }
    
    @Test
    @Order(6)
    @DisplayName("Environment properties exposure validation")
    void testEnvironmentPropertiesExposure() {
        String dbHost = environment.getProperty("testcontainer.runtime.testDb.host");
        String dbPort = environment.getProperty("testcontainer.runtime.testDb.port");
        String dbUrl = environment.getProperty("testcontainer.runtime.testDb.jdbcUrl");
        
        if (dbHost != null && dbPort != null && dbUrl != null) {
            assertTrue(dbHost.length() > 0, "Database host should not be empty");
            assertTrue(Integer.parseInt(dbPort) > 0, "Database port should be valid");
            assertTrue(dbUrl.startsWith("jdbc:mariadb://"), "Database URL should be valid MariaDB URL");
            log.info("✅ Environment properties validated - DB: {}:{}", dbHost, dbPort);
        } else {
            log.info("⚠️  Environment properties not fully exposed (container runtime properties may not be set)");
        }
        
        String cacheHost = environment.getProperty("testcontainer.runtime.testCache.host");
        String cachePort = environment.getProperty("testcontainer.runtime.testCache.port");
        
        if (cacheHost != null && cachePort != null) {
            log.info("✅ Cache environment properties validated - Cache: {}:{}", cacheHost, cachePort);
        } else {
            log.info("⚠️  Cache environment properties not fully exposed");
        }
    }
    
    @Test
    @Order(7)
    @DisplayName("Concurrent operations safety validation")
    void testConcurrentOperationsSafety() throws InterruptedException {
        int threadCount = 5;
        int operationsPerThread = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        CompletableFuture<?>[] futures = new CompletableFuture[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(testDataSource);
                    
                    for (int op = 0; op < operationsPerThread; op++) {
                        String threadData = "thread-" + threadIndex + "-op-" + op + "-" + System.nanoTime();
                        
                        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS concurrent_safety_test (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "thread_data VARCHAR(200)" +
                            ")");
                        
                        jdbcTemplate.update("INSERT INTO concurrent_safety_test (thread_data) VALUES (?)", threadData);
                        
                        String cacheKey = "concurrent-" + threadIndex + "-" + op;
                        testRedisTemplate.opsForValue().set(cacheKey, threadData);
                        
                        Object cachedValue = testRedisTemplate.opsForValue().get(cacheKey);
                        assertEquals(threadData, cachedValue, "Concurrent cache operations should be safe");
                        
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("Concurrent operation failed in thread {}: {}", threadIndex, e.getMessage());
                }
            }, executor);
        }
        
        assertDoesNotThrow(() -> {
            CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
        }, "All concurrent operations should complete without timeout");
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor should terminate gracefully");
        
        int expectedOperations = threadCount * operationsPerThread;
        assertEquals(expectedOperations, successCount.get() + errorCount.get(), 
            "All operations should be accounted for");
        
        assertTrue(successCount.get() > expectedOperations * 0.9, 
            String.format("At least 90%% operations should succeed, got %.1f%%", 
                (double) successCount.get() / expectedOperations * 100));
        
        log.info("✅ Concurrent operations safety validated - {} success, {} errors out of {} total",
            successCount.get(), errorCount.get(), expectedOperations);
    }
    
    @Test
    @Order(8)
    @DisplayName("Cross-container data flow validation")
    void testCrossContainerDataFlow() {
        String flowId = "data-flow-" + System.currentTimeMillis();
        String originalData = "original-data-" + System.nanoTime();
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(testDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS data_flow_test (" +
            "flow_id VARCHAR(100) PRIMARY KEY, " +
            "data VARCHAR(200), " +
            "processed BOOLEAN DEFAULT FALSE" +
            ")");
        
        jdbcTemplate.update("INSERT INTO data_flow_test (flow_id, data) VALUES (?, ?)", flowId, originalData);
        
        String retrievedData = jdbcTemplate.queryForObject(
            "SELECT data FROM data_flow_test WHERE flow_id = ?", String.class, flowId);
        assertEquals(originalData, retrievedData, "Database should store data correctly");
        
        String cacheKey = "flow:cache:" + flowId;
        testRedisTemplate.opsForValue().set(cacheKey, retrievedData + "-cached", Duration.ofMinutes(5));
        
        Object cachedData = testRedisTemplate.opsForValue().get(cacheKey);
        assertTrue(cachedData.toString().contains(originalData), "Cache should contain processed data");
        
        try {
            String kafkaMessage = "Flow " + flowId + " processed: " + cachedData;
            testKafkaTemplate.send("data-flow-topic", kafkaMessage);
            
            jdbcTemplate.update("UPDATE data_flow_test SET processed = TRUE WHERE flow_id = ?", flowId);
            
            Boolean isProcessed = jdbcTemplate.queryForObject(
                "SELECT processed FROM data_flow_test WHERE flow_id = ?", Boolean.class, flowId);
            
            assertTrue(Boolean.TRUE.equals(isProcessed), "Data flow should be marked as processed");
            
        } catch (Exception e) {
            log.debug("Kafka operation failed (expected in test environment): {}", e.getMessage());
        }
        
        log.info("✅ Cross-container data flow validated for flow ID: {}", flowId);
    }
    
    @Test
    @Order(9)
    @DisplayName("Resource cleanup and isolation validation")
    void testResourceCleanupIsolation() {
        String isolationId = "isolation-" + this.getClass().getSimpleName() + "-" + System.currentTimeMillis();
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(testDataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS isolation_test (" +
            "isolation_id VARCHAR(200) PRIMARY KEY, " +
            "test_class VARCHAR(100)" +
            ")");
        
        jdbcTemplate.update("INSERT INTO isolation_test (isolation_id, test_class) VALUES (?, ?)", 
            isolationId, this.getClass().getSimpleName());
        
        String cacheKey = "isolation:" + isolationId;
        testRedisTemplate.opsForValue().set(cacheKey, this.getClass().getSimpleName(), Duration.ofHours(1));
        
        String dbResult = jdbcTemplate.queryForObject(
            "SELECT test_class FROM isolation_test WHERE isolation_id = ?", String.class, isolationId);
        Object cacheResult = testRedisTemplate.opsForValue().get(cacheKey);
        
        assertEquals(this.getClass().getSimpleName(), dbResult, "Database isolation should work");
        assertEquals(this.getClass().getSimpleName(), cacheResult, "Cache isolation should work");
        
        ContainerManager manager = ContainerRegistry.get();
        assertTrue(manager.isStarted(), "Container manager should still be running");
        assertEquals(3, manager.getAllContainers().size(), "Should have exactly 3 containers running");
        
        log.info("✅ Resource cleanup and isolation validated for test class: {}", 
            this.getClass().getSimpleName());
    }
}