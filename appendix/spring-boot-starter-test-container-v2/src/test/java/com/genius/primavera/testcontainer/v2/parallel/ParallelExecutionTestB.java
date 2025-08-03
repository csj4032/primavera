package com.genius.primavera.testcontainer.v2.parallel;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 병렬 실행 테스트 클래스 B - Redis 컨테이너 사용
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
@Execution(ExecutionMode.CONCURRENT)
@DisplayName("병렬 실행 테스트 클래스 B - Redis CONCURRENT")
class ParallelExecutionTestB extends AutoDynamicPropertySource {

    private static final AtomicInteger testCounter = new AtomicInteger(0);
    private static final AtomicInteger globalCounter = new AtomicInteger(0);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeAll
    static void setupClass() {
        log.info("=== ParallelExecutionTestB - BeforeAll executed at {} by thread: {} ===", 
                LocalDateTime.now(), Thread.currentThread().getName());
    }

    @BeforeEach
    void setupMethod(TestInfo testInfo) {
        int order = globalCounter.incrementAndGet();
        log.info("ParallelTestB.{} - BeforeEach #{} at {} by thread: {}", 
                testInfo.getDisplayName(), order, LocalDateTime.now(), Thread.currentThread().getName());
    }

    @Test
    @Order(1)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트B-1: Redis 병렬 기본 작업")
    void testB1_BasicRedisConcurrentOperations() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestB1 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // Redis 연결 확인
        stringRedisTemplate.opsForValue().set("test:connection", "connected");
        String connectionTest = stringRedisTemplate.opsForValue().get("test:connection");
        assertEquals("connected", connectionTest);
        
        Thread.sleep(800);
        
        // 고유 키로 데이터 저장
        String uniqueKey = String.format("testB1:%s:%d", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum);
        String uniqueValue = String.format("value_%s_%d", threadName, testNum);
        
        stringRedisTemplate.opsForValue().set(uniqueKey, uniqueValue);
        String retrievedValue = stringRedisTemplate.opsForValue().get(uniqueKey);
        
        assertEquals(uniqueValue, retrievedValue);
        
        log.info("TestB1 completed: test#{}, thread: {}, time: {}, key: {}", 
                testNum, threadName, LocalDateTime.now(), uniqueKey);
    }

    @Test
    @Order(2)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트B-2: Redis 병렬 리스트 작업")
    void testB2_RedisListConcurrentOperations() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestB2 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        String listKey = String.format("list:testB2:%s:%d", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum);
        
        // 리스트에 여러 항목 추가
        for (int i = 0; i < 5; i++) {
            stringRedisTemplate.opsForList().rightPush(listKey, String.format("item_%d_%s", i, threadName));
            Thread.sleep(100); // 중간 지연
        }
        
        Long listSize = stringRedisTemplate.opsForList().size(listKey);
        assertEquals(5L, listSize);
        
        String firstItem = stringRedisTemplate.opsForList().index(listKey, 0);
        assertTrue(firstItem.contains("item_0"));
        assertTrue(firstItem.contains(threadName));
        
        log.info("TestB2 completed: test#{}, thread: {}, time: {}, list size: {}", 
                testNum, threadName, LocalDateTime.now(), listSize);
    }

    @Test
    @Order(3)
    @Execution(ExecutionMode.SAME_THREAD)
    @DisplayName("테스트B-3: Redis SAME_THREAD 해시 작업")
    void testB3_RedisHashSameThreadOperations() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestB3 (SAME_THREAD) started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        String hashKey = String.format("hash:testB3:same_thread:%d", testNum);
        
        // 해시에 여러 필드 설정
        stringRedisTemplate.opsForHash().put(hashKey, "thread", threadName);
        stringRedisTemplate.opsForHash().put(hashKey, "testNum", testNum.toString());
        stringRedisTemplate.opsForHash().put(hashKey, "timestamp", LocalDateTime.now().toString());
        stringRedisTemplate.opsForHash().put(hashKey, "mode", "SAME_THREAD");
        
        Thread.sleep(600);
        
        // 해시 크기 확인
        Long hashSize = stringRedisTemplate.opsForHash().size(hashKey);
        assertEquals(4L, hashSize);
        
        // 특정 필드 확인
        String mode = (String) stringRedisTemplate.opsForHash().get(hashKey, "mode");
        assertEquals("SAME_THREAD", mode);
        
        log.info("TestB3 (SAME_THREAD) completed: test#{}, thread: {}, time: {}, mode: {}", 
                testNum, threadName, LocalDateTime.now(), mode);
    }

    @Test
    @Order(4)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트B-4: Redis 병렬 성능 테스트")
    void testB4_RedisPerformanceConcurrentTest() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestB4 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        long startTime = System.currentTimeMillis();
        String keyPrefix = String.format("perf:testB4:%s:%d", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum);
        
        // 대량 데이터 작업
        for (int i = 0; i < 100; i++) {
            String key = keyPrefix + ":" + i;
            stringRedisTemplate.opsForValue().set(key, "value" + i);
            
            if (i % 20 == 0) {
                Thread.sleep(50); // 중간 지연
            }
        }
        
        // 조회 테스트
        for (int i = 0; i < 100; i++) {
            String key = keyPrefix + ":" + i;
            String value = stringRedisTemplate.opsForValue().get(key);
            assertEquals("value" + i, value);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 15000, "100 set/get operations should complete within 15 seconds");
        
        log.info("TestB4 completed: test#{}, thread: {}, time: {}, duration: {}ms", 
                testNum, threadName, LocalDateTime.now(), duration);
    }

    @Test
    @Order(5)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트B-5: Redis 병렬 원자적 연산")
    void testB5_RedisAtomicConcurrentOperations() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestB5 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        String counterKey = "counter:global:testB5";
        String uniqueCounterKey = String.format("counter:testB5:%s:%d", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum);
        
        // 전역 카운터 증가 (원자적 연산)
        Long globalCount = stringRedisTemplate.opsForValue().increment(counterKey);
        assertNotNull(globalCount);
        assertTrue(globalCount > 0);
        
        Thread.sleep(300);
        
        // 개별 카운터 증가
        for (int i = 0; i < 10; i++) {
            stringRedisTemplate.opsForValue().increment(uniqueCounterKey);
            Thread.sleep(50);
        }
        
        String finalUniqueCount = stringRedisTemplate.opsForValue().get(uniqueCounterKey);
        assertEquals("10", finalUniqueCount);
        
        log.info("TestB5 completed: test#{}, thread: {}, time: {}, global: {}, unique: {}", 
                testNum, threadName, LocalDateTime.now(), globalCount, finalUniqueCount);
    }

    @AfterEach
    void teardownMethod(TestInfo testInfo) {
        log.info("ParallelTestB.{} - AfterEach completed at {} by thread: {}", 
                testInfo.getDisplayName(), LocalDateTime.now(), Thread.currentThread().getName());
    }

    @AfterAll
    static void teardownClass() {
        log.info("=== ParallelExecutionTestB - AfterAll executed at {} by thread: {} ===", 
                LocalDateTime.now(), Thread.currentThread().getName());
        log.info("Total tests executed: {}, Total method executions: {}", 
                testCounter.get(), globalCounter.get());
    }
}