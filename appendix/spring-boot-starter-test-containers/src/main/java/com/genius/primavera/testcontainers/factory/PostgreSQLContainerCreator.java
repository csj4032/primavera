package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerConfiguration;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class PostgreSQLContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec) {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(spec.getImageOrDefault(ContainerType.POSTGRESQL)))
                .withDatabaseName(spec.getDatabaseOrDefault())
                .withUsername(spec.getUsernameOrDefault())
                .withPassword(spec.getPasswordOrDefault())
                .withStartupTimeout(Duration.ofSeconds(spec.getStartupTimeoutOrDefault()));
        
        ContainerConfigurationHelper.configureContainer(container, spec);
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.POSTGRESQL;
    }
}