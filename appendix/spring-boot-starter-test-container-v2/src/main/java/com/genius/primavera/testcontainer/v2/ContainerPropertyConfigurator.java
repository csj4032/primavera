package com.genius.primavera.testcontainer.v2;

import com.genius.primavera.testcontainer.v2.configurator.PropertyConfiguratorFactory;
import org.testcontainers.containers.GenericContainer;

public class ContainerPropertyConfigurator {
    
    public static void configureSpringProperties(ContainerType containerType, GenericContainer<?> container) {
        PropertyConfiguratorFactory.configureSpringProperties(container);
    }
}