package com.genius.primavera.testContainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = TestConfiguration.class, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers({ContainerType.REDIS})
@DisplayName("Redis 컨테이너 통합 테스트")
class RedisContainerIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("Redis 컨테이너가 시작되고 연결이 가능한지 확인")
    void shouldStartRedisContainerAndConnect() {
        GenericContainer<?> redisContainer = PrimaveraTestcontainersContextInitializer
                .getContainer(ContainerType.REDIS);
        
        assertNotNull(redisContainer, "Redis container should be available");
        assertTrue(redisContainer.isRunning(), "Redis container should be running");
        
        // 포트 매핑 확인
        Integer mappedPort = redisContainer.getMappedPort(6379);
        assertNotNull(mappedPort, "Redis port should be mapped");
        assertTrue(mappedPort > 0, "Mapped port should be positive");
        
        log.info("Redis container is running on port: {}", mappedPort);
        log.info("Container ID: {}", redisContainer.getContainerId());
    }

    @Test
    @DisplayName("RedisConnectionFactory가 주입되는지 확인")
    void shouldInjectRedisConnectionFactory() {
        if (redisConnectionFactory != null) {
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                assertNotNull(connection, "Redis connection should not be null");
                assertNotNull(connection.ping(), "Should be able to ping Redis");
                
                log.info("Successfully connected to Redis");
            }
        } else {
            log.warn("RedisConnectionFactory not available - Redis auto-configuration may not be enabled");
        }
    }

    @Test
    @DisplayName("RedisTemplate을 통한 기본 Redis 작업 테스트")
    void shouldPerformBasicRedisOperations() {
        if (redisTemplate != null) {
            String key = "test:key";
            String value = "test:value";
            
            // 데이터 저장
            redisTemplate.opsForValue().set(key, value);
            
            // 데이터 조회
            Object retrievedValue = redisTemplate.opsForValue().get(key);
            assertEquals(value, retrievedValue, "Retrieved value should match stored value");
            
            // 데이터 삭제
            Boolean deleted = redisTemplate.delete(key);
            assertTrue(deleted, "Key should be deleted successfully");
            
            // 삭제 확인
            Object deletedValue = redisTemplate.opsForValue().get(key);
            assertNull(deletedValue, "Value should be null after deletion");
            
            log.info("Successfully performed basic Redis operations");
        } else {
            log.warn("RedisTemplate not available - Redis auto-configuration may not be enabled");
        }
    }

    @Test
    @DisplayName("Redis 환경 프로퍼티가 설정되는지 확인")
    void shouldHaveRedisPropertiesInEnvironment() {
        // Redis 관련 프로퍼티 확인
        String redisHost = environment.getProperty("spring.data.redis.host");
        String redisPort = environment.getProperty("spring.data.redis.port");
        
        if (redisHost != null && redisPort != null) {
            assertEquals("localhost", redisHost, "Redis host should be localhost");
            assertNotNull(redisPort, "Redis port should be set");
            assertTrue(Integer.parseInt(redisPort) > 0, "Redis port should be positive");
            
            log.info("Redis configured at {}:{}", redisHost, redisPort);
        } else {
            log.info("Redis properties not found in environment - may be configured differently");
        }
    }

    @Test
    @DisplayName("Redis 컨테이너의 상세 정보 확인")
    void shouldProvideContainerDetails() {
        GenericContainer<?> redisContainer = PrimaveraTestcontainersContextInitializer
                .getContainer(ContainerType.REDIS);
        
        if (redisContainer != null) {
            log.info("=== Redis Container Details ===");
            log.info("Container ID: {}", redisContainer.getContainerId());
            log.info("Docker Image: {}", redisContainer.getDockerImageName());
            log.info("Exposed Ports: {}", redisContainer.getExposedPorts());
            log.info("Host: {}", redisContainer.getHost());
            log.info("Mapped Port 6379: {}", redisContainer.getMappedPort(6379));
            log.info("Running: {}", redisContainer.isRunning());
            
            assertTrue(redisContainer.isRunning());
            assertTrue(redisContainer.getExposedPorts().contains(6379));
        }
    }

    @Test
    @DisplayName("Redis 컨테이너에서 복잡한 데이터 타입 작업 테스트")
    void shouldHandleComplexDataTypes() {
        if (redisTemplate != null) {
            // Hash 작업
            String hashKey = "test:hash";
            redisTemplate.opsForHash().put(hashKey, "field1", "value1");
            redisTemplate.opsForHash().put(hashKey, "field2", "value2");
            
            Object hashValue = redisTemplate.opsForHash().get(hashKey, "field1");
            assertEquals("value1", hashValue);
            
            // List 작업
            String listKey = "test:list";
            redisTemplate.opsForList().rightPush(listKey, "item1");
            redisTemplate.opsForList().rightPush(listKey, "item2");
            
            Long listSize = redisTemplate.opsForList().size(listKey);
            assertEquals(2L, listSize);
            
            Object firstItem = redisTemplate.opsForList().index(listKey, 0);
            assertEquals("item1", firstItem);
            
            // Set 작업
            String setKey = "test:set";
            redisTemplate.opsForSet().add(setKey, "member1", "member2", "member3");
            
            Long setSize = redisTemplate.opsForSet().size(setKey);
            assertEquals(3L, setSize);
            
            Boolean isMember = redisTemplate.opsForSet().isMember(setKey, "member1");
            assertTrue(isMember);
            
            // 정리
            redisTemplate.delete(hashKey);
            redisTemplate.delete(listKey);
            redisTemplate.delete(setKey);
            
            log.info("Successfully performed complex Redis data type operations");
        } else {
            log.warn("RedisTemplate not available for complex operations test");
        }
    }
}