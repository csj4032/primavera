package com.genius.primavera.infrastructure.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
public record S3Properties(
    String bucketName,
    String endpoint,
    boolean pathStyleAccess
) {
    public S3Properties(String bucketName, String endpoint, boolean pathStyleAccess) {
        this.bucketName = bucketName;
        this.endpoint = endpoint;
        this.pathStyleAccess = pathStyleAccess;
    }
}