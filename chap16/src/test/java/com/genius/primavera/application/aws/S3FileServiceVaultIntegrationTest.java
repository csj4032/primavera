package com.genius.primavera.application.aws;

import com.genius.primavera.infrastructure.aws.AwsProperties;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(value = {
        @EnableTestContainers.TestContainer(type = ContainerType.LOCALSTACK, name = "localstack")
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableConfigurationProperties(AwsProperties.class)
public class S3FileServiceVaultIntegrationTest {

    @Autowired
    private S3FileService s3FileService;

    @Autowired
    private AwsProperties awsProperties;

    private static final String TEST_FILE_KEY = "vault-test/sample.txt";
    private static final String TEST_FILE_CONTENT = "Hello from Vault + S3!";

    @Test
    @Order(1)
    @DisplayName("application-test.yml configuration values verification (Vault integration scenario)")
    void verifyVaultTestConfiguration() {
        log.info("Vault + application-test.yml configuration check:");
        log.info("   Access Key: {}", awsProperties.credentials().accessKey());
        log.info("   Secret Key: {}", awsProperties.credentials().secretKey());
        log.info("   Region: {}", awsProperties.region().value());
        log.info("   Bucket: {}", awsProperties.s3().bucketName());
        log.info("   Endpoint: {}", awsProperties.s3().hasEndpoint() ? awsProperties.s3().endpoint() : "(AWS Default)");
        log.info("   Environment: Vault integration + {}", awsProperties.isLocalStack() ? "LocalStack" : "Real AWS");

        assertThat(awsProperties.credentials().accessKey()).isNotEmpty();
        assertThat(awsProperties.credentials().secretKey()).isNotEmpty();
        assertThat(awsProperties.region().value()).isNotEmpty();
        assertThat(awsProperties.s3().bucketName()).isNotEmpty();

        if (awsProperties.isLocalStack()) {
            log.info("LocalStack + application-test.yml configuration is properly set up.");
            log.info("For real Vault usage: run ../../infrastructure/vault-init.sh script.");
        } else {
            log.warn("Testing in real AWS + Vault environment.");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Vaulttest AWS Endpoint connection S3 test file")
    void uploadFileWithVaultCredentials() {
        MockMultipartFile file = new MockMultipartFile("file", "vault-test.txt", "text/plain", TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        String fileUrl = s3FileService.uploadFile(TEST_FILE_KEY, file);
        assertThat(fileUrl).isNotEmpty();
        assertThat(fileUrl).contains(TEST_FILE_KEY);
        assertThat(s3FileService.fileExists(TEST_FILE_KEY)).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Vault with file test Endpoint should exists")
    void downloadFileWithVaultCredentials() throws Exception {
        var downloadedStream = s3FileService.downloadFile(TEST_FILE_KEY);
        assertThat(downloadedStream).isPresent();
        try (var stream = downloadedStream.get()) {
            String downloadedContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(downloadedContent).isEqualTo(TEST_FILE_CONTENT);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Vault with test logging configuration should exists")
    void getFileMetadataWithVaultCredentials() {
        var metadata = s3FileService.getFileMetadata(TEST_FILE_KEY);
        assertThat(metadata).isPresent();
        assertThat(metadata.get().key()).isEqualTo(TEST_FILE_KEY);
        assertThat(metadata.get().size()).isEqualTo(TEST_FILE_CONTENT.length());
    }

    @Test
    @Order(4)
    @DisplayName("Vault with test should not should exists")
    void deleteFileWithVaultCredentials() {
        boolean deleted = s3FileService.deleteFile(TEST_FILE_KEY);
        assertThat(deleted).isTrue();
        assertThat(s3FileService.fileExists(TEST_FILE_KEY)).isFalse();
    }
}