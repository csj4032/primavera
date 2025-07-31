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
 * HikariCP 성능 최우선 설정 테스트
 * <p>
 * 설정 특징:
 * - minimum-idle: 10 (즉시 사용 가능한 연결)
 * - maximum-pool-size: 20 (높은 동시성)
 * - 누수 감지 비활성화 (성능 우선)
 * <p>
 * 기대 결과:
 * - 최고 처리량
 * - 최소 지연시간
 * - 높은 메모리 사용량
 * - 최대 동시성 지원
 */
@Slf4j
@SpringBootTest
@DisplayName("HikariCP 성능 최우선 설정 테스트")
@ActiveProfiles("hikari-performance-focused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HikariPerformancePoolTest {

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
        ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (PerformanceHikariPool)");
        poolMXBean = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
    }

    @Test
    @Order(1)
    @DisplayName("성능 최우선 연결 풀 설정 확인")
    void verifyPerformancePoolConfiguration() {
        log.info("=== HikariCP 성능 최우선 설정 풀 상태 ===");
        log.info("최대 풀 크기: {}", hikariDataSource.getMaximumPoolSize());
        log.info("최소 유휴 연결: {}", hikariDataSource.getMinimumIdle());
        log.info("연결 타임아웃: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("유휴 타임아웃: {}ms", hikariDataSource.getIdleTimeout());
        log.info("최대 생존 시간: {}ms", hikariDataSource.getMaxLifetime());
        log.info("누수 감지 임계값: {}ms (0=비활성화)", hikariDataSource.getLeakDetectionThreshold());

        Assertions.assertEquals(20, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(10, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(20000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(0, hikariDataSource.getLeakDetectionThreshold()); // 성능을 위해 비활성화
    }

    @Test
    @Order(2)
    @DisplayName("고성능 대량 쿼리 처리 테스트")
    void measureHighVolumeQueryPerformance() {
        final int QUERY_COUNT = 1000;
        Instant start = Instant.now();
        log.info("=== 고성능 대량 쿼리 테스트 시작: {}개 쿼리 ===", QUERY_COUNT);
        IntStream.rangeClosed(1, QUERY_COUNT)
                .parallel()
                .forEach(i -> {
                    User user = userMapper.findById(1L);
                    Assertions.assertNotNull(user);
                    if (i % 200 == 0) {
                        log.debug("Progress: {}/{} queries completed", i, QUERY_COUNT);
                    }
                });

        Duration elapsed = Duration.between(start, Instant.now());
        double queriesPerSecond = QUERY_COUNT / (elapsed.toMillis() / 1000.0);
        log.info("=== 고성능 대량 쿼리 테스트 결과 ===");
        log.info("{}개 쿼리 실행 시간: {}ms", QUERY_COUNT, elapsed.toMillis());
        log.info("평균 쿼리당 시간: {}ms", elapsed.toMillis() / (double) QUERY_COUNT);
        log.info("처리량: {} queries/sec", String.format("%.2f", queriesPerSecond));
        logPoolStatistics("대량 쿼리 테스트 후");
    }

    @Test
    @Order(3)
    @DisplayName("최대 동시성 스트레스 테스트")
    void measureMaximumConcurrencyStressTest() {
        final int THREAD_COUNT = 30; // 풀 크기(20)보다 많은 쓰레드
        final int QUERIES_PER_THREAD = 20;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Instant start = Instant.now();
        log.info("=== 최대 동시성 스트레스 테스트 시작: {}개 쓰레드 × {}개 쿼리 ===", THREAD_COUNT, QUERIES_PER_THREAD);
        CompletableFuture<ThreadResult>[] futures = IntStream.range(0, THREAD_COUNT)
                .mapToObj(threadId -> CompletableFuture.supplyAsync(() -> {
                    Instant threadStart = Instant.now();
                    int successCount = 0;
                    int errorCount = 0;

                    for (int i = 0; i < QUERIES_PER_THREAD; i++) {
                        try {
                            User user = userMapper.findById(1L);
                            Assertions.assertNotNull(user);
                            successCount++;
                        } catch (Exception e) {
                            errorCount++;
                            log.warn("Thread-{} Query-{} failed: {}", threadId, i + 1, e.getMessage());
                        }
                    }

                    Duration threadElapsed = Duration.between(threadStart, Instant.now());
                    return new ThreadResult(threadId, threadElapsed, successCount, errorCount);
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());
        ThreadResult[] results = java.util.Arrays.stream(futures).map(CompletableFuture::join).toArray(ThreadResult[]::new);
        int totalSuccess = java.util.Arrays.stream(results).mapToInt(r -> r.successCount).sum();
        int totalErrors = java.util.Arrays.stream(results).mapToInt(r -> r.errorCount).sum();
        double avgThreadTime = java.util.Arrays.stream(results).mapToLong(r -> r.duration.toMillis()).average().orElse(0.0);
        double totalQueriesPerSecond = totalSuccess / (totalElapsed.toMillis() / 1000.0);
        log.info("=== 최대 동시성 스트레스 테스트 결과 ===");
        log.info("총 실행 시간: {}ms", totalElapsed.toMillis());
        log.info("평균 쓰레드 실행 시간: {}ms", String.format("%.2f", avgThreadTime));
        log.info("성공한 쿼리: {}개", totalSuccess);
        log.info("실패한 쿼리: {}개", totalErrors);
        log.info("성공률: {}%", String.format("%.2f", (totalSuccess * 100.0) / (totalSuccess + totalErrors)));
        log.info("전체 처리량: {} queries/sec", String.format("%.2f", totalQueriesPerSecond));
        logPoolStatistics("최대 동시성 테스트 후");
        executor.shutdown();
        Assertions.assertTrue(totalQueriesPerSecond > 50, "성능 최우선 설정에서는 50 queries/sec 이상 처리량 기대");
    }

    @Test
    @Order(4)
    @DisplayName("연결 생성 오버헤드 최소화 확인")
    void verifyMinimalConnectionCreationOverhead() {
        log.info("=== 연결 생성 오버헤드 최소화 테스트 ===");
        logPoolStatistics("테스트 시작 전 (사전 생성된 연결 확인)");
        long[] queryTimes = new long[10];
        for (int i = 0; i < 10; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());
            queryTimes[i] = queryTime.toMillis();
            Assertions.assertNotNull(user);
            log.debug("즉시 쿼리 {}: {}ms", i + 1, queryTime.toMillis());
        }

        double avgQueryTime = java.util.Arrays.stream(queryTimes).average().orElse(0.0);
        long maxQueryTime = java.util.Arrays.stream(queryTimes).max().orElse(0L);
        long minQueryTime = java.util.Arrays.stream(queryTimes).min().orElse(0L);
        log.info("=== 연결 생성 오버헤드 테스트 결과 ===");
        log.info("평균 쿼리 시간: {}ms", String.format("%.2f", avgQueryTime));
        log.info("최대 쿼리 시간: {}ms", maxQueryTime);
        log.info("최소 쿼리 시간: {}ms", minQueryTime);
        log.info("시간 편차: {}ms", maxQueryTime - minQueryTime);
        logPoolStatistics("연결 오버헤드 테스트 후");
        Assertions.assertTrue(avgQueryTime < 50, "사전 생성된 연결로 인해 평균 50ms 이하 응답 시간 기대");
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

    private record ThreadResult(int threadId, Duration duration, int successCount, int errorCount) {
    }
}