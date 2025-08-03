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
 * 케이스: MySQL + PER_METHOD + TestInstance.Lifecycle.PER_METHOD (기본값)
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MYSQL},
    lifecycleMode = ContainerLifecycleMode.PER_METHOD
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MySQL 컨테이너 - PER_METHOD 라이프사이클")
class MySQLPerMethodDefaultTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("MySQL 연결 및 버전 확인")
    void testMySQLConnection() {
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        assertNotNull(version);
        assertTrue(version.toLowerCase().contains("mysql"));
        log.info("MySQL version: {}", version.substring(0, Math.min(50, version.length())));
    }

    @Test
    @Order(2)
    @DisplayName("MySQL 메서드별 격리 테스트")
    void testMySQLMethodIsolation() {
        // 초기 데이터 확인
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, count);
        
        // 데이터 추가
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "mysql@test.com", "{noop}password", "MySQLUser");
        
        count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count);
        log.info("MySQL method isolation: {} users", count);
    }

    @Test
    @Order(3)
    @DisplayName("MySQL 트랜잭션 및 롤백 테스트")
    void testMySQLTransaction() {
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        
        try {
            jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                    "invalid-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-very-long-email@test.com", 
                    "{noop}password", "InvalidUser");
        } catch (Exception e) {
            log.info("Expected error for long email: {}", e.getMessage());
        }
        
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(initialCount, finalCount); // 데이터가 롤백되어야 함
        log.info("MySQL transaction test: Count maintained at {}", finalCount);
    }
}