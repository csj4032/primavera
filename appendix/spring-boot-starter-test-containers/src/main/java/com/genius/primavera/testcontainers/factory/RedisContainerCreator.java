package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerConfiguration;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
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