package com.genius.primavera.infrastructure.cache;

import com.genius.primavera.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자 프로필 캐싱 서비스
 * 
 * 주요 기능:
 * - 소셜 로그인 사용자 프로필 정보 캐싱
 * - 프로바이더별 사용자 정보 동기화
 * - 프로필 변경 시 자동 캐시 갱신
 * - 다중 프로바이더 연동 사용자 통합 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileCacheService {

    // 로컬 캐시 백업 저장소
    private final Map<String, UserProfileCacheEntry> profileStore = new ConcurrentHashMap<>();

    /**
     * 사용자 프로필을 캐시에 저장
     * 
     * @param user 사용자 정보
     * @param providerType 소셜 프로바이더 타입
     * @return 캐시된 프로필 엔트리
     */
    @CachePut(value = "userProfiles", key = "#user.id")
    public UserProfileCacheEntry cacheUserProfile(User user, String providerType) {
        UserProfileCacheEntry entry = UserProfileCacheEntry.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .status(user.getStatus().name())
                .primaryProvider(providerType)
                .lastLoginAt(LocalDateTime.now())
                .cachedAt(LocalDateTime.now())
                .build();

        // 메모리 저장소에도 백업
        profileStore.put(String.valueOf(user.getId()), entry);

        log.info("👤 사용자 프로필 캐싱 - ID: {}, 이메일: {}, 프로바이더: {}", 
                user.getId(), user.getEmail(), providerType);

        return entry;
    }

    /**
     * 사용자 ID로 프로필 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 프로필 (없으면 Optional.empty())
     */
    @Cacheable(value = "userProfiles", key = "#userId")
    public Optional<UserProfileCacheEntry> getUserProfile(Long userId) {
        UserProfileCacheEntry entry = profileStore.get(String.valueOf(userId));
        
        if (entry == null) {
            log.debug("🔍 사용자 프로필 캐시 미스 - ID: {}", userId);
            return Optional.empty();
        }

        log.debug("🎯 사용자 프로필 캐시 히트 - ID: {}", userId);
        return Optional.of(entry);
    }

    /**
     * 이메일로 사용자 프로필 조회 (역방향 조회)
     * 
     * @param email 사용자 이메일
     * @return 사용자 프로필
     */
    public Optional<UserProfileCacheEntry> getUserProfileByEmail(String email) {
        return profileStore.values().stream()
                .filter(entry -> email.equals(entry.getEmail()))
                .findFirst();
    }

    /**
     * 사용자 프로필 업데이트
     * 
     * @param userId 사용자 ID
     * @param updatedUser 업데이트된 사용자 정보
     * @return 갱신된 프로필 엔트리
     */
    @CachePut(value = "userProfiles", key = "#userId")
    public UserProfileCacheEntry updateUserProfile(Long userId, User updatedUser) {
        Optional<UserProfileCacheEntry> existingEntry = getUserProfile(userId);
        
        UserProfileCacheEntry.UserProfileCacheEntryBuilder builder = UserProfileCacheEntry.builder()
                .userId(updatedUser.getId())
                .email(updatedUser.getEmail())
                .nickname(updatedUser.getNickname())
                .status(updatedUser.getStatus().name())
                .cachedAt(LocalDateTime.now());

        // 기존 정보 유지
        if (existingEntry.isPresent()) {
            UserProfileCacheEntry existing = existingEntry.get();
            builder.primaryProvider(existing.getPrimaryProvider())
                   .lastLoginAt(existing.getLastLoginAt())
                   .loginCount(existing.getLoginCount());
        }

        UserProfileCacheEntry updatedEntry = builder.build();
        profileStore.put(String.valueOf(userId), updatedEntry);

        log.info("🔄 사용자 프로필 업데이트 - ID: {}, 이메일: {}", userId, updatedUser.getEmail());
        return updatedEntry;
    }

    /**
     * 로그인 시간 업데이트
     * 
     * @param userId 사용자 ID
     * @param providerType 로그인한 프로바이더
     */
    @CachePut(value = "userProfiles", key = "#userId")
    public UserProfileCacheEntry updateLastLogin(Long userId, String providerType) {
        Optional<UserProfileCacheEntry> existingEntry = getUserProfile(userId);
        
        if (existingEntry.isEmpty()) {
            log.warn("⚠️ 로그인 업데이트 실패 - 프로필 없음: {}", userId);
            return null;
        }

        UserProfileCacheEntry existing = existingEntry.get();
        UserProfileCacheEntry updatedEntry = existing.toBuilder()
                .lastLoginAt(LocalDateTime.now())
                .loginCount(existing.getLoginCount() + 1)
                .primaryProvider(providerType)  // 최신 로그인 프로바이더로 업데이트
                .build();

        profileStore.put(String.valueOf(userId), updatedEntry);

        log.info("🚀 로그인 시간 업데이트 - ID: {}, 프로바이더: {}, 로그인 횟수: {}", 
                userId, providerType, updatedEntry.getLoginCount());

        return updatedEntry;
    }

    /**
     * 특정 사용자 프로필 캐시 삭제
     * 
     * @param userId 사용자 ID
     */
    @CacheEvict(value = "userProfiles", key = "#userId")
    public void evictUserProfile(Long userId) {
        profileStore.remove(String.valueOf(userId));
        log.info("🗑️ 사용자 프로필 캐시 삭제 - ID: {}", userId);
    }

    /**
     * 모든 사용자 프로필 캐시 삭제
     */
    @CacheEvict(value = "userProfiles", allEntries = true)
    public void evictAllUserProfiles() {
        profileStore.clear();
        log.info("🗑️ 모든 사용자 프로필 캐시 삭제");
    }

    /**
     * 특정 프로바이더 사용자들의 캐시 갱신
     * 
     * @param providerType 프로바이더 타입
     */
    public void refreshProviderUsers(String providerType) {
        long refreshCount = profileStore.values().stream()
                .filter(entry -> providerType.equals(entry.getPrimaryProvider()))
                .peek(entry -> {
                    // 캐시 갱신 로직 (실제로는 DB에서 최신 정보 조회)
                    entry.setCachedAt(LocalDateTime.now());
                })
                .count();

        log.info("🔄 프로바이더별 사용자 캐시 갱신 - 프로바이더: {}, 갱신 수: {}", providerType, refreshCount);
    }

    /**
     * 캐시 통계 정보
     */
    public ProfileCacheStats getCacheStats() {
        long totalProfiles = profileStore.size();
        
        Map<String, Long> providerStats = new ConcurrentHashMap<>();
        profileStore.values().forEach(entry -> {
            String provider = entry.getPrimaryProvider();
            providerStats.merge(provider, 1L, Long::sum);
        });

        return ProfileCacheStats.builder()
                .totalProfiles(totalProfiles)
                .providerDistribution(providerStats)
                .averageLoginCount(calculateAverageLoginCount())
                .oldestCacheEntry(findOldestCacheEntry())
                .build();
    }

    // === Private Methods ===

    private double calculateAverageLoginCount() {
        return profileStore.values().stream()
                .mapToInt(UserProfileCacheEntry::getLoginCount)
                .average()
                .orElse(0.0);
    }

    private LocalDateTime findOldestCacheEntry() {
        return profileStore.values().stream()
                .map(UserProfileCacheEntry::getCachedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    /**
     * 사용자 프로필 캐시 엔트리
     */
    @lombok.Data
    @lombok.Builder(toBuilder = true)
    public static class UserProfileCacheEntry {
        private Long userId;
        private String email;
        private String nickname;
        private String status;
        private String primaryProvider;
        private LocalDateTime lastLoginAt;
        private LocalDateTime cachedAt;
        @lombok.Builder.Default
        private int loginCount = 1;
    }

    /**
     * 프로필 캐시 통계
     */
    @lombok.Data
    @lombok.Builder
    public static class ProfileCacheStats {
        private long totalProfiles;
        private Map<String, Long> providerDistribution;
        private double averageLoginCount;
        private LocalDateTime oldestCacheEntry;
    }
}