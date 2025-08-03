package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

public class RedisContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        PrimaveraTestcontainersProperties.RedisConfig redisConfig = (PrimaveraTestcontainersProperties.RedisConfig) config;
        GenericContainer<?> container = new GenericContainer<>(config.getDockerImageName()).withExposedPorts(redisConfig.getPort());
        if (redisConfig.getPassword() != null && !redisConfig.getPassword().isEmpty()) container.withCommand("redis-server", "--requirepass", redisConfig.getPassword());
        config.getEnvironment().forEach(container::withEnv);
        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-redis", Map.of(
                "spring.data.redis.host", container.getHost(),
                "spring.data.redis.port", container.getMappedPort(6379)
        )));
    }
}