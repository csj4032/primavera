package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(properties = {
    "spring.test.context.cache.maxSize=0",
    "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Init Script Database Integration Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera")
})
class InitScriptDatabaseTest {

    @Autowired
    @Qualifier("primavera")
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private ContainerManager containerManager;

    @BeforeAll
    void setupInitScriptTests() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        containerManager = ContainerRegistry.get();
        
        log.info("Init script database integration test environment initialized");
    }

    @Test
    @Order(1)
    @DisplayName("logging file test should test verification")
    void testDatabaseContainerStartup() {
        ContainerInfo containerInfo = containerManager.getContainer("primavera");
        
        assertNotNull(containerInfo, "Primavera file operation file should");
        assertTrue(containerInfo.container().isRunning(), "fileshould execution file should");
        
        assertDoesNotThrow(() -> {
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            assertNotNull(version, "logging test operation connection should connection should");
            assertTrue(version.toLowerCase().contains("mariadb"), "MariaDB test created successfully should");
            log.info("logging test: {}", version);
        }, "logging testshould successfully file should");
        
        log.info("logging file test should test verification completed");
    }

    @Test
    @Order(2)
    @DisplayName("connection Endpoint should not USERS connection test verification")
    void testInitScriptTableStructure() {
        assertDoesNotThrow(() -> {
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS'",
                Integer.class);
            assertEquals(1, tableCount, "USERS connectionshould file should");
        }, "USERS connection test verificationshould should not should");

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' ORDER BY ORDINAL_POSITION"
        );

        assertFalse(columns.isEmpty(), "USERS connection file should");
        
        List<String> columnNames = columns.stream()
            .map(col -> (String) col.get("COLUMN_NAME"))
            .toList();
        
        assertTrue(columnNames.contains("ID"), "ID connection file should");
        assertTrue(columnNames.contains("EMAIL"), "EMAIL connection file should");
        assertTrue(columnNames.contains("PASSWORD"), "PASSWORD connection file should");
        assertTrue(columnNames.contains("NICKNAME"), "NICKNAME connection file should");
        assertTrue(columnNames.contains("STATUS"), "STATUS connection file should");
        assertTrue(columnNames.contains("CREATED_AT"), "CREATED_AT connection file should");
        assertTrue(columnNames.contains("UPDATED_AT"), "UPDATED_AT connection file should");

        log.info("USERS connection test verification completed - test should: {}", columns.size());
        columns.forEach(col -> 
            log.debug("test: {} - test: {}, Nullable: {}", 
                col.get("COLUMN_NAME"), col.get("DATA_TYPE"), col.get("IS_NULLABLE"))
        );
    }

    @Test
    @Order(3)
    @DisplayName("connection Endpoint connection test user data verification")
    void testInitScriptUserData() {
        Integer totalUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, totalUsers, "connection with 6test usershould Endpoint should");

        String[] expectedEmails = {
            "genius@primavera.com", "admin@primavera.com", "user@primavera.com", 
            "son@primavera.com", "messi@primavera.com", "ronaldo@primavera.com"
        };

        for (String email : expectedEmails) {
            Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?", Integer.class, email);
            assertEquals(1, userCount, email + " usershould file should");
        }

        List<Map<String, Object>> users = jdbcTemplate.queryForList(
            "SELECT ID, EMAIL, NICKNAME, STATUS FROM USERS ORDER BY ID");

        assertEquals(6, users.size(), "6test user dataneeds to be added0 should");

        Map<String, Object> geniusUser = users.get(0);
        assertEquals(1L, ((Number) geniusUser.get("ID")).longValue(), "should test user IDshould 1connection should");
        assertEquals("genius@primavera.com", geniusUser.get("EMAIL"), "should test user file should");
        assertEquals("Genius", geniusUser.get("NICKNAME"), "should test user file should");
        assertEquals(1, ((Number) geniusUser.get("STATUS")).intValue(), "should test user connection 1connection should");

        log.info("connection user data verification completed - should {} should", totalUsers);
        users.forEach(user -> 
            log.debug("user: ID={}, EMAIL={}, NICKNAME={}, STATUS={}", 
                user.get("ID"), user.get("EMAIL"), user.get("NICKNAME"), user.get("STATUS"))
        );
    }

    @Test
    @Order(4)
    @DisplayName("connection data test CRUD test should")
    void testCrudOperationsOnInitializedData() {
        Map<String, Object> existingUser = jdbcTemplate.queryForMap(
            "SELECT * FROM USERS WHERE EMAIL = ?", "genius@primavera.com");
        
        assertNotNull(existingUser, "test user configuration should not should");
        assertEquals("Genius", existingUser.get("NICKNAME"), "test user file should");

        int updateResult = jdbcTemplate.update(
            "UPDATE USERS SET NICKNAME = ?, STATUS = ? WHERE EMAIL = ?",
            "Updated Genius", 2, "genius@primavera.com");
        assertEquals(1, updateResult, "user Endpoint should not should");

        Map<String, Object> updatedUser = jdbcTemplate.queryForMap(
            "SELECT NICKNAME, STATUS FROM USERS WHERE EMAIL = ?", "genius@primavera.com");
        assertEquals("Updated Genius", updatedUser.get("NICKNAME"), "file logging should");
        assertEquals(2, ((Number) updatedUser.get("STATUS")).intValue(), "connection logging should");

        int insertResult = jdbcTemplate.update(
            "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
            "newuser@test.com", "{noop}newpass", "NewUser", 1);
        assertEquals(1, insertResult, "should user connection should not should");

        Integer totalUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(7, totalUsers, "should user shouldshould 7shouldshould connection should");

        Map<String, Object> newUser = jdbcTemplate.queryForMap(
            "SELECT EMAIL, NICKNAME, STATUS FROM USERS WHERE EMAIL = ?", "newuser@test.com");
        assertEquals("NewUser", newUser.get("NICKNAME"), "should user file should");
        assertEquals(1, ((Number) newUser.get("STATUS")).intValue(), "should user connection 1connection should");

        log.info("connection data test CRUD test completed - should user: {}", totalUsers);
    }

    @Test
    @Order(5)
    @DisplayName("logging connection should file verification")
    void testDatabaseConstraintsAndIndexes() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
                "genius@primavera.com", "{noop}duplicate", "Duplicate", 1);
        }, "test file test connection should not should");

        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
            "SELECT INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' AND INDEX_NAME = 'IDX_USERS_EMAIL'"
        );

        assertFalse(indexes.isEmpty(), "IDX_USERS_EMAIL connectionshould file should");
        assertEquals("EMAIL", indexes.get(0).get("COLUMN_NAME"), "connectionshould EMAIL test connection should");

        List<Map<String, Object>> primaryKey = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' AND CONSTRAINT_NAME = 'PRIMARY'"
        );

        assertFalse(primaryKey.isEmpty(), "PRIMARY KEYshould file should");
        assertEquals("ID", primaryKey.get(0).get("COLUMN_NAME"), "PRIMARY KEYshould ID test connection should");

        log.info("logging file should connection verification completed");
    }

    @Test
    @Order(6)
    @DisplayName("test test should test file verification")
    void testDateColumnDefaultsAndAutoUpdate() throws InterruptedException {
        long beforeInsert = System.currentTimeMillis() / 1000;

        int insertResult = jdbcTemplate.update(
            "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
            "datetime@test.com", "{noop}test", "DateTimeTest", 1);
        assertEquals(1, insertResult, "user connection should not should");

        long afterInsert = System.currentTimeMillis() / 1000;

        Map<String, Object> dateUser = jdbcTemplate.queryForMap(
            "SELECT ID, CREATED_AT, UPDATED_AT FROM USERS WHERE EMAIL = ?", "datetime@test.com");

        assertNotNull(dateUser.get("CREATED_AT"), "CREATED_ATshould connection should");
        assertNotNull(dateUser.get("UPDATED_AT"), "UPDATED_ATshould connection should");

        assertEquals(dateUser.get("CREATED_AT"), dateUser.get("UPDATED_AT"), 
            "test creation should CREATED_ATshould UPDATED_ATshould file should");

        Long userId = ((Number) dateUser.get("ID")).longValue();

        Thread.sleep(1000);

        int updateResult = jdbcTemplate.update(
            "UPDATE USERS SET NICKNAME = ? WHERE ID = ?", "UpdatedDateTimeTest", userId);
        assertEquals(1, updateResult, "data Endpoint should not should");

        Map<String, Object> updatedDateUser = jdbcTemplate.queryForMap(
            "SELECT CREATED_AT, UPDATED_AT FROM USERS WHERE ID = ?", userId);

        assertEquals(dateUser.get("CREATED_AT"), updatedDateUser.get("CREATED_AT"), 
            "CREATED_ATshould file connection should");
        assertNotEquals(dateUser.get("UPDATED_AT"), updatedDateUser.get("UPDATED_AT"), 
            "UPDATED_ATshould file should connection should");

        log.info("test test should test file verification completed");
    }

    @Test
    @Order(7)
    @DisplayName("logging test should connection data processing test")
    void testDatabasePerformanceAndLargeDataHandling() {
        int batchSize = 100;
        String testPrefix = "performance_";
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < batchSize; i++) {
            try {
                jdbcTemplate.update(
                    "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
                    testPrefix + i + "@perf.com", "{noop}test", "PerfUser" + i, 1);
            } catch (Exception e) {
                log.warn("test test failure (connection): {}", e.getMessage());
            }
        }

        long insertTime = System.currentTimeMillis() - startTime;

        Integer perfUserCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE ?", Integer.class, testPrefix + "%@perf.com");
        
        assertTrue(perfUserCount > 0, "test usershould Endpoint should");
        log.info("test data test completed - {} should, should: {}ms", perfUserCount, insertTime);

        startTime = System.currentTimeMillis();
        
        List<Map<String, Object>> perfUsers = jdbcTemplate.queryForList(
            "SELECT ID, EMAIL, NICKNAME, STATUS FROM USERS WHERE EMAIL LIKE ? ORDER BY ID LIMIT 50", 
            testPrefix + "%@perf.com");
        
        long queryTime = System.currentTimeMillis() - startTime;

        assertFalse(perfUsers.isEmpty(), "test data inquiry shouldshould file should");
        assertTrue(queryTime < 5000, "test configuration 5needs to be added completedconnection should");

        startTime = System.currentTimeMillis();
        
        Map<String, Object> aggregateResult = jdbcTemplate.queryForMap(
            "SELECT COUNT(*) as user_count, MAX(ID) as max_id, MIN(ID) as min_id FROM USERS");
        
        long aggregateTime = System.currentTimeMillis() - startTime;

        assertTrue(((Number) aggregateResult.get("user_count")).intValue() > 6, 
            "should user shouldshould test data connection should");
        assertTrue(aggregateTime < 2000, "test should 2needs to be added completedconnection should");

        log.info("logging test completed - inquiry: {}ms, test: {}ms, should user: {}", 
            queryTime, aggregateTime, aggregateResult.get("user_count"));
    }
}