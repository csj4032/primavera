package com.genius.primavera.hikari;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
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
@ActiveProfiles("hikari-minimal")
@DisplayName("HikariCP 최소 설정 성능 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HikariMinimalPoolTest {

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
        ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool (MinimalHikariPool)");
        poolMXBean = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMXBean.class);
    }

    @Test
    @Order(1)
    @DisplayName("연결 풀 기본 상태 확인")
    void verifyPoolConfiguration() {
        log.info("=== HikariCP 최소 설정 풀 상태 ===");
        log.info("최대 풀 크기: {}", hikariDataSource.getMaximumPoolSize());
        log.info("최소 유휴 연결: {}", hikariDataSource.getMinimumIdle());
        log.info("연결 타임아웃: {}ms", hikariDataSource.getConnectionTimeout());
        log.info("유휴 타임아웃: {}ms", hikariDataSource.getIdleTimeout());
        log.info("최대 생존 시간: {}ms", hikariDataSource.getMaxLifetime());

        Assertions.assertEquals(3, hikariDataSource.getMaximumPoolSize());
        Assertions.assertEquals(1, hikariDataSource.getMinimumIdle());
        Assertions.assertEquals(5000, hikariDataSource.getConnectionTimeout());
    }

    @Test
    @Order(2)
    @DisplayName("단일 연결 성능 측정")
    void measureSingleConnectionPerformance() {
        Instant start = Instant.now();

        // 단순 쿼리 100회 실행
        IntStream.rangeClosed(1, 100)
            .forEach(i -> {
                User user = userMapper.findById(1L);
                Assertions.assertNotNull(user);
            });

        Duration elapsed = Duration.between(start, Instant.now());
        log.info("단일 연결 100회 쿼리 실행 시간: {}ms", elapsed.toMillis());

        logPoolStatistics("단일 연결 테스트 후");
    }

    @Test
    @Order(3)
    @DisplayName("동시 연결 경합 테스트")
    void measureConcurrentConnectionContentionTest() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Instant start = Instant.now();

        // 10개 쓰레드가 동시에 5개씩 쿼리 실행 (총 50개 쿼리)
        CompletableFuture<Void>[] futures = IntStream.range(0, 10)
            .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                for (int i = 0; i < 5; i++) {
                    User user = userMapper.findById(1L);
                    Assertions.assertNotNull(user);
                    log.debug("Thread-{}: Query-{} 완료", threadId, i + 1);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration elapsed = Duration.between(start, Instant.now());

        log.info("=== 동시 연결 경합 테스트 결과 ===");
        log.info("10개 쓰레드 × 5개 쿼리 (총 50개) 실행 시간: {}ms", elapsed.toMillis());
        log.info("평균 쿼리당 시간: {}ms", elapsed.toMillis() / 50.0);

        logPoolStatistics("동시 연결 경합 테스트 후");
        executor.shutdown();
    }

    @Test
    @Order(4)
    @DisplayName("연결 풀 포화 상황 테스트")
    void measurePoolSaturationBehavior() {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        Instant start = Instant.now();

        log.info("=== 연결 풀 포화 테스트 시작 (20개 쓰레드 vs 3개 최대 연결) ===");

        CompletableFuture<Void>[] futures = IntStream.range(0, 20)
            .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                try {
                    Instant threadStart = Instant.now();
                    User user = userMapper.findById(1L);
                    Duration threadElapsed = Duration.between(threadStart, Instant.now());

                    log.info("Thread-{}: 연결 획득 및 쿼리 완료 시간: {}ms",
                        threadId, threadElapsed.toMillis());
                    Assertions.assertNotNull(user);
                } catch (Exception e) {
                    log.error("Thread-{}: 연결 실패 - {}", threadId, e.getMessage());
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        Duration totalElapsed = Duration.between(start, Instant.now());

        log.info("=== 연결 풀 포화 테스트 완료 ===");
        log.info("총 실행 시간: {}ms", totalElapsed.toMillis());
        log.info("최대 연결 수 제한으로 인한 대기 현상 관찰");

        logPoolStatistics("풀 포화 테스트 후");
        executor.shutdown();
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