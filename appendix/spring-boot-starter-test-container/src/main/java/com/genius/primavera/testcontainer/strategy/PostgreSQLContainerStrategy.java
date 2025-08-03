package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class PostgreSQLContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        PrimaveraTestcontainersProperties.DatabaseConfig dbConfig = (PrimaveraTestcontainersProperties.DatabaseConfig) config;
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(config.getDockerImageName())
                .withDatabaseName(dbConfig.getDatabaseName())
                .withUsername(dbConfig.getUsername())
                .withPassword(dbConfig.getPassword());
        if (dbConfig.getInitScript() != null && !dbConfig.getInitScript().isEmpty()) container.withInitScript(dbConfig.getInitScript());
        config.getEnvironment().forEach(container::withEnv);
        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        PostgreSQLContainer<?> postgresContainer = (PostgreSQLContainer<?>) container;
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-postgresql", Map.of(
                "spring.datasource.url", postgresContainer.getJdbcUrl(),
                "spring.datasource.username", postgresContainer.getUsername(),
                "spring.datasource.password", postgresContainer.getPassword(),
                "spring.datasource.driver-class-name", postgresContainer.getDriverClassName()
        )));
    }
}