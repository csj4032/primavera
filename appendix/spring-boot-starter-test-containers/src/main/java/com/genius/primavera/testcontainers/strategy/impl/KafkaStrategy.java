package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.KafkaContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * Kafka-specific strategy implementation
 */
public class KafkaStrategy implements ContainerTypeStrategy {
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.KAFKA;
    }
    
    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        // KafkaContainerSpec defaults are handled in the spec itself
    }
    
    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) {
            return instanceConfig.getKafka();
        }
        return null;
    }
    
    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        String kafkaPrefix = "spring.kafka." + containerInfo.name();
        properties.put(kafkaPrefix + ".bootstrap-servers", containerInfo.getConnectionString());
    }
    
    @Override
    public BaseContainerSpec createDefaultSpec() {
        KafkaContainerSpec spec = new KafkaContainerSpec();
        spec.setImage(ContainerType.KAFKA.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}