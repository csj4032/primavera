package com.genius.primavera.application.aws;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface S3FileService {
    String uploadFile(String keyName, MultipartFile file);
    String uploadFile(String keyName, InputStream inputStream, long contentLength, String contentType);
    Optional<InputStream> downloadFile(String keyName);
    boolean deleteFile(String keyName);
    boolean fileExists(String keyName);
    List<String> listFiles(String prefix);
    Optional<S3FileMetadata> getFileMetadata(String keyName);
}