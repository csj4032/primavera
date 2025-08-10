package com.genius.primavera.application;

import com.genius.primavera.testingsupport.WebCacheIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("test service test - Redis + MariaDB + MockMvc")
public class CacheServiceDemoTest implements WebCacheIntegrationTest {

    static {
        WebCacheIntegrationTest.startWebCacheContainers();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @Order(1)
    @DisplayName("Redis test should test verification")
    void shouldConnectToRedisAndPerformBasicOperations() {

        assertThat(redisTemplate.getConnectionFactory()).isNotNull();

        String testKey = "test:connection";
        String testValue = "Hello Redis from TestContainer!";
        
        redisTemplate.opsForValue().set(testKey, testValue);

        String retrievedValue = redisTemplate.opsForValue().get(testKey);
        assertThat(retrievedValue).isEqualTo(testValue);
        
        log.info(" Redis test verification: {} = {}", testKey, retrievedValue);
    }

    @Test
    @Order(2)
    @DisplayName("test test")
    void shouldRespectCacheExpiration() throws InterruptedException {

        String cacheKey = "test:expiration";
        String cacheValue = "This will expire soon";

        redisTemplate.opsForValue().set(cacheKey, cacheValue, Duration.ofSeconds(2));

        assertThat(redisTemplate.opsForValue().get(cacheKey)).isEqualTo(cacheValue);

        TimeUnit.SECONDS.sleep(3);

        assertThat(redisTemplate.opsForValue().get(cacheKey)).isNull();
        
        log.info("⏰ test test verification completed");
    }

    @Test
    @Order(3)
    @DisplayName("test data test test")
    void shouldHandleComplexDataTypes() {

        String hashKey = "user:1001";

        redisTemplate.opsForHash().put(hashKey, "name", "test");
        redisTemplate.opsForHash().put(hashKey, "email", "test@primavera.com");
        redisTemplate.opsForHash().put(hashKey, "role", "USER");

        assertThat(redisTemplate.opsForHash().get(hashKey, "name")).isEqualTo("test");
        assertThat(redisTemplate.opsForHash().get(hashKey, "email")).isEqualTo("test@primavera.com");
        assertThat(redisTemplate.opsForHash().get(hashKey, "role")).isEqualTo("USER");

        var userHash = redisTemplate.opsForHash().entries(hashKey);

        assertThat(userHash).hasSize(3);
        assertThat(userHash).containsEntry("name", "test");
        
        log.info(" test data test verification: {}", userHash);
    }

    @Test
    @Order(4)
    @DisplayName("should connection test test")
    void shouldIntegrateWebRequestsWithCache() throws Exception {

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

        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk())
            .andDo(result -> {

                String cached = redisTemplate.opsForValue().get(apiCacheKey);
                assertThat(cached).isNotNull();
                log.info(" should connection test verification - test data connection");
            });
    }

    @Test
    @Order(5)
    @DisplayName("test connection test")
    void shouldCompareCachePatterns() {

        String patternKey = "performance:test";
        int iterations = 1000;

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

        log.info(" test test result:");
        log.info("   SET test {} should: {}ms", iterations, setDuration / 1_000_000);
        log.info("   GET test {} should: {}ms", iterations, getDuration / 1_000_000);

        assertThat(setDuration).isLessThan(TimeUnit.SECONDS.toNanos(5));
        assertThat(getDuration).isLessThan(TimeUnit.SECONDS.toNanos(5));
    }

    @AfterEach
    void cleanupCache() {

        redisTemplate.getConnectionFactory().getConnection().flushAll();
        log.debug("🧹 test should Redis test connection");
    }

    @AfterAll
    static void tearDown() {
        log.info(" test service test completed - TestContainers test connection");
    }
}