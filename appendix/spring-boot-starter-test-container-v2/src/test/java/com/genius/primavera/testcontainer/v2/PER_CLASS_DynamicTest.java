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
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // PER_CLASS 모드
@
@EnableTestContainers(containers = {ContainerType.MARIADB})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PER_CLASS + DynamicPropertySource 통합 테스트")
class PER_CLASS_DynamicTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        log.info("=== @DynamicPropertySource 실행 ===");
        DynamicContainerSupport.configureContainers(PER_CLASS_DynamicTest.class, registry);
        log.info("=== @DynamicPropertySource 완료 ===");
    }

    @Test
    @Order(1)
    @DisplayName("데이터베이스 연결 및 초기 데이터 확인")
    void testDatabaseConnectionAndInitialData() {
        log.info("=== 첫 번째 테스트 시작 ===");
        
        // 연결 테스트
        String result = jdbcTemplate.queryForObject("SELECT 'PER_CLASS + DynamicPropertySource 성공!'", String.class);
        assertEquals("PER_CLASS + DynamicPropertySource 성공!", result);
        
        // 초기 데이터 확인
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, userCount, "초기 사용자 데이터가 4개여야 합니다");
        
        // 새 데이터 삽입
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "dynamic1@test.com", "{noop}password", "Dynamic1");
        
        Integer afterInsert = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, afterInsert);
        
        log.info("첫 번째 테스트 완료 - 총 사용자 수: {}", afterInsert);
    }

    @Test
    @Order(2)
    @DisplayName("데이터 지속성 확인")
    void testDataPersistence() {
        log.info("=== 두 번째 테스트 시작 ===");
        
        // 첫 번째 테스트에서 추가한 데이터가 유지되는지 확인
        String nickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?", 
                String.class, 
                "dynamic1@test.com");
        
        assertEquals("Dynamic1", nickname, "PER_CLASS 모드에서 데이터가 유지되어야 합니다");
        
        // 추가 데이터 삽입
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "dynamic2@test.com", "{noop}password", "Dynamic2");
        
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, userCount);
        
        log.info("두 번째 테스트 완료 - 총 사용자 수: {}", userCount);
    }

    @Test
    @Order(3)
    @DisplayName("최종 데이터 상태 검증")
    void testFinalDataState() {
        log.info("=== 세 번째 테스트 시작 ===");
        
        // 모든 추가된 데이터가 존재하는지 확인
        Integer dynamicUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE 'dynamic%'", 
                Integer.class);
        
        assertEquals(2, dynamicUsers, "Dynamic 사용자 2명이 존재해야 합니다");
        
        // 전체 사용자 수 확인
        Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, totalUsers, "총 사용자는 6명이어야 합니다 (초기 4명 + 추가 2명)");
        
        log.info("세 번째 테스트 완료 - PER_CLASS + DynamicPropertySource 모든 테스트 성공!");
    }
    
    @AfterAll
    static void cleanup() {
        log.info("=== 테스트 완료 후 정리 ===");
        // 필요시 정리 작업 수행
    }
}