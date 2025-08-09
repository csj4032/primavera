package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

/**
 * Redis 컨테이너 전용 설정
 * BaseContainerSpec의 공통 설정에 Redis 고유 설정을 추가합니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties
@Validated
public class RedisContainerSpec extends BaseContainerSpec {
    
    /**
     * Redis 비밀번호 (선택사항)
     * 설정하지 않으면 비밀번호 없이 접근 가능
     * 최소 6자 이상 권장
     */
    @Size(min = 6, message = "Redis password must be at least 6 characters")
    private String password;
    
    /**
     * 최대 메모리 사용량
     * 기본값: "128m"
     * 형식: 숫자 + 단위(k, m, g) 예: "128m", "1g", "512k"
     */
    @Pattern(regexp = "^\\d+[kmgKMG]?$", message = "Invalid memory format (use: 128m, 1g, etc.)")
    private String maxMemory = "128m";
    
    /**
     * 메모리 부족 시 데이터 제거 정책
     * 기본값: ALLKEYS_LRU (모든 키에 대해 LRU 적용)
     */
    private MaxMemoryPolicy maxMemoryPolicy = MaxMemoryPolicy.ALLKEYS_LRU;
    
    /**
     * RDB 지속성 활성화 (스냅샷 저장)
     * 기본값: false
     * true로 설정하면 주기적으로 데이터를 디스크에 저장
     */
    private Boolean persistenceEnabled = false;
    
    /**
     * AOF(Append Only File) 지속성 활성화
     * 기본값: false
     * true로 설정하면 모든 쓰기 명령을 로그에 저장
     */
    private Boolean aofEnabled = false;
    
    /**
     * 데이터베이스 개수
     * 기본값: 16
     * 최소: 1, 최대: 16384
     */
    @Min(value = 1, message = "Database count must be at least 1")
    @Max(value = 16384, message = "Database count must not exceed 16384")
    private Integer databases = 16;
    
    /**
     * Redis 서버 포트
     * 기본값: 6379
     */
    @Min(value = 1024, message = "Port must be at least 1024")
    @Max(value = 65535, message = "Port must not exceed 65535")
    private Integer port = 6379;
    
    /**
     * 클라이언트 타임아웃 (초)
     * 기본값: 0 (타임아웃 없음)
     * 0이면 비활성화, 양수면 해당 초 후 연결 해제
     */
    @Min(value = 0, message = "Timeout must be non-negative")
    private Integer timeout = 0;
    
    /**
     * TCP keepalive 설정 (초)
     * 기본값: 300
     * 0이면 비활성화
     */
    @Min(value = 0, message = "TCP keepalive must be non-negative")
    private Integer tcpKeepAlive = 300;
    
    /**
     * Redis 로그 레벨
     * 기본값: NOTICE
     */
    private RedisLogLevel redisLogLevel = RedisLogLevel.NOTICE;
    
    /**
     * AOF(Append Only File) 활성화
     * 기본값: false
     * 데이터 영속성을 위한 AOF 로깅 설정
     */
    private Boolean appendOnlyEnabled = false;
    
    /**
     * Redis 메모리 부족 시 제거 정책 옵션
     */
    public enum MaxMemoryPolicy {
        /** 제거하지 않음 (메모리 부족 시 오류 발생) */
        NOEVICTION,
        /** 모든 키에 대해 LRU(Least Recently Used) 적용 */
        ALLKEYS_LRU,
        /** TTL이 설정된 키에 대해 LRU 적용 */
        VOLATILE_LRU,
        /** 모든 키 중 랜덤 제거 */
        ALLKEYS_RANDOM,
        /** TTL이 설정된 키 중 랜덤 제거 */
        VOLATILE_RANDOM,
        /** TTL이 가장 짧은 키 제거 */
        VOLATILE_TTL,
        /** 모든 키에 대해 LFU(Least Frequently Used) 적용 */
        ALLKEYS_LFU,
        /** TTL이 설정된 키에 대해 LFU 적용 */
        VOLATILE_LFU
    }
    
    /**
     * Redis 로그 레벨 옵션
     */
    public enum RedisLogLevel {
        DEBUG, VERBOSE, NOTICE, WARNING
    }
}