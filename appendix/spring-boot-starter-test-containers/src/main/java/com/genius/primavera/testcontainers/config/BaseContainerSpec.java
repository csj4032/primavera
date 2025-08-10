package com.genius.primavera.testcontainers.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Validated
public abstract class BaseContainerSpec {
    
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9._/-]*:[a-zA-Z0-9._-]+$", 
             message = "Invalid Docker image format")
    private String image;
    
    @Min(value = 10, message = "Startup timeout must be at least 10 seconds")
    @Max(value = 600, message = "Startup timeout must not exceed 600 seconds")
    private Integer startupTimeout = 60;
    
    @NotNull
    private Map<@NotBlank String, @NotNull String> environment = new HashMap<>();
    
    @NotNull
    private List<@NotBlank String> networkAliases = new ArrayList<>();
    
    private Map<@NotNull @Positive Integer, @Positive Integer> portBindings = new HashMap<>();
    
    private RestartPolicy restartPolicy = RestartPolicy.NO;
    
    private LogLevel logLevel = LogLevel.INFO;
    
    public enum RestartPolicy {
        NO,
        ALWAYS,
        UNLESS_STOPPED,
        ON_FAILURE
    }
    
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}