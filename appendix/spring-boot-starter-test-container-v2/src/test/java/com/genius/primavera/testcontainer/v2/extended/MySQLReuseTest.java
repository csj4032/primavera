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
 * 케이스: MySQL + REUSE + TestInstance.Lifecycle.PER_METHOD
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MYSQL},
    lifecycleMode = ContainerLifecycleMode.REUSE
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MySQL 컨테이너 - REUSE 라이프사이클")
class MySQLReuseTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("MySQL REUSE 첫 번째 실행")
    void firstExecution() {
        String connectionId = jdbcTemplate.queryForObject("SELECT CONNECTION_ID()", String.class);
        assertNotNull(connectionId);
        
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "reuse1@mysql.com", "{noop}password", "ReuseUser1");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        log.info("MySQL REUSE first: Connection {}, {} users", connectionId, count);
    }

    @Test
    @Order(2)
    @DisplayName("MySQL REUSE 두 번째 실행")
    void secondExecution() {
        String connectionId = jdbcTemplate.queryForObject("SELECT CONNECTION_ID()", String.class);
        assertNotNull(connectionId);
        
        // REUSE 모드에서는 이전 데이터가 유지될 수 있음
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertTrue(count >= 4); // 최소한 초기 데이터는 있어야 함
        
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "reuse2@mysql.com", "{noop}password", "ReuseUser2");
        
        count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        log.info("MySQL REUSE second: Connection {}, {} users", connectionId, count);
    }

    @Test
    @Order(3)
    @DisplayName("MySQL REUSE 성능 테스트")
    void performanceTest() {
        long startTime = System.currentTimeMillis();
        
        // 연속적인 쿼리 실행
        for (int i = 0; i < 50; i++) {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 5000, "Should complete 50 queries within 5 seconds");
        log.info("MySQL REUSE performance: 50 queries in {}ms", duration);
    }
}