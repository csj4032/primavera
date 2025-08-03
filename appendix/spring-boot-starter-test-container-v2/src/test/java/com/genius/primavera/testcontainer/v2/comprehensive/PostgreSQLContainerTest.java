package com.genius.primavera.testcontainer.v2.comprehensive;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: 단일 POSTGRESQL + PER_CLASS 라이프사이클 + TestInstance.Lifecycle.PER_CLASS
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.POSTGRESQL},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("단일 PostgreSQL - PER_CLASS 라이프사이클")
class PostgreSQLContainerTest extends AutoDynamicPropertySource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("PostgreSQL 연결 및 초기 데이터 확인")
    void testPostgreSQLConnection() {
        // PostgreSQL 특정 쿼리로 연결 확인
        String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
        assertNotNull(version);
        assertTrue(version.toLowerCase().contains("postgresql"));
        
        // 초기 데이터 확인
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, userCount);
        
        log.info("PostgreSQL version: {}, Initial users: {}", version.substring(0, 50), userCount);
    }

    @Test
    @Order(2)
    @DisplayName("PostgreSQL 데이터 삽입 및 조회")
    void testPostgreSQLDataOperations() {
        // 데이터 삽입
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "postgres@test.com", "{noop}password", "PostgresUser");
        
        // 데이터 조회
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?",
                String.class, "postgres@test.com");
        assertEquals("PostgresUser", nickname);
        
        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, totalCount);
        
        log.info("PostgreSQL data operations: Added user, total count = {}", totalCount);
    }

    @Test
    @Order(3)
    @DisplayName("PostgreSQL PER_CLASS 데이터 유지 확인")
    void testPostgreSQLDataPersistence() {
        // 이전 메서드에서 삽입한 데이터가 유지되는지 확인
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count);
        
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?",
                String.class, "postgres@test.com");
        assertEquals("PostgresUser", nickname);
        
        log.info("PostgreSQL PER_CLASS persistence: Data maintained, count = {}", count);
    }
}