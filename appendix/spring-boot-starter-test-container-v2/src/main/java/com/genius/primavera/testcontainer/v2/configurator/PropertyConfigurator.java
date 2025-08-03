package com.genius.primavera.testcontainer.v2.configurator;

import org.testcontainers.containers.GenericContainer;

public interface PropertyConfigurator {
    
    void configureSpringProperties(GenericContainer<?> container);
    
    boolean supports(Class<? extends GenericContainer<?>> containerClass);
}