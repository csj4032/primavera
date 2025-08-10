package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import com.genius.primavera.testcontainers.factory.KafkaContainerCreator;
import com.genius.primavera.testcontainers.factory.VaultContainerCreator;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategyRegistry;
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
@Validated
@ConfigurationProperties(prefix = "testcontainers")
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
        private MariaDBContainerSpec mariadb;
        
        @Valid
        private MySqlContainerSpec mysql;
        
        @Valid
        private PostgreSqlContainerSpec postgresql;
        
        @Valid
        private RedisContainerSpec redis;
        
        @Valid
        private MongoContainerSpec mongodb;
        
        @Valid
        private KafkaContainerSpec kafka;
        
        @Valid
        private ElasticsearchContainerSpec elasticsearch;
        
        @Valid
        private VaultContainerSpec vault;
        
        @Valid
        private LocalStackContainerSpec localstack;
        
        public BaseContainerSpec getSpecForType() {
            return ContainerTypeStrategyRegistry.getStrategy(type)
                .map(strategy -> strategy.getSpecFromConfiguration(this))
                .orElse(null);
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
