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
@DisplayName("LocalStack AWS service translated_text_1 creation test")
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
        
        log.info("LocalStackBeanCreator test translated_text_3 translated_text_7");
    }

    @Test
    @Order(1)
    @DisplayName("LocalStackBeanCreator translated_text_2 translated_text_2 verification")
    void testSupportedType() {
        assertEquals(ContainerType.LOCALSTACK, beanCreator.getSupportedType());
        log.info(" LocalStackBeanCreator translated_text_2 translated_text_2: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 AWS service translated_text_2 verification")
    void testSupportedServices() {
        Set<LocalStackContainerSpec.AwsService> supportedServices = beanCreator.getSupportedServices();
        
        assertNotNull(supportedServices, "translated_text_2 service translated_text_2translated_text_1 nulltranslated_text_2 translated_text_4");
        
        if (supportedServices.isEmpty()) {
            log.info(" AWS SDK dependencytranslated_text_1 translated_text_3 translated_text_2 servicetranslated_text_1 translated_text_4");
        } else {
            log.info(" translated_text_2 AWS service: {}", supportedServices);
        }
        
        for (LocalStackContainerSpec.AwsService service : Set.of(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS,
                LocalStackContainerSpec.AwsService.LAMBDA
        )) {
            if (beanCreator.isServiceSupported(service)) {
                log.info(" {} servicetranslated_text_1 translated_text_2 (AWS SDK translated_text_2 translated_text_2)", service);
            } else {
                log.info(" {} service translated_text_5 (AWS SDK dependency translated_text_2)", service);
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("AWS service translated_text_3 translated_text_4 test")
    void testGetFactory() {
        for (LocalStackContainerSpec.AwsService service : LocalStackContainerSpec.AwsService.values()) {
            var factoryOpt = beanCreator.getFactory(service);
            
            if (factoryOpt.isPresent()) {
                var factory = factoryOpt.get();
                assertEquals(service, factory.getSupportedService(), 
                    "translated_text_3 translated_text_2 servicetranslated_text_1 translated_text_4 translated_text_3");
                assertNotNull(factory.getBeanName(), "translated_text_1 translated_text_1translated_text_1 nulltranslated_text_2 translated_text_4");
                assertFalse(factory.getBeanName().trim().isEmpty(), "translated_text_1 translated_text_1translated_text_1 translated_text_5 translated_text_4");
                
                log.debug(" {} service translated_text_3: {} -> translated_text_1 translated_text_1: '{}'", 
                    service, factory.getClass().getSimpleName(), factory.getBeanName());
            } else {
                log.debug(" {} service translated_text_3 translated_text_2 translated_text_1 translated_text_4 (dependency translated_text_2)", service);
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_3 translated_text_1 translated_text_2translated_text_1 translated_text_2 exception processing")
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
        
        assertTrue(exception.getMessage().contains("LocalStackContainertranslated_text_1 translated_text_3"), 
                "exception translated_text_1translated_text_1 translated_text_4 translated_text_3");
        
        log.info(" translated_text_3 translated_text_1 translated_text_2translated_text_1 translated_text_2 exception processing verification: {}", exception.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_3 translated_text_2 translated_text_2translated_text_1 translated_text_2 exception processing")
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
        
        assertTrue(exception.getMessage().contains("LocalStackContainerSpectranslated_text_1 translated_text_3"), 
                "exception translated_text_1translated_text_1 translated_text_4 translated_text_3");
        
        log.info(" translated_text_3 translated_text_2 translated_text_2translated_text_1 translated_text_2 exception processing verification: {}", exception.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("AWS servicetranslated_text_1 translated_text_2 translated_text_1 translated_text_2 service translated_text_2")
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
        
        assertNotNull(result, "resulttranslated_text_1 nulltranslated_text_2 translated_text_4");
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            log.info(" translated_text_2 service translated_text_2 translated_text_1 creation translated_text_1: {}", clientMap.keySet());
        } else {
            log.info(" translated_text_2 service translated_text_2 result: {}", result.getClass().getSimpleName());
        }
    }

    @Test
    @Order(7)
    @DisplayName("translated_text_1 creationtranslated_text_1 translated_text_4 translated_text_16 verification")
    void testBeanCreatorRegistration() {
        var creatorOpt = com.genius.primavera.testcontainers.bean.BeanCreatorRegistry
                .findCreator(ContainerType.LOCALSTACK);
        
        assertTrue(creatorOpt.isPresent(), "LocalStackBeanCreatortranslated_text_1 registeredtranslated_text_1 translated_text_3");
        assertInstanceOf(LocalStackBeanCreator.class, creatorOpt.get(), 
                "translated_text_13 creatortranslated_text_1 LocalStackBeanCreator translated_text_1 translated_text_3");
        
        log.info(" LocalStackBeanCreatortranslated_text_1 BeanCreatorRegistrytranslated_text_1 translated_text_4 translated_text_17");
    }

    @Test
    @Order(8)
    @DisplayName("translated_text_2 AWS service translated_text_2 test")
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
                    "service translated_text_2 " + services + " processing translated_text_1 exceptiontranslated_text_1 translated_text_4 translated_text_4");
            
            assertNotNull(result, "resulttranslated_text_1 nulltranslated_text_2 translated_text_4");
            
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientMap = (Map<String, Object>) result;
                log.info(" service translated_text_2 {} -> creation translated_text_1: {}", 
                        services, clientMap.keySet());
            }
        }
    }

    @AfterAll
    void tearDown() {
        log.info("LocalStackBeanCreator testtranslated_text_1 translated_text_14");
    }
}