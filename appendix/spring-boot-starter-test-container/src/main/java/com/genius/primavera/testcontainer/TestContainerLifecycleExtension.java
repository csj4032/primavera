package com.genius.primavera.testContainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Set;

@Slf4j
public class TestContainerLifecycleExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        EnablePrimaveraTestcontainers annotation = testClass.getAnnotation(EnablePrimaveraTestcontainers.class);
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
        ContainerManager.stopContainers(ContainerLifecycleMode.PER_TEST);
        TestContextHolder.clearContext();
        System.clearProperty("primavera.testcontainers.test-class");
        System.clearProperty("primavera.testcontainers.container-types");
        System.clearProperty("primavera.testcontainers.lifecycle-mode");
    }
}