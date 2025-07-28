package com.genius.primavera.application.aws;

import com.genius.primavera.infrastructure.aws.AwsProperties;
import com.genius.primavera.infrastructure.aws.S3Properties;
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

/**
 * application-test.yml 설정을 통한 Vault 통합 시나리오 S3 테스트
 * Vault 컨테이너를 직접 실행하지 않고, application-test.yml의 설정 우선순위를 검증합니다.
 * 실제 Vault 사용 시에는 ../../infrastructure/vault-init.sh 스크립트를 실행하세요.
 */
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
        // LocalStack S3 엔드포인트만 동적으로 설정
        // 나머지는 application-test.yml에서 처리
        registry.add("aws.s3.localstack-endpoint", localstack::getEndpoint);
    }

    @BeforeAll
    static void beforeAll() {
        // LocalStack용 버킷 생성 (application-test.yml 설정 기반)
        createLocalStackBucket();
    }

    @Test
    @Order(0)
    @DisplayName("application-test.yml 설정값 확인 (Vault 통합 시나리오)")
    void verifyVaultTestConfiguration() {
        System.out.println("🔐📋 Vault + application-test.yml 설정 확인:");
        System.out.println("   Access Key: " + maskSecret(awsProperties.credentials().accessKey()));
        System.out.println("   Secret Key: " + maskSecret(awsProperties.credentials().secretKey()));
        System.out.println("   Region: " + awsProperties.region().value());
        System.out.println("   Bucket: " + awsProperties.s3().bucketName());
        System.out.println("   Endpoint: " + (awsProperties.s3().hasEndpoint() ? awsProperties.s3().endpoint() : "(AWS Default)"));

        // LocalStack 사용 여부 확인
        System.out.println("   환경: 🔐 Vault 통합 + " + (awsProperties.isLocalStack() ? "🐳 LocalStack" : "🌟 실제 AWS"));

        // 설정값 검증
        assertThat(awsProperties.credentials().accessKey()).isNotEmpty();
        assertThat(awsProperties.credentials().secretKey()).isNotEmpty();
        assertThat(awsProperties.region().value()).isNotEmpty();
        assertThat(awsProperties.s3().bucketName()).isNotEmpty();

        if (awsProperties.isLocalStack()) {
            System.out.println("✅ LocalStack + application-test.yml 설정이 올바르게 구성되었습니다.");
            System.out.println("💡 실제 Vault 사용 시: ../../infrastructure/vault-init.sh 스크립트를 실행하세요.");
        } else {
            System.out.println("⚠️  실제 AWS + Vault 환경에서 테스트 중입니다.");
        }
    }

    private String maskSecret(String secret) {
        return secret.length() > 4 ? secret.substring(0, 4) + "****" : "****";
    }

    private static void createLocalStackBucket() {
        System.out.println("🐳 LocalStack S3 버킷 생성 중...");

        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                ))
                .region(Region.of(localstack.getRegion()))
                .forcePathStyle(true)
                .build()) {

            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(TEST_BUCKET)
                    .build());

            System.out.println("✅ LocalStack 버킷 생성 완료: " + TEST_BUCKET);
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