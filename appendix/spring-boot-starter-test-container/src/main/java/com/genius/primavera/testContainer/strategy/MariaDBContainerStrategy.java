package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.config.MariaDBContainerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

/**
 * MariaDB TestContainer 관리 Strategy
 */
@Slf4j
@RequiredArgsConstructor
public class MariaDBContainerStrategy implements ContainerStrategy {
    
    private static final String CONTAINER_TYPE = "mariadb";
    private final MariaDBContainerConfig config;
    private MariaDBContainer<?> container;
    
    @Override
    public GenericContainer<?> startContainer(ConfigurableApplicationContext context) {
        if (container == null) {
            container = createContainer();
        }
        
        if (!container.isRunning()) {
            log.info("Starting MariaDB container with image: {}", config.getImage());
            container.start();
            log.info("MariaDB container started successfully");
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
    
    private MariaDBContainer<?> createContainer() {
        var containerBuilder = new MariaDBContainer<>(DockerImageName.parse(config.getImage()))
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
            new MapPropertySource("testcontainers-mariadb", properties)
        );
        
        log.debug("MariaDB properties configured: url={}, username={}", 
                 container.getJdbcUrl(), container.getUsername());
    }
}