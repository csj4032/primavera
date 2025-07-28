package com.genius.primavera.application.aws;

import com.genius.primavera.infrastructure.aws.S3Properties;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import io.awspring.cloud.s3.ObjectMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {

    private final S3Template s3Template;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public String uploadFile(String keyName, MultipartFile file) {
        try {
            return uploadFile(keyName, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            log.error("Failed to upload file: {}", keyName, e);
            throw new RuntimeException("Failed to upload file: " + keyName, e);
        }
    }

    @Override
    public String uploadFile(String keyName, InputStream inputStream, long contentLength, String contentType) {
        try {
            S3Resource s3Resource = s3Template.upload(
                s3Properties.bucketName(), 
                keyName, 
                inputStream,
                ObjectMetadata.builder()
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build()
            );
            
            String fileUrl = s3Resource.getURL().toString();
            log.info("Successfully uploaded file: {} to URL: {}", keyName, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("Failed to upload file: {}", keyName, e);
            throw new RuntimeException("Failed to upload file: " + keyName, e);
        }
    }

    @Override
    public Optional<InputStream> downloadFile(String keyName) {
        try {
            S3Resource s3Resource = s3Template.download(s3Properties.bucketName(), keyName);
            if (s3Resource.exists()) {
                return Optional.of(s3Resource.getInputStream());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to download file: {}", keyName, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteFile(String keyName) {
        try {
            s3Template.deleteObject(s3Properties.bucketName(), keyName);
            log.info("Successfully deleted file: {}", keyName);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file: {}", keyName, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String keyName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(s3Properties.bucketName())
                .key(keyName)
                .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check file existence: {}", keyName, e);
            return false;
        }
    }

    @Override
    public List<String> listFiles(String prefix) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(s3Properties.bucketName());
            
            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }
            
            ListObjectsV2Request request = requestBuilder.build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            
            return response.contents().stream()
                .map(S3Object::key)
                .toList();
                
        } catch (Exception e) {
            log.error("Failed to list files with prefix: {}", prefix, e);
            throw new RuntimeException("Failed to list files", e);
        }
    }

    @Override
    public Optional<S3FileMetadata> getFileMetadata(String keyName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(s3Properties.bucketName())
                .key(keyName)
                .build();
            
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            
            S3FileMetadata metadata = new S3FileMetadata(
                keyName,
                response.contentLength(),
                response.lastModified(),
                response.contentType(),
                response.eTag()
            );
            
            return Optional.of(metadata);
            
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get file metadata: {}", keyName, e);
            return Optional.empty();
        }
    }
}