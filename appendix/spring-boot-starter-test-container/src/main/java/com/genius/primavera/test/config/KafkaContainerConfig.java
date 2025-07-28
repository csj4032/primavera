package com.genius.primavera.test.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka TestContainer 설정
 */
@Data
@ConfigurationProperties(prefix = "primavera.testcontainers.kafka")
public class KafkaContainerConfig {
    
    private String image = "confluentinc/cp-kafka:latest";
    private boolean reuse = true;
}