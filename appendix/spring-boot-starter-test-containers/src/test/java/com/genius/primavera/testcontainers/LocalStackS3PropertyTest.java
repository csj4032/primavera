package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.property.LocalStackPropertyRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.testcontainers.containers.localstack.LocalStackContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(properties = {
    "spring.test.context.cache.maxSize=0",
    "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("LocalStack S3 Property Registration Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.LOCALSTACK, name = "localstack")
})
class LocalStackS3PropertyTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        LocalStackPropertyRegistrar.registerEndpoints(registry);
    }

    @Test
    @Order(1)
    @DisplayName("LocalStack 컨테이너 및 프로퍼티 등록 확인")
    void testLocalStackPropertiesRegistration() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        assertNotNull(localStackInfo, "LocalStack 컨테이너 정보가 존재해야 합니다");
        assertTrue(localStackInfo.container().isRunning(), "LocalStack 컨테이너가 실행 중이어야 합니다");
        
        LocalStackContainer container = (LocalStackContainer) localStackInfo.container();
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "엔드포인트가 존재해야 합니다");
        assertTrue(endpoint.startsWith("http://"), "HTTP 엔드포인트여야 합니다");
        
        log.info("✅ LocalStack 엔드포인트: {}", endpoint);
        log.info("✅ Access Key: {}", container.getAccessKey());
        log.info("✅ Secret Key: {}", container.getSecretKey());
        log.info("✅ Region: {}", container.getRegion());
    }

    @Test
    @Order(2)
    @DisplayName("LocalStack 컨테이너 정보 가져오기 테스트")
    void testGetLocalStackContainer() {
        LocalStackContainer container = assertDoesNotThrow(() -> {
            return LocalStackPropertyRegistrar.getContainer("localstack");
        }, "LocalStack 컨테이너 정보를 가져오는 데 성공해야 합니다");
        
        assertNotNull(container, "컨테이너가 null이 아니어야 합니다");
        assertTrue(container.isRunning(), "컨테이너가 실행 중이어야 합니다");
        
        log.info("✅ LocalStack 컨테이너 정보 가져오기 성공");
        log.info("  - 엔드포인트: {}", container.getEndpoint());
        log.info("  - Access Key: {}", container.getAccessKey());
        log.info("  - Secret Key: {}", container.getSecretKey());
        log.info("  - Region: {}", container.getRegion());
    }

    @Test
    @Order(3)
    @DisplayName("LocalStack 서비스 엔드포인트 접근 테스트")
    void testLocalStackServiceAccess() {
        LocalStackContainer container = LocalStackPropertyRegistrar.getContainer("localstack");
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "기본 엔드포인트가 존재해야 합니다");
        assertTrue(endpoint.startsWith("http://"), "HTTP 엔드포인트여야 합니다");
        
        try {
            String s3Endpoint = container.getEndpointOverride(LocalStackContainer.Service.S3).toString();
            assertNotNull(s3Endpoint, "S3 엔드포인트가 존재해야 합니다");
            log.info("✅ S3 엔드포인트: {}", s3Endpoint);
        } catch (Exception e) {
            log.warn("S3 엔드포인트 비활성화: {}", e.getMessage());
        }
        
        try {
            String dynamoEndpoint = container.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString();
            assertNotNull(dynamoEndpoint, "DynamoDB 엔드포인트가 존재해야 합니다");
            log.info("✅ DynamoDB 엔드포인트: {}", dynamoEndpoint);
        } catch (Exception e) {
            log.warn("DynamoDB 엔드포인트 비활성화: {}", e.getMessage());
        }
        
        log.info("✅ LocalStack 서비스 엔드포인트 접근 테스트 완료");
    }

    @Test
    @Order(4)
    @DisplayName("LocalStack 프로퍼티 등록 확인")
    void testLocalStackPropertyRegistration() {
        LocalStackContainer container = LocalStackPropertyRegistrar.getContainer("localstack");
        
        assertNotNull(container.getEndpoint(), "엔드포인트가 존재해야 합니다");
        assertNotNull(container.getAccessKey(), "Access Key가 존재해야 합니다");
        assertNotNull(container.getSecretKey(), "Secret Key가 존재해야 합니다");
        assertNotNull(container.getRegion(), "Region이 존재해야 합니다");
        
        log.info("✅ LocalStack 컨테이너 정보 확인 완료");
        log.info("  - 엔드포인트: {}", container.getEndpoint());
        log.info("  - Access Key: {}", container.getAccessKey());
        log.info("  - Secret Key: [****]");
        log.info("  - Region: {}", container.getRegion());
        
        LocalStackContainer defaultContainer = LocalStackPropertyRegistrar.getContainer();
        assertEquals(container.getContainerId(), defaultContainer.getContainerId(), 
                "기본 컨테이너와 명시적 컨테이너가 동일해야 합니다");
        
        log.info("✅ LocalStack 프로퍼티 등록 기능 테스트 완료");
    }
}