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
    @DisplayName("LocalStack translated_text_4 translated_text_1 translated_text_4 registration verification")
    void testLocalStackPropertiesRegistration() {
        ContainerManager manager = ContainerRegistry.get();
        ContainerInfo localStackInfo = manager.getContainer("localstack");

        assertNotNull(localStackInfo, "LocalStack translated_text_4 translated_text_12 translated_text_4 translated_text_3");
        assertTrue(localStackInfo.container().isRunning(), "LocalStack translated_text_4translated_text_1 execution translated_text_4 translated_text_3");
        
        LocalStackContainer container = (LocalStackContainer) localStackInfo.container();
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "translated_text_6 translated_text_4 translated_text_3");
        assertTrue(endpoint.startsWith("http://"), "HTTP translated_text_7 translated_text_3");
        
        log.info(" LocalStack translated_text_5: {}", endpoint);
        log.info(" Access Key: {}", container.getAccessKey());
        log.info(" Secret Key: {}", container.getSecretKey());
        log.info(" Region: {}", container.getRegion());
    }

    @Test
    @Order(2)
    @DisplayName("LocalStack translated_text_4 information translated_text_4 test")
    void testGetLocalStackContainer() {
        LocalStackContainer container = assertDoesNotThrow(() -> {
            return LocalStackPropertyRegistrar.getContainer("localstack");
        }, "LocalStack translated_text_4 information translated_text_4 translated_text_1 translated_text_9 translated_text_3");
        
        assertNotNull(container, "translated_text_4translated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3");
        assertTrue(container.isRunning(), "translated_text_4translated_text_1 execution translated_text_4 translated_text_3");
        
        log.info(" LocalStack translated_text_4 information translated_text_4 success");
        log.info("  - translated_text_5: {}", container.getEndpoint());
        log.info("  - Access Key: {}", container.getAccessKey());
        log.info("  - Secret Key: {}", container.getSecretKey());
        log.info("  - Region: {}", container.getRegion());
    }

    @Test
    @Order(3)
    @DisplayName("LocalStack service translated_text_5 translated_text_2 test")
    void testLocalStackServiceAccess() {
        LocalStackContainer container = LocalStackPropertyRegistrar.getContainer("localstack");
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "translated_text_2 translated_text_6 translated_text_4 translated_text_3");
        assertTrue(endpoint.startsWith("http://"), "HTTP translated_text_7 translated_text_3");
        
        try {
            String s3Endpoint = container.getEndpointOverride(LocalStackContainer.Service.S3).toString();
            assertNotNull(s3Endpoint, "S3 translated_text_6 translated_text_4 translated_text_3");
            log.info(" S3 translated_text_5: {}", s3Endpoint);
        } catch (Exception e) {
            log.warn("S3 translated_text_5 translated_text_4: {}", e.getMessage());
        }
        
        try {
            String dynamoEndpoint = container.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString();
            assertNotNull(dynamoEndpoint, "DynamoDB translated_text_6 translated_text_4 translated_text_3");
            log.info(" DynamoDB translated_text_5: {}", dynamoEndpoint);
        } catch (Exception e) {
            log.warn("DynamoDB translated_text_5 translated_text_4: {}", e.getMessage());
        }
        
        log.info(" LocalStack service translated_text_5 translated_text_2 test completed");
    }

    @Test
    @Order(4)
    @DisplayName("LocalStack translated_text_4 registration verification")
    void testLocalStackPropertyRegistration() {
        LocalStackContainer container = LocalStackPropertyRegistrar.getContainer("localstack");
        
        assertNotNull(container.getEndpoint(), "translated_text_6 translated_text_4 translated_text_3");
        assertNotNull(container.getAccessKey(), "Access Keytranslated_text_1 translated_text_4 translated_text_3");
        assertNotNull(container.getSecretKey(), "Secret Keytranslated_text_1 translated_text_4 translated_text_3");
        assertNotNull(container.getRegion(), "Regiontranslated_text_1 translated_text_4 translated_text_3");
        
        log.info(" LocalStack translated_text_4 information verification completed");
        log.info("  - translated_text_5: {}", container.getEndpoint());
        log.info("  - Access Key: {}", container.getAccessKey());
        log.info("  - Secret Key: [****]");
        log.info("  - Region: {}", container.getRegion());
        
        LocalStackContainer defaultContainer = LocalStackPropertyRegistrar.getContainer();
        assertEquals(container.getContainerId(), defaultContainer.getContainerId(), 
                "translated_text_2 translated_text_4 translated_text_3 translated_text_4translated_text_1 translated_text_4 translated_text_3");
        
        log.info(" LocalStack translated_text_4 registration translated_text_2 test completed");
    }
}