package com.genius.primavera.testcontainers.bean.aws;

import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.net.URI;

@Slf4j
public class S3ClientFactory extends AwsServiceClientFactory {

    private static final String S3_CLIENT_CLASS = "software.amazon.awssdk.services.s3.S3Client";
    private static final String AWS_CREDENTIALS_CLASS = "software.amazon.awssdk.auth.credentials.AwsBasicCredentials";
    private static final String AWS_CREDENTIALS_PROVIDER_CLASS = "software.amazon.awssdk.auth.credentials.StaticCredentialsProvider";

    @Override
    public Object createClient(LocalStackContainer container) {
        if (!isAvailable()) {
            throw new IllegalStateException("AWS S3 SDK is not available in classpath. AWS SDK v2  dependency needs to be added.");
        }

        try {
            Class<?> s3ClientClass = Class.forName(S3_CLIENT_CLASS);
            Class<?> awsCredentialsClass = Class.forName(AWS_CREDENTIALS_CLASS);
            Class<?> credentialsProviderClass = Class.forName(AWS_CREDENTIALS_PROVIDER_CLASS);

            Object credentials = awsCredentialsClass.getMethod("create", String.class, String.class)
                    .invoke(null, getAccessKey(container), getSecretKey(container));

            Class<?> awsCredentialsInterface = Class.forName("software.amazon.awssdk.auth.credentials.AwsCredentials");
            Object credentialsProvider = credentialsProviderClass.getMethod("create", awsCredentialsInterface)
                    .invoke(null, credentials);

            Object builder = s3ClientClass.getMethod("builder").invoke(null);

            String endpointUrl = getEndpointUrl(container, LocalStackContainer.Service.S3);
            builder.getClass().getMethod("endpointOverride", URI.class)
                    .invoke(builder, URI.create(endpointUrl));

            Class<?> awsCredentialsProviderInterface = Class.forName("software.amazon.awssdk.auth.credentials.AwsCredentialsProvider");
            builder.getClass().getMethod("credentialsProvider", awsCredentialsProviderInterface)
                    .invoke(builder, credentialsProvider);

            Class<?> regionClass = Class.forName("software.amazon.awssdk.regions.Region");
            Object region = regionClass.getMethod("of", String.class).invoke(null, getRegion(container));
            builder.getClass().getMethod("region", regionClass).invoke(builder, region);

            Class<?> s3ConfigClass = Class.forName("software.amazon.awssdk.services.s3.S3Configuration");
            Object s3ConfigBuilder = s3ConfigClass.getMethod("builder").invoke(null);
            
            java.lang.reflect.Method pathStyleMethod = s3ConfigBuilder.getClass().getMethod("pathStyleAccessEnabled", Boolean.class);
            pathStyleMethod.setAccessible(true);
            pathStyleMethod.invoke(s3ConfigBuilder, Boolean.TRUE);
            
            java.lang.reflect.Method buildMethod = s3ConfigBuilder.getClass().getMethod("build");
            buildMethod.setAccessible(true);
            Object builtS3Config = buildMethod.invoke(s3ConfigBuilder);
            
            java.lang.reflect.Method serviceConfigMethod = builder.getClass().getMethod("serviceConfiguration", s3ConfigClass);
            serviceConfigMethod.setAccessible(true);
            serviceConfigMethod.invoke(builder, builtS3Config);

            Object s3Client = builder.getClass().getMethod("build").invoke(builder);

            log.info(" S3Client created successfully. Endpoint: {}", endpointUrl);
            return s3Client;

        } catch (Exception e) {
            log.error("S3Client creation failed with error", e);
            throw new RuntimeException("S3Client creation failure", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return areClassesAvailable(
                S3_CLIENT_CLASS,
                AWS_CREDENTIALS_CLASS,
                AWS_CREDENTIALS_PROVIDER_CLASS,
                "software.amazon.awssdk.regions.Region",
                "software.amazon.awssdk.services.s3.S3Configuration"
        );
    }

    @Override
    public LocalStackContainerSpec.AwsService getSupportedService() {
        return LocalStackContainerSpec.AwsService.S3;
    }

    @Override
    public String getBeanName() {
        return "s3Client";
    }

    @Override
    public boolean isPrimary() {
        return true;
    }
}