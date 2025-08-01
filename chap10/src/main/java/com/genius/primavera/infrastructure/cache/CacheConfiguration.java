package com.genius.primavera.infrastructure.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 캐싱 전략 설정 클래스
 * 
 * 다중 캐시 백엔드 지원:
 * - Redis: 분산 환경에서 세션 및 토큰 저장
 * - Caffeine: 로컬 캐시로 자주 접근하는 데이터 고속 처리
 * 
 * 캐시 계층 구조:
 * 1. L1 Cache (Caffeine) - 애플리케이션 메모리 내 고속 캐시
 * 2. L2 Cache (Redis) - 분산 환경 공유 캐시
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Redis 기반 캐시 매니저 - 운영 환경 기본값
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.info("🚀 Redis 캐시 매니저 초기화 중...");
        
        // JSON 직렬화 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 기본 TTL 30분
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // 캐시별 맞춤 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // OAuth2 토큰 캐시 - 1시간 TTL
        cacheConfigurations.put("oauth2Tokens", defaultConfig
                .entryTtl(Duration.ofHours(1)));
        
        // 사용자 프로필 캐시 - 2시간 TTL
        cacheConfigurations.put("userProfiles", defaultConfig
                .entryTtl(Duration.ofHours(2)));
        
        // 소셜 프로바이더 정보 캐시 - 6시간 TTL (자주 변경되지 않음)
        cacheConfigurations.put("socialProviders", defaultConfig
                .entryTtl(Duration.ofHours(6)));
        
        // 사용자 세션 캐시 - 30분 TTL
        cacheConfigurations.put("userSessions", defaultConfig
                .entryTtl(Duration.ofMinutes(30)));

        log.info("✅ Redis 캐시 설정 완료 - 총 {}개 캐시 구성", cacheConfigurations.size());
        
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Caffeine 기반 로컬 캐시 매니저 - 개발/테스트 환경
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine")
    public CacheManager caffeineCacheManager() {
        log.info("🚀 Caffeine 로컬 캐시 매니저 초기화 중...");
        
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Caffeine 캐시 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)  // 최대 1000개 엔트리
                .expireAfterWrite(30, TimeUnit.MINUTES)  // 30분 후 만료
                .expireAfterAccess(15, TimeUnit.MINUTES)  // 15분 미접근 시 만료
                .recordStats());  // 통계 수집 활성화
        
        // 캐시 이름 사전 등록
        cacheManager.setCacheNames(java.util.List.of(
                "oauth2Tokens", "userProfiles", "socialProviders", "userSessions"
        ));
        
        log.info("✅ Caffeine 캐시 설정 완료");
        return cacheManager;
    }

    /**
     * Redis Template 설정 - 수동 캐시 조작용
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        
        // Key-Value 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}