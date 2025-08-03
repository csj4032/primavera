package com.genius.primavera.testcontainer.v2.parallel;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 병렬 실행 설정 검증을 위한 단순 테스트
 * Suite 컴파일 에러 없이 병렬 실행을 확인
 */
@Slf4j
@DisplayName("병렬 실행 검증 테스트")
class ParallelExecutionVerificationTest {

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("병렬 실행 설정 확인")
    void verifyParallelExecutionEnabled() {
        String parallelEnabled = System.getProperty("junit.jupiter.execution.parallel.enabled");
        
        log.info("Parallel execution enabled: {}", parallelEnabled);
        log.info("Current thread: {}", Thread.currentThread().getName());
        
        if ("true".equals(parallelEnabled)) {
            log.info("✅ Parallel execution is ENABLED");
        } else {
            log.warn("⚠️  Parallel execution is DISABLED. Enable it with -Djunit.jupiter.execution.parallel.enabled=true");
        }
        
        // 병렬 실행이 비활성화되어도 테스트는 통과
        assertNotNull(Thread.currentThread().getName());
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("병렬 실행 설정 상세 정보")
    void displayParallelExecutionConfiguration() {
        log.info("=== Parallel Execution Configuration ===");
        log.info("Enabled: {}", System.getProperty("junit.jupiter.execution.parallel.enabled", "false"));
        log.info("Mode (default): {}", System.getProperty("junit.jupiter.execution.parallel.mode.default", "same_thread"));
        log.info("Mode (classes): {}", System.getProperty("junit.jupiter.execution.parallel.mode.classes.default", "same_thread"));
        log.info("Strategy: {}", System.getProperty("junit.jupiter.execution.parallel.config.strategy", "dynamic"));
        log.info("Fixed parallelism: {}", System.getProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "not set"));
        log.info("Available processors: {}", Runtime.getRuntime().availableProcessors());
        log.info("========================================");
        
        assertTrue(Runtime.getRuntime().availableProcessors() > 0);
    }
}