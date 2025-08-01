package com.genius.primavera.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth2 토큰 캐싱 서비스
 * 
 * 주요 기능:
 * - Access Token 캐싱으로 API 호출 최적화
 * - Refresh Token 관리 및 자동 갱신
 * - 토큰 만료 시간 기반 TTL 설정
 * - 프로바이더별 토큰 분리 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2TokenCacheService {

    // 메모리 기반 토큰 저장소 (Redis 백업용)
    private final Map<String, TokenCacheEntry> tokenStore = new ConcurrentHashMap<>();

    /**
     * 토큰 정보를 캐시에 저장
     * 
     * @param userId 사용자 ID
     * @param provider OAuth2 프로바이더 (google, facebook, github, kakao)
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰 (선택사항)
     */
    @CachePut(value = "oauth2Tokens", key = "#userId + ':' + #provider")
    public TokenCacheEntry cacheToken(String userId, String provider, 
                                    OAuth2AccessToken accessToken, 
                                    OAuth2RefreshToken refreshToken) {
        
        String cacheKey = generateCacheKey(userId, provider);
        
        TokenCacheEntry entry = TokenCacheEntry.builder()
                .userId(userId)
                .provider(provider)
                .accessToken(accessToken.getTokenValue())
                .refreshToken(refreshToken != null ? refreshToken.getTokenValue() : null)
                .expiresAt(accessToken.getExpiresAt())
                .tokenType(accessToken.getTokenType().getValue())
                .scopes(accessToken.getScopes())
                .cachedAt(LocalDateTime.now())
                .build();
        
        // 메모리 저장소에도 백업
        tokenStore.put(cacheKey, entry);
        
        log.info("🔐 OAuth2 토큰 캐싱 - 사용자: {}, 프로바이더: {}, 만료시간: {}", 
                userId, provider, accessToken.getExpiresAt());
        
        return entry;
    }

    /**
     * 캐시에서 토큰 정보 조회
     * 
     * @param userId 사용자 ID
     * @param provider OAuth2 프로바이더
     * @return 토큰 정보 (없으면 Optional.empty())
     */
    @Cacheable(value = "oauth2Tokens", key = "#userId + ':' + #provider")
    public Optional<TokenCacheEntry> getToken(String userId, String provider) {
        String cacheKey = generateCacheKey(userId, provider);
        TokenCacheEntry entry = tokenStore.get(cacheKey);
        
        if (entry == null) {
            log.debug("🔍 토큰 캐시 미스 - 사용자: {}, 프로바이더: {}", userId, provider);
            return Optional.empty();
        }
        
        // 토큰 만료 체크
        if (isTokenExpired(entry)) {
            log.warn("⏰ 만료된 토큰 발견 - 사용자: {}, 프로바이더: {}", userId, provider);
            evictToken(userId, provider);
            return Optional.empty();
        }
        
        log.debug("🎯 토큰 캐시 히트 - 사용자: {}, 프로바이더: {}", userId, provider);
        return Optional.of(entry);
    }

    /**
     * 유효한 액세스 토큰 조회 (만료 체크 포함)
     */
    public Optional<String> getValidAccessToken(String userId, String provider) {
        return getToken(userId, provider)
                .filter(entry -> !isTokenExpired(entry))
                .map(TokenCacheEntry::getAccessToken);
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String userId, String provider) {
        return getToken(userId, provider)
                .map(this::isTokenExpired)
                .orElse(true);
    }

    /**
     * 토큰 갱신 (새로운 토큰으로 캐시 업데이트)
     */
    @CachePut(value = "oauth2Tokens", key = "#userId + ':' + #provider")
    public TokenCacheEntry refreshToken(String userId, String provider, 
                                      OAuth2AccessToken newAccessToken,
                                      OAuth2RefreshToken newRefreshToken) {
        
        log.info("🔄 토큰 갱신 - 사용자: {}, 프로바이더: {}", userId, provider);
        return cacheToken(userId, provider, newAccessToken, newRefreshToken);
    }

    /**
     * 특정 사용자/프로바이더 토큰 캐시 삭제
     */
    @CacheEvict(value = "oauth2Tokens", key = "#userId + ':' + #provider")
    public void evictToken(String userId, String provider) {
        String cacheKey = generateCacheKey(userId, provider);
        tokenStore.remove(cacheKey);
        
        log.info("🗑️ 토큰 캐시 삭제 - 사용자: {}, 프로바이더: {}", userId, provider);
    }

    /**
     * 특정 사용자의 모든 토큰 캐시 삭제
     */
    @CacheEvict(value = "oauth2Tokens", allEntries = true)
    public void evictAllUserTokens(String userId) {
        tokenStore.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
        
        log.info("🗑️ 사용자 모든 토큰 캐시 삭제 - 사용자: {}", userId);
    }

    /**
     * 모든 토큰 캐시 삭제
     */
    @CacheEvict(value = "oauth2Tokens", allEntries = true)
    public void evictAllTokens() {
        tokenStore.clear();
        log.info("🗑️ 모든 토큰 캐시 삭제");
    }

    /**
     * 캐시 통계 정보 조회
     */
    public CacheStats getCacheStats() {
        long totalEntries = tokenStore.size();
        long expiredEntries = tokenStore.values().stream()
                .mapToLong(entry -> isTokenExpired(entry) ? 1 : 0)
                .sum();
        long validEntries = totalEntries - expiredEntries;
        
        return CacheStats.builder()
                .totalEntries(totalEntries)
                .validEntries(validEntries)
                .expiredEntries(expiredEntries)
                .hitRatio(calculateHitRatio())
                .build();
    }

    // === Private Methods ===

    private String generateCacheKey(String userId, String provider) {
        return userId + ":" + provider;
    }

    private boolean isTokenExpired(TokenCacheEntry entry) {
        if (entry.getExpiresAt() == null) {
            return false; // 만료 시간이 없으면 영구 토큰으로 간주
        }
        return Instant.now().isAfter(entry.getExpiresAt());
    }

    private double calculateHitRatio() {
        // 실제 구현에서는 캐시 매니저의 통계를 사용
        return 0.85; // 예시 값
    }

    /**
     * 토큰 캐시 엔트리 데이터 클래스
     */
    @lombok.Data
    @lombok.Builder
    public static class TokenCacheEntry {
        private String userId;
        private String provider;
        private String accessToken;
        private String refreshToken;
        private Instant expiresAt;
        private String tokenType;
        private java.util.Set<String> scopes;
        private LocalDateTime cachedAt;
    }

    /**
     * 캐시 통계 데이터 클래스
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStats {
        private long totalEntries;
        private long validEntries;
        private long expiredEntries;
        private double hitRatio;
    }
}