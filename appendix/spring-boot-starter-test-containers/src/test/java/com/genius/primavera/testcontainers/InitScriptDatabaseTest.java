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

/**
 * Init Script Database Integration Tests
 * - 데이터베이스 초기화 스크립트 처리 검증
 * - classpath:./sql/init.sql 파일을 통한 테이블 및 데이터 초기화 확인
 * - 초기화된 데이터의 정확성 검증
 * - 컨테이너 재시작 시 초기화 스크립트 재실행 확인
 * - 스크립트 경로 처리 및 로깅 검증
 */
@Slf4j
@SpringBootTest
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
    @DisplayName("데이터베이스 컨테이너 시작 및 연결 확인")
    void testDatabaseContainerStartup() {
        ContainerInfo containerInfo = containerManager.getContainer("primavera");
        
        assertNotNull(containerInfo, "Primavera 컨테이너 정보가 존재해야 함");
        assertTrue(containerInfo.getContainer().isRunning(), "컨테이너가 실행 중이어야 함");
        
        // MariaDB containers don't have healthcheck by default, so we verify connection instead
        assertDoesNotThrow(() -> {
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            assertNotNull(version, "데이터베이스 버전 정보를 가져올 수 있어야 함");
            assertTrue(version.toLowerCase().contains("mariadb"), "MariaDB 버전 정보여야 함");
            log.info("데이터베이스 버전: {}", version);
        }, "데이터베이스 연결이 정상적으로 작동해야 함");
        
        log.info("데이터베이스 컨테이너 시작 및 연결 확인 완료");
    }

    @Test
    @Order(2)
    @DisplayName("초기화 스크립트로 생성된 USERS 테이블 구조 확인")
    void testInitScriptTableStructure() {
        // USERS 테이블이 존재하는지 확인
        assertDoesNotThrow(() -> {
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS'",
                Integer.class);
            assertEquals(1, tableCount, "USERS 테이블이 존재해야 함");
        }, "USERS 테이블 존재 확인이 성공해야 함");

        // 테이블 컬럼 구조 확인
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' ORDER BY ORDINAL_POSITION"
        );

        assertFalse(columns.isEmpty(), "USERS 테이블에 컬럼이 존재해야 함");
        
        // 필수 컬럼들 존재 확인
        List<String> columnNames = columns.stream()
            .map(col -> (String) col.get("COLUMN_NAME"))
            .toList();
        
        assertTrue(columnNames.contains("ID"), "ID 컬럼이 존재해야 함");
        assertTrue(columnNames.contains("EMAIL"), "EMAIL 컬럼이 존재해야 함");
        assertTrue(columnNames.contains("PASSWORD"), "PASSWORD 컬럼이 존재해야 함");
        assertTrue(columnNames.contains("NICKNAME"), "NICKNAME 컬럼이 존재해야 함");
        assertTrue(columnNames.contains("STATUS"), "STATUS 컬럼이 존재해야 함");
        assertTrue(columnNames.contains("CREATED_AT"), "CREATED_AT 컬럼이 존재해야 함");
        assertTrue(columnNames.contains("UPDATED_AT"), "UPDATED_AT 컬럼이 존재해야 함");

        log.info("USERS 테이블 구조 확인 완료 - 컬럼 수: {}", columns.size());
        columns.forEach(col -> 
            log.debug("컬럼: {} - 타입: {}, Nullable: {}", 
                col.get("COLUMN_NAME"), col.get("DATA_TYPE"), col.get("IS_NULLABLE"))
        );
    }

    @Test
    @Order(3)
    @DisplayName("초기화 스크립트로 삽입된 기본 사용자 데이터 확인")
    void testInitScriptUserData() {
        // 전체 사용자 수 확인
        Integer totalUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, totalUsers, "초기화 스크립트에서 6명의 사용자가 삽입되어야 함");

        // 특정 사용자들 존재 확인
        String[] expectedEmails = {
            "genius@primavera.com", "admin@primavera.com", "user@primavera.com", 
            "son@primavera.com", "messi@primavera.com", "ronaldo@primavera.com"
        };

        for (String email : expectedEmails) {
            Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?", Integer.class, email);
            assertEquals(1, userCount, email + " 사용자가 존재해야 함");
        }

        // 사용자 데이터 상세 확인
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
            "SELECT ID, EMAIL, NICKNAME, STATUS FROM USERS ORDER BY ID");

        assertEquals(6, users.size(), "6명의 사용자 데이터가 조회되어야 함");

        // 첫 번째 사용자 (genius@primavera.com) 확인
        Map<String, Object> geniusUser = users.get(0);
        assertEquals(1L, ((Number) geniusUser.get("ID")).longValue(), "첫 번째 사용자의 ID는 1이어야 함");
        assertEquals("genius@primavera.com", geniusUser.get("EMAIL"), "첫 번째 사용자의 이메일이 일치해야 함");
        assertEquals("Genius", geniusUser.get("NICKNAME"), "첫 번째 사용자의 닉네임이 일치해야 함");
        assertEquals(1, ((Number) geniusUser.get("STATUS")).intValue(), "첫 번째 사용자의 상태가 1이어야 함");

        log.info("초기화된 사용자 데이터 확인 완료 - 총 {} 명", totalUsers);
        users.forEach(user -> 
            log.debug("사용자: ID={}, EMAIL={}, NICKNAME={}, STATUS={}", 
                user.get("ID"), user.get("EMAIL"), user.get("NICKNAME"), user.get("STATUS"))
        );
    }

    @Test
    @Order(4)
    @DisplayName("초기화된 데이터에 대한 CRUD 작업 수행")
    void testCrudOperationsOnInitializedData() {
        // 기존 사용자 조회
        Map<String, Object> existingUser = jdbcTemplate.queryForMap(
            "SELECT * FROM USERS WHERE EMAIL = ?", "genius@primavera.com");
        
        assertNotNull(existingUser, "기존 사용자 조회가 성공해야 함");
        assertEquals("Genius", existingUser.get("NICKNAME"), "기존 사용자의 닉네임이 일치해야 함");

        // 기존 사용자 업데이트
        int updateResult = jdbcTemplate.update(
            "UPDATE USERS SET NICKNAME = ?, STATUS = ? WHERE EMAIL = ?",
            "Updated Genius", 2, "genius@primavera.com");
        assertEquals(1, updateResult, "사용자 업데이트가 성공해야 함");

        // 업데이트 결과 확인
        Map<String, Object> updatedUser = jdbcTemplate.queryForMap(
            "SELECT NICKNAME, STATUS FROM USERS WHERE EMAIL = ?", "genius@primavera.com");
        assertEquals("Updated Genius", updatedUser.get("NICKNAME"), "닉네임이 업데이트되어야 함");
        assertEquals(2, ((Number) updatedUser.get("STATUS")).intValue(), "상태가 업데이트되어야 함");

        // 새로운 사용자 추가
        int insertResult = jdbcTemplate.update(
            "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
            "newuser@test.com", "{noop}newpass", "NewUser", 1);
        assertEquals(1, insertResult, "새 사용자 삽입이 성공해야 함");

        // 전체 사용자 수 확인 (초기 6명 + 새로 추가한 1명)
        Integer totalUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(7, totalUsers, "총 사용자 수가 7명이 되어야 함");

        // 새 사용자 조회 확인
        Map<String, Object> newUser = jdbcTemplate.queryForMap(
            "SELECT EMAIL, NICKNAME, STATUS FROM USERS WHERE EMAIL = ?", "newuser@test.com");
        assertEquals("NewUser", newUser.get("NICKNAME"), "새 사용자의 닉네임이 일치해야 함");
        assertEquals(1, ((Number) newUser.get("STATUS")).intValue(), "새 사용자의 상태가 1이어야 함");

        log.info("초기화된 데이터에 대한 CRUD 작업 완료 - 총 사용자: {}", totalUsers);
    }

    @Test
    @Order(5)
    @DisplayName("데이터베이스 인덱스 및 제약조건 확인")
    void testDatabaseConstraintsAndIndexes() {
        // EMAIL 컬럼의 UNIQUE 제약조건 확인
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
                "genius@primavera.com", "{noop}duplicate", "Duplicate", 1);
        }, "중복 이메일로 인해 삽입이 실패해야 함");

        // 인덱스 존재 확인 (IDX_USERS_EMAIL)
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
            "SELECT INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' AND INDEX_NAME = 'IDX_USERS_EMAIL'"
        );

        assertFalse(indexes.isEmpty(), "IDX_USERS_EMAIL 인덱스가 존재해야 함");
        assertEquals("EMAIL", indexes.get(0).get("COLUMN_NAME"), "인덱스가 EMAIL 컬럼에 설정되어야 함");

        // PRIMARY KEY 확인
        List<Map<String, Object>> primaryKey = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'USERS' AND CONSTRAINT_NAME = 'PRIMARY'"
        );

        assertFalse(primaryKey.isEmpty(), "PRIMARY KEY가 존재해야 함");
        assertEquals("ID", primaryKey.get(0).get("COLUMN_NAME"), "PRIMARY KEY가 ID 컬럼에 설정되어야 함");

        log.info("데이터베이스 제약조건 및 인덱스 확인 완료");
    }

    @Test
    @Order(6)
    @DisplayName("날짜 컬럼의 기본값 및 자동 업데이트 확인")
    void testDateColumnDefaultsAndAutoUpdate() throws InterruptedException {
        // 현재 시간 기록
        long beforeInsert = System.currentTimeMillis() / 1000; // 초 단위로 변환

        // CREATED_AT, UPDATED_AT 기본값 확인을 위한 사용자 삽입
        int insertResult = jdbcTemplate.update(
            "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
            "datetime@test.com", "{noop}test", "DateTimeTest", 1);
        assertEquals(1, insertResult, "사용자 삽입이 성공해야 함");

        // 삽입 후 시간 기록
        long afterInsert = System.currentTimeMillis() / 1000;

        // 삽입된 데이터의 날짜 확인
        Map<String, Object> dateUser = jdbcTemplate.queryForMap(
            "SELECT ID, CREATED_AT, UPDATED_AT FROM USERS WHERE EMAIL = ?", "datetime@test.com");

        assertNotNull(dateUser.get("CREATED_AT"), "CREATED_AT이 설정되어야 함");
        assertNotNull(dateUser.get("UPDATED_AT"), "UPDATED_AT이 설정되어야 함");

        // CREATED_AT과 UPDATED_AT이 초기에는 동일해야 함
        assertEquals(dateUser.get("CREATED_AT"), dateUser.get("UPDATED_AT"), 
            "초기 생성 시 CREATED_AT과 UPDATED_AT이 동일해야 함");

        Long userId = ((Number) dateUser.get("ID")).longValue();

        // 잠시 대기 후 업데이트 수행
        Thread.sleep(1000);

        // 데이터 업데이트
        int updateResult = jdbcTemplate.update(
            "UPDATE USERS SET NICKNAME = ? WHERE ID = ?", "UpdatedDateTimeTest", userId);
        assertEquals(1, updateResult, "데이터 업데이트가 성공해야 함");

        // 업데이트된 데이터의 날짜 확인
        Map<String, Object> updatedDateUser = jdbcTemplate.queryForMap(
            "SELECT CREATED_AT, UPDATED_AT FROM USERS WHERE ID = ?", userId);

        // CREATED_AT은 변경되지 않고, UPDATED_AT은 변경되어야 함
        assertEquals(dateUser.get("CREATED_AT"), updatedDateUser.get("CREATED_AT"), 
            "CREATED_AT은 변경되지 않아야 함");
        assertNotEquals(dateUser.get("UPDATED_AT"), updatedDateUser.get("UPDATED_AT"), 
            "UPDATED_AT은 업데이트 시 변경되어야 함");

        log.info("날짜 컬럼의 기본값 및 자동 업데이트 확인 완료");
    }

    @Test
    @Order(7)
    @DisplayName("데이터베이스 성능 및 대용량 데이터 처리 테스트")
    void testDatabasePerformanceAndLargeDataHandling() {
        int batchSize = 100;
        String testPrefix = "performance_";
        
        long startTime = System.currentTimeMillis();

        // 대량 데이터 삽입
        for (int i = 0; i < batchSize; i++) {
            try {
                jdbcTemplate.update(
                    "INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES (?, ?, ?, ?)",
                    testPrefix + i + "@perf.com", "{noop}test", "PerfUser" + i, 1);
            } catch (Exception e) {
                log.warn("중복으로 인한 삽입 실패 (예상됨): {}", e.getMessage());
            }
        }

        long insertTime = System.currentTimeMillis() - startTime;

        // 삽입된 성능 테스트 데이터 수 확인
        Integer perfUserCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE ?", Integer.class, testPrefix + "%@perf.com");
        
        assertTrue(perfUserCount > 0, "성능 테스트 사용자가 삽입되어야 함");
        log.info("성능 테스트 데이터 삽입 완료 - {} 건, 소요시간: {}ms", perfUserCount, insertTime);

        // 대량 조회 성능 테스트
        startTime = System.currentTimeMillis();
        
        List<Map<String, Object>> perfUsers = jdbcTemplate.queryForList(
            "SELECT ID, EMAIL, NICKNAME, STATUS FROM USERS WHERE EMAIL LIKE ? ORDER BY ID LIMIT 50", 
            testPrefix + "%@perf.com");
        
        long queryTime = System.currentTimeMillis() - startTime;

        assertFalse(perfUsers.isEmpty(), "성능 테스트 데이터 조회 결과가 존재해야 함");
        assertTrue(queryTime < 5000, "대량 조회가 5초 이내에 완료되어야 함");

        // 집계 쿼리 성능 테스트
        startTime = System.currentTimeMillis();
        
        Map<String, Object> aggregateResult = jdbcTemplate.queryForMap(
            "SELECT COUNT(*) as user_count, MAX(ID) as max_id, MIN(ID) as min_id FROM USERS");
        
        long aggregateTime = System.currentTimeMillis() - startTime;

        assertTrue(((Number) aggregateResult.get("user_count")).intValue() > 6, 
            "총 사용자 수가 초기 데이터보다 많아야 함");
        assertTrue(aggregateTime < 2000, "집계 쿼리가 2초 이내에 완료되어야 함");

        log.info("데이터베이스 성능 테스트 완료 - 조회: {}ms, 집계: {}ms, 총 사용자: {}", 
            queryTime, aggregateTime, aggregateResult.get("user_count"));
    }
}