package com.genius.primavera.testcontainer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@EnablePrimaveraTestcontainers(lifecycleMode = ContainerLifecycleMode.PER_CLASS)
@ActiveProfiles("lifecycle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PER_CLASS 생명주기 모드 테스트")
public class LifecycleModeTest {
    
    private static String firstContainerId = null;
    
    @Test
    @Order(1)
    @DisplayName("첫 번째 테스트 - 컨테이너 생성 확인")
    public void test01_FirstContainerCreation() {
        System.out.println("\n=== 첫 번째 테스트 실행 ===");
        
        GenericContainer<?> mariadbContainer = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_CLASS);
        
        assertNotNull(mariadbContainer, "MariaDB 컨테이너가 생성되어야 함");
        assertTrue(mariadbContainer.isRunning(), "MariaDB 컨테이너가 실행 중이어야 함");
        
        firstContainerId = mariadbContainer.getContainerId();
        System.out.println("첫 번째 컨테이너 ID: " + firstContainerId);
        System.out.println("첫 번째 컨테이너 주소: " + mariadbContainer.getHost() + ":" + mariadbContainer.getMappedPort(3306));
    }
    
    @Test
    @Order(2) 
    @DisplayName("두 번째 테스트 - 같은 컨테이너 재사용 확인")
    public void test02_SameContainerReuse() {
        System.out.println("\n=== 두 번째 테스트 실행 ===");
        
        GenericContainer<?> mariadbContainer = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_CLASS);
        
        assertNotNull(mariadbContainer, "MariaDB 컨테이너가 존재해야 함");
        assertTrue(mariadbContainer.isRunning(), "MariaDB 컨테이너가 실행 중이어야 함");
        
        String secondContainerId = mariadbContainer.getContainerId();
        System.out.println("두 번째 컨테이너 ID: " + secondContainerId);
        System.out.println("두 번째 컨테이너 주소: " + mariadbContainer.getHost() + ":" + mariadbContainer.getMappedPort(3306));
        
        // PER_CLASS 모드에서는 같은 컨테이너를 재사용해야 함
        assertEquals(firstContainerId, secondContainerId, 
                "PER_CLASS 모드에서는 같은 테스트 클래스 내에서 같은 컨테이너를 재사용해야 함");
        
        System.out.println("✅ 같은 컨테이너 재사용 확인됨!");
    }
    
    @Test
    @Order(3)
    @DisplayName("세 번째 테스트 - 지속적인 컨테이너 재사용")
    public void test03_ContinuousContainerReuse() {
        System.out.println("\n=== 세 번째 테스트 실행 ===");
        
        GenericContainer<?> mariadbContainer = PrimaveraTestcontainersInitializer.getContainer("mariadb", ContainerLifecycleMode.PER_CLASS);
        
        assertNotNull(mariadbContainer, "MariaDB 컨테이너가 존재해야 함");
        assertTrue(mariadbContainer.isRunning(), "MariaDB 컨테이너가 실행 중이어야 함");
        
        String thirdContainerId = mariadbContainer.getContainerId();
        System.out.println("세 번째 컨테이너 ID: " + thirdContainerId);
        
        // 여전히 같은 컨테이너를 사용해야 함
        assertEquals(firstContainerId, thirdContainerId, 
                "PER_CLASS 모드에서는 모든 테스트에서 같은 컨테이너를 사용해야 함");
        
        System.out.println("✅ 지속적인 컨테이너 재사용 확인됨!");
        
        // 컨테이너 정보 요약 출력
        System.out.println("\n=== PER_CLASS 모드 요약 ===");
        System.out.println("컨테이너 ID: " + thirdContainerId);
        System.out.println("컨테이너 상태: " + (mariadbContainer.isRunning() ? "실행 중" : "중지됨"));
        System.out.println("주소: " + mariadbContainer.getHost() + ":" + mariadbContainer.getMappedPort(3306));
        System.out.println("🎉 PER_CLASS 모드가 정상적으로 작동합니다!");
    }
}