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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableTestContainers(containers = {ContainerType.MARIADB})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PER_CLASS 모드 간단한 테스트")
class SimplePerClassTest extends AutoDynamicPropertySource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("첫 번째 테스트 - 데이터 삽입")
    void firstTest() {
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "test1@v2.com", "{noop}password", "Test1");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count);
        log.info("First test: inserted user, total count = {}", count);
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 테스트 - 데이터 유지 확인")
    void secondTest() {
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?", 
                String.class, 
                "test1@v2.com");
        
        assertEquals("Test1", nickname);
        log.info("Second test: data persisted from first test");
    }
}