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
 * 병렬 실행 테스트 클래스 C - PER_METHOD 라이프사이클 + 병렬 실행
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MARIADB},
    lifecycleMode = ContainerLifecycleMode.PER_METHOD
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT)
@DisplayName("병렬 실행 테스트 클래스 C - PER_METHOD + CONCURRENT")
class ParallelExecutionTestC {

    private static final AtomicInteger testCounter = new AtomicInteger(0);
    private static final AtomicInteger executionOrder = new AtomicInteger(0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupMethod(TestInfo testInfo) {
        int order = executionOrder.incrementAndGet();
        log.info("ParallelTestC.{} - BeforeEach #{} at {} by thread: {}", 
                testInfo.getDisplayName(), order, LocalDateTime.now(), Thread.currentThread().getName());
    }

    @Test
    @Order(1)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트C-1: PER_METHOD + CONCURRENT 기본 확인")
    void testC1_PerMethodConcurrentBasic() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestC1 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // PER_METHOD 모드에서는 각 테스트마다 새로운 컨테이너가 시작됨
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, userCount); // 매번 초기 상태
        
        Thread.sleep(700);
        
        String uniqueEmail = String.format("testC1_%s_%d@permethod.com", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum);
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                uniqueEmail, "{noop}password", "TestC1User" + testNum);
        
        Integer newCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, newCount);
        
        log.info("TestC1 completed: test#{}, thread: {}, time: {}, final count: {}", 
                testNum, threadName, LocalDateTime.now(), newCount);
    }

    @Test
    @Order(2)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트C-2: PER_METHOD 컨테이너 독립성 확인")
    void testC2_PerMethodContainerIsolation() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestC2 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // 다시 초기 상태여야 함 (PER_METHOD로 인한 새 컨테이너)
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, initialCount);
        
        Thread.sleep(900);
        
        // 여러 사용자 추가
        for (int i = 0; i < 3; i++) {
            String email = String.format("testC2_%s_%d_%d@permethod.com", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum, i);
            jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                    email, "{noop}password", String.format("TestC2User%d_%d", testNum, i));
        }
        
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(7, finalCount); // 4 + 3
        
        log.info("TestC2 completed: test#{}, thread: {}, time: {}, added 3 users: {} -> {}", 
                testNum, threadName, LocalDateTime.now(), initialCount, finalCount);
    }

    @Test
    @Order(3)
    @Execution(ExecutionMode.SAME_THREAD)
    @DisplayName("테스트C-3: PER_METHOD + SAME_THREAD 조합")
    void testC3_PerMethodSameThread() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestC3 (SAME_THREAD) started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // 역시 새로운 컨테이너이므로 초기 상태
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, userCount);
        
        Thread.sleep(500);
        
        String email = String.format("testC3_same_thread_%d@permethod.com", testNum);
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                email, "{noop}password", "TestC3SameThread" + testNum);
        
        // 트랜잭션 테스트
        try {
            jdbcTemplate.execute("START TRANSACTION");
            jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                    "temp@transaction.com", "{noop}password", "TempUser");
            jdbcTemplate.execute("ROLLBACK");
        } catch (Exception e) {
            log.warn("Transaction test failed (expected in some scenarios): {}", e.getMessage());
        }
        
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, finalCount); // 롤백된 사용자는 제외
        
        log.info("TestC3 (SAME_THREAD) completed: test#{}, thread: {}, time: {}, final count: {}", 
                testNum, threadName, LocalDateTime.now(), finalCount);
    }

    @Test
    @Order(4)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트C-4: PER_METHOD 병렬 성능 테스트")
    void testC4_PerMethodConcurrentPerformance() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestC4 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        long startTime = System.currentTimeMillis();
        
        // PER_METHOD 모드에서 새 컨테이너 시작 시간도 포함된 성능 테스트
        Integer initialCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, initialCount);
        
        // 반복 쿼리 실행
        for (int i = 0; i < 30; i++) {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
            
            if (i % 5 == 0) {
                String email = String.format("perfC4_%s_%d_%d@permethod.com", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum, i);
                jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                        email, "{noop}password", "PerfC4User" + i);
            }
            
            Thread.sleep(50);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertTrue(finalCount > initialCount);
        
        // PER_METHOD 모드는 컨테이너 시작 시간이 포함되므로 더 관대한 시간 제한
        assertTrue(duration < 20000, "PER_METHOD operations should complete within 20 seconds including container startup");
        
        log.info("TestC4 completed: test#{}, thread: {}, time: {}, duration: {}ms, users: {} -> {}", 
                testNum, threadName, LocalDateTime.now(), duration, initialCount, finalCount);
    }

    @Test
    @Order(5)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("테스트C-5: PER_METHOD 최종 독립성 검증")
    void testC5_PerMethodFinalIsolationVerification() throws InterruptedException {
        int testNum = testCounter.incrementAndGet();
        String threadName = Thread.currentThread().getName();
        
        log.info("TestC5 started: test#{}, thread: {}, time: {}", testNum, threadName, LocalDateTime.now());
        
        // 다른 테스트의 영향을 받지 않고 항상 초기 상태여야 함
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, userCount, "PER_METHOD should always start with clean state");
        
        Thread.sleep(400);
        
        // 이전 테스트들에서 생성한 데이터가 없어야 함
        Integer testC1Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE 'testC1_%@permethod.com'", Integer.class);
        assertEquals(0, testC1Count, "Previous test data should not exist in PER_METHOD mode");
        
        Integer testC2Count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE 'testC2_%@permethod.com'", Integer.class);
        assertEquals(0, testC2Count, "Previous test data should not exist in PER_METHOD mode");
        
        // 자신만의 고유 데이터 생성
        String uniqueEmail = String.format("testC5_%s_%d@permethod.com", threadName.replaceAll("[^a-zA-Z0-9]", "_"), testNum);
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                uniqueEmail, "{noop}password", "TestC5FinalUser" + testNum);
        
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(5, finalCount);
        
        log.info("TestC5 completed: test#{}, thread: {}, time: {}, isolation verified, final count: {}", 
                testNum, threadName, LocalDateTime.now(), finalCount);
    }

    @AfterEach
    void teardownMethod(TestInfo testInfo) {
        log.info("ParallelTestC.{} - AfterEach completed at {} by thread: {}", 
                testInfo.getDisplayName(), LocalDateTime.now(), Thread.currentThread().getName());
    }

    @AfterAll
    static void teardownClass() {
        log.info("=== ParallelExecutionTestC - AfterAll executed at {} by thread: {} ===", 
                LocalDateTime.now(), Thread.currentThread().getName());
        log.info("Total tests executed: {}, Total method executions: {}", 
                testCounter.get(), executionOrder.get());
    }
}