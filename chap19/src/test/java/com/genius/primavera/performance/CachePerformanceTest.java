package com.genius.primavera.performance;

import com.genius.primavera.application.cache.MultiLayerCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Cache Performance Tests")
class CachePerformanceTest {
    
    @Autowired
    private MultiLayerCacheService cacheService;
    
    private final AtomicInteger loadCounter = new AtomicInteger(0);
    
    @BeforeEach
    void setUp() {
        cacheService.clear().block();
        loadCounter.set(0);
    }
    
    @Test
    @DisplayName("L1 cache should provide fastest access")
    void l1CacheShouldProvideFastestAccess() {
        // Given
        String key = "performance-test-l1";
        String value = "test-value";
        
        // First call - cache miss, load from source
        var firstCall = measureTime(() -> 
            cacheService.get(key, String.class, () -> {
                loadCounter.incrementAndGet();
                return Mono.just(value).delayElement(Duration.ofMillis(10));
            }).block()
        );
        
        // Second call - L1 cache hit
        var secondCall = measureTime(() -> 
            cacheService.get(key, String.class, () -> {
                loadCounter.incrementAndGet();
                return Mono.just(value).delayElement(Duration.ofMillis(10));
            }).block()
        );
        
        // Then
        assertThat(firstCall.duration).isGreaterThan(10); // Should include delay
        assertThat(secondCall.duration).isLessThan(5);    // Should be fast from L1
        assertThat(loadCounter.get()).isEqualTo(1);        // Only loaded once
        
        System.out.printf("First call (cache miss): %d ms\n", firstCall.duration);
        System.out.printf("Second call (L1 hit): %d ms\n", secondCall.duration);
        System.out.printf("Performance improvement: %.2fx\n", 
            (double) firstCall.duration / secondCall.duration);
    }
    
    @Test
    @DisplayName("Cache metrics should track hit rates accurately")
    void cacheMetricsShouldTrackHitRatesAccurately() {
        // Given
        String keyPrefix = "metrics-test-";
        int requests = 100;
        
        // Warm up cache with some keys
        for (int i = 0; i < 10; i++) {
            String key = keyPrefix + i;
            cacheService.get(key, String.class, () -> {
                loadCounter.incrementAndGet();
                return Mono.just("value-" + i);
            }).block();
        }
        
        // Make repeated requests (should hit cache)
        for (int i = 0; i < requests; i++) {
            String key = keyPrefix + (i % 10); // Repeat same 10 keys
            cacheService.get(key, String.class, () -> {
                loadCounter.incrementAndGet();
                return Mono.just("value-" + (i % 10));
            }).block();
        }
        
        // When
        var metrics = cacheService.getMetrics();
        
        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getL1HitRate()).isGreaterThan(0.8); // Should have high hit rate
        assertThat(loadCounter.get()).isEqualTo(10); // Only loaded 10 unique keys
        
        System.out.printf("L1 Hit Rate: %.2f%%\n", metrics.getL1HitRate() * 100);
        System.out.printf("L1 Miss Rate: %.2f%%\n", metrics.getL1MissRate() * 100);
        System.out.printf("Cache Size: %d\n", metrics.getL1Size());
        System.out.printf("Load Counter: %d\n", loadCounter.get());
    }
    
    @Test
    @DisplayName("Cache eviction should work correctly")
    void cacheEvictionShouldWorkCorrectly() {
        // Given
        String key = "eviction-test";
        String value = "test-value";
        
        // Load into cache
        cacheService.get(key, String.class, () -> {
            loadCounter.incrementAndGet();
            return Mono.just(value);
        }).block();
        
        assertThat(loadCounter.get()).isEqualTo(1);
        
        // Verify it's cached
        cacheService.get(key, String.class, () -> {
            loadCounter.incrementAndGet();
            return Mono.just(value);
        }).block();
        
        assertThat(loadCounter.get()).isEqualTo(1); // Should still be 1
        
        // When - evict the key
        cacheService.evict(key).block();
        
        // Then - should reload from source
        cacheService.get(key, String.class, () -> {
            loadCounter.incrementAndGet();
            return Mono.just(value);
        }).block();
        
        assertThat(loadCounter.get()).isEqualTo(2); // Should increment
    }
    
    @Test
    @DisplayName("Concurrent cache access should be thread-safe")
    void concurrentCacheAccessShouldBeThreadSafe() {
        // Given
        String key = "concurrent-test";
        int threadCount = 50;
        
        // When - multiple concurrent requests
        var results = reactor.core.publisher.Flux.range(0, threadCount)
            .parallel(threadCount)
            .flatMap(i -> 
                cacheService.get(key, String.class, () -> {
                    loadCounter.incrementAndGet();
                    return Mono.just("shared-value")
                        .delayElement(Duration.ofMillis(10));
                })
            )
            .sequential()
            .collectList()
            .block();
        
        // Then
        assertThat(results).hasSize(threadCount);
        assertThat(results).allMatch(result -> "shared-value".equals(result));
        
        // Should only load once despite concurrent requests
        await().atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> 
                assertThat(loadCounter.get()).isEqualTo(1)
            );
        
        System.out.printf("Concurrent requests: %d\n", threadCount);
        System.out.printf("Source loads: %d\n", loadCounter.get());
        System.out.printf("Efficiency: %.2f%%\n", 
            (1.0 - (double) loadCounter.get() / threadCount) * 100);
    }
    
    private TimedResult<String> measureTime(java.util.function.Supplier<String> operation) {
        long start = System.currentTimeMillis();
        String result = operation.get();
        long duration = System.currentTimeMillis() - start;
        return new TimedResult<>(result, duration);
    }
    
    private record TimedResult<T>(T result, long duration) {}
}