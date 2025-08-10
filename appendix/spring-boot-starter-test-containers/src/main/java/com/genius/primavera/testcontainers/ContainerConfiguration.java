package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@ConfigurationProperties(prefix = "testcontainers")
@Validated
public class ContainerConfiguration {
    
    @Valid
    private GlobalDefaults defaults = new GlobalDefaults();
    
    @NotNull
    @Valid
    private Map<String, ContainerInstanceConfig> containers = new HashMap<>();
    
    public Optional<ContainerInstanceConfig> getContainerConfig(String name) {
        return Optional.ofNullable(containers.get(name));
    }
    
    @Data
    @Validated
    public static class GlobalDefaults {
        
        @Min(value = 10, message = "Global startup timeout must be at least 10 seconds")
        @Max(value = 600, message = "Global startup timeout must not exceed 600 seconds")
        private Integer startupTimeout = 60;
        
        private Map<String, String> environment = new HashMap<>();
        
        private ImagePullPolicy imagePullPolicy = ImagePullPolicy.IF_NOT_PRESENT;
        
        private NetworkMode networkMode = NetworkMode.BRIDGE;
    }
    
    @Data
    @Validated
    public static class ContainerInstanceConfig {
        
        @NotNull(message = "Container type is required")
        private ContainerType type;
        
        @Valid
        private MariaDbContainerSpec mariadb;
        
        @Valid
        private MySqlContainerSpec mysql;
        
        @Valid
        private PostgreSqlContainerSpec postgresql;
        
        @Valid
        private RedisContainerSpec redis;
        
        @Valid
        private MongoContainerSpec mongodb;
        
        @Valid
        private BaseContainerSpec kafka;
        
        @Valid
        private BaseContainerSpec elasticsearch;
        
        @Valid
        private BaseContainerSpec vault;
        
        @Valid
        private LocalStackContainerSpec localstack;
        
        public BaseContainerSpec getSpecForType() {
            return switch (type) {
                case MARIADB -> mariadb;
                case MYSQL -> mysql;
                case POSTGRESQL -> postgresql;
                case REDIS -> redis;
                case MONGODB -> mongodb;
                case KAFKA -> kafka;
                case ELASTICSEARCH -> elasticsearch;
                case VAULT -> vault;
                case LOCALSTACK -> localstack;
                default -> null;
            };
        }
        
        public boolean hasSpecForType() {
            return getSpecForType() != null;
        }
    }
    
    public enum ImagePullPolicy {
        IF_NOT_PRESENT,
        ALWAYS
    }
    
    public enum NetworkMode {
        HOST,
        BRIDGE
    }
}
