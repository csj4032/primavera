package com.genius.primavera.testcontainer.v2.factory;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class KafkaContainerFactory implements ContainerBuilderFactory {
    
    @Override
    public GenericContainer<?> createContainer(TestContainerProperties.ContainerConfig config) {
        TestContainerProperties.KafkaConfig kafkaConfig = (TestContainerProperties.KafkaConfig) config;
        
        KafkaContainer container = new KafkaContainer(DockerImageName.parse(kafkaConfig.getDockerImageName()));
        
        log.info("Created Kafka container with image: {}", kafkaConfig.getDockerImageName());
        return container;
    }
    
    @Override
    public boolean supports(ContainerType containerType) {
        return containerType == ContainerType.KAFKA;
    }
}