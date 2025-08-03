package com.genius.primavera.testcontainer.v2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * PER_CLASS 모드에서 자동으로 TestContainer를 설정하는 베이스 클래스
 * 
 * 사용법:
 * @TestInstance(TestInstance.Lifecycle.PER_CLASS)
 * @EnableTestContainers(containers = {ContainerType.MARIADB})
 * class MyTest extends AutoDynamicPropertySource {
 *     // @DynamicPropertySource 메서드 자동 상속
 * }
 */
@Slf4j
public abstract class AutoDynamicPropertySource {
    
    private static Class<?> currentTestClass;
    
    protected AutoDynamicPropertySource() {
        currentTestClass = this.getClass();
    }
    
    @DynamicPropertySource
    static void configureTestContainers(DynamicPropertyRegistry registry) {
        if (currentTestClass != null) {
            log.info("Configuring TestContainers for: {}", currentTestClass.getSimpleName());
            DynamicContainerSupport.configureContainers(currentTestClass, registry);
        } else {
            log.warn("Test class not found for @DynamicPropertySource configuration");
        }
    }
}