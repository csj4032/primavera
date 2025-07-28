package com.genius.primavera.testContainer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Kafka TestContainer Mixin
 * 
 * 사용법:
 * @SpringBootTest
 * public class MyTest implements KafkaTestcontainerMixin {
 *     @Autowired
 *     private KafkaTemplate kafkaTemplate; // 자동으로 Kafka 컨테이너에 연결됨
 * }
 */
@Testcontainers
public interface KafkaTestcontainerMixin {
    
    @Container
    ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @DynamicPropertySource
    static void configureKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    default String getKafkaBootstrapServers() {
        return kafka.getBootstrapServers();
    }
}