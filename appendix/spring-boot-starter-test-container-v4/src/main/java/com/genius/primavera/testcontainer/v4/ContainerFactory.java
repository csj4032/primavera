package com.genius.primavera.testcontainer.v4;

import org.testcontainers.containers.GenericContainer;

public class ContainerFactory {
    
    public static GenericContainer<?> create(ContainerType type, ContainerConfiguration.ContainerSpec spec) {
        ContainerCreator creator = ContainerCreatorRegistry.getCreator(type);
        return creator.create(spec);
    }
}