package com.genius.primavera.testContainer.config;

import org.springframework.core.env.Environment;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class KafkaContainerConfig implements ContainerConfig<KafkaContainer> {

    private static final String IMAGE_KEY = "primavera.testcontainers.kafka.image";
    private static final String AUTO_CREATE_TOPICS_ENABLE_KEY = "primavera.testcontainers.kafka.auto-create-topics-enable";
    private static final String ZOOKEEPER_CONNECT_KEY = "primavera.testcontainers.kafka.zookeeper-connect";
    private static final String ADVERTISED_LISTENERS_KEY = "primavera.testcontainers.kafka.advertised-listeners"; // This is typically dynamically set by Testcontainers

    // Default values for Kafka
    private static final String DEFAULT_IMAGE = "confluentinc/cp-kafka:7.4.0"; // A specific version is better than 'latest' for stability
    private static final String DEFAULT_AUTO_CREATE_TOPICS_ENABLE = "true";

    public KafkaContainerConfig() {
    }

    @Override
    public String getImageName() {
        return "confluentinc/cp-kafka:latest";
    }

    @Override
    public KafkaContainer createContainer(Environment environment) {
        String image = environment.getProperty(IMAGE_KEY, DEFAULT_IMAGE);
        String autoCreateTopicsEnable = environment.getProperty(AUTO_CREATE_TOPICS_ENABLE_KEY, DEFAULT_AUTO_CREATE_TOPICS_ENABLE);
        String zookeeperConnect = environment.getProperty(ZOOKEEPER_CONNECT_KEY); // No default, rely on Testcontainers or app default
        String advertisedListeners = environment.getProperty(ADVERTISED_LISTENERS_KEY); // No default
        KafkaContainer container = new KafkaContainer(DockerImageName.parse(image))
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", autoCreateTopicsEnable)
                .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT")
                .withReuse(true);

        return container;
    }

    @Override
    public Map<String, Object> getSpringProperties(KafkaContainer container, Environment environment) {
        return Map.of("spring.kafka.bootstrap-servers", container.getBootstrapServers());
    }
}
