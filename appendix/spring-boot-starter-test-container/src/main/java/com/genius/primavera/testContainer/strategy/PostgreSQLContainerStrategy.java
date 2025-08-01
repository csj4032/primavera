package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@Slf4j
public class PostgreSQLContainerStrategy extends AbstractContainerStrategy<PostgreSQLContainer<?>> {
    
    private final PrimaveraTestcontainersProperties.PostgreSQL config;
    
    public PostgreSQLContainerStrategy(Environment environment, PrimaveraTestcontainersProperties.PostgreSQL config) {
        super(ContainerType.POSTGRESQL, environment);
        this.config = config;
    }

    @Override
    protected PostgreSQLContainer<?> createContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(config.getImage()))
            .withDatabaseName(config.getDatabaseName())
            .withUsername(config.getUsername())
            .withPassword(config.getPassword());
            
        if (config.getInitScript() != null) {
            container.withInitScript(config.getInitScript());
        }
        
        if (config.getStartupTimeout() != null) {
            container.withStartupTimeout(config.getStartupTimeout());
        }
        
        return container;
    }

    @Override
    public Map<String, Object> getSpringProperties(PostgreSQLContainer<?> container) {
        if (!container.isRunning()) {
            throw new IllegalStateException("Container must be started before accessing properties");
        }
        return Map.of(
            "spring.datasource.url", container.getJdbcUrl(),
            "spring.datasource.username", container.getUsername(),
            "spring.datasource.password", container.getPassword(),
            "spring.datasource.driver-class-name", container.getDriverClassName()
        );
    }
}