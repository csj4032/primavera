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
 * 케이스: 단일 MARIADB + PER_CLASS 라이프사이클 + TestInstance.Lifecycle.PER_CLASS
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MARIADB},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("단일 MariaDB - PER_CLASS 라이프사이클 - PER_CLASS 인스턴스")
class SingleMariaDBPerClassLifecycleTest extends AutoDynamicPropertySource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("첫 번째 메서드 - 데이터베이스 연결 확인")
    void firstMethod() {
        String result = jdbcTemplate.queryForObject("SELECT 'PER_CLASS Method1'", String.class);
        assertEquals("PER_CLASS Method1", result);
        
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "perclass1@test.com", "{noop}password", "PerClass1User");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count); // 초기 4명 + 추가 1명
        log.info("PER_CLASS First method: Total users = {}", count);
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 메서드 - 데이터 유지 확인")
    void secondMethod() {
        // PER_CLASS에서는 이전 메서드 데이터가 유지되어야 함
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count); // 이전 메서드에서 추가한 데이터 유지
        
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "perclass2@test.com", "{noop}password", "PerClass2User");
        
        count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, count); // 초기 4명 + 추가 2명
        log.info("PER_CLASS Second method: Total users = {}", count);
    }

    @Test
    @Order(3)
    @DisplayName("세 번째 메서드 - 누적 데이터 확인")
    void thirdMethod() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, count); // 모든 이전 데이터 유지
        log.info("PER_CLASS Third method: Total users = {}", count);
        
        // 특정 사용자 존재 확인
        String nickname1 = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?", 
                String.class, "perclass1@test.com");
        assertEquals("PerClass1User", nickname1);
        
        String nickname2 = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?", 
                String.class, "perclass2@test.com");
        assertEquals("PerClass2User", nickname2);
    }
}