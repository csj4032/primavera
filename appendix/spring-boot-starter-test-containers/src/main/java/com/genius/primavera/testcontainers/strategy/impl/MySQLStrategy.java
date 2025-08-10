package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.MySqlContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * MySQL-specific strategy implementation
 */
public class MySQLStrategy implements ContainerTypeStrategy {
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MYSQL;
    }
    
    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        if (spec instanceof MySqlContainerSpec mysqlSpec) {
            if (mysqlSpec.getDatabase() == null) mysqlSpec.setDatabase("primavera");
            if (mysqlSpec.getUsername() == null) mysqlSpec.setUsername("primavera");
            if (mysqlSpec.getPassword() == null) mysqlSpec.setPassword("primavera");
            if (mysqlSpec.getRootPassword() == null) mysqlSpec.setRootPassword("root");
        }
    }
    
    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) {
            return instanceConfig.getMysql();
        }
        return null;
    }
    
    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        // MySQL is handled as SQL database in the main configuration logic
    }
    
    @Override
    public BaseContainerSpec createDefaultSpec() {
        MySqlContainerSpec spec = new MySqlContainerSpec();
        spec.setImage(ContainerType.MYSQL.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}