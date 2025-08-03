package com.genius.primavera.testcontainer.v2;

import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

public class TestContextCustomizerFactory implements ContextCustomizerFactory {
    
    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, 
                                                   List<ContextConfigurationAttributes> configAttributes) {
        
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        if (annotation != null) {
            // 테스트 클래스마다 고유한 컨텍스트를 만들기 위해 클래스명을 포함
            return new TestContextCustomizer(testClass.getName());
        }
        return null;
    }
}