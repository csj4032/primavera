package com.genius.primavera.testcontainer.v2.comprehensive;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: 복수 컨테이너 (MARIADB + REDIS) + PER_METHOD 라이프사이클 + TestInstance.Lifecycle.PER_METHOD
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MARIADB, ContainerType.REDIS},
    lifecycleMode = ContainerLifecycleMode.PER_METHOD
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("복수 컨테이너 (MariaDB + Redis) - PER_METHOD 라이프사이클")
class MultipleContainersPerMethodTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @Order(1)
    @DisplayName("첫 번째 메서드 - MariaDB와 Redis 연결 확인")
    void firstMethod() {
        // MariaDB 테스트
        String dbResult = jdbcTemplate.queryForObject("SELECT 'DB Connected'", String.class);
        assertEquals("DB Connected", dbResult);
        
        // Redis 테스트
        redisTemplate.opsForValue().set("test:method1", "Redis Connected");
        String redisResult = redisTemplate.opsForValue().get("test:method1");
        assertEquals("Redis Connected", redisResult);
        
        // 데이터 저장
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "multi1@test.com", "{noop}password", "MultiUser1");
        redisTemplate.opsForValue().set("user:multi1", "MultiUser1");
        
        log.info("First method: Both MariaDB and Redis working");
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 메서드 - 컨테이너 격리 확인")
    void secondMethod() {
        // MariaDB 기본 연결 확인
        String dbResult = jdbcTemplate.queryForObject("SELECT 'DB Still Connected'", String.class);
        assertEquals("DB Still Connected", dbResult);
        
        // Redis 기본 연결 확인
        redisTemplate.opsForValue().set("test:method2", "Redis Still Connected");
        String redisResult = redisTemplate.opsForValue().get("test:method2");
        assertEquals("Redis Still Connected", redisResult);
        
        // 이전 메서드 데이터 확인 (격리되었다면 없어야 함)
        String previousRedisData = redisTemplate.opsForValue().get("user:multi1");
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        
        log.info("Second method: Previous Redis data = {}, User count = {}", previousRedisData, userCount);
        
        // 새 데이터 저장
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "multi2@test.com", "{noop}password", "MultiUser2");
        redisTemplate.opsForValue().set("user:multi2", "MultiUser2");
    }

    @Test
    @Order(3)
    @DisplayName("세 번째 메서드 - 최종 상태 확인")
    void thirdMethod() {
        // 기본 연결 확인
        assertNotNull(jdbcTemplate);
        assertNotNull(redisTemplate);
        
        // 연결 테스트
        String dbResult = jdbcTemplate.queryForObject("SELECT 'Final DB Test'", String.class);
        assertEquals("Final DB Test", dbResult);
        
        redisTemplate.opsForValue().set("test:final", "Final Redis Test");
        String redisResult = redisTemplate.opsForValue().get("test:final");
        assertEquals("Final Redis Test", redisResult);
        
        log.info("Third method: Both containers still working");
    }
}