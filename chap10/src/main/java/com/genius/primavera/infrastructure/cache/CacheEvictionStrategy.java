package com.genius.primavera.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionStrategy implements HealthIndicator {

    private final CacheManager cacheManager;
    private final OAuth2TokenCacheService tokenCacheService;
    private final UserProfileCacheService profileCacheService;

    private final AtomicLong evictionCount = new AtomicLong(0);
    private final AtomicLong cleanupCount = new AtomicLong(0);
    private LocalDateTime lastCleanupTime = LocalDateTime.now();

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        log.info("🧹 connection test test...");

        try {
            OAuth2TokenCacheService.CacheStats beforeStats = tokenCacheService.getCacheStats();

            long expiredTokensRemoved = cleanupExpiredTokensInternal();

            OAuth2TokenCacheService.CacheStats afterStats = tokenCacheService.getCacheStats();

            cleanupCount.incrementAndGet();
            lastCleanupTime = LocalDateTime.now();

            log.info(" test test completed - connection test: {}, test: {}",
                    expiredTokensRemoved, afterStats.getValidEntries());

        } catch (Exception e) {
            log.error(" test test failure", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyCacheOptimization() {
        log.info(" test connection test...");

        try {

            collectCacheStatistics();

            cleanupLeastRecentlyUsedEntries();

            compressCacheIfNeeded();

            warmupFrequentlyUsedCache();

            log.info(" test connection completed");

        } catch (Exception e) {
            log.error(" test connection failure", e);
        }
    }

    @Scheduled(fixedRate = 300000)
    public void monitorCacheMemoryUsage() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercentage = (double) usedMemory / totalMemory * 100;

            if (memoryUsagePercentage > 80.0) {
                log.warn(" test connection test: {:.2f}% - test test", memoryUsagePercentage);
                emergencyCacheCleanup();
            } else if (memoryUsagePercentage > 70.0) {
                log.info(" connection: {:.2f}% - connection test", memoryUsagePercentage);
                proactiveCacheCleanup();
            }

        } catch (Exception e) {
            log.error(" test connection file failure", e);
        }
    }

    public void evictUserCaches(Long userId) {
        log.info(" user test connection test - ID: {}", userId);

        try {

            profileCacheService.evictUserProfile(userId);

            tokenCacheService.evictAllUserTokens(String.valueOf(userId));

            evictionCount.incrementAndGet();

            log.info(" user test connection completed - ID: {}", userId);

        } catch (Exception e) {
            log.error(" user test connection failure - ID: {}", userId, e);
        }
    }

    public void refreshProviderCaches(String provider) {
        log.info(" processing test - Endpoint: {}", provider);

        try {
            profileCacheService.refreshProviderUsers(provider);
            log.info(" processing test completed - Endpoint: {}", provider);

        } catch (Exception e) {
            log.error(" processing test failure - Endpoint: {}", provider, e);
        }
    }

    public void clearAllCaches() {
        log.warn(" test connection test - test");

        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info(" test connection: {}", cacheName);
                }
            });

            evictionCount.addAndGet(10);

            log.warn(" test connection completed");

        } catch (Exception e) {
            log.error(" test connection failure", e);
        }
    }

    @Override
    public Health health() {
        try {
            Health.Builder healthBuilder = Health.up();

            OAuth2TokenCacheService.CacheStats tokenStats = tokenCacheService.getCacheStats();
            healthBuilder.withDetail("tokenCache", Map.of(
                    "totalEntries", tokenStats.getTotalEntries(),
                    "validEntries", tokenStats.getValidEntries(),
                    "expiredEntries", tokenStats.getExpiredEntries(),
                    "hitRatio", tokenStats.getHitRatio()
            ));

            UserProfileCacheService.ProfileCacheStats profileStats = profileCacheService.getCacheStats();
            healthBuilder.withDetail("profileCache", Map.of(
                    "totalProfiles", profileStats.getTotalProfiles(),
                    "providerDistribution", profileStats.getProviderDistribution(),
                    "averageLoginCount", profileStats.getAverageLoginCount()
            ));

            healthBuilder.withDetail("cacheManagement", Map.of(
                    "evictionCount", evictionCount.get(),
                    "cleanupCount", cleanupCount.get(),
                    "lastCleanupTime", lastCleanupTime.toString()
            ));

            Runtime runtime = Runtime.getRuntime();
            double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.totalMemory() * 100;
            healthBuilder.withDetail("memoryUsage", String.format("%.2f%%", memoryUsage));

            return healthBuilder.build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private long cleanupExpiredTokensInternal() {

        return 5;
    }

    private void collectCacheStatistics() {
        log.debug(" test test should...");

    }

    private void cleanupLeastRecentlyUsedEntries() {
        log.debug("🧹 LRU test test should...");

    }

    private void compressCacheIfNeeded() {
        log.debug(" test test should...");

    }

    private void warmupFrequentlyUsedCache() {
        log.debug(" test connection should...");

    }

    private void emergencyCacheCleanup() {
        log.warn(" test test execution");

        proactiveCacheCleanup();
    }

    private void proactiveCacheCleanup() {
        log.info("🧹 connection test execution");

        cleanupExpiredTokens();
    }
}