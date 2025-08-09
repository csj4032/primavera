package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

/**
 * MongoDB 컨테이너 전용 설정
 * BaseContainerSpec의 공통 설정에 MongoDB 고유 설정을 추가합니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties
@Validated
public class MongoContainerSpec extends BaseContainerSpec {
    
    /**
     * MongoDB 데이터베이스 이름
     * 기본값: "primavera"
     * 영문자로 시작하고 영문자, 숫자, 언더스코어, 하이픈만 허용
     */
    @NotBlank(message = "Database name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "Invalid MongoDB database name")
    private String database = "primavera";
    
    /**
     * MongoDB 사용자명
     * 기본값: "primavera"
     * 3자 이상 64자 이하
     */
    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    private String username = "primavera";
    
    /**
     * MongoDB 비밀번호
     * 기본값: "primavera"
     * 최소 6자 이상
     */
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password = "primavera";
    
    /**
     * 인증 데이터베이스
     * 기본값: "admin"
     * 사용자 인증 정보가 저장된 데이터베이스
     */
    @NotBlank(message = "Auth database cannot be blank")
    private String authDatabase = "admin";
    
    /**
     * 복제셋 이름 (선택사항)
     * 복제셋을 사용할 경우 설정
     */
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "Invalid replica set name")
    private String replicaSetName;
    
    /**
     * 샤딩 활성화
     * 기본값: false
     * true로 설정하면 샤딩 환경으로 구성
     */
    private Boolean shardingEnabled = false;
    
    /**
     * MongoDB 포트
     * 기본값: 27017
     */
    @Min(value = 1024, message = "Port must be at least 1024")
    @Max(value = 65535, message = "Port must not exceed 65535")
    private Integer port = 27017;
    
    /**
     * WiredTiger 캐시 크기 (MB)
     * 기본값: null (MongoDB가 자동 설정)
     * 설정하면 해당 크기로 캐시 제한
     */
    @Min(value = 256, message = "Cache size must be at least 256MB")
    private Integer wiredTigerCacheSizeMB;
    
    /**
     * 저널링 활성화
     * 기본값: true
     * 데이터 안정성을 위해 권장
     */
    private Boolean journalEnabled = true;
    
    /**
     * OpLog 크기 (MB)
     * 기본값: null (MongoDB가 자동 설정)
     * 복제셋 사용 시 OpLog 크기 설정
     */
    @Min(value = 100, message = "OpLog size must be at least 100MB")
    private Integer oplogSizeMB;
    
    /**
     * 인덱스 백그라운드 빌드
     * 기본값: true
     * 인덱스 생성 시 백그라운드에서 수행
     */
    private Boolean indexBuildInBackground = true;
    
    /**
     * 스토리지 엔진
     * 기본값: WIRED_TIGER
     */
    private StorageEngine storageEngine = StorageEngine.WIRED_TIGER;
    
    /**
     * 인증 메커니즘
     * 기본값: SCRAM_SHA_256
     */
    private AuthMechanism authMechanism = AuthMechanism.SCRAM_SHA_256;
    
    /**
     * MongoDB 스토리지 엔진 옵션
     */
    public enum StorageEngine {
        /** WiredTiger (기본값, 권장) */
        WIRED_TIGER,
        /** In-Memory (메모리 기반, 테스트용) */
        IN_MEMORY
    }
    
    /**
     * MongoDB 인증 메커니즘 옵션
     */
    public enum AuthMechanism {
        /** SCRAM-SHA-1 */
        SCRAM_SHA_1,
        /** SCRAM-SHA-256 (기본값, 권장) */
        SCRAM_SHA_256,
        /** MongoDB-CR (deprecated) */
        @Deprecated
        MONGODB_CR
    }
}