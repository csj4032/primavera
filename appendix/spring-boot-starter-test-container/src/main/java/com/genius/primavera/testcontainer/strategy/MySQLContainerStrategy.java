package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;

public class MySQLContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        PrimaveraTestcontainersProperties.DatabaseConfig dbConfig = (PrimaveraTestcontainersProperties.DatabaseConfig) config;
        MySQLContainer<?> container = new MySQLContainer<>(config.getDockerImageName())
                .withDatabaseName(dbConfig.getDatabaseName())
                .withUsername(dbConfig.getUsername())
                .withPassword(dbConfig.getPassword());
        if (dbConfig.getInitScript() != null && !dbConfig.getInitScript().isEmpty()) container.withInitScript(dbConfig.getInitScript());
        config.getEnvironment().forEach(container::withEnv);
        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        MySQLContainer<?> mysqlContainer = (MySQLContainer<?>) container;
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-mysql", Map.of(
                "spring.datasource.url", mysqlContainer.getJdbcUrl(),
                "spring.datasource.username", mysqlContainer.getUsername(),
                "spring.datasource.password", mysqlContainer.getPassword(),
                "spring.datasource.driver-class-name", mysqlContainer.getDriverClassName()
        )));
    }
}