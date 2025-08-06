package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Cross-Container Communication Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "communicationDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "communicationCache"),
    @EnableTestContainers.TestContainer(type = ContainerType.KAFKA, name = "communicationMessaging")
})
class CrossContainerCommunicationTest {
    
    @Autowired
    @Qualifier("communicationDb")
    private DataSource communicationDataSource;
    
    @Autowired
    @Qualifier("communicationCache")
    private RedisTemplate<String, Object> communicationRedisTemplate;
    
    @Autowired
    @Qualifier("communicationMessaging")
    private KafkaTemplate<String, Object> communicationKafkaTemplate;
    
    @Test
    @Order(1)
    @DisplayName("Database to Cache data synchronization")
    void testDatabaseToCacheSynchronization() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(communicationDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sync_test (" +
            "id INT PRIMARY KEY, " +
            "name VARCHAR(100), " +
            "value VARCHAR(200), " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")");
        
        String testId = "sync-test-1";
        String testName = "Database Entry";
        String testValue = "database-value-" + System.currentTimeMillis();
        
        jdbcTemplate.update("INSERT INTO sync_test (id, name, value) VALUES (1, ?, ?) " +
            "ON DUPLICATE KEY UPDATE name = VALUES(name), value = VALUES(value)",
            testName, testValue);
        
        Map<String, Object> dbData = jdbcTemplate.queryForMap("SELECT * FROM sync_test WHERE id = 1");
        
        String cacheKey = "sync:db:1";
        communicationRedisTemplate.opsForHash().putAll(cacheKey, dbData);
        
        Map<Object, Object> cachedData = communicationRedisTemplate.opsForHash().entries(cacheKey);
        
        assertFalse(cachedData.isEmpty(), "Cache should contain synchronized data");
        assertEquals(dbData.get("name"), cachedData.get("name"), "Name should be synchronized");
        assertEquals(dbData.get("value"), cachedData.get("value"), "Value should be synchronized");
        
        log.info("Database to Cache synchronization validated - DB: {}, Cache: {}", 
            dbData.size(), cachedData.size());
    }
    
    @Test
    @Order(2)
    @DisplayName("Cache-based distributed locking")
    void testCacheBasedDistributedLocking() throws InterruptedException {
        String lockKey = "distributed-lock";
        String lockValue = "test-lock-" + System.currentTimeMillis();
        
        Boolean lockAcquired = communicationRedisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));
        
        assertTrue(Boolean.TRUE.equals(lockAcquired), "Should acquire lock successfully");
        
        Boolean secondLockAttempt = communicationRedisTemplate.opsForValue()
            .setIfAbsent(lockKey, "another-value", Duration.ofSeconds(5));
        
        assertFalse(Boolean.TRUE.equals(secondLockAttempt), "Should not acquire lock when already held");
        
        Object currentLockValue = communicationRedisTemplate.opsForValue().get(lockKey);
        assertEquals(lockValue, currentLockValue, "Lock value should be preserved");
        
        communicationRedisTemplate.delete(lockKey);
        
        Boolean thirdLockAttempt = communicationRedisTemplate.opsForValue()
            .setIfAbsent(lockKey, "new-lock", Duration.ofSeconds(5));
        
        assertTrue(Boolean.TRUE.equals(thirdLockAttempt), "Should acquire lock after release");
        
        log.info("Cache-based distributed locking validated");
    }
    
    @Test
    @Order(3)
    @DisplayName("Multi-container data pipeline")
    void testMultiContainerDataPipeline() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(communicationDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS pipeline_events (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "event_type VARCHAR(50), " +
            "event_data TEXT, " +
            "processed BOOLEAN DEFAULT FALSE, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
        
        String eventType = "USER_REGISTRATION";
        String eventData = "{\"userId\": 12345, \"email\": \"test@example.com\", \"timestamp\": " + 
            System.currentTimeMillis() + "}";
        
        jdbcTemplate.update("INSERT INTO pipeline_events (event_type, event_data) VALUES (?, ?)",
            eventType, eventData);
        
        List<Map<String, Object>> unprocessedEvents = jdbcTemplate.queryForList(
            "SELECT * FROM pipeline_events WHERE processed = FALSE");
        
        assertFalse(unprocessedEvents.isEmpty(), "Should have unprocessed events");
        
        for (Map<String, Object> event : unprocessedEvents) {
            Long eventId = ((Number) event.get("id")).longValue();
            String cacheKey = "pipeline:event:" + eventId;
            
            communicationRedisTemplate.opsForHash().putAll(cacheKey, event);
            communicationRedisTemplate.expire(cacheKey, Duration.ofMinutes(10));
            
            String kafkaTopic = "event-processing";
            String kafkaMessage = "Event ID: " + eventId + ", Type: " + event.get("event_type");
            
            try {
                communicationKafkaTemplate.send(kafkaTopic, kafkaMessage);
                
                jdbcTemplate.update("UPDATE pipeline_events SET processed = TRUE WHERE id = ?", eventId);
                
                log.info("Event {} processed through pipeline: DB -> Cache -> Kafka", eventId);
            } catch (Exception e) {
                log.warn("Kafka sending failed (expected in test environment): {}", e.getMessage());
            }
        }
        
        Integer processedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM pipeline_events WHERE processed = TRUE", Integer.class);
        
        assertTrue(processedCount > 0, "Events should be marked as processed");
        
        log.info("Multi-container data pipeline validated - {} events processed", processedCount);
    }
    
    @Test
    @Order(4)
    @DisplayName("Cross-container transaction coordination")
    void testCrossContainerTransactionCoordination() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(communicationDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transaction_log (" +
            "transaction_id VARCHAR(100) PRIMARY KEY, " +
            "status VARCHAR(20), " +
            "amount DECIMAL(10,2), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
        
        String transactionId = "tx-" + System.currentTimeMillis();
        Double amount = 100.50;
        
        try {
            jdbcTemplate.update("INSERT INTO transaction_log (transaction_id, status, amount) VALUES (?, ?, ?)",
                transactionId, "PENDING", amount);
            
            String cacheKey = "transaction:" + transactionId;
            Map<String, Object> transactionData = Map.of(
                "id", transactionId,
                "status", "PROCESSING",
                "amount", amount,
                "timestamp", System.currentTimeMillis()
            );
            
            communicationRedisTemplate.opsForHash().putAll(cacheKey, transactionData);
            
            Boolean lockAcquired = communicationRedisTemplate.opsForValue()
                .setIfAbsent("lock:" + transactionId, "processing", Duration.ofMinutes(5));
            
            if (Boolean.TRUE.equals(lockAcquired)) {
                try {
                    Thread.sleep(100);
                    
                    jdbcTemplate.update("UPDATE transaction_log SET status = ? WHERE transaction_id = ?",
                        "COMPLETED", transactionId);
                    
                    communicationRedisTemplate.opsForHash().put(cacheKey, "status", "COMPLETED");
                    
                    String kafkaMessage = String.format("Transaction %s completed with amount %.2f",
                        transactionId, amount);
                    
                    try {
                        communicationKafkaTemplate.send("transaction-events", kafkaMessage);
                    } catch (Exception e) {
                        log.debug("Kafka notification failed (expected): {}", e.getMessage());
                    }
                    
                } finally {
                    communicationRedisTemplate.delete("lock:" + transactionId);
                }
            }
            
            String finalStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM transaction_log WHERE transaction_id = ?",
                String.class, transactionId);
            
            Object cacheStatus = communicationRedisTemplate.opsForHash().get(cacheKey, "status");
            
            assertEquals("COMPLETED", finalStatus, "Database status should be COMPLETED");
            assertEquals("COMPLETED", cacheStatus, "Cache status should be COMPLETED");
            
        } catch (Exception e) {
            log.error("Transaction coordination failed: {}", e.getMessage());
            
            jdbcTemplate.update("UPDATE transaction_log SET status = ? WHERE transaction_id = ?",
                "FAILED", transactionId);
            
            fail("Transaction coordination should not fail: " + e.getMessage());
        }
        
        log.info("Cross-container transaction coordination validated for {}", transactionId);
    }
    
    @Test
    @Order(5)
    @DisplayName("Container failover and recovery simulation")
    void testContainerFailoverRecovery() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(communicationDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS failover_test (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "data VARCHAR(100), " +
            "backup_location VARCHAR(200)" +
            ")");
        
        String primaryData = "primary-data-" + System.currentTimeMillis();
        
        jdbcTemplate.update("INSERT INTO failover_test (data) VALUES (?)", primaryData);
        
        Integer dataId = jdbcTemplate.queryForObject(
            "SELECT id FROM failover_test WHERE data = ?", Integer.class, primaryData);
        
        String backupKey = "backup:failover:" + dataId;
        communicationRedisTemplate.opsForValue().set(backupKey, primaryData, Duration.ofHours(1));
        
        jdbcTemplate.update("UPDATE failover_test SET backup_location = ? WHERE id = ?",
            backupKey, dataId);
        
        Object cachedBackup = communicationRedisTemplate.opsForValue().get(backupKey);
        assertEquals(primaryData, cachedBackup, "Backup data should match primary data");
        
        String recoveredData = jdbcTemplate.queryForObject(
            "SELECT data FROM failover_test WHERE id = ?", String.class, dataId);
        
        assertEquals(primaryData, recoveredData,
            "Data should be recoverable from primary storage");
        
        assertTrue(communicationRedisTemplate.hasKey(backupKey),
            "Backup should exist in cache for failover scenarios");
        
        log.info("Container failover and recovery simulation validated for data ID: {}", dataId);
    }
    
    @Test
    @Order(6)
    @DisplayName("Performance under cross-container load")
    void testPerformanceUnderCrossContainerLoad() throws InterruptedException {
        int operationCount = 100;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        Instant startTime = Instant.now();
        
        CompletableFuture<?>[] futures = new CompletableFuture[operationCount];
        
        for (int i = 0; i < operationCount; i++) {
            final int operationId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(communicationDataSource);
                    
                    String data = "load-test-" + operationId + "-" + System.nanoTime();
                    
                    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS load_test (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "operation_id INT, " +
                        "data VARCHAR(200)" +
                        ")");
                    
                    jdbcTemplate.update("INSERT INTO load_test (operation_id, data) VALUES (?, ?)",
                        operationId, data);
                    
                    String cacheKey = "load:test:" + operationId;
                    communicationRedisTemplate.opsForValue().set(cacheKey, data, Duration.ofMinutes(5));
                    
                    try {
                        communicationKafkaTemplate.send("load-test-topic", "Operation " + operationId + " completed");
                    } catch (Exception e) {
                        log.debug("Kafka operation failed (expected): {}", e.getMessage());
                    }
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("Load test operation {} failed: {}", operationId, e.getMessage());
                }
            });
        }
        
        try {
            CompletableFuture.allOf(futures).get(120, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Load test failed", e);
        }
        
        Duration totalDuration = Duration.between(startTime, Instant.now());
        
        assertEquals(operationCount, successCount.get() + errorCount.get(),
            "All operations should be accounted for");
        assertTrue(successCount.get() > operationCount * 0.9,
            String.format("At least 90%% operations should succeed, got %.1f%%",
                (double) successCount.get() / operationCount * 100));
        
        double operationsPerSecond = (double) successCount.get() / totalDuration.toSeconds();
        assertTrue(operationsPerSecond > 1.0,
            String.format("Should achieve reasonable throughput, got %.2f ops/sec", operationsPerSecond));
        
        log.info("Performance test completed - {} operations in {}ms ({:.2f} ops/sec), {} errors",
            successCount.get(), totalDuration.toMillis(), operationsPerSecond, errorCount.get());
    }
    
    @Test
    @Order(7)
    @DisplayName("Data consistency across containers")
    void testDataConsistencyAcrossContainers() {
        Map<String, String> testData = new ConcurrentHashMap<>();
        testData.put("user:123", "John Doe");
        testData.put("user:456", "Jane Smith");
        testData.put("user:789", "Bob Johnson");
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(communicationDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS consistency_test (" +
            "user_key VARCHAR(50) PRIMARY KEY, " +
            "user_name VARCHAR(100), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
        
        testData.forEach((key, name) -> {
            jdbcTemplate.update("INSERT INTO consistency_test (user_key, user_name) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE user_name = VALUES(user_name)", key, name);
            
            communicationRedisTemplate.opsForValue().set("cache:" + key, name, Duration.ofHours(1));
        });
        
        testData.forEach((key, expectedName) -> {
            String dbName = jdbcTemplate.queryForObject(
                "SELECT user_name FROM consistency_test WHERE user_key = ?", String.class, key);
            
            Object cacheName = communicationRedisTemplate.opsForValue().get("cache:" + key);
            
            assertEquals(expectedName, dbName, "Database should have consistent data for " + key);
            assertEquals(expectedName, cacheName, "Cache should have consistent data for " + key);
            assertEquals(dbName, cacheName, "Database and cache should be consistent for " + key);
        });
        
        List<Map<String, Object>> allDbData = jdbcTemplate.queryForList(
            "SELECT user_key, user_name FROM consistency_test ORDER BY user_key");
        
        assertEquals(testData.size(), allDbData.size(),
            "Database should contain all test records");
        
        long cacheKeyCount = testData.keySet().stream()
            .mapToLong(key -> communicationRedisTemplate.hasKey("cache:" + key) ? 1 : 0)
            .sum();
        
        assertEquals(testData.size(), cacheKeyCount,
            "Cache should contain all test records");
        
        log.info("Data consistency validated across {} records in database and cache", testData.size());
    }
}