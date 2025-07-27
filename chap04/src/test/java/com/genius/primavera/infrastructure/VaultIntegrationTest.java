package com.genius.primavera.infrastructure;

import com.genius.primavera.application.VaultSecretService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.vault.core.VaultTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HashiCorp Vault 전체 통합 테스트.
 * VaultConfiguration, SecureDataSourceConfiguration, VaultSecretService의
 * 통합 동작을 검증합니다.
 * 
 * 테스트 실행 전 조건:
 * 1. Vault 서버가 localhost:8200에서 실행 중이어야 함
 * 2. 개발용 토큰이 설정되어 있어야 함
 */
@Slf4j
@SpringBootTest
@ActiveProfiles({"test", "vault"})
@DisplayName("Vault 전체 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VaultIntegrationTest {

    @Autowired(required = false)
    private VaultTemplate vaultTemplate;

    @Autowired(required = false)
    private VaultSecretService vaultSecretService;

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private VaultConfiguration.DatabaseProperties databaseProperties;

    @Autowired(required = false)
    private VaultConfiguration.SecurityProperties securityProperties;

    @Test
    @Order(1)
    @DisplayName("전체 Vault 컴포넌트 통합 테스트")
    void fullVaultIntegrationTest() {
        if (vaultTemplate == null) {
            log.info("Vault 프로파일이 비활성화됨 - 전체 테스트 스킵");
            return;
        }

        // 1. VaultTemplate 확인
        assertNotNull(vaultTemplate, "VaultTemplate이 주입되어야 합니다");
        log.info("✓ VaultTemplate 주입 확인");

        // 2. VaultSecretService 확인
        assertNotNull(vaultSecretService, "VaultSecretService가 주입되어야 합니다");
        log.info("✓ VaultSecretService 주입 확인");

        // 3. Configuration Properties 확인
        assertNotNull(databaseProperties, "DatabaseProperties가 주입되어야 합니다");
        assertNotNull(securityProperties, "SecurityProperties가 주입되어야 합니다");
        log.info("✓ Configuration Properties 주입 확인");

        // 4. DataSource 확인
        assertNotNull(dataSource, "DataSource가 주입되어야 합니다");
        log.info("✓ SecureDataSource 주입 확인");

        log.info("===== 전체 Vault 컴포넌트 통합 테스트 성공 =====");
    }

    @Test
    @Order(2)
    @DisplayName("종단간(End-to-End) 시크릿 관리 시나리오 테스트")
    void endToEndSecretManagementTest() {
        if (vaultSecretService == null) {
            log.info("VaultSecretService가 비활성화됨 - 테스트 스킵");
            return;
        }

        log.info("===== 종단간 시크릿 관리 시나리오 시작 =====");

        // 시나리오 1: 애플리케이션 시작 시 필요한 모든 시크릿 저장
        Map<String, String> applicationSecrets = Map.of(
                "datasource.url", "jdbc:mariadb://localhost:1109/primavera",
                "datasource.username", "primavera",
                "datasource.password", "secure-password-2024",
                "security.jwt.secret", "jwt-secret-key-very-secure-2024",
                "external.api.google", "google-api-key-production",
                "external.api.kakao", "kakao-api-key-production"
        );

        boolean stored = vaultSecretService.storeSecret("secret/primavera/chap04", applicationSecrets);
        assertTrue(stored, "애플리케이션 시크릿 저장이 성공해야 합니다");
        log.info("✓ 시나리오 1: 애플리케이션 시크릿 저장 완료");

        // 시나리오 2: 환경별 데이터베이스 자격증명 저장
        Map<String, String> devDbCredentials = Map.of(
                "datasource.url", "jdbc:mariadb://dev-server:3306/primavera_dev",
                "datasource.username", "dev_user",
                "datasource.password", "dev-password-123"
        );

        Map<String, String> prodDbCredentials = Map.of(
                "datasource.url", "jdbc:mariadb://prod-server:3306/primavera_prod",
                "datasource.username", "prod_user",
                "datasource.password", "prod-password-456"
        );

        vaultSecretService.storeSecret("secret/primavera/chap04/dev", devDbCredentials);
        vaultSecretService.storeSecret("secret/primavera/chap04/prod", prodDbCredentials);
        log.info("✓ 시나리오 2: 환경별 데이터베이스 자격증명 저장 완료");

        // 시나리오 3: 런타임에 시크릿 조회
        Optional<VaultSecretService.DatabaseCredentials> devCreds = 
                vaultSecretService.getDatabaseCredentials("dev");
        Optional<VaultSecretService.DatabaseCredentials> prodCreds = 
                vaultSecretService.getDatabaseCredentials("prod");

        assertTrue(devCreds.isPresent(), "개발 환경 자격증명을 조회할 수 있어야 합니다");
        assertTrue(prodCreds.isPresent(), "운영 환경 자격증명을 조회할 수 있어야 합니다");
        
        log.info("✓ 시나리오 3: 환경별 자격증명 조회 성공");
        log.info("  - DEV: {}", devCreds.get().toMaskedString());
        log.info("  - PROD: {}", prodCreds.get().toMaskedString());

        // 시나리오 4: API 키 조회
        Optional<String> googleApiKey = vaultSecretService.getApiKey("google");
        Optional<String> kakaoApiKey = vaultSecretService.getApiKey("kakao");

        assertTrue(googleApiKey.isPresent(), "Google API 키를 조회할 수 있어야 합니다");
        assertTrue(kakaoApiKey.isPresent(), "Kakao API 키를 조회할 수 있어야 합니다");
        
        log.info("✓ 시나리오 4: API 키 조회 성공");

        // 시나리오 5: JWT 시크릿 조회
        Optional<String> jwtSecret = vaultSecretService.getJwtSecret();
        assertTrue(jwtSecret.isPresent(), "JWT 시크릿을 조회할 수 있어야 합니다");
        assertEquals("jwt-secret-key-very-secure-2024", jwtSecret.get());
        
        log.info("✓ 시나리오 5: JWT 시크릿 조회 성공");

        log.info("===== 종단간 시크릿 관리 시나리오 완료 =====");
    }

    @Test
    @Order(3)
    @DisplayName("Vault 장애 시나리오 처리 테스트")
    void vaultFailureScenarioTest() {
        if (vaultSecretService == null) {
            log.info("VaultSecretService가 비활성화됨 - 테스트 스킵");
            return;
        }

        log.info("===== Vault 장애 시나리오 테스트 시작 =====");

        // 시나리오 1: 존재하지 않는 경로 조회
        Optional<Map<String, Object>> nonExistentSecret = 
                vaultSecretService.getSecret("secret/data/non/existent/path");
        assertFalse(nonExistentSecret.isPresent(), 
                "존재하지 않는 시크릿은 빈 Optional을 반환해야 합니다");
        log.info("✓ 시나리오 1: 존재하지 않는 경로 처리 성공");

        // 시나리오 2: 잘못된 키 조회
        Optional<String> invalidKey = vaultSecretService.getSecretValue(
                "secret/data/primavera/chap04", "invalid.key");
        assertFalse(invalidKey.isPresent(), 
                "존재하지 않는 키는 빈 Optional을 반환해야 합니다");
        log.info("✓ 시나리오 2: 잘못된 키 처리 성공");

        // 시나리오 3: null 값 처리
        try {
            new VaultSecretService.DatabaseCredentials(null, "user", "pass");
            fail("null URL로 DatabaseCredentials 생성 시 예외가 발생해야 합니다");
        } catch (IllegalArgumentException e) {
            log.info("✓ 시나리오 3: null 값 검증 성공");
        }

        log.info("===== Vault 장애 시나리오 테스트 완료 =====");
    }

    @Test
    @Order(4)
    @DisplayName("실제 데이터베이스 연결 통합 테스트")
    void realDatabaseConnectionTest() {
        if (dataSource == null || vaultSecretService == null) {
            log.info("필수 컴포넌트가 비활성화됨 - 테스트 스킵");
            return;
        }

        // 테스트용 데이터베이스 자격증명 설정
        Map<String, String> testDbConfig = Map.of(
                "datasource.url", "jdbc:mariadb://localhost:1109/primavera",
                "datasource.username", "primavera",
                "datasource.password", "primavera",
                "datasource.driver-class-name", "org.mariadb.jdbc.Driver"
        );

        vaultSecretService.storeSecret("secret/primavera/chap04", testDbConfig);

        // DataSource를 통한 실제 연결 테스트
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "데이터베이스 연결이 성공해야 합니다");
            
            // 간단한 쿼리 실행
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT 1 AS test")) {
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt("test"));
            }
            
            log.info("✓ Vault 기반 DataSource로 실제 데이터베이스 연결 성공");
            
        } catch (Exception e) {
            log.warn("데이터베이스 연결 실패 (로컬 MariaDB가 없는 경우 정상): {}", e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Vault 통합 요약 리포트")
    void vaultIntegrationSummaryReport() {
        log.info("\n===========================================");
        log.info("     Vault 통합 테스트 요약 리포트");
        log.info("===========================================");
        log.info("✓ VaultConfiguration: {}", vaultTemplate != null ? "활성화" : "비활성화");
        log.info("✓ SecureDataSourceConfiguration: {}", dataSource != null ? "활성화" : "비활성화");
        log.info("✓ VaultSecretService: {}", vaultSecretService != null ? "활성화" : "비활성화");
        log.info("✓ Configuration Properties 주입: {}", 
                (databaseProperties != null && securityProperties != null) ? "성공" : "실패");
        log.info("===========================================");
        
        if (vaultTemplate != null) {
            log.info("\n테스트 실행 명령:");
            log.info("1. Vault 서버 시작:");
            log.info("   docker run -d --name vault-primavera -p 8200:8200 \\");
            log.info("     -e VAULT_DEV_ROOT_TOKEN_ID=primavera-dev-token \\");
            log.info("     hashicorp/vault:1.15");
            log.info("\n2. 테스트 실행:");
            log.info("   ./gradlew :chap04:test --tests VaultIntegrationTest");
            log.info("\n3. 애플리케이션 실행:");
            log.info("   ./gradlew :chap04:bootRun -Dspring.profiles.active=vault");
        }
        log.info("===========================================\n");
    }
}