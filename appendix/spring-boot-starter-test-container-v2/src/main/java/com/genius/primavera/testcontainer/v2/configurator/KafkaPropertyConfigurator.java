package com.genius.primavera.testcontainer.v2.configurator;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;

@Slf4j
public class KafkaPropertyConfigurator implements PropertyConfigurator {
    
    @Override
    public void configureSpringProperties(GenericContainer<?> container) {
        KafkaContainer kafkaContainer = (KafkaContainer) container;
        String bootstrapServers = kafkaContainer.getBootstrapServers();
        
        System.setProperty("spring.kafka.bootstrap-servers", bootstrapServers);
        
        log.info("Set Kafka properties - Bootstrap servers: {}", bootstrapServers);
    }
    
    @Override
    public boolean supports(Class<? extends GenericContainer<?>> containerClass) {
        return KafkaContainer.class.isAssignableFrom(containerClass);
    }
}