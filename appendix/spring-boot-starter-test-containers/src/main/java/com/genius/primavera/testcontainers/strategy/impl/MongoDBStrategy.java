package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.MongoContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * MongoDB-specific strategy implementation
 */
public class MongoDBStrategy implements ContainerTypeStrategy {
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MONGODB;
    }
    
    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        if (spec instanceof MongoContainerSpec mongoSpec) {
            if (mongoSpec.getDatabase() == null) mongoSpec.setDatabase("primavera");
            if (mongoSpec.getUsername() == null) mongoSpec.setUsername("primavera");
            if (mongoSpec.getPassword() == null) mongoSpec.setPassword("primavera");
        }
    }
    
    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) {
            return instanceConfig.getMongodb();
        }
        return null;
    }
    
    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        String mongoPrefix = "spring.data.mongodb." + containerInfo.name();
        properties.put(mongoPrefix + ".uri", containerInfo.getConnectionString());
    }
    
    @Override
    public BaseContainerSpec createDefaultSpec() {
        MongoContainerSpec spec = new MongoContainerSpec();
        spec.setImage(ContainerType.MONGODB.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}