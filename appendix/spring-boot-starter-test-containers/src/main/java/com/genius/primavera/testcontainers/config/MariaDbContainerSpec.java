package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

/**
 * MariaDB 컨테이너 전용 설정
 * DatabaseContainerSpec의 공통 설정에 MariaDB 고유 설정을 추가합니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties
@Validated
public class MariaDbContainerSpec extends DatabaseContainerSpec {
    
    /**
     * MariaDB 문자셋
     * 기본값: "utf8mb4"
     * 지원 문자셋: utf8, utf8mb4, latin1
     */
    @Pattern(regexp = "^(utf8|utf8mb4|latin1|ascii|binary)$", message = "Invalid character set")
    private String characterSet = "utf8mb4";
    
    /**
     * MariaDB 콜레이션 (정렬 규칙)
     * 기본값: "utf8mb4_unicode_ci"
     */
    @NotBlank(message = "Collation cannot be blank")
    private String collation = "utf8mb4_unicode_ci";
    
    /**
     * 바이너리 로깅 활성화
     * 기본값: false
     * 복제나 백업 복구를 위해 필요한 경우 true로 설정
     */
    private Boolean binlogEnabled = false;
    
    /**
     * InnoDB 버퍼 풀 크기 (MB)
     * 기본값: 128MB
     * 최소: 64MB
     */
    @Min(value = 64, message = "Buffer pool size must be at least 64MB")
    private Integer innodbBufferPoolSize = 128;
    
    /**
     * SQL 모드 설정
     * 기본값: STRICT_TRANS_TABLES
     */
    private SqlMode sqlMode = SqlMode.STRICT_TRANS_TABLES;
    
    /**
     * 스토리지 엔진
     * 기본값: InnoDB
     */
    private StorageEngine defaultStorageEngine = StorageEngine.INNODB;
    
    /**
     * 쿼리 캐시 활성화
     * 기본값: false (MariaDB 10.1.7 이후 deprecated)
     */
    @Deprecated(since = "MariaDB 10.1.7", forRemoval = true)
    private Boolean queryCacheEnabled = false;
    
    /**
     * 최대 연결 수
     * 기본값: 151
     * 최소: 10, 최대: 100000
     */
    @Min(value = 10, message = "Max connections must be at least 10")
    @Max(value = 100000, message = "Max connections must not exceed 100000")
    private Integer maxConnections = 151;
    
    /**
     * 스레드 캐시 크기
     * 기본값: 9
     */
    @Min(value = 0, message = "Thread cache size must be non-negative")
    private Integer threadCacheSize = 9;
    
    /**
     * Root 사용자 비밀번호
     * 기본값: "root"
     * MariaDB root 사용자의 비밀번호
     */
    @Size(min = 4, message = "Root password must be at least 4 characters")
    private String rootPassword = "root";
    
    /**
     * 슬로우 쿼리 로그 활성화
     * 기본값: false
     * 성능 분석을 위한 슬로우 쿼리 로깅 설정
     */
    private Boolean slowQueryLogEnabled = false;
    
    /**
     * MariaDB SQL 모드 옵션
     */
    public enum SqlMode {
        /** 기본 모드 */
        NONE,
        /** 엄격한 트랜잭션 테이블 모드 */
        STRICT_TRANS_TABLES,
        /** 엄격한 모든 테이블 모드 */
        STRICT_ALL_TABLES,
        /** 전통적인 모드 */
        TRADITIONAL,
        /** ANSI SQL 호환 모드 */
        ANSI
    }
    
    /**
     * MariaDB 스토리지 엔진 옵션
     */
    public enum StorageEngine {
        /** InnoDB (기본값, 트랜잭션 지원) */
        INNODB,
        /** MyISAM (빠른 읽기, 트랜잭션 미지원) */
        MYISAM,
        /** Aria (MyISAM 개선판) */
        ARIA,
        /** ColumnStore (분석용) */
        COLUMNSTORE,
        /** Memory (메모리 기반) */
        MEMORY
    }
}