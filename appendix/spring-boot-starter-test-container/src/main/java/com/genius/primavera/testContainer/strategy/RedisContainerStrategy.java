package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.config.RedisContainerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis TestContainer 관리 Strategy
 */
@Slf4j
@RequiredArgsConstructor
public class RedisContainerStrategy implements ContainerStrategy {
    
    private static final String CONTAINER_TYPE = "redis";
    private final RedisContainerConfig config;
    private GenericContainer<?> container;
    
    @Override
    public GenericContainer<?> startContainer(ConfigurableApplicationContext context) {
        if (container == null) {
            container = createContainer();
        }
        
        if (!container.isRunning()) {
            log.info("Starting Redis container with image: {}", config.getImage());
            container.start();
            log.info("Redis container started successfully");
        }
        
        configureSpringProperties(context);
        return container;
    }
    
    @Override
    public String getContainerType() {
        return CONTAINER_TYPE;
    }
    
    @Override
    public boolean isRunning() {
        return container != null && container.isRunning();
    }
    
    @Override
    public GenericContainer<?> getContainer() {
        return container;
    }
    
    private GenericContainer<?> createContainer() {
        var containerBuilder = new GenericContainer<>(DockerImageName.parse(config.getImage()))
                .withExposedPorts(config.getPort());
        
        if (config.getPassword() != null) {
            containerBuilder.withCommand("redis-server", "--requirepass", config.getPassword());
        }
        
        if (config.isReuse()) {
            containerBuilder.withReuse(true);
        }
        
        return containerBuilder;
    }
    
    private void configureSpringProperties(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        Map<String, Object> properties = new HashMap<>();
        
        properties.put("spring.data.redis.host", container.getHost());
        properties.put("spring.data.redis.port", container.getMappedPort(config.getPort()));
        
        if (config.getPassword() != null) {
            properties.put("spring.data.redis.password", config.getPassword());
        }
        
        environment.getPropertySources().addFirst(
            new MapPropertySource("testcontainers-redis", properties)
        );
        
        log.debug("Redis properties configured: host={}, port={}", 
                 container.getHost(), container.getMappedPort(config.getPort()));
    }
}