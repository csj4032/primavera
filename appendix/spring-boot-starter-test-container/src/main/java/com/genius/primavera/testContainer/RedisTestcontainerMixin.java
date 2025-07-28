package com.genius.primavera.testContainer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Redis TestContainer Mixin
 * 
 * 사용법:
 * @SpringBootTest
 * public class MyTest implements RedisTestcontainerMixin {
 *     @Autowired
 *     private RedisTemplate redisTemplate; // 자동으로 Redis 컨테이너에 연결됨
 * }
 */
@Testcontainers
public interface RedisTestcontainerMixin {
    
    @Container
    GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
    
    default String getRedisConnectionString() {
        return redis.getHost() + ":" + redis.getMappedPort(6379);
    }
}