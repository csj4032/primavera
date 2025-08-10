package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties
@Validated
public class RedisContainerSpec extends BaseContainerSpec {
    
    @Size(min = 6, message = "Redis password must be at least 6 characters")
    private String password;
    
    @Pattern(regexp = "^\\d+[kmgKMG]?$", message = "Invalid memory format (use: 128m, 1g, etc.)")
    private String maxMemory = "128m";
    
    private MaxMemoryPolicy maxMemoryPolicy = MaxMemoryPolicy.ALLKEYS_LRU;
    
    private Boolean persistenceEnabled = false;
    
    private Boolean aofEnabled = false;
    
    @Min(value = 1, message = "Database count must be at least 1")
    @Max(value = 16384, message = "Database count must not exceed 16384")
    private Integer databases = 16;
    
    @Min(value = 1024, message = "Port must be at least 1024")
    @Max(value = 65535, message = "Port must not exceed 65535")
    private Integer port = 6379;
    
    @Min(value = 0, message = "Timeout must be non-negative")
    private Integer timeout = 0;
    
    @Min(value = 0, message = "TCP keepalive must be non-negative")
    private Integer tcpKeepAlive = 300;
    
    private RedisLogLevel redisLogLevel = RedisLogLevel.NOTICE;
    
    private Boolean appendOnlyEnabled = false;
    
    public enum MaxMemoryPolicy {
        ALLKEYS_LRU,
        ALLKEYS_RANDOM,
        VOLATILE_TTL,
        VOLATILE_LFU
    }
    
    public enum RedisLogLevel {
        DEBUG, VERBOSE, NOTICE, WARNING
    }
}