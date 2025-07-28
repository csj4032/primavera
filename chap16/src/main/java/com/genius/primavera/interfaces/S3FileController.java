package com.genius.primavera.interfaces;

import com.genius.primavera.application.aws.S3FileMetadata;
import com.genius.primavera.application.aws.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3FileController {

    private final S3FileService s3FileService;

    /**
     * 파일을 S3에 업로드합니다.
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "uploads") String folder) {
        
        try {
            // 파일명에 타임스탬프 추가하여 중복 방지
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String keyName = String.format("%s/%s_%s", folder, timestamp, file.getOriginalFilename());
            
            String fileUrl = s3FileService.uploadFile(keyName, file);
            
            FileUploadResponse response = new FileUploadResponse(
                keyName,
                fileUrl,
                file.getSize(),
                file.getContentType(),
                "Upload successful"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            FileUploadResponse errorResponse = new FileUploadResponse(
                null, null, 0, null, "Upload failed: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * S3에서 파일을 다운로드합니다.
     */
    @GetMapping("/download/{*keyName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String keyName) {
        try {
            Optional<InputStream> fileStream = s3FileService.downloadFile(keyName);
            
            if (fileStream.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Optional<S3FileMetadata> metadata = s3FileService.getFileMetadata(keyName);
            
            HttpHeaders headers = new HttpHeaders();
            if (metadata.isPresent()) {
                S3FileMetadata meta = metadata.get();
                headers.setContentLength(meta.size());
                if (meta.contentType() != null) {
                    headers.setContentType(MediaType.parseMediaType(meta.contentType()));
                }
            }
            
            // 파일명 추출 (경로에서 마지막 부분)
            String fileName = keyName.substring(keyName.lastIndexOf('/') + 1);
            headers.setContentDispositionFormData("attachment", fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream.get()));
                    
        } catch (Exception e) {
            log.error("Failed to download file: {}", keyName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * S3에서 파일을 삭제합니다.
     */
    @DeleteMapping("/delete/{*keyName}")
    public ResponseEntity<FileOperationResponse> deleteFile(@PathVariable String keyName) {
        try {
            boolean deleted = s3FileService.deleteFile(keyName);
            
            FileOperationResponse response = new FileOperationResponse(
                keyName,
                deleted ? "File deleted successfully" : "File deletion failed"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to delete file: {}", keyName, e);
            FileOperationResponse errorResponse = new FileOperationResponse(
                keyName, "Delete failed: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * S3 파일 목록을 조회합니다.
     */
    @GetMapping("/list")
    public ResponseEntity<FileListResponse> listFiles(
            @RequestParam(value = "prefix", required = false) String prefix) {
        try {
            List<String> files = s3FileService.listFiles(prefix);
            
            FileListResponse response = new FileListResponse(
                files,
                files.size(),
                prefix
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to list files with prefix: {}", prefix, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일 메타데이터를 조회합니다.
     */
    @GetMapping("/metadata/{*keyName}")
    public ResponseEntity<S3FileMetadata> getFileMetadata(@PathVariable String keyName) {
        try {
            Optional<S3FileMetadata> metadata = s3FileService.getFileMetadata(keyName);
            
            return metadata.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
                    
        } catch (Exception e) {
            log.error("Failed to get file metadata: {}", keyName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일 존재 여부를 확인합니다.
     */
    @GetMapping("/exists/{*keyName}")
    public ResponseEntity<FileExistsResponse> checkFileExists(@PathVariable String keyName) {
        try {
            boolean exists = s3FileService.fileExists(keyName);
            
            FileExistsResponse response = new FileExistsResponse(keyName, exists);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to check file existence: {}", keyName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Response DTOs
    public record FileUploadResponse(
        String key,
        String url,
        long size,
        String contentType,
        String message
    ) {}

    public record FileOperationResponse(
        String key,
        String message
    ) {}

    public record FileListResponse(
        List<String> files,
        int count,
        String prefix
    ) {}

    public record FileExistsResponse(
        String key,
        boolean exists
    ) {}
}