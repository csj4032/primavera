package com.genius.primavera.testcontainer.parallel;

import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 병렬 실행 테스트 3 - ResourceLock을 사용한 동기화
 * 특정 리소스에 대한 접근을 제어하여 동시성 문제를 방지하는 방법 시연
 */
@Slf4j
@SpringBootTest
@EnableTestContainers
@Execution(ExecutionMode.CONCURRENT)
class ParallelExecutionTest3 {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 공유 리소스 식별자
    private static final String SHARED_COUNTER_RESOURCE = "shared.counter";
    private static final String GLOBAL_STATE_RESOURCE = "global.state";

    @Test
    @DisplayName("병렬 테스트 3-A: 순차 실행이 필요한 카운터 테스트")
    @ResourceLock(SHARED_COUNTER_RESOURCE)
    void testA_sequentialCounter() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 3-A 시작 - 순차 실행", threadName);
        
        Thread.sleep(100);
        
        // 카운터 테이블이 없으면 생성
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS test_counter (
                id INT PRIMARY KEY,
                value INT NOT NULL,
                updated_by VARCHAR(100),
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """);
        
        // 초기값 설정 (존재하지 않을 때만)
        jdbcTemplate.execute("INSERT IGNORE INTO test_counter (id, value, updated_by) VALUES (1, 0, 'INIT')");
        
        // 현재 값 조회
        Integer currentValue = jdbcTemplate.queryForObject(
            "SELECT value FROM test_counter WHERE id = 1", 
            Integer.class
        );
        
        // 값 증가
        int newValue = currentValue + 1;
        jdbcTemplate.update(
            "UPDATE test_counter SET value = ?, updated_by = ? WHERE id = 1", 
            newValue, threadName
        );
        
        // 업데이트된 값 확인
        Integer updatedValue = jdbcTemplate.queryForObject(
            "SELECT value FROM test_counter WHERE id = 1", 
            Integer.class
        );
        assertThat(updatedValue).isEqualTo(newValue);
        
        log.info("[{}] 테스트 3-A 완료 - 카운터: {} -> {}", threadName, currentValue, updatedValue);
    }

    @Test
    @DisplayName("병렬 테스트 3-B: 또 다른 순차 실행 테스트")
    @ResourceLock(SHARED_COUNTER_RESOURCE)
    void testB_anotherSequentialTest() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 3-B 시작 - 순차 실행", threadName);
        
        Thread.sleep(150);
        
        // 현재 카운터 값 조회
        Integer currentValue = jdbcTemplate.queryForObject(
            "SELECT value FROM test_counter WHERE id = 1", 
            Integer.class
        );
        
        // 값을 2 증가
        int newValue = currentValue + 2;
        jdbcTemplate.update(
            "UPDATE test_counter SET value = ?, updated_by = ? WHERE id = 1", 
            newValue, threadName
        );
        
        log.info("[{}] 테스트 3-B 완료 - 카운터: {} -> {}", threadName, currentValue, newValue);
    }

    @Test
    @DisplayName("병렬 테스트 3-C: 병렬 실행 가능한 독립적 작업")
    void testC_independentParallelWork() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 3-C 시작 - 병렬 실행 가능", threadName);
        
        Thread.sleep(80);
        
        // 독립적인 테이블에서 작업 (병렬 실행 가능)
        String tableName = "test_parallel_" + threadName.replaceAll("[^a-zA-Z0-9]", "_");
        
        // 스레드별 독립 테이블 생성
        jdbcTemplate.execute(String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                id INT AUTO_INCREMENT PRIMARY KEY,
                data VARCHAR(100),
                thread_name VARCHAR(100)
            )
        """, tableName));
        
        // 데이터 삽입
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.update(
                String.format("INSERT INTO %s (data, thread_name) VALUES (?, ?)", tableName),
                "Data " + i, threadName
            );
        }
        
        // 삽입된 데이터 확인
        Integer count = jdbcTemplate.queryForObject(
            String.format("SELECT COUNT(*) FROM %s", tableName), 
            Integer.class
        );
        assertThat(count).isEqualTo(3);
        
        log.info("[{}] 테스트 3-C 완료 - 독립 테이블 {} 에 {} 건 삽입", threadName, tableName, count);
    }

    @Test
    @DisplayName("병렬 테스트 3-D: 글로벌 상태 관리")
    @ResourceLock(GLOBAL_STATE_RESOURCE)
    void testD_globalStateManagement() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 3-D 시작 - 글로벌 상태 관리", threadName);
        
        Thread.sleep(200);
        
        // 글로벌 상태 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS test_global_state (
                key_name VARCHAR(50) PRIMARY KEY,
                value_data TEXT,
                last_updated_by VARCHAR(100),
                last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """);
        
        // 상태 정보 설정
        String stateKey = "LAST_PROCESSOR";
        jdbcTemplate.execute(String.format(
            "INSERT INTO test_global_state (key_name, value_data, last_updated_by) " +
            "VALUES ('%s', '%s', '%s') " +
            "ON DUPLICATE KEY UPDATE value_data = '%s', last_updated_by = '%s'",
            stateKey, threadName, threadName, threadName, threadName
        ));
        
        // 현재 상태 확인
        List<Map<String, Object>> states = jdbcTemplate.queryForList(
            "SELECT * FROM test_global_state WHERE key_name = ?", 
            stateKey
        );
        assertThat(states).hasSize(1);
        assertThat(states.get(0).get("value_data")).isEqualTo(threadName);
        
        // 처리 로그 기록
        String logKey = "PROCESSING_LOG";
        String existingLog = "";
        try {
            existingLog = jdbcTemplate.queryForObject(
                "SELECT value_data FROM test_global_state WHERE key_name = ?",
                String.class, logKey
            );
        } catch (Exception e) {
            // 로그가 없으면 빈 문자열 유지
        }
        
        String newLog = existingLog.isEmpty() ? threadName : existingLog + "," + threadName;
        jdbcTemplate.execute(String.format(
            "INSERT INTO test_global_state (key_name, value_data, last_updated_by) " +
            "VALUES ('%s', '%s', '%s') " +
            "ON DUPLICATE KEY UPDATE value_data = '%s', last_updated_by = '%s'",
            logKey, newLog, threadName, newLog, threadName
        ));
        
        log.info("[{}] 테스트 3-D 완료 - 글로벌 상태 업데이트 완료", threadName);
    }

    @Test
    @DisplayName("병렬 테스트 3-E: 마지막 순차 실행 테스트")
    @ResourceLock(SHARED_COUNTER_RESOURCE)
    void testE_finalSequentialTest() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 3-E 시작 - 최종 순차 실행", threadName);
        
        Thread.sleep(120);
        
        // 최종 카운터 값 확인
        Integer finalValue = jdbcTemplate.queryForObject(
            "SELECT value FROM test_counter WHERE id = 1", 
            Integer.class
        );
        
        // 최종 상태 로깅
        jdbcTemplate.update(
            "UPDATE test_counter SET updated_by = ?, value = ? WHERE id = 1", 
            threadName + "_FINAL", finalValue + 100
        );
        
        // 처리 완료 마킹
        Integer completedValue = jdbcTemplate.queryForObject(
            "SELECT value FROM test_counter WHERE id = 1", 
            Integer.class
        );
        
        assertThat(completedValue).isEqualTo(finalValue + 100);
        
        log.info("[{}] 테스트 3-E 완료 - 최종 카운터 값: {}", threadName, completedValue);
    }
}