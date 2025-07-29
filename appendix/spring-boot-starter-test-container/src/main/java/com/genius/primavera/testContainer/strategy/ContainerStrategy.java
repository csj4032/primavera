package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

public interface ContainerStrategy {

    ContainerType getContainerType();

    void startContainer(ConfigurableApplicationContext applicationContext);

    GenericContainer<?> getContainer();

    boolean isRunning();
}