package com.genius.primavera.testcontainer.v3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * TestContainer 설정 Properties (v3)
 * 
 * <p>application-test.yml에서 컨테이너별 상세 설정을 읽어옵니다.</p>
 * 
 * <h3>설정 예시:</h3>
 * <pre>
 * testcontainer:
 *   containers:
 *     primaryDb:
 *       image: "mariadb:11.4.7"
 *       database: "primary"
 *       username: "primary_user"
 *       password: "primary_pass"
 *       init-script: "sql/primary-init.sql"
 *       environment:
 *         MYSQL_CHARSET: "utf8mb4"
 *         MYSQL_COLLATION: "utf8mb4_unicode_ci"
 *       network-aliases:
 *         - "primary-db"
 *         - "main-db"
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "testcontainer")
public class TestContainerProperties {
    
    /**
     * 컨테이너별 설정 맵
     * Key: 컨테이너 이름, Value: 컨테이너 설정
     */
    @NestedConfigurationProperty
    private Map<String, ContainerConfig> containers = new HashMap<>();
    
    /**
     * 개별 컨테이너 설정
     */
    @Data
    public static class ContainerConfig {
        /**
         * Docker 이미지 (예: "mariadb:11.4.7")
         */
        private String image;
        
        /**
         * 데이터베이스 이름 (SQL 데이터베이스용)
         */
        private String database = "test";
        
        /**
         * 사용자명 (데이터베이스용)
         */
        private String username = "test";
        
        /**
         * 비밀번호 (데이터베이스, Redis 등)
         */
        private String password = "test";
        
        /**
         * 초기화 SQL 스크립트 경로 (SQL 데이터베이스용)
         */
        private String initScript;
        
        /**
         * 컨테이너 환경 변수
         */
        private Map<String, String> environment = new HashMap<>();
        
        /**
         * 네트워크 별칭
         */
        private String[] networkAliases = {};
        
        /**
         * 컨테이너 시작 타임아웃 (초)
         */
        private int startupTimeout = 60;
        
        /**
         * 포트 매핑 (컨테이너 포트 -> 호스트 포트)
         * 비어있으면 랜덤 포트 사용
         */
        private Map<Integer, Integer> ports = new HashMap<>();
        
        /**
         * 추가 설정 프로퍼티
         */
        private Map<String, Object> properties = new HashMap<>();
    }
}