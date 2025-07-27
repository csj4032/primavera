package com.genius.primavera.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("vault")
public class VaultSecretService {

    private final VaultTemplate vaultTemplate;

    public Optional<Map<String, Object>> getSecret(String path) {
        try {
            log.debug("Vault에서 시크릿 조회 시작: {}", path);
            VaultResponse response = vaultTemplate.read(path);
            
            if (response == null || response.getData() == null) {
                log.warn("시크릿이 존재하지 않습니다: {}", path);
                return Optional.empty();
            }
            
            log.info("시크릿 조회 성공: {} (키 개수: {})", path, response.getData().size());
            return Optional.of(response.getData());
            
        } catch (Exception e) {
            log.error("시크릿 조회 실패: {}", path, e);
            return Optional.empty();
        }
    }

    public Optional<String> getSecretValue(String path, String key) {
        return getSecret(path)
                .map(data -> data.get(key))
                .map(String::valueOf);
    }

    public Optional<DatabaseCredentials> getDatabaseCredentials(String environment) {
        String path = String.format("secret/data/primavera/chap04/%s", environment);
        
        return getSecret(path).map(data -> {
            String url = (String) data.get("datasource.url");
            String username = (String) data.get("datasource.username");
            String password = (String) data.get("datasource.password");
            
            return new DatabaseCredentials(url, username, password);
        });
    }

    public Optional<String> getApiKey(String apiName) {
        String key = String.format("external.api.%s", apiName);
        return getSecretValue("secret/data/primavera/chap04", key);
    }

    public Optional<String> getJwtSecret() {
        return getSecretValue("secret/data/primavera/chap04", "security.jwt.secret");
    }

    public boolean storeSecret(String path, Map<String, String> secrets) {
        try {
            log.info("시크릿 저장 시작: {} (키 개수: {})", path, secrets.size());
            vaultTemplate.write(path, secrets);
            log.info("시크릿 저장 완료: {}", path);
            return true;
            
        } catch (Exception e) {
            log.error("시크릿 저장 실패: {}", path, e);
            return false;
        }
    }

    public record DatabaseCredentials(String url, String username, String password) {
        
        public DatabaseCredentials {
            if (url == null || username == null || password == null) {
                throw new IllegalArgumentException("데이터베이스 연결 정보는 null일 수 없습니다");
            }
        }
        
        public String toMaskedString() {
            return String.format("DatabaseCredentials{url='%s', username='%s', password='***'}", 
                    maskUrl(url), maskUsername(username));
        }
        
        private String maskUrl(String url) {
            return url.replaceAll("(password=)[^&]*", "$1***");
        }
        
        private String maskUsername(String username) {
            if (username.length() <= 2) return "***";
            return username.substring(0, 2) + "***";
        }
    }
}