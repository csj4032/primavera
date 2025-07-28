package com.genius.primavera.application.aws;

import com.genius.primavera.infrastructure.aws.S3Properties;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.s3.ObjectMetadata;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class S3FileServiceUnitTest {

    @Mock
    private S3Template s3Template;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Resource s3Resource;

    private S3Properties s3Properties;
    private S3FileServiceImpl s3FileService;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String FILE_KEY = "test/sample.txt";
    private static final String FILE_CONTENT = "Hello, S3!";

    @BeforeEach
    void setUp() {
        s3Properties = new S3Properties(BUCKET_NAME, "", false);
        s3FileService = new S3FileServiceImpl(s3Template, s3Client, s3Properties);
    }

    @Test
    @Order(1)
    @DisplayName("MultipartFile 업로드 성공")
    void uploadMultipartFile_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        when(s3Template.upload(eq(BUCKET_NAME), eq(FILE_KEY), any(InputStream.class), any(ObjectMetadata.class))).thenReturn(s3Resource);
        when(s3Resource.getURL()).thenReturn(new URL("https://s3.amazonaws.com/test-bucket/test/sample.txt"));
        String result = s3FileService.uploadFile(FILE_KEY, file);
        assertThat(result).isEqualTo("https://s3.amazonaws.com/test-bucket/test/sample.txt");
        verify(s3Template).upload(eq(BUCKET_NAME), eq(FILE_KEY), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    @Order(2)
    @DisplayName("InputStream 업로드 성공")
    void uploadInputStream_Success() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        when(s3Template.upload(eq(BUCKET_NAME), eq(FILE_KEY), eq(inputStream), any(ObjectMetadata.class))).thenReturn(s3Resource);
        when(s3Resource.getURL()).thenReturn(new URL("https://s3.amazonaws.com/test-bucket/test/sample.txt"));
        String result = s3FileService.uploadFile(FILE_KEY, inputStream, FILE_CONTENT.length(), "text/plain");
        assertThat(result).isEqualTo("https://s3.amazonaws.com/test-bucket/test/sample.txt");
        verify(s3Template).upload(eq(BUCKET_NAME), eq(FILE_KEY), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    @Order(3)
    @DisplayName("파일 다운로드 성공")
    void downloadFile_Success() throws Exception {
        InputStream expectedStream = new ByteArrayInputStream(FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        when(s3Template.download(BUCKET_NAME, FILE_KEY)).thenReturn(s3Resource);
        when(s3Resource.exists()).thenReturn(true);
        when(s3Resource.getInputStream()).thenReturn(expectedStream);
        Optional<InputStream> result = s3FileService.downloadFile(FILE_KEY);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedStream);
        verify(s3Template).download(BUCKET_NAME, FILE_KEY);
    }

    @Test
    @Order(4)
    @DisplayName("존재하지 않는 파일 다운로드")
    void downloadFile_FileNotExists() {
        when(s3Template.download(BUCKET_NAME, FILE_KEY)).thenReturn(s3Resource);
        when(s3Resource.exists()).thenReturn(false);
        Optional<InputStream> result = s3FileService.downloadFile(FILE_KEY);
        assertThat(result).isEmpty();
        verify(s3Template).download(BUCKET_NAME, FILE_KEY);
    }

    @Test
    @Order(5)
    @DisplayName("파일 삭제 성공")
    void deleteFile_Success() {
        doNothing().when(s3Template).deleteObject(BUCKET_NAME, FILE_KEY);
        boolean result = s3FileService.deleteFile(FILE_KEY);
        assertThat(result).isTrue();
        verify(s3Template).deleteObject(BUCKET_NAME, FILE_KEY);
    }

    @Test
    @Order(6)
    @DisplayName("파일 존재 확인 - 존재함")
    void fileExists_True() {
        HeadObjectResponse response = HeadObjectResponse.builder().build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(response);
        boolean result = s3FileService.fileExists(FILE_KEY);
        assertThat(result).isTrue();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @Order(7)
    @DisplayName("파일 존재 확인 - 존재하지 않음")
    void fileExists_False() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        boolean result = s3FileService.fileExists(FILE_KEY);
        assertThat(result).isFalse();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @Order(8)
    @DisplayName("파일 목록 조회 성공")
    void listFiles_Success() {
        S3Object s3Object1 = S3Object.builder().key("test/file1.txt").build();
        S3Object s3Object2 = S3Object.builder().key("test/file2.txt").build();
        ListObjectsV2Response response = ListObjectsV2Response.builder().contents(s3Object1, s3Object2).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);
        List<String> result = s3FileService.listFiles("test/");
        assertThat(result).hasSize(2);
        assertThat(result).contains("test/file1.txt", "test/file2.txt");
        verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    @Order(9)
    @DisplayName("파일 메타데이터 조회 성공")
    void getFileMetadata_Success() {
        Instant lastModified = Instant.now();
        HeadObjectResponse response = HeadObjectResponse.builder().contentLength((long) FILE_CONTENT.length()).lastModified(lastModified).contentType("text/plain").eTag("\"etag123\"").build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(response);
        Optional<S3FileMetadata> result = s3FileService.getFileMetadata(FILE_KEY);
        assertThat(result).isPresent();
        S3FileMetadata metadata = result.get();
        assertThat(metadata.key()).isEqualTo(FILE_KEY);
        assertThat(metadata.size()).isEqualTo(FILE_CONTENT.length());
        assertThat(metadata.lastModified()).isEqualTo(lastModified);
        assertThat(metadata.contentType()).isEqualTo("text/plain");
        assertThat(metadata.etag()).isEqualTo("\"etag123\"");
    }

    @Test
    @Order(10)
    @DisplayName("존재하지 않는 파일의 메타데이터 조회")
    void getFileMetadata_FileNotExists() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        Optional<S3FileMetadata> result = s3FileService.getFileMetadata(FILE_KEY);
        assertThat(result).isEmpty();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }
}