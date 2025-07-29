package com.genius.primavera.testContainer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@Slf4j
public class RedisContainerConfig implements ContainerConfig<GenericContainer<?>> {

    private static final String IMAGE_KEY = "primavera.testcontainers.redis.image";
    private static final String PORT_KEY = "primavera.testcontainers.redis.port";
    private static final String DEFAULT_IMAGE = "redis:6-alpine";
    private static final int DEFAULT_PORT = 6379;

    public RedisContainerConfig() {
    }

    @Override
    public String getImageName() {
        return "redis:6-alpine";
    }

    @Override
    public GenericContainer<?> createContainer(Environment environment) {
        log.info("Creating Redis container with image {} and port {}", IMAGE_KEY, PORT_KEY);
        String image = environment.getProperty(IMAGE_KEY, DEFAULT_IMAGE);
        Integer port = environment.getProperty(PORT_KEY, Integer.class, DEFAULT_PORT);
        return new GenericContainer<>(DockerImageName.parse(image)).withExposedPorts(port).withReuse(true);
    }

    @Override
    public Map<String, Object> getSpringProperties(GenericContainer<?> container, Environment environment) {
        return Map.of(
                "spring.redis.host", container.getHost(),
                "spring.redis.port", container.getMappedPort(environment.getProperty(PORT_KEY, Integer.class, DEFAULT_PORT))
        );
    }
}