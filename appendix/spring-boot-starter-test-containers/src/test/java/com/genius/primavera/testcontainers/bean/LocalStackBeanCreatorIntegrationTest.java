package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("LocalStack AWS service translated_text_1 translated_text_2 test")
class LocalStackBeanCreatorIntegrationTest {

    private LocalStackBeanCreator beanCreator;
    private LocalStackContainer container;
    private LocalStackContainerSpec spec;

    @BeforeAll
    void setUp() {
        beanCreator = new LocalStackBeanCreator();
        
        container = new LocalStackContainer(
                org.testcontainers.utility.DockerImageName.parse("localstack/localstack:2.3.0")
        ).withServices(
                LocalStackContainer.Service.S3,
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.SQS,
                LocalStackContainer.Service.SNS
        );
        
        log.info("LocalStack translated_text_5 translated_text_5... (translated_text_3 translated_text_2 translated_text_1 translated_text_4)");
        container.start();
        
        spec = new LocalStackContainerSpec();
        spec.addServices(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        );
        spec.setDebugMode(false);
        spec.setEdgePort(4566);
        
        log.info(" LocalStack translated_text_5 translated_text_7. translated_text_5: {}", container.getEndpoint());
        log.info("- translated_text_3 translated_text_1: {}", container.getAccessKey());
        log.info("- translated_text_3 translated_text_1: {}", container.getSecretKey());
        log.info("- translated_text_2: {}", container.getRegion());
    }

    @AfterAll
    void tearDown() {
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("LocalStack translated_text_5 translated_text_7");
        }
    }

    @Test
    @Order(1)
    @DisplayName("translated_text_2 LocalStack translated_text_6 AWS translated_text_5 translated_text_1 creation")
    void testRealAwsClientCreation() {
        ContainerInfo containerInfo = new ContainerInfo(
                "integration-test-localstack",
                ContainerType.LOCALSTACK,
                container,
                spec
        );

        log.info("LocalStack translated_text_6 AWS translated_text_5 creationtranslated_text_3...");
        
        Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        assertNotNull(result, "creation translated_text_7 nulltranslated_text_2 translated_text_4");
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            
            if (clientMap.isEmpty()) {
                log.info(" AWS SDK dependencytranslated_text_1 translated_text_3 translated_text_5 creation translated_text_5");
            } else {
                log.info(" creation AWS translated_text_5: {}", clientMap.keySet());
                
                clientMap.forEach((beanName, client) -> {
                    assertNotNull(client, "translated_text_5 " + beanName + "translated_text_1 nulltranslated_text_2 translated_text_4");
                    log.info("  - {}: {}", beanName, client.getClass().getName());
                });
            }
            
        } else {
            log.info(" AWS translated_text_5 creation result: {} (Maptranslated_text_1 translated_text_2 translated_text_2)", result.getClass().getSimpleName());
        }
    }

    @Test
    @Order(2) 
    @DisplayName("translated_text_2 AWS service translated_text_5 creation test")
    void testIndividualServiceClients() {
        LocalStackContainerSpec.AwsService[] testServices = {
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        };

        for (LocalStackContainerSpec.AwsService service : testServices) {
            if (!beanCreator.isServiceSupported(service)) {
                log.info(" {} service translated_text_5 (AWS SDK dependency translated_text_2)", service);
                continue;
            }

            LocalStackContainerSpec singleServiceSpec = new LocalStackContainerSpec();
            singleServiceSpec.setServices(Set.of(service));
            
            ContainerInfo containerInfo = new ContainerInfo(
                    "test-" + service.name().toLowerCase(),
                    ContainerType.LOCALSTACK,
                    container,
                    singleServiceSpec
            );

            Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo),
                    service + " service translated_text_5 creation translated_text_1 translated_text_10 translated_text_4 translated_text_4");
            
            assertNotNull(result, service + " service translated_text_7 nulltranslated_text_2 translated_text_4");
            
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientMap = (Map<String, Object>) result;
                
                if (!clientMap.isEmpty()) {
                    assertEquals(1, clientMap.size(), service + " service translated_text_6 1translated_text_1 translated_text_5 creation translated_text_3");
                    log.info(" {} service translated_text_5 creation success: {}", service, clientMap.keySet().iterator().next());
                } else {
                    log.info(" {} service translated_text_5 creation translated_text_5 (AWS SDK translated_text_2)", service);
                }
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("LocalStack translated_text_3 verification")
    void testLocalStackConnectivity() {
        assertTrue(container.isRunning(), "LocalStack translated_text_5 execution translated_text_1translated_text_1 translated_text_3");
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "translated_text_5 nulltranslated_text_2 translated_text_4");
        assertTrue(endpoint.startsWith("http"), "translated_text_5 HTTP URLtranslated_text_1 translated_text_3");
        
        log.info(" LocalStack translated_text_3 verification completed:");
        log.info("  - translated_text_1 execution translated_text_2: {}", container.isRunning());
        log.info("  - translated_text_5: {}", endpoint);
        log.info("  - translated_text_3 translated_text_1: {}", container.getAccessKey());
        log.info("  - translated_text_2: {}", container.getRegion());
    }

    @Test
    @Order(4)
    @DisplayName("AWS service translated_text_5 verification")
    void testServiceEndpoints() {
        LocalStackContainer.Service[] services = {
                LocalStackContainer.Service.S3,
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.SQS,
                LocalStackContainer.Service.SNS
        };

        for (LocalStackContainer.Service service : services) {
            String serviceEndpoint = container.getEndpointOverride(service).toString();
            assertNotNull(serviceEndpoint, service + " translated_text_5 nulltranslated_text_2 translated_text_4");
            assertTrue(serviceEndpoint.startsWith("http"), service + " translated_text_5 HTTP URLtranslated_text_1 translated_text_3");
            
            log.info(" {} service translated_text_5: {}", service, serviceEndpoint);
        }
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_1 service translated_text_2 creation translated_text_2 test")
    void testMultipleServicePerformance() {
        LocalStackContainerSpec multiServiceSpec = new LocalStackContainerSpec();
        multiServiceSpec.addServices(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        );
        
        ContainerInfo containerInfo = new ContainerInfo(
                "multi-service-performance",
                ContainerType.LOCALSTACK,
                container,
                multiServiceSpec
        );

        long startTime = System.currentTimeMillis();
        
        Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertNotNull(result, "translated_text_7 nulltranslated_text_2 translated_text_4");
        assertTrue(duration < 30000, "translated_text_1 service creationtranslated_text_1 30translated_text_1 translated_text_1translated_text_1 completed translated_text_3");
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            log.info(" translated_text_1 service creation translated_text_2: {}translated_text_1 translated_text_5 {}mstranslated_text_1 creation", clientMap.size(), duration);
        } else {
            log.info(" translated_text_1 service creation completed: {}ms", duration);
        }
    }
}