package com.genius.primavera.testcontainer.v2;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.testcontainers.containers.GenericContainer;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TestContainerTestExecutionListener implements TestExecutionListener {
    
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        
        if (annotation == null) {
            return;
        }
        
        TestInstance testInstance = testClass.getAnnotation(TestInstance.class);
        if (testInstance != null && testInstance.value() == TestInstance.Lifecycle.PER_CLASS) {
            log.info("PER_CLASS mode - containers handled by @DynamicPropertySource");
            return;
        }
        
        log.info("Starting containers before test class: {}", testClass.getName());
        
        TestContainerProperties properties = loadProperties();
        Set<ContainerType> containerTypes = Arrays.stream(annotation.containers())
                .collect(Collectors.toSet());
        ContainerLifecycleMode lifecycleMode = annotation.lifecycleMode();
        
        for (ContainerType containerType : containerTypes) {
            startContainer(containerType, properties, lifecycleMode, testClass.getName());
        }
    }
    
    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        
        if (annotation == null) return;
        
        TestInstance testInstance = testClass.getAnnotation(TestInstance.class);
        if (testInstance != null && testInstance.value() == TestInstance.Lifecycle.PER_CLASS) {
            return;
        }
        
        log.info("Stopping containers after test class: {}", testClass.getName());
        ContainerManager.stopContainers(ContainerLifecycleMode.PER_METHOD);
    }
    
    private TestContainerProperties loadProperties() {
        return DynamicContainerSupport.loadProperties();
    }
    
    private void startContainer(ContainerType containerType, TestContainerProperties properties, 
                              ContainerLifecycleMode lifecycleMode, String testClassName) {
        if (ContainerManager.containsContainer(containerType, lifecycleMode, testClassName)) {
            GenericContainer<?> existingContainer = ContainerManager.getContainer(containerType, lifecycleMode, testClassName);
            ContainerPropertyConfigurator.configureSpringProperties(containerType, existingContainer);
            log.info("Reusing existing container: {} for test class: {}", containerType, testClassName);
            return;
        }
        
        try {
            TestContainerProperties.ContainerConfig config = getContainerConfig(properties, containerType);
            GenericContainer<?> container = ContainerFactory.createContainer(containerType, config);
            container.start();
            ContainerManager.putContainer(containerType, container, lifecycleMode, testClassName);
            ContainerPropertyConfigurator.configureSpringProperties(containerType, container);
            log.info("Started container: {} for test class: {}", containerType, testClassName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create and start container: " + containerType, e);
        }
    }
    
    private TestContainerProperties.ContainerConfig getContainerConfig(TestContainerProperties properties, ContainerType containerType) {
        return switch (containerType) {
            case MARIADB -> properties.getMariadb();
            case MYSQL -> properties.getMysql();
            case POSTGRESQL -> properties.getPostgresql();
            case REDIS -> properties.getRedis();
            case KAFKA -> properties.getKafka();
            case ELASTICSEARCH -> properties.getElasticsearch();
            case MONGODB -> properties.getMongodb();
        };
    }
}