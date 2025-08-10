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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileCacheService {

    private final Map<String, UserProfileCacheEntry> profileStore = new ConcurrentHashMap<>();

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

        profileStore.put(String.valueOf(user.getId()), entry);

        log.info(" user translated_text_3 translated_text_2 - ID: {}, translated_text_3: {}, translated_text_5: {}", 
                user.getId(), user.getEmail(), providerType);

        return entry;
    }

    @Cacheable(value = "userProfiles", key = "#userId")
    public Optional<UserProfileCacheEntry> getUserProfile(Long userId) {
        UserProfileCacheEntry entry = profileStore.get(String.valueOf(userId));
        
        if (entry == null) {
            log.debug(" user translated_text_3 translated_text_2 translated_text_2 - ID: {}", userId);
            return Optional.empty();
        }

        log.debug(" user translated_text_3 translated_text_2 translated_text_2 - ID: {}", userId);
        return Optional.of(entry);
    }

    public Optional<UserProfileCacheEntry> getUserProfileByEmail(String email) {
        return profileStore.values().stream()
                .filter(entry -> email.equals(entry.getEmail()))
                .findFirst();
    }

    @CachePut(value = "userProfiles", key = "#userId")
    public UserProfileCacheEntry updateUserProfile(Long userId, User updatedUser) {
        Optional<UserProfileCacheEntry> existingEntry = getUserProfile(userId);
        
        UserProfileCacheEntry.UserProfileCacheEntryBuilder builder = UserProfileCacheEntry.builder()
                .userId(updatedUser.getId())
                .email(updatedUser.getEmail())
                .nickname(updatedUser.getNickname())
                .status(updatedUser.getStatus().name())
                .cachedAt(LocalDateTime.now());

        if (existingEntry.isPresent()) {
            UserProfileCacheEntry existing = existingEntry.get();
            builder.primaryProvider(existing.getPrimaryProvider())
                   .lastLoginAt(existing.getLastLoginAt())
                   .loginCount(existing.getLoginCount());
        }

        UserProfileCacheEntry updatedEntry = builder.build();
        profileStore.put(String.valueOf(userId), updatedEntry);

        log.info(" user translated_text_3 translated_text_4 - ID: {}, translated_text_3: {}", userId, updatedUser.getEmail());
        return updatedEntry;
    }

    @CachePut(value = "userProfiles", key = "#userId")
    public UserProfileCacheEntry updateLastLogin(Long userId, String providerType) {
        Optional<UserProfileCacheEntry> existingEntry = getUserProfile(userId);
        
        if (existingEntry.isEmpty()) {
            log.warn(" translated_text_3 translated_text_4 failure - translated_text_3 translated_text_2: {}", userId);
            return null;
        }

        UserProfileCacheEntry existing = existingEntry.get();
        UserProfileCacheEntry updatedEntry = existing.toBuilder()
                .lastLoginAt(LocalDateTime.now())
                .loginCount(existing.getLoginCount() + 1)
                .primaryProvider(providerType)
                .build();

        profileStore.put(String.valueOf(userId), updatedEntry);

        log.info(" translated_text_3 translated_text_2 translated_text_4 - ID: {}, translated_text_5: {}, translated_text_3 translated_text_2: {}", 
                userId, providerType, updatedEntry.getLoginCount());

        return updatedEntry;
    }

    @CacheEvict(value = "userProfiles", key = "#userId")
    public void evictUserProfile(Long userId) {
        profileStore.remove(String.valueOf(userId));
        log.info(" user translated_text_3 translated_text_2 deletion - ID: {}", userId);
    }

    @CacheEvict(value = "userProfiles", allEntries = true)
    public void evictAllUserProfiles() {
        profileStore.clear();
        log.info(" all user translated_text_3 translated_text_2 deletion");
    }

    public void refreshProviderUsers(String providerType) {
        long refreshCount = profileStore.values().stream()
                .filter(entry -> providerType.equals(entry.getPrimaryProvider()))
                .peek(entry -> {

                    entry.setCachedAt(LocalDateTime.now());
                })
                .count();

        log.info(" translated_text_5 user translated_text_2 translated_text_2 - translated_text_5: {}, translated_text_2 translated_text_1: {}", providerType, refreshCount);
    }

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

    @lombok.Data
    @lombok.Builder
    public static class ProfileCacheStats {
        private long totalProfiles;
        private Map<String, Long> providerDistribution;
        private double averageLoginCount;
        private LocalDateTime oldestCacheEntry;
    }
}