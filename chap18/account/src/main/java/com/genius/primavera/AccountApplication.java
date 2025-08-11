package com.genius.primavera;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Map;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(PrimaveraConfiguration.class)
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}

@Getter
@Setter
@ConfigurationProperties("primavera.config")
class PrimaveraConfiguration {

    private String name;
    private boolean enabled;
    private Map<String, String> logs;

    public PrimaveraConfiguration(String name, boolean enabled, Map<String, String> logs) {
        this.name = name;
        this.enabled = enabled;
        this.logs = logs;
    }
}