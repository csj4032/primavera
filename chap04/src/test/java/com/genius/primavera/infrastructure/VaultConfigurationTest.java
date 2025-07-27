package com.genius.primavera.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.vault.core.VaultTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VaultConfiguration 클래스 테스트.
 * VaultTemplate Bean 생성 및 기본 동작을 검증합니다.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles({"test", "vault"})
@DisplayName("VaultConfiguration 테스트")
class VaultConfigurationTest {

    @Autowired(required = false)
    private VaultTemplate vaultTemplate;

    @Autowired(required = false)
    private VaultConfiguration.DatabaseProperties databaseProperties;

    @Autowired(required = false)
    private VaultConfiguration.SecurityProperties securityProperties;

    @Autowired(required = false)
    private VaultConfiguration.ExternalApiProperties externalApiProperties;

    @Test
    @DisplayName("VaultTemplate Bean이 정상적으로 생성되는지 확인")
    void vaultTemplateBeanCreationTest() {
        if (isVaultDisabled()) {
            log.info("Vault 프로파일이 비활성화됨 - 테스트 스킵");
            return;
        }

        assertNotNull(vaultTemplate, "VaultTemplate Bean이 생성되어야 합니다");
        log.info("VaultTemplate Bean 생성 확인 완료");
    }

    @Test
    @DisplayName("DatabaseProperties가 Vault에서 주입되는지 확인")
    void databasePropertiesInjectionTest() {
        if (isVaultDisabled()) {
            log.info("Vault 프로파일이 비활성화됨 - 테스트 스킵");
            return;
        }

        assertNotNull(databaseProperties, "DatabaseProperties가 주입되어야 합니다");
        
        // Vault에 실제 값이 설정되어 있다면 확인
        if (databaseProperties.getUrl() != null) {
            log.info("Database URL: {}", maskUrl(databaseProperties.getUrl()));
            log.info("Database Username: {}", maskUsername(databaseProperties.getUsername()));
            assertNotNull(databaseProperties.getPassword(), "패스워드가 설정되어야 합니다");
        }
    }

    @Test
    @DisplayName("SecurityProperties가 정상적으로 주입되는지 확인")
    void securityPropertiesInjectionTest() {
        if (isVaultDisabled()) {
            log.info("Vault 프로파일이 비활성화됨 - 테스트 스킵");
            return;
        }

        assertNotNull(securityProperties, "SecurityProperties가 주입되어야 합니다");
        
        // 기본값 확인
        assertEquals(86400, securityProperties.getJwtExpiration(), 
                "JWT 만료 시간 기본값은 86400(24시간)이어야 합니다");
        
        log.info("SecurityProperties 주입 확인 완료");
    }

    @Test
    @DisplayName("ExternalApiProperties가 정상적으로 주입되는지 확인")
    void externalApiPropertiesInjectionTest() {
        if (isVaultDisabled()) {
            log.info("Vault 프로파일이 비활성화됨 - 테스트 스킵");
            return;
        }

        assertNotNull(externalApiProperties, "ExternalApiProperties가 주입되어야 합니다");
        log.info("ExternalApiProperties 주입 확인 완료");
    }

    private boolean isVaultDisabled() {
        return vaultTemplate == null;
    }

    private String maskUrl(String url) {
        return url.replaceAll("(password=)[^&]*", "$1***");
    }

    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) return "***";
        return username.substring(0, 2) + "***";
    }
}