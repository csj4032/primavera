package com.genius.primavera.infrastructure.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "primavera")
public class PrimaveraProperties {

    private LogProperties logs = new LogProperties();

    @Data
    public static class LogProperties {
        private String path;
    }
}