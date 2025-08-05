package com.genius.primavera.testcontainer.parallel;

import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 병렬 실행 테스트 2 - PER_CLASS 라이프사이클과 병렬 실행
 * 클래스 레벨에서 컨테이너를 공유하면서 병렬 실행이 어떻게 작동하는지 확인
 */
@Slf4j
@EnableTestContainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ParallelExecutionTest2 {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final AtomicInteger testCounter = new AtomicInteger(0);

    @Test
    @DisplayName("병렬 테스트 2-A: 대량 데이터 삽입")
    void testA_bulkInsert() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        int testNum = testCounter.incrementAndGet();
        log.info("[{}] 테스트 2-A ({}) 시작", threadName, testNum);
        Thread.sleep(120);
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.execute(String.format("INSERT INTO test_users (name, email) VALUES ('Bulk User %d-%d', 'bulk%d_%d@test.com')", testNum, i, testNum, i));
        }
        Integer bulkCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE 'bulk" + testNum + "_%'", Integer.class);
        assertThat(bulkCount).isEqualTo(5);
        log.info("[{}] 테스트 2-A ({}) 완료 - 대량 삽입: {} 건", threadName, testNum, bulkCount);
    }

    @Test
    @DisplayName("병렬 테스트 2-B: 데이터 집계 쿼리")
    void testB_aggregateQuery() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        int testNum = testCounter.incrementAndGet();
        log.info("[{}] 테스트 2-B ({}) 시작", threadName, testNum);
        Thread.sleep(90); // 병렬 실행 시뮬레이션
        jdbcTemplate.execute(String.format("INSERT INTO test_users (name, email) VALUES ('Aggregate User %d', 'aggregate%d@test.com')", testNum, testNum));
        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        assertThat(totalCount).isGreaterThanOrEqualTo(3); // 최소 기본 데이터 + 추가 데이터
        List<Map<String, Object>> domainStats = jdbcTemplate.queryForList("SELECT SUBSTRING_INDEX(email, '@', -1) as domain, COUNT(*) as count " + "FROM test_users GROUP BY SUBSTRING_INDEX(email, '@', -1) ORDER BY count DESC");
        assertThat(domainStats).isNotEmpty();
        log.info("[{}] 테스트 2-B ({}) 완료 - 전체 사용자: {}, 도메인 종류: {}", threadName, testNum, totalCount, domainStats.size());
    }

    @Test
    @DisplayName("병렬 테스트 2-C: 트랜잭션 테스트")
    void testC_transactionTest() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        int testNum = testCounter.incrementAndGet();
        log.info("[{}] 테스트 2-C ({}) 시작", threadName, testNum);
        Thread.sleep(160);
        try {
            jdbcTemplate.execute("START TRANSACTION");
            jdbcTemplate.execute(String.format("INSERT INTO test_users (name, email) VALUES ('Transaction User %d-1', 'trans%d_1@test.com')", testNum, testNum));
            jdbcTemplate.execute(String.format("INSERT INTO test_users (name, email) VALUES ('Transaction User %d-2', 'trans%d_2@test.com')", testNum, testNum));
            Integer transCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE 'trans" + testNum + "_%'", Integer.class);
            assertThat(transCount).isEqualTo(2);
            jdbcTemplate.execute("COMMIT");
        } catch (Exception e) {
            jdbcTemplate.execute("ROLLBACK");
            throw e;
        }

        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE 'trans" + testNum + "_%'", Integer.class);
        assertThat(finalCount).isEqualTo(2);
        log.info("[{}] 테스트 2-C ({}) 완료 - 트랜잭션 처리 성공", threadName, testNum);
    }

    @Test
    @DisplayName("병렬 테스트 2-D: 동시성 스트레스 테스트")
    void testD_concurrencyStress() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        int testNum = testCounter.incrementAndGet();
        log.info("[{}] 테스트 2-D ({}) 시작", threadName, testNum);
        Thread.sleep(250);
        long startTime = System.currentTimeMillis();
        for (int i = 1; i <= 10; i++) {
            jdbcTemplate.execute(String.format("INSERT INTO test_users (name, email) VALUES ('Stress User %d-%d', 'stress%d_%d@test.com')", testNum, i, testNum, i));
            if (i % 3 == 0) {
                Integer currentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE 'stress" + testNum + "_%'", Integer.class);
                assertThat(currentCount).isEqualTo(i);
            }
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Integer stressCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE 'stress" + testNum + "_%'", Integer.class);
        assertThat(stressCount).isEqualTo(10);
        log.info("[{}] 테스트 2-D ({}) 완료 - 스트레스 테스트: {} 건, 소요시간: {}ms", threadName, testNum, stressCount, duration);
    }
}