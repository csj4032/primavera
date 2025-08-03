package com.genius.primavera.testcontainer.v2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(containers = {ContainerType.MARIADB})
@DisplayName("PER_METHOD 모드 간단한 테스트")
class SimplePerMethodTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("데이터베이스 연결 테스트")
    void testDatabaseConnection() {
        String result = jdbcTemplate.queryForObject("SELECT 'Hello TestContainer V2!'", String.class);
        assertEquals("Hello TestContainer V2!", result);
        log.info("Database connection successful");
    }

    @Test
    @DisplayName("초기 데이터 확인")
    void testInitialData() {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertTrue(userCount >= 4, "Should have at least 4 initial users, found: " + userCount);
        log.info("Found {} initial users", userCount);
    }
}