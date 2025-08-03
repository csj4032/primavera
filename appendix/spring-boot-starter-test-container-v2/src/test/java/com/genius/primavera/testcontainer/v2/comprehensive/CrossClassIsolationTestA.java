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
 * 케이스: 클래스 간 격리 테스트 A - PER_CLASS 라이프사이클
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
@DisplayName("클래스 간 격리 테스트 A")
class CrossClassIsolationTestA extends AutoDynamicPropertySource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("클래스 A - 초기 데이터 확인")
    void testClassAInitialData() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, count); // 초기 4명만 있어야 함
        
        log.info("Class A Initial: {} users", count);
    }

    @Test
    @Order(2)
    @DisplayName("클래스 A - 특별한 데이터 추가")
    void testClassADataInsertion() {
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "class-a@test.com", "{noop}password", "ClassAUser");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count);
        
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?",
                String.class, "class-a@test.com");
        assertEquals("ClassAUser", nickname);
        
        log.info("Class A Added: {} users, added ClassAUser", count);
    }

    @Test
    @Order(3)
    @DisplayName("클래스 A - 데이터 유지 확인")
    void testClassADataPersistence() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count); // 이전 메서드에서 추가한 데이터 유지
        
        // 클래스 A만의 데이터가 있는지 확인
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?",
                String.class, "class-a@test.com");
        assertEquals("ClassAUser", nickname);
        
        log.info("Class A Final: {} users, ClassAUser maintained", count);
    }
}