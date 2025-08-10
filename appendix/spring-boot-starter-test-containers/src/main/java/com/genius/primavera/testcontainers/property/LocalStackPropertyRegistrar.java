package com.genius.primavera.testcontainers.property;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.localstack.LocalStackContainer;

@Slf4j
public class LocalStackPropertyRegistrar {
    
    private static final String DEFAULT_CONTAINER_NAME = "localstack";
    
    public static void registerEndpoints(DynamicPropertyRegistry registry) {
        registerEndpoints(registry, DEFAULT_CONTAINER_NAME);
    }
    
    public static void registerEndpoints(DynamicPropertyRegistry registry, String containerName) {
        var containerManager = ContainerRegistry.get();
        var localStackInfo = containerManager.getContainer(containerName);
        
        if (localStackInfo == null) {
            log.warn("LocalStack 컨테이너를 찾을 수 없습니다: {}", containerName);
            return;
        }
        
        if (!(localStackInfo.container() instanceof LocalStackContainer)) {
            log.warn("컨테이너가 LocalStackContainer 타입이 아닙니다: {}", containerName);
            return;
        }
        
        var container = (LocalStackContainer) localStackInfo.container();
        
        registerAwsCloudProperties(registry, container);
        
        registerCustomProperties(registry, container);
        
        log.info("✅ LocalStack 엔드포인트 프로퍼티가 등록되었습니다: {}", container.getEndpoint());
    }
    
    private static void registerAwsCloudProperties(DynamicPropertyRegistry registry, LocalStackContainer container) {
        String endpoint = container.getEndpoint().toString();
        
        registry.add("spring.cloud.aws.s3.endpoint", () -> endpoint);
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> endpoint);
        registry.add("spring.cloud.aws.sqs.endpoint", () -> endpoint);
        registry.add("spring.cloud.aws.sns.endpoint", () -> endpoint);
        
        registry.add("spring.cloud.aws.credentials.access-key", container::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", container::getSecretKey);
        registry.add("spring.cloud.aws.region.static", container::getRegion);
        
        registry.add("spring.cloud.aws.s3.path-style-access", () -> "true");
    }
    
    private static void registerCustomProperties(DynamicPropertyRegistry registry, LocalStackContainer container) {
        registry.add("aws.s3.localstack-endpoint", container::getEndpoint);
        registry.add("aws.s3.endpoint", container::getEndpoint);
        registry.add("aws.credentials.access-key", container::getAccessKey);
        registry.add("aws.credentials.secret-key", container::getSecretKey);
        registry.add("aws.region", container::getRegion);
    }
    
    public static LocalStackContainer getContainer() {
        return getContainer(DEFAULT_CONTAINER_NAME);
    }
    
    public static LocalStackContainer getContainer(String containerName) {
        var containerManager = ContainerRegistry.get();
        var localStackInfo = containerManager.getContainer(containerName);
        
        if (localStackInfo == null) {
            throw new IllegalStateException("LocalStack 컨테이너를 찾을 수 없습니다: " + containerName);
        }
        
        if (!(localStackInfo.container() instanceof LocalStackContainer)) {
            throw new IllegalStateException("컨테이너가 LocalStackContainer 타입이 아닙니다: " + containerName);
        }
        
        return (LocalStackContainer) localStackInfo.container();
    }
}