package com.genius.primavera.testcontainers.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 모든 컨테이너 타입에 공통으로 적용되는 기본 설정
 * IDE 자동완성과 설정 검증을 지원합니다.
 */
@Data
@Validated
public abstract class BaseContainerSpec {
    
    /**
     * Docker 이미지 이름 (예: "mariadb:11.4.7", "redis:7-alpine")
     * 기본값: 각 ContainerType의 defaultImage
     */
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9._/-]*:[a-zA-Z0-9._-]+$", 
             message = "Invalid Docker image format")
    private String image;
    
    /**
     * 컨테이너 시작 대기 시간 (초)
     * 기본값: 60초
     * 최소: 10초, 최대: 600초 (10분)
     */
    @Min(value = 10, message = "Startup timeout must be at least 10 seconds")
    @Max(value = 600, message = "Startup timeout must not exceed 600 seconds")
    private Integer startupTimeout = 60;
    
    /**
     * 컨테이너 환경 변수
     * 키와 값 모두 공백이 아닌 문자열이어야 합니다.
     */
    @NotNull
    private Map<@NotBlank String, @NotNull String> environment = new HashMap<>();
    
    /**
     * 네트워크 별칭 목록
     * 컨테이너가 네트워크 내에서 사용할 추가 호스트명들
     */
    @NotNull
    private List<@NotBlank String> networkAliases = new ArrayList<>();
    
    /**
     * 컨테이너 포트 매핑 (containerPort:hostPort)
     * 예: 3306 -> 23306 (MariaDB를 호스트의 23306 포트로 매핑)
     */
    private Map<@NotNull @Positive Integer, @Positive Integer> portBindings = new HashMap<>();
    
    /**
     * 컨테이너 재시작 정책
     * 기본값: NO (재시작하지 않음)
     */
    private RestartPolicy restartPolicy = RestartPolicy.NO;
    
    /**
     * 로그 레벨
     * 기본값: INFO
     */
    private LogLevel logLevel = LogLevel.INFO;
    
    /**
     * 컨테이너 재시작 정책 옵션
     */
    public enum RestartPolicy {
        /** 재시작하지 않음 */
        NO,
        /** 항상 재시작 */
        ALWAYS,
        /** 오류 발생 시에만 재시작 */
        ON_FAILURE,
        /** 명시적으로 중지하지 않는 한 재시작 */
        UNLESS_STOPPED
    }
    
    /**
     * 로그 레벨 옵션
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}