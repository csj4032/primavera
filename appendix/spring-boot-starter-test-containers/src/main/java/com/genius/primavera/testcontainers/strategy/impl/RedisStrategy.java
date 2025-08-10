package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.RedisContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * Redis-specific strategy implementation
 */
public class RedisStrategy implements ContainerTypeStrategy {
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.REDIS;
    }
    
    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        if (spec instanceof RedisContainerSpec redisSpec) {
            if (redisSpec.getMaxMemory() == null) redisSpec.setMaxMemory("256mb");
        }
    }
    
    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) {
            return instanceConfig.getRedis();
        }
        return null;
    }
    
    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        String redisPrefix = "spring.data.redis." + containerInfo.name();
        properties.put(redisPrefix + ".host", containerInfo.getHost());
        properties.put(redisPrefix + ".port", containerInfo.getMappedPort());
        
        if (containerInfo.spec() instanceof RedisContainerSpec redisSpec && redisSpec.getPassword() != null) {
            properties.put(redisPrefix + ".password", redisSpec.getPassword());
        }
    }
    
    @Override
    public BaseContainerSpec createDefaultSpec() {
        RedisContainerSpec spec = new RedisContainerSpec();
        spec.setImage(ContainerType.REDIS.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}