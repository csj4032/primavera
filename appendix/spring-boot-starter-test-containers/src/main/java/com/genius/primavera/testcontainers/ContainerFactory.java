package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import org.testcontainers.containers.GenericContainer;

public class ContainerFactory {
    
    public static GenericContainer<?> create(ContainerType type, BaseContainerSpec spec) {
        return ContainerCreatorRegistry.findCreator(type)
                .map(creator -> creator.create(spec))
                .orElseThrow(() -> new IllegalArgumentException("No creator registered for container type: " + type));
    }
}