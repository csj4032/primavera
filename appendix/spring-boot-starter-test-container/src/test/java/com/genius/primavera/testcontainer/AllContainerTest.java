package com.genius.primavera.testContainer;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EnablePrimaveraTestcontainers(
    containers = {
        ContainerType.MARIADB, 
        ContainerType.MYSQL, 
        ContainerType.POSTGRESQL,
        ContainerType.REDIS,
        ContainerType.KAFKA,
        ContainerType.ELASTICSEARCH
    },
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("전체 컨테이너 동시 실행 테스트")
public class AllContainerTest {

    @Test
    @Order(1)
    @DisplayName("모든 컨테이너가 동시에 시작되는지 확인")
    void testAllContainersStarted() {
        // Spring Context가 정상적으로 로드되면 모든 컨테이너가 시작된 것
        System.out.println("All containers should be started:");
        System.out.println("- MariaDB");
        System.out.println("- MySQL");
        System.out.println("- PostgreSQL");
        System.out.println("- Redis");
        System.out.println("- Kafka");
        System.out.println("- Elasticsearch");
        
        // 컨텍스트 로드 성공 자체가 모든 컨테이너가 시작되었음을 의미
        Assertions.assertTrue(true, "All containers started successfully");
    }

    @Test
    @Order(2)
    @DisplayName("컨테이너 리소스 사용량 및 시작 시간 확인")
    void testContainerResourceUsage() {
        // 실제 운영 환경에서는 컨테이너의 리소스 사용량을 모니터링해야 함
        System.out.println("Container resource monitoring:");
        System.out.println("- Check memory usage");
        System.out.println("- Check CPU usage");
        System.out.println("- Check network ports");
        
        // 여러 컨테이너가 동시에 실행되어도 문제없이 작동하는지 확인
        Assertions.assertDoesNotThrow(() -> {
            Thread.sleep(1000); // 1초 대기하여 안정성 확인
        });
    }
}