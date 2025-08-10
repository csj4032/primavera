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
    @DisplayName("translated_text_7 translated_text_4 translated_text_2 translated_text_1 translated_text_2 verification")
    void testDatabaseContainerStartup() {
        ContainerInfo containerInfo = containerManager.getContainer("primavera");
        
        assertNotNull(containerInfo, "Primavera translated_text_4 translated_text_12 translated_text_4 translated_text_1");
        assertTrue(containerInfo.container().isRunning(), "translated_text_4translated_text_1 execution translated_text_4 translated_text_1");
        
        assertDoesNotThrow(() -> {
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            assertNotNull(version, "translated_text_7 translated_text_2 translated_text_12 translated_text_3 translated_text_1 translated_text_3 translated_text_1");
            assertTrue(version.toLowerCase().contains("mariadb"), "MariaDB translated_text_2 translated_text_13 translated_text_1");
            log.info("translated_text_7 translated_text_2: {}", version);
        }, "translated_text_7 translated_text_2translated_text_1 successfully translated_text_4 translated_text_1");
        
        log.info("translated_text_7 translated_text_4 translated_text_2 translated_text_1 translated_text_2 verification completed");
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_3 translated_text_5 translated_text_9 USERS translated_text_3 translated_text_2 verification")
    void testInitScriptTableStructure() {
        assertDoesNotThrow(() -> {
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS'",
                Integer.class);
            assertEquals(1, tableCount, "USERS translated_text_3translated_text_1 translated_text_4 translated_text_1");
        }, "USERS translated_text_3 translated_text_2 verificationtranslated_text_1 translated_text_9 translated_text_1");

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' ORDER BY ORDINAL_POSITION"
        );

        assertFalse(columns.isEmpty(), "USERS translated_text_3 translated_text_3 translated_text_4 translated_text_1");
        
        List<String> columnNames = columns.stream()
            .map(col -> (String) col.get("COLUMN_NAME"))
            .toList();
        
        assertTrue(columnNames.contains("ID"), "ID translated_text_3 translated_text_4 translated_text_1");
        assertTrue(columnNames.contains("EMAIL"), "EMAIL translated_text_3 translated_text_4 translated_text_1");
        assertTrue(columnNames.contains("PASSWORD"), "PASSWORD translated_text_3 translated_text_4 translated_text_1");
        assertTrue(columnNames.contains("NICKNAME"), "NICKNAME translated_text_3 translated_text_4 translated_text_1");
        assertTrue(columnNames.contains("STATUS"), "STATUS translated_text_3 translated_text_4 translated_text_1");
        assertTrue(columnNames.contains("CREATED_AT"), "CREATED_AT translated_text_3 translated_text_4 translated_text_1");
        assertTrue(columnNames.contains("UPDATED_AT"), "UPDATED_AT translated_text_3 translated_text_4 translated_text_1");

        log.info("USERS translated_text_3 translated_text_2 verification completed - translated_text_2 translated_text_1: {}", columns.size());
        columns.forEach(col -> 
            log.debug("translated_text_2: {} - translated_text_2: {}, Nullable: {}", 
                col.get("COLUMN_NAME"), col.get("DATA_TYPE"), col.get("IS_NULLABLE"))
        );
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_3 translated_text_5 translated_text_3 translated_text_2 user data verification")
    void testInitScriptUserData() {
        Integer totalUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, totalUsers, "translated_text_3 translated_text_6 6translated_text_2 usertranslated_text_1 translated_text_5 translated_text_1");

        String[] expectedEmails = {
            "genius@primavera.com", "admin@primavera.com", "user@primavera.com", 
            "son@primavera.com", "messi@primavera.com", "ronaldo@primavera.com"
        };

        for (String email : expectedEmails) {
            Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?", Integer.class, email);
            assertEquals(1, userCount, email + " usertranslated_text_1 translated_text_4 translated_text_1");
        }

        List<Map<String, Object>> users = jdbcTemplate.queryForList(
            "SELECT ID, EMAIL, NICKNAME, STATUS FROM USERS ORDER BY ID");

        assertEquals(6, users.size(), "6translated_text_2 user datatranslated_text_1 translated_text_10 translated_text_1");

        Map<String, Object> geniusUser = users.get(0);
        assertEquals(1L, ((Number) geniusUser.get("ID")).longValue(), "translated_text_1 translated_text_2 user IDtranslated_text_1 1translated_text_3 translated_text_1");
        assertEquals("genius@primavera.com", geniusUser.get("EMAIL"), "translated_text_1 translated_text_2 user translated_text_4 translated_text_4 translated_text_1");
        assertEquals("Genius", geniusUser.get("NICKNAME"), "translated_text_1 translated_text_2 user translated_text_4 translated_text_4 translated_text_1");
        assertEquals(1, ((Number) geniusUser.get("STATUS")).intValue(), "translated_text_1 translated_text_2 user translated_text_3 1translated_text_3 translated_text_1");

        log.info("translated_text_3 user data verification completed - translated_text_1 {} translated_text_1", totalUsers);
        users.forEach(user -> 
            log.debug("user: ID={}, EMAIL={}, NICKNAME={}, STATUS={}", 
                user.get("ID"), user.get("EMAIL"), user.get("NICKNAME"), user.get("STATUS"))
        );
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_3 data translated_text_2 CRUD translated_text_2 translated_text_1")
    void testCrudOperationsOnInitializedData() {
        Map<String, Object> existingUser = jdbcTemplate.queryForMap(
            "SELECT * FROM USERS WHERE EMAIL = ?", "genius@primavera.com");
        
        assertNotNull(existingUser, "translated_text_2 user translated_text_8 translated_text_9 translated_text_1");
        assertEquals("Genius", existingUser.get("NICKNAME"), "translated_text_2 user translated_text_4 translated_text_4 translated_text_1");

        int updateResult = jdbcTemplate.update(
            "UPDATE USERS SET NICKNAME = ?, STATUS = ? WHERE EMAIL = ?",
            "Updated Genius", 2, "genius@primavera.com");
        assertEquals(1, updateResult, "user translated_text_5 translated_text_9 translated_text_1");

        Map<String, Object> updatedUser = jdbcTemplate.queryForMap(
            "SELECT NICKNAME, STATUS FROM USERS WHERE EMAIL = ?", "genius@primavera.com");
        assertEquals("Updated Genius", updatedUser.get("NICKNAME"), "translated_text_4 translated_text_7 translated_text_1");
        assertEquals(2, ((Number) updatedUser.get("STATUS")).intValue(), "translated_text_3 translated_text_7 translated_text_1");

        int insertResult = jdbcTemplate.update(
            "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
            "newuser@test.com", "{noop}newpass", "NewUser", 1);
        assertEquals(1, insertResult, "translated_text_1 user translated_text_3 translated_text_9 translated_text_1");

        Integer totalUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(7, totalUsers, "translated_text_1 user translated_text_1translated_text_1 7translated_text_1translated_text_1 translated_text_3 translated_text_1");

        Map<String, Object> newUser = jdbcTemplate.queryForMap(
            "SELECT EMAIL, NICKNAME, STATUS FROM USERS WHERE EMAIL = ?", "newuser@test.com");
        assertEquals("NewUser", newUser.get("NICKNAME"), "translated_text_1 user translated_text_4 translated_text_4 translated_text_1");
        assertEquals(1, ((Number) newUser.get("STATUS")).intValue(), "translated_text_1 user translated_text_3 1translated_text_3 translated_text_1");

        log.info("translated_text_3 data translated_text_2 CRUD translated_text_2 completed - translated_text_1 user: {}", totalUsers);
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_7 translated_text_3 translated_text_1 translated_text_4 verification")
    void testDatabaseConstraintsAndIndexes() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
                "genius@primavera.com", "{noop}duplicate", "Duplicate", 1);
        }, "translated_text_2 translated_text_4 translated_text_2 translated_text_3 translated_text_9 translated_text_1");

        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
            "SELECT INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' AND INDEX_NAME = 'IDX_USERS_EMAIL'"
        );

        assertFalse(indexes.isEmpty(), "IDX_USERS_EMAIL translated_text_3translated_text_1 translated_text_4 translated_text_1");
        assertEquals("EMAIL", indexes.get(0).get("COLUMN_NAME"), "translated_text_3translated_text_1 EMAIL translated_text_2 translated_text_3 translated_text_1");

        List<Map<String, Object>> primaryKey = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' AND CONSTRAINT_NAME = 'PRIMARY'"
        );

        assertFalse(primaryKey.isEmpty(), "PRIMARY KEYtranslated_text_1 translated_text_4 translated_text_1");
        assertEquals("ID", primaryKey.get(0).get("COLUMN_NAME"), "PRIMARY KEYtranslated_text_1 ID translated_text_2 translated_text_3 translated_text_1");

        log.info("translated_text_7 translated_text_4 translated_text_1 translated_text_3 verification completed");
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 translated_text_1 translated_text_2 translated_text_4 verification")
    void testDateColumnDefaultsAndAutoUpdate() throws InterruptedException {
        long beforeInsert = System.currentTimeMillis() / 1000;

        int insertResult = jdbcTemplate.update(
            "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
            "datetime@test.com", "{noop}test", "DateTimeTest", 1);
        assertEquals(1, insertResult, "user translated_text_3 translated_text_9 translated_text_1");

        long afterInsert = System.currentTimeMillis() / 1000;

        Map<String, Object> dateUser = jdbcTemplate.queryForMap(
            "SELECT ID, CREATED_AT, UPDATED_AT FROM USERS WHERE EMAIL = ?", "datetime@test.com");

        assertNotNull(dateUser.get("CREATED_AT"), "CREATED_ATtranslated_text_1 translated_text_3 translated_text_1");
        assertNotNull(dateUser.get("UPDATED_AT"), "UPDATED_ATtranslated_text_1 translated_text_3 translated_text_1");

        assertEquals(dateUser.get("CREATED_AT"), dateUser.get("UPDATED_AT"), 
            "translated_text_2 creation translated_text_1 CREATED_ATtranslated_text_1 UPDATED_ATtranslated_text_1 translated_text_4 translated_text_1");

        Long userId = ((Number) dateUser.get("ID")).longValue();

        Thread.sleep(1000);

        int updateResult = jdbcTemplate.update(
            "UPDATE USERS SET NICKNAME = ? WHERE ID = ?", "UpdatedDateTimeTest", userId);
        assertEquals(1, updateResult, "data translated_text_5 translated_text_9 translated_text_1");

        Map<String, Object> updatedDateUser = jdbcTemplate.queryForMap(
            "SELECT CREATED_AT, UPDATED_AT FROM USERS WHERE ID = ?", userId);

        assertEquals(dateUser.get("CREATED_AT"), updatedDateUser.get("CREATED_AT"), 
            "CREATED_ATtranslated_text_1 translated_text_4 translated_text_3 translated_text_1");
        assertNotEquals(dateUser.get("UPDATED_AT"), updatedDateUser.get("UPDATED_AT"), 
            "UPDATED_ATtranslated_text_1 translated_text_4 translated_text_1 translated_text_3 translated_text_1");

        log.info("translated_text_2 translated_text_2 translated_text_2 translated_text_1 translated_text_2 translated_text_4 verification completed");
    }

    @Test
    @Order(7)
    @DisplayName("translated_text_7 translated_text_2 translated_text_1 translated_text_3 data processing test")
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
                log.warn("translated_text_2 translated_text_2 translated_text_2 failure (translated_text_3): {}", e.getMessage());
            }
        }

        long insertTime = System.currentTimeMillis() - startTime;

        Integer perfUserCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE ?", Integer.class, testPrefix + "%@perf.com");
        
        assertTrue(perfUserCount > 0, "translated_text_2 test usertranslated_text_1 translated_text_5 translated_text_1");
        log.info("translated_text_2 test data translated_text_2 completed - {} translated_text_1, translated_text_1: {}ms", perfUserCount, insertTime);

        startTime = System.currentTimeMillis();
        
        List<Map<String, Object>> perfUsers = jdbcTemplate.queryForList(
            "SELECT ID, EMAIL, NICKNAME, STATUS FROM USERS WHERE EMAIL LIKE ? ORDER BY ID LIMIT 50", 
            testPrefix + "%@perf.com");
        
        long queryTime = System.currentTimeMillis() - startTime;

        assertFalse(perfUsers.isEmpty(), "translated_text_2 test data inquiry translated_text_1translated_text_1 translated_text_4 translated_text_1");
        assertTrue(queryTime < 5000, "translated_text_2 translated_text_8 5translated_text_1 translated_text_1 completedtranslated_text_3 translated_text_1");

        startTime = System.currentTimeMillis();
        
        Map<String, Object> aggregateResult = jdbcTemplate.queryForMap(
            "SELECT COUNT(*) as user_count, MAX(ID) as max_id, MIN(ID) as min_id FROM USERS");
        
        long aggregateTime = System.currentTimeMillis() - startTime;

        assertTrue(((Number) aggregateResult.get("user_count")).intValue() > 6, 
            "translated_text_1 user translated_text_1translated_text_1 translated_text_2 data translated_text_3 translated_text_1");
        assertTrue(aggregateTime < 2000, "translated_text_2 translated_text_1 2translated_text_1 translated_text_1 completedtranslated_text_3 translated_text_1");

        log.info("translated_text_7 translated_text_2 test completed - inquiry: {}ms, translated_text_2: {}ms, translated_text_1 user: {}", 
            queryTime, aggregateTime, aggregateResult.get("user_count"));
    }
}