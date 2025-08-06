package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerConfiguration;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MySQLContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec) {
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse(spec.getImageOrDefault(ContainerType.MYSQL)))
                .withDatabaseName(spec.getDatabaseOrDefault())
                .withUsername(spec.getUsernameOrDefault())
                .withPassword(spec.getPasswordOrDefault())
                .withStartupTimeout(Duration.ofSeconds(spec.getStartupTimeoutOrDefault()));
        
        ContainerConfigurationHelper.configureContainer(container, spec);
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MYSQL;
    }
}