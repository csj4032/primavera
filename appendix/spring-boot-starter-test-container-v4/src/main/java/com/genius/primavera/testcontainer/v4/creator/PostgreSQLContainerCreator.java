package com.genius.primavera.testcontainer.v4.creator;

import com.genius.primavera.testcontainer.v4.ContainerConfiguration;
import com.genius.primavera.testcontainer.v4.ContainerCreator;
import com.genius.primavera.testcontainer.v4.ContainerType;
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