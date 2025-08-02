package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.Map;

public class RedisContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        PrimaveraTestcontainersProperties.RedisConfig redisConfig = (PrimaveraTestcontainersProperties.RedisConfig) config;
        
        GenericContainer<?> container = new GenericContainer<>(config.getDockerImageName())
                .withExposedPorts(redisConfig.getPort());

        // 패스워드가 설정되어 있으면 Redis 설정에 추가
        if (redisConfig.getPassword() != null && !redisConfig.getPassword().isEmpty()) {
            container.withCommand("redis-server", "--requirepass", redisConfig.getPassword());
        }

        // 환경 변수 설정
        config.getEnvironment().forEach(container::withEnv);

        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.data.redis.host", container.getHost());
        properties.put("spring.data.redis.port", container.getMappedPort(6379));

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-redis", properties));
    }
}