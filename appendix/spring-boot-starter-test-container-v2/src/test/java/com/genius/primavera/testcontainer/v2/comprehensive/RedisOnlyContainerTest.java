package com.genius.primavera.testcontainer.v2.comprehensive;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: 단일 REDIS + PER_METHOD 라이프사이클 + TestInstance.Lifecycle.PER_METHOD
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.REDIS},
    lifecycleMode = ContainerLifecycleMode.PER_METHOD
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("단일 Redis - PER_METHOD 라이프사이클")
class RedisOnlyContainerTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @Order(1)
    @DisplayName("Redis 연결 및 기본 작업")
    void testRedisBasicOperations() {
        // 기본 문자열 작업
        redisTemplate.opsForValue().set("test:key1", "value1");
        String value = redisTemplate.opsForValue().get("test:key1");
        assertEquals("value1", value);
        
        // 숫자 작업
        redisTemplate.opsForValue().set("test:counter", "10");
        Long incremented = redisTemplate.opsForValue().increment("test:counter");
        assertEquals(11L, incremented);
        
        log.info("Redis basic operations: String and counter working");
    }

    @Test
    @Order(2)
    @DisplayName("Redis 리스트 및 셋 작업")
    void testRedisCollectionOperations() {
        // 리스트 작업
        redisTemplate.opsForList().rightPush("test:list", "item1");
        redisTemplate.opsForList().rightPush("test:list", "item2");
        redisTemplate.opsForList().rightPush("test:list", "item3");
        
        Long listSize = redisTemplate.opsForList().size("test:list");
        assertEquals(3L, listSize);
        
        String firstItem = redisTemplate.opsForList().leftPop("test:list");
        assertEquals("item1", firstItem);
        
        // 셋 작업
        redisTemplate.opsForSet().add("test:set", "member1", "member2", "member3");
        Long setSize = redisTemplate.opsForSet().size("test:set");
        assertEquals(3L, setSize);
        
        Boolean isMember = redisTemplate.opsForSet().isMember("test:set", "member2");
        assertTrue(isMember);
        
        log.info("Redis collections: List size = {}, Set size = {}", listSize, setSize);
    }

    @Test
    @Order(3)
    @DisplayName("Redis TTL 및 만료 처리")
    void testRedisTTLOperations() throws InterruptedException {
        // TTL 설정
        redisTemplate.opsForValue().set("test:expiring", "will_expire", 2, TimeUnit.SECONDS);
        
        // 즉시 확인
        String value = redisTemplate.opsForValue().get("test:expiring");
        assertEquals("will_expire", value);
        
        // TTL 확인
        Long ttl = redisTemplate.getExpire("test:expiring");
        assertTrue(ttl > 0 && ttl <= 2);
        
        // 만료까지 대기
        Thread.sleep(2100);
        
        // 만료 후 확인
        String expiredValue = redisTemplate.opsForValue().get("test:expiring");
        assertNull(expiredValue);
        
        log.info("Redis TTL: Key expired successfully after 2 seconds");
    }

    @Test
    @Order(4)
    @DisplayName("Redis 해시 작업")
    void testRedisHashOperations() {
        // 해시 작업
        redisTemplate.opsForHash().put("test:hash", "field1", "value1");
        redisTemplate.opsForHash().put("test:hash", "field2", "value2");
        redisTemplate.opsForHash().put("test:hash", "field3", "value3");
        
        String hashValue = (String) redisTemplate.opsForHash().get("test:hash", "field2");
        assertEquals("value2", hashValue);
        
        Long hashSize = redisTemplate.opsForHash().size("test:hash");
        assertEquals(3L, hashSize);
        
        Set<Object> fields = redisTemplate.opsForHash().keys("test:hash");
        assertEquals(3, fields.size());
        assertTrue(fields.contains("field1"));
        assertTrue(fields.contains("field2"));
        assertTrue(fields.contains("field3"));
        
        log.info("Redis hash: Size = {}, Fields = {}", hashSize, fields);
    }
}