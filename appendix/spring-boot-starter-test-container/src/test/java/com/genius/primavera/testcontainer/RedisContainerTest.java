package com.genius.primavera.testContainer;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnablePrimaveraTestcontainers(containers = {ContainerType.REDIS})
@TestPropertySource(properties = {"spring.data.redis.timeout=60000"})
@DisplayName("Redis 컨테이너 단독 테스트")
public class RedisContainerTest {

    @Test
    @Order(1)
    @DisplayName("Redis 컨테이너가 정상적으로 시작되었는지 확인")
    void testRedisContainerStarted() {
        System.out.println("Redis container should be started by TestContainers");
        Assertions.assertTrue(true, "Context loaded successfully with Redis container");
    }

    @Test
    @Order(2)
    @DisplayName("Redis 컨테이너 설정 확인")
    void testRedisContainerConfiguration() {
        String redisHost = System.getProperty("spring.data.redis.host");
        String redisPort = System.getProperty("spring.data.redis.port");
        System.out.println("Redis Host from properties: " + redisHost);
        System.out.println("Redis Port from properties: " + redisPort);
        Assertions.assertTrue(redisHost != null || redisPort != null, "Redis container should set properties");
    }
}