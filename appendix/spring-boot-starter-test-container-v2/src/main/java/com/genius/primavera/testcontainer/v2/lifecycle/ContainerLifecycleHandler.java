package com.genius.primavera.testcontainer.v2.lifecycle;

import org.junit.jupiter.api.extension.ExtensionContext;

public interface ContainerLifecycleHandler {
    
    void beforeAll(ExtensionContext context);
    
    void afterAll(ExtensionContext context);
    
    boolean supports(ExtensionContext context);
}