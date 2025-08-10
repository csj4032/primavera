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
    @DisplayName("file test should service test verification")
    void testContainersAndServiceInjection() {
        var containerManager = ContainerRegistry.get();
        var localStackInfo = containerManager.getContainer("localstack");
        assertNotNull(localStackInfo, "LocalStack file operation file connection");
        assertTrue(localStackInfo.container().isRunning(), "LocalStack fileshould execution file connection");
        assertNotNull(s3FileService, "S3FileServiceshould test connection");
        assertNotNull(awsProperties, "AwsPropertiesshould test connection");
        var container = (LocalStackContainer) localStackInfo.container();
        log.info("Container status check completed - LocalStack: {}", container.getEndpoint());
    }

    @Test
    @Order(2)
    @DisplayName("test connection test")
    void testSingleFileUpload() throws IOException {
        String testContent = "test test - " + System.currentTimeMillis();
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "test-single-file.txt";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", testData);
        String uploadResult = assertDoesNotThrow(() -> s3FileService.uploadFile(fileName, mockFile), "test connectionshould should not connection");
        assertNotNull(uploadResult, "connection resultshould nullshould file connection");
        assertFalse(uploadResult.trim().isEmpty(), "connection resultshould file connection");
        boolean exists = assertDoesNotThrow(() -> s3FileService.fileExists(fileName));
        assertTrue(exists, "connection testshould file connection");
        log.info(" test connection success: {} -> {}", fileName, uploadResult);
    }

    @Test
    @Order(3)
    @DisplayName("test file should test validation test")
    void testFileDownloadAndVerification() throws IOException {
        String originalContent = "file test - " + System.currentTimeMillis() + "\nfile test";
        byte[] originalData = originalContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "download-test.txt";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", originalData);
        String uploadResult = s3FileService.uploadFile(fileName, mockFile);
        assertNotNull(uploadResult, "test connectionshould should not connection");
        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> s3FileService.downloadFile(fileName), "test fileshould should not connection");
        assertTrue(downloadResult.isPresent(), "file shouldshould file connection");
        byte[] downloadedData = downloadResult.get().readAllBytes();
        String downloadedContent = new String(downloadedData, StandardCharsets.UTF_8);
        assertEquals(originalContent, downloadedContent, "file testshould connection file connection");
        assertEquals(originalData.length, downloadedData.length, "testshould file connection");
        log.info(" test file should validation completed - test: {} bytes", downloadedData.length);
    }

    @Test
    @Order(4)
    @DisplayName("test deletion test")
    void testFileDelete() throws IOException {
        String testContent = "deletion test";
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        String fileName = "delete-test.txt";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", testData);
        s3FileService.uploadFile(fileName, mockFile);
        assertTrue(s3FileService.fileExists(fileName), "deletion should testshould file connection");
        boolean deleteResult = assertDoesNotThrow(() -> s3FileService.deleteFile(fileName), "test deletionshould should not connection");
        assertTrue(deleteResult, "deletion should should not connection");
        boolean existsAfterDelete = s3FileService.fileExists(fileName);
        assertFalse(existsAfterDelete, "deletion should testshould file connection");
        log.info(" test deletion test completed: {}", fileName);
    }

    @Test
    @Order(5)
    @DisplayName("connection test connection/file test")
    void testLargeFileHandling() throws IOException {
        int fileSize = 2 * 1024 * 1024;
        byte[] largeData = new byte[fileSize];
        for (int i = 0; i < fileSize; i++) largeData[i] = (byte) ((i % 256) ^ (i / 1024));
        String fileName = "large-file-test.bin";
        MultipartFile mockFile = new MockMultipartFile("file", fileName, "application/octet-stream", largeData);
        long uploadStart = System.currentTimeMillis();
        String uploadResult = assertDoesNotThrow(() -> s3FileService.uploadFile(fileName, mockFile), "connection test connectionshould should not connection");
        long uploadTime = System.currentTimeMillis() - uploadStart;
        assertNotNull(uploadResult, "connection resultshould nullneeds to be added connection");
        long downloadStart = System.currentTimeMillis();
        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> s3FileService.downloadFile(fileName), "connection test fileshould should not connection");
        assertTrue(downloadResult.isPresent(), "file resultshould file connection");
        byte[] downloadedData = downloadResult.get().readAllBytes();
        long downloadTime = System.currentTimeMillis() - downloadStart;
        assertEquals(fileSize, downloadedData.length, "file testshould connection file connection");
        for (int i = 0; i < fileSize; i += 1024) assertEquals(largeData[i], downloadedData[i], "file testshould connection file connection (test: " + i + ")");
        log.info(" connection test processing completed - test: {} MB, connection: {}ms, file: {}ms", fileSize / (1024 * 1024), uploadTime, downloadTime);
    }

    @Test
    @Order(6)
    @DisplayName("connection test connection test")
    void testMultipleFileFormats() throws IOException {
        String textContent = "Endpoint! test connection test.\ntest should testshould connection.";
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
            String uploadResult = assertDoesNotThrow(() -> s3FileService.uploadFile(fileNames[index], mockFile), fileNames[index] + " connectionshould should not connection");
            assertNotNull(uploadResult, fileNames[i] + " connection resultshould nullshould file connection");
            assertTrue(s3FileService.fileExists(fileNames[i]), fileNames[i] + " testshould file connection");
        }
        Optional<InputStream> textResult = s3FileService.downloadFile(textFileName);
        assertTrue(textResult.isPresent(), "connection testshould file connection");
        byte[] downloadedText = textResult.get().readAllBytes();
        assertEquals(textContent, new String(downloadedText, StandardCharsets.UTF_8), "connection testshould file connection");
        Optional<InputStream> jsonResult = s3FileService.downloadFile(jsonFileName);
        assertTrue(jsonResult.isPresent(), "JSON testshould file connection");
        byte[] downloadedJson = jsonResult.get().readAllBytes();
        assertEquals(jsonContent, new String(downloadedJson, StandardCharsets.UTF_8), "JSON testshould file connection");
        Optional<InputStream> binaryResult = s3FileService.downloadFile(binaryFileName);
        assertTrue(binaryResult.isPresent(), "should testshould file connection");
        byte[] downloadedBinary = binaryResult.get().readAllBytes();
        assertArrayEquals(binaryData, downloadedBinary, "should testshould file connection");
        log.info(" connection test connection/file test completed");
    }

    @Test
    @Order(7)
    @DisplayName("test connection test")
    void testConcurrentFileUpload() {
        int numberOfFiles = 10;
        CompletableFuture<String>[] uploadFutures = new CompletableFuture[numberOfFiles];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfFiles; i++) {
            String fileName = "concurrent-test-" + i + ".txt";
            String content = "test connection test #" + i + " - " + System.currentTimeMillis();
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            uploadFutures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", data);
                    return s3FileService.uploadFile(fileName, mockFile);
                } catch (Exception e) {
                    throw new RuntimeException("test connection failure: " + fileName, e);
                }
            });
        }
        CompletableFuture.allOf(uploadFutures).join();
        long totalTime = System.currentTimeMillis() - startTime;
        for (int i = 0; i < numberOfFiles; i++) {
            final int index = i;
            String uploadResult = assertDoesNotThrow(() -> uploadFutures[index].get(5, TimeUnit.SECONDS), "connection #" + index + "should should not connection");
            assertNotNull(uploadResult, "connection result #" + index + "should nullshould file connection");
            String fileName = "concurrent-test-" + index + ".txt";
            assertTrue(s3FileService.fileExists(fileName), "connection test #" + index + "should file connection");
        }
        double avgTimePerFile = (double) totalTime / numberOfFiles;
        log.info(" test connection test completed - {} test, should test: {}ms, test: {:.2f}ms/test", numberOfFiles, totalTime, avgTimePerFile);
    }

    @Test
    @Order(8)
    @DisplayName("test processing test")
    void testErrorHandling() {
        String nonExistentFile = "non-existent-file-" + System.currentTimeMillis() + ".txt";
        Optional<InputStream> downloadResult = assertDoesNotThrow(() -> s3FileService.downloadFile(nonExistentFile));
        assertFalse(downloadResult.isPresent(), "file test should Optionalshould file connection");
        boolean deleteResult = assertDoesNotThrow(() -> s3FileService.deleteFile(nonExistentFile), "file test deletionshould should not connection");
        log.info("file test deletion result: {}", deleteResult);
        boolean exists = assertDoesNotThrow(() -> s3FileService.fileExists(nonExistentFile));
        assertFalse(exists, "file test file connection Endpoint connection");
        log.info(" test processing test completed");
    }

    @Test
    @Order(9)
    @DisplayName("test test processing test")
    void testSpecialCharactersInFileName() throws IOException {
        String[] specialFileNames = {
                "testtest.txt",
                "file-with-dashes.txt",
                "file_with_underscores.txt",
                "file.with.dots.txt",
                "file (with spaces).txt"
        };

        String testContent = "test test";
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        for (String fileName : specialFileNames) {
            try {
                MultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", testData);
                String uploadResult = assertDoesNotThrow(() -> s3FileService.uploadFile(fileName, mockFile), "test test connectionshould should not connection: " + fileName);
                assertNotNull(uploadResult, "connection resultshould nullshould file connection: " + fileName);
                boolean exists = s3FileService.fileExists(fileName);
                assertTrue(exists, "connection testshould file connection: " + fileName);
                Optional<InputStream> downloadResult = s3FileService.downloadFile(fileName);
                assertTrue(downloadResult.isPresent(), "testshould file connection: " + fileName);
                byte[] downloadedData = downloadResult.get().readAllBytes();
                assertEquals(testContent, new String(downloadedData, StandardCharsets.UTF_8), "file testshould connection file connection: " + fileName);
                log.info(" test test processing success: {}", fileName);
            } catch (Exception e) {
                log.warn("test test processing failure (test should): {} - {}", fileName, e.getMessage());
            }
        }
        log.info(" test test processing test completed");
    }
}