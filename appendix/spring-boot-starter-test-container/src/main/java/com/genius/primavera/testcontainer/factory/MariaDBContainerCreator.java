package com.genius.primavera.testcontainer.factory;

import com.genius.primavera.testcontainer.ContainerConfiguration;
import com.genius.primavera.testcontainer.ContainerCreator;
import com.genius.primavera.testcontainer.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MariaDBContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec) {
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(spec.getImageOrDefault(ContainerType.MARIADB)))
                .withDatabaseName(spec.getDatabaseOrDefault())
                .withUsername(spec.getUsernameOrDefault())
                .withPassword(spec.getPasswordOrDefault())
                .withStartupTimeout(Duration.ofSeconds(spec.getStartupTimeoutOrDefault()));
        
        ContainerConfigurationHelper.configureContainer(container, spec);
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MARIADB;
    }
}