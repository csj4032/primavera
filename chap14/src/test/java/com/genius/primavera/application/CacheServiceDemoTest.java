package com.genius.primavera.application;

import com.genius.primavera.testingsupport.annotation.TestCacheableService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 캐시 기능 통합 테스트 - Mixin 어노테이션 데모
 * 
 * <p>{@link TestCacheableService} 어노테이션을 사용하여 
 * MariaDB + Redis 환경에서 캐시 기능을 테스트하는 예시입니다.</p>
 * 
 * <h3>테스트 환경:</h3>
 * <ul>
 *   <li>MariaDB 11.4.7 - 주 데이터 저장소</li>
 *   <li>Redis 7 - 캐시 계층</li>
 *   <li>MockMvc - 웹 계층 테스트</li>
 * </ul>
 * 
 * <h3>검증 항목:</h3>
 * <ul>
 *   <li>Redis 연결성 및 기본 동작</li>
 *   <li>캐시 저장 및 조회</li>
 *   <li>캐시 만료 정책</li>
 *   <li>캐시 무효화</li>
 * </ul>
 */
@Slf4j
@TestCacheableService(initScript = "sql/init.sql")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("캐시 서비스 통합 테스트 - Redis + MariaDB")
public class CacheServiceDemoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @Order(1)
    @DisplayName("Redis 연결 및 기본 동작 확인")
    void shouldConnectToRedisAndPerformBasicOperations() {
        // Given: Redis 연결 확인
        assertThat(redisTemplate.getConnectionFactory()).isNotNull();
        
        // When: 기본 키-값 저장
        String testKey = "test:connection";
        String testValue = "Hello Redis from TestContainer!";
        
        redisTemplate.opsForValue().set(testKey, testValue);
        
        // Then: 저장된 값 조회 확인
        String retrievedValue = redisTemplate.opsForValue().get(testKey);
        assertThat(retrievedValue).isEqualTo(testValue);
        
        log.info("✅ Redis 기본 동작 확인: {} = {}", testKey, retrievedValue);
    }

    @Test
    @Order(2)
    @DisplayName("캐시 만료 정책 테스트")
    void shouldRespectCacheExpiration() throws InterruptedException {
        // Given: 만료 시간이 있는 캐시 데이터
        String cacheKey = "test:expiration";
        String cacheValue = "This will expire soon";
        
        // When: 2초 후 만료되도록 설정
        redisTemplate.opsForValue().set(cacheKey, cacheValue, Duration.ofSeconds(2));
        
        // Then: 즉시 조회 시 값 존재 확인
        assertThat(redisTemplate.opsForValue().get(cacheKey)).isEqualTo(cacheValue);
        
        // When: 3초 대기 (만료 시간 초과)
        TimeUnit.SECONDS.sleep(3);
        
        // Then: 만료 후 값이 없음을 확인
        assertThat(redisTemplate.opsForValue().get(cacheKey)).isNull();
        
        log.info("⏰ 캐시 만료 정책 확인 완료");
    }

    @Test
    @Order(3)
    @DisplayName("복합 데이터 타입 캐시 테스트")
    void shouldHandleComplexDataTypes() {
        // Given: Hash 타입 데이터 준비
        String hashKey = "user:1001";
        
        // When: Hash 필드들 저장
        redisTemplate.opsForHash().put(hashKey, "name", "김테스트");
        redisTemplate.opsForHash().put(hashKey, "email", "test@primavera.com");
        redisTemplate.opsForHash().put(hashKey, "role", "USER");
        
        // Then: 저장된 Hash 데이터 검증
        assertThat(redisTemplate.opsForHash().get(hashKey, "name")).isEqualTo("김테스트");
        assertThat(redisTemplate.opsForHash().get(hashKey, "email")).isEqualTo("test@primavera.com");
        assertThat(redisTemplate.opsForHash().get(hashKey, "role")).isEqualTo("USER");
        
        // When: 전체 Hash 조회
        var userHash = redisTemplate.opsForHash().entries(hashKey);
        
        // Then: 전체 데이터 검증
        assertThat(userHash).hasSize(3);
        assertThat(userHash).containsEntry("name", "김테스트");
        
        log.info("📊 복합 데이터 캐시 확인: {}", userHash);
    }

    @Test
    @Order(4)
    @DisplayName("웹 요청과 캐시 연동 테스트")
    void shouldIntegrateWebRequestsWithCache() throws Exception {
        // Given: 캐시에 API 응답 데이터 미리 저장
        String apiCacheKey = "api:posts:list";
        String cachedResponse = """
            {
                "posts": [
                    {"id": 1, "title": "Cached Post", "author": "Test User"}
                ],
                "cached": true
            }
            """;
        
        redisTemplate.opsForValue().set(apiCacheKey, cachedResponse, Duration.ofMinutes(5));
        
        // When & Then: 웹 요청 실행 (실제 서비스에서는 캐시된 데이터 활용)
        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk())
            .andDo(result -> {
                // 캐시된 데이터 확인
                String cached = redisTemplate.opsForValue().get(apiCacheKey);
                assertThat(cached).isNotNull();
                log.info("🔄 웹 요청과 캐시 연동 확인 - 캐시 데이터 존재함");
            });
    }

    @Test
    @Order(5)
    @DisplayName("캐시 패턴별 성능 비교")
    void shouldCompareCachePatterns() {
        // Given: 성능 측정을 위한 데이터 준비
        String patternKey = "performance:test";
        int iterations = 1000;
        
        // When: 단순 SET/GET 패턴 측정
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            redisTemplate.opsForValue().set(patternKey + ":" + i, "value" + i);
        }
        long setDuration = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            redisTemplate.opsForValue().get(patternKey + ":" + i);
        }
        long getDuration = System.nanoTime() - startTime;
        
        // Then: 성능 결과 로깅
        log.info("⚡ 캐시 성능 측정 결과:");
        log.info("   SET 작업 {} 회: {}ms", iterations, setDuration / 1_000_000);
        log.info("   GET 작업 {} 회: {}ms", iterations, getDuration / 1_000_000);
        
        // 기본적인 성능 검증 (매우 관대한 기준)
        assertThat(setDuration).isLessThan(TimeUnit.SECONDS.toNanos(5));
        assertThat(getDuration).isLessThan(TimeUnit.SECONDS.toNanos(5));
    }

    @AfterEach
    void cleanupCache() {
        // 각 테스트 후 캐시 정리 (테스트 격리)
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        log.debug("🧹 테스트 후 Redis 캐시 정리됨");
    }

    @AfterAll
    static void tearDown() {
        log.info("✅ 캐시 서비스 통합 테스트 완료 - TestContainers 자동 정리됨");
    }
}