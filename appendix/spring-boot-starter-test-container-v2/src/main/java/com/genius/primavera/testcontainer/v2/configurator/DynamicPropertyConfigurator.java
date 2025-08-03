package com.genius.primavera.testcontainer.v2.configurator;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;

public interface DynamicPropertyConfigurator {
    
    void configureDynamicProperties(GenericContainer<?> container, DynamicPropertyRegistry registry);
    
    boolean supports(Class<? extends GenericContainer<?>> containerClass);
}