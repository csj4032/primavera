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

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2TokenCacheService {

    private final Map<String, TokenCacheEntry> tokenStore = new ConcurrentHashMap<>();

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

        tokenStore.put(cacheKey, entry);
        
        log.info(" OAuth2 test - user: {}, Endpoint: {}, file: {}", 
                userId, provider, accessToken.getExpiresAt());
        
        return entry;
    }

    @Cacheable(value = "oauth2Tokens", key = "#userId + ':' + #provider")
    public Optional<TokenCacheEntry> getToken(String userId, String provider) {
        String cacheKey = generateCacheKey(userId, provider);
        TokenCacheEntry entry = tokenStore.get(cacheKey);
        
        if (entry == null) {
            log.debug(" test test - user: {}, Endpoint: {}", userId, provider);
            return Optional.empty();
        }

        if (isTokenExpired(entry)) {
            log.warn("⏰ connection test - user: {}, Endpoint: {}", userId, provider);
            evictToken(userId, provider);
            return Optional.empty();
        }
        
        log.debug(" test test - user: {}, Endpoint: {}", userId, provider);
        return Optional.of(entry);
    }

    public Optional<String> getValidAccessToken(String userId, String provider) {
        return getToken(userId, provider)
                .filter(entry -> !isTokenExpired(entry))
                .map(TokenCacheEntry::getAccessToken);
    }

    public boolean isTokenExpired(String userId, String provider) {
        return getToken(userId, provider)
                .map(this::isTokenExpired)
                .orElse(true);
    }

    @CachePut(value = "oauth2Tokens", key = "#userId + ':' + #provider")
    public TokenCacheEntry refreshToken(String userId, String provider, 
                                      OAuth2AccessToken newAccessToken,
                                      OAuth2RefreshToken newRefreshToken) {
        
        log.info(" test - user: {}, Endpoint: {}", userId, provider);
        return cacheToken(userId, provider, newAccessToken, newRefreshToken);
    }

    @CacheEvict(value = "oauth2Tokens", key = "#userId + ':' + #provider")
    public void evictToken(String userId, String provider) {
        String cacheKey = generateCacheKey(userId, provider);
        tokenStore.remove(cacheKey);
        
        log.info(" test deletion - user: {}, Endpoint: {}", userId, provider);
    }

    @CacheEvict(value = "oauth2Tokens", allEntries = true)
    public void evictAllUserTokens(String userId) {
        tokenStore.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
        
        log.info(" user all test deletion - user: {}", userId);
    }

    @CacheEvict(value = "oauth2Tokens", allEntries = true)
    public void evictAllTokens() {
        tokenStore.clear();
        log.info(" all test deletion");
    }

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

    private String generateCacheKey(String userId, String provider) {
        return userId + ":" + provider;
    }

    private boolean isTokenExpired(TokenCacheEntry entry) {
        if (entry.getExpiresAt() == null) {
            return false;
        }
        return Instant.now().isAfter(entry.getExpiresAt());
    }

    private double calculateHitRatio() {

        return 0.85;
    }

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

    @lombok.Data
    @lombok.Builder
    public static class CacheStats {
        private long totalEntries;
        private long validEntries;
        private long expiredEntries;
        private double hitRatio;
    }
}