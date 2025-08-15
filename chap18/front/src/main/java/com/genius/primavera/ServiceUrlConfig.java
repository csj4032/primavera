package com.genius.primavera;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "primavera.services")
public class ServiceUrlConfig {
    private String accountUrl = "http://localhost:8081";
    private String orderUrl = "http://localhost:8082";
    private String productUrl = "http://localhost:8083";
}