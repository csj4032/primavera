package com.genius.primavera.testcontainer.containertype;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis 컨테이너 단독 테스트
 */
@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        }
)
@DisplayName("Redis TestContainer 테스트")
@EnableTestContainers(containers = ContainerType.REDIS)
class RedisContainerTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GenericContainer<?> redisContainer;

    @Test
    @DisplayName("Redis 컨테이너 연결 및 기본 작업 테스트")
    void testRedisConnection() {
        log.info("Redis 컨테이너 테스트 시작");
        if (redisContainer != null) {
            assertThat(redisContainer.isRunning()).isTrue();
            log.info("Redis 컨테이너 실행 중: {}:{}", redisContainer.getHost(), redisContainer.getMappedPort(ContainerType.REDIS.getDefaultPort()));
        }

        // Redis 기본 작업 테스트
        String testKey = "test:redis:key";
        String testValue = "Redis Container Test Value";

        // 값 저장
        redisTemplate.opsForValue().set(testKey, testValue);

        // 값 조회
        String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
        assertThat(retrievedValue).isEqualTo(testValue);

        log.info("Redis 테스트 완료 - 저장된 값: {}, 조회된 값: {}", testValue, retrievedValue);

        // 추가 Redis 작업 테스트
        String hashKey = "test:hash";
        redisTemplate.opsForHash().put(hashKey, "field1", "value1");
        redisTemplate.opsForHash().put(hashKey, "field2", "value2");

        Object hashValue = redisTemplate.opsForHash().get(hashKey, "field1");
        assertThat(hashValue).isEqualTo("value1");

        log.info("Redis Hash 테스트 완료");
    }
}