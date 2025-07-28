package com.genius.primavera.test;

/**
 * TestContainers에서 지원하는 컨테이너 타입들
 * 
 * 각 타입은 특정 인프라 컴포넌트에 대응됩니다:
 * - MARIADB: 관계형 데이터베이스 (MariaDB 11.4.7)
 * - REDIS: 인메모리 캐시/세션 저장소 (Redis 7-alpine)
 * - KAFKA: 메시지 큐/이벤트 스트리밍 (Confluent Kafka)
 * - POSTGRESQL: 관계형 데이터베이스 대안 (PostgreSQL 15-alpine)
 * 
 * 사용 예시:
 * @EnablePrimaveraTestcontainers({ContainerType.MARIADB, ContainerType.REDIS})
 */
public enum ContainerType {
    
    /**
     * MariaDB 11.4.7 컨테이너
     * - 포트: 3306
     * - 데이터베이스: primavera
     * - 사용자: primavera/primavera
     * - Spring 프로퍼티: spring.datasource.*
     */
    MARIADB("mariadb:11.4.7", 3306),
    
    /**
     * Redis 7-alpine 컨테이너
     * - 포트: 6379
     * - Spring 프로퍼티: spring.data.redis.*
     */
    REDIS("redis:7-alpine", 6379),
    
    /**
     * Confluent Kafka 컨테이너
     * - 포트: 9092 (동적 할당)
     * - Spring 프로퍼티: spring.kafka.bootstrap-servers
     */
    KAFKA("confluentinc/cp-kafka:latest", 9092),
    
    /**
     * PostgreSQL 15-alpine 컨테이너
     * - 포트: 5432
     * - 데이터베이스: testdb
     * - 사용자: test/test
     * - Spring 프로퍼티: spring.datasource.*
     */
    POSTGRESQL("postgres:15-alpine", 5432);
    
    private final String dockerImage;
    private final int defaultPort;
    
    ContainerType(String dockerImage, int defaultPort) {
        this.dockerImage = dockerImage;
        this.defaultPort = defaultPort;
    }
    
    public String getDockerImage() {
        return dockerImage;
    }
    
    public int getDefaultPort() {
        return defaultPort;
    }
    
    /**
     * 컨테이너 타입에 따른 설명 반환
     */
    public String getDescription() {
        return switch (this) {
            case MARIADB -> "MariaDB 11.4.7 관계형 데이터베이스";
            case REDIS -> "Redis 7 인메모리 캐시 서버";
            case KAFKA -> "Confluent Kafka 메시지 브로커";
            case POSTGRESQL -> "PostgreSQL 15 관계형 데이터베이스";
        };
    }
    
    /**
     * 컨테이너가 제공하는 주요 Spring 프로퍼티 키들
     */
    public String[] getSpringPropertyKeys() {
        return switch (this) {
            case MARIADB, POSTGRESQL -> new String[]{
                "spring.datasource.url",
                "spring.datasource.username", 
                "spring.datasource.password",
                "spring.datasource.driver-class-name"
            };
            case REDIS -> new String[]{
                "spring.data.redis.host",
                "spring.data.redis.port"
            };
            case KAFKA -> new String[]{
                "spring.kafka.bootstrap-servers"
            };
        };
    }
}