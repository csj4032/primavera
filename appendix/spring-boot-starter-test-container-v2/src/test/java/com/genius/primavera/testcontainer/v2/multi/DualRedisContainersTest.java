package com.genius.primavera.testcontainer.v2.multi;

import com.genius.primavera.testcontainer.v2.*;
import com.genius.primavera.testcontainer.v2.EnableMultipleTestContainers.ContainerDefinition;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: 2개의 Redis 컨테이너와 각각 대응하는 RedisTemplate 설정
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("이중 Redis 컨테이너 - 다중 RedisTemplate")
class DualRedisContainersTest {

    @Container
    static GenericContainer<?> redis1 = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "redis1pass");

    @Container
    static GenericContainer<?> redis2 = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "redis2pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Redis 1 설정
        registry.add("app.redis1.host", redis1::getHost);
        registry.add("app.redis1.port", () -> redis1.getMappedPort(6379));
        registry.add("app.redis1.password", () -> "redis1pass");
        
        // Redis 2 설정
        registry.add("app.redis2.host", redis2::getHost);
        registry.add("app.redis2.port", () -> redis2.getMappedPort(6379));
        registry.add("app.redis2.password", () -> "redis2pass");
    }

    @TestConfiguration
    static class DualRedisConfig {
        
        @Bean("redis1ConnectionFactory")
        public RedisConnectionFactory redis1ConnectionFactory() {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redis1.getHost(), redis1.getMappedPort(6379));
            factory.setPassword("redis1pass");
            return factory;
        }
        
        @Bean("redis1StringTemplate")
        public StringRedisTemplate redis1StringTemplate(@Qualifier("redis1ConnectionFactory") RedisConnectionFactory connectionFactory) {
            StringRedisTemplate template = new StringRedisTemplate();
            template.setConnectionFactory(connectionFactory);
            return template;
        }
        
        @Bean("redis1Template")
        public RedisTemplate<String, Object> redis1Template(@Qualifier("redis1ConnectionFactory") RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(new StringRedisSerializer());
            return template;
        }
        
        @Bean("redis2ConnectionFactory")
        public RedisConnectionFactory redis2ConnectionFactory() {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redis2.getHost(), redis2.getMappedPort(6379));
            factory.setPassword("redis2pass");
            return factory;
        }
        
        @Bean("redis2StringTemplate")
        public StringRedisTemplate redis2StringTemplate(@Qualifier("redis2ConnectionFactory") RedisConnectionFactory connectionFactory) {
            StringRedisTemplate template = new StringRedisTemplate();
            template.setConnectionFactory(connectionFactory);
            return template;
        }
        
        @Bean("redis2Template")
        public RedisTemplate<String, Object> redis2Template(@Qualifier("redis2ConnectionFactory") RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(new StringRedisSerializer());
            return template;
        }
    }

    @Autowired
    @Qualifier("redis1StringTemplate")
    private StringRedisTemplate redis1StringTemplate;

    @Autowired
    @Qualifier("redis2StringTemplate")
    private StringRedisTemplate redis2StringTemplate;

    @Autowired
    @Qualifier("redis1Template")
    private RedisTemplate<String, Object> redis1Template;

    @Autowired
    @Qualifier("redis2Template")
    private RedisTemplate<String, Object> redis2Template;

    @Test
    @Order(1)
    @DisplayName("두 Redis 컨테이너 연결 확인")
    void testDualRedisConnections() {
        // Redis 1 연결 테스트
        redis1StringTemplate.opsForValue().set("test:redis1", "value1");
        String value1 = redis1StringTemplate.opsForValue().get("test:redis1");
        assertEquals("value1", value1);
        
        // Redis 2 연결 테스트
        redis2StringTemplate.opsForValue().set("test:redis2", "value2");
        String value2 = redis2StringTemplate.opsForValue().get("test:redis2");
        assertEquals("value2", value2);
        
        log.info("Dual Redis connections: redis1={}, redis2={}", value1, value2);
    }

    @Test
    @Order(2)
    @DisplayName("각 Redis에 독립적인 데이터 저장")
    void testIndependentDataStorage() {
        // Redis 1에 데이터 저장
        redis1StringTemplate.opsForValue().set("user:1", "John");
        redis1StringTemplate.opsForHash().put("user:1:profile", "name", "John Doe");
        redis1StringTemplate.opsForHash().put("user:1:profile", "email", "john@redis1.com");
        
        // Redis 2에 다른 데이터 저장
        redis2StringTemplate.opsForValue().set("user:1", "Jane");
        redis2StringTemplate.opsForHash().put("user:1:profile", "name", "Jane Smith");
        redis2StringTemplate.opsForHash().put("user:1:profile", "email", "jane@redis2.com");
        
        // 독립성 확인
        String redis1User = redis1StringTemplate.opsForValue().get("user:1");
        String redis2User = redis2StringTemplate.opsForValue().get("user:1");
        
        assertEquals("John", redis1User);
        assertEquals("Jane", redis2User);
        
        String redis1Email = (String) redis1StringTemplate.opsForHash().get("user:1:profile", "email");
        String redis2Email = (String) redis2StringTemplate.opsForHash().get("user:1:profile", "email");
        
        assertEquals("john@redis1.com", redis1Email);
        assertEquals("jane@redis2.com", redis2Email);
        
        log.info("Independent storage: redis1 user={}, email={}, redis2 user={}, email={}", 
                redis1User, redis1Email, redis2User, redis2Email);
    }

    @Test
    @Order(3)
    @DisplayName("각 Redis에서 서로 다른 데이터 구조 사용")
    void testDifferentDataStructures() {
        // Redis 1: 리스트 기반 큐 시스템
        redis1StringTemplate.opsForList().rightPush("queue:tasks", "task1", "task2", "task3");
        Long queueSize = redis1StringTemplate.opsForList().size("queue:tasks");
        String firstTask = redis1StringTemplate.opsForList().leftPop("queue:tasks");
        
        assertEquals(3L, queueSize);
        assertEquals("task1", firstTask);
        
        // Redis 2: 정렬된 셋 기반 리더보드
        redis2StringTemplate.opsForZSet().add("leaderboard", "player1", 100.0);
        redis2StringTemplate.opsForZSet().add("leaderboard", "player2", 85.0);
        redis2StringTemplate.opsForZSet().add("leaderboard", "player3", 95.0);
        
        Long leaderboardSize = redis2StringTemplate.opsForZSet().size("leaderboard");
        var topPlayer = redis2StringTemplate.opsForZSet().reverseRange("leaderboard", 0, 0);
        
        assertEquals(3L, leaderboardSize);
        assertTrue(topPlayer.contains("player1"));
        
        log.info("Different structures: redis1 queue size={}, first task={}, redis2 leaderboard size={}, top player={}", 
                queueSize, firstTask, leaderboardSize, topPlayer.iterator().next());
    }

    @Test
    @Order(4)
    @DisplayName("두 Redis 간 데이터 동기화 시뮬레이션")
    void testDataSynchronization() {
        // Redis 1에서 데이터 생성
        redis1StringTemplate.opsForHash().put("product:123", "name", "Laptop");
        redis1StringTemplate.opsForHash().put("product:123", "price", "1500");
        redis1StringTemplate.opsForHash().put("product:123", "stock", "10");
        
        // Redis 1에서 Redis 2로 데이터 복사 (동기화 시뮬레이션)
        var productData = redis1StringTemplate.opsForHash().entries("product:123");
        productData.forEach((key, value) -> 
            redis2StringTemplate.opsForHash().put("product:123", key, value)
        );
        
        // 동기화 검증
        String redis1Name = (String) redis1StringTemplate.opsForHash().get("product:123", "name");
        String redis2Name = (String) redis2StringTemplate.opsForHash().get("product:123", "name");
        
        assertEquals(redis1Name, redis2Name);
        assertEquals("Laptop", redis2Name);
        
        // Redis 2에서 업데이트
        redis2StringTemplate.opsForHash().put("product:123", "stock", "8");
        
        String redis1Stock = (String) redis1StringTemplate.opsForHash().get("product:123", "stock");
        String redis2Stock = (String) redis2StringTemplate.opsForHash().get("product:123", "stock");
        
        assertEquals("10", redis1Stock); // 원본 유지
        assertEquals("8", redis2Stock);  // 업데이트됨
        
        log.info("Data sync: redis1 stock={}, redis2 stock={}, sync verified={}", 
                redis1Stock, redis2Stock, redis1Name.equals(redis2Name));
    }

    @Test
    @Order(5)
    @DisplayName("두 Redis 성능 비교")
    void testPerformanceComparison() {
        // Redis 1 성능 테스트
        long redis1Start = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            redis1StringTemplate.opsForValue().set("perf1:" + i, "value" + i);
        }
        long redis1InsertTime = System.currentTimeMillis() - redis1Start;
        
        redis1Start = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            redis1StringTemplate.opsForValue().get("perf1:" + i);
        }
        long redis1ReadTime = System.currentTimeMillis() - redis1Start;
        
        // Redis 2 성능 테스트
        long redis2Start = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            redis2StringTemplate.opsForValue().set("perf2:" + i, "value" + i);
        }
        long redis2InsertTime = System.currentTimeMillis() - redis2Start;
        
        redis2Start = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            redis2StringTemplate.opsForValue().get("perf2:" + i);
        }
        long redis2ReadTime = System.currentTimeMillis() - redis2Start;
        
        assertTrue(redis1InsertTime < 5000, "Redis1 500 inserts should complete within 5 seconds");
        assertTrue(redis1ReadTime < 5000, "Redis1 500 reads should complete within 5 seconds");
        assertTrue(redis2InsertTime < 5000, "Redis2 500 inserts should complete within 5 seconds");
        assertTrue(redis2ReadTime < 5000, "Redis2 500 reads should complete within 5 seconds");
        
        log.info("Performance comparison: redis1 insert={}ms, read={}ms, redis2 insert={}ms, read={}ms", 
                redis1InsertTime, redis1ReadTime, redis2InsertTime, redis2ReadTime);
    }
}