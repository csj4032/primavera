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
public class PerMethodTestExecutionListener implements TestExecutionListener {
    
    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        
        if (annotation == null) {
            return;
        }
        
        TestInstance testInstance = testClass.getAnnotation(TestInstance.class);
        if (testInstance != null && testInstance.value() == TestInstance.Lifecycle.PER_CLASS) {
            return; // PER_CLASS는 @DynamicPropertySource로 처리
        }
        
        String methodName = testContext.getTestMethod().getName();
        log.info("Starting containers before test method: {}.{}", testClass.getSimpleName(), methodName);
        
        TestContainerProperties properties = DynamicContainerSupport.loadProperties();
        Set<ContainerType> containerTypes = Arrays.stream(annotation.containers())
                .collect(Collectors.toSet());
        
        for (ContainerType containerType : containerTypes) {
            startContainerForMethod(containerType, properties, testClass.getName(), methodName);
        }
    }
    
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        
        if (annotation == null) return;
        
        TestInstance testInstance = testClass.getAnnotation(TestInstance.class);
        if (testInstance != null && testInstance.value() == TestInstance.Lifecycle.PER_CLASS) {
            return;
        }
        
        String methodName = testContext.getTestMethod().getName();
        log.info("Stopping containers after test method: {}.{}", testClass.getSimpleName(), methodName);
        
        for (ContainerType containerType : annotation.containers()) {
            ContainerManager.stopAndRemoveContainer(containerType, ContainerLifecycleMode.PER_METHOD, 
                    testClass.getName(), methodName);
        }
    }
    
    private void startContainerForMethod(ContainerType containerType, TestContainerProperties properties, 
                                       String testClassName, String methodName) {
        if (ContainerManager.containsContainer(containerType, ContainerLifecycleMode.PER_METHOD, testClassName, methodName)) {
            log.info("Container already exists for method: {}.{}", testClassName, methodName);
            return;
        }
        
        try {
            TestContainerProperties.ContainerConfig config = getContainerConfig(properties, containerType);
            GenericContainer<?> container = ContainerFactory.createContainer(containerType, config);
            container.start();
            ContainerManager.putContainer(containerType, container, ContainerLifecycleMode.PER_METHOD, testClassName, methodName);
            ContainerPropertyConfigurator.configureSpringProperties(containerType, container);
            log.info("Started container: {} for method: {}.{}", containerType, testClassName, methodName);
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