package com.genius.primavera.test.strategy;

import com.genius.primavera.test.config.PostgreSQLContainerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * PostgreSQL TestContainer 관리 Strategy
 */
@Slf4j
@RequiredArgsConstructor
public class PostgreSQLContainerStrategy implements ContainerStrategy {
    
    private static final String CONTAINER_TYPE = "postgresql";
    private final PostgreSQLContainerConfig config;
    private PostgreSQLContainer<?> container;
    
    @Override
    public GenericContainer<?> startContainer(ConfigurableApplicationContext context) {
        if (container == null) {
            container = createContainer();
        }
        
        if (!container.isRunning()) {
            log.info("Starting PostgreSQL container with image: {}", config.getImage());
            container.start();
            log.info("PostgreSQL container started successfully");
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
    
    private PostgreSQLContainer<?> createContainer() {
        var containerBuilder = new PostgreSQLContainer<>(config.getImage())
                .withDatabaseName(config.getDatabaseName())
                .withUsername(config.getUsername())
                .withPassword(config.getPassword());
        
        if (config.getInitScript() != null) {
            containerBuilder.withInitScript(config.getInitScript());
        }
        
        if (config.isReuse()) {
            containerBuilder.withReuse(true);
        }
        
        return containerBuilder;
    }
    
    private void configureSpringProperties(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        Map<String, Object> properties = new HashMap<>();
        
        properties.put("spring.datasource.url", container.getJdbcUrl());
        properties.put("spring.datasource.username", container.getUsername());
        properties.put("spring.datasource.password", container.getPassword());
        properties.put("spring.datasource.driver-class-name", config.getDriverClassName());
        
        environment.getPropertySources().addFirst(
            new MapPropertySource("testcontainers-postgresql", properties)
        );
        
        log.debug("PostgreSQL properties configured: url={}, username={}", 
                 container.getJdbcUrl(), container.getUsername());
    }
}