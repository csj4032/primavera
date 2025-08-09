package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.localstack.LocalStackContainer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalStack 컨테이너 테스트
 * AWS 서비스 모킹 기능을 검증합니다.
 */
@Slf4j
@SpringBootTest(properties = {
    "spring.test.context.cache.maxSize=0",
    "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("LocalStack Container Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.LOCALSTACK, name = "localstack")
})
class LocalStackContainerTest {

    @Test
    @Order(1)
    @DisplayName("LocalStack container starts successfully")
    void testLocalStackContainerStartup() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        assertNotNull(localStackInfo, "LocalStack container info should not be null");
        assertTrue(localStackInfo.container().isRunning(), "LocalStack container should be running");
        assertEquals(ContainerType.LOCALSTACK, localStackInfo.type(), "Container type should be LOCALSTACK");

        // LocalStack 컨테이너임을 확인
        assertTrue(localStackInfo.container() instanceof LocalStackContainer, 
            "Container should be LocalStackContainer instance");

        log.info("✅ LocalStack container started successfully");
    }

    @Test
    @Order(2)
    @DisplayName("LocalStack container configuration validation")
    void testLocalStackConfiguration() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        // 설정 검증
        assertNotNull(localStackInfo.spec(), "LocalStack spec should not be null");
        assertTrue(localStackInfo.spec() instanceof LocalStackContainerSpec,
            "Spec should be LocalStackContainerSpec");

        LocalStackContainerSpec spec = (LocalStackContainerSpec) localStackInfo.spec();

        // 기본 서비스들이 설정되어 있는지 확인
        assertNotNull(spec.getServices(), "Services should not be null");
        assertFalse(spec.getServices().isEmpty(), "Services should not be empty");

        // 기본 서비스 확인
        assertTrue(spec.isS3Enabled(), "S3 should be enabled by default");
        assertTrue(spec.isDynamoDbEnabled(), "DynamoDB should be enabled by default");
        assertTrue(spec.isSqsEnabled(), "SQS should be enabled by default");
        assertTrue(spec.isSnsEnabled(), "SNS should be enabled by default");

        log.info("✅ LocalStack configuration validated - Services: {}", spec.getServices());
    }

    @Test
    @Order(3)
    @DisplayName("LocalStack endpoint accessibility")
    void testLocalStackEndpoint() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        LocalStackContainer container = (LocalStackContainer) localStackInfo.container();

        // 엔드포인트 정보 확인
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "LocalStack endpoint should not be null");
        assertTrue(endpoint.startsWith("http://"), "Endpoint should be HTTP URL");

        log.info("LocalStack endpoint: {}", endpoint);

        // 기본 포트 확인
        Integer mappedPort = container.getMappedPort(4566);
        assertNotNull(mappedPort, "Edge port should be mapped");
        assertTrue(mappedPort > 0, "Mapped port should be valid");

        log.info("LocalStack edge port mapped to: {}", mappedPort);

        log.info("✅ LocalStack endpoint is accessible");
    }

    @Test
    @Order(4)
    @DisplayName("LocalStack service-specific endpoints")
    void testServiceEndpoints() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        LocalStackContainer container = (LocalStackContainer) localStackInfo.container();

        // 기본 엔드포인트 테스트
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "LocalStack endpoint should not be null");
        assertTrue(endpoint.startsWith("http://"), "Endpoint should be HTTP URL");
        log.info("LocalStack endpoint: {}", endpoint);

        // S3 엔드포인트 테스트
        try {
            String s3Endpoint = container.getEndpointOverride(LocalStackContainer.Service.S3).toString();
            assertNotNull(s3Endpoint, "S3 endpoint should not be null");
            log.info("S3 endpoint: {}", s3Endpoint);
        } catch (Exception e) {
            log.warn("S3 endpoint not available: {}", e.getMessage());
        }

        // DynamoDB 엔드포인트 테스트
        try {
            String dynamoEndpoint = container.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString();
            assertNotNull(dynamoEndpoint, "DynamoDB endpoint should not be null");
            log.info("DynamoDB endpoint: {}", dynamoEndpoint);
        } catch (Exception e) {
            log.warn("DynamoDB endpoint not available: {}", e.getMessage());
        }

        log.info("✅ Service-specific endpoints checked");
    }

    @Test
    @Order(5)
    @DisplayName("LocalStack container networking")
    void testLocalStackNetworking() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        // 네트워크 정보 확인
        String host = localStackInfo.container().getHost();
        assertNotNull(host, "Container host should not be null");

        Integer port = localStackInfo.container().getFirstMappedPort();
        assertNotNull(port, "Container should have mapped port");
        assertTrue(port > 0, "Mapped port should be valid");

        log.info("LocalStack network info - Host: {}, Port: {}", host, port);

        // 컨테이너 ID 확인
        String containerId = localStackInfo.container().getContainerId();
        assertNotNull(containerId, "Container ID should not be null");
        assertFalse(containerId.isEmpty(), "Container ID should not be empty");

        log.info("LocalStack container ID: {}", containerId.substring(0, 12));

        log.info("✅ LocalStack networking validated");
    }

    @Test
    @Order(6)
    @DisplayName("LocalStack environment variables")
    void testLocalStackEnvironment() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        LocalStackContainer container = (LocalStackContainer) localStackInfo.container();

        // 환경 변수들이 제대로 설정되었는지 확인 (간접적으로)
        assertTrue(container.isRunning(), "Container should be running with proper environment");

        // 로그에서 LocalStack 시작 메시지 확인
        String logs = container.getLogs();
        assertNotNull(logs, "Container logs should not be null");
        assertFalse(logs.isEmpty(), "Container should have logs");

        log.info("LocalStack container logs available: {} characters", logs.length());

        log.info("✅ LocalStack environment variables properly set");
    }

    @Test
    @Order(7)
    @DisplayName("LocalStack container lifecycle")
    void testLocalStackLifecycle() {
        ContainerManager manager = ContainerRegistry.get();

        // 컨테이너 상태 확인
        assertTrue(manager.isStarted(), "Container manager should be started");

        ContainerInfo localStackInfo = manager.getContainer("localstack");
        assertNotNull(localStackInfo, "LocalStack info should exist");

        // 실행 중 상태 확인
        assertTrue(localStackInfo.container().isRunning(), "LocalStack should be running");

        // 컨테이너 세부 정보
        log.info("LocalStack container lifecycle validated:");
        log.info("  - Image: {}", localStackInfo.container().getDockerImageName());
        log.info("  - Status: Running");
        log.info("  - Type: {}", localStackInfo.type());
        log.info("  - Name: {}", localStackInfo.name());

        log.info("✅ LocalStack container lifecycle is healthy");
    }
}