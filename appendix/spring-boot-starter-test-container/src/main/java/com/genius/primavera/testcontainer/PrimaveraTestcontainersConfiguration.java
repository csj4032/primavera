package com.genius.primavera.testcontainer;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PrimaveraTestcontainersProperties.class)
public class PrimaveraTestcontainersConfiguration {

    private final PrimaveraTestcontainersProperties properties;

    public PrimaveraTestcontainersConfiguration(PrimaveraTestcontainersProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        System.out.println("🚀 Primavera TestContainers Configuration initialized");
        System.out.println("📋 Lifecycle Mode: " + properties.getLifecycleMode());
        System.out.println("📦 Available Containers:");
        properties.getContainers().forEach((type, config) -> {
            System.out.println("  - " + type.toUpperCase() + ": " + config.getDockerImageName());
        });
    }
}