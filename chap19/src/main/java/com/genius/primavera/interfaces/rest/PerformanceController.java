package com.genius.primavera.interfaces.rest;

import com.genius.primavera.application.cache.MultiLayerCacheService;
import com.genius.primavera.application.database.QueryOptimizationService;
import com.genius.primavera.application.monitoring.PerformanceMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    
    private final QueryOptimizationService queryOptimizationService;
    private final MultiLayerCacheService cacheService;
    private final PerformanceMonitorService monitorService;
    
    @GetMapping("/query/optimized")
    public Mono<QueryOptimizationService.QueryResult<List<com.genius.primavera.domain.model.User>>> 
            getOptimizedQuery() {
        return Mono.fromCallable(queryOptimizationService::findUsersOptimized);
    }
    
    @GetMapping("/query/nplus1")
    public Mono<QueryOptimizationService.QueryResult<List<com.genius.primavera.domain.model.User>>> 
            getNPlusOneQuery() {
        return Mono.fromCallable(queryOptimizationService::findUsersWithNPlusOne);
    }
    
    @GetMapping("/query/entity-graph")
    public Mono<QueryOptimizationService.QueryResult<List<com.genius.primavera.domain.model.User>>> 
            getEntityGraphQuery() {
        return Mono.fromCallable(queryOptimizationService::findUsersWithEntityGraph);
    }
    
    @PostMapping("/query/batch-insert")
    public Mono<QueryOptimizationService.BatchResult> batchInsert(@RequestParam int count) {
        var userData = IntStream.range(0, count)
            .mapToObj(i -> QueryOptimizationService.UserData.builder()
                .email("user" + i + "@example.com")
                .name("User " + i)
                .password("password" + i)
                .active(true)
                .build())
            .toList();
        
        return Mono.fromCallable(() -> queryOptimizationService.batchInsertUsers(userData));
    }
    
    @GetMapping("/cache/metrics")
    public Mono<MultiLayerCacheService.CacheMetrics> getCacheMetrics() {
        return Mono.fromCallable(cacheService::getMetrics);
    }
    
    @PostMapping("/cache/test/{key}")
    public Mono<String> testCache(@PathVariable String key) {
        return cacheService.get(key, String.class, 
            () -> Mono.just("Generated value for " + key));
    }
    
    @DeleteMapping("/cache/evict/{key}")
    public Mono<Void> evictCache(@PathVariable String key) {
        return cacheService.evict(key);
    }
    
    @DeleteMapping("/cache/clear")
    public Mono<Void> clearCache() {
        return cacheService.clear();
    }
    
    @GetMapping("/system/metrics")
    public Mono<PerformanceMonitorService.SystemMetrics> getSystemMetrics() {
        return Mono.fromCallable(monitorService::getSystemMetrics);
    }
    
    @GetMapping("/gc/statistics")
    public Mono<List<PerformanceMonitorService.GCInfo>> getGCStatistics() {
        return Mono.fromCallable(monitorService::getGCStatistics);
    }
}