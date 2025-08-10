package com.genius.primavera.testcontainers.bean.aws;

import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.net.URI;

@Slf4j
public class SnsClientFactory extends AwsServiceClientFactory {

    private static final String SNS_CLIENT_CLASS = "software.amazon.awssdk.services.sns.SnsClient";
    private static final String AWS_CREDENTIALS_CLASS = "software.amazon.awssdk.auth.credentials.AwsBasicCredentials";
    private static final String AWS_CREDENTIALS_PROVIDER_CLASS = "software.amazon.awssdk.auth.credentials.StaticCredentialsProvider";

    @Override
    public Object createClient(LocalStackContainer container) {
        if (!isAvailable()) {
            throw new IllegalStateException("AWS SNS SDKtranslated_text_1 translated_text_6 translated_text_4. AWS SDK v2 dependencytranslated_text_1 translated_text_1.");
        }

        try {
            Class<?> snsClientClass = Class.forName(SNS_CLIENT_CLASS);
            Class<?> awsCredentialsClass = Class.forName(AWS_CREDENTIALS_CLASS);
            Class<?> credentialsProviderClass = Class.forName(AWS_CREDENTIALS_PROVIDER_CLASS);

            Object credentials = awsCredentialsClass.getMethod("create", String.class, String.class)
                    .invoke(null, getAccessKey(container), getSecretKey(container));

            Class<?> awsCredentialsInterface = Class.forName("software.amazon.awssdk.auth.credentials.AwsCredentials");
            Object credentialsProvider = credentialsProviderClass.getMethod("create", awsCredentialsInterface)
                    .invoke(null, credentials);

            Object builder = snsClientClass.getMethod("builder").invoke(null);

            String endpointUrl = getEndpointUrl(container, LocalStackContainer.Service.SNS);
            builder.getClass().getMethod("endpointOverride", URI.class)
                    .invoke(builder, URI.create(endpointUrl));

            Class<?> awsCredentialsProviderInterface = Class.forName("software.amazon.awssdk.auth.credentials.AwsCredentialsProvider");
            builder.getClass().getMethod("credentialsProvider", awsCredentialsProviderInterface)
                    .invoke(builder, credentialsProvider);

            Class<?> regionClass = Class.forName("software.amazon.awssdk.regions.Region");
            Object region = regionClass.getMethod("of", String.class).invoke(null, getRegion(container));
            builder.getClass().getMethod("region", regionClass).invoke(builder, region);

            Object snsClient = builder.getClass().getMethod("build").invoke(builder);

            log.info(" SnsClienttranslated_text_1 translated_text_10 translated_text_13. translated_text_5: {}", endpointUrl);
            return snsClient;

        } catch (Exception e) {
            log.error("SnsClient creation translated_text_1 error translated_text_2", e);
            throw new RuntimeException("SnsClient creation failure", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return areClassesAvailable(
                SNS_CLIENT_CLASS,
                AWS_CREDENTIALS_CLASS,
                AWS_CREDENTIALS_PROVIDER_CLASS,
                "software.amazon.awssdk.regions.Region"
        );
    }

    @Override
    public LocalStackContainerSpec.AwsService getSupportedService() {
        return LocalStackContainerSpec.AwsService.SNS;
    }

    @Override
    public String getBeanName() {
        return "snsClient";
    }

    @Override
    public boolean isPrimary() {
        return true;
    }
}