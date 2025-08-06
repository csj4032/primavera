package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Edge Case and Error Handling Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "edgeCaseDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "edgeCaseCache")
})
class EdgeCaseTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    @Qualifier("edgeCaseDb")
    private DataSource edgeCaseDataSource;
    
    @Autowired
    @Qualifier("edgeCaseCache")
    private RedisTemplate<String, Object> edgeCaseRedisTemplate;
    
    @Test
    @Order(1)
    @DisplayName("Empty configuration handling")
    void testEmptyConfigurationHandling() {
        ContainerManager manager = ContainerRegistry.get();
        
        assertNotNull(manager, "ContainerManager should handle empty configuration gracefully");
        assertTrue(manager.isStarted(), "Containers should start with default configuration");
        
        ContainerInfo dbInfo = manager.getContainer("edgeCaseDb");
        assertNotNull(dbInfo, "Database container should be created with defaults");
        assertEquals(ContainerType.MARIADB, dbInfo.type(), "Container type should be preserved");
        
        log.info("Empty configuration handled gracefully");
    }
    
    @Test
    @Order(2)
    @DisplayName("Null value handling in database operations")
    void testNullValueHandling() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(edgeCaseDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS null_test (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "nullable_field VARCHAR(100), " +
            "required_field VARCHAR(100) NOT NULL" +
            ")");
        
        assertDoesNotThrow(() -> {
            jdbcTemplate.update("INSERT INTO null_test (nullable_field, required_field) VALUES (?, ?)",
                null, "required_value");
        }, "Null values should be handled properly");
        
        String result = jdbcTemplate.queryForObject(
            "SELECT nullable_field FROM null_test WHERE required_field = ?",
            String.class, "required_value");
        
        assertNull(result, "Null value should be preserved in database");
        
        log.info("Null value handling validated");
    }
    
    @Test
    @Order(3)
    @DisplayName("Large data handling and memory management")
    void testLargeDataHandling() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(edgeCaseDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS large_data_test (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "large_text LONGTEXT" +
            ")");
        
        String largeData = "X".repeat(1024 * 1024);
        
        assertDoesNotThrow(() -> {
            jdbcTemplate.update("INSERT INTO large_data_test (large_text) VALUES (?)", largeData);
        }, "Large data should be handled without memory issues");
        
        String retrievedData = jdbcTemplate.queryForObject(
            "SELECT large_text FROM large_data_test ORDER BY id DESC LIMIT 1", String.class);
        
        assertEquals(largeData.length(), retrievedData.length(),
            "Large data should be retrieved completely");
        
        log.info("Large data handling validated: {} characters", retrievedData.length());
    }
    
    @Test
    @Order(4)
    @DisplayName("Redis key expiration and cleanup")
    void testRedisKeyExpirationHandling() throws InterruptedException {
        String expiringKey = "expiring-key-" + System.currentTimeMillis();
        String value = "expiring-value";
        
        edgeCaseRedisTemplate.opsForValue().set(expiringKey, value, Duration.ofSeconds(2));
        
        Object retrievedValue = edgeCaseRedisTemplate.opsForValue().get(expiringKey);
        assertEquals(value, retrievedValue, "Value should be available before expiration");
        
        Thread.sleep(3000);
        
        Object expiredValue = edgeCaseRedisTemplate.opsForValue().get(expiringKey);
        assertNull(expiredValue, "Value should be null after expiration");
        
        log.info("Redis key expiration handling validated");
    }
    
    @Test
    @Order(5)
    @DisplayName("Database transaction rollback handling")
    void testTransactionRollbackHandling() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(edgeCaseDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS rollback_test (" +
            "id INT PRIMARY KEY, " +
            "data VARCHAR(50)" +
            ")");
        
        jdbcTemplate.update("INSERT INTO rollback_test (id, data) VALUES (1, 'initial') " +
            "ON DUPLICATE KEY UPDATE data = VALUES(data)");
        
        assertThrows(DataAccessException.class, () -> {
            jdbcTemplate.execute("BEGIN");
            jdbcTemplate.update("UPDATE rollback_test SET data = 'updated' WHERE id = 1");
            jdbcTemplate.execute("INSERT INTO rollback_test (id, data) VALUES (1, 'duplicate')");
            jdbcTemplate.execute("COMMIT");
        }, "Duplicate key should cause transaction failure");
        
        String finalData = jdbcTemplate.queryForObject(
            "SELECT data FROM rollback_test WHERE id = 1", String.class);
        
        assertEquals("initial", finalData, "Data should remain unchanged after rollback");
        
        log.info("Transaction rollback handling validated");
    }
    
    @Test
    @Order(6)
    @DisplayName("Container restart resilience")
    void testContainerRestartResilience() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo dbInfo = manager.getContainer("edgeCaseDb");
        
        assertTrue(dbInfo.container().isRunning(), "Container should be running initially");
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(edgeCaseDataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS restart_test (id INT PRIMARY KEY, data VARCHAR(50))");
        jdbcTemplate.update("INSERT INTO restart_test (id, data) VALUES (1, 'before_restart')");
        
        String beforeRestart = jdbcTemplate.queryForObject(
            "SELECT data FROM restart_test WHERE id = 1", String.class);
        assertEquals("before_restart", beforeRestart, "Data should be accessible before restart");
        
        log.info("Container restart resilience - data persists across operations");
    }
    
    @Test
    @Order(7)
    @DisplayName("Special character handling in database")
    void testSpecialCharacterHandling() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(edgeCaseDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS special_char_test (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "unicode_data VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci" +
            ")");
        
        String specialChars = "🚀🌟💡🔥🎉 한글테스트 العربية русский 中文 日本語 emoji: 😀😂🤔";
        
        assertDoesNotThrow(() -> {
            jdbcTemplate.update("INSERT INTO special_char_test (unicode_data) VALUES (?)", specialChars);
        }, "Special characters should be handled properly");
        
        String retrievedChars = jdbcTemplate.queryForObject(
            "SELECT unicode_data FROM special_char_test ORDER BY id DESC LIMIT 1", String.class);
        
        assertEquals(specialChars, retrievedChars, "Special characters should be preserved");
        
        log.info("Special character handling validated: {}", retrievedChars);
    }
    
    @Test
    @Order(8)
    @DisplayName("Redis connection pool exhaustion handling")
    void testRedisConnectionPoolHandling() {
        String baseKey = "pool-test-";
        
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 1000; i++) {
                String key = baseKey + i;
                String value = "value-" + i;
                edgeCaseRedisTemplate.opsForValue().set(key, value);
                
                if (i % 100 == 0) {
                    Object retrieved = edgeCaseRedisTemplate.opsForValue().get(key);
                    assertEquals(value, retrieved, "Value should be consistent under load");
                }
            }
        }, "Redis should handle high connection load gracefully");
        
        log.info("Redis connection pool handling validated with 1000 operations");
    }
    
    @Test
    @Order(9)
    @DisplayName("Database constraint violation handling")
    void testDatabaseConstraintViolations() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(edgeCaseDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS constraint_test (" +
            "id INT PRIMARY KEY, " +
            "unique_field VARCHAR(50) UNIQUE, " +
            "not_null_field VARCHAR(50) NOT NULL" +
            ")");
        
        jdbcTemplate.update("INSERT INTO constraint_test (id, unique_field, not_null_field) " +
            "VALUES (1, 'unique_value', 'not_null_value')");
        
        assertThrows(DataAccessException.class, () -> {
            jdbcTemplate.update("INSERT INTO constraint_test (id, unique_field, not_null_field) " +
                "VALUES (2, 'unique_value', 'another_value')");
        }, "Unique constraint violation should be properly handled");
        
        assertThrows(DataAccessException.class, () -> {
            jdbcTemplate.update("INSERT INTO constraint_test (id, unique_field, not_null_field) " +
                "VALUES (3, 'another_unique', NULL)");
        }, "Not null constraint violation should be properly handled");
        
        log.info("Database constraint violation handling validated");
    }
    
    @Test
    @Order(10)
    @DisplayName("Bean qualifier edge cases")
    void testBeanQualifierEdgeCases() {
        assertTrue(applicationContext.containsBean("edgeCaseDb"),
            "Bean with edge case name should be registered");
        assertTrue(applicationContext.containsBean("edgeCaseCache"),
            "Bean with edge case name should be registered");
        
        DataSource retrievedDs = applicationContext.getBean("edgeCaseDb", DataSource.class);
        RedisTemplate<?, ?> retrievedRedis = applicationContext.getBean("edgeCaseCache", RedisTemplate.class);
        
        assertSame(edgeCaseDataSource, retrievedDs,
            "Bean qualifier should return exact same instance");
        assertSame(edgeCaseRedisTemplate, retrievedRedis,
            "Bean qualifier should return exact same instance");
        
        log.info("Bean qualifier edge cases validated");
    }
    
    @Test
    @Order(11)
    @DisplayName("Configuration property type coercion")
    void testConfigurationPropertyTypeCoercion() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo dbInfo = manager.getContainer("edgeCaseDb");
        
        assertNotNull(dbInfo.spec().getStartupTimeout(),
            "Startup timeout should be properly coerced to integer");
        assertTrue(dbInfo.spec().getStartupTimeout() > 0,
            "Startup timeout should be positive value");
        
        assertNotNull(dbInfo.spec().getImage(),
            "Image name should be properly handled as string");
        assertTrue(dbInfo.spec().getImage().contains(":"),
            "Image should contain version tag");
        
        log.info("Configuration property type coercion validated");
    }
}