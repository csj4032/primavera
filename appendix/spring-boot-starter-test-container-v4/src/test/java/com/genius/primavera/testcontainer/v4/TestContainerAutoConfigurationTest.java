package com.genius.primavera.testcontainer.v4;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("TestContainerAutoConfiguration Unit Tests")
class TestContainerAutoConfigurationTest {
    
    @Test
    @DisplayName("Auto configuration class can be instantiated")
    void testAutoConfigurationInstantiable() {
        assertDoesNotThrow(() -> {
            TestContainerAutoConfiguration config = new TestContainerAutoConfiguration();
            assertNotNull(config, "AutoConfiguration should be instantiable");
        }, "AutoConfiguration should be instantiable without errors");
        
        log.info("✅ TestContainerAutoConfiguration instantiated successfully");
    }
    
    @Test
    @DisplayName("Auto configuration class has proper annotations")
    void testAutoConfigurationAnnotations() {
        Class<TestContainerAutoConfiguration> configClass = TestContainerAutoConfiguration.class;
        
        assertTrue(configClass.isAnnotationPresent(org.springframework.boot.autoconfigure.AutoConfiguration.class),
            "Should have @AutoConfiguration annotation");
        
        log.info("✅ TestContainerAutoConfiguration has required annotations");
    }
    
    @Test
    @DisplayName("Auto configuration class is in correct package")
    void testAutoConfigurationPackage() {
        String packageName = TestContainerAutoConfiguration.class.getPackage().getName();
        assertEquals("com.genius.primavera.testcontainer.v4", packageName,
            "Should be in correct package for auto-detection");
        
        log.info("✅ TestContainerAutoConfiguration in correct package: {}", packageName);
    }
}