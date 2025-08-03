package com.genius.primavera.testcontainer.v2;

import com.genius.primavera.testcontainer.v2.lifecycle.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;

import java.util.List;

@Slf4j
public class TestContainerExtension implements BeforeAllCallback, AfterAllCallback {
    
    private final List<ContainerLifecycleHandler> handlers = List.of(
        new PerMethodLifecycleHandler(),
        new PerClassLifecycleHandler()
    );

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        handlers.stream()
                .filter(handler -> handler.supports(context))
                .findFirst()
                .ifPresentOrElse(
                    handler -> handler.beforeAll(context),
                    () -> log.warn("No suitable handler found for test class: {}", 
                                 context.getRequiredTestClass().getName())
                );
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        handlers.stream()
                .filter(handler -> handler.supports(context))
                .findFirst()
                .ifPresent(handler -> handler.afterAll(context));
    }

}