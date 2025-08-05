package com.genius.primavera.testcontainer.containertype;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 다중 컨테이너 테스트 (MariaDB + Redis)
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("다중 컨테이너 (MariaDB + Redis) 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableTestContainers(containers = {ContainerType.MARIADB, ContainerType.REDIS})
public class MultiContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MariaDBContainer<?> mariaDBContainer;

    @Autowired
    @Qualifier("redisContainer")
    private GenericContainer<?> redisContainer;

    @Test
    @Order(1)
    @DisplayName("MariaDB와 Redis 컨테이너 동시 실행 테스트")
    void testMultipleContainers() {
        log.info("다중 컨테이너 테스트 시작");

        if (mariaDBContainer != null) {
            assertThat(mariaDBContainer.isRunning()).isTrue();
            log.info("MariaDB 컨테이너 실행 중: {}", mariaDBContainer.getJdbcUrl());
        }

        if (redisContainer != null) {
            assertThat(redisContainer.isRunning()).isTrue();
            log.info("Redis 컨테이너 실행 중: {}:{}", redisContainer.getHost(), redisContainer.getMappedPort(6379));
            log.info("Redis 컨테이너 노출 포트 목록: {}", redisContainer.getExposedPorts());
            log.info("Redis 컨테이너 도커 이미지: {}", redisContainer.getDockerImageName());
        }

        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        log.info("MariaDB 초기 사용자 수: {}", userCount);
        assertThat(userCount).isGreaterThanOrEqualTo(2);
        jdbcTemplate.execute("INSERT INTO test_users (name, email) VALUES ('Multi Container User', 'multi@test.com')");
        String cacheKey = "user:count";
        String userData = "multi-container-test-data";
        redisTemplate.opsForValue().set(cacheKey, userData);
        String cachedData = (String) redisTemplate.opsForValue().get(cacheKey);
        assertThat(cachedData).isEqualTo(userData);
        log.info("Redis 캐시 데이터 확인: {}", cachedData);
        Integer newUserCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        redisTemplate.opsForValue().set("user:total:count", newUserCount.toString());
        String cachedCount = (String) redisTemplate.opsForValue().get("user:total:count");
        assertThat(Integer.parseInt(cachedCount)).isEqualTo(newUserCount);
        log.info("다중 컨테이너 통합 테스트 완료 - DB 사용자 수: {}, 캐시된 값: {}", newUserCount, cachedCount);
    }

    @Test
    @Order(2)
    @DisplayName("캐시 기반 성능 최적화 시나리오 테스트")
    void testCacheOptimizationScenario() {
        log.info("캐시 기반 성능 최적화 시나리오 테스트 시작");

        for (int i = 1; i <= 100; i++) {
            jdbcTemplate.execute(String.format("INSERT INTO test_users (name, email) VALUES ('Bulk User %d', 'bulk%d@cache.com')", i, i));
        }

        long startTime = System.currentTimeMillis();
        Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_users", Integer.class);
        long dbQueryTime = System.currentTimeMillis() - startTime;

        String cacheKey = "performance:user:count";
        redisTemplate.opsForValue().set(cacheKey, totalUsers.toString());

        startTime = System.currentTimeMillis();
        String cachedResult = (String) redisTemplate.opsForValue().get(cacheKey);
        long cacheQueryTime = System.currentTimeMillis() - startTime;
        assertThat(Integer.parseInt(cachedResult)).isEqualTo(totalUsers);
        log.info("성능 비교 - DB 쿼리 시간: {}ms, 캐시 조회 시간: {}ms", dbQueryTime, cacheQueryTime);

        String complexQuery = """
                    SELECT 
                        COUNT(*) as total_count,
                        COUNT(DISTINCT SUBSTRING_INDEX(email, '@', -1)) as unique_domains,
                        AVG(LENGTH(name)) as avg_name_length
                    FROM test_users
                """;

        startTime = System.currentTimeMillis();
        var complexResult = jdbcTemplate.queryForMap(complexQuery);
        long complexQueryTime = System.currentTimeMillis() - startTime;

        String complexCacheKey = "performance:complex:stats";
        redisTemplate.opsForHash().putAll(complexCacheKey, complexResult);

        startTime = System.currentTimeMillis();
        var cachedComplexResult = redisTemplate.opsForHash().entries(complexCacheKey);
        long cachedComplexQueryTime = System.currentTimeMillis() - startTime;

        assertThat(cachedComplexResult).isNotEmpty();

        // Redis에서 가져온 값은 타입이 다를 수 있으므로 문자열로 비교하거나 타입 변환 후 비교
        Object cachedCount = cachedComplexResult.get("total_count");
        Object dbCount = complexResult.get("total_count");

        // Long 타입으로 통일해서 비교
        Long cachedCountLong = cachedCount instanceof Number ? ((Number) cachedCount).longValue() : Long.parseLong(cachedCount.toString());
        Long dbCountLong = dbCount instanceof Number ? ((Number) dbCount).longValue() : Long.parseLong(dbCount.toString());

        assertThat(cachedCountLong).isEqualTo(dbCountLong);

        log.info("복잡한 쿼리 성능 비교 - DB: {}ms, 캐시: {}ms", complexQueryTime, cachedComplexQueryTime);
        log.info("캐시 기반 성능 최적화 시나리오 테스트 완료");
    }
}