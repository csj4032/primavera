package com.genius.primavera.testContainer;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Set;

public class TestContainerLifecycleExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // 테스트 클래스의 어노테이션 정보를 컨텍스트에 저장
        Class<?> testClass = context.getRequiredTestClass();
        EnablePrimaveraTestcontainers annotation = testClass.getAnnotation(EnablePrimaveraTestcontainers.class);
        if (annotation != null) {
            // ThreadLocal을 사용하여 컨텍스트 정보 저장
            Set<ContainerType> containerTypes = Set.of(annotation.containers());
            TestContextHolder.setContext(testClass.getName(), containerTypes, annotation.lifecycleMode());
            
            // 시스템 프로퍼티도 백업으로 설정 (호환성을 위해)
            System.setProperty("primavera.testcontainers.test-class", testClass.getName());
            StringBuilder containerTypesStr = new StringBuilder();
            for (ContainerType type : annotation.containers()) {
                if (containerTypesStr.length() > 0) {
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
        // 테스트 완료 후 PER_TEST 모드의 컨테이너들을 정리
        ContainerManager.stopContainers(ContainerLifecycleMode.PER_TEST);
        
        // ThreadLocal 컨텍스트 정리
        TestContextHolder.clearContext();
        
        // 시스템 프로퍼티 정리
        System.clearProperty("primavera.testcontainers.test-class");
        System.clearProperty("primavera.testcontainers.container-types");
        System.clearProperty("primavera.testcontainers.lifecycle-mode");
    }
}