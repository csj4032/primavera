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
 * 케이스: MySQL + PER_CLASS + TestInstance.Lifecycle.PER_CLASS
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MYSQL},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MySQL 컨테이너 - PER_CLASS 라이프사이클")
class MySQLPerClassTest extends AutoDynamicPropertySource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("MySQL PER_CLASS 초기화")
    void initMySQL() {
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        assertTrue(version.toLowerCase().contains("mysql"));
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, count);
        
        log.info("MySQL PER_CLASS initialized: version={}, users={}", 
                version.substring(0, Math.min(20, version.length())), count);
    }

    @Test
    @Order(2)
    @DisplayName("MySQL 대량 데이터 처리")
    void testMySQLBulkOperations() {
        // 대량 데이터 삽입
        for (int i = 1; i <= 100; i++) {
            jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                    String.format("bulk%03d@mysql.com", i), "{noop}password", String.format("BulkUser%03d", i));
        }
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(104, count); // 초기 4 + 추가 100
        
        // 통계 조회
        Integer bulkCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE 'bulk%@mysql.com'", Integer.class);
        assertEquals(100, bulkCount);
        
        log.info("MySQL bulk operations: {} total users, {} bulk users", count, bulkCount);
    }

    @Test
    @Order(3)
    @DisplayName("MySQL 복잡한 쿼리 테스트")
    void testMySQLComplexQueries() {
        // 조인 쿼리 (MySQL 특정 구문 사용)
        String complexQuery = """
            SELECT u.NICKNAME, r.NAME as ROLE_NAME, r.DESCRIPTION
            FROM USERS u 
            LEFT JOIN USER_ROLES ur ON u.ID = ur.USER_ID 
            LEFT JOIN ROLES r ON ur.ROLE_ID = r.ID 
            WHERE u.EMAIL LIKE 'bulk%@mysql.com' 
            LIMIT 5
            """;
        
        var results = jdbcTemplate.queryForList(complexQuery);
        assertFalse(results.isEmpty());
        
        // 집계 쿼리
        String aggregateQuery = """
            SELECT 
                COUNT(*) as total_users,
                COUNT(ur.USER_ID) as users_with_roles,
                COUNT(DISTINCT ur.ROLE_ID) as distinct_roles
            FROM USERS u 
            LEFT JOIN USER_ROLES ur ON u.ID = ur.USER_ID
            """;
        
        var aggregateResult = jdbcTemplate.queryForMap(aggregateQuery);
        assertNotNull(aggregateResult);
        
        log.info("MySQL complex queries: {} rows returned, aggregate: {}", 
                results.size(), aggregateResult);
    }
}