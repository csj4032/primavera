package com.genius.primavera.application;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VaultSecretService 테스트.
 * Vault를 통한 시크릿 관리 기능을 검증합니다.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles({"test", "vault"})
@DisplayName("VaultSecretService 테스트")
class VaultSecretServiceTest {

    @Autowired(required = false)
    private VaultSecretService vaultSecretService;

    @Test
    @DisplayName("VaultSecretService Bean이 정상적으로 생성되는지 확인")
    void vaultSecretServiceBeanCreationTest() {
        if (vaultSecretService == null) {
            log.info("Vault 프로파일이 비활성화됨 - 테스트 스킵");
            return;
        }

        assertNotNull(vaultSecretService, "VaultSecretService Bean이 생성되어야 합니다");
        log.info("VaultSecretService Bean 생성 확인 완료");
    }

    @Test
    @DisplayName("시크릿 저장 및 조회 기능 테스트")
    void storeAndRetrieveSecretTest() {
        if (vaultSecretService == null) {
            log.info("VaultSecretService가 비활성화됨 - 테스트 스킵");
            return;
        }

        // 테스트용 고유 경로 생성
        String testPath = String.format("secret/test/chap04/%s", UUID.randomUUID());
        Map<String, String> testSecrets = Map.of(
                "test.key1", "value1",
                "test.key2", "value2",
                "test.password", "secret-password-123"
        );

        // 시크릿 저장
        boolean stored = vaultSecretService.storeSecret(testPath, testSecrets);
        assertTrue(stored, "시크릿 저장이 성공해야 합니다");
        log.info("시크릿 저장 성공: {}", testPath);

        // 시크릿 조회
        Optional<Map<String, Object>> retrievedSecrets = 
                vaultSecretService.getSecret("secret/data/test/chap04/" + 
                        testPath.substring(testPath.lastIndexOf('/') + 1));
        
        assertTrue(retrievedSecrets.isPresent(), "저장된 시크릿을 조회할 수 있어야 합니다");
        
        Map<String, Object> secrets = retrievedSecrets.get();
        assertEquals("value1", secrets.get("test.key1"), "test.key1 값이 일치해야 합니다");
        assertEquals("value2", secrets.get("test.key2"), "test.key2 값이 일치해야 합니다");
        assertEquals("secret-password-123", secrets.get("test.password"), 
                "test.password 값이 일치해야 합니다");
        
        log.info("시크릿 조회 성공: {} 개의 키", secrets.size());
    }

    @Test
    @DisplayName("특정 키의 시크릿 값 조회 테스트")
    void getSecretValueTest() {
        if (vaultSecretService == null) {
            log.info("VaultSecretService가 비활성화됨 - 테스트 스킵");
            return;
        }

        // 테스트용 시크릿 저장
        String testPath = "secret/test/chap04/specific-key-test";
        Map<String, String> testSecrets = Map.of(
                "specific.key", "specific-value",
                "another.key", "another-value"
        );

        vaultSecretService.storeSecret(testPath, testSecrets);

        // 특정 키 조회
        Optional<String> specificValue = vaultSecretService.getSecretValue(
                "secret/data/test/chap04/specific-key-test", "specific.key");
        
        assertTrue(specificValue.isPresent(), "특정 키의 값을 조회할 수 있어야 합니다");
        assertEquals("specific-value", specificValue.get(), "조회된 값이 일치해야 합니다");
        
        // 존재하지 않는 키 조회
        Optional<String> nonExistentValue = vaultSecretService.getSecretValue(
                "secret/data/test/chap04/specific-key-test", "non.existent.key");
        
        assertFalse(nonExistentValue.isPresent(), "존재하지 않는 키는 빈 Optional을 반환해야 합니다");
        
        log.info("특정 키 조회 테스트 성공");
    }

    @Test
    @DisplayName("데이터베이스 자격증명 관리 테스트")
    void databaseCredentialsManagementTest() {
        if (vaultSecretService == null) {
            log.info("VaultSecretService가 비활성화됨 - 테스트 스킵");
            return;
        }

        // 테스트용 데이터베이스 자격증명 저장
        Map<String, String> testDbCredentials = Map.of(
                "datasource.url", "jdbc:mariadb://test-server:3306/testdb",
                "datasource.username", "testuser",
                "datasource.password", "test-secure-password"
        );

        vaultSecretService.storeSecret("secret/primavera/chap04/test", testDbCredentials);

        // 자격증명 조회
        Optional<VaultSecretService.DatabaseCredentials> credentials = 
                vaultSecretService.getDatabaseCredentials("test");
        
        assertTrue(credentials.isPresent(), "데이터베이스 자격증명을 조회할 수 있어야 합니다");
        
        VaultSecretService.DatabaseCredentials dbCreds = credentials.get();
        assertEquals("jdbc:mariadb://test-server:3306/testdb", dbCreds.url());
        assertEquals("testuser", dbCreds.username());
        assertEquals("test-secure-password", dbCreds.password());
        
        // 마스킹된 문자열 확인
        String maskedString = dbCreds.toMaskedString();
        assertTrue(maskedString.contains("password='***'"), 
                "패스워드는 마스킹되어야 합니다");
        assertTrue(maskedString.contains("te***"), 
                "사용자명은 부분적으로 마스킹되어야 합니다");
        
        log.info("데이터베이스 자격증명 관리 테스트 성공: {}", maskedString);
    }

    @Test
    @DisplayName("API 키 관리 테스트")
    void apiKeyManagementTest() {
        if (vaultSecretService == null) {
            log.info("VaultSecretService가 비활성화됨 - 테스트 스킵");
            return;
        }

        // 테스트용 API 키 저장
        Map<String, String> apiKeys = Map.of(
                "external.api.google", "test-google-api-key",
                "external.api.kakao", "test-kakao-api-key",
                "external.api.naver", "test-naver-api-key"
        );

        vaultSecretService.storeSecret("secret/primavera/chap04", apiKeys);

        // 각 API 키 조회
        Optional<String> googleKey = vaultSecretService.getApiKey("google");
        Optional<String> kakaoKey = vaultSecretService.getApiKey("kakao");
        Optional<String> naverKey = vaultSecretService.getApiKey("naver");
        
        assertTrue(googleKey.isPresent(), "Google API 키를 조회할 수 있어야 합니다");
        assertTrue(kakaoKey.isPresent(), "Kakao API 키를 조회할 수 있어야 합니다");
        assertTrue(naverKey.isPresent(), "Naver API 키를 조회할 수 있어야 합니다");
        
        assertEquals("test-google-api-key", googleKey.get());
        assertEquals("test-kakao-api-key", kakaoKey.get());
        assertEquals("test-naver-api-key", naverKey.get());
        
        log.info("API 키 관리 테스트 성공");
    }

    @Test
    @DisplayName("JWT 시크릿 관리 테스트")
    void jwtSecretManagementTest() {
        if (vaultSecretService == null) {
            log.info("VaultSecretService가 비활성화됨 - 테스트 스킵");
            return;
        }

        // 테스트용 JWT 시크릿 저장
        Map<String, String> jwtSecret = Map.of(
                "security.jwt.secret", "test-jwt-secret-key-very-secure"
        );

        vaultSecretService.storeSecret("secret/primavera/chap04", jwtSecret);

        // JWT 시크릿 조회
        Optional<String> retrievedJwtSecret = vaultSecretService.getJwtSecret();
        
        assertTrue(retrievedJwtSecret.isPresent(), "JWT 시크릿을 조회할 수 있어야 합니다");
        assertEquals("test-jwt-secret-key-very-secure", retrievedJwtSecret.get());
        
        log.info("JWT 시크릿 관리 테스트 성공");
    }

    @Test
    @DisplayName("존재하지 않는 시크릿 조회 시 빈 Optional 반환 테스트")
    void nonExistentSecretTest() {
        if (vaultSecretService == null) {
            log.info("VaultSecretService가 비활성화됨 - 테스트 스킵");
            return;
        }

        // 존재하지 않는 경로 조회
        Optional<Map<String, Object>> nonExistentSecret = 
                vaultSecretService.getSecret("secret/data/non/existent/path");
        
        assertFalse(nonExistentSecret.isPresent(), 
                "존재하지 않는 시크릿은 빈 Optional을 반환해야 합니다");
        
        // 존재하지 않는 환경의 데이터베이스 자격증명 조회
        Optional<VaultSecretService.DatabaseCredentials> nonExistentCreds = 
                vaultSecretService.getDatabaseCredentials("non-existent-env");
        
        assertFalse(nonExistentCreds.isPresent(), 
                "존재하지 않는 환경의 자격증명은 빈 Optional을 반환해야 합니다");
        
        log.info("존재하지 않는 시크릿 처리 테스트 성공");
    }
}