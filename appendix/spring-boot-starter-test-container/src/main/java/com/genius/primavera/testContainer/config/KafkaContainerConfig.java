package com.genius.primavera.testContainer.config;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.core.env.Environment;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Map;

public class KafkaContainerConfig implements ContainerConfig<KafkaContainer> {

    private final PrimaveraTestcontainersProperties.Kafka kafkaProperties;

    public KafkaContainerConfig(PrimaveraTestcontainersProperties properties) {
        this.kafkaProperties = properties.getKafka();
    }

    @Override
    public String getImageName() {
        return "confluentinc/cp-kafka:latest";
    }

    @Override
    public KafkaContainer createContainer() {
        String image = kafkaProperties.getImage();
        return new KafkaContainer(image)
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                .withEnv("KAFKA_ZOOKEEPER_CONNECT", kafkaProperties.getZookeeperConnect())
                .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://" + kafkaProperties.getAdvertisedListeners())
                .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT")
                .withReuse(true);
    }


    @Override
    public Map<String, Object> getSpringProperties(KafkaContainer container, Environment environment) {
        return Map.of("spring.kafka.bootstrap-servers", container.getBootstrapServers());
    }
}
