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
@DisplayName("LocalStack AWS service should creation test")
class LocalStackBeanCreatorTest {

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
        
        spec = new LocalStackContainerSpec();
        spec.addServices(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        );
        spec.setDebugMode(false);
        spec.setEdgePort(4566);
        
        log.info("LocalStackBeanCreator test connection logging");
    }

    @Test
    @Order(1)
    @DisplayName("LocalStackBeanCreator test verification")
    void testSupportedType() {
        assertEquals(ContainerType.LOCALSTACK, beanCreator.getSupportedType());
        log.info(" LocalStackBeanCreator test: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("test AWS service test verification")
    void testSupportedServices() {
        Set<LocalStackContainerSpec.AwsService> supportedServices = beanCreator.getSupportedServices();
        
        assertNotNull(supportedServices, "test service testshould nulltest file");
        
        if (supportedServices.isEmpty()) {
            log.info(" AWS SDK dependencyshould connection test serviceshould file");
        } else {
            log.info(" test AWS service: {}", supportedServices);
        }
        
        for (LocalStackContainerSpec.AwsService service : Set.of(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS,
                LocalStackContainerSpec.AwsService.LAMBDA
        )) {
            if (beanCreator.isServiceSupported(service)) {
                log.info(" {} serviceshould test (AWS SDK test)", service);
            } else {
                log.info(" {} service Endpoint (AWS SDK dependency test)", service);
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("AWS service connection file test")
    void testGetFactory() {
        for (LocalStackContainerSpec.AwsService service : LocalStackContainerSpec.AwsService.values()) {
            var factoryOpt = beanCreator.getFactory(service);
            
            if (factoryOpt.isPresent()) {
                var factory = factoryOpt.get();
                assertEquals(service, factory.getSupportedService(), 
                    "connection test serviceshould file connection");
                assertNotNull(factory.getBeanName(), "needs to be addedshould nulltest file");
                assertFalse(factory.getBeanName().trim().isEmpty(), "needs to be addedshould Endpoint file");
                
                log.debug(" {} service connection: {} -> needs to be added: '{}'", 
                    service, factory.getClass().getSimpleName(), factory.getBeanName());
            } else {
                log.debug(" {} service connection test should file (dependency test)", service);
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("connection should testshould test exception processing")
    void testInvalidContainerType() {
        var mockContainer = new org.testcontainers.containers.GenericContainer<>("redis:7-alpine");
        ContainerInfo invalidContainerInfo = new ContainerInfo(
                "invalid-container",
                ContainerType.REDIS,
                mockContainer,
                spec
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> beanCreator.createBean(invalidContainerInfo));
        
        assertTrue(exception.getMessage().contains("LocalStackContainershould connection"), 
                "exception shouldshould file connection");
        
        log.info(" connection should testshould test exception processing verification: {}", exception.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("connection testshould test exception processing")
    void testInvalidSpecType() {
        var invalidSpec = new com.genius.primavera.testcontainers.config.RedisContainerSpec();
        ContainerInfo invalidSpecInfo = new ContainerInfo(
                "invalid-spec",
                ContainerType.LOCALSTACK,
                container,
                invalidSpec
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> beanCreator.createBean(invalidSpecInfo));
        
        assertTrue(exception.getMessage().contains("LocalStackContainerSpecshould connection"), 
                "exception shouldshould file connection");
        
        log.info(" connection testshould test exception processing verification: {}", exception.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("AWS servicetest should test service test")
    void testDefaultServicesWhenEmpty() {
        LocalStackContainerSpec emptySpec = new LocalStackContainerSpec();
        emptySpec.setServices(Set.of());
        
        ContainerInfo containerInfo = new ContainerInfo(
                "empty-services",
                ContainerType.LOCALSTACK,
                container,
                emptySpec
        );

        Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        assertNotNull(result, "resultshould nulltest file");
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            log.info(" test service test should creation should: {}", clientMap.keySet());
        } else {
            log.info(" test service test result: {}", result.getClass().getSimpleName());
        }
    }

    @Test
    @Order(7)
    @DisplayName("should creationshould file should6 verification")
    void testBeanCreatorRegistration() {
        var creatorOpt = com.genius.primavera.testcontainers.bean.BeanCreatorRegistry
                .findCreator(ContainerType.LOCALSTACK);
        
        assertTrue(creatorOpt.isPresent(), "LocalStackBeanCreatorshould registeredshould connection");
        assertInstanceOf(LocalStackBeanCreator.class, creatorOpt.get(), 
                "created successfully creatorshould LocalStackBeanCreator should connection");
        
        log.info(" LocalStackBeanCreatorshould BeanCreatorRegistryshould file should7");
    }

    @Test
    @Order(8)
    @DisplayName("test AWS service test")
    void testMultipleServiceCombination() {
        Set<LocalStackContainerSpec.AwsService>[] serviceCombinations = new Set[]{
                Set.of(LocalStackContainerSpec.AwsService.S3),
                Set.of(LocalStackContainerSpec.AwsService.S3, LocalStackContainerSpec.AwsService.DYNAMODB),
                Set.of(LocalStackContainerSpec.AwsService.SQS, LocalStackContainerSpec.AwsService.SNS),
                Set.of(LocalStackContainerSpec.AwsService.S3, LocalStackContainerSpec.AwsService.DYNAMODB, 
                       LocalStackContainerSpec.AwsService.SQS, LocalStackContainerSpec.AwsService.SNS,
                       LocalStackContainerSpec.AwsService.LAMBDA)
        };

        for (int i = 0; i < serviceCombinations.length; i++) {
            Set<LocalStackContainerSpec.AwsService> services = serviceCombinations[i];
            
            LocalStackContainerSpec testSpec = new LocalStackContainerSpec();
            testSpec.setServices(services);
            
            ContainerInfo containerInfo = new ContainerInfo(
                    "test-combination-" + i,
                    ContainerType.LOCALSTACK,
                    container,
                    testSpec
            );

            Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo), 
                    "service test " + services + " processing should exceptionshould file");
            
            assertNotNull(result, "resultshould nulltest file");
            
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientMap = (Map<String, Object>) result;
                log.info(" service test {} -> creation should: {}", 
                        services, clientMap.keySet());
            }
        }
    }

    @AfterAll
    void tearDown() {
        log.info("LocalStackBeanCreator testneeds to be added4");
    }
}