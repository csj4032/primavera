package com.genius.primavera.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.vault.config.SecretBackendConfigurer;
import org.springframework.cloud.vault.config.VaultConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

@Slf4j
@Configuration
@Profile("vault")
public class VaultConfiguration implements VaultConfigurer {

    @Value("${spring.cloud.vault.host:localhost}")
    private String vaultHost;

    @Value("${spring.cloud.vault.port:8200}")
    private int vaultPort;

    @Value("${spring.cloud.vault.scheme:http}")
    private String vaultScheme;

    @Value("${spring.cloud.vault.token:}")
    private String vaultToken;

    @Bean
    public VaultTemplate vaultTemplate() {
        log.info("Vault 연결 설정: {}://{}:{}", vaultScheme, vaultHost, vaultPort);
        
        VaultEndpoint endpoint = VaultEndpoint.create(vaultHost, vaultPort);
        endpoint.setScheme(vaultScheme);
        
        ClientAuthentication authentication = new TokenAuthentication(vaultToken);
        
        VaultTemplate template = new VaultTemplate(endpoint, authentication);
        log.info("VaultTemplate 초기화 완료");
        
        return template;
    }

    @Override
    public void addSecretBackends(SecretBackendConfigurer configurer) {

    }

    /**
     * 데이터베이스 연결 정보를 Vault에서 관리하는 속성 클래스
     */
    @ConfigurationProperties("datasource")
    @Component
    public static class DatabaseProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    }

    /**
     * 보안 관련 설정을 Vault에서 관리하는 속성 클래스
     */
    @ConfigurationProperties("security")
    @Component
    public static class SecurityProperties {
        private String jwtSecret;
        private long jwtExpiration = 86400; // 24시간
        private String encryptionKey;

        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }

        public long getJwtExpiration() { return jwtExpiration; }
        public void setJwtExpiration(long jwtExpiration) { this.jwtExpiration = jwtExpiration; }

        public String getEncryptionKey() { return encryptionKey; }
        public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }
    }

    /**
     * 외부 API 키를 Vault에서 관리하는 속성 클래스
     */
    @ConfigurationProperties("external.api")
    @Component
    public static class ExternalApiProperties {
        private String googleApiKey;
        private String kakaoApiKey;
        private String naverApiKey;

        public String getGoogleApiKey() { return googleApiKey; }
        public void setGoogleApiKey(String googleApiKey) { this.googleApiKey = googleApiKey; }

        public String getKakaoApiKey() { return kakaoApiKey; }
        public void setKakaoApiKey(String kakaoApiKey) { this.kakaoApiKey = kakaoApiKey; }

        public String getNaverApiKey() { return naverApiKey; }
        public void setNaverApiKey(String naverApiKey) { this.naverApiKey = naverApiKey; }
    }
}