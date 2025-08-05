package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Kafka TestContainer 설정
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class KafkaContainerConfiguration {

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        log.info("Kafka TestContainer를 생성합니다: {}", ContainerType.KAFKA.getDefaultImage());
        
        return new KafkaContainer(DockerImageName.parse(ContainerType.KAFKA.getDefaultImage()))
                .withReuse(false);
    }
}