package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Validated
@ConfigurationProperties
@EqualsAndHashCode(callSuper = true)
public class LocalStackContainerSpec extends BaseContainerSpec {

    @NotNull(message = "Services cannot be null")
    private Set<AwsService> services = new HashSet<>(Set.of(AwsService.S3));

    private Boolean debugMode = false;

    private String dataDirectory;

    private LambdaExecutor lambdaExecutor = LambdaExecutor.DOCKER;

    private String dockerNetworkMode;

    private String externalHostName;

    @Min(value = 1024, message = "Edge port must be at least 1024")
    @Max(value = 65535, message = "Edge port must not exceed 65535")
    private Integer edgePort = 4566;

    private Boolean useLegacyPorts = false;

    public enum AwsService {
        S3,
        DYNAMODB,
        SQS,
        SNS,
        LAMBDA,
        KINESIS,
        CLOUDWATCH,
        EC2,
        SSM,
        APIGATEWAY,
        SES,
        SECRETSMANAGER,
        IAM,
        STEPFUNCTIONS,
        CLOUDFORMATION
    }

    public enum LambdaExecutor {
        LOCAL,
        DOCKER
    }

    public void addService(AwsService service) {
        if (this.services == null) {
            this.services = new HashSet<>();
        }
        this.services.add(service);
    }

    public void addServices(AwsService... services) {
        if (this.services == null) {
            this.services = new HashSet<>();
        }
        for (AwsService service : services) {
            this.services.add(service);
        }
    }

    public void removeService(AwsService service) {
        if (this.services != null) {
            this.services.remove(service);
        }
    }

    public boolean hasService(AwsService service) {
        return this.services != null && this.services.contains(service);
    }

    public boolean isS3Enabled() {
        return hasService(AwsService.S3);
    }

    public boolean isDynamoDbEnabled() {
        return hasService(AwsService.DYNAMODB);
    }

    public boolean isSqsEnabled() {
        return hasService(AwsService.SQS);
    }

    public boolean isSnsEnabled() {
        return hasService(AwsService.SNS);
    }

    public boolean isLambdaEnabled() {
        return hasService(AwsService.LAMBDA);
    }
}