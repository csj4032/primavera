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
 * 케이스: 복수 컨테이너 (MARIADB + REDIS) + PER_CLASS 라이프사이클 + TestInstance.Lifecycle.PER_CLASS
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MARIADB, ContainerType.REDIS},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("복수 컨테이너 (MariaDB + Redis) - PER_CLASS 라이프사이클")
class MultipleContainersPerClassTest extends AutoDynamicPropertySource {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @Order(1)
    @DisplayName("첫 번째 메서드 - 복수 컨테이너 초기 설정")
    void firstMethod() {
        // MariaDB 초기 데이터 확인
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, initialCount); // 초기 4명
        
        // 데이터 추가
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "perclass.multi1@test.com", "{noop}password", "PerClassMultiUser1");
        
        // Redis 데이터 저장
        redisTemplate.opsForValue().set("perclass:user1", "PerClassMultiUser1");
        redisTemplate.opsForValue().set("perclass:counter", "1");
        
        // 상태 확인
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, finalCount);
        
        String redisValue = redisTemplate.opsForValue().get("perclass:user1");
        assertEquals("PerClassMultiUser1", redisValue);
        
        log.info("PER_CLASS Multi First: DB users = {}, Redis value = {}", finalCount, redisValue);
    }

    @Test
    @Order(2)
    @DisplayName("두 번째 메서드 - 데이터 유지 및 추가")
    void secondMethod() {
        // 이전 메서드 데이터 유지 확인
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, userCount); // 이전 메서드에서 추가한 데이터 유지
        
        String redisValue = redisTemplate.opsForValue().get("perclass:user1");
        assertEquals("PerClassMultiUser1", redisValue); // Redis 데이터도 유지
        
        // 추가 데이터 저장
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "perclass.multi2@test.com", "{noop}password", "PerClassMultiUser2");
        
        redisTemplate.opsForValue().set("perclass:user2", "PerClassMultiUser2");
        redisTemplate.opsForValue().set("perclass:counter", "2");
        
        // 최종 상태 확인
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, finalCount);
        
        String counter = redisTemplate.opsForValue().get("perclass:counter");
        assertEquals("2", counter);
        
        log.info("PER_CLASS Multi Second: DB users = {}, Redis counter = {}", finalCount, counter);
    }

    @Test
    @Order(3)
    @DisplayName("세 번째 메서드 - 누적 데이터 최종 확인")
    void thirdMethod() {
        // 모든 이전 데이터 유지 확인
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(6, userCount);
        
        // 특정 사용자들 존재 확인
        String user1 = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?",
                String.class, "perclass.multi1@test.com");
        assertEquals("PerClassMultiUser1", user1);
        
        String user2 = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE EMAIL = ?",
                String.class, "perclass.multi2@test.com");
        assertEquals("PerClassMultiUser2", user2);
        
        // Redis 데이터 확인
        String redisUser1 = redisTemplate.opsForValue().get("perclass:user1");
        String redisUser2 = redisTemplate.opsForValue().get("perclass:user2");
        String counter = redisTemplate.opsForValue().get("perclass:counter");
        
        assertEquals("PerClassMultiUser1", redisUser1);
        assertEquals("PerClassMultiUser2", redisUser2);
        assertEquals("2", counter);
        
        log.info("PER_CLASS Multi Final: All data preserved - DB users = {}, Redis entries = 3", userCount);
    }
}