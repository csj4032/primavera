package com.genius.primavera;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import java.util.Map;

@Slf4j
@Testcontainers
@DisplayName("Vault Access Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VaultAccessTest {

    @Container
    static final VaultContainer<?> vaultContainer = new VaultContainer<>("hashicorp/vault:1.15.4").withVaultToken("primavera-vault-token");

    @Test
    @Order(1)
    @DisplayName("Test Vault Write")
    public void testVaultWrite() throws VaultException {
        VaultConfig config = new VaultConfig().address(vaultContainer.getHttpHostAddress()).token("primavera-vault-token").engineVersion(2).build();
        Vault vault = new Vault(config);
        Map<String, Object> secrets = Map.of("username", "primavera", "password", "primavera");
        Map<String, Object> requestData = Map.of("data", secrets);
        vault.logical().write("secret/data/primavera", requestData);
        log.info("Data written to Vault");
    }

    @Test
    @Order(2)
    @DisplayName("Test Vault Read")
    public void testVaultSystemProperties() throws VaultException {
        VaultConfig config = new VaultConfig().address(vaultContainer.getHttpHostAddress()).token("primavera-vault-token").engineVersion(2).build();
        Vault vault = new Vault(config);
        LogicalResponse response = vault.logical().read("secret/data/primavera");
        Map<String, String> responseData = response.getData();
        log.info("Full response data: {}", responseData);
        Assertions.assertNotNull(responseData);
        Assertions.assertTrue(responseData.containsKey("data"), "Response data should contain 'data' key");
        String data = responseData.get("data");
        log.info("Data read from Vault: {}", data);
    }
}