package com.genius.primavera.testContainer.config;

import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

public interface ContainerConfig<T extends GenericContainer<?>> {

    String getImageName();

    T createContainer(Environment environment);

    Map<String, Object> getSpringProperties(T container, Environment environment);
}
