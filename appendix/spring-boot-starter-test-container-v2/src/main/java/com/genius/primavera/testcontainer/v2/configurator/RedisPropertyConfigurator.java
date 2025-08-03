package com.genius.primavera.testcontainer.v2.configurator;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

@Slf4j
public class RedisPropertyConfigurator implements PropertyConfigurator {
    
    @Override
    public void configureSpringProperties(GenericContainer<?> container) {
        System.setProperty("spring.data.redis.host", container.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(container.getMappedPort(6379)));
        
        log.info("Set Redis properties - Host: {}, Port: {}", 
                container.getHost(), container.getMappedPort(6379));
    }
    
    @Override
    public boolean supports(Class<? extends GenericContainer<?>> containerClass) {
        return containerClass.equals(GenericContainer.class);
    }
}