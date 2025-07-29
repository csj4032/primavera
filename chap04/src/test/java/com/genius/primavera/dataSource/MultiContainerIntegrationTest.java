package com.genius.primavera.dataSource;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import com.genius.primavera.testContainer.UnifiedTestcontainersMixin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 다중 컨테이너를 사용하는 통합 테스트 예시
 * 
 * ApplicationContextInitializer에서 MARIADB와 REDIS 컨테이너를 동시에 시작하고 관리합니다.
 * 필요에 따라 더 많은 컨테이너 타입을 추가할 수 있습니다.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers({ContainerType.MARIADB, ContainerType.REDIS})
@DisplayName("다중 컨테이너 통합 테스트")
class MultiContainerIntegrationTest implements UnifiedTestcontainersMixin {

    @Autowired
    private DataSource dataSource;
    
    // Redis 관련 Bean들은 실제 Redis 의존성이 있을 때만 사용 가능
    // @Autowired
    // private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    @DisplayName("MariaDB와 Redis 컨테이너가 모두 정상적으로 시작되는지 확인")
    void testMultipleContainersStartup() {
        // Given: ApplicationContextInitializer에서 시작된 여러 컨테이너들
        
        // When & Then: 모든 컨테이너가 정상적으로 실행 중인지 확인
        assertThat(isMariaDBRunning()).isTrue();
        assertThat(isRedisRunning()).isTrue();
        
        log.info("=== Multi-Container Test Results ===");
        log.info("MariaDB Running: {}", isMariaDBRunning());
        log.info("MariaDB URL: {}", getMariaDBJdbcUrl());
        log.info("Redis Running: {}", isRedisRunning());
        log.info("Redis Connection: {}", getRedisConnectionString());
        log.info("====================================");
        
        // 전체 컨테이너 정보 출력
        logAllContainerInfo();
    }
    
    @Test
    @DisplayName("MariaDB 연결 테스트")
    void testMariaDBConnection() throws Exception {
        // Given: 실행 중인 MariaDB 컨테이너
        assertThat(isMariaDBRunning()).isTrue();
        
        // When: DataSource를 통한 연결
        try (Connection connection = dataSource.getConnection()) {
            
            // Then: 연결 성공
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(5)).isTrue();
            
            log.info("MariaDB connection established successfully");
        }
    }
    
    @Test
    @DisplayName("Redis 연결 정보 확인")
    void testRedisConnectionInfo() {
        // Given: 실행 중인 Redis 컨테이너
        assertThat(isRedisRunning()).isTrue();
        
        // When & Then: Redis 연결 정보 확인
        String host = getRedisHost();
        Integer port = getRedisPort();
        String connectionString = getRedisConnectionString();
        
        assertThat(host).isNotNull();
        assertThat(port).isNotNull().isPositive();
        assertThat(connectionString).isNotNull().contains(":");
        
        log.info("Redis connection info - Host: {}, Port: {}, Full: {}", 
                host, port, connectionString);
        
        // 실제 Redis 클라이언트가 있다면 다음과 같이 테스트 가능:
        // redisTemplate.opsForValue().set("test-key", "test-value");
        // assertThat(redisTemplate.opsForValue().get("test-key")).isEqualTo("test-value");
    }
    
    @Test
    @DisplayName("컨테이너 타입별 개별 상태 확인")
    void testIndividualContainerStatus() {
        // Given: 시작된 컨테이너들
        
        // When & Then: 각 컨테이너의 개별 상태 확인
        assertThat(isMariaDBRunning()).as("MariaDB should be running").isTrue();
        assertThat(isRedisRunning()).as("Redis should be running").isTrue();
        
        // 시작되지 않은 컨테이너들은 false 반환
        assertThat(isKafkaRunning()).as("Kafka should not be running").isFalse();
        assertThat(isPostgreSQLRunning()).as("PostgreSQL should not be running").isFalse();
        
        log.info("Individual container status check completed");
    }
}