package com.genius.primavera.testcontainer.v4;

import org.testcontainers.containers.GenericContainer;

public interface ContainerCreator {
    
    GenericContainer<?> create(ContainerConfiguration.ContainerSpec spec);
    
    ContainerType getSupportedType();
}