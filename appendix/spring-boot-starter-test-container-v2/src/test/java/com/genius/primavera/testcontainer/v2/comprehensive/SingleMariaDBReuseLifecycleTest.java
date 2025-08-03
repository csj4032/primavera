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
 * 케이스: 단일 MARIADB + REUSE 라이프사이클 + TestInstance.Lifecycle.PER_METHOD (기본값)
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MARIADB},
    lifecycleMode = ContainerLifecycleMode.REUSE
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("단일 MariaDB - REUSE 라이프사이클 - PER_METHOD 인스턴스")
class SingleMariaDBReuseLifecycleTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("첫 번째 메서드 - REUSE 컨테이너 연결 확인")
    void firstMethod() {
        String result = jdbcTemplate.queryForObject("SELECT 'REUSE Method1'", String.class);
        assertEquals("REUSE Method1", result);
        
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "reuse1@test.com", "{noop}password", "ReuseUser1");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        log.info("REUSE First method: Total users = {}", count);
        assertTrue(count >= 5); // 초기 데이터 + 새 데이터
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 메서드 - REUSE 컨테이너 데이터 확인")
    void secondMethod() {
        // REUSE 모드에서는 컨테이너가 재사용되므로 데이터가 유지될 수 있음
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        log.info("REUSE Second method: Total users = {}", count);
        assertTrue(count >= 4); // 최소한 초기 데이터는 있어야 함
        
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "reuse2@test.com", "{noop}password", "ReuseUser2");
    }

    @Test
    @Order(3)
    @DisplayName("세 번째 메서드 - REUSE 컨테이너 최종 확인")
    void thirdMethod() {
        String result = jdbcTemplate.queryForObject("SELECT 'REUSE Final'", String.class);
        assertEquals("REUSE Final", result);
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        log.info("REUSE Third method: Total users = {}", count);
        assertTrue(count >= 4);
    }
}