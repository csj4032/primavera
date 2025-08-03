package com.genius.primavera.testcontainer.v2.parallel;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 병렬 실행 테스트 클래스 A - CONCURRENT 모드
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MARIADB},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT)
@DisplayName("병렬 실행 테스트 클래스 A - CONCURRENT")
class ParallelExecutionTestA extends AutoDynamicPropertySource {

    private static final AtomicInteger testCounter = new AtomicInteger(0);
    private static final AtomicInteger methodExecutionOrder = new AtomicInteger(0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void setupClass() {
        log.info("=== ParallelExecutionTestA - BeforeAll executed at {} by thread: {} ===", 
                LocalDateTime.now(), Thread.currentThread().getName());
    }

    @BeforeEach
    void setupMethod(TestInfo testInfo) {
        int order = methodExecutionOrder.incrementAndGet();
        log.info("ParallelTestA.{} - BeforeEach #{} at {} by thread: {}", 
                testInfo.getDisplayName(), order, LocalDateTime.now(), Thread.currentThread().getName());
    }

    @Test
    @Order(1)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트A-1: 병렬 실행 기본 확인")
    void testA1_BasicConcurrentExecution() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestA1 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // DB 연결 확인
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, userCount);
        
        // 의도적인 지연으로 병렬 실행 확인
        Thread.sleep(1000);
        
        // 테스트 고유 데이터 삽입
        String uniqueEmail = String.format("testA1_%s_%d@parallel.com", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum);
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                uniqueEmail, "{noop}password", "TestA1User" + testNum);
        
        // 삽입 확인
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS WHERE EMAIL = ?", Integer.class, uniqueEmail);
        assertEquals(1, count);
        
        log.info("TestA1 completed: test#{}, thread: {}, time: {}, email: {}", 
                testNum, threadName, LocalDateTime.now(), uniqueEmail);
    }

    @Test
    @Order(2)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트A-2: 병렬 실행 데이터 격리")
    void testA2_DataIsolationInConcurrent() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestA2 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // 현재 사용자 수 확인
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertTrue(initialCount >= 4);
        
        // 지연
        Thread.sleep(800);
        
        // 고유 데이터 생성
        String uniqueNickname = String.format("TestA2_%s_%d", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum);
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                String.format("testA2_%d@parallel.com", testNum), "{noop}password", uniqueNickname);
        
        // 자신이 생성한 데이터만 조회되는지 확인
        String retrievedNickname = jdbcTemplate.queryForObject(
                "SELECT NICKNAME FROM USERS WHERE NICKNAME = ?", String.class, uniqueNickname);
        assertEquals(uniqueNickname, retrievedNickname);
        
        log.info("TestA2 completed: test#{}, thread: {}, time: {}, nickname: {}", 
                testNum, threadName, LocalDateTime.now(), uniqueNickname);
    }

    @Test
    @Order(3)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트A-3: 병렬 실행 성능 테스트")
    void testA3_ConcurrentPerformanceTest() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestA3 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        long startTime = System.currentTimeMillis();
        
        // 반복 작업
        for (int i = 0; i < 50; i++) {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
            
            if (i % 10 == 0) {
                Thread.sleep(50); // 중간중간 지연
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 10000, "50 queries should complete within 10 seconds even in concurrent mode");
        
        log.info("TestA3 completed: test#{}, thread: {}, time: {}, duration: {}ms", 
                testNum, threadName, LocalDateTime.now(), duration);
    }

    @Test
    @Order(4)
    @Execution(ExecutionMode.SAME_THREAD)
    @DisplayName("테스트A-4: SAME_THREAD 모드 확인")
    void testA4_SameThreadExecution() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestA4 (SAME_THREAD) started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // SAME_THREAD 모드에서는 다른 테스트와 순차 실행되어야 함
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertTrue(userCount >= 4);
        
        Thread.sleep(500);
        
        String uniqueEmail = String.format("testA4_same_thread_%d@parallel.com", testNum);
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                uniqueEmail, "{noop}password", "TestA4SameThread" + testNum);
        
        log.info("TestA4 (SAME_THREAD) completed: test#{}, thread: {}, time: {}", 
                testNum, threadName, LocalDateTime.now());
    }

    @Test
    @Order(5)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트A-5: 최종 상태 확인")
    void testA5_FinalStateVerification() {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestA5 (final check) started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // 모든 테스트가 데이터를 추가했으므로 초기값보다 많아야 함
        Integer finalUserCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertTrue(finalUserCount > 4, "Final user count should be greater than initial 4 users");
        
        // 각 테스트에서 생성한 고유 데이터들이 존재하는지 확인
        Integer testA1Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE 'testA1_%@parallel.com'", Integer.class);
        assertTrue(testA1Count >= 1, "TestA1 should have created at least 1 user");
        
        log.info("TestA5 (final check) completed: test#{}, thread: {}, time: {}, final count: {}, testA1 users: {}", 
                testNum, threadName, LocalDateTime.now(), finalUserCount, testA1Count);
    }

    @AfterEach
    void teardownMethod(TestInfo testInfo) {
        log.info("ParallelTestA.{} - AfterEach completed at {} by thread: {}", 
                testInfo.getDisplayName(), LocalDateTime.now(), Thread.currentThread().getName());
    }

    @AfterAll
    static void teardownClass() {
        log.info("=== ParallelExecutionTestA - AfterAll executed at {} by thread: {} ===", 
                LocalDateTime.now(), Thread.currentThread().getName());
        log.info("Total tests executed: {}, Total method executions: {}", 
                testCounter.get(), methodExecutionOrder.get());
    }
}