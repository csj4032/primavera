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
public class MongoContainerSpec extends BaseContainerSpec {

    @NotBlank(message = "Database name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "Invalid MongoDB database name")
    private String database = "primavera";

    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    private String username = "primavera";

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password = "primavera";

    @NotBlank(message = "Auth database cannot be blank")
    private String authDatabase = "admin";

    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "Invalid replica set name")
    private String replicaSetName;

    private Boolean shardingEnabled = false;

    @Min(value = 1024, message = "Port must be at least 1024")
    @Max(value = 65535, message = "Port must not exceed 65535")
    private Integer port = 27017;

    @Min(value = 256, message = "Cache size must be at least 256MB")
    private Integer wiredTigerCacheSizeMB;

    private Boolean journalEnabled = true;

    @Min(value = 100, message = "OpLog size must be at least 100MB")
    private Integer oplogSizeMB;

    private Boolean indexBuildInBackground = true;

    private StorageEngine storageEngine = StorageEngine.WIRED_TIGER;

    private AuthMechanism authMechanism = AuthMechanism.SCRAM_SHA_256;

    public enum StorageEngine {
        WIRED_TIGER,
        IN_MEMORY,
        MMAPV1
    }

    public enum AuthMechanism {
        SCRAM_SHA_256
    }
}