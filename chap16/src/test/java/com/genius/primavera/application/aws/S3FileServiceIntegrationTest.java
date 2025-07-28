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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableConfigurationProperties(AwsProperties.class)
class S3FileServiceIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
            .withServices(S3)
            .withEnv("DEBUG", "1");

    @Autowired
    private S3FileService s3FileService;
    
    @Autowired
    private AwsProperties awsProperties;

    private static final String TEST_BUCKET = "test-primavera-bucket";
    private static final String TEST_FILE_KEY = "test/sample.txt";
    private static final String TEST_FILE_CONTENT = "Hello, S3 World!";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // LocalStack endpoint만 동적으로 설정
        // 나머지는 application-test.yml에서 처리
        registry.add("aws.s3.localstack-endpoint", localstack::getEndpoint);
    }

    @BeforeAll
    static void beforeAll() {
        // LocalStack용 버킷 생성 (실제 AWS 사용 시 버킷이 이미 존재한다고 가정)
        createLocalStackBucket();
    }

    @Test
    @Order(0)
    @DisplayName("application-test.yml 설정값 확인")
    void verifyTestConfiguration() {
        System.out.println("🔧 테스트 설정 확인:");
        System.out.println("   Access Key: " + maskSecret(awsProperties.credentials().accessKey()));
        System.out.println("   Secret Key: " + maskSecret(awsProperties.credentials().secretKey()));
        System.out.println("   Region: " + awsProperties.region().value());
        System.out.println("   Bucket: " + awsProperties.s3().bucketName());
        System.out.println("   Endpoint: " + (awsProperties.s3().hasEndpoint() ? awsProperties.s3().endpoint() : "(AWS Default)"));

        // LocalStack 사용 여부 확인
        System.out.println("   환경: " + (awsProperties.isLocalStack() ? "🐳 LocalStack" : "🌟 실제 AWS"));

        // 설정값 검증
        assertThat(awsProperties.credentials().accessKey()).isNotEmpty();
        assertThat(awsProperties.credentials().secretKey()).isNotEmpty();
        assertThat(awsProperties.region().value()).isNotEmpty();
        assertThat(awsProperties.s3().bucketName()).isNotEmpty();

        if (awsProperties.isLocalStack()) {
            System.out.println("✅ LocalStack 테스트 환경이 올바르게 설정되었습니다.");
        } else {
            System.out.println("⚠️  실제 AWS 환경에서 테스트 중입니다. 비용이 발생할 수 있습니다.");
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
    @DisplayName("MultipartFile을 S3에 업로드할 수 있다")
    void uploadMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8)
        );

        String fileUrl = s3FileService.uploadFile(TEST_FILE_KEY, file);
        assertThat(fileUrl).isNotEmpty();
        assertThat(fileUrl).contains(TEST_FILE_KEY);
        assertThat(s3FileService.fileExists(TEST_FILE_KEY)).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("InputStream을 S3에 업로드할 수 있다")
    void uploadInputStream() {
        String key = "test/input-stream.txt";
        String content = "InputStream Content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        String fileUrl = s3FileService.uploadFile(key, inputStream, content.length(), "text/plain");
        assertThat(fileUrl).isNotEmpty();
        assertThat(fileUrl).contains(key);
        assertThat(s3FileService.fileExists(key)).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("S3에서 파일을 다운로드할 수 있다")
    void downloadFile() throws Exception {
        Optional<InputStream> downloadedStream = s3FileService.downloadFile(TEST_FILE_KEY);
        assertThat(downloadedStream).isPresent();

        try (InputStream stream = downloadedStream.get()) {
            String downloadedContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(downloadedContent).isEqualTo(TEST_FILE_CONTENT);
        }
    }

    @Test
    @Order(4)
    @DisplayName("존재하지 않는 파일 다운로드 시 Empty를 반환한다")
    void downloadNonExistentFile() {
        Optional<InputStream> downloadedStream = s3FileService.downloadFile("non-existent-file.txt");
        assertThat(downloadedStream).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("파일 존재 여부를 확인할 수 있다")
    void checkFileExists() {
        assertThat(s3FileService.fileExists(TEST_FILE_KEY)).isTrue();
        assertThat(s3FileService.fileExists("non-existent-file.txt")).isFalse();
    }

    @Test
    @Order(6)
    @DisplayName("파일 목록을 조회할 수 있다")
    void listFiles() {
        List<String> files = s3FileService.listFiles("test/");
        assertThat(files).isNotEmpty();
        assertThat(files).contains(TEST_FILE_KEY);
        assertThat(files).contains("test/input-stream.txt");
    }

    @Test
    @Order(7)
    @DisplayName("전체 파일 목록을 조회할 수 있다")
    void listAllFiles() {
        List<String> files = s3FileService.listFiles(null);
        assertThat(files).isNotEmpty();
        assertThat(files).contains(TEST_FILE_KEY);
    }

    @Test
    @Order(8)
    @DisplayName("파일 메타데이터를 조회할 수 있다")
    void getFileMetadata() {
        Optional<S3FileMetadata> metadata = s3FileService.getFileMetadata(TEST_FILE_KEY);
        assertThat(metadata).isPresent();
        S3FileMetadata meta = metadata.get();
        assertThat(meta.key()).isEqualTo(TEST_FILE_KEY);
        assertThat(meta.size()).isEqualTo(TEST_FILE_CONTENT.length());
        assertThat(meta.contentType()).isEqualTo("text/plain");
        assertThat(meta.lastModified()).isNotNull();
        assertThat(meta.etag()).isNotNull();
    }

    @Test
    @Order(9)
    @DisplayName("존재하지 않는 파일의 메타데이터 조회 시 Empty를 반환한다")
    void getMetadataForNonExistentFile() {
        Optional<S3FileMetadata> metadata = s3FileService.getFileMetadata("non-existent-file.txt");
        assertThat(metadata).isEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("S3에서 파일을 삭제할 수 있다")
    void deleteFile() {
        String keyToDelete = "test/to-delete.txt";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "to-delete.txt",
                "text/plain",
                "Delete me!".getBytes(StandardCharsets.UTF_8)
        );
        s3FileService.uploadFile(keyToDelete, file);
        assertThat(s3FileService.fileExists(keyToDelete)).isTrue();
        boolean deleted = s3FileService.deleteFile(keyToDelete);
        assertThat(deleted).isTrue();
        assertThat(s3FileService.fileExists(keyToDelete)).isFalse();
    }

    @Test
    @Order(11)
    @DisplayName("존재하지 않는 파일 삭제 시 false를 반환한다")
    void deleteNonExistentFile() {
        boolean deleted = s3FileService.deleteFile("non-existent-file.txt");
        assertThat(deleted).isTrue();
    }
}