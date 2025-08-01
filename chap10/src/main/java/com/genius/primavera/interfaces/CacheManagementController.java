package com.genius.primavera.interfaces;

import com.genius.primavera.infrastructure.cache.CacheEvictionStrategy;
import com.genius.primavera.infrastructure.cache.OAuth2TokenCacheService;
import com.genius.primavera.infrastructure.cache.UserProfileCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 캐시 관리 및 모니터링 컨트롤러
 * 
 * 관리자 전용 캐시 관리 기능:
 * - 캐시 상태 모니터링
 * - 수동 캐시 무효화
 * - 캐시 통계 조회
 * - 긴급 캐시 정리
 */
@Slf4j
@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class CacheManagementController {

    private final CacheManager cacheManager;
    private final OAuth2TokenCacheService tokenCacheService;
    private final UserProfileCacheService profileCacheService;
    private final CacheEvictionStrategy cacheEvictionStrategy;

    /**
     * 전체 캐시 상태 대시보드
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getCacheDashboard() {
        log.info("📊 캐시 대시보드 조회 요청");
        
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // 1. 캐시 헬스 체크
            Health cacheHealth = cacheEvictionStrategy.health();
            dashboard.put("health", cacheHealth.getStatus().toString());
            dashboard.put("healthDetails", cacheHealth.getDetails());
            
            // 2. 토큰 캐시 통계
            OAuth2TokenCacheService.CacheStats tokenStats = tokenCacheService.getCacheStats();
            dashboard.put("tokenCache", Map.of(
                    "totalEntries", tokenStats.getTotalEntries(),
                    "validEntries", tokenStats.getValidEntries(),
                    "expiredEntries", tokenStats.getExpiredEntries(),
                    "hitRatio", String.format("%.2f%%", tokenStats.getHitRatio() * 100)
            ));
            
            // 3. 프로필 캐시 통계
            UserProfileCacheService.ProfileCacheStats profileStats = profileCacheService.getCacheStats();
            dashboard.put("profileCache", Map.of(
                    "totalProfiles", profileStats.getTotalProfiles(),
                    "providerDistribution", profileStats.getProviderDistribution(),
                    "averageLoginCount", String.format("%.1f", profileStats.getAverageLoginCount()),
                    "oldestEntry", profileStats.getOldestCacheEntry().toString()
            ));
            
            // 4. 시스템 메모리 정보
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            dashboard.put("memory", Map.of(
                    "total", formatBytes(totalMemory),
                    "used", formatBytes(usedMemory),
                    "free", formatBytes(freeMemory),
                    "usagePercentage", String.format("%.2f%%", (double) usedMemory / totalMemory * 100)
            ));
            
            // 5. 등록된 캐시 목록
            dashboard.put("cacheNames", cacheManager.getCacheNames());
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("❌ 캐시 대시보드 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "캐시 대시보드 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 특정 캐시 상세 정보 조회
     */
    @GetMapping("/{cacheName}/details")
    public ResponseEntity<Map<String, Object>> getCacheDetails(@PathVariable String cacheName) {
        log.info("🔍 캐시 상세 정보 조회 - 캐시명: {}", cacheName);
        
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("name", cacheName);
        details.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
        
        // 캐시별 특화 정보
        switch (cacheName) {
            case "oauth2Tokens" -> {
                OAuth2TokenCacheService.CacheStats stats = tokenCacheService.getCacheStats();
                details.put("statistics", stats);
                details.put("type", "OAuth2 Token Cache");
            }
            case "userProfiles" -> {
                UserProfileCacheService.ProfileCacheStats stats = profileCacheService.getCacheStats();
                details.put("statistics", stats);
                details.put("type", "User Profile Cache");
            }
            default -> details.put("type", "Generic Cache");
        }
        
        return ResponseEntity.ok(details);
    }

    /**
     * 특정 사용자 캐시 무효화
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> evictUserCache(@PathVariable Long userId) {
        log.info("🗑️ 사용자 캐시 무효화 요청 - ID: {}", userId);
        
        try {
            cacheEvictionStrategy.evictUserCaches(userId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "사용자 캐시가 성공적으로 무효화되었습니다.",
                    "userId", userId.toString()
            ));
        } catch (Exception e) {
            log.error("❌ 사용자 캐시 무효화 실패 - ID: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "사용자 캐시 무효화 실패: " + e.getMessage(),
                            "userId", userId.toString()
                    ));
        }
    }

    /**
     * 특정 캐시 전체 클리어
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        log.info("🗑️ 캐시 클리어 요청 - 캐시명: {}", cacheName);
        
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return ResponseEntity.notFound().build();
            }
            
            cache.clear();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "캐시가 성공적으로 클리어되었습니다.",
                    "cacheName", cacheName
            ));
        } catch (Exception e) {
            log.error("❌ 캐시 클리어 실패 - 캐시명: {}", cacheName, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "캐시 클리어 실패: " + e.getMessage(),
                            "cacheName", cacheName
                    ));
        }
    }

    /**
     * 전체 캐시 클리어 (긴급 상황용)
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        log.warn("🚨 전체 캐시 클리어 요청");
        
        try {
            cacheEvictionStrategy.clearAllCaches();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "모든 캐시가 성공적으로 클리어되었습니다.",
                    "clearedCaches", cacheManager.getCacheNames()
            ));
        } catch (Exception e) {
            log.error("❌ 전체 캐시 클리어 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "전체 캐시 클리어 실패: " + e.getMessage()
                    ));
        }
    }

    /**
     * 프로바이더별 캐시 갱신
     */
    @PostMapping("/refresh/provider/{provider}")
    public ResponseEntity<Map<String, String>> refreshProviderCache(@PathVariable String provider) {
        log.info("🔄 프로바이더 캐시 갱신 요청 - 프로바이더: {}", provider);
        
        try {
            cacheEvictionStrategy.refreshProviderCaches(provider);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "프로바이더 캐시가 성공적으로 갱신되었습니다.",
                    "provider", provider
            ));
        } catch (Exception e) {
            log.error("❌ 프로바이더 캐시 갱신 실패 - 프로바이더: {}", provider, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "프로바이더 캐시 갱신 실패: " + e.getMessage(),
                            "provider", provider
                    ));
        }
    }

    /**
     * 캐시 통계 조회 (CSV 다운로드)
     */
    @GetMapping("/statistics/export")
    public ResponseEntity<String> exportCacheStatistics() {
        log.info("📊 캐시 통계 CSV 내보내기 요청");
        
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("캐시명,항목수,히트율,상태\n");
            
            // 토큰 캐시 통계
            OAuth2TokenCacheService.CacheStats tokenStats = tokenCacheService.getCacheStats();
            csv.append(String.format("oauth2Tokens,%d,%.2f%%,활성\n", 
                    tokenStats.getTotalEntries(), tokenStats.getHitRatio() * 100));
            
            // 프로필 캐시 통계
            UserProfileCacheService.ProfileCacheStats profileStats = profileCacheService.getCacheStats();
            csv.append(String.format("userProfiles,%d,N/A,활성\n", profileStats.getTotalProfiles()));
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=cache-statistics.csv")
                    .body(csv.toString());
                    
        } catch (Exception e) {
            log.error("❌ 캐시 통계 내보내기 실패", e);
            return ResponseEntity.internalServerError()
                    .body("캐시 통계 내보내기 실패: " + e.getMessage());
        }
    }

    /**
     * 수동 캐시 정리 트리거
     */
    @PostMapping("/cleanup/manual")
    public ResponseEntity<Map<String, String>> triggerManualCleanup() {
        log.info("🧹 수동 캐시 정리 트리거");
        
        try {
            // 비동기로 캐시 정리 실행
            new Thread(() -> {
                try {
                    cacheEvictionStrategy.cleanupExpiredTokens();
                    log.info("✅ 수동 캐시 정리 완료");
                } catch (Exception e) {
                    log.error("❌ 수동 캐시 정리 실패", e);
                }
            }).start();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "캐시 정리가 백그라운드에서 시작되었습니다."
            ));
        } catch (Exception e) {
            log.error("❌ 수동 캐시 정리 트리거 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "캐시 정리 트리거 실패: " + e.getMessage()
                    ));
        }
    }

    // === Private Helper Methods ===

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}