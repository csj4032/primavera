package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@Slf4j
public class MariaDBContainerStrategy extends AbstractContainerStrategy<MariaDBContainer<?>> {

    private final PrimaveraTestcontainersProperties.Mariadb config;

    public MariaDBContainerStrategy(Environment environment, PrimaveraTestcontainersProperties.Mariadb config) {
        super(ContainerType.MARIADB, environment);
        this.config = config;
    }

    @Override
    protected MariaDBContainer<?> createContainer() {
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(config.getImage()))
            .withDatabaseName(config.getDatabaseName())
            .withUsername(config.getUsername())
            .withPassword(config.getPassword());
        
        if (config.getInitScript() != null && !config.getInitScript().trim().isEmpty()) {
            container.withInitScript(config.getInitScript());
        }
        
        return container;
    }

    @Override
    public Map<String, Object> getSpringProperties(MariaDBContainer<?> container) {
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