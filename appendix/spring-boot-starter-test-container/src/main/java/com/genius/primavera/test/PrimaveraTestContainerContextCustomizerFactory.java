package com.genius.primavera.test;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

public class PrimaveraTestContainerContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        PrimaveraTestContainer annotation = testClass.getAnnotation(PrimaveraTestContainer.class);
        if (annotation != null) {
            return new PrimaveraTestContainerContextCustomizer(annotation);
        }
        return null;
    }
}