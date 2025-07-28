package com.genius.primavera.vault;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vault에서 설정한 값이 application-test.yml 설정으로 올바르게 로드되는지 확인하는 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VaultConfigurationTest {

    @Autowired
    private Environment environment;

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
    @DisplayName("활성 프로필과 Vault Token 확인")
    public void vaultTokenIsValid() {
        assertThat(vaultToken).withFailMessage("Vault token is not set or is empty").isNotEmpty();
        log.info("✅ Vault Token이 올바르게 설정되었습니다: {}", vaultToken);
    }

    @Test
    @Order(2)
    @DisplayName("Vault에서 설정한 값이 application-test.yml로 올바르게 로드되는지 확인")
    public void vaultConfigurationIsLoaded() {
        assertThat(driverClassName).withFailMessage("Driver class name mismatch. Expected: org.mariadb.jdbc.Driver, Actual: %s", driverClassName).isEqualTo("org.mariadb.jdbc.Driver");
        assertThat(url).withFailMessage("URL mismatch. Expected: jdbc:tc:mariadb:11.4.7:///primavera_basic, Actual: %s", url).isEqualTo("jdbc:tc:mariadb:11.4.7:///primavera_basic");
        assertThat(username).withFailMessage("Username mismatch. Expected: test, Actual: %s", username).isEqualTo("test");
        assertThat(password).withFailMessage("Password mismatch. Expected: test, Actual: %s", password).isEqualTo("test");
        log.info("✅ Vault에서 설정값을 정상적으로 가져왔습니다!");
    }
}