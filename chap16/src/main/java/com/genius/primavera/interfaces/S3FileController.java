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

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "folder", defaultValue = "uploads") String folder) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String keyName = String.format("%s/%s_%s", folder, timestamp, file.getOriginalFilename());
            String fileUrl = s3FileService.uploadFile(keyName, file);
            return ResponseEntity.ok(new FileUploadResponse(keyName, fileUrl, file.getSize(), file.getContentType(), "Upload successful"));
        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileUploadResponse(null, null, 0, null, "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{*keyName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String keyName) {
        try {
            Optional<InputStream> fileStream = s3FileService.downloadFile(keyName);
            if (fileStream.isEmpty()) return ResponseEntity.notFound().build();

            Optional<S3FileMetadata> metadata = s3FileService.getFileMetadata(keyName);
            HttpHeaders headers = new HttpHeaders();
            metadata.ifPresent(meta -> {
                headers.setContentLength(meta.size());
                if (meta.contentType() != null) headers.setContentType(MediaType.parseMediaType(meta.contentType()));
            });

            String fileName = keyName.substring(keyName.lastIndexOf('/') + 1);
            headers.setContentDispositionFormData("attachment", fileName);
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(fileStream.get()));
        } catch (Exception e) {
            log.error("Failed to download file: {}", keyName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{*keyName}")
    public ResponseEntity<FileOperationResponse> deleteFile(@PathVariable String keyName) {
        try {
            boolean deleted = s3FileService.deleteFile(keyName);
            return ResponseEntity.ok(new FileOperationResponse(keyName, deleted ? "File deleted successfully" : "File deletion failed"));
        } catch (Exception e) {
            log.error("Failed to delete file: {}", keyName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FileOperationResponse(keyName, "Delete failed: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<FileListResponse> listFiles(@RequestParam(value = "prefix", required = false) String prefix) {
        try {
            List<String> files = s3FileService.listFiles(prefix);
            return ResponseEntity.ok(new FileListResponse(files, files.size(), prefix));
        } catch (Exception e) {
            log.error("Failed to list files with prefix: {}", prefix, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/metadata/{*keyName}")
    public ResponseEntity<S3FileMetadata> getFileMetadata(@PathVariable String keyName) {
        try {
            return s3FileService.getFileMetadata(keyName).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Failed to get file metadata: {}", keyName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exists/{*keyName}")
    public ResponseEntity<FileExistsResponse> checkFileExists(@PathVariable String keyName) {
        try {
            return ResponseEntity.ok(new FileExistsResponse(keyName, s3FileService.fileExists(keyName)));
        } catch (Exception e) {
            log.error("Failed to check file existence: {}", keyName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public record FileUploadResponse(
            String key,
            String url,
            long size,
            String contentType,
            String message
    ) {
    }

    public record FileOperationResponse(
            String key,
            String message
    ) {
    }

    public record FileListResponse(
            List<String> files,
            int count,
            String prefix
    ) {
    }

    public record FileExistsResponse(
            String key,
            boolean exists
    ) {
    }
}