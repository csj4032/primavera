package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

/**
 * PostgreSQL 컨테이너 전용 설정
 * DatabaseContainerSpec의 공통 설정에 PostgreSQL 고유 설정을 추가합니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties
@Validated
public class PostgreSqlContainerSpec extends DatabaseContainerSpec {

    /**
     * PostgreSQL 로케일 설정
     * 기본값: "en_US.UTF-8"
     * 데이터베이스의 기본 로케일
     */
    @NotBlank(message = "Locale cannot be blank")
    private String locale = "en_US.UTF-8";

    /**
     * 문자 인코딩
     * 기본값: "UTF8"
     * 지원 인코딩: UTF8, LATIN1, SQL_ASCII 등
     */
    @Pattern(regexp = "^(UTF8|LATIN1|SQL_ASCII|EUC_KR|WIN1252)$", message = "Invalid encoding")
    private String encoding = "UTF8";

    /**
     * 템플릿 데이터베이스
     * 기본값: "template1"
     * 새 데이터베이스 생성 시 사용할 템플릿
     */
    @NotBlank(message = "Template database cannot be blank")
    private String templateDatabase = "template1";

    /**
     * 공유 버퍼 크기
     * 기본값: "128MB"
     * PostgreSQL의 공유 메모리 버퍼 크기
     */
    @Pattern(regexp = "^\\d+[kKmMgG][bB]?$", message = "Invalid shared buffers format (e.g., 128MB)")
    private String sharedBuffers = "128MB";

    /**
     * 작업 메모리 크기
     * 기본값: "4MB"
     * 정렬, 해시 등 작업에 사용되는 메모리
     */
    @Pattern(regexp = "^\\d+[kKmMgG][bB]?$", message = "Invalid work mem format (e.g., 4MB)")
    private String workMem = "4MB";

    /**
     * 유지 관리 작업 메모리 크기
     * 기본값: "64MB"
     * VACUUM, CREATE INDEX 등에 사용되는 메모리
     */
    @Pattern(regexp = "^\\d+[kKmMgG][bB]?$", message = "Invalid maintenance work mem format (e.g., 64MB)")
    private String maintenanceWorkMem = "64MB";

    /**
     * WAL(Write Ahead Log) 버퍼 크기
     * 기본값: "16MB"
     */
    @Pattern(regexp = "^\\d+[kKmMgG][bB]?$", message = "Invalid WAL buffers format (e.g., 16MB)")
    private String walBuffers = "16MB";

    /**
     * 체크포인트 세그먼트 수
     * 기본값: 32
     * WAL 파일 개수 제한
     */
    @Min(value = 1, message = "Checkpoint segments must be at least 1")
    @Max(value = 256, message = "Checkpoint segments must not exceed 256")
    private Integer checkpointSegments = 32;

    /**
     * 최대 연결 수
     * 기본값: 100
     * 최소: 10, 최대: 1000
     */
    @Min(value = 10, message = "Max connections must be at least 10")
    @Max(value = 1000, message = "Max connections must not exceed 1000")
    private Integer maxConnections = 100;

    /**
     * 슬로우 쿼리 로깅 임계값 (밀리초)
     * 기본값: null (비활성화)
     * 설정 시 해당 시간보다 오래 걸리는 쿼리 로깅
     */
    @Min(value = 0, message = "Log min duration must be non-negative")
    private Integer logMinDurationStatement;

    /**
     * 자동 VACUUM 활성화
     * 기본값: true
     * 자동 정리 작업 실행 여부
     */
    private Boolean autovacuum = true;

    /**
     * 통계 수집 활성화
     * 기본값: true
     * 쿼리 플래너를 위한 통계 정보 수집
     */
    private Boolean trackActivities = true;

    /**
     * 쿼리 통계 수집 활성화
     * 기본값: false
     * 실행된 SQL 문의 통계 수집
     */
    private Boolean trackStatements = false;

    /**
     * 체크섬 활성화
     * 기본값: true
     * 데이터 페이지 무결성 검증
     */
    private Boolean dataChecksums = true;

    /**
     * SSL 모드
     * 기본값: PREFER
     * SSL 연결 설정
     */
    private SslMode sslMode = SslMode.PREFER;

    /**
     * 타임존 설정
     * 기본값: "Asia/Seoul"
     */
    private String timezone = "Asia/Seoul";

    /**
     * 날짜 스타일
     * 기본값: "ISO, YMD"
     */
    private String dateStyle = "ISO, YMD";

    /**
     * 확장 기능 목록
     * 기본값: 빈 배열
     * 자동으로 설치할 PostgreSQL 확장들
     */
    private String[] extensions = {};

    /**
     * PostgreSQL 로그 레벨 옵션
     */
    public enum PostgreSqlLogLevel {
        DEBUG5, DEBUG4, DEBUG3, DEBUG2, DEBUG1,
        INFO, NOTICE, WARNING, ERROR, LOG, FATAL, PANIC
    }

    /**
     * SSL 모드 옵션
     */
    public enum SslMode {
        /**
         * SSL 비활성화
         */
        DISABLE,
        /**
         * SSL 허용 (기본값 아님)
         */
        ALLOW,
        /**
         * SSL 우선 (기본값)
         */
        PREFER,
        /**
         * SSL 필수 (검증 안함)
         */
        REQUIRE,
        /**
         * SSL 필수 (CA 검증)
         */
        VERIFY_CA,
        /**
         * SSL 필수 (전체 검증)
         */
        VERIFY_FULL
    }
}