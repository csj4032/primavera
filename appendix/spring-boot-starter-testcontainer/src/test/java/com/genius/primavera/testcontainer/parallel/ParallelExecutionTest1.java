package com.genius.primavera.testcontainer.parallel;

import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 병렬 실행 테스트 1 - 기본 병렬 실행
 * 여러 테스트가 동시에 실행되면서 TestContainers가 올바르게 격리되는지 확인
 */
@Slf4j
@SpringBootTest
@EnableTestContainers
@Execution(ExecutionMode.CONCURRENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParallelExecutionTest1 {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    @DisplayName("병렬 테스트 1-A: 사용자 데이터 조회")
    void testA_queryUsers() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 1-A 시작", threadName);
        Thread.sleep(100); // 병렬 실행 시뮬레이션
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("[{}] 테스트 1-A 완료 - 사용자 수: {}", threadName, count);
    }

    @Test
    @Order(2)
    @DisplayName("병렬 테스트 1-B: 사용자 데이터 삽입")
    void testB_insertUser() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 1-B 시작", threadName);
        Thread.sleep(150); // 병렬 실행 시뮬레이션
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('Parallel User 1B', 'parallel1b@test.com')");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("[{}] 테스트 1-B 완료 - 데이터 삽입 후 사용자 수: {}", threadName, count);
    }

    @Test
    @Order(3)
    @DisplayName("병렬 테스트 1-C: 사용자 데이터 업데이트")
    void testC_updateUser() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 1-C 시작", threadName);
        Thread.sleep(200); // 병렬 실행 시뮬레이션
        int updated = jdbcTemplate.update("UPDATE test_users SET name = 'Updated User' WHERE email = 'test1@example.com'");
        assertThat(updated).isEqualTo(1);
        String updatedName = jdbcTemplate.queryForObject("SELECT name FROM test_users WHERE email = 'test1@example.com'",String.class);
        log.info("[{}] 테스트 1-C 완료 - 사용자 업데이트 성공", threadName);
    }

    @Test
    @Order(4)
    @DisplayName("병렬 테스트 1-D: 복합 쿼리 실행")
    void testD_complexQuery() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        log.info("[{}] 테스트 1-D 시작", threadName);
        Thread.sleep(80);
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('Complex User', 'complex@test.com')");
        Integer countWithTest = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users WHERE email LIKE '%test%'", Integer.class);
        log.info("[{}] 테스트 1-D 완료 - 복합 쿼리 결과: {}", threadName, countWithTest);
    }
}