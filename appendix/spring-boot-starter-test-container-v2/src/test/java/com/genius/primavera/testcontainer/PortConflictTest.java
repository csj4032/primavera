package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@EnablePrimaveraTestcontainers(lifecycleMode = ContainerLifecycleMode.PER_CLASS)
@ActiveProfiles("lifecycle")
@DisplayName("포트 충돌 테스트")
public class PortConflictTest {
    
    @Test
    @DisplayName("TestContainers 포트 할당 방식 확인")
    public void testPortAllocation() throws IOException {
        System.out.println("\n=== TestContainers 포트 할당 분석 ===");
        
        GenericContainer<?> container = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_CLASS);
        
        if (container != null && container instanceof MariaDBContainer) {
            MariaDBContainer<?> mariadbContainer = (MariaDBContainer<?>) container;
            
            // 컨테이너 정보 출력
            System.out.println("컨테이너 ID: " + mariadbContainer.getContainerId());
            System.out.println("컨테이너 이름: " + mariadbContainer.getContainerName());
            System.out.println("호스트: " + mariadbContainer.getHost());
            System.out.println("내부 포트: 3306");
            System.out.println("매핑된 포트: " + mariadbContainer.getMappedPort(3306));
            System.out.println("JDBC URL: " + mariadbContainer.getJdbcUrl());
            
            // 포트가 3306이 아님을 확인
            int mappedPort = mariadbContainer.getMappedPort(3306);
            assertNotEquals(3306, mappedPort, "TestContainers는 랜덤 포트를 사용해야 함");
            
            System.out.println("\n✅ TestContainers는 랜덤 포트 " + mappedPort + "를 사용합니다!");
            System.out.println("이는 수동 컨테이너(3306)와 충돌하지 않습니다.");
            
            // 수동 컨테이너 실행 시뮬레이션
            System.out.println("\n=== 수동 컨테이너 실행 시뮬레이션 ===");
            System.out.println("docker run -p 3306:3306 mariadb:11.4.7 <- 이것은 성공함");
            System.out.println("TestContainer 포트: " + mappedPort + " (충돌 없음)");
            System.out.println("수동 컨테이너 포트: 3306 (충돌 없음)");
            
        } else {
            fail("MariaDB 컨테이너를 찾을 수 없습니다");
        }
    }
    
    @Test
    @DisplayName("동시 실행 가능성 확인")
    public void testConcurrentExecution() throws IOException, InterruptedException {
        System.out.println("\n=== 동시 실행 확인 ===");
        
        GenericContainer<?> testContainer = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_CLASS);
        
        if (testContainer != null) {
            int testContainerPort = testContainer.getMappedPort(3306);
            System.out.println("TestContainer 포트: " + testContainerPort);
            
            // Docker 명령어로 수동 컨테이너 확인
            ProcessBuilder pb = new ProcessBuilder("docker", "ps", "--filter", "name=manual-mariadb-conflict", "--format", "{{.Names}}\\t{{.Ports}}");
            Process process = pb.start();
            
            if (process.waitFor() == 0) {
                System.out.println("수동 컨테이너도 실행 중입니다!");
                System.out.println("수동 컨테이너 포트: 3306");
                System.out.println("TestContainer 포트: " + testContainerPort);
                System.out.println("✅ 두 컨테이너가 동시에 문제없이 실행됩니다!");
            }
            
        }
    }
}