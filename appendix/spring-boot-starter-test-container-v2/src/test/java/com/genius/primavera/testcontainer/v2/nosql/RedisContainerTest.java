package com.genius.primavera.testcontainer.v2.nosql;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: Redis 컨테이너 단독 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.REDIS},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Redis 컨테이너 테스트")
class RedisContainerTest extends AutoDynamicPropertySource {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @Order(1)
    @DisplayName("Redis 기본 연결 및 문자열 작업")
    void testRedisBasicStringOperations() {
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        
        // 기본 SET/GET
        valueOps.set("test:key1", "value1");
        String result = valueOps.get("test:key1");
        assertEquals("value1", result);
        
        // TTL 설정
        valueOps.set("test:key2", "value2", Duration.ofSeconds(60));
        Long ttl = stringRedisTemplate.getExpire("test:key2", TimeUnit.SECONDS);
        assertTrue(ttl > 0 && ttl <= 60);
        
        // 증감 연산
        valueOps.set("test:counter", "0");
        Long incremented = valueOps.increment("test:counter");
        assertEquals(1L, incremented);
        
        log.info("Redis basic operations: value={}, ttl={}s, counter={}", result, ttl, incremented);
    }

    @Test
    @Order(2)
    @DisplayName("Redis 해시 작업")
    void testRedisHashOperations() {
        String hashKey = "test:user:1";
        
        // 해시 필드 설정
        stringRedisTemplate.opsForHash().put(hashKey, "name", "John Doe");
        stringRedisTemplate.opsForHash().put(hashKey, "email", "john@redis.com");
        stringRedisTemplate.opsForHash().put(hashKey, "age", "30");
        
        // 해시 필드 조회
        String name = (String) stringRedisTemplate.opsForHash().get(hashKey, "name");
        String email = (String) stringRedisTemplate.opsForHash().get(hashKey, "email");
        
        assertEquals("John Doe", name);
        assertEquals("john@redis.com", email);
        
        // 전체 해시 조회
        var allFields = stringRedisTemplate.opsForHash().entries(hashKey);
        assertEquals(3, allFields.size());
        
        log.info("Redis hash operations: name={}, email={}, fields={}", name, email, allFields.size());
    }

    @Test
    @Order(3)
    @DisplayName("Redis 리스트 작업")
    void testRedisListOperations() {
        String listKey = "test:tasks";
        
        // 리스트에 요소 추가
        stringRedisTemplate.opsForList().rightPush(listKey, "Task 1");
        stringRedisTemplate.opsForList().rightPush(listKey, "Task 2");
        stringRedisTemplate.opsForList().rightPush(listKey, "Task 3");
        
        // 리스트 크기 확인
        Long size = stringRedisTemplate.opsForList().size(listKey);
        assertEquals(3L, size);
        
        // 요소 조회
        String firstTask = stringRedisTemplate.opsForList().index(listKey, 0);
        assertEquals("Task 1", firstTask);
        
        // 요소 제거
        String popped = stringRedisTemplate.opsForList().leftPop(listKey);
        assertEquals("Task 1", popped);
        
        Long newSize = stringRedisTemplate.opsForList().size(listKey);
        assertEquals(2L, newSize);
        
        log.info("Redis list operations: size={}, first={}, popped={}, newSize={}", 
                size, firstTask, popped, newSize);
    }

    @Test
    @Order(4)
    @DisplayName("Redis 셋 작업")
    void testRedisSetOperations() {
        String setKey = "test:tags";
        
        // 셋에 요소 추가
        stringRedisTemplate.opsForSet().add(setKey, "java", "spring", "redis", "testcontainers");
        
        // 셋 크기 확인
        Long size = stringRedisTemplate.opsForSet().size(setKey);
        assertEquals(4L, size);
        
        // 멤버십 확인
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(setKey, "java");
        assertTrue(isMember);
        
        Boolean isNotMember = stringRedisTemplate.opsForSet().isMember(setKey, "python");
        assertFalse(isNotMember);
        
        // 모든 멤버 조회
        Set<String> members = stringRedisTemplate.opsForSet().members(setKey);
        assertNotNull(members);
        assertEquals(4, members.size());
        assertTrue(members.contains("spring"));
        
        log.info("Redis set operations: size={}, members={}, contains spring={}", 
                size, members, members.contains("spring"));
    }

    @Test
    @Order(5)
    @DisplayName("Redis 정렬된 셋 작업")
    void testRedisSortedSetOperations() {
        String zsetKey = "test:leaderboard";
        
        // 스코어와 함께 요소 추가
        stringRedisTemplate.opsForZSet().add(zsetKey, "Alice", 100.0);
        stringRedisTemplate.opsForZSet().add(zsetKey, "Bob", 85.0);
        stringRedisTemplate.opsForZSet().add(zsetKey, "Charlie", 95.0);
        stringRedisTemplate.opsForZSet().add(zsetKey, "David", 110.0);
        
        // 크기 확인
        Long size = stringRedisTemplate.opsForZSet().size(zsetKey);
        assertEquals(4L, size);
        
        // 점수 순으로 조회 (내림차순 - 높은 점수부터)
        Set<String> topUsers = stringRedisTemplate.opsForZSet().reverseRange(zsetKey, 0, 2);
        assertNotNull(topUsers);
        assertEquals(3, topUsers.size());
        
        // 특정 사용자 순위 확인 (0-based, 오름차순 기준)
        Long davidRank = stringRedisTemplate.opsForZSet().reverseRank(zsetKey, "David");
        assertEquals(0L, davidRank); // 가장 높은 점수이므로 0위
        
        // 특정 사용자 점수 확인
        Double aliceScore = stringRedisTemplate.opsForZSet().score(zsetKey, "Alice");
        assertEquals(100.0, aliceScore);
        
        log.info("Redis sorted set operations: size={}, top users={}, David rank={}, Alice score={}", 
                size, topUsers, davidRank, aliceScore);
    }

    @Test
    @Order(6)
    @DisplayName("Redis 성능 테스트")
    void testRedisPerformance() {
        long startTime = System.currentTimeMillis();
        
        // 대량 데이터 삽입
        for (int i = 0; i < 1000; i++) {
            stringRedisTemplate.opsForValue().set("perf:key:" + i, "value" + i);
        }
        
        long insertTime = System.currentTimeMillis() - startTime;
        
        // 대량 데이터 조회
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            String value = stringRedisTemplate.opsForValue().get("perf:key:" + i);
            assertNotNull(value);
        }
        
        long readTime = System.currentTimeMillis() - startTime;
        
        assertTrue(insertTime < 5000, "1000 inserts should complete within 5 seconds");
        assertTrue(readTime < 5000, "1000 reads should complete within 5 seconds");
        
        log.info("Redis performance: 1000 inserts={}ms, 1000 reads={}ms", insertTime, readTime);
    }
}