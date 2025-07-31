package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@Slf4j
public class RedisContainerStrategy extends AbstractContainerStrategy<GenericContainer<?>> {

    private final PrimaveraTestcontainersProperties.Redis config;

    public RedisContainerStrategy(Environment environment, PrimaveraTestcontainersProperties.Redis config) {
        super(ContainerType.REDIS, environment);
        this.config = config;
    }

    @Override
    protected GenericContainer<?> createContainer() {
        return new GenericContainer<>(DockerImageName.parse(config.getImage()))
            .withExposedPorts(config.getPort());
    }

    @Override
    protected Map<String, Object> getSpringProperties(GenericContainer<?> container) {
        return Map.of(
            "spring.redis.host", container.getHost(),
            "spring.redis.port", container.getMappedPort(config.getPort())
        );
    }
}
