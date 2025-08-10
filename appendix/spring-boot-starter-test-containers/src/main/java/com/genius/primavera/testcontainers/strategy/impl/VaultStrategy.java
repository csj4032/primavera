package com.genius.primavera.testcontainers.strategy.impl;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.VaultContainerSpec;
import com.genius.primavera.testcontainers.strategy.ContainerTypeStrategy;
import com.genius.primavera.testcontainers.ContainerConfiguration;

import java.util.Map;

/**
 * Vault-specific strategy implementation
 */
public class VaultStrategy implements ContainerTypeStrategy {
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.VAULT;
    }
    
    @Override
    public void applyDefaults(BaseContainerSpec spec) {
        // VaultContainerSpec defaults are handled in the spec itself
    }
    
    @Override
    public BaseContainerSpec getSpecFromConfiguration(Object config) {
        if (config instanceof ContainerConfiguration.ContainerInstanceConfig instanceConfig) {
            return instanceConfig.getVault();
        }
        return null;
    }
    
    @Override
    public void configureSpecificProperties(ContainerInfo containerInfo, Map<String, Object> properties) {
        // Vault-specific properties would be configured here if needed
    }
    
    @Override
    public BaseContainerSpec createDefaultSpec() {
        VaultContainerSpec spec = new VaultContainerSpec();
        spec.setImage(ContainerType.VAULT.getDefaultImage());
        applyDefaults(spec);
        return spec;
    }
}