package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@Slf4j
@DisplayName("Vault Access Test")
@TestMethodOrder(MethodOrderer.class)
public class VaultAccessTest {

    @Test
    @DisplayName("Test Vault Environment Variables")
    public void testVaultEnvironmentVariables() {
        String vaultHost = System.getenv("VAULT_HOST");
        String vaultPort = System.getenv("VAULT_PORT");
        String vaultToken = System.getenv("VAULT_TOKEN");
        log.info("VAULT_HOST: {}", vaultHost);
        log.info("VAULT_PORT: {}", vaultPort);
        log.info("VAULT_TOKEN: {}", vaultToken);
        assert vaultHost != null && !vaultHost.isEmpty() : "VAULT_HOST is not set";
        assert vaultPort != null && !vaultPort.isEmpty() : "VAULT_PORT is not set";
        assert vaultToken != null && !vaultToken.isEmpty() : "VAULT_TOKEN is not set";
    }
}
