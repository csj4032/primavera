package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TestContainers 메인 설정 클래스
 * IDE 자동완성과 설정 검증을 완벽하게 지원합니다.
 * 
 * application.yml 예시:
 * <pre>
 * testcontainers:
 *   defaults:
 *     startupTimeout: 60
 *     imagePullPolicy: IF_NOT_PRESENT
 *   containers:
 *     userdb:
 *       type: MARIADB
 *       mariadb:
 *         database: "users" 
 *         username: "user_admin"
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "testcontainers")
@Validated
public class ContainerConfiguration {
    
    /**
     * 전역 기본값 설정
     * 모든 컨테이너에 공통으로 적용될 기본값들을 설정합니다.
     */
    @Valid
    private GlobalDefaults defaults = new GlobalDefaults();
    
    /**
     * 개별 컨테이너 설정 맵
     * key: 컨테이너 이름 (EnableTestContainers의 name과 매칭)
     * value: 해당 컨테이너의 설정
     */
    @NotNull
    @Valid
    private Map<String, ContainerInstanceConfig> containers = new HashMap<>();
    
    /**
     * 지정된 이름의 컨테이너 설정을 조회합니다.
     * 
     * @param name 컨테이너 이름
     * @return 컨테이너 설정 (있으면 반환, 없으면 Optional.empty())
     */
    public Optional<ContainerInstanceConfig> getContainerConfig(String name) {
        return Optional.ofNullable(containers.get(name));
    }
    
    /**
     * 전역 기본값 설정
     */
    @Data
    @Validated
    public static class GlobalDefaults {
        
        /**
         * 모든 컨테이너의 기본 시작 타임아웃 (초)
         * 기본값: 60초
         * 개별 컨테이너 설정에서 재정의 가능
         */
        @Min(value = 10, message = "Global startup timeout must be at least 10 seconds")
        @Max(value = 600, message = "Global startup timeout must not exceed 600 seconds")
        private Integer startupTimeout = 60;
        
        /**
         * 모든 컨테이너의 기본 환경 변수
         * 개별 컨테이너의 환경 변수와 병합됩니다.
         */
        private Map<String, String> environment = new HashMap<>();
        
        /**
         * Docker 이미지 Pull 정책
         * 기본값: IF_NOT_PRESENT
         */
        private ImagePullPolicy imagePullPolicy = ImagePullPolicy.IF_NOT_PRESENT;
        
        /**
         * 네트워크 모드
         * 기본값: BRIDGE
         */
        private NetworkMode networkMode = NetworkMode.BRIDGE;
    }
    
    /**
     * 개별 컨테이너 인스턴스 설정
     */
    @Data
    @Validated
    public static class ContainerInstanceConfig {
        
        /**
         * 컨테이너 타입 (필수)
         * EnableTestContainers 어노테이션의 type과 일치해야 합니다.
         */
        @NotNull(message = "Container type is required")
        private ContainerType type;
        
        /**
         * MariaDB 설정 (type이 MARIADB일 때만 사용)
         * MariaDB 관련 모든 설정을 포함합니다.
         */
        @Valid
        private MariaDbContainerSpec mariadb;
        
        /**
         * MySQL 설정 (type이 MYSQL일 때만 사용)
         * MySQL 관련 모든 설정을 포함합니다.
         */
        @Valid
        private MySqlContainerSpec mysql;
        
        /**
         * PostgreSQL 설정 (type이 POSTGRESQL일 때만 사용)
         * PostgreSQL 관련 모든 설정을 포함합니다.
         */
        @Valid
        private PostgreSqlContainerSpec postgresql;
        
        /**
         * Redis 설정 (type이 REDIS일 때만 사용)
         * Redis 관련 모든 설정을 포함합니다.
         */
        @Valid
        private RedisContainerSpec redis;
        
        /**
         * MongoDB 설정 (type이 MONGODB일 때만 사용)
         * MongoDB 관련 모든 설정을 포함합니다.
         */
        @Valid
        private MongoContainerSpec mongodb;
        
        /**
         * Kafka 설정 (type이 KAFKA일 때만 사용)
         * Kafka 관련 모든 설정을 포함합니다.
         */
        @Valid
        private BaseContainerSpec kafka;  // TODO: KafkaContainerSpec 구현 후 변경
        
        /**
         * Elasticsearch 설정 (type이 ELASTICSEARCH일 때만 사용)
         * Elasticsearch 관련 모든 설정을 포함합니다.
         */
        @Valid
        private BaseContainerSpec elasticsearch;  // TODO: ElasticsearchContainerSpec 구현 후 변경
        
        /**
         * Vault 설정 (type이 VAULT일 때만 사용)
         * Vault 관련 모든 설정을 포함합니다.
         */
        @Valid
        private BaseContainerSpec vault;  // TODO: VaultContainerSpec 구현 후 변경
        
        /**
         * LocalStack 설정 (type이 LOCALSTACK일 때만 사용)
         * AWS 서비스 모킹 관련 모든 설정을 포함합니다.
         */
        @Valid
        private LocalStackContainerSpec localstack;
        
        /**
         * 현재 타입에 해당하는 설정 객체를 반환합니다.
         * 
         * @return 타입별 설정 객체 (설정되지 않았으면 null)
         */
        public BaseContainerSpec getSpecForType() {
            return switch (type) {
                case MARIADB -> mariadb;
                case MYSQL -> mysql;
                case POSTGRESQL -> postgresql;
                case REDIS -> redis;
                case MONGODB -> mongodb;
                case KAFKA -> kafka;
                case ELASTICSEARCH -> elasticsearch;
                case VAULT -> vault;
                case LOCALSTACK -> localstack;
                default -> null;
            };
        }
        
        /**
         * 해당 타입의 설정이 존재하는지 확인합니다.
         * 
         * @return 설정 존재 여부
         */
        public boolean hasSpecForType() {
            return getSpecForType() != null;
        }
    }
    
    /**
     * Docker 이미지 Pull 정책
     */
    public enum ImagePullPolicy {
        /** 항상 최신 이미지를 Pull */
        ALWAYS,
        /** 로컬에 이미지가 없는 경우에만 Pull (기본값) */
        IF_NOT_PRESENT,
        /** 절대 Pull하지 않음 (로컬 이미지만 사용) */
        NEVER
    }
    
    /**
     * Docker 네트워크 모드
     */
    public enum NetworkMode {
        /** Bridge 네트워크 (기본값) */
        BRIDGE,
        /** Host 네트워크 */
        HOST,
        /** None (네트워크 없음) */
        NONE
    }
}