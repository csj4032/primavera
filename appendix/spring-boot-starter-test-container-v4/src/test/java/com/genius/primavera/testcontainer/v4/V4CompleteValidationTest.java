package com.genius.primavera.testcontainer.v4;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("V4 Complete Validation Test - Maximum Coverage")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "completeDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "completeCache")
})
class V4CompleteValidationTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private Environment environment;
    
    @Autowired
    @Qualifier("completeDb")
    private DataSource completeDataSource;
    
    @Autowired
    @Qualifier("completeCache")
    private RedisTemplate<String, Object> completeRedisTemplate;
    
    @Test
    @Order(1)
    @DisplayName("Validate complete container lifecycle and state management")
    void testCompleteContainerLifecycleAndState() {
        ContainerManager manager = ContainerRegistry.get();
        assertNotNull(manager, "ContainerManager should be available");
        assertTrue(manager.isStarted(), "ContainerManager should be started");
        
        assertEquals(2, manager.getAllContainers().size(), "Should have exactly 2 containers");
        
        ContainerInfo dbInfo = manager.getContainer("completeDb");
        ContainerInfo cacheInfo = manager.getContainer("completeCache");
        
        assertNotNull(dbInfo, "Database container info should exist");
        assertNotNull(cacheInfo, "Cache container info should exist");
        
        assertEquals("completeDb", dbInfo.getName(), "Database container name should match");
        assertEquals("completeCache", cacheInfo.getName(), "Cache container name should match");
        
        assertEquals(ContainerType.MARIADB, dbInfo.getType(), "Database type should be MariaDB");
        assertEquals(ContainerType.REDIS, cacheInfo.getType(), "Cache type should be Redis");
        
        assertNotNull(dbInfo.getContainer(), "Database container instance should exist");
        assertNotNull(cacheInfo.getContainer(), "Cache container instance should exist");
        
        assertTrue(dbInfo.getContainer().isRunning(), "Database container should be running");
        assertTrue(cacheInfo.getContainer().isRunning(), "Cache container should be running");
        
        assertNotNull(dbInfo.getSpec(), "Database container spec should exist");
        assertNotNull(cacheInfo.getSpec(), "Cache container spec should exist");
        
        log.info("✅ Complete container lifecycle and state management validated");
    }
    
    @Test
    @Order(2)
    @DisplayName("Validate comprehensive database operations")
    void testComprehensiveDatabaseOperations() throws Exception {
        try (Connection connection = completeDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.execute("CREATE TABLE IF NOT EXISTS comprehensive_test (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "text_field VARCHAR(500), " +
                "number_field DECIMAL(10,2), " +
                "date_field TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "boolean_field BOOLEAN DEFAULT FALSE, " +
                "INDEX idx_text (text_field), " +
                "INDEX idx_number (number_field)" +
                ")");
            
            statement.execute("INSERT INTO comprehensive_test (text_field, number_field, boolean_field) VALUES " +
                "('Test Data 1', 123.45, TRUE), " +
                "('Test Data 2', 678.90, FALSE), " +
                "('Test Data 3', 999.99, TRUE)");
            
            try (ResultSet resultSet = statement.executeQuery(
                "SELECT COUNT(*) as count, SUM(number_field) as total, AVG(number_field) as average " +
                "FROM comprehensive_test WHERE boolean_field = TRUE")) {
                
                assertTrue(resultSet.next(), "Query should return results");
                assertEquals(2, resultSet.getInt("count"), "Should have 2 TRUE boolean records");
                assertTrue(resultSet.getDouble("total") > 1000, "Total should be greater than 1000");
                assertTrue(resultSet.getDouble("average") > 500, "Average should be greater than 500");
            }
            
            statement.execute("CREATE TABLE IF NOT EXISTS transaction_test (" +
                "id INT PRIMARY KEY, " +
                "balance DECIMAL(10,2)" +
                ")");
            
            statement.execute("INSERT INTO transaction_test (id, balance) VALUES (1, 1000.00)");
            
            connection.setAutoCommit(false);
            try {
                statement.executeUpdate("UPDATE transaction_test SET balance = balance - 100 WHERE id = 1");
                statement.executeUpdate("INSERT INTO transaction_test (id, balance) VALUES (2, 100.00)");
                connection.commit();
                
                try (ResultSet balanceCheck = statement.executeQuery("SELECT SUM(balance) as total FROM transaction_test")) {
                    assertTrue(balanceCheck.next(), "Balance check should return result");
                    assertEquals(1000.00, balanceCheck.getDouble("total"), 0.01, "Total balance should remain 1000");
                }
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        
        log.info("✅ Comprehensive database operations validated");
    }
    
    @Test
    @Order(3)
    @DisplayName("Validate advanced Redis operations")
    void testAdvancedRedisOperations() {
        String baseKey = "complete-test-";
        
        completeRedisTemplate.opsForValue().set(baseKey + "string", "test-value", Duration.ofMinutes(10));
        completeRedisTemplate.opsForValue().increment(baseKey + "counter");
        completeRedisTemplate.opsForValue().increment(baseKey + "counter");
        
        completeRedisTemplate.opsForList().rightPush(baseKey + "list", "item1");
        completeRedisTemplate.opsForList().rightPush(baseKey + "list", "item2");
        completeRedisTemplate.opsForList().rightPush(baseKey + "list", "item3");
        
        completeRedisTemplate.opsForSet().add(baseKey + "set", "member1", "member2", "member3");
        
        completeRedisTemplate.opsForHash().put(baseKey + "hash", "field1", "value1");
        completeRedisTemplate.opsForHash().put(baseKey + "hash", "field2", "value2");
        
        completeRedisTemplate.opsForZSet().add(baseKey + "zset", "score1", 1.0);
        completeRedisTemplate.opsForZSet().add(baseKey + "zset", "score2", 2.0);
        completeRedisTemplate.opsForZSet().add(baseKey + "zset", "score3", 3.0);
        
        assertEquals("test-value", completeRedisTemplate.opsForValue().get(baseKey + "string"));
        assertEquals("2", completeRedisTemplate.opsForValue().get(baseKey + "counter").toString());
        assertEquals(3L, completeRedisTemplate.opsForList().size(baseKey + "list").longValue());
        assertEquals(3L, completeRedisTemplate.opsForSet().size(baseKey + "set").longValue());
        assertEquals(2L, completeRedisTemplate.opsForHash().size(baseKey + "hash").longValue());
        assertEquals(3L, completeRedisTemplate.opsForZSet().size(baseKey + "zset").longValue());
        
        assertEquals("item1", completeRedisTemplate.opsForList().index(baseKey + "list", 0));
        assertTrue(completeRedisTemplate.opsForSet().isMember(baseKey + "set", "member1"));
        assertEquals("value1", completeRedisTemplate.opsForHash().get(baseKey + "hash", "field1"));
        assertEquals(2.0, completeRedisTemplate.opsForZSet().score(baseKey + "zset", "score2"));
        
        log.info("✅ Advanced Redis operations validated");
    }
    
    @Test
    @Order(4)
    @DisplayName("Validate environment properties and runtime configuration")
    void testEnvironmentPropertiesAndRuntimeConfiguration() {
        String dbHost = environment.getProperty("testcontainer.runtime.completeDb.host");
        String dbPort = environment.getProperty("testcontainer.runtime.completeDb.port");
        String dbUrl = environment.getProperty("testcontainer.runtime.completeDb.jdbcUrl");
        
        String cacheHost = environment.getProperty("testcontainer.runtime.completeCache.host");
        String cachePort = environment.getProperty("testcontainer.runtime.completeCache.port");
        
        if (dbHost != null && dbPort != null && dbUrl != null) {
            assertTrue(dbHost.length() > 0, "Database host should not be empty");
            assertTrue(Integer.parseInt(dbPort) > 0, "Database port should be valid");
            assertTrue(dbUrl.contains("jdbc:mariadb://"), "JDBC URL should be MariaDB URL");
            assertTrue(dbUrl.contains(dbHost), "JDBC URL should contain host");
            assertTrue(dbUrl.contains(dbPort), "JDBC URL should contain port");
            
            log.info("✅ Database runtime properties validated: {}:{}", dbHost, dbPort);
        }
        
        if (cacheHost != null && cachePort != null) {
            assertTrue(cacheHost.length() > 0, "Cache host should not be empty");
            assertTrue(Integer.parseInt(cachePort) > 0, "Cache port should be valid");
            
            log.info("✅ Cache runtime properties validated: {}:{}", cacheHost, cachePort);
        }
        
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo dbInfo = manager.getContainer("completeDb");
        ContainerInfo cacheInfo = manager.getContainer("completeCache");
        
        assertTrue(dbInfo.getContainer().getFirstMappedPort() > 0, "Database container should have mapped port");
        assertTrue(cacheInfo.getContainer().getFirstMappedPort() > 0, "Cache container should have mapped port");
        
        assertNotNull(dbInfo.getContainer().getHost(), "Database container should have host");
        assertNotNull(cacheInfo.getContainer().getHost(), "Cache container should have host");
        
        log.info("✅ Environment properties and runtime configuration validated");
    }
    
    @Test
    @Order(5)
    @DisplayName("Validate container registry thread safety")
    void testContainerRegistryThreadSafety() throws InterruptedException {
        CompletableFuture<ContainerManager>[] futures = new CompletableFuture[10];
        
        for (int i = 0; i < 10; i++) {
            futures[i] = CompletableFuture.supplyAsync(() -> ContainerRegistry.get());
        }
        
        CompletableFuture.allOf(futures).join();
        
        ContainerManager firstManager = futures[0].join();
        for (int i = 1; i < futures.length; i++) {
            assertSame(firstManager, futures[i].join(), 
                "All threads should get the same ContainerManager instance");
        }
        
        String testClassName = "ThreadSafetyTest";
        Object lock1 = ContainerRegistry.getLock(testClassName);
        Object lock2 = ContainerRegistry.getLock(testClassName);
        
        assertSame(lock1, lock2, "Same class should get same lock instance");
        
        ContainerRegistry.removeLock(testClassName);
        Object lock3 = ContainerRegistry.getLock(testClassName);
        assertNotSame(lock1, lock3, "New lock should be different after removal");
        
        log.info("✅ Container registry thread safety validated");
    }
    
    @Test
    @Order(6)
    @DisplayName("Validate error handling and edge cases")
    void testErrorHandlingAndEdgeCases() {
        ContainerManager manager = ContainerRegistry.get();
        
        assertNull(manager.getContainer("nonexistent"), 
            "Getting non-existent container should return null");
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(completeDataSource);
        
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("CREATE TABLE invalid_sql syntax error");
        }, "Invalid SQL should throw exception");
        
        assertThrows(Exception.class, () -> {
            jdbcTemplate.queryForObject("SELECT * FROM nonexistent_table", String.class);
        }, "Querying non-existent table should throw exception");
        
        assertDoesNotThrow(() -> {
            completeRedisTemplate.opsForValue().get("nonexistent-key");
        }, "Getting non-existent Redis key should not throw exception");
        
        Object nonexistentValue = completeRedisTemplate.opsForValue().get("nonexistent-key");
        assertNull(nonexistentValue, "Non-existent Redis key should return null");
        
        log.info("✅ Error handling and edge cases validated");
    }
    
    @Test
    @Order(7)
    @DisplayName("Validate complete integration and data consistency")
    void testCompleteIntegrationAndDataConsistency() {
        String integrationId = "integration-" + System.currentTimeMillis();
        String testData = "Complete integration test data";
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(completeDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS integration_test (" +
            "integration_id VARCHAR(100) PRIMARY KEY, " +
            "test_data VARCHAR(500), " +
            "cache_key VARCHAR(100), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
        
        String cacheKey = "integration:" + integrationId;
        
        jdbcTemplate.update(
            "INSERT INTO integration_test (integration_id, test_data, cache_key) VALUES (?, ?, ?)",
            integrationId, testData, cacheKey);
        
        completeRedisTemplate.opsForValue().set(cacheKey, testData, Duration.ofMinutes(5));
        
        String dbData = jdbcTemplate.queryForObject(
            "SELECT test_data FROM integration_test WHERE integration_id = ?", 
            String.class, integrationId);
        
        Object cacheData = completeRedisTemplate.opsForValue().get(cacheKey);
        
        assertEquals(testData, dbData, "Database data should match original");
        assertEquals(testData, cacheData, "Cache data should match original");
        assertEquals(dbData, cacheData, "Database and cache data should be consistent");
        
        completeRedisTemplate.opsForHash().put("integration:stats", "db_records", "1");
        completeRedisTemplate.opsForHash().put("integration:stats", "cache_records", "1");
        completeRedisTemplate.opsForHash().put("integration:stats", "last_sync", String.valueOf(System.currentTimeMillis()));
        
        assertEquals("1", completeRedisTemplate.opsForHash().get("integration:stats", "db_records"));
        assertEquals("1", completeRedisTemplate.opsForHash().get("integration:stats", "cache_records"));
        assertNotNull(completeRedisTemplate.opsForHash().get("integration:stats", "last_sync"));
        
        log.info("✅ Complete integration and data consistency validated for ID: {}", integrationId);
    }
    
    @Test
    @Order(8)
    @DisplayName("Validate resource utilization and performance")
    void testResourceUtilizationAndPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(completeDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS performance_test (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "batch_data VARCHAR(200), " +
            "batch_number INT, " +
            "INDEX idx_batch (batch_number)" +
            ")");
        
        final int BATCH_SIZE = 100;
        for (int batch = 0; batch < 5; batch++) {
            final int currentBatch = batch;
            CompletableFuture.runAsync(() -> {
                for (int i = 0; i < BATCH_SIZE; i++) {
                    String data = "Batch-" + currentBatch + "-Item-" + i + "-" + Thread.currentThread().getName();
                    jdbcTemplate.update(
                        "INSERT INTO performance_test (batch_data, batch_number) VALUES (?, ?)",
                        data, currentBatch);
                    
                    String cacheKey = "perf:" + currentBatch + ":" + i;
                    completeRedisTemplate.opsForValue().set(cacheKey, data, Duration.ofMinutes(1));
                }
            }).get(30, TimeUnit.SECONDS);
        }
        
        Integer totalRecords = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM performance_test", Integer.class);
        assertEquals(BATCH_SIZE * 5, totalRecords.intValue(), 
            "Should have inserted all batch records");
        
        for (int batch = 0; batch < 5; batch++) {
            Integer batchCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM performance_test WHERE batch_number = ?", 
                Integer.class, batch);
            assertEquals(BATCH_SIZE, batchCount.intValue(), 
                "Each batch should have correct number of records");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 30000, 
            String.format("Performance test should complete within 30 seconds, took %d ms", duration));
        
        try (Connection connection = completeDataSource.getConnection()) {
            assertTrue(connection.isValid(5), "Connection should be valid");
        }
        
        log.info("✅ Resource utilization and performance validated - {} records in {} ms", 
            totalRecords, duration);
    }
}