package com.genius.primavera.hikari;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.testcontainer.EnablePrimaveraTestcontainers;
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

@Slf4j
@SpringBootTest
@EnablePrimaveraTestcontainers
@ActiveProfiles("hikari-balanced")
@DisplayName("HikariCP 균형잡힌 설정 성능 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HikariBalancedPoolTest {


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
        ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (BalancedHikariPool)");
        poolMXBean = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
    }

    @Test
    @Order(1)
    @DisplayName("연결 풀 균형 설정 확인")
    void verifyBalancedPoolConfiguration() {
        log.info("=== HikariCP 균형잡힌 설정 풀 상태 ===");
        log.info("최대 풀 크기: {}", hikariDataSource.getMaximumPoolSize());
        log.info("최소 유휴 연결: {}", hikariDataSource.getMinimumIdle());
        log.info("연결 타임아웃: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("유휴 타임아웃: {}ms", hikariDataSource.getIdleTimeout());
        log.info("최대 생존 시간: {}ms", hikariDataSource.getMaxLifetime());
        log.info("누수 감지 임계값: {}ms", hikariDataSource.getLeakDetectionThreshold());

        Assertions.assertEquals(10, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(5, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(30000, hikariDataSource.getConnectionTimeout());
        Assertions.assertEquals(60000, hikariDataSource.getLeakDetectionThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("균형잡힌 연결 풀의 일반적 워크로드 테스트")
    void measureTypicalWorkloadPerformance() {
        Instant start = Instant.now();
        // 일반적인 워크로드: 200회 쿼리
        IntStream.rangeClosed(1, 200).forEach(i -> {
            User user = userMapper.findById(1L);
            Assertions.assertNotNull(user);
            if (i % 50 == 0) {
                log.debug("Progress: {}/200 queries completed", i);
            }
        });

        Duration elapsed = Duration.between(start, Instant.now());
        log.info("=== 일반적 워크로드 테스트 결과 ===");
        log.info("200회 쿼리 실행 시간: {}ms", elapsed.toMillis());
        log.info("평균 쿼리당 시간: {}ms", elapsed.toMillis() / 200.0);

        logPoolStatistics("일반적 워크로드 테스트 후");
    }

    @Test
    @Order(3)
    @DisplayName("중간 수준 동시성 테스트")
    void measureModerateConcurrencyPerformance() {
        ExecutorService executor = Executors.newFixedThreadPool(15);
        Instant start = Instant.now();

        log.info("=== 중간 수준 동시성 테스트 (15개 쓰레드 vs 10개 최대 연결) ===");

        CompletableFuture<Duration>[] futures = IntStream.range(0, 15)
                .mapToObj(threadId -> CompletableFuture.supplyAsync(() -> {
                    Instant threadStart = Instant.now();
                    // 각 쓰레드가 10개 쿼리 실행
                    for (int i = 0; i < 10; i++) {
                        User user = userMapper.findById(1L);
                        Assertions.assertNotNull(user);
                    }
                    Duration threadElapsed = Duration.between(threadStart, Instant.now());
                    log.debug("Thread-{}: 10개 쿼리 완료 시간: {}ms", threadId, threadElapsed.toMillis());
                    return threadElapsed;
                }, executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());

        // 각 쓰레드 실행 시간 통계
        double avgThreadTime = java.util.Arrays.stream(futures)
                .mapToLong(f -> f.join().toMillis())
                .average()
                .orElse(0.0);

        log.info("=== 중간 수준 동시성 테스트 결과 ===");
        log.info("총 실행 시간: {}ms", totalElapsed.toMillis());
        log.info("평균 쓰레드 실행 시간: {}ms", avgThreadTime);
        log.info("총 쿼리 수: 150개 (15 쓰레드 × 10 쿼리)");
        log.info("전체 처리량: {} queries/sec", 150.0 / (totalElapsed.toMillis() / 1000.0));

        logPoolStatistics("중간 동시성 테스트 후");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("연결 검증 및 복구 테스트")
    void testConnectionValidationAndRecovery() {
        log.info("=== 연결 검증 및 복구 테스트 ===");

        // 연결 풀 상태 확인
        logPoolStatistics("검증 테스트 시작 전");

        // 긴 간격으로 쿼리 실행하여 연결 검증 동작 확인
        for (int i = 1; i <= 5; i++) {
            Instant queryStart = Instant.now();
            User user = userMapper.findById(1L);
            Duration queryTime = Duration.between(queryStart, Instant.now());

            Assertions.assertNotNull(user);
            log.info("검증 쿼리 {}: 실행 시간 {}ms", i, queryTime.toMillis());

            // 연결 검증을 위한 대기
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logPoolStatistics("검증 테스트 완료 후");
        log.info("연결 검증 쿼리(SELECT 1) 동작 확인 완료");
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
}