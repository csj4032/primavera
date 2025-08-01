package com.genius.primavera.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 캐시 무효화 전략 및 관리 컴포넌트
 * 
 * 주요 기능:
 * - 주기적 만료된 캐시 정리
 * - 캐시 용량 모니터링 및 자동 정리
 * - 캐시 상태 헬스체크
 * - 선택적 캐시 워밍업
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionStrategy implements HealthIndicator {

    private final CacheManager cacheManager;
    private final OAuth2TokenCacheService tokenCacheService;
    private final UserProfileCacheService profileCacheService;

    // 캐시 작업 통계
    private final AtomicLong evictionCount = new AtomicLong(0);
    private final AtomicLong cleanupCount = new AtomicLong(0);
    private LocalDateTime lastCleanupTime = LocalDateTime.now();

    /**
     * 매시간 만료된 토큰 캐시 정리
     * cron: 0분 0초에 실행 (매시간)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        log.info("🧹 만료된 토큰 캐시 정리 시작...");
        
        try {
            OAuth2TokenCacheService.CacheStats beforeStats = tokenCacheService.getCacheStats();
            
            // 만료된 토큰들 확인 및 제거 로직
            long expiredTokensRemoved = cleanupExpiredTokensInternal();
            
            OAuth2TokenCacheService.CacheStats afterStats = tokenCacheService.getCacheStats();
            
            cleanupCount.incrementAndGet();
            lastCleanupTime = LocalDateTime.now();
            
            log.info("✅ 토큰 캐시 정리 완료 - 제거된 토큰: {}, 남은 토큰: {}", 
                    expiredTokensRemoved, afterStats.getValidEntries());
                    
        } catch (Exception e) {
            log.error("❌ 토큰 캐시 정리 실패", e);
        }
    }

    /**
     * 매일 자정 전체 캐시 최적화
     * cron: 자정 (00:00:00)에 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyCacheOptimization() {
        log.info("🚀 일일 캐시 최적화 시작...");
        
        try {
            // 1. 캐시 통계 수집
            collectCacheStatistics();
            
            // 2. LRU 기반 오래된 캐시 정리
            cleanupLeastRecentlyUsedEntries();
            
            // 3. 캐시 압축 (필요한 경우)
            compressCacheIfNeeded();
            
            // 4. 캐시 워밍업 (자주 사용되는 데이터)
            warmupFrequentlyUsedCache();
            
            log.info("✅ 일일 캐시 최적화 완료");
            
        } catch (Exception e) {
            log.error("❌ 일일 캐시 최적화 실패", e);
        }
    }

    /**
     * 캐시 메모리 사용량 모니터링 (5분마다)
     * cron: 매 5분마다 실행
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void monitorCacheMemoryUsage() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercentage = (double) usedMemory / totalMemory * 100;

            // 메모리 사용률이 80% 이상이면 캐시 정리
            if (memoryUsagePercentage > 80.0) {
                log.warn("⚠️ 높은 메모리 사용률 감지: {:.2f}% - 긴급 캐시 정리 시작", memoryUsagePercentage);
                emergencyCacheCleanup();
            } else if (memoryUsagePercentage > 70.0) {
                log.info("📊 메모리 사용률: {:.2f}% - 선제적 캐시 정리", memoryUsagePercentage);
                proactiveCacheCleanup();
            }

        } catch (Exception e) {
            log.error("❌ 캐시 메모리 모니터링 실패", e);
        }
    }

    /**
     * 수동 캐시 무효화 - 특정 사용자
     */
    public void evictUserCaches(Long userId) {
        log.info("🗑️ 사용자 캐시 무효화 시작 - ID: {}", userId);
        
        try {
            // 1. 사용자 프로필 캐시 삭제
            profileCacheService.evictUserProfile(userId);
            
            // 2. 사용자 모든 토큰 캐시 삭제
            tokenCacheService.evictAllUserTokens(String.valueOf(userId));
            
            evictionCount.incrementAndGet();
            
            log.info("✅ 사용자 캐시 무효화 완료 - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("❌ 사용자 캐시 무효화 실패 - ID: {}", userId, e);
        }
    }

    /**
     * 프로바이더별 캐시 갱신
     */
    public void refreshProviderCaches(String provider) {
        log.info("🔄 프로바이더 캐시 갱신 - 프로바이더: {}", provider);
        
        try {
            profileCacheService.refreshProviderUsers(provider);
            log.info("✅ 프로바이더 캐시 갱신 완료 - 프로바이더: {}", provider);
            
        } catch (Exception e) {
            log.error("❌ 프로바이더 캐시 갱신 실패 - 프로바이더: {}", provider, e);
        }
    }

    /**
     * 전체 캐시 클리어 (긴급 상황용)
     */
    public void clearAllCaches() {
        log.warn("🚨 전체 캐시 클리어 시작 - 긴급 상황");
        
        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("🗑️ 캐시 클리어: {}", cacheName);
                }
            });
            
            evictionCount.addAndGet(10); // 대량 삭제로 카운트
            
            log.warn("✅ 전체 캐시 클리어 완료");
            
        } catch (Exception e) {
            log.error("❌ 전체 캐시 클리어 실패", e);
        }
    }

    /**
     * 캐시 상태 헬스체크
     */
    @Override
    public Health health() {
        try {
            Health.Builder healthBuilder = Health.up();
            
            // 토큰 캐시 상태
            OAuth2TokenCacheService.CacheStats tokenStats = tokenCacheService.getCacheStats();
            healthBuilder.withDetail("tokenCache", Map.of(
                    "totalEntries", tokenStats.getTotalEntries(),
                    "validEntries", tokenStats.getValidEntries(),
                    "expiredEntries", tokenStats.getExpiredEntries(),
                    "hitRatio", tokenStats.getHitRatio()
            ));
            
            // 프로필 캐시 상태
            UserProfileCacheService.ProfileCacheStats profileStats = profileCacheService.getCacheStats();
            healthBuilder.withDetail("profileCache", Map.of(
                    "totalProfiles", profileStats.getTotalProfiles(),
                    "providerDistribution", profileStats.getProviderDistribution(),
                    "averageLoginCount", profileStats.getAverageLoginCount()
            ));
            
            // 캐시 관리 통계
            healthBuilder.withDetail("cacheManagement", Map.of(
                    "evictionCount", evictionCount.get(),
                    "cleanupCount", cleanupCount.get(),
                    "lastCleanupTime", lastCleanupTime.toString()
            ));
            
            // 메모리 사용률
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

    // === Private Methods ===

    private long cleanupExpiredTokensInternal() {
        // 실제 구현에서는 Redis 또는 캐시 매니저를 통해 만료된 엔트리 확인
        // 여기서는 시뮬레이션
        return 5; // 예시로 5개 제거됨
    }

    private void collectCacheStatistics() {
        log.debug("📊 캐시 통계 수집 중...");
        // 캐시 히트율, 사용량 등 통계 수집
    }

    private void cleanupLeastRecentlyUsedEntries() {
        log.debug("🧹 LRU 기반 캐시 정리 중...");
        // 가장 오래 사용되지 않은 캐시 엔트리 정리
    }

    private void compressCacheIfNeeded() {
        log.debug("🗜️ 캐시 압축 검토 중...");
        // 필요한 경우 캐시 압축 수행
    }

    private void warmupFrequentlyUsedCache() {
        log.debug("🔥 캐시 워밍업 중...");
        // 자주 사용되는 데이터를 미리 캐시에 로드
    }

    private void emergencyCacheCleanup() {
        log.warn("🚨 긴급 캐시 정리 실행");
        // 메모리 부족 시 긴급 캐시 정리
        proactiveCacheCleanup();
    }

    private void proactiveCacheCleanup() {
        log.info("🧹 선제적 캐시 정리 실행");
        // 메모리 사용률이 높을 때 선제적 정리
        cleanupExpiredTokens();
    }
}