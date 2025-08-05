package com.genius.primavera.testcontainer.v4.config;

import com.genius.primavera.testcontainer.v4.ContainerCreatorRegistry;
import com.genius.primavera.testcontainer.v4.ContainerType;
import com.genius.primavera.testcontainer.v4.bean.BeanCreatorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ConfigurationProperties(prefix = "testcontainer.registry")
public class TestContainerConfigurationProcessor {
    
    private Map<String, Boolean> enabled = new ConcurrentHashMap<>();
    private boolean autoDiscovery = true;
    private boolean lazyInitialization = true;
    
    public void processConfiguration(Environment environment) {
        log.info("Processing TestContainer configuration...");
        
        if (lazyInitialization) {
            log.debug("Lazy initialization enabled - registries will be initialized on first use");
        } else {
            log.debug("Eager initialization - initializing registries now");
            ContainerCreatorRegistry.initialize();
            BeanCreatorRegistry.initialize();
        }
        
        configureEnabledTypes();
        
        if (!autoDiscovery) {
            log.info("Auto-discovery disabled - only explicitly configured container types will be available");
        }
    }
    
    private void configureEnabledTypes() {
        for (ContainerType type : ContainerType.values()) {
            String typeName = type.name().toLowerCase();
            boolean isEnabled = enabled.getOrDefault(typeName, true); // Default to enabled
            
            if (!isEnabled) {
                log.info("Container type {} is disabled by configuration", type);
                ContainerCreatorRegistry.setTypeEnabled(type, false);
                BeanCreatorRegistry.setTypeEnabled(type, false);
            } else {
                log.debug("Container type {} is enabled", type);
            }
        }
    }
    
    public void setContainerTypeEnabled(ContainerType type, boolean enabled) {
        String typeName = type.name().toLowerCase();
        this.enabled.put(typeName, enabled);
        
        ContainerCreatorRegistry.setTypeEnabled(type, enabled);
        BeanCreatorRegistry.setTypeEnabled(type, enabled);
        
        log.info("Container type {} is now {}", type, enabled ? "enabled" : "disabled");
    }
    
    public boolean isContainerTypeEnabled(ContainerType type) {
        String typeName = type.name().toLowerCase();
        return enabled.getOrDefault(typeName, true);
    }
    
    // Getters and setters for configuration properties
    
    public Map<String, Boolean> getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Map<String, Boolean> enabled) {
        this.enabled = enabled;
    }
    
    public boolean isAutoDiscovery() {
        return autoDiscovery;
    }
    
    public void setAutoDiscovery(boolean autoDiscovery) {
        this.autoDiscovery = autoDiscovery;
    }
    
    public boolean isLazyInitialization() {
        return lazyInitialization;
    }
    
    public void setLazyInitialization(boolean lazyInitialization) {
        this.lazyInitialization = lazyInitialization;
    }
}