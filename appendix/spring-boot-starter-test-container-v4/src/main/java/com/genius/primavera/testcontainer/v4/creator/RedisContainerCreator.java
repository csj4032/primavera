package com.genius.primavera.testcontainer.v4.creator;

import com.genius.primavera.testcontainer.v4.ContainerConfiguration;
import com.genius.primavera.testcontainer.v4.ContainerCreator;
import com.genius.primavera.testcontainer.v4.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Optional;

public class RedisContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(spec.getImageOrDefault(ContainerType.REDIS)))
                .withExposedPorts(6379)
                .withStartupTimeout(Duration.ofSeconds(spec.getStartupTimeoutOrDefault()));
        
        Optional.ofNullable(spec.getPassword())
                .ifPresent(password -> container.withCommand("redis-server", "--requirepass", password));
        
        ContainerConfigurationHelper.configureContainer(container, spec);
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.REDIS;
    }
}