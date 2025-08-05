package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MariaDBContainer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MariaDB TestContainer 설정 - 테스트 클래스별 독립적인 컨테이너 생성
 * Thread 기반 컨테이너 격리 전략 사용
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class MariaDBContainerConfiguration {

    private static final ConcurrentHashMap<String, MariaDBContainer<?>> containerCache = new ConcurrentHashMap<>();
    private static final AtomicInteger containerCounter = new AtomicInteger(0);
    private static final ThreadLocal<MariaDBContainer<?>> threadLocalContainer = new ThreadLocal<>();

    @Bean
    @ServiceConnection
    @Primary
    public MariaDBContainer<?> mariaDBContainer() {
        // 환경 변수에서 테스트 격리 식별자 추출
        String testIsolationId = System.getProperty("primavera.test.isolation", "Unknown");
        String threadName = Thread.currentThread().getName();
        int containerNumber = containerCounter.incrementAndGet();
        
        // 테스트별 고유 캐시 키 생성
        String cacheKey = testIsolationId + "_" + threadName + "_" + containerNumber + "_" + System.currentTimeMillis();
        
        log.info("★ MariaDB TestContainer 생성 요청:");
        log.info("   - 테스트 격리 ID: {}", testIsolationId);
        log.info("   - 스레드: {}", threadName);
        log.info("   - 컨테이너 번호: {}", containerNumber);
        log.info("   - 캐시키: {}", cacheKey);
        
        // 완전히 독립적인 새 컨테이너 생성
        MariaDBContainer<?> container = new MariaDBContainer<>(ContainerType.MARIADB.getDefaultImage())
                .withUsername("primavera")
                .withPassword("primavera")
                .withDatabaseName("primavera")
                .withInitScript("sql/init.sql")
                .withReuse(false) // 절대 재사용 안함
                .withLabel("test-isolation-id", testIsolationId)
                .withLabel("thread-name", threadName)
                .withLabel("container-number", String.valueOf(containerNumber))
                .withLabel("cache-key", cacheKey)
                .withLabel("creation-time", String.valueOf(System.currentTimeMillis()));
        
        // JVM Identity 추가 (컨테이너 생성 후)
        container = container.withLabel("jvm-id", String.valueOf(System.identityHashCode(container)));
        
        // 캐시에 저장 (디버깅 목적)
        containerCache.put(cacheKey, container);
        
        log.info("★ 새로운 독립 MariaDB 컨테이너 생성 완료:");
        log.info("   - 테스트 격리 ID: {}", testIsolationId);
        log.info("   - JVM Identity: {}", System.identityHashCode(container));
        log.info("   - 캐시키: {}", cacheKey);
        log.info("   - 총 생성된 컨테이너 수: {}", containerCache.size());
        
        return container;
    }

    /**
     * 현재 실행 중인 테스트 클래스명을 추출
     */
    private String getCurrentTestClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        // 스택 트레이스에서 테스트 클래스 찾기
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("test") && 
                (className.endsWith("Test") || 
                 className.endsWith("TestA") || 
                 className.endsWith("TestB"))) {
                return className.substring(className.lastIndexOf('.') + 1);
            }
        }
        
        // Spring 관련 클래스들을 제외하고 테스트 관련 클래스 찾기
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (!className.startsWith("org.springframework") && 
                !className.startsWith("java.") && 
                !className.startsWith("sun.") &&
                className.contains("isolation")) {
                return className.substring(className.lastIndexOf('.') + 1);
            }
        }
        
        return "UnknownTest_" + System.currentTimeMillis();
    }

    /**
     * 캐시된 컨테이너 정보 조회
     */
    public static int getCachedContainerCount() {
        return containerCache.size();
    }

    /**
     * 현재 ThreadLocal 컨테이너 정보
     */
    public static MariaDBContainer<?> getCurrentThreadContainer() {
        return threadLocalContainer.get();
    }

    /**
     * ThreadLocal 정리
     */
    public static void clearThreadLocal() {
        MariaDBContainer<?> container = threadLocalContainer.get();
        if (container != null) {
            log.info("ThreadLocal 컨테이너 정리: {}", container.getContainerId());
        }
        threadLocalContainer.remove();
    }

    /**
     * 전체 캐시 정리
     */
    public static void clearCache() {
        log.info("MariaDB 컨테이너 캐시 정리: {} 개 컨테이너", containerCache.size());
        
        containerCache.values().forEach(container -> {
            try {
                if (container.isRunning()) {
                    log.info("컨테이너 중지: {}", container.getContainerId());
                    container.stop();
                }
            } catch (Exception e) {
                log.warn("컨테이너 중지 중 오류: {}", e.getMessage());
            }
        });
        
        containerCache.clear();
        containerCounter.set(0);
        threadLocalContainer.remove();
        
        log.info("MariaDB 컨테이너 캐시 정리 완료");
    }
}