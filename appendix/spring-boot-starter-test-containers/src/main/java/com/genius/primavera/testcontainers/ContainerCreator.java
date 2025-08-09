package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import org.testcontainers.containers.GenericContainer;

public interface ContainerCreator {
    
    GenericContainer<?> create(BaseContainerSpec spec);
    
    ContainerType getSupportedType();
}