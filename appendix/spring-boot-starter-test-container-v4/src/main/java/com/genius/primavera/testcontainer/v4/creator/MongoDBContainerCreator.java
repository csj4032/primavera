package com.genius.primavera.testcontainer.v4.creator;

import com.genius.primavera.testcontainer.v4.ContainerConfiguration;
import com.genius.primavera.testcontainer.v4.ContainerCreator;
import com.genius.primavera.testcontainer.v4.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MongoDBContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(spec.getImageOrDefault(ContainerType.MONGODB)))
                .withExposedPorts(27017)
                .withStartupTimeout(Duration.ofSeconds(spec.getStartupTimeoutOrDefault()));
        
        ContainerConfigurationHelper.configureContainer(container, spec);
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MONGODB;
    }
}