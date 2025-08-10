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

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getCacheDashboard() {
        log.info(" translated_text_2 translated_text_4 inquiry translated_text_2");
        
        Map<String, Object> dashboard = new HashMap<>();
        
        try {

            Health cacheHealth = cacheEvictionStrategy.health();
            dashboard.put("health", cacheHealth.getStatus().toString());
            dashboard.put("healthDetails", cacheHealth.getDetails());

            OAuth2TokenCacheService.CacheStats tokenStats = tokenCacheService.getCacheStats();
            dashboard.put("tokenCache", Map.of(
                    "totalEntries", tokenStats.getTotalEntries(),
                    "validEntries", tokenStats.getValidEntries(),
                    "expiredEntries", tokenStats.getExpiredEntries(),
                    "hitRatio", String.format("%.2f%%", tokenStats.getHitRatio() * 100)
            ));

            UserProfileCacheService.ProfileCacheStats profileStats = profileCacheService.getCacheStats();
            dashboard.put("profileCache", Map.of(
                    "totalProfiles", profileStats.getTotalProfiles(),
                    "providerDistribution", profileStats.getProviderDistribution(),
                    "averageLoginCount", String.format("%.1f", profileStats.getAverageLoginCount()),
                    "oldestEntry", profileStats.getOldestCacheEntry().toString()
            ));

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

            dashboard.put("cacheNames", cacheManager.getCacheNames());
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_4 inquiry failure", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "translated_text_2 translated_text_4 inquiry failure: " + e.getMessage()));
        }
    }

    @GetMapping("/{cacheName}/details")
    public ResponseEntity<Map<String, Object>> getCacheDetails(@PathVariable String cacheName) {
        log.info(" translated_text_2 translated_text_2 information inquiry - translated_text_2: {}", cacheName);
        
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("name", cacheName);
        details.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());

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

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> evictUserCache(@PathVariable Long userId) {
        log.info(" user translated_text_2 translated_text_3 translated_text_2 - ID: {}", userId);
        
        try {
            cacheEvictionStrategy.evictUserCaches(userId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "user translated_text_2 translated_text_10 translated_text_3.",
                    "userId", userId.toString()
            ));
        } catch (Exception e) {
            log.error(" user translated_text_2 translated_text_3 failure - ID: {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "user translated_text_2 translated_text_3 failure: " + e.getMessage(),
                            "userId", userId.toString()
                    ));
        }
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        log.info(" translated_text_2 translated_text_3 translated_text_2 - translated_text_2: {}", cacheName);
        
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return ResponseEntity.notFound().build();
            }
            
            cache.clear();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "translated_text_2 translated_text_10 translated_text_3.",
                    "cacheName", cacheName
            ));
        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_3 failure - translated_text_2: {}", cacheName, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "translated_text_2 translated_text_3 failure: " + e.getMessage(),
                            "cacheName", cacheName
                    ));
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        log.warn(" translated_text_2 translated_text_2 translated_text_3 translated_text_2");
        
        try {
            cacheEvictionStrategy.clearAllCaches();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "all translated_text_2 translated_text_10 translated_text_3.",
                    "clearedCaches", cacheManager.getCacheNames()
            ));
        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_2 translated_text_3 failure", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "translated_text_2 translated_text_2 translated_text_3 failure: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/refresh/provider/{provider}")
    public ResponseEntity<Map<String, String>> refreshProviderCache(@PathVariable String provider) {
        log.info(" translated_text_5 translated_text_2 translated_text_2 translated_text_2 - translated_text_5: {}", provider);
        
        try {
            cacheEvictionStrategy.refreshProviderCaches(provider);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "translated_text_5 translated_text_2 translated_text_10 translated_text_2.",
                    "provider", provider
            ));
        } catch (Exception e) {
            log.error(" translated_text_5 translated_text_2 translated_text_2 failure - translated_text_5: {}", provider, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "translated_text_5 translated_text_2 translated_text_2 failure: " + e.getMessage(),
                            "provider", provider
                    ));
        }
    }

    @GetMapping("/statistics/export")
    public ResponseEntity<String> exportCacheStatistics() {
        log.info(" translated_text_2 translated_text_2 CSV translated_text_4 translated_text_2");
        
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("translated_text_2,translated_text_3,translated_text_3,translated_text_2\n");

            OAuth2TokenCacheService.CacheStats tokenStats = tokenCacheService.getCacheStats();
            csv.append(String.format("oauth2Tokens,%d,%.2f%%,translated_text_2\n", 
                    tokenStats.getTotalEntries(), tokenStats.getHitRatio() * 100));

            UserProfileCacheService.ProfileCacheStats profileStats = profileCacheService.getCacheStats();
            csv.append(String.format("userProfiles,%d,N/A,translated_text_2\n", profileStats.getTotalProfiles()));
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=cache-statistics.csv")
                    .body(csv.toString());
                    
        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_2 translated_text_4 failure", e);
            return ResponseEntity.internalServerError()
                    .body("translated_text_2 translated_text_2 translated_text_4 failure: " + e.getMessage());
        }
    }

    @PostMapping("/cleanup/manual")
    public ResponseEntity<Map<String, String>> triggerManualCleanup() {
        log.info("🧹 translated_text_2 translated_text_2 translated_text_2 translated_text_3");
        
        try {

            new Thread(() -> {
                try {
                    cacheEvictionStrategy.cleanupExpiredTokens();
                    log.info(" translated_text_2 translated_text_2 translated_text_2 completed");
                } catch (Exception e) {
                    log.error(" translated_text_2 translated_text_2 translated_text_2 failure", e);
                }
            }).start();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "translated_text_2 translated_text_2 translated_text_7 translated_text_7."
            ));
        } catch (Exception e) {
            log.error(" translated_text_2 translated_text_2 translated_text_2 translated_text_3 failure", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "translated_text_2 translated_text_2 translated_text_3 failure: " + e.getMessage()
                    ));
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}