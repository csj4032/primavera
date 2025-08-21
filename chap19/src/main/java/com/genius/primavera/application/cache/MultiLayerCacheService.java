package com.genius.primavera.application.cache;

import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Service
public class MultiLayerCacheService {
    
    private final Cache<String, Object> l1Cache;
    private final ReactiveRedisTemplate<String, Object> l2Cache;
    private final MeterRegistry meterRegistry;
    private final Map<String, CacheStatistics> statistics = new ConcurrentHashMap<>();
    
    public MultiLayerCacheService(
            @Qualifier("caffeineCache") Cache<String, Object> l1Cache,
            ReactiveRedisTemplate<String, Object> l2Cache,
            MeterRegistry meterRegistry) {
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
        this.meterRegistry = meterRegistry;
    }
    
    public <T> Mono<T> get(String key, Class<T> type, Supplier<Mono<T>> loader) {
        var stats = statistics.computeIfAbsent(key, k -> new CacheStatistics(k));
        
        // L1 Cache Check
        return Mono.justOrEmpty(l1Cache.getIfPresent(key))
            .cast(type)
            .doOnNext(v -> {
                recordCacheHit("L1", key);
                stats.incrementL1Hits();
                log.debug("L1 cache hit for key: {}", key);
            })
            .switchIfEmpty(
                // L2 Cache Check
                l2Cache.opsForValue().get(key)
                    .cast(type)
                    .doOnNext(v -> {
                        recordCacheHit("L2", key);
                        stats.incrementL2Hits();
                        l1Cache.put(key, v);
                        log.debug("L2 cache hit for key: {}, promoting to L1", key);
                    })
            )
            .switchIfEmpty(
                // Load from source
                loader.get()
                    .doOnNext(v -> {
                        recordCacheMiss(key);
                        stats.incrementMisses();
                        // Store in all layers
                        l1Cache.put(key, v);
                        l2Cache.opsForValue()
                            .set(key, v, Duration.ofMinutes(10))
                            .subscribe(
                                success -> log.debug("Stored key {} in L2 cache", key),
                                error -> log.error("Failed to store in L2 cache: {}", error.getMessage())
                            );
                        log.debug("Cache miss for key: {}, loaded from source", key);
                    })
            );
    }
    
    public Mono<Void> evict(String key) {
        return Mono.fromRunnable(() -> {
            l1Cache.invalidate(key);
            log.debug("Evicted key {} from L1 cache", key);
        })
        .then(l2Cache.delete(key))
        .doOnSuccess(v -> log.debug("Evicted key {} from L2 cache", key))
        .then();
    }
    
    public Mono<Void> evictAll(List<String> keys) {
        return Flux.fromIterable(keys)
            .flatMap(this::evict)
            .then();
    }
    
    public Mono<Void> clear() {
        return Mono.fromRunnable(() -> {
            l1Cache.invalidateAll();
            log.info("Cleared L1 cache");
        })
        .then(l2Cache.getConnectionFactory()
            .getReactiveConnection()
            .serverCommands()
            .flushAll())
        .doOnSuccess(v -> log.info("Cleared L2 cache"))
        .then();
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("Starting cache warmup...");
        
        var criticalKeys = List.of(
            "config:system",
            "user:popular:top10",
            "product:featured",
            "metadata:application"
        );
        
        Flux.fromIterable(criticalKeys)
            .flatMap(this::preloadData)
            .doOnComplete(() -> log.info("Cache warmup completed with {} keys", criticalKeys.size()))
            .subscribe();
    }
    
    private Mono<Void> preloadData(String key) {
        return Mono.defer(() -> {
            // Simulate loading critical data
            var data = generateMockData(key);
            l1Cache.put(key, data);
            return l2Cache.opsForValue()
                .set(key, data, Duration.ofHours(1))
                .then()
                .doOnSuccess(v -> log.debug("Preloaded key: {}", key));
        });
    }
    
    private Object generateMockData(String key) {
        return Map.of(
            "key", key,
            "timestamp", Instant.now().toString(),
            "data", "Mock data for " + key
        );
    }
    
    public CacheMetrics getMetrics() {
        var l1Stats = l1Cache.stats();
        
        return CacheMetrics.builder()
            .l1HitRate(l1Stats.hitRate())
            .l1MissRate(l1Stats.missRate())
            .l1EvictionCount(l1Stats.evictionCount())
            .l1Size(l1Cache.estimatedSize())
            .statistics(Map.copyOf(statistics))
            .build();
    }
    
    private void recordCacheHit(String layer, String key) {
        meterRegistry.counter("cache.hit", 
            "layer", layer, 
            "key", simplifyKey(key)
        ).increment();
    }
    
    private void recordCacheMiss(String key) {
        meterRegistry.counter("cache.miss", 
            "key", simplifyKey(key)
        ).increment();
    }
    
    private String simplifyKey(String key) {
        // Extract key pattern for metrics
        return key.split(":")[0];
    }
    
    @Data
    @Builder
    public static class CacheMetrics {
        private double l1HitRate;
        private double l1MissRate;
        private long l1EvictionCount;
        private long l1Size;
        private Map<String, CacheStatistics> statistics;
    }
    
    @Data
    public static class CacheStatistics {
        private final String key;
        private long l1Hits = 0;
        private long l2Hits = 0;
        private long misses = 0;
        private final Instant createdAt = Instant.now();
        
        public synchronized void incrementL1Hits() {
            l1Hits++;
        }
        
        public synchronized void incrementL2Hits() {
            l2Hits++;
        }
        
        public synchronized void incrementMisses() {
            misses++;
        }
        
        public double getHitRate() {
            long total = l1Hits + l2Hits + misses;
            return total == 0 ? 0 : (double)(l1Hits + l2Hits) / total;
        }
    }
}