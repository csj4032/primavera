package com.genius.primavera.vault;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vault에서 설정한 값이 application-test.yml 설정으로 올바르게 로드되는지 확인하는 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VaultConfigurationValidationTest {

    @Value("${spring.cloud.vault.token}")
    private String vaultToken;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Test
    @Order(1)
    @DisplayName("Vault Token이 올바르게 설정되었는지 확인")
    public void vaultTokenIsValid() {
        log.info("VAULT TOKEN: {}", vaultToken);
        assertThat(vaultToken).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("Vault에서 설정한 값이 application-test.yml로 올바르게 로드되는지 확인")
    public void vaultConfigurationIsLoaded() {
        log.info("Driver Class Name: {}", driverClassName);
        log.info("URL: {}", url);
        log.info("Username: {}", username);
        log.info("Password: {}", password);
        log.info("Vault에서 설정값을 정상적으로 가져옴");
        Assertions.assertEquals("org.mariadb.jdbc.Driver", driverClassName);
        Assertions.assertEquals("jdbc:tc:mariadb:11.4.7:///primavera_basic", url);
        Assertions.assertEquals("test", username);
        Assertions.assertEquals("test", password);
    }
}