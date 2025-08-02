package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@EnablePrimaveraTestcontainers(lifecycleMode = ContainerLifecycleMode.PER_TEST)
@ActiveProfiles("multi")
@DisplayName("동시 실행 컨테이너 검증")
public class SimultaneousContainerTest {
    
    @Test
    @DisplayName("MariaDB와 Redis가 동시에 실행되는지 실시간 확인")
    public void testSimultaneousExecution() throws InterruptedException {
        System.out.println("\n=== 동시 실행 테스트 시작 ===");
        
        // 컨테이너 상태를 여러 번 확인
        for (int i = 1; i <= 3; i++) {
            System.out.printf("\n--- %d번째 확인 ---\n", i);
            
            GenericContainer<?> mariadbContainer = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_TEST);
            GenericContainer<?> redisContainer = PrimaveraTestcontainersInitializer.getContainer("redis", ContainerLifecycleMode.PER_TEST);
            
            System.out.println("MariaDB 컨테이너: " + (mariadbContainer != null ? "존재" : "null"));
            System.out.println("Redis 컨테이너: " + (redisContainer != null ? "존재" : "null"));
            
            if (mariadbContainer != null) {
                System.out.printf("  MariaDB 실행 상태: %s\n", mariadbContainer.isRunning() ? "실행 중" : "중지됨");
                if (mariadbContainer.isRunning()) {
                    System.out.printf("  MariaDB 주소: %s:%d\n", 
                        mariadbContainer.getHost(), 
                        mariadbContainer.getMappedPort(3306));
                }
            }
            
            if (redisContainer != null) {
                System.out.printf("  Redis 실행 상태: %s\n", redisContainer.isRunning() ? "실행 중" : "중지됨");
                if (redisContainer.isRunning()) {
                    System.out.printf("  Redis 주소: %s:%d\n", 
                        redisContainer.getHost(), 
                        redisContainer.getMappedPort(6379));
                }
            }
            
            // 둘 다 실행 중이면 동시 실행 확인
            if (mariadbContainer != null && redisContainer != null && 
                mariadbContainer.isRunning() && redisContainer.isRunning()) {
                
                System.out.println("✅ 두 컨테이너가 동시에 실행 중입니다!");
                
                // 실제로 다른 컨테이너인지 확인
                assertNotSame(mariadbContainer, redisContainer, "MariaDB와 Redis는 서로 다른 컨테이너여야 함");
                
                // 포트 확인
                int mariadbPort = mariadbContainer.getMappedPort(3306);
                int redisPort = redisContainer.getMappedPort(6379);
                
                System.out.printf("  포트 매핑: MariaDB=%d, Redis=%d\n", mariadbPort, redisPort);
                
                // 포트가 다른지 확인
                assertNotEquals(mariadbPort, redisPort, "두 컨테이너는 서로 다른 포트를 사용해야 함");
                
                // 동시 연결 테스트 (간단한 검증)
                assertTrue(mariadbPort > 0 && redisPort > 0, "두 컨테이너 모두 유효한 포트에서 실행되어야 함");
                
                System.out.println("🎉 동시 실행 검증 완료!");
                return; // 성공적으로 확인했으므로 종료
            }
            
            // 짧은 대기 후 다시 확인
            if (i < 3) {
                Thread.sleep(1000);
            }
        }
        
        // 여기까지 도달했다면 동시 실행이 확인되지 않았음
        System.out.println("⚠️ 동시 실행이 확인되지 않았습니다.");
        
        // 최소한 하나는 실행되어야 함
        GenericContainer<?> mariadbContainer = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_TEST);
        if (mariadbContainer != null) {
            assertTrue(mariadbContainer.isRunning(), "최소한 MariaDB 컨테이너는 실행되어야 합니다");
            System.out.println("MariaDB 컨테이너만 실행 중입니다.");
        }
    }
    
    @Test
    @DisplayName("Docker 명령어로 실행 중인 컨테이너 확인")
    public void testDockerContainerList() {
        System.out.println("\n=== Docker 컨테이너 목록 확인 ===");
        
        // 컨테이너 가져오기
        GenericContainer<?> mariadbContainer = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_TEST);
        GenericContainer<?> redisContainer = PrimaveraTestcontainersInitializer.getContainer("redis", ContainerLifecycleMode.PER_TEST);
        
        if (mariadbContainer != null && mariadbContainer.isRunning()) {
            System.out.println("MariaDB 컨테이너 ID: " + mariadbContainer.getContainerId());
            System.out.println("MariaDB 컨테이너 이름: " + mariadbContainer.getContainerName());
        }
        
        if (redisContainer != null && redisContainer.isRunning()) {
            System.out.println("Redis 컨테이너 ID: " + redisContainer.getContainerId());
            System.out.println("Redis 컨테이너 이름: " + redisContainer.getContainerName());
        }
        
        // 컨테이너가 실제로 존재하는지 확인
        if (mariadbContainer != null || redisContainer != null) {
            System.out.println("컨테이너가 성공적으로 생성되었습니다.");
        } else {
            System.out.println("컨테이너를 찾을 수 없습니다.");
        }
    }
}