package com.genius.primavera.testContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

@Slf4j
public class PrimaveraTestcontainersListener implements TestExecutionListener {

    public static final String TESTCONTAINERS_CONFIG_PROPERTY = "primavera.testcontainers.config";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public void beforeTestClass(TestContext testContext) { // prepareTestInstance 대신 이 메서드 사용
        EnablePrimaveraTestcontainers annotation = testContext.getTestClass().getAnnotation(EnablePrimaveraTestcontainers.class);
        if (annotation != null) {
            String containerTypesJson = objectMapper.writeValueAsString(annotation.value());
            System.setProperty(TESTCONTAINERS_CONFIG_PROPERTY, containerTypesJson);
            log.info("PrimaveraTestcontainersListener: @EnablePrimaveraTestcontainers 어노테이션 발견. 컨테이너 타입: {}", containerTypesJson);
        } else {
            System.clearProperty(TESTCONTAINERS_CONFIG_PROPERTY);
            log.info("PrimaveraTestcontainersListener: @EnablePrimaveraTestcontainers 어노테이션 미발견.");
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        System.clearProperty(TESTCONTAINERS_CONFIG_PROPERTY);
        log.info("PrimaveraTestcontainersListener: 시스템 프로퍼티 '{}' 정리.", TESTCONTAINERS_CONFIG_PROPERTY);
    }
}