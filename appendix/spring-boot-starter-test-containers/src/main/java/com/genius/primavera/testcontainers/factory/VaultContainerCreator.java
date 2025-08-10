package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.VaultContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
public class VaultContainerCreator implements ContainerCreator {

    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        log.info("Received spec type: {}", spec.getClass().getSimpleName());
        
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.VAULT.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;

        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(image))
                .withExposedPorts(8200)
                .withStartupTimeout(Duration.ofSeconds(timeout));

        if (spec instanceof VaultContainerSpec vaultSpec) {
            log.info("Using VaultContainerSpec");
            
            log.info("VaultContainerSpec details:");
            log.info("  - rootToken: {}", vaultSpec.getRootToken());
            log.info("  - vaultVersion: {}", vaultSpec.getVaultVersion());
            log.info("  - devMode: {}", vaultSpec.getDevMode());
            log.info("  - logLevel: {}", vaultSpec.getLogLevel());
            log.info("  - tlsDisable: {}", vaultSpec.getTlsDisable());
            
            // Basic Vault configuration
            container.withEnv("VAULT_DEV_ROOT_TOKEN_ID", vaultSpec.getRootToken());
            container.withEnv("VAULT_DEV_LISTEN_ADDRESS", vaultSpec.getListenAddress());
            container.withEnv("VAULT_LOG_LEVEL", vaultSpec.getLogLevel().name().toLowerCase());
            
            if (vaultSpec.getDevMode()) {
                container.withEnv("VAULT_DEV_MODE", "true");
            }
            
            if (vaultSpec.getUiEnabled() != null) {
                container.withEnv("VAULT_UI", vaultSpec.getUiEnabled());
            }
            
            if (vaultSpec.getApiAddr() != null) {
                container.withEnv("VAULT_API_ADDR", vaultSpec.getApiAddr());
            }
            
            if (vaultSpec.getClusterAddr() != null) {
                container.withEnv("VAULT_CLUSTER_ADDR", vaultSpec.getClusterAddr());
            }
            
            if (vaultSpec.getTlsDisable()) {
                container.withEnv("VAULT_TLS_DISABLE", "1");
            }
            
            if (vaultSpec.getMaxLeaseTtl() != null) {
                container.withEnv("VAULT_MAX_LEASE_TTL", vaultSpec.getMaxLeaseTtl().toString() + "h");
            }
            
            if (vaultSpec.getDefaultLeaseTtl() != null) {
                container.withEnv("VAULT_DEFAULT_LEASE_TTL", vaultSpec.getDefaultLeaseTtl().toString() + "h");
            }
            
            // Storage backend configuration
            if (vaultSpec.getStorageBackend() != null) {
                container.withEnv("VAULT_STORAGE_BACKEND", vaultSpec.getStorageBackend().name().toLowerCase());
            }
            
            // Add capability for IPC_LOCK (required for Vault)
            container.withPrivilegedMode(true);
            
        } else {
            log.info("Using default Vault configuration - spec type: {}", spec.getClass().getSimpleName());
            // Default dev mode setup
            container.withEnv("VAULT_DEV_ROOT_TOKEN_ID", "primavera-vault-token");
            container.withEnv("VAULT_DEV_LISTEN_ADDRESS", "0.0.0.0:8200");
            container.withEnv("VAULT_LOG_LEVEL", "info");
        }

        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }

        return container;
    }

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.VAULT;
    }
}