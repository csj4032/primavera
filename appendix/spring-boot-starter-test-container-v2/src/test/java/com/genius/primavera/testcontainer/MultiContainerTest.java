package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@EnablePrimaveraTestcontainers
@ActiveProfiles("multi")
@DisplayName("멀티 컨테이너 테스트")
public class MultiContainerTest {
    
    @Test
    @DisplayName("MariaDB 컨테이너가 실행 중인지 확인")
    public void testMariaDBContainerIsRunning() {
        GenericContainer<?> container = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_TEST);
        assertNotNull(container, "MariaDB 컨테이너가 null이어서는 안 됩니다");
        assertTrue(container.isRunning(), "MariaDB 컨테이너가 실행 중이어야 합니다");
    }
    
    @Test
    @DisplayName("Redis 컨테이너가 실행 중인지 확인")
    public void testRedisContainerIsRunning() {
        GenericContainer<?> container = PrimaveraTestcontainersInitializer.getContainer("redis", ContainerLifecycleMode.PER_TEST);
        if (container != null) {
            assertTrue(container.isRunning(), "Redis 컨테이너가 실행 중이어야 합니다");
            System.out.println("Redis container is running on: " + container.getHost() + ":" + container.getMappedPort(6379));
        } else {
            System.out.println("Redis 컨테이너가 설정에서 비활성화되었습니다.");
        }
    }
    
    @Test
    @DisplayName("비활성화된 PostgreSQL 컨테이너는 null이어야 함")
    public void testDisabledPostgreSQLContainer() {
        GenericContainer<?> container = PrimaveraTestcontainersInitializer.getContainer("postgresql", ContainerLifecycleMode.PER_TEST);
        assertNull(container, "비활성화된 PostgreSQL 컨테이너는 null이어야 합니다");
    }
}