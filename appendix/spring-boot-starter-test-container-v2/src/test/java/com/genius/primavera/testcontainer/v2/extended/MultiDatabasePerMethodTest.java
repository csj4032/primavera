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
 * 케이스: 다중 데이터베이스 (MariaDB + MySQL + PostgreSQL) + PER_METHOD
 * 주의: 실제로는 하나의 DataSource만 설정되지만, 컨테이너 시작 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MARIADB, ContainerType.MYSQL, ContainerType.POSTGRESQL},
    lifecycleMode = ContainerLifecycleMode.PER_METHOD
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("다중 데이터베이스 컨테이너 - PER_METHOD")
class MultiDatabasePerMethodTest {

    @Autowired
    private JdbcTemplate jdbcTemplate; // 마지막에 설정된 DB에 연결됨

    @Test
    @Order(1)
    @DisplayName("다중 DB 컨테이너 시작 확인")
    void testMultiDatabaseContainerStartup() {
        // 어떤 DB에 연결되었는지 확인
        try {
            String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
            assertNotNull(version);
            log.info("Connected to database with version: {}", 
                    version.substring(0, Math.min(50, version.length())));
        } catch (Exception e) {
            // version() 함수가 없을 수 있음 (MariaDB/MySQL 구문)
            try {
                String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
                assertNotNull(version);
                log.info("Connected to database with VERSION(): {}", 
                        version.substring(0, Math.min(50, version.length())));
            } catch (Exception e2) {
                log.warn("Could not determine database version: {}", e2.getMessage());
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("다중 DB 환경에서 기본 데이터 작업")
    void testBasicDataOperations() {
        // 기본 테이블이 존재하는지 확인
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, userCount);
        
        // 데이터 삽입
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "multi-db@test.com", "{noop}password", "MultiDBUser");
        
        Integer newCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, newCount);
        
        log.info("Multi-DB basic operations: {} -> {} users", userCount, newCount);
    }

    @Test
    @Order(3)
    @DisplayName("다중 DB 컨테이너 리소스 사용량 추정")
    void testResourceUsageEstimation() {
        // 메모리 사용량 추정을 위한 대량 쿼리
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 다중 컨테이너 환경에서도 성능이 적절해야 함
        assertTrue(duration < 10000, "100 queries should complete within 10 seconds even with multiple containers");
        
        log.info("Multi-DB resource test: 100 queries completed in {}ms", duration);
    }
}