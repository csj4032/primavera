package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.config.PostgreSQLContainerConfig;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLContainerStrategy extends AbstractContainerStrategy<PostgreSQLContainer<?>> {
    public PostgreSQLContainerStrategy(PostgreSQLContainerConfig config) {
        super(ContainerType.POSTGRESQL, config);
    }
}