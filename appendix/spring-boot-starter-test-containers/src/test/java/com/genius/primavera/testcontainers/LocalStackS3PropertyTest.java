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
    @DisplayName("LocalStack file should file registration verification")
    void testLocalStackPropertiesRegistration() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        assertNotNull(localStackInfo, "LocalStack file operation file connection");
        assertTrue(localStackInfo.container().isRunning(), "LocalStack fileshould execution file connection");
        
        LocalStackContainer container = (LocalStackContainer) localStackInfo.container();
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "with file connection");
        assertTrue(endpoint.startsWith("http://"), "HTTP logging connection");
        
        log.info(" LocalStack Endpoint: {}", endpoint);
        log.info(" Access Key: {}", container.getAccessKey());
        log.info(" Secret Key: {}", container.getSecretKey());
        log.info(" Region: {}", container.getRegion());
    }

    @Test
    @Order(2)
    @DisplayName("LocalStack file information file test")
    void testGetLocalStackContainer() {
        LocalStackContainer container = assertDoesNotThrow(() -> {
            return LocalStackPropertyRegistrar.getContainer("localstack");
        }, "LocalStack file information file should should not connection");
        
        assertNotNull(container, "fileshould nullshould file connection");
        assertTrue(container.isRunning(), "fileshould execution file connection");
        
        log.info(" LocalStack file information file success");
        log.info("  - Endpoint: {}", container.getEndpoint());
        log.info("  - Access Key: {}", container.getAccessKey());
        log.info("  - Secret Key: {}", container.getSecretKey());
        log.info("  - Region: {}", container.getRegion());
    }

    @Test
    @Order(3)
    @DisplayName("LocalStack service processing test")
    void testLocalStackServiceAccess() {
        LocalStackContainer container = LocalStackPropertyRegistrar.getContainer("localstack");
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "test with file connection");
        assertTrue(endpoint.startsWith("http://"), "HTTP logging connection");
        
        try {
            String s3Endpoint = container.getEndpointOverride(LocalStackContainer.Service.S3).toString();
            assertNotNull(s3Endpoint, "S3 with file connection");
            log.info(" S3 Endpoint: {}", s3Endpoint);
        } catch (Exception e) {
            log.warn("S3 Endpoint file: {}", e.getMessage());
        }
        
        try {
            String dynamoEndpoint = container.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString();
            assertNotNull(dynamoEndpoint, "DynamoDB with file connection");
            log.info(" DynamoDB Endpoint: {}", dynamoEndpoint);
        } catch (Exception e) {
            log.warn("DynamoDB Endpoint file: {}", e.getMessage());
        }
        
        log.info(" LocalStack service processing test completed");
    }

    @Test
    @Order(4)
    @DisplayName("LocalStack file registration verification")
    void testLocalStackPropertyRegistration() {
        LocalStackContainer container = LocalStackPropertyRegistrar.getContainer("localstack");
        
        assertNotNull(container.getEndpoint(), "with file connection");
        assertNotNull(container.getAccessKey(), "Access Keyshould file connection");
        assertNotNull(container.getSecretKey(), "Secret Keyshould file connection");
        assertNotNull(container.getRegion(), "Regionshould file connection");
        
        log.info(" LocalStack file information verification completed");
        log.info("  - Endpoint: {}", container.getEndpoint());
        log.info("  - Access Key: {}", container.getAccessKey());
        log.info("  - Secret Key: [****]");
        log.info("  - Region: {}", container.getRegion());
        
        LocalStackContainer defaultContainer = LocalStackPropertyRegistrar.getContainer();
        assertEquals(container.getContainerId(), defaultContainer.getContainerId(), 
                "test file connection fileshould file connection");
        
        log.info(" LocalStack file registration test completed");
    }
}