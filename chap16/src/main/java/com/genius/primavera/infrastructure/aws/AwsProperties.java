package com.genius.primavera.infrastructure.aws;

import org.springframework.boot.context.properties.bind.Name;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.aws")
public record AwsProperties(Credentials credentials, Region region, S3 s3) {

    public record Credentials(String accessKey, String secretKey) {
    }

    public record Region(@Name("static") String value) {
    }

    public record S3(String bucketName, String endpoint) {
        public boolean hasEndpoint() {
            return endpoint != null && !endpoint.isEmpty();
        }

        public boolean isLocalStack() {
            return hasEndpoint() && endpoint.contains("localstack");
        }
    }

    public boolean isLocalStack() {
        return "test".equals(credentials.accessKey()) || s3.isLocalStack();
    }

    public boolean isRealAws() {
        return !isLocalStack();
    }
}