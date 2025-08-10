package com.genius.primavera.testcontainers.bean.aws;

import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.net.URI;

@Slf4j
public class DynamoDbClientFactory extends AwsServiceClientFactory {

    private static final String DYNAMODB_CLIENT_CLASS = "software.amazon.awssdk.services.dynamodb.DynamoDbClient";
    private static final String AWS_CREDENTIALS_CLASS = "software.amazon.awssdk.auth.credentials.AwsBasicCredentials";
    private static final String AWS_CREDENTIALS_PROVIDER_CLASS = "software.amazon.awssdk.auth.credentials.StaticCredentialsProvider";

    @Override
    public Object createClient(LocalStackContainer container) {
        if (!isAvailable()) throw new IllegalStateException("AWS DynamoDB SDK is not available in classpath. AWS SDK v2  dependency needs to be added.");
        try {
            Class<?> dynamoDbClientClass = Class.forName(DYNAMODB_CLIENT_CLASS);
            Class<?> awsCredentialsClass = Class.forName(AWS_CREDENTIALS_CLASS);
            Class<?> credentialsProviderClass = Class.forName(AWS_CREDENTIALS_PROVIDER_CLASS);
            Object credentials = awsCredentialsClass.getMethod("create", String.class, String.class).invoke(null, getAccessKey(container), getSecretKey(container));
            Class<?> awsCredentialsInterface = Class.forName("software.amazon.awssdk.auth.credentials.AwsCredentials");
            Object credentialsProvider = credentialsProviderClass.getMethod("create", awsCredentialsInterface).invoke(null, credentials);
            Object builder = dynamoDbClientClass.getMethod("builder").invoke(null);
            String endpointUrl = getEndpointUrl(container, LocalStackContainer.Service.DYNAMODB);
            builder.getClass().getMethod("endpointOverride", URI.class).invoke(builder, URI.create(endpointUrl));
            Class<?> awsCredentialsProviderInterface = Class.forName("software.amazon.awssdk.auth.credentials.AwsCredentialsProvider");
            builder.getClass().getMethod("credentialsProvider", awsCredentialsProviderInterface).invoke(builder, credentialsProvider);
            Class<?> regionClass = Class.forName("software.amazon.awssdk.regions.Region");
            Object region = regionClass.getMethod("of", String.class).invoke(null, getRegion(container));
            builder.getClass().getMethod("region", regionClass).invoke(builder, region);
            Object dynamoDbClient = builder.getClass().getMethod("build").invoke(builder);
            log.info(" DynamoDbClient created successfully. Endpoint: {}", endpointUrl);
            return dynamoDbClient;
        } catch (Exception e) {
            log.error("DynamoDbClient creation failed with error", e);
            throw new RuntimeException("DynamoDbClient creation failure", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return areClassesAvailable(DYNAMODB_CLIENT_CLASS, AWS_CREDENTIALS_CLASS, AWS_CREDENTIALS_PROVIDER_CLASS, "software.amazon.awssdk.regions.Region");
    }

    @Override
    public LocalStackContainerSpec.AwsService getSupportedService() {
        return LocalStackContainerSpec.AwsService.DYNAMODB;
    }

    @Override
    public String getBeanName() {
        return "dynamoDbClient";
    }

    @Override
    public boolean isPrimary() {
        return true;
    }
}