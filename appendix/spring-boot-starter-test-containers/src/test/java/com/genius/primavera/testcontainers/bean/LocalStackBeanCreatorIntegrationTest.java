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
@DisplayName("LocalStack AWS service should test")
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
        
        log.info("LocalStack Endpoint Endpoint... (connection test should file)");
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
        
        log.info(" LocalStack Endpoint logging. Endpoint: {}", container.getEndpoint());
        log.info("- connection should: {}", container.getAccessKey());
        log.info("- connection should: {}", container.getSecretKey());
        log.info("- test: {}", container.getRegion());
    }

    @AfterAll
    void tearDown() {
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("LocalStack Endpoint logging");
        }
    }

    @Test
    @Order(1)
    @DisplayName("test LocalStack with AWS Endpoint should creation")
    void testRealAwsClientCreation() {
        ContainerInfo containerInfo = new ContainerInfo(
                "integration-test-localstack",
                ContainerType.LOCALSTACK,
                container,
                spec
        );

        log.info("LocalStack with AWS Endpoint creationconnection...");
        
        Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        assertNotNull(result, "creation logging nulltest file");
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            
            if (clientMap.isEmpty()) {
                log.info(" AWS SDK dependencyshould connection Endpoint creation Endpoint");
            } else {
                log.info(" creation AWS Endpoint: {}", clientMap.keySet());
                
                clientMap.forEach((beanName, client) -> {
                    assertNotNull(client, "Endpoint " + beanName + "should nulltest file");
                    log.info("  - {}: {}", beanName, client.getClass().getName());
                });
            }
            
        } else {
            log.info(" AWS Endpoint creation result: {} (Mapshould test)", result.getClass().getSimpleName());
        }
    }

    @Test
    @Order(2) 
    @DisplayName("test AWS service Endpoint creation test")
    void testIndividualServiceClients() {
        LocalStackContainerSpec.AwsService[] testServices = {
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        };

        for (LocalStackContainerSpec.AwsService service : testServices) {
            if (!beanCreator.isServiceSupported(service)) {
                log.info(" {} service Endpoint (AWS SDK dependency test)", service);
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
                    service + " service Endpoint creation needs to be added0 file");
            
            assertNotNull(result, service + " service logging nulltest file");
            
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientMap = (Map<String, Object>) result;
                
                if (!clientMap.isEmpty()) {
                    assertEquals(1, clientMap.size(), service + " service with 1should Endpoint creation connection");
                    log.info(" {} service Endpoint creation success: {}", service, clientMap.keySet().iterator().next());
                } else {
                    log.info(" {} service Endpoint creation Endpoint (AWS SDK test)", service);
                }
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("LocalStack connection verification")
    void testLocalStackConnectivity() {
        assertTrue(container.isRunning(), "LocalStack Endpoint execution shouldshould connection");
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "Endpoint nulltest file");
        assertTrue(endpoint.startsWith("http"), "Endpoint HTTP URLshould connection");
        
        log.info(" LocalStack connection verification completed:");
        log.info("  - should execution test: {}", container.isRunning());
        log.info("  - Endpoint: {}", endpoint);
        log.info("  - connection should: {}", container.getAccessKey());
        log.info("  - test: {}", container.getRegion());
    }

    @Test
    @Order(4)
    @DisplayName("AWS service Endpoint verification")
    void testServiceEndpoints() {
        LocalStackContainer.Service[] services = {
                LocalStackContainer.Service.S3,
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.SQS,
                LocalStackContainer.Service.SNS
        };

        for (LocalStackContainer.Service service : services) {
            String serviceEndpoint = container.getEndpointOverride(service).toString();
            assertNotNull(serviceEndpoint, service + " Endpoint nulltest file");
            assertTrue(serviceEndpoint.startsWith("http"), service + " Endpoint HTTP URLshould connection");
            
            log.info(" {} service Endpoint: {}", service, serviceEndpoint);
        }
    }

    @Test
    @Order(5)
    @DisplayName("should service test creation test")
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
        
        assertNotNull(result, "logging nulltest file");
        assertTrue(duration < 30000, "should service creationshould 30needs to be addedshould completed connection");
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            log.info(" should service creation test: {}should Endpoint {}msshould creation", clientMap.size(), duration);
        } else {
            log.info(" should service creation completed: {}ms", duration);
        }
    }
}