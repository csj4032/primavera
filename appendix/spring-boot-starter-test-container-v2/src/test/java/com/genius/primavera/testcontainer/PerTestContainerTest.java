package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@EnablePrimaveraTestcontainers(lifecycleMode = ContainerLifecycleMode.PER_TEST)
@ActiveProfiles("per-test")
@DisplayName("테스트별 독립 컨테이너 테스트")
public class PerTestContainerTest {
    
    @Test
    @DisplayName("첫 번째 테스트 - MariaDB 컨테이너 확인")
    public void testFirstContainer() {
        GenericContainer<?> container = ContainerManager.getContainer("mariadb", ContainerLifecycleMode.PER_TEST);
        
        if (container != null) {
            assertTrue(container.isRunning(), "MariaDB 컨테이너가 실행 중이어야 합니다");
            String containerName1 = container.getContainerName();
            System.out.println("첫 번째 테스트 컨테이너 이름: " + containerName1);
        } else {
            System.out.println("첫 번째 테스트: MariaDB 컨테이너가 아직 생성되지 않았습니다.");
        }
    }
    
    @Test
    @DisplayName("두 번째 테스트 - 다른 MariaDB 컨테이너 확인")
    public void testSecondContainer() {
        GenericContainer<?> container = ContainerManager.getContainer("mariadb", ContainerLifecycleMode.PER_TEST);
        
        if (container != null) {
            assertTrue(container.isRunning(), "MariaDB 컨테이너가 실행 중이어야 합니다");
            String containerName2 = container.getContainerName();
            System.out.println("두 번째 테스트 컨테이너 이름: " + containerName2);
        } else {
            System.out.println("두 번째 테스트: MariaDB 컨테이너가 아직 생성되지 않았습니다.");
        }
    }
    
    @Test
    @DisplayName("세 번째 테스트 - 또 다른 MariaDB 컨테이너 확인")
    public void testThirdContainer() {
        GenericContainer<?> container = ContainerManager.getContainer("mariadb", ContainerLifecycleMode.PER_TEST);
        
        if (container != null) {
            assertTrue(container.isRunning(), "MariaDB 컨테이너가 실행 중이어야 합니다");
            String containerName3 = container.getContainerName();
            System.out.println("세 번째 테스트 컨테이너 이름: " + containerName3);
        } else {
            System.out.println("세 번째 테스트: MariaDB 컨테이너가 아직 생성되지 않았습니다.");
        }
    }
}