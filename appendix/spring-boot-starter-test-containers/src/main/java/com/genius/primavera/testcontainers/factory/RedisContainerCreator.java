package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.RedisContainerSpec;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RedisContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.REDIS.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(image))
                .withExposedPorts(6379)
                .withStartupTimeout(Duration.ofSeconds(timeout));
        
        if (spec instanceof RedisContainerSpec redisSpec) {
            List<String> command = new ArrayList<>();
            command.add("redis-server");
            
            if (redisSpec.getPassword() != null) {
                command.add("--requirepass");
                command.add(redisSpec.getPassword());
            }
            
            if (redisSpec.getMaxMemory() != null) {
                command.add("--maxmemory");
                command.add(redisSpec.getMaxMemory());
            }
            
            if (redisSpec.getMaxMemoryPolicy() != null) {
                command.add("--maxmemory-policy");
                command.add(redisSpec.getMaxMemoryPolicy().name().toLowerCase().replace('_', '-'));
            }
            
            if (redisSpec.getPersistenceEnabled() != null && !redisSpec.getPersistenceEnabled()) {
                command.add("--save");
                command.add("\"\"");
            }
            
            if (command.size() > 1) {
                container.withCommand(command.toArray(new String[0]));
            }
        }
        
        // 공통 환경 변수 적용
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.REDIS;
    }
}