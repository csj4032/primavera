package com.genius.primavera.testcontainer.v2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // PER_CLASS 테스트!
@EnableTestContainers(containers = {ContainerType.MARIADB}) // @DynamicPropertySource와 함께 사용
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PER_CLASS 라이프사이클 테스트 - V2 with DynamicPropertySource")
class PER_CLASS_LifecycleTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        log.info("=== DynamicPropertySource 설정 시작 ===");
        DynamicContainerSupport.configureContainers(PER_CLASS_LifecycleTest.class, registry);
        log.info("=== DynamicPropertySource 설정 완료 ===");
    }

    @Test
    @Order(1)
    @DisplayName("첫 번째 테스트 - 데이터 삽입")
    void firstTest() {
        log.info("=== PER_CLASS 첫 번째 테스트 시작 ===");
        
        // 초기 사용자 수 확인
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        log.info("초기 사용자 수: {}", initialCount);
        
        // 새 사용자 추가
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "perclass1@v2.com", "{noop}password", "PerClass1");
        
        Integer afterInsert = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(initialCount + 1, afterInsert);
        
        log.info("첫 번째 테스트 완료 - 사용자 수: {}", afterInsert);
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 테스트 - 데이터 유지 확인")
    void secondTest() {
        log.info("=== PER_CLASS 두 번째 테스트 시작 ===");
        
        // 첫 번째 테스트에서 추가한 데이터가 유지되는지 확인
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?", 
                String.class, 
                "perclass1@v2.com");
        
        assertEquals("PerClass1", nickname, "PER_CLASS 모드에서 데이터가 유지되어야 합니다");
        
        // 또 다른 사용자 추가
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "perclass2@v2.com", "{noop}password", "PerClass2");
        
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        log.info("두 번째 테스트 완료 - 총 사용자 수: {}", userCount);
    }

    @Test
    @Order(3)
    @DisplayName("세 번째 테스트 - 최종 데이터 확인")
    void thirdTest() {
        log.info("=== PER_CLASS 세 번째 테스트 시작 ===");
        
        // 모든 데이터가 유지되는지 확인
        Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertTrue(totalUsers >= 6, "PER_CLASS 모드에서 모든 데이터가 유지되어야 합니다"); // 초기 4개 + 추가 2개
        
        // 추가한 사용자들이 모두 존재하는지 확인
        Integer addedUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL IN (?, ?)", 
                Integer.class, 
                "perclass1@v2.com", "perclass2@v2.com");
        
        assertEquals(2, addedUsers, "추가한 사용자 2명이 모두 존재해야 합니다");
        
        log.info("세 번째 테스트 완료 - PER_CLASS 라이프사이클 정상 동작 확인");
    }
}