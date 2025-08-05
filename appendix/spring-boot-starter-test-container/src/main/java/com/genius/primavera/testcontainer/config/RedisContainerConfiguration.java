package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;

/**
 * Redis TestContainer 설정
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class RedisContainerConfiguration {

    @Bean
    public GenericContainer<?> redisContainer() {
        log.info("★ Redis TestContainer 생성 요청:");
        log.info("   - 이미지: {}", ContainerType.REDIS.getDefaultImage());
        log.info("   - 기본 포트: {}", ContainerType.REDIS.getDefaultPort());

        GenericContainer<?> redis = new GenericContainer<>(ContainerType.REDIS.getDefaultImage())
                .withExposedPorts(ContainerType.REDIS.getDefaultPort())
                .withReuse(false);

        log.info("★ Redis 컨테이너 시작 중...");
        redis.start();

        Integer mappedPort = redis.getMappedPort(ContainerType.REDIS.getDefaultPort());
        log.info("★ Redis 컨테이너 시작 완료:");
        log.info("   - Host: {}", redis.getHost());
        log.info("   - Mapped Port: {}", mappedPort);
        log.info("   - Container ID: {}", redis.getContainerId());

        return redis;
    }
}