package com.genius.primavera.application.aws;

import com.genius.primavera.infrastructure.aws.AwsProperties;
import com.genius.primavera.infrastructure.aws.S3Properties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableConfigurationProperties(AwsProperties.class)
public class S3FileServiceVaultIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
            .withServices(S3)
            .withEnv("DEBUG", "1");


    @Autowired
    private S3FileService s3FileService;

    @Autowired
    private S3Properties s3Properties;

    @Autowired
    private AwsProperties awsProperties;

    private static final String TEST_BUCKET = "test-primavera-bucket";
    private static final String TEST_FILE_KEY = "vault-test/sample.txt";
    private static final String TEST_FILE_CONTENT = "Hello from Vault + S3!";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.localstack-endpoint", localstack::getEndpoint);
    }

    @BeforeAll
    static void beforeAll() {
        createLocalStackBucket();
    }

    @Test
    @Order(0)
    @DisplayName("application-test.yml configuration values verification (Vault integration scenario)")
    void verifyVaultTestConfiguration() {
        log.info("Vault + application-test.yml configuration check:");
        log.info("   Access Key: {}", maskSecret(awsProperties.credentials().accessKey()));
        log.info("   Secret Key: {}", maskSecret(awsProperties.credentials().secretKey()));
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

    private String maskSecret(String secret) {
        return secret.length() > 4 ? secret.substring(0, 4) + "****" : "****";
    }

    private static void createLocalStackBucket() {
        log.info("Creating LocalStack S3 bucket...");
        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .region(Region.of(localstack.getRegion()))
                .forcePathStyle(true)
                .build()) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(TEST_BUCKET).build());
            log.info("LocalStack bucket creation completed: {}", TEST_BUCKET);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Vault에서 AWS 자격증명을 가져와 S3 파일 업로드가 가능하다")
    void uploadFileWithVaultCredentials() {
        MockMultipartFile file = new MockMultipartFile("file", "vault-test.txt", "text/plain", TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        String fileUrl = s3FileService.uploadFile(TEST_FILE_KEY, file);
        assertThat(fileUrl).isNotEmpty();
        assertThat(fileUrl).contains(TEST_FILE_KEY);
        assertThat(s3FileService.fileExists(TEST_FILE_KEY)).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Vault 자격증명으로 업로드된 파일을 다운로드할 수 있다")
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
    @DisplayName("Vault 자격증명으로 파일 메타데이터를 조회할 수 있다")
    void getFileMetadataWithVaultCredentials() {
        var metadata = s3FileService.getFileMetadata(TEST_FILE_KEY);
        assertThat(metadata).isPresent();
        assertThat(metadata.get().key()).isEqualTo(TEST_FILE_KEY);
        assertThat(metadata.get().size()).isEqualTo(TEST_FILE_CONTENT.length());
    }

    @Test
    @Order(4)
    @DisplayName("Vault 자격증명으로 파일을 삭제할 수 있다")
    void deleteFileWithVaultCredentials() {
        boolean deleted = s3FileService.deleteFile(TEST_FILE_KEY);
        assertThat(deleted).isTrue();
        assertThat(s3FileService.fileExists(TEST_FILE_KEY)).isFalse();
    }
}