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
        log.info("🧹 translated_text_3 translated_text_2 translated_text_2 translated_text_2 translated_text_2...");

        try {
            OAuth2TokenCacheService.CacheStats beforeStats = tokenCacheService.getCacheStats();

            long expiredTokensRemoved = cleanupExpiredTokensInternal();

            OAuth2TokenCacheService.CacheStats afterStats = tokenCacheService.getCacheStats();

            cleanupCount.incrementAndGet();
            lastCleanupTime = LocalDateTime.now();

            log.info(" translated_text_2 translated_text_2 translated_text_2 completed - translated_text_3 translated_text_2: {}, translated_text_2 translated_text_2: {}",
                    expiredTokensRemoved, afterStats.getValidEntries());

        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_2 translated_text_2 failure", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyCacheOptimization() {
        log.info(" translated_text_2 translated_text_2 translated_text_3 translated_text_2...");

        try {

            collectCacheStatistics();

            cleanupLeastRecentlyUsedEntries();

            compressCacheIfNeeded();

            warmupFrequentlyUsedCache();

            log.info(" translated_text_2 translated_text_2 translated_text_3 completed");

        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_2 translated_text_3 failure", e);
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
                log.warn(" translated_text_2 translated_text_3 translated_text_3 translated_text_2: {:.2f}% - translated_text_2 translated_text_2 translated_text_2 translated_text_2", memoryUsagePercentage);
                emergencyCacheCleanup();
            } else if (memoryUsagePercentage > 70.0) {
                log.info(" translated_text_3 translated_text_3: {:.2f}% - translated_text_3 translated_text_2 translated_text_2", memoryUsagePercentage);
                proactiveCacheCleanup();
            }

        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_3 translated_text_4 failure", e);
        }
    }

    public void evictUserCaches(Long userId) {
        log.info(" user translated_text_2 translated_text_3 translated_text_2 - ID: {}", userId);

        try {

            profileCacheService.evictUserProfile(userId);

            tokenCacheService.evictAllUserTokens(String.valueOf(userId));

            evictionCount.incrementAndGet();

            log.info(" user translated_text_2 translated_text_3 completed - ID: {}", userId);

        } catch (Exception e) {
            log.error(" user translated_text_2 translated_text_3 failure - ID: {}", userId, e);
        }
    }

    public void refreshProviderCaches(String provider) {
        log.info(" translated_text_5 translated_text_2 translated_text_2 - translated_text_5: {}", provider);

        try {
            profileCacheService.refreshProviderUsers(provider);
            log.info(" translated_text_5 translated_text_2 translated_text_2 completed - translated_text_5: {}", provider);

        } catch (Exception e) {
            log.error(" translated_text_5 translated_text_2 translated_text_2 failure - translated_text_5: {}", provider, e);
        }
    }

    public void clearAllCaches() {
        log.warn(" translated_text_2 translated_text_2 translated_text_3 translated_text_2 - translated_text_2 translated_text_2");

        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info(" translated_text_2 translated_text_3: {}", cacheName);
                }
            });

            evictionCount.addAndGet(10);

            log.warn(" translated_text_2 translated_text_2 translated_text_3 completed");

        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_2 translated_text_3 failure", e);
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
        log.debug(" translated_text_2 translated_text_2 translated_text_2 translated_text_1...");

    }

    private void cleanupLeastRecentlyUsedEntries() {
        log.debug("🧹 LRU translated_text_2 translated_text_2 translated_text_2 translated_text_1...");

    }

    private void compressCacheIfNeeded() {
        log.debug(" translated_text_2 translated_text_2 translated_text_2 translated_text_1...");

    }

    private void warmupFrequentlyUsedCache() {
        log.debug(" translated_text_2 translated_text_3 translated_text_1...");

    }

    private void emergencyCacheCleanup() {
        log.warn(" translated_text_2 translated_text_2 translated_text_2 execution");

        proactiveCacheCleanup();
    }

    private void proactiveCacheCleanup() {
        log.info("🧹 translated_text_3 translated_text_2 translated_text_2 execution");

        cleanupExpiredTokens();
    }
}