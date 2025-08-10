package com.genius.primavera.batch.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "primavera.config")
public class PrimaveraConfiguration {

    private String name;
    private boolean enabled;
    private Map<String, String> logs;

    public PrimaveraConfiguration(String name, boolean enabled, Map<String, String> logs) {
        this.name = name;
        this.enabled = enabled;
        this.logs = logs;
    }
}