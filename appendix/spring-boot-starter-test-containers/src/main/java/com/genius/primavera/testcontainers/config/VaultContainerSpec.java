package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties
@EqualsAndHashCode(callSuper = true)
public class VaultContainerSpec extends BaseContainerSpec {

    @NotBlank(message = "Root token cannot be blank")
    @Size(min = 8, message = "Root token must be at least 8 characters")
    private String rootToken = "primavera-vault-token";

    @Pattern(regexp = "^[0-9]+\\.[0-9]+$", message = "Invalid Vault version format")
    private String vaultVersion = "1.15";

    private Boolean devMode = true;

    private String listenAddress = "0.0.0.0:8200";

    private String uiEnabled = "true";

    private String apiAddr = "http://0.0.0.0:8200";

    @NotNull
    private Map<@NotBlank String, @NotNull String> secretsEngines = new HashMap<>();

    @NotNull
    private Map<@NotBlank String, @NotNull Object> secrets = new HashMap<>();

    @NotNull
    private List<@NotBlank String> policies = new ArrayList<>();

    private StorageBackend storageBackend = StorageBackend.INMEM;

    private Boolean tlsDisable = true;

    @Min(value = 1, message = "Max lease TTL must be at least 1 second")
    private Integer maxLeaseTtl = 768;

    @Min(value = 1, message = "Default lease TTL must be at least 1 second")
    private Integer defaultLeaseTtl = 768;

    private String clusterAddr = "http://0.0.0.0:8201";

    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    public enum StorageBackend {
        INMEM,
        FILE,
        CONSUL,
        ETCD,
        S3,
        MYSQL,
        POSTGRESQL
    }
}