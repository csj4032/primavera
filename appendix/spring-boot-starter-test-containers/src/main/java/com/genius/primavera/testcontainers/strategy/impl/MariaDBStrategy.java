package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.MariaDBContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * MariaDB-specific strategy implementation
 */
public class MariaDBStrategy implements ContainerTypeStrategy {
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MARIADB;
    }
    
    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        if (spec instanceof MariaDBContainerSpec mariaDbSpec) {
            if (mariaDbSpec.getDatabase() == null) mariaDbSpec.setDatabase("primavera");
            if (mariaDbSpec.getUsername() == null) mariaDbSpec.setUsername("primavera");
            if (mariaDbSpec.getPassword() == null) mariaDbSpec.setPassword("primavera");
            if (mariaDbSpec.getRootPassword() == null) mariaDbSpec.setRootPassword("root");
        }
    }
    
    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) {
            return instanceConfig.getMariadb();
        }
        return null;
    }
    
    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        // MariaDB is handled as SQL database in the main configuration logic
        // No additional specific properties needed beyond SQL database properties
    }
    
    @Override
    public BaseContainerSpec createDefaultSpec() {
        MariaDBContainerSpec spec = new MariaDBContainerSpec();
        spec.setImage(ContainerType.MARIADB.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}