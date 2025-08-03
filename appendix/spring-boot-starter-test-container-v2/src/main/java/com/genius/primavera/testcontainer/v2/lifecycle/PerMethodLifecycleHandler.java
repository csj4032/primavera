package com.genius.primavera.testcontainer.v2.lifecycle;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;

import java.util.Set;

@Slf4j
public class PerMethodLifecycleHandler extends AbstractContainerLifecycleHandler {
    
    @Override
    public boolean supports(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        TestInstance testInstance = testClass.getAnnotation(TestInstance.class);
        return testInstance == null || testInstance.value() == TestInstance.Lifecycle.PER_METHOD;
    }
    
    @Override
    public void beforeAll(ExtensionContext context) {
        processContainers(context);
    }
    
    @Override
    public void afterAll(ExtensionContext context) {
        log.info("Stopping containers after all tests");
        ContainerManager.stopContainers(ContainerLifecycleMode.PER_METHOD);
    }
    
    @Override
    protected void handleContainers(Set<ContainerType> containerTypes, 
                                  TestContainerProperties properties,
                                  ContainerLifecycleMode lifecycleMode, 
                                  Class<?> testClass) {
        
        for (ContainerType containerType : containerTypes) {
            startContainer(containerType, properties, lifecycleMode, testClass.getName());
        }
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
}