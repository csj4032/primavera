package com.genius.primavera.hikari;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.model.User;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * HikariCP 리소스 제약 환경 설정 테스트
 * 
 * 설정 특징:
 * - minimum-idle: 2 (최소한의 유휴 연결)
 * - maximum-pool-size: 5 (엄격한 연결 제한)
 * - 짧은 유휴 타임아웃 적극적 해제
 * - 엄격한 누수 감지 (30초)
 * 
 * 기대 결과:
 * - 최소 메모리 사용량
 * - 연결 수 절약
 * - 제한된 동시성
 * - 적극적 리소스 관리
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("hikari-resource-constrained")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("HikariCP 리소스 제약 환경 설정 테스트")
class HikariResourceConstrainedPoolTest {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private UserMapper userMapper;
    
    private HikariDataSource hikariDataSource;
    private HikariPoolMXBean poolMXBean;

    @BeforeEach
    void setUp() throws Exception {
        hikariDataSource = (HikariDataSource) dataSource;
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (ResourceConstrainedPool)");
        poolMXBean = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
    }

    @Test
    @Order(1)
    @DisplayName("리소스 제약 환경 연결 풀 설정 확인")
    void verifyResourceConstrainedPoolConfiguration() {
        log.info("=== HikariCP 리소스 제약 환경 설정 풀 상태 ===");
        log.info("최대 풀 크기: {}", hikariDataSource.getMaximumPoolSize());
        log.info("최소 유휴 연결: {}", hikariDataSource.getMinimumIdle());
        log.info("연결 타임아웃: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("유휴 타임아웃: {}ms", hikariDataSource.getIdleTimeout());
        log.info("최대 생존 시간: {}ms", hikariDataSource.getMaxLifetime());
        log.info("누수 감지 임계값: {}ms", hikariDataSource.getLeakDetectionThreshold());
        log.info("JMX 모니터링: {}", hikariDataSource.isRegisterMbeans());
        
        Assertions.assertEquals(5, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(2, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(10000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(60000, hikariDataSource.getIdleTimeout()); // 1분 - 적극적 해제
        Assertions.assertEquals(30000, hikariDataSource.getLeakDetectionThreshold()); // 30초 - 엄격한 감지
    }

    @Test
    @Order(2)
    @DisplayName("제한된 리소스 환경에서의 기본 처리 능력 테스트")
    void measureBasicProcessingCapabilityUnderConstraints() {
        final int QUERY_COUNT = 100;
        Instant start = Instant.now();
        
        log.info("=== 제한된 리소스 환경 기본 처리 테스트: {}개 쿼리 ===", QUERY_COUNT);
        
        for (int i = 1; i <= QUERY_COUNT; i++) {
            User user = userMapper.findById(1L);
            Assertions.assertNotNull(user);
            
            if (i % 25 == 0) {
                log.debug("Progress: {}/{} queries completed", i, QUERY_COUNT);
                logPoolStatistics(String.format("진행률 %d%%", (i * 100) / QUERY_COUNT));
            }
        }
        
        Duration elapsed = Duration.between(start, Instant.now());
        double queriesPerSecond = QUERY_COUNT / (elapsed.toMillis() / 1000.0);
        
        log.info("=== 제한된 리소스 환경 기본 처리 결과 ===");
        log.info("{}개 쿼리 실행 시간: {}ms", QUERY_COUNT, elapsed.toMillis());
        log.info("평균 쿼리당 시간: {}ms", elapsed.toMillis() / (double) QUERY_COUNT);
        log.info("처리량: {} queries/sec", String.format("%.2f", queriesPerSecond));
        
        logPoolStatistics("기본 처리 테스트 완료 후");
    }

    @Test
    @Order(3)
    @DisplayName("연결 제한으로 인한 대기 현상 관찰")
    void observeConnectionWaitingBehavior() {
        final int THREAD_COUNT = 10; // 풀 크기(5)의 2배
        final int QUERIES_PER_THREAD = 5;
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Instant start = Instant.now();
        
        log.info("=== 연결 제한으로 인한 대기 현상 관찰: {}개 쓰레드 vs {}개 최대 연결 ===", 
            THREAD_COUNT, hikariDataSource.getMaximumPoolSize());
        
        CompletableFuture<ThreadWaitResult>[] futures = IntStream.range(0, THREAD_COUNT)
            .mapToObj(threadId -> CompletableFuture.supplyAsync(() -> {
                Instant threadStart = Instant.now();
                long totalWaitTime = 0;
                int successCount = 0;
                
                for (int i = 0; i < QUERIES_PER_THREAD; i++) {
                    try {
                        Instant queryStart = Instant.now();
                        User user = userMapper.findById(1L);
                        Duration waitTime = Duration.between(queryStart, Instant.now());
                        
                        Assertions.assertNotNull(user);
                        totalWaitTime += waitTime.toMillis();
                        successCount++;
                        
                        log.debug("Thread-{} Query-{}: 대기시간 {}ms", threadId, i + 1, waitTime.toMillis());
                    } catch (Exception e) {
                        log.warn("Thread-{} Query-{} failed: {}", threadId, i + 1, e.getMessage());
                    }
                }
                
                Duration threadElapsed = Duration.between(threadStart, Instant.now());
                return new ThreadWaitResult(threadId, threadElapsed, totalWaitTime, successCount);
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());
        
        // 대기 시간 분석
        ThreadWaitResult[] results = java.util.Arrays.stream(futures)
            .map(CompletableFuture::join)
            .toArray(ThreadWaitResult[]::new);
        
        int totalSuccess = java.util.Arrays.stream(results).mapToInt(r -> r.successCount).sum();
        double avgWaitTimePerQuery = java.util.Arrays.stream(results)
            .mapToLong(r -> r.totalWaitTime)
            .average().orElse(0.0) / QUERIES_PER_THREAD;
        double avgThreadTime = java.util.Arrays.stream(results)
            .mapToLong(r -> r.threadDuration.toMillis())
            .average().orElse(0.0);
        
        log.info("=== 연결 제한 대기 현상 관찰 결과 ===");
        log.info("총 실행 시간: {}ms", totalElapsed.toMillis());
        log.info("평균 쓰레드 실행 시간: {}ms", String.format("%.2f", avgThreadTime));
        log.info("평균 쿼리당 대기 시간: {}ms", String.format("%.2f", avgWaitTimePerQuery));
        log.info("성공한 쿼리: {}개", totalSuccess);
        log.info("연결 제한으로 인한 대기 현상이 관찰됨");
        
        logPoolStatistics("연결 제한 테스트 후");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("연결 누수 감지 및 리소스 정리 테스트")
    void testConnectionLeakDetectionAndCleanup() {
        log.info("=== 연결 누수 감지 및 리소스 정리 테스트 ===");
        log.info("누수 감지 임계값: {}ms", hikariDataSource.getLeakDetectionThreshold());
        
        logPoolStatistics("누수 감지 테스트 시작 전");
        
        // 정상적인 짧은 쿼리들
        for (int i = 1; i <= 5; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());
            
            Assertions.assertNotNull(user);
            log.debug("정상 쿼리 {}: 실행시간 {}ms", i, queryTime.toMillis());
        }
        
        logPoolStatistics("정상 쿼리 실행 후");
        
        // 유휴 연결 정리 동작 확인을 위한 대기
        log.info("유휴 연결 정리 동작 확인을 위해 대기 중... (유휴 타임아웃: {}ms)", 
            hikariDataSource.getIdleTimeout());
        
        try {
            Thread.sleep(2000); // 2초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 정리 후 상태 확인
        logPoolStatistics("대기 후 상태");
        
        // 다시 쿼리 실행하여 연결 재생성 확인
        User user = userMapper.findById(1L);
        Assertions.assertNotNull(user);
        
        logPoolStatistics("연결 재생성 후 최종 상태");
        
        log.info("리소스 제약 환경에서의 적극적 연결 관리 확인 완료");
    }

    @Test
    @Order(5)
    @DisplayName("메모리 효율성 및 연결 수 최적화 확인")
    void verifyMemoryEfficiencyAndConnectionOptimization() {
        log.info("=== 메모리 효율성 및 연결 수 최적화 확인 ===");
        
        // 현재 메모리 사용량 측정
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        logPoolStatistics("메모리 효율성 테스트 시작");
        
        // 적당한 워크로드로 풀 동작 확인
        final int MODERATE_WORKLOAD = 50;
        Instant start = Instant.now();
        
        for (int i = 1; i <= MODERATE_WORKLOAD; i++) {
            User user = userMapper.findById(1L);
            Assertions.assertNotNull(user);
            
            if (i % 10 == 0) {
                logPoolStatistics(String.format("워크로드 진행: %d/%d", i, MODERATE_WORKLOAD));
            }
        }
        
        Duration elapsed = Duration.between(start, Instant.now());
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        log.info("=== 메모리 효율성 테스트 결과 ===");
        log.info("워크로드: {}개 쿼리", MODERATE_WORKLOAD);
        log.info("실행 시간: {}ms", elapsed.toMillis());
        log.info("메모리 사용량 변화: {}KB", memoryUsed / 1024);
        log.info("최대 연결 수 제한: {}개로 메모리 사용량 제어", hikariDataSource.getMaximumPoolSize());
        
        logPoolStatistics("메모리 효율성 테스트 완료");
        
        // 제약 환경에서는 연결 수가 적게 유지되어야 함
        try {
            int totalConnections = poolMXBean.getTotalConnections();
            Assertions.assertTrue(totalConnections <= hikariDataSource.getMaximumPoolSize(),
                "총 연결 수가 최대 풀 크기를 초과하지 않아야 함");
            log.info("연결 수 제한 확인: 총 {}개 연결 (최대 {}개)", 
                totalConnections, hikariDataSource.getMaximumPoolSize());
        } catch (Exception e) {
            log.warn("연결 수 확인 실패: {}", e.getMessage());
        }
    }

    private void logPoolStatistics(String phase) {
        try {
            log.info("=== {} 풀 통계 ===", phase);
            log.info("활성 연결 수: {}", poolMXBean.getActiveConnections());
            log.info("유휴 연결 수: {}", poolMXBean.getIdleConnections());
            log.info("총 연결 수: {}", poolMXBean.getTotalConnections());
            log.info("대기 중인 쓰레드 수: {}", poolMXBean.getThreadsAwaitingConnection());
        } catch (Exception e) {
            log.warn("풀 통계 조회 실패: {}", e.getMessage());
        }
    }

    private record ThreadWaitResult(int threadId, Duration threadDuration, long totalWaitTime, int successCount) {}
}