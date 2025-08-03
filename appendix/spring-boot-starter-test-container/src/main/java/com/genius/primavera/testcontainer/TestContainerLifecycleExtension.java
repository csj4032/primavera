package com.genius.primavera.testContainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;

import java.util.Set;

@Slf4j
public class TestContainerLifecycleExtension implements BeforeAllCallback, AfterAllCallback, AfterTestExecutionCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        EnablePrimaveraTestcontainers annotation = testClass.getAnnotation(EnablePrimaveraTestcontainers.class);
        log.info("Starting containers before all tests in class: {}", annotation);
        if (annotation != null) {
            Set<ContainerType> containerTypes = Set.of(annotation.containers());
            log.info("Reusing existing container: {} for test class: {}", containerTypes, testClass.getName());
            TestContextHolder.setContext(testClass.getName(), containerTypes, annotation.lifecycleMode());
            System.setProperty("primavera.testcontainers.test-class", testClass.getName());
            StringBuilder containerTypesStr = new StringBuilder();
            for (ContainerType type : annotation.containers()) {
                if (!containerTypesStr.isEmpty()) {
                    containerTypesStr.append(",");
                }
                containerTypesStr.append(type.name());
            }
            System.setProperty("primavera.testcontainers.container-types", containerTypesStr.toString());
            System.setProperty("primavera.testcontainers.lifecycle-mode", annotation.lifecycleMode().name());
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        EnablePrimaveraTestcontainers annotation = testClass.getAnnotation(EnablePrimaveraTestcontainers.class);
        log.info("Stopping containers after all tests in class: {}", testClass.getName());
        ContainerManager.stopContainers(ContainerLifecycleMode.PER_METHOD);
        if (annotation != null && annotation.lifecycleMode() == ContainerLifecycleMode.PER_CLASS) {
            String testClassName = testClass.getName();
            Set<ContainerType> containerTypes = Set.of(annotation.containers());
            log.info("Stopping PER_CLASS containers for class: {}, types: {}", testClassName, containerTypes);
            for (ContainerType containerType : containerTypes) {
                ContainerKey key = ContainerKey.forPerClass(containerType, testClassName);
                log.info("Stopping and removing container: {}", key.getDisplayName());
                ContainerManager.stopAndRemoveContainer(containerType, ContainerLifecycleMode.PER_CLASS, testClassName);
            }
        }
        TestContextHolder.clearContext();
        System.clearProperty("primavera.testcontainers.test-class");
        System.clearProperty("primavera.testcontainers.container-types");
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        log.info("Test execution completed for: {}", context.getRequiredTestMethod().getName());
    }
}