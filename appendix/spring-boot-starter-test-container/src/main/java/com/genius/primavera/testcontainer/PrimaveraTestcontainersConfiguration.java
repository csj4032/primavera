package com.genius.primavera.testContainer;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(PrimaveraTestcontainersProperties.class)
public class PrimaveraTestcontainersConfiguration {

    private final PrimaveraTestcontainersProperties properties;

    public PrimaveraTestcontainersConfiguration(PrimaveraTestcontainersProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        properties.getContainers().forEach((type, config) -> {
            log.info("{} container config: {}", type, config);
        });
    }
}