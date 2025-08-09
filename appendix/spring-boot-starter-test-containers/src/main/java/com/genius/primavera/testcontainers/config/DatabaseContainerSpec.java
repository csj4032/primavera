package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

/**
 * 데이터베이스 컨테이너(MariaDB, MySQL, PostgreSQL)의 공통 설정
 * BaseContainerSpec의 공통 설정에 데이터베이스 전용 설정을 추가합니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Validated
public class DatabaseContainerSpec extends BaseContainerSpec {
    
    /**
     * 데이터베이스 이름
     * 기본값: "primavera"
     * 영문자로 시작하고 영문자, 숫자, 언더스코어만 허용
     */
    @NotBlank(message = "Database name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Invalid database name")
    private String database = "primavera";
    
    /**
     * 데이터베이스 사용자명
     * 기본값: "primavera"
     * 3자 이상 64자 이하
     */
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    private String username = "primavera";
    
    /**
     * 데이터베이스 비밀번호
     * 기본값: "primavera"
     * 최소 6자 이상
     */
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password = "primavera";
    
    /**
     * 데이터베이스 초기화 스크립트 경로
     * 지원 형식: classpath:, file:, http://, https://
     * 파일 확장자: .sql 또는 .sh
     * 예: "classpath:sql/init.sql", "file:/tmp/init.sql"
     */
    @Pattern(regexp = "^(classpath:|file:|http://|https://)?.*\\.(sql|sh)$", 
             message = "Init script must be .sql or .sh file")
    private String initScript;
    
    /**
     * 연결 풀 최대 크기
     * 기본값: 10
     * 최소: 1, 최대: 100
     */
    @Min(value = 1, message = "Max connections must be at least 1")
    @Max(value = 100, message = "Max connections must not exceed 100")
    private Integer maxConnections = 10;
    
    /**
     * 연결 타임아웃 (밀리초)
     * 기본값: 30000 (30초)
     * 최소: 1초
     */
    @Min(value = 1000, message = "Connection timeout must be at least 1000ms")
    private Integer connectionTimeout = 30000;
    
    /**
     * 트랜잭션 격리 수준
     * 기본값: READ_COMMITTED
     */
    private IsolationLevel isolationLevel = IsolationLevel.READ_COMMITTED;
    
    /**
     * 자동 커밋 활성화
     * 기본값: true
     */
    private Boolean autoCommit = true;
    
    /**
     * 트랜잭션 격리 수준 옵션
     */
    public enum IsolationLevel {
        /** 커밋되지 않은 읽기 허용 */
        READ_UNCOMMITTED,
        /** 커밋된 데이터만 읽기 (기본값) */
        READ_COMMITTED,
        /** 반복 읽기 보장 */
        REPEATABLE_READ,
        /** 직렬화 가능 */
        SERIALIZABLE
    }
}