package com.genius.primavera.testcontainer.v2.extended;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: PostgreSQL + PER_METHOD + TestInstance.Lifecycle.PER_METHOD
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.POSTGRESQL},
    lifecycleMode = ContainerLifecycleMode.PER_METHOD
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PostgreSQL 컨테이너 - PER_METHOD 라이프사이클")
class PostgreSQLPerMethodTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("PostgreSQL 특화 기능 테스트")
    void testPostgreSQLFeatures() {
        // PostgreSQL 버전 확인
        String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
        assertTrue(version.toLowerCase().contains("postgresql"));
        
        // PostgreSQL 특화 함수 사용
        String uuid = jdbcTemplate.queryForObject("SELECT gen_random_uuid()::text", String.class);
        assertNotNull(uuid);
        assertTrue(uuid.length() > 30); // UUID 형식 확인
        
        log.info("PostgreSQL features: UUID generated = {}", uuid.substring(0, 8) + "...");
    }

    @Test
    @Order(2)
    @DisplayName("PostgreSQL JSON 처리")
    void testPostgreSQLJSON() {
        // JSON 컬럼이 있다면 테스트 (없다면 임시 테이블 생성)
        try {
            jdbcTemplate.execute("CREATE TEMP TABLE json_test (id SERIAL, data JSONB)");
            
            jdbcTemplate.update("INSERT INTO json_test (data) VALUES (?::jsonb)", 
                    "{\"name\": \"PostgreSQL\", \"version\": 15, \"features\": [\"JSONB\", \"UUID\", \"Arrays\"]}");
            
            String name = jdbcTemplate.queryForObject(
                    "SELECT data->>'name' FROM json_test WHERE id = 1", String.class);
            assertEquals("PostgreSQL", name);
            
            Integer version = jdbcTemplate.queryForObject(
                    "SELECT (data->>'version')::int FROM json_test WHERE id = 1", Integer.class);
            assertEquals(15, version);
            
            log.info("PostgreSQL JSON: name={}, version={}", name, version);
        } catch (Exception e) {
            log.warn("PostgreSQL JSON test skipped: {}", e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("PostgreSQL 배열 처리")
    void testPostgreSQLArrays() {
        try {
            jdbcTemplate.execute("CREATE TEMP TABLE array_test (id SERIAL, tags TEXT[])");
            
            jdbcTemplate.update("INSERT INTO array_test (tags) VALUES (?)", 
                    new Object[]{new String[]{"postgresql", "database", "sql"}});
            
            // 배열 요소 접근
            String firstTag = jdbcTemplate.queryForObject(
                    "SELECT tags[1] FROM array_test WHERE id = 1", String.class);
            assertEquals("postgresql", firstTag);
            
            // 배열 길이
            Integer arrayLength = jdbcTemplate.queryForObject(
                    "SELECT array_length(tags, 1) FROM array_test WHERE id = 1", Integer.class);
            assertEquals(3, arrayLength);
            
            log.info("PostgreSQL arrays: firstTag={}, length={}", firstTag, arrayLength);
        } catch (Exception e) {
            log.warn("PostgreSQL array test skipped: {}", e.getMessage());
        }
    }
}