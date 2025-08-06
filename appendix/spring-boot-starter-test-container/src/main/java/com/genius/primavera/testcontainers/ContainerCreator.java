package com.genius.primavera.testcontainers;

import org.testcontainers.containers.GenericContainer;

public interface ContainerCreator {
    
    GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec);
    
    ContainerType getSupportedType();
}