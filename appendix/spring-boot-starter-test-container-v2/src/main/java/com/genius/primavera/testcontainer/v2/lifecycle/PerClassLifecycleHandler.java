package com.genius.primavera.testcontainer.v2.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import com.genius.primavera.testcontainer.v2.*;

import java.util.Set;

@Slf4j
public class PerClassLifecycleHandler extends AbstractContainerLifecycleHandler {
    
    @Override
    public boolean supports(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        TestInstance testInstance = testClass.getAnnotation(TestInstance.class);
        return testInstance != null && testInstance.value() == TestInstance.Lifecycle.PER_CLASS;
    }
    
    @Override
    public void beforeAll(ExtensionContext context) {
        log.info("PER_CLASS mode detected - use @DynamicPropertySource or extend AutoDynamicPropertySource");
    }
    
    @Override
    public void afterAll(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        
        if (annotation == null) return;
        
        log.info("Stopping PER_CLASS containers after all tests");
        
        if (annotation.lifecycleMode() == ContainerLifecycleMode.PER_CLASS) {
            for (ContainerType containerType : annotation.containers()) {
                ContainerManager.stopAndRemoveContainer(containerType, ContainerLifecycleMode.PER_CLASS, testClass.getName());
            }
        }
    }
    
    @Override
    protected void handleContainers(Set<ContainerType> containerTypes, 
                                  TestContainerProperties properties,
                                  ContainerLifecycleMode lifecycleMode, 
                                  Class<?> testClass) {
        // PER_CLASS 모드에서는 @DynamicPropertySource에서 컨테이너를 처리하므로 여기서는 아무것도 하지 않음
    }
}