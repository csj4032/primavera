package com.genius.primavera.testcontainer.v2.configurator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

@Slf4j
public class DynamicPropertyConfiguratorFactory {
    
    private static final List<DynamicPropertyConfigurator> CONFIGURATORS = List.of(
        new MariaDBDynamicPropertyConfigurator()
    );
    
    public static void configureDynamicProperties(GenericContainer<?> container, DynamicPropertyRegistry registry) {
        @SuppressWarnings("unchecked")
        Class<? extends GenericContainer<?>> containerClass = (Class<? extends GenericContainer<?>>) container.getClass();
        
        DynamicPropertyConfigurator configurator = CONFIGURATORS.stream()
                .filter(config -> config.supports(containerClass))
                .findFirst()
                .orElse(null);
        
        if (configurator != null) {
            configurator.configureDynamicProperties(container, registry);
        } else {
            log.warn("No dynamic property configurator found for container class: {}", containerClass.getName());
        }
    }
}