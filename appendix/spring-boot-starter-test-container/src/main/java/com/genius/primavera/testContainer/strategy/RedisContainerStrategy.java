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
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(config.getImage()))
            .withExposedPorts(config.getPort())
            .withCommand("redis-server", "--bind", "0.0.0.0")
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*Ready to accept connections.*", 1))
            .withStartupTimeout(java.time.Duration.ofSeconds(60));
        
        log.info("Created Redis container with image: {} on port: {}", config.getImage(), config.getPort());
        return container;
    }

    @Override
    public Map<String, Object> getSpringProperties(GenericContainer<?> container) {
        if (!container.isRunning()) {
            throw new IllegalStateException("Container must be started before accessing properties");
        }
        return Map.of(
            "spring.data.redis.host", container.getHost(),
            "spring.data.redis.port", container.getMappedPort(config.getPort())
        );
    }
}
