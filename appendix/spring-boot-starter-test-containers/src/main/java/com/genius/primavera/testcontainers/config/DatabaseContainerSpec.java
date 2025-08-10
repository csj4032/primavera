package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Validated
public class DatabaseContainerSpec extends BaseContainerSpec {
    
    @NotBlank(message = "Database name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Invalid database name")
    private String database = "primavera";
    
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    private String username = "primavera";
    
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password = "primavera";
    
    @Pattern(regexp = "^(classpath:|file:|http://|https://)?.*\\.(sql|sh)$", message = "Init script must be .sql or .sh file")
    private String initScript;
    
    @Min(value = 1, message = "Max connections must be at least 1")
    @Max(value = 100, message = "Max connections must not exceed 100")
    private Integer maxConnections = 10;
    
    @Min(value = 1000, message = "Connection timeout must be at least 1000ms")
    private Integer connectionTimeout = 30000;
    
    private IsolationLevel isolationLevel = IsolationLevel.READ_COMMITTED;
    
    private Boolean autoCommit = true;
    
    public enum IsolationLevel {
        READ_COMMITTED,
        SERIALIZABLE
    }
}