package com.genius.primavera.application.aws;

import com.genius.primavera.infrastructure.aws.AwsProperties;
import com.genius.primavera.testcontainers.ContainerRegistry;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EnableTestContainers(value = {@EnableTestContainers.TestContainer(type = ContainerType.LOCALSTACK, name = "localstack")})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableConfigurationProperties(AwsProperties.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("S3FileService Integration Tests with TestContainers")
public class S3FileServiceIntegrationTest {

    @Autowired
    private S3FileService s3FileService;

    @Autowired
    private AwsProperties awsProperties;

    @Autowired
    private S3Client s3Client;

    @BeforeAll
    void setupBucket() {
        try {
            String bucketName = awsProperties.s3().bucketName();
            log.info("Starting test S3 bucket creation: {}", bucketName);
            try {
                s3Client.headBucket(builder -> builder.bucket(bucketName));
                log.info("Bucket already exists: {}", bucketName);
            } catch (Exception e) {
                log.info("Creating bucket: {}", bucketName);
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                log.info("Bucket creation completed: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Error occurred during bucket creation", e);
            throw new RuntimeException("Failed to create test S3 bucket", e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("translated_text_4 translated_text_2 translated_text_1 service translated_text_2 verification")
    void testContainersAndServiceInjection() {
        var containerManager = ContainerRegistry.get();
        var localStackInfo = containerManager.getContainer("localstack");
        assertNotNull(localStackInfo, "LocalStack translated_text_4 translated_text_12 translated_text_4 translated_text_3");
        assertTrue(localStackInfo.container().isRunning(), "LocalStack translated_text_4translated_text_1 execution translated_text_4 translated_text_3");
        assertNotNull(s3FileService, "S3FileServicetranslated_text_1 translated_text_2 translated_text_3");
        assertNotNull(awsProperties, "AwsPropertiestranslated_text_1 translated_text_2 translated_text_3");
        var container = (LocalStackContainer) localStackInfo.container();
        log.info("Container status check completed - LocalStack: {}", container.getEndpoint());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_2 translated_text_3 test")
    void testSingleFileUpload() throws IOException {
        String testContent = "test translated_text_2 translated_text_2 - " + System.currentTimeMillis();
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "test-single-file.txt";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", testData);
        String uploadResult = assertDoesNotThrow(() -> s3FileService.uploadFile(fileName, mockFile), "translated_text_2 translated_text_3translated_text_1 translated_text_9 translated_text_3");
        assertNotNull(uploadResult, "translated_text_3 resulttranslated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3");
        assertFalse(uploadResult.trim().isEmpty(), "translated_text_3 resulttranslated_text_1 translated_text_4 translated_text_3 translated_text_3");
        boolean exists = assertDoesNotThrow(() -> s3FileService.fileExists(fileName));
        assertTrue(exists, "translated_text_3 translated_text_2translated_text_1 translated_text_4 translated_text_3");
        log.info(" translated_text_2 translated_text_2 translated_text_3 success: {} -> {}", fileName, uploadResult);
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 translated_text_4 translated_text_1 translated_text_2 validation test")
    void testFileDownloadAndVerification() throws IOException {
        String originalContent = "translated_text_4 test translated_text_2 - " + System.currentTimeMillis() + "\ntranslated_text_4 translated_text_2";
        byte[] originalData = originalContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "download-test.txt";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", originalData);
        String uploadResult = s3FileService.uploadFile(fileName, mockFile);
        assertNotNull(uploadResult, "translated_text_2 translated_text_3translated_text_1 translated_text_9 translated_text_3");
        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> s3FileService.downloadFile(fileName), "translated_text_2 translated_text_4translated_text_1 translated_text_9 translated_text_3");
        assertTrue(downloadResult.isPresent(), "translated_text_4 translated_text_1translated_text_1 translated_text_4 translated_text_3");
        byte[] downloadedData = downloadResult.get().readAllBytes();
        String downloadedContent = new String(downloadedData, StandardCharsets.UTF_8);
        assertEquals(originalContent, downloadedContent, "translated_text_4 translated_text_2translated_text_1 translated_text_3 translated_text_4 translated_text_3");
        assertEquals(originalData.length, downloadedData.length, "translated_text_2 translated_text_2translated_text_1 translated_text_4 translated_text_3");
        log.info(" translated_text_2 translated_text_4 translated_text_1 validation completed - translated_text_2: {} bytes", downloadedData.length);
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 deletion translated_text_2 test")
    void testFileDelete() throws IOException {
        String testContent = "deletion test translated_text_2";
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "delete-test.txt";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", testData);
        s3FileService.uploadFile(fileName, mockFile);
        assertTrue(s3FileService.fileExists(fileName), "deletion translated_text_1 translated_text_2translated_text_1 translated_text_4 translated_text_3");
        boolean deleteResult = assertDoesNotThrow(() -> s3FileService.deleteFile(fileName), "translated_text_2 deletiontranslated_text_1 translated_text_9 translated_text_3");
        assertTrue(deleteResult, "deletion translated_text_1 translated_text_9 translated_text_3");
        boolean existsAfterDelete = s3FileService.fileExists(fileName);
        assertFalse(existsAfterDelete, "deletion translated_text_1 translated_text_2translated_text_1 translated_text_4 translated_text_3 translated_text_3");
        log.info(" translated_text_2 deletion test completed: {}", fileName);
    }

    @Test
    @Order(5)
    @DisplayName("translated_text_3 translated_text_2 translated_text_3/translated_text_4 test")
    void testLargeFileHandling() throws IOException {
        int fileSize = 2 * 1024 * 1024;
        byte[] largeData = new byte[fileSize];
        for (int i = 0; i < fileSize; i++) largeData[i] = (byte) ((i % 256) ^ (i / 1024));
        String fileName = "large-file-test.bin";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "application/octet-stream", largeData);
        long uploadStart = System.currentTimeMillis();
        String uploadResult = assertDoesNotThrow(() -> s3FileService.uploadFile(fileName, mockFile), "translated_text_3 translated_text_2 translated_text_3translated_text_1 translated_text_9 translated_text_3");
        long uploadTime = System.currentTimeMillis() - uploadStart;
        assertNotNull(uploadResult, "translated_text_3 resulttranslated_text_1 nulltranslated_text_1 translated_text_1 translated_text_3");
        long downloadStart = System.currentTimeMillis();
        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> s3FileService.downloadFile(fileName), "translated_text_3 translated_text_2 translated_text_4translated_text_1 translated_text_9 translated_text_3");
        assertTrue(downloadResult.isPresent(), "translated_text_4 resulttranslated_text_1 translated_text_4 translated_text_3");
        byte[] downloadedData = downloadResult.get().readAllBytes();
        long downloadTime = System.currentTimeMillis() - downloadStart;
        assertEquals(fileSize, downloadedData.length, "translated_text_4 translated_text_2 translated_text_2translated_text_1 translated_text_3 translated_text_4 translated_text_3");
        for (int i = 0; i < fileSize; i += 1024) assertEquals(largeData[i], downloadedData[i], "translated_text_4 translated_text_2 translated_text_2translated_text_1 translated_text_3 translated_text_4 translated_text_3 (translated_text_2: " + i + ")");
        log.info(" translated_text_3 translated_text_2 processing completed - translated_text_2: {} MB, translated_text_3: {}ms, translated_text_4: {}ms", fileSize / (1024 * 1024), uploadTime, downloadTime);
    }

    @Test
    @Order(6)
    @DisplayName("translated_text_3 translated_text_2 translated_text_2 translated_text_3 test")
    void testMultipleFileFormats() throws IOException {
        String textContent = "translated_text_5! translated_text_2 translated_text_3 translated_text_2.\ntranslated_text_2 translated_text_1 translated_text_2translated_text_1 translated_text_3.";
        byte[] textData = textContent.getBytes(StandardCharsets.UTF_8);
        String textFileName = "text-file.txt";
        String jsonContent = "{\"name\": \"test\", \"value\": 123, \"array\": [1, 2, 3]}";
        byte[] jsonData = jsonContent.getBytes(StandardCharsets.UTF_8);
        String jsonFileName = "data.json";
        byte[] binaryData = new byte[1024];
        for (int i = 0; i < binaryData.length; i++) binaryData[i] = (byte) (Math.sin(i * 0.1) * 127);
        String binaryFileName = "image.bin";
        String[] fileNames = {textFileName, jsonFileName, binaryFileName};
        byte[][] fileData = {textData, jsonData, binaryData};
        String[] contentTypes = {"text/plain", "application/json", "application/octet-stream"};
        for (int i = 0; i < fileNames.length; i++) {
            final int index = i;
            MultipartFile mockFile = new MockMultipartFile("file", fileNames[index], contentTypes[index], fileData[index]);
            String uploadResult = assertDoesNotThrow(() -> s3FileService.uploadFile(fileNames[index], mockFile), fileNames[index] + " translated_text_3translated_text_1 translated_text_9 translated_text_3");
            assertNotNull(uploadResult, fileNames[i] + " translated_text_3 resulttranslated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3");
            assertTrue(s3FileService.fileExists(fileNames[i]), fileNames[i] + " translated_text_2translated_text_1 translated_text_4 translated_text_3");
        }
        Optional<InputStream> textResult = s3FileService.downloadFile(textFileName);
        assertTrue(textResult.isPresent(), "translated_text_3 translated_text_2translated_text_1 translated_text_4 translated_text_3");
        byte[] downloadedText = textResult.get().readAllBytes();
        assertEquals(textContent, new String(downloadedText, StandardCharsets.UTF_8), "translated_text_3 translated_text_2 translated_text_2translated_text_1 translated_text_4 translated_text_3");
        Optional<InputStream> jsonResult = s3FileService.downloadFile(jsonFileName);
        assertTrue(jsonResult.isPresent(), "JSON translated_text_2translated_text_1 translated_text_4 translated_text_3");
        byte[] downloadedJson = jsonResult.get().readAllBytes();
        assertEquals(jsonContent, new String(downloadedJson, StandardCharsets.UTF_8), "JSON translated_text_2 translated_text_2translated_text_1 translated_text_4 translated_text_3");
        Optional<InputStream> binaryResult = s3FileService.downloadFile(binaryFileName);
        assertTrue(binaryResult.isPresent(), "translated_text_1 translated_text_2translated_text_1 translated_text_4 translated_text_3");
        byte[] downloadedBinary = binaryResult.get().readAllBytes();
        assertArrayEquals(binaryData, downloadedBinary, "translated_text_1 translated_text_2 translated_text_2translated_text_1 translated_text_4 translated_text_3");
        log.info(" translated_text_3 translated_text_2 translated_text_2 translated_text_3/translated_text_4 test completed");
    }

    @Test
    @Order(7)
    @DisplayName("translated_text_2 translated_text_2 translated_text_3 translated_text_2 test")
    void testConcurrentFileUpload() {
        int numberOfFiles = 10;
        CompletableFuture<String>[] uploadFutures = new CompletableFuture[numberOfFiles];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfFiles; i++) {
            String fileName = "concurrent-test-" + i + ".txt";
            String content = "translated_text_2 translated_text_3 test translated_text_2 #" + i + " - " + System.currentTimeMillis();
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            uploadFutures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", data);
                    return s3FileService.uploadFile(fileName, mockFile);
                } catch (Exception e) {
                    throw new RuntimeException("translated_text_2 translated_text_3 failure: " + fileName, e);
                }
            });
        }
        CompletableFuture.allOf(uploadFutures).join();
        long totalTime = System.currentTimeMillis() - startTime;
        for (int i = 0; i < numberOfFiles; i++) {
            final int index = i;
            String uploadResult = assertDoesNotThrow(() -> uploadFutures[index].get(5, TimeUnit.SECONDS), "translated_text_3 #" + index + "translated_text_1 translated_text_9 translated_text_3");
            assertNotNull(uploadResult, "translated_text_3 result #" + index + "translated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3");
            String fileName = "concurrent-test-" + index + ".txt";
            assertTrue(s3FileService.fileExists(fileName), "translated_text_3 translated_text_2 #" + index + "translated_text_1 translated_text_4 translated_text_3");
        }
        double avgTimePerFile = (double) totalTime / numberOfFiles;
        log.info(" translated_text_2 translated_text_2 translated_text_3 test completed - {} translated_text_2, translated_text_1 translated_text_2: {}ms, translated_text_2: {:.2f}ms/translated_text_2", numberOfFiles, totalTime, avgTimePerFile);
    }

    @Test
    @Order(8)
    @DisplayName("translated_text_2 translated_text_2 processing test")
    void testErrorHandling() {
        String nonExistentFile = "non-existent-file-" + System.currentTimeMillis() + ".txt";
        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> s3FileService.downloadFile(nonExistentFile));
        assertFalse(downloadResult.isPresent(), "translated_text_4 translated_text_2 translated_text_2 translated_text_1 Optionaltranslated_text_1 translated_text_4 translated_text_3");
        boolean deleteResult = assertDoesNotThrow(() -> s3FileService.deleteFile(nonExistentFile), "translated_text_4 translated_text_2 translated_text_2 deletiontranslated_text_1 translated_text_9 translated_text_3");
        log.info("translated_text_4 translated_text_2 translated_text_2 deletion result: {}", deleteResult);
        boolean exists = assertDoesNotThrow(() -> s3FileService.fileExists(nonExistentFile));
        assertFalse(exists, "translated_text_4 translated_text_2 translated_text_2 translated_text_4 translated_text_3 translated_text_5 translated_text_3");
        log.info(" translated_text_2 translated_text_2 processing test completed");
    }

    @Test
    @Order(9)
    @DisplayName("translated_text_2 translated_text_2 translated_text_2 processing test")
    void testSpecialCharactersInFileName() throws IOException {
        String[] specialFileNames = {
                "translated_text_2translated_text_2.txt",
                "file-with-dashes.txt",
                "file_with_underscores.txt",
                "file.with.dots.txt",
                "file (with spaces).txt"
        };

        String testContent = "translated_text_2 translated_text_2 translated_text_2 test";
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        for (String fileName : specialFileNames) {
            try {
                MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", testData);
                String uploadResult = assertDoesNotThrow(() -> s3FileService.uploadFile(fileName, mockFile), "translated_text_2 translated_text_2 translated_text_2 translated_text_3translated_text_1 translated_text_9 translated_text_3: " + fileName);
                assertNotNull(uploadResult, "translated_text_3 resulttranslated_text_1 nulltranslated_text_1 translated_text_4 translated_text_3: " + fileName);
                boolean exists = s3FileService.fileExists(fileName);
                assertTrue(exists, "translated_text_3 translated_text_2translated_text_1 translated_text_4 translated_text_3: " + fileName);
                Optional<InputStream> downloadResult = s3FileService.downloadFile(fileName);
                assertTrue(downloadResult.isPresent(), "translated_text_2translated_text_1 translated_text_4 translated_text_3: " + fileName);
                byte[] downloadedData = downloadResult.get().readAllBytes();
                assertEquals(testContent, new String(downloadedData, StandardCharsets.UTF_8), "translated_text_4 translated_text_2translated_text_1 translated_text_3 translated_text_4 translated_text_3: " + fileName);
                log.info(" translated_text_2 translated_text_2 translated_text_2 processing success: {}", fileName);
            } catch (Exception e) {
                log.warn("translated_text_2 translated_text_2 translated_text_2 processing failure (translated_text_2 translated_text_1): {} - {}", fileName, e.getMessage());
            }
        }
        log.info(" translated_text_2 translated_text_2 translated_text_2 processing test completed");
    }
}