package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class KafkaContainerStrategy implements ContainerStrategy {

    @Override
    public GenericContainer<?> createContainer(PrimaveraTestcontainersProperties.ContainerConfig config) {
        KafkaContainer container = new KafkaContainer(DockerImageName.parse(config.getDockerImageName()));
        config.getEnvironment().forEach(container::withEnv);
        return container;
    }

    @Override
    public void configureApplicationContext(ConfigurableApplicationContext applicationContext, GenericContainer<?> container) {
        KafkaContainer kafkaContainer = (KafkaContainer) container;
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-kafka", Map.of(
                "spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers(),
                "spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer", "org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.consumer.key-deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer", "org.apache.kafka.common.serialization.StringDeserializer"
        )));
    }
}