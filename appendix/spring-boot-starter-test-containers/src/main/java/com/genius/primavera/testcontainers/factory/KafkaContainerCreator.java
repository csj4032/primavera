package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.KafkaContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
public class KafkaContainerCreator implements ContainerCreator {

    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        log.info("Received spec type: {}", spec.getClass().getSimpleName());
        
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.KAFKA.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;

        KafkaContainer container = new KafkaContainer(DockerImageName.parse(image))
                .withStartupTimeout(Duration.ofSeconds(timeout));

        if (spec instanceof KafkaContainerSpec kafkaSpec) {
            log.info("Using KafkaContainerSpec");
            
            log.info("KafkaContainerSpec details:");
            log.info("  - kafkaVersion: {}", kafkaSpec.getKafkaVersion());
            log.info("  - brokers: {}", kafkaSpec.getBrokers());
            log.info("  - partitions: {}", kafkaSpec.getPartitions());
            log.info("  - replicationFactor: {}", kafkaSpec.getReplicationFactor());
            log.info("  - enableJmx: {}", kafkaSpec.getEnableJmx());
            
            // Kafka specific configurations
            if (kafkaSpec.getEnableJmx()) {
                container.withEnv("KAFKA_JMX_ENABLED", "true");
                container.withEnv("KAFKA_JMX_PORT", "9999");
            }
            
            container.withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");
            container.withEnv("KAFKA_NUM_PARTITIONS", kafkaSpec.getPartitions().toString());
            container.withEnv("KAFKA_DEFAULT_REPLICATION_FACTOR", kafkaSpec.getReplicationFactor().toString());
            
            if (kafkaSpec.getRetentionMs() != null) {
                container.withEnv("KAFKA_LOG_RETENTION_MS", kafkaSpec.getRetentionMs().toString());
            }
            
            if (kafkaSpec.getSegmentBytes() != null) {
                container.withEnv("KAFKA_LOG_SEGMENT_BYTES", kafkaSpec.getSegmentBytes().toString());
            }
            
            if (kafkaSpec.getCompressionType() != null) {
                container.withEnv("KAFKA_COMPRESSION_TYPE", kafkaSpec.getCompressionType().name().toLowerCase());
            }
            
            if (kafkaSpec.getCleanupPolicy() != null) {
                container.withEnv("KAFKA_LOG_CLEANUP_POLICY", kafkaSpec.getCleanupPolicy().name().toLowerCase());
            }
            
        } else {
            log.info("Using default Kafka configuration - spec type: {}", spec.getClass().getSimpleName());
        }

        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }

        return container;
    }

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.KAFKA;
    }
}