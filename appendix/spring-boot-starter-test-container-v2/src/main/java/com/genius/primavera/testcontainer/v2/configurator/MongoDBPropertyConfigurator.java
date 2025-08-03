package com.genius.primavera.testcontainer.v2.configurator;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

@Slf4j
public class MongoDBPropertyConfigurator implements PropertyConfigurator {
    
    @Override
    public void configureSpringProperties(GenericContainer<?> container) {
        String mongoUri = String.format("mongodb://%s:%d/primavera", 
                container.getHost(), container.getMappedPort(27017));
        
        System.setProperty("spring.data.mongodb.uri", mongoUri);
        
        log.info("Set MongoDB properties - URI: {}", mongoUri);
    }
    
    @Override
    public boolean supports(Class<? extends GenericContainer<?>> containerClass) {
        return MongoDBContainer.class.isAssignableFrom(containerClass);
    }
}