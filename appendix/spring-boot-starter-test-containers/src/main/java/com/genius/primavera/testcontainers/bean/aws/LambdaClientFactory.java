package com.genius.primavera.testcontainers.bean.aws;

import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.net.URI;

@Slf4j
public class LambdaClientFactory extends AwsServiceClientFactory {

    private static final String LAMBDA_CLIENT_CLASS = "software.amazon.awssdk.services.lambda.LambdaClient";
    private static final String AWS_CREDENTIALS_CLASS = "software.amazon.awssdk.auth.credentials.AwsBasicCredentials";
    private static final String AWS_CREDENTIALS_PROVIDER_CLASS = "software.amazon.awssdk.auth.credentials.StaticCredentialsProvider";

    @Override
    public Object createClient(LocalStackContainer container) {
        if (!isAvailable()) {
            throw new IllegalStateException("AWS Lambda SDK가 클래스패스에 없습니다. AWS SDK v2 의존성을 추가해주세요.");
        }

        try {
            Class<?> lambdaClientClass = Class.forName(LAMBDA_CLIENT_CLASS);
            Class<?> awsCredentialsClass = Class.forName(AWS_CREDENTIALS_CLASS);
            Class<?> credentialsProviderClass = Class.forName(AWS_CREDENTIALS_PROVIDER_CLASS);

            Object credentials = awsCredentialsClass.getMethod("create", String.class, String.class)
                    .invoke(null, getAccessKey(container), getSecretKey(container));

            Class<?> awsCredentialsInterface = Class.forName("software.amazon.awssdk.auth.credentials.AwsCredentials");
            Object credentialsProvider = credentialsProviderClass.getMethod("create", awsCredentialsInterface)
                    .invoke(null, credentials);

            Object builder = lambdaClientClass.getMethod("builder").invoke(null);

            String endpointUrl = getEndpointUrl(container, LocalStackContainer.Service.LAMBDA);
            builder.getClass().getMethod("endpointOverride", URI.class)
                    .invoke(builder, URI.create(endpointUrl));

            Class<?> awsCredentialsProviderInterface = Class.forName("software.amazon.awssdk.auth.credentials.AwsCredentialsProvider");
            builder.getClass().getMethod("credentialsProvider", awsCredentialsProviderInterface)
                    .invoke(builder, credentialsProvider);

            Class<?> regionClass = Class.forName("software.amazon.awssdk.regions.Region");
            Object region = regionClass.getMethod("of", String.class).invoke(null, getRegion(container));
            builder.getClass().getMethod("region", regionClass).invoke(builder, region);

            Object lambdaClient = builder.getClass().getMethod("build").invoke(builder);

            log.info("✅ LambdaClient가 성공적으로 생성되었습니다. 엔드포인트: {}", endpointUrl);
            return lambdaClient;

        } catch (Exception e) {
            log.error("LambdaClient 생성 중 오류 발생", e);
            throw new RuntimeException("LambdaClient 생성 실패", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return areClassesAvailable(
                LAMBDA_CLIENT_CLASS,
                AWS_CREDENTIALS_CLASS,
                AWS_CREDENTIALS_PROVIDER_CLASS,
                "software.amazon.awssdk.regions.Region"
        );
    }

    @Override
    public LocalStackContainerSpec.AwsService getSupportedService() {
        return LocalStackContainerSpec.AwsService.LAMBDA;
    }

    @Override
    public String getBeanName() {
        return "lambdaClient";
    }

    @Override
    public boolean isPrimary() {
        return true; // Lambda는 서버리스 컴퓨팅으로 자주 사용되므로 Primary로 설정
    }
}