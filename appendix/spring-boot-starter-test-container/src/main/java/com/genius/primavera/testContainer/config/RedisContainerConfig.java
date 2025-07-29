package com.genius.primavera.testContainer.config;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class RedisContainerConfig implements ContainerConfig<GenericContainer<?>> {

    private final PrimaveraTestcontainersProperties.Redis redisProperties;

    public RedisContainerConfig(PrimaveraTestcontainersProperties properties) {
        this.redisProperties = properties.getRedis();
    }

    @Override
    public String getImageName() {
        return "redis:6-alpine";
    }

    @Override
    public GenericContainer<?> createContainer() {
        String image = redisProperties.getImage();
        int port = redisProperties.getPort();
        return new GenericContainer<>(DockerImageName.parse(image))
                .withExposedPorts(port)
                .withReuse(true);
    }

    @Override
    public Map<String, Object> getSpringProperties(GenericContainer<?> container, Environment environment) {
        return Map.of(
                "spring.redis.host", container.getHost(),
                "spring.redis.port", redisProperties.getPort()
        );
    }
}