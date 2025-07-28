package com.genius.primavera.application.aws;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface S3FileService {
    
    /**
     * 파일을 S3에 업로드합니다.
     * 
     * @param keyName S3 객체 키 (파일 경로)
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL
     */
    String uploadFile(String keyName, MultipartFile file);
    
    /**
     * 파일을 S3에 업로드합니다.
     * 
     * @param keyName S3 객체 키 (파일 경로)
     * @param inputStream 업로드할 파일의 InputStream
     * @param contentLength 파일 크기
     * @param contentType 파일의 MIME 타입
     * @return 업로드된 파일의 URL
     */
    String uploadFile(String keyName, InputStream inputStream, long contentLength, String contentType);
    
    /**
     * S3에서 파일을 다운로드합니다.
     * 
     * @param keyName S3 객체 키 (파일 경로)
     * @return 파일의 InputStream (Optional)
     */
    Optional<InputStream> downloadFile(String keyName);
    
    /**
     * S3에서 파일을 삭제합니다.
     * 
     * @param keyName S3 객체 키 (파일 경로)
     * @return 삭제 성공 여부
     */
    boolean deleteFile(String keyName);
    
    /**
     * S3에서 파일 존재 여부를 확인합니다.
     * 
     * @param keyName S3 객체 키 (파일 경로)
     * @return 파일 존재 여부
     */
    boolean fileExists(String keyName);
    
    /**
     * S3 버킷의 파일 목록을 조회합니다.
     * 
     * @param prefix 접두사 필터 (선택사항)
     * @return 파일 키 목록
     */
    List<String> listFiles(String prefix);
    
    /**
     * 파일의 메타데이터를 조회합니다.
     * 
     * @param keyName S3 객체 키 (파일 경로)
     * @return 파일 크기, 수정일 등 메타데이터
     */
    Optional<S3FileMetadata> getFileMetadata(String keyName);
}