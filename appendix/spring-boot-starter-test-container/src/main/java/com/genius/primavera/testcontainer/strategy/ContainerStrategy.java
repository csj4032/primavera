package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

public interface ContainerStrategy {
    GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config);
    void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container);
}