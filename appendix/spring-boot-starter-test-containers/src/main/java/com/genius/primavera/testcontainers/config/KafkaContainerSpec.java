package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@ConfigurationProperties
@EqualsAndHashCode(callSuper = true)
public class KafkaContainerSpec extends BaseContainerSpec {

    @NotBlank(message = "Kafka version cannot be blank")
    private String kafkaVersion = "7.0.1";

    @NotBlank(message = "Confluent platform version cannot be blank")
    private String confluentPlatformVersion = "7.0.1";

    @Min(value = 1, message = "Number of brokers must be at least 1")
    @Max(value = 10, message = "Number of brokers must not exceed 10")
    private Integer brokers = 1;

    @Min(value = 1, message = "Number of partitions must be at least 1")
    @Max(value = 100, message = "Number of partitions must not exceed 100")
    private Integer partitions = 3;

    @Min(value = 1, message = "Replication factor must be at least 1")
    @Max(value = 10, message = "Replication factor must not exceed 10")
    private Short replicationFactor = 1;

    private Boolean enableJmx = false;

    private Boolean enableSchemaRegistry = false;

    private Boolean enableConnect = false;

    private Boolean enableKsql = false;

    @Min(value = 1000, message = "Retention time must be at least 1000ms")
    private Long retentionMs = 604800000L; // 7 days

    @Min(value = 1024, message = "Segment size must be at least 1KB")
    private Long segmentBytes = 1073741824L; // 1GB

    private CompressionType compressionType = CompressionType.PRODUCER;

    private CleanupPolicy cleanupPolicy = CleanupPolicy.DELETE;

    @NotNull
    private List<@NotBlank String> topics = new ArrayList<>();

    public enum CompressionType {
        UNCOMPRESSED,
        SNAPPY,
        LZ4,
        GZIP,
        PRODUCER
    }

    public enum CleanupPolicy {
        DELETE,
        COMPACT,
        DELETE_COMPACT
    }
}