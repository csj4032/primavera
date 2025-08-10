package com.genius.primavera.testcontainers.strategy;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;

import java.util.Map;

/**
 * Strategy interface for container type-specific operations
 * Eliminates duplicate switch-case statements across the codebase
 */
public interface ContainerTypeStrategy {
    
    /**
     * Returns the container type this strategy handles
     */
    ContainerType getSupportedType();
    
    /**
     * Applies type-specific default values to a spec
     */
    void applyDefaults(BaseContainerSpec spec);
    
    /**
     * Retrieves the specific spec from container configuration
     */
    BaseContainerSpec getSpecFromConfiguration(Object config);
    
    /**
     * Configures container-specific properties for Spring context
     */
    void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties);
    
    /**
     * Creates a default spec instance for this container type
     */
    BaseContainerSpec createDefaultSpec();
}