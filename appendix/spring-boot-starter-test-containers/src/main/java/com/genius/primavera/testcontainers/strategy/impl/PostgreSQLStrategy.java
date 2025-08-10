package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.PostgreSqlContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * PostgreSQL-specific strategy implementation
 */
public class PostgreSQLStrategy implements ContainerTypeStrategy {
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.POSTGRESQL;
    }
    
    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        if (spec instanceof PostgreSqlContainerSpec pgSpec) {
            if (pgSpec.getDatabase() == null) pgSpec.setDatabase("primavera");
            if (pgSpec.getUsername() == null) pgSpec.setUsername("primavera");
            if (pgSpec.getPassword() == null) pgSpec.setPassword("primavera");
        }
    }
    
    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) {
            return instanceConfig.getPostgresql();
        }
        return null;
    }
    
    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        // PostgreSQL is handled as SQL database in the main configuration logic
    }
    
    @Override
    public BaseContainerSpec createDefaultSpec() {
        PostgreSqlContainerSpec spec = new PostgreSqlContainerSpec();
        spec.setImage(ContainerType.POSTGRESQL.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}