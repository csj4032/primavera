package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * chap16 배치 처리 통합 테스트 추상 클래스
 * 
 * 특징:
 * - MariaDB 11.4 컨테이너 (배치 작업 데이터)
 * - 파일 업로드/다운로드 테스트 지원
 * - 배치 작업 실행 환경 제공
 * - JUnit 5 PER_CLASS + CONCURRENT 지원
 * 
 * 사용법:
 * ```java
 * class FileProcessingBatchTest extends AbstractIntegrationTest {
 *     @Test
 *     void shouldProcessExcelFile() {
 *         // 파일 처리 배치 작업 테스트
 *     }
 * }
 * ```
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public abstract class AbstractIntegrationTest {
    
    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init-test.sql");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MariaDB 설정
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
        
        // 배치 처리 관련 설정
        registry.add("spring.batch.jdbc.initialize-schema", () -> "always");
        registry.add("primavera.batch.chunk-size", () -> "100");
        registry.add("primavera.file.upload-path", () -> "/tmp/test-uploads");
        registry.add("primavera.file.download-path", () -> "/tmp/test-downloads");
        
        log.info("🐳 MariaDB 배치 테스트 컨테이너 설정 완료: {}", mariadb.getJdbcUrl());
        log.info("📂 파일 처리 테스트 경로 설정 완료");
    }
}