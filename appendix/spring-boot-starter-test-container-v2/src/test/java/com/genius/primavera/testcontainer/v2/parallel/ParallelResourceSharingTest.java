package com.genius.primavera.testcontainer.v2.parallel;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 병렬 실행에서 리소스 공유 및 동기화 테스트
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
@DisplayName("병렬 실행 리소스 공유 테스트")
class ParallelResourceSharingTest extends AutoDynamicPropertySource {

    private static final String SHARED_RESOURCE = "shared_resource";
    private static final String COUNTER_RESOURCE = "counter_resource";
    private static final AtomicInteger testExecutionOrder = new AtomicInteger(0);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeAll
    static void setupClass() {
        log.info("=== ParallelResourceSharingTest - BeforeAll executed at {} by thread: {} ===", 
                LocalDateTime.now(), Thread.currentThread().getName());
    }

    @Test
    @Order(1)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("리소스-1: 병렬 실행 - 독립 리소스")
    void testR1_IndependentResourcesInParallel() throws InterruptedException {
        int executionOrder = testExecutionOrder.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestR1 started: order#{}, thread: {}, time: {}", executionOrder, threadName, LocalDateTime.now());
        
        // 각 스레드별 독립적인 키 사용
        String independentKey = String.format("independent:%s:%d", threadName.replaceAll("[^a-zA-Z0-9]", "_"), executionOrder);
        String independentValue = String.format("value_%s_%d", threadName, executionOrder);
        
        stringRedisTemplate.opsForValue().set(independentKey, independentValue);
        
        Thread.sleep(800); // 다른 스레드가 작업할 시간 제공
        
        String retrievedValue = stringRedisTemplate.opsForValue().get(independentKey);
        assertEquals(independentValue, retrievedValue);
        
        log.info("TestR1 completed: order#{}, thread: {}, time: {}, key: {}", 
                executionOrder, threadName, LocalDateTime.now(), independentKey);
    }

    @Test
    @Order(2)
    @Execution(ExecutionMode.CONCURRENT)
    @ResourceLock(SHARED_RESOURCE)
    @DisplayName("리소스-2: 공유 리소스 - 동기화됨")
    void testR2_SharedResourceWithLock() throws InterruptedException {
        int executionOrder = testExecutionOrder.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestR2 (LOCKED) started: order#{}, thread: {}, time: {}", executionOrder, threadName, LocalDateTime.now());
        
        String sharedKey = "shared:resource:locked";
        
        // 공유 리소스에 접근 (동기화됨)
        String currentValue = stringRedisTemplate.opsForValue().get(sharedKey);
        if (currentValue == null) {
            currentValue = "0";
        }
        
        int currentNumber = Integer.parseInt(currentValue);
        Thread.sleep(500); // 다른 스레드의 간섭을 시뮬레이션
        
        String newValue = String.valueOf(currentNumber + 1);
        stringRedisTemplate.opsForValue().set(sharedKey, newValue);
        
        log.info("TestR2 (LOCKED) completed: order#{}, thread: {}, time: {}, value: {} -> {}", 
                executionOrder, threadName, LocalDateTime.now(), currentValue, newValue);
    }

    @Test
    @Order(3)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("리소스-3: 공유 리소스 - 비동기화 (경쟁 상태)")
    void testR3_SharedResourceWithoutLock() throws InterruptedException {
        int executionOrder = testExecutionOrder.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestR3 (NO LOCK) started: order#{}, thread: {}, time: {}", executionOrder, threadName, LocalDateTime.now());
        
        String sharedKey = "shared:resource:unlocked";
        
        // 공유 리소스에 접근 (비동기화 - 경쟁 상태 발생 가능)
        String currentValue = stringRedisTemplate.opsForValue().get(sharedKey);
        if (currentValue == null) {
            currentValue = "0";
        }
        
        int currentNumber = Integer.parseInt(currentValue);
        Thread.sleep(300); // 경쟁 상태를 유발하기 위한 지연
        
        String newValue = String.valueOf(currentNumber + 1);
        stringRedisTemplate.opsForValue().set(sharedKey, newValue);
        
        log.info("TestR3 (NO LOCK) completed: order#{}, thread: {}, time: {}, value: {} -> {} (may have race condition)", 
                executionOrder, threadName, LocalDateTime.now(), currentValue, newValue);
    }

    @Test
    @Order(4)
    @Execution(ExecutionMode.CONCURRENT)
    @ResourceLock(COUNTER_RESOURCE)
    @DisplayName("리소스-4: 원자적 연산으로 공유 카운터")
    void testR4_AtomicSharedCounter() throws InterruptedException {
        int executionOrder = testExecutionOrder.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestR4 (ATOMIC) started: order#{}, thread: {}, time: {}", executionOrder, threadName, LocalDateTime.now());
        
        String atomicCounterKey = "atomic:counter:locked";
        
        // Redis의 원자적 증가 연산 사용
        Long newValue = stringRedisTemplate.opsForValue().increment(atomicCounterKey);
        
        Thread.sleep(400);
        
        // 현재 값 확인
        String currentValue = stringRedisTemplate.opsForValue().get(atomicCounterKey);
        assertNotNull(currentValue);
        assertTrue(Long.parseLong(currentValue) >= newValue);
        
        log.info("TestR4 (ATOMIC) completed: order#{}, thread: {}, time: {}, incremented to: {}, current: {}", 
                executionOrder, threadName, LocalDateTime.now(), newValue, currentValue);
    }

    @Test
    @Order(5)
    @Execution(ExecutionMode.SAME_THREAD)
    @DisplayName("리소스-5: SAME_THREAD - 순차 실행 확인")
    void testR5_SameThreadSequentialExecution() throws InterruptedException {
        int executionOrder = testExecutionOrder.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestR5 (SAME_THREAD) started: order#{}, thread: {}, time: {}", executionOrder, threadName, LocalDateTime.now());
        
        String sequentialKey = "sequential:same_thread";
        
        // 이전 테스트들이 완료된 후 실행되므로 안전하게 접근 가능
        String timestamp = LocalDateTime.now().toString();
        stringRedisTemplate.opsForValue().set(sequentialKey, timestamp);
        
        Thread.sleep(600);
        
        String retrievedTimestamp = stringRedisTemplate.opsForValue().get(sequentialKey);
        assertEquals(timestamp, retrievedTimestamp);
        
        // 이전 테스트들의 결과 검증
        String lockedValue = stringRedisTemplate.opsForValue().get("shared:resource:locked");
        String unlockedValue = stringRedisTemplate.opsForValue().get("shared:resource:unlocked");
        String atomicValue = stringRedisTemplate.opsForValue().get("atomic:counter:locked");
        
        log.info("TestR5 (SAME_THREAD) completed: order#{}, thread: {}, time: {}", executionOrder, threadName, LocalDateTime.now());
        log.info("Previous test results - locked: {}, unlocked: {}, atomic: {}", lockedValue, unlockedValue, atomicValue);
        
        // 원자적 카운터는 정확해야 함
        if (atomicValue != null) {
            assertTrue(Integer.parseInt(atomicValue) > 0);
        }
    }

    @Test
    @Order(6)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("리소스-6: 병렬 실행 최종 검증")
    void testR6_FinalParallelVerification() throws InterruptedException {
        int executionOrder = testExecutionOrder.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestR6 (FINAL) started: order#{}, thread: {}, time: {}", executionOrder, threadName, LocalDateTime.now());
        
        // 모든 이전 테스트에서 생성된 키들의 존재 확인
        var keys = stringRedisTemplate.keys("*");
        assertNotNull(keys);
        assertTrue(keys.size() > 0);
        
        Thread.sleep(200);
        
        // 독립적인 키들의 개수 확인
        var independentKeys = stringRedisTemplate.keys("independent:*");
        assertTrue(independentKeys.size() > 0, "Independent keys should exist from parallel execution");
        
        // 병렬 실행에서 각 스레드가 고유한 데이터를 생성했는지 확인
        String finalKey = String.format("final:verification:%s:%d", threadName.replaceAll("[^a-zA-Z0-9]", "_"), executionOrder);
        stringRedisTemplate.opsForValue().set(finalKey, String.format("verified_by_%s_at_%d", threadName, System.currentTimeMillis()));
        
        log.info("TestR6 (FINAL) completed: order#{}, thread: {}, time: {}, total keys: {}, independent keys: {}", 
                executionOrder, threadName, LocalDateTime.now(), keys.size(), independentKeys.size());
    }

    @AfterEach
    void teardownMethod(TestInfo testInfo) {
        log.info("ParallelResourceSharingTest.{} - AfterEach completed at {} by thread: {}", 
                testInfo.getDisplayName(), LocalDateTime.now(), Thread.currentThread().getName());
    }

    @AfterAll
    static void teardownClass() {
        log.info("=== ParallelResourceSharingTest - AfterAll executed at {} by thread: {} ===", 
                LocalDateTime.now(), Thread.currentThread().getName());
        log.info("Total method executions: {}", testExecutionOrder.get());
    }
}