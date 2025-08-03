package com.genius.primavera.testcontainer.v2.factory;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j  
public class RedisContainerFactory implements ContainerBuilderFactory {
    
    @Override
    public GenericContainer<?> createContainer(TestContainerProperties.ContainerConfig config) {
        TestContainerProperties.RedisConfig redisConfig = (TestContainerProperties.RedisConfig) config;
        
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(redisConfig.getDockerImageName()))
                .withExposedPorts(6379);
        
        log.info("Created Redis container with image: {}", redisConfig.getDockerImageName());
        return container;
    }
    
    @Override
    public boolean supports(ContainerType containerType) {
        return containerType == ContainerType.REDIS;
    }
}