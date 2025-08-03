package com.genius.primavera.testcontainer.v2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(containers = {ContainerType.MARIADB})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MariaDB TestContainer V2 통합 테스트")
class MariaDBContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("데이터베이스 연결 테스트")
    void testDatabaseConnection() {
        String result = jdbcTemplate.queryForObject("SELECT 'Hello MariaDB V2!'", String.class);
        assertEquals("Hello MariaDB V2!", result);
        log.info("MariaDB connection test passed: {}", result);
    }

    @Test
    @Order(2)
    @DisplayName("초기 데이터 검증")
    void testInitialData() {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, userCount, "초기 사용자 데이터가 4개여야 합니다");
        
        Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ROLES", Integer.class);
        assertEquals(3, roleCount, "초기 권한 데이터가 3개여야 합니다");
        
        log.info("Initial data validation passed - Users: {}, Roles: {}", userCount, roleCount);
    }

    @Test
    @Order(3)
    @DisplayName("데이터 삽입/조회 테스트")
    void testDataInsertAndSelect() {
        // 새 사용자 삽입
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "testuser@v2.com", "{noop}password", "TestUserV2");
        
        // 삽입된 데이터 조회
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?", 
                String.class, 
                "testuser@v2.com");
        
        assertEquals("TestUserV2", nickname);
        log.info("Data insert/select test passed: {}", nickname);
    }
}