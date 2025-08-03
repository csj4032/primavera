package com.genius.primavera.testcontainer.v2.parallel;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 병렬 실행 모니터링 및 통계 수집
 * Suite와 함께 실행되어 전체 병렬 실행 상태를 모니터링
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("병렬 실행 모니터링")
class ParallelExecutionMonitor {

    private static final AtomicInteger totalTestsStarted = new AtomicInteger(0);
    private static final AtomicInteger totalTestsCompleted = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, Integer> threadUsage = new ConcurrentHashMap<>();
    private static final LocalDateTime startTime = LocalDateTime.now();

    static {
        log.info("=== ParallelExecutionMonitor initialized at {} ===", startTime);
        log.info("System properties:");
        log.info("- Available processors: {}", Runtime.getRuntime().availableProcessors());
        log.info("- Max memory: {} MB", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        log.info("- Parallel execution enabled: {}", System.getProperty("junit.jupiter.execution.parallel.enabled"));
        log.info("- Parallel mode default: {}", System.getProperty("junit.jupiter.execution.parallel.mode.default"));
        log.info("- Parallel config strategy: {}", System.getProperty("junit.jupiter.execution.parallel.config.strategy"));
        log.info("- Fixed parallelism: {}", System.getProperty("junit.jupiter.execution.parallel.config.fixed.parallelism"));
    }

    @BeforeEach
    void monitorTestStart(TestInfo testInfo) {
        int testNum = totalTestsStarted.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        threadUsage.compute(threadName, (k, v) -> v == null ? 1 : v + 1);
        
        log.info("Monitor: Test #{} '{}' started on thread '{}'", 
                testNum, testInfo.getDisplayName(), threadName);
    }

    @Test
    @Order(1)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("모니터-1: 병렬 실행 환경 확인")
    void testMonitor1_CheckParallelEnvironment() {
        String threadName = Thread.currentThread().getName();
        
        boolean parallelEnabled = "true".equals(System.getProperty("junit.jupiter.execution.parallel.enabled"));
        assertTrue(parallelEnabled, "Parallel execution should be enabled");
        
        log.info("Monitor-1: Parallel execution confirmed on thread '{}'", threadName);
    }

    @Test
    @Order(2)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("모니터-2: 스레드 사용량 추적")
    void testMonitor2_TrackThreadUsage() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        
        Thread.sleep(500); // 다른 테스트들이 실행될 시간 제공
        
        int uniqueThreads = threadUsage.size();
        assertTrue(uniqueThreads > 0, "At least one thread should be used");
        
        log.info("Monitor-2: {} unique threads detected so far on thread '{}'", uniqueThreads, threadName);
        threadUsage.forEach((thread, count) -> 
            log.info("  - Thread '{}': {} tests", thread, count));
    }

    @Test
    @Order(3)
    @Execution(ExecutionMode.SAME_THREAD)
    @DisplayName("모니터-3: 순차 실행 모드 확인")
    void testMonitor3_SequentialMode() {
        String threadName = Thread.currentThread().getName();
        
        log.info("Monitor-3: Sequential mode test on thread '{}'", threadName);
        
        // SAME_THREAD 모드에서는 이전 테스트와 같은 스레드에서 실행되어야 함
        assertTrue(threadUsage.containsKey(threadName), 
                "Should run on a thread that was already used");
    }

    @Test
    @Order(99)
    @Execution(ExecutionMode.SAME_THREAD)
    @DisplayName("모니터-최종: 실행 통계 수집")
    void testMonitorFinal_CollectStatistics() throws InterruptedException {
        // 다른 테스트들이 완료될 시간 대기
        Thread.sleep(1000);
        
        LocalDateTime endTime = LocalDateTime.now();
        
        log.info("=== Parallel Execution Statistics ===");
        log.info("Start time: {}", startTime);
        log.info("End time: {}", endTime);
        log.info("Total tests started: {}", totalTestsStarted.get());
        log.info("Total tests completed: {}", totalTestsCompleted.get());
        log.info("Unique threads used: {}", threadUsage.size());
        log.info("Thread usage details:");
        threadUsage.forEach((thread, count) -> 
            log.info("  - {}: {} tests", thread, count));
        log.info("=====================================");
    }

    @AfterEach
    void monitorTestComplete(TestInfo testInfo) {
        int testNum = totalTestsCompleted.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("Monitor: Test #{} '{}' completed on thread '{}'", 
                testNum, testInfo.getDisplayName(), threadName);
    }
}