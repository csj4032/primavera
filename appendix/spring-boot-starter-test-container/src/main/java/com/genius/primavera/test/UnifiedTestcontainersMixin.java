package com.genius.primavera.test;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;

/**
 * 통합 TestContainers Mixin 인터페이스
 * 
 * ApplicationContextInitializer 방식과 기존 Mixin 방식을 모두 지원합니다.
 * 
 * 1. ApplicationContextInitializer 방식 (권장):
 * @SpringBootTest
 * @EnablePrimaveraTestcontainers({ContainerType.MARIADB, ContainerType.REDIS})
 * class MyTest implements UnifiedTestcontainersMixin {
 *     // 컨테이너는 ApplicationContextInitializer에서 자동 관리
 *     // 이 인터페이스는 컨테이너 접근 헬퍼 메서드만 제공
 * }
 * 
 * 2. 기존 Mixin 방식 (호환성 유지):
 * @SpringBootTest  
 * class MyTest implements MariaDBTestcontainerMixin, RedisTestcontainerMixin {
 *     // 기존 방식대로 각 Mixin에서 @Container와 @DynamicPropertySource 사용
 * }
 */
public interface UnifiedTestcontainersMixin {
    
    /**
     * MariaDB 컨테이너 접근
     * ApplicationContextInitializer에서 시작된 컨테이너를 반환합니다.
     */
    default MariaDBContainer<?> getMariaDBContainer() {
        return PrimaveraTestcontainersContextInitializer.getMariaDBContainer();
    }
    
    /**
     * Redis 컨테이너 접근
     */
    default GenericContainer<?> getRedisContainer() {
        return PrimaveraTestcontainersContextInitializer.getRedisContainer();
    }
    
    /**
     * Kafka 컨테이너 접근
     */
    default ConfluentKafkaContainer getKafkaContainer() {
        return PrimaveraTestcontainersContextInitializer.getKafkaContainer();
    }
    
    /**
     * PostgreSQL 컨테이너 접근
     */
    default PostgreSQLContainer<?> getPostgreSQLContainer() {
        return PrimaveraTestcontainersContextInitializer.getPostgreSQLContainer();
    }
    
    /**
     * MariaDB 연결 정보 헬퍼 메서드들
     */
    default String getMariaDBJdbcUrl() {
        MariaDBContainer<?> container = getMariaDBContainer();
        return container != null ? container.getJdbcUrl() : null;
    }
    
    default String getMariaDBHost() {
        MariaDBContainer<?> container = getMariaDBContainer();
        return container != null ? container.getHost() : null;
    }
    
    default Integer getMariaDBPort() {
        MariaDBContainer<?> container = getMariaDBContainer();
        return container != null ? container.getMappedPort(3306) : null;
    }
    
    /**
     * Redis 연결 정보 헬퍼 메서드들
     */
    default String getRedisHost() {
        GenericContainer<?> container = getRedisContainer();
        return container != null ? container.getHost() : null;
    }
    
    default Integer getRedisPort() {
        GenericContainer<?> container = getRedisContainer();
        return container != null ? container.getMappedPort(6379) : null;
    }
    
    default String getRedisConnectionString() {
        GenericContainer<?> container = getRedisContainer();
        if (container != null) {
            return container.getHost() + ":" + container.getMappedPort(6379);
        }
        return null;
    }
    
    /**
     * Kafka 연결 정보 헬퍼 메서드들
     */
    default String getKafkaBootstrapServers() {
        ConfluentKafkaContainer container = getKafkaContainer();
        return container != null ? container.getBootstrapServers() : null;
    }
    
    /**
     * PostgreSQL 연결 정보 헬퍼 메서드들
     */
    default String getPostgreSQLJdbcUrl() {
        PostgreSQLContainer<?> container = getPostgreSQLContainer();
        return container != null ? container.getJdbcUrl() : null;
    }
    
    /**
     * 모든 활성 컨테이너 정보를 출력
     */
    default void logAllContainerInfo() {
        System.out.println("=== Active TestContainers ===");
        
        if (getMariaDBContainer() != null) {
            System.out.println("MariaDB: " + getMariaDBJdbcUrl());
        }
        
        if (getRedisContainer() != null) {
            System.out.println("Redis: " + getRedisConnectionString());
        }
        
        if (getKafkaContainer() != null) {
            System.out.println("Kafka: " + getKafkaBootstrapServers());
        }
        
        if (getPostgreSQLContainer() != null) {
            System.out.println("PostgreSQL: " + getPostgreSQLJdbcUrl());
        }
        
        System.out.println("=============================");
    }
    
    /**
     * 컨테이너 상태 확인
     */
    default boolean isMariaDBRunning() {
        MariaDBContainer<?> container = getMariaDBContainer();
        return container != null && container.isRunning();
    }
    
    default boolean isRedisRunning() {
        GenericContainer<?> container = getRedisContainer();
        return container != null && container.isRunning();
    }
    
    default boolean isKafkaRunning() {
        ConfluentKafkaContainer container = getKafkaContainer();
        return container != null && container.isRunning();
    }
    
    default boolean isPostgreSQLRunning() {
        PostgreSQLContainer<?> container = getPostgreSQLContainer();
        return container != null && container.isRunning();
    }
}