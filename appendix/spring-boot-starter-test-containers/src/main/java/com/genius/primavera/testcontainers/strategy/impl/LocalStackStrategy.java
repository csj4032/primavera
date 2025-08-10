package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * LocalStack-specific strategy implementation
 */
public class LocalStackStrategy implements ContainerTypeStrategy {
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.LOCALSTACK;
    }
    
    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        // LocalStackContainerSpec defaults are handled in the spec itself
    }
    
    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) {
            return instanceConfig.getLocalstack();
        }
        return null;
    }
    
    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        // LocalStack-specific properties would be configured here if needed
    }
    
    @Override
    public BaseContainerSpec createDefaultSpec() {
        LocalStackContainerSpec spec = new LocalStackContainerSpec();
        spec.setImage(ContainerType.LOCALSTACK.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}