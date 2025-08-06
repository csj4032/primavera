package com.genius.primavera.testcontainer;

import org.testcontainers.containers.GenericContainer;

public interface ContainerCreator {
    
    GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec);
    
    ContainerType getSupportedType();
}