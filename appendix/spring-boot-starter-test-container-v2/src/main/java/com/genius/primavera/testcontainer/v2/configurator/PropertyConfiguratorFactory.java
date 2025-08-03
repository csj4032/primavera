package com.genius.primavera.testcontainer.v2.configurator;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

@Slf4j
public class PropertyConfiguratorFactory {
    
    private static final List<PropertyConfigurator> CONFIGURATORS = List.of(
        new MariaDBPropertyConfigurator(),
        new MySQLPropertyConfigurator(),
        new PostgreSQLPropertyConfigurator(),
        new RedisPropertyConfigurator(),
        new MongoDBPropertyConfigurator(),
        new KafkaPropertyConfigurator(),
        new ElasticsearchPropertyConfigurator()
    );
    
    public static void configureSpringProperties(GenericContainer<?> container) {
        @SuppressWarnings("unchecked")
        Class<? extends GenericContainer<?>> containerClass = (Class<? extends GenericContainer<?>>) container.getClass();
        
        PropertyConfigurator configurator = CONFIGURATORS.stream()
                .filter(config -> config.supports(containerClass))
                .findFirst()
                .orElse(null);
        
        if (configurator != null) {
            configurator.configureSpringProperties(container);
        } else {
            log.warn("No property configurator found for container class: {}", containerClass.getName());
        }
    }
}