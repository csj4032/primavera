package com.genius.primavera.vault;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vault API를 직접 호출하여 설정값을 가져오는 테스트
 */
@Slf4j
@Disabled
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VaultDirectConfigurationTest {

    @Value("${spring.cloud.vault.token}")
    private String vaultToken;

    @Value("${spring.cloud.vault.host}")
    private String vaultHost;

    @Value("${spring.cloud.vault.port}")
    private String vaultPort;

    private static final String SECRET_PATH = "/v1/secret/data/DataAccessApplication/test";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Order(1)
    @DisplayName("Vault 서버 상태 확인")
    public void vaultServerHealthCheck() throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity(String.format("http://%s:%s/v1/sys/health", vaultHost, vaultPort), String.class);
        log.info("Vault Health Response: {}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode healthNode = objectMapper.readTree(response.getBody());
        assertThat(healthNode.get("initialized").asBoolean()).isTrue();
        assertThat(healthNode.get("sealed").asBoolean()).isFalse();
    }

    @Test
    @Order(2)
    @DisplayName("Vault Token 유효성 확인")
    public void vaultTokenValidation() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(String.format("http://%s:%s/v1/auth/token/lookup-self", vaultHost, vaultPort), HttpMethod.GET, entity, String.class);
        log.info("Token Validation Response: {}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode tokenNode = objectMapper.readTree(response.getBody());
        JsonNode dataNode = tokenNode.get("data");
        assertThat(dataNode.get("policies").toString()).contains("primavera-app-read");
    }

    @Test
    @Order(3)
    @DisplayName("DataAccessApplication/test 시크릿 직접 조회")
    public void directVaultSecretAccess() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(String.format("http://%s:%s/%s", vaultHost, vaultPort, SECRET_PATH), HttpMethod.GET, entity, String.class);
        log.info("Secret Response: {}", response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode responseNode = objectMapper.readTree(response.getBody());
        JsonNode dataNode = responseNode.get("data").get("data");
        String driverClassName = dataNode.get("spring.datasource.driver-class-name").asText();
        String url = dataNode.get("spring.datasource.url").asText();
        String username = dataNode.get("spring.datasource.username").asText();
        String password = dataNode.get("spring.datasource.password").asText();
        assertThat(driverClassName).isEqualTo("org.mariadb.jdbc.Driver");
        assertThat(url).isEqualTo("jdbc:tc:mariadb:11.4.7:///primavera_basic");
        assertThat(username).isEqualTo("test");
        assertThat(password).isEqualTo("test");
        log.info("✅ Vault 직접 접근으로 올바른 TestContainers 설정을 확인했습니다!");
    }
}