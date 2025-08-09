package com.genius.primavera.application.aws;

import com.genius.primavera.infrastructure.aws.AwsProperties;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import com.genius.primavera.testcontainers.ContainerRegistry;
import com.genius.primavera.testcontainers.property.LocalStackPropertyRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S3FileService 통합 테스트
 * spring-boot-starter-test-containers를 사용하여 LocalStack과 MariaDB를 함께 테스트합니다.
 */
@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EnableTestContainers(value = {
        @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "mariadb"),
        @EnableTestContainers.TestContainer(type = ContainerType.LOCALSTACK, name = "localstack")
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableConfigurationProperties(AwsProperties.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("S3FileService Integration Tests with TestContainers")
public class S3FileServiceIntegrationTest {

    @Autowired
    private S3FileService s3FileService;

    @Autowired
    private AwsProperties awsProperties;

    private static final String TEST_BUCKET = "test-primavera-bucket";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        LocalStackPropertyRegistrar.registerEndpoints(registry);
    }

    @BeforeAll
    static void beforeAll() {
        // LocalStack용 버킷 생성 - LocalStackPropertyRegistrar 사용
        createLocalStackBucket();
    }

    private static void createLocalStackBucket() {
        log.info("🐳 LocalStack S3 버킷 생성 중... (spring-boot-starter-test-containers 사용)");

        // LocalStackPropertyRegistrar를 통해 컨테이너 정보 가져오기
        var container = LocalStackPropertyRegistrar.getContainer("localstack");

        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(container.getEndpoint())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(container.getAccessKey(), container.getSecretKey())
                ))
                .region(Region.of(container.getRegion()))
                .forcePathStyle(true)
                .build()) {

            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(TEST_BUCKET)
                    .build());

            log.info("✅ LocalStack 버킷 생성 완료: {} (엔드포인트: {})",
                    TEST_BUCKET, container.getEndpoint());
        } catch (Exception e) {
            log.error("❌ LocalStack 버킷 생성 실패: {}", e.getMessage());
            throw new RuntimeException("버킷 생성 실패", e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("컨테이너 상태 및 서비스 주입 확인")
    void testContainersAndServiceInjection() {
        var containerManager = ContainerRegistry.get();

        // LocalStack 컨테이너 확인
        var localStackInfo = containerManager.getContainer("localstack");
        assertNotNull(localStackInfo, "LocalStack 컨테이너 정보가 존재해야 합니다");
        assertTrue(localStackInfo.container().isRunning(), "LocalStack 컨테이너가 실행 중이어야 합니다");

        // MariaDB 컨테이너 확인
        var mariadbInfo = containerManager.getContainer("mariadb");
        assertNotNull(mariadbInfo, "MariaDB 컨테이너 정보가 존재해야 합니다");
        assertTrue(mariadbInfo.container().isRunning(), "MariaDB 컨테이너가 실행 중이어야 합니다");

        // 서비스 주입 확인
        assertNotNull(s3FileService, "S3FileService가 주입되어야 합니다");
        assertNotNull(awsProperties, "AwsProperties가 주입되어야 합니다");

        var container = (LocalStackContainer) localStackInfo.container();
        log.info("✅ 컨테이너 상태 확인 완료 - LocalStack: {}, MariaDB: {}",
                container.getEndpoint(), mariadbInfo.container().getFirstMappedPort());
    }

    @Test
    @Order(2)
    @DisplayName("단일 파일 업로드 테스트")
    void testSingleFileUpload() throws IOException {
        // 테스트 데이터 준비
        String testContent = "테스트 파일 내용 - " + System.currentTimeMillis();
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "test-single-file.txt";

        // MockMultipartFile 생성
        MultipartFile mockFile = new MockMultipartFile(
                "file", fileName, "text/plain", testData);

        // 파일 업로드
        String uploadResult = assertDoesNotThrow(() -> {
            return s3FileService.uploadFile(fileName, mockFile);
        }, "파일 업로드가 성공해야 합니다");

        assertNotNull(uploadResult, "업로드 결과가 null이 아니어야 합니다");
        assertFalse(uploadResult.trim().isEmpty(), "업로드 결과가 비어있지 않아야 합니다");

        // 파일 존재 확인
        boolean exists = assertDoesNotThrow(() -> s3FileService.fileExists(fileName));
        assertTrue(exists, "업로드된 파일이 존재해야 합니다");

        log.info("✅ 단일 파일 업로드 성공: {} -> {}", fileName, uploadResult);
    }

    @Test
    @Order(3)
    @DisplayName("파일 다운로드 및 내용 검증 테스트")
    void testFileDownloadAndVerification() throws IOException {
        // 테스트 데이터 준비 및 업로드
        String originalContent = "다운로드 테스트 내용 - " + System.currentTimeMillis() + "\n멀티라인 내용입니다";
        byte[] originalData = originalContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "download-test.txt";

        MultipartFile mockFile = new MockMultipartFile(
                "file", fileName, "text/plain", originalData);

        String uploadResult = s3FileService.uploadFile(fileName, mockFile);
        assertNotNull(uploadResult, "파일 업로드가 성공해야 합니다");

        // 파일 다운로드
        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> {
            return s3FileService.downloadFile(fileName);
        }, "파일 다운로드가 성공해야 합니다");

        assertTrue(downloadResult.isPresent(), "다운로드된 데이터가 존재해야 합니다");

        // InputStream을 byte array로 변환
        byte[] downloadedData = downloadResult.get().readAllBytes();

        // 내용 검증
        String downloadedContent = new String(downloadedData, StandardCharsets.UTF_8);
        assertEquals(originalContent, downloadedContent, "다운로드된 내용이 원본과 일치해야 합니다");
        assertEquals(originalData.length, downloadedData.length, "파일 크기가 일치해야 합니다");

        log.info("✅ 파일 다운로드 및 검증 완료 - 크기: {} bytes", downloadedData.length);
    }

    @Test
    @Order(4)
    @DisplayName("파일 삭제 기능 테스트")
    void testFileDelete() throws IOException {
        // 테스트 파일 업로드
        String testContent = "삭제 테스트용 파일";
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "delete-test.txt";

        MultipartFile mockFile = new MockMultipartFile(
                "file", fileName, "text/plain", testData);

        s3FileService.uploadFile(fileName, mockFile);

        // 파일 존재 확인
        assertTrue(s3FileService.fileExists(fileName), "삭제 전 파일이 존재해야 합니다");

        // 파일 삭제
        boolean deleteResult = assertDoesNotThrow(() -> {
            return s3FileService.deleteFile(fileName);
        }, "파일 삭제가 성공해야 합니다");

        assertTrue(deleteResult, "삭제 작업이 성공해야 합니다");

        // 삭제 후 파일 존재 여부 확인
        boolean existsAfterDelete = s3FileService.fileExists(fileName);
        assertFalse(existsAfterDelete, "삭제 후 파일이 존재하지 않아야 합니다");

        log.info("✅ 파일 삭제 테스트 완료: {}", fileName);
    }

    @Test
    @Order(5)
    @DisplayName("대용량 파일 업로드/다운로드 테스트")
    void testLargeFileHandling() throws IOException {
        // 2MB 크기의 테스트 데이터 생성
        int fileSize = 2 * 1024 * 1024; // 2MB
        byte[] largeData = new byte[fileSize];

        // 패턴이 있는 데이터로 채우기 (검증 가능하도록)
        for (int i = 0; i < fileSize; i++) {
            largeData[i] = (byte) ((i % 256) ^ (i / 1024));
        }

        String fileName = "large-file-test.bin";

        MultipartFile mockFile = new MockMultipartFile(
                "file", fileName, "application/octet-stream", largeData);

        // 대용량 파일 업로드
        long uploadStart = System.currentTimeMillis();
        String uploadResult = assertDoesNotThrow(() -> {
            return s3FileService.uploadFile(fileName, mockFile);
        }, "대용량 파일 업로드가 성공해야 합니다");
        long uploadTime = System.currentTimeMillis() - uploadStart;

        assertNotNull(uploadResult, "업로드 결과가 null이 아이어야 합니다");

        // 대용량 파일 다운로드
        long downloadStart = System.currentTimeMillis();
        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> {
            return s3FileService.downloadFile(fileName);
        }, "대용량 파일 다운로드가 성공해야 합니다");

        assertTrue(downloadResult.isPresent(), "다운로드 결과가 존재해야 합니다");
        byte[] downloadedData = downloadResult.get().readAllBytes();
        long downloadTime = System.currentTimeMillis() - downloadStart;

        // 크기 검증
        assertEquals(fileSize, downloadedData.length, "다운로드된 파일 크기가 원본과 일치해야 합니다");

        // 내용 검증 (샘플링)
        for (int i = 0; i < fileSize; i += 1024) {
            assertEquals(largeData[i], downloadedData[i],
                    "다운로드된 파일의 내용이 원본과 일치해야 합니다 (위치: " + i + ")");
        }

        log.info("✅ 대용량 파일 처리 완료 - 크기: {} MB, 업로드: {}ms, 다운로드: {}ms",
                fileSize / (1024 * 1024), uploadTime, downloadTime);
    }

    @Test
    @Order(6)
    @DisplayName("다양한 파일 형식 업로드 테스트")
    void testMultipleFileFormats() throws IOException {
        // 텍스트 파일
        String textContent = "안녕하세요! 한글 텍스트 파일입니다.\n여러 줄 내용을 포함합니다.";
        byte[] textData = textContent.getBytes(StandardCharsets.UTF_8);
        String textFileName = "text-file.txt";

        // JSON 파일
        String jsonContent = "{\"name\": \"테스트\", \"value\": 123, \"array\": [1, 2, 3]}";
        byte[] jsonData = jsonContent.getBytes(StandardCharsets.UTF_8);
        String jsonFileName = "data.json";

        // 바이너리 파일 (가상의 이미지 데이터)
        byte[] binaryData = new byte[1024];
        for (int i = 0; i < binaryData.length; i++) {
            binaryData[i] = (byte) (Math.sin(i * 0.1) * 127);
        }
        String binaryFileName = "image.bin";

        // 모든 파일 업로드
        String[] fileNames = {textFileName, jsonFileName, binaryFileName};
        byte[][] fileData = {textData, jsonData, binaryData};
        String[] contentTypes = {"text/plain", "application/json", "application/octet-stream"};

        for (int i = 0; i < fileNames.length; i++) {
            final int index = i;
            MultipartFile mockFile = new MockMultipartFile(
                    "file", fileNames[index], contentTypes[index], fileData[index]);

            String uploadResult = assertDoesNotThrow(() -> {
                return s3FileService.uploadFile(fileNames[index], mockFile);
            }, fileNames[index] + " 업로드가 성공해야 합니다");

            assertNotNull(uploadResult, fileNames[i] + " 업로드 결과가 null이 아니어야 합니다");

            // 파일 존재 확인
            assertTrue(s3FileService.fileExists(fileNames[i]),
                    fileNames[i] + " 파일이 존재해야 합니다");
        }

        // 텍스트 파일 다운로드 및 검증
        Optional<InputStream> textResult = s3FileService.downloadFile(textFileName);
        assertTrue(textResult.isPresent(), "텍스트 파일이 존재해야 합니다");
        byte[] downloadedText = textResult.get().readAllBytes();
        assertEquals(textContent, new String(downloadedText, StandardCharsets.UTF_8),
                "텍스트 파일 내용이 일치해야 합니다");

        // JSON 파일 다운로드 및 검증
        Optional<InputStream> jsonResult = s3FileService.downloadFile(jsonFileName);
        assertTrue(jsonResult.isPresent(), "JSON 파일이 존재해야 합니다");
        byte[] downloadedJson = jsonResult.get().readAllBytes();
        assertEquals(jsonContent, new String(downloadedJson, StandardCharsets.UTF_8),
                "JSON 파일 내용이 일치해야 합니다");

        // 바이너리 파일 다운로드 및 검증
        Optional<InputStream> binaryResult = s3FileService.downloadFile(binaryFileName);
        assertTrue(binaryResult.isPresent(), "바이너리 파일이 존재해야 합니다");
        byte[] downloadedBinary = binaryResult.get().readAllBytes();
        assertArrayEquals(binaryData, downloadedBinary, "바이너리 파일 내용이 일치해야 합니다");

        log.info("✅ 다양한 파일 형식 업로드/다운로드 테스트 완료");
    }

    @Test
    @Order(7)
    @DisplayName("동시 파일 업로드 성능 테스트")
    void testConcurrentFileUpload() {
        int numberOfFiles = 10;
        int fileSize = 1024; // 1KB per file

        CompletableFuture<String>[] uploadFutures = new CompletableFuture[numberOfFiles];

        long startTime = System.currentTimeMillis();

        // 동시에 여러 파일 업로드
        for (int i = 0; i < numberOfFiles; i++) {
            final int fileIndex = i;
            String fileName = "concurrent-test-" + fileIndex + ".txt";
            String content = "동시 업로드 테스트 파일 #" + fileIndex + " - " + System.currentTimeMillis();
            byte[] data = content.getBytes(StandardCharsets.UTF_8);

            uploadFutures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    MultipartFile mockFile = new MockMultipartFile(
                            "file", fileName, "text/plain", data);
                    return s3FileService.uploadFile(fileName, mockFile);
                } catch (Exception e) {
                    throw new RuntimeException("파일 업로드 실패: " + fileName, e);
                }
            });
        }

        // 모든 업로드 완료 대기
        CompletableFuture.allOf(uploadFutures).join();

        long totalTime = System.currentTimeMillis() - startTime;

        // 모든 업로드 결과 검증
        for (int i = 0; i < numberOfFiles; i++) {
            final int index = i;
            String uploadResult = assertDoesNotThrow(() -> {
                return uploadFutures[index].get(5, TimeUnit.SECONDS);
            }, "업로드 #" + index + "이 성공해야 합니다");

            assertNotNull(uploadResult, "업로드 결과 #" + index + "이 null이 아니어야 합니다");

            // 파일 존재 확인
            String fileName = "concurrent-test-" + index + ".txt";
            assertTrue(s3FileService.fileExists(fileName),
                    "업로드된 파일 #" + index + "이 존재해야 합니다");
        }

        double avgTimePerFile = (double) totalTime / numberOfFiles;
        log.info("✅ 동시 파일 업로드 테스트 완료 - {} 파일, 총 시간: {}ms, 평균: {:.2f}ms/파일",
                numberOfFiles, totalTime, avgTimePerFile);
    }

    @Test
    @Order(8)
    @DisplayName("에러 상황 처리 테스트")
    void testErrorHandling() {
        // 존재하지 않는 파일 다운로드 시도
        String nonExistentFile = "non-existent-file-" + System.currentTimeMillis() + ".txt";

        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> {
            return s3FileService.downloadFile(nonExistentFile);
        });

        assertFalse(downloadResult.isPresent(), "존재하지 않는 파일은 빈 Optional을 반환해야 합니다");

        // 존재하지 않는 파일 삭제 시도
        boolean deleteResult = assertDoesNotThrow(() -> {
            return s3FileService.deleteFile(nonExistentFile);
        });

        // 삭제 결과는 구현에 따라 다를 수 있으므로 예외가 발생하지 않는지만 확인
        log.info("존재하지 않는 파일 삭제 결과: {}", deleteResult);

        // 존재하지 않는 파일 존재 여부 확인
        boolean exists = assertDoesNotThrow(() -> {
            return s3FileService.fileExists(nonExistentFile);
        });

        assertFalse(exists, "존재하지 않는 파일은 존재하지 않다고 반환되어야 합니다");

        log.info("✅ 에러 상황 처리 테스트 완료");
    }

    @Test
    @Order(9)
    @DisplayName("파일명 특수 문자 처리 테스트")
    void testSpecialCharactersInFileName() throws IOException {
        // 다양한 특수 문자가 포함된 파일명 테스트
        String[] specialFileNames = {
                "한글파일명.txt",
                "file-with-dashes.txt",
                "file_with_underscores.txt",
                "file.with.dots.txt",
                "file (with spaces).txt"
        };

        String testContent = "특수 문자 파일명 테스트";
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);

        for (String fileName : specialFileNames) {
            try {
                MultipartFile mockFile = new MockMultipartFile(
                        "file", fileName, "text/plain", testData);

                // 파일 업로드
                String uploadResult = assertDoesNotThrow(() -> {
                    return s3FileService.uploadFile(fileName, mockFile);
                }, "특수 문자 파일명 업로드가 성공해야 합니다: " + fileName);

                assertNotNull(uploadResult, "업로드 결과가 null이 아니어야 합니다: " + fileName);

                // 파일 존재 확인
                boolean exists = s3FileService.fileExists(fileName);
                assertTrue(exists, "업로드된 파일이 존재해야 합니다: " + fileName);

                // 파일 다운로드 및 검증
                Optional<InputStream> downloadResult = s3FileService.downloadFile(fileName);
                assertTrue(downloadResult.isPresent(), "파일이 다운로드되어야 합니다: " + fileName);
                byte[] downloadedData = downloadResult.get().readAllBytes();
                assertEquals(testContent, new String(downloadedData, StandardCharsets.UTF_8),
                        "다운로드된 내용이 원본과 일치해야 합니다: " + fileName);

                log.info("✅ 특수 문자 파일명 처리 성공: {}", fileName);

            } catch (Exception e) {
                log.warn("특수 문자 파일명 처리 실패 (예상 가능): {} - {}", fileName, e.getMessage());
                // 일부 특수 문자는 S3에서 지원하지 않을 수 있으므로 경고만 로그
            }
        }

        log.info("✅ 특수 문자 파일명 처리 테스트 완료");
    }
}