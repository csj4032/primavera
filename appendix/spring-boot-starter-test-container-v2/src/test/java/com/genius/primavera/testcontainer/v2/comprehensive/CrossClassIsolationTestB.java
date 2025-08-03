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
 * 케이스: 클래스 간 격리 테스트 B - PER_CLASS 라이프사이클
 * 클래스 A의 데이터가 보이지 않아야 함 (독립적인 컨테이너)
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
@DisplayName("클래스 간 격리 테스트 B")
class CrossClassIsolationTestB extends AutoDynamicPropertySource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("클래스 B - 초기 데이터 확인 (클래스 A 데이터 미포함)")
    void testClassBInitialData() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, count); // 클래스 A의 데이터가 보이지 않아야 함
        
        // 클래스 A의 데이터가 없는지 확인
        Integer classAUserCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?",
                Integer.class, "class-a@test.com");
        assertEquals(0, classAUserCount); // 클래스 A의 데이터가 없어야 함
        
        log.info("Class B Initial: {} users, no Class A data", count);
    }

    @Test
    @Order(2)
    @DisplayName("클래스 B - 자체 데이터 추가")
    void testClassBDataInsertion() {
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "class-b@test.com", "{noop}password", "ClassBUser");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count);
        
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?",
                String.class, "class-b@test.com");
        assertEquals("ClassBUser", nickname);
        
        // 여전히 클래스 A 데이터는 없어야 함
        Integer classAUserCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?",
                Integer.class, "class-a@test.com");
        assertEquals(0, classAUserCount);
        
        log.info("Class B Added: {} users, added ClassBUser, still no Class A data", count);
    }

    @Test
    @Order(3)
    @DisplayName("클래스 B - 격리 상태 최종 확인")
    void testClassBIsolation() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, count);
        
        // 클래스 B 데이터 존재 확인
        String classBNickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?",
                String.class, "class-b@test.com");
        assertEquals("ClassBUser", classBNickname);
        
        // 클래스 A 데이터 미존재 확인
        Integer classAUserCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?",
                Integer.class, "class-a@test.com");
        assertEquals(0, classAUserCount);
        
        log.info("Class B Final: {} users, ClassBUser exists, Class A data isolated", count);
    }
}