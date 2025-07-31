package com.genius.primavera.lightweight.example.runners;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.example.services.MessageService;
import com.genius.primavera.lightweight.example.services.LifecycleService;
import com.genius.primavera.lightweight.framework.PrimaveraApplication;
import com.genius.primavera.lightweight.interfaces.PrimaveraApplicationRunner;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 시작 시 실행되는 Runner 예제
 */
@Slf4j
@PrimaveraComponent
public class ApplicationStartupRunner implements PrimaveraApplicationRunner {
    
    @PrimaveraAutowired
    private MessageService messageService;
    
    @PrimaveraAutowired
    private LifecycleService lifecycleService;
    
    @Override
    public void run() throws Exception {
        log.info("🌸 ApplicationStartupRunner 실행 시작");
        
        // 애플리케이션 정보 출력
        printApplicationInfo();
        
        // 환영 메시지 처리
        messageService.processWelcomeMessage("Primavera 사용자");
        
        // 커스텀 메시지 처리
        messageService.processCustomMessage("경량 프레임워크가 성공적으로 시작되었습니다!");
        
        // 환경 변수 정보 출력
        printEnvironmentInfo();
        
        // 라이프사이클 서비스 테스트
        testLifecycleService();
        
        log.info("🌸 ApplicationStartupRunner 실행 완료");
    }
    
    /**
     * 애플리케이션 정보를 출력합니다.
     */
    private void printApplicationInfo() {
        var context = PrimaveraApplication.getApplicationContext();
        
        System.out.println("\n=== 🌸 Primavera 애플리케이션 정보 ===");
        
        // Configuration에서 생성된 Bean 정보
        if (context.containsBean("applicationName")) {
            String appName = context.getBean("applicationName");
            System.out.println("애플리케이션명: " + appName);
        }
        
        if (context.containsBean("applicationVersion")) {
            String appVersion = context.getBean("applicationVersion");
            System.out.println("버전: " + appVersion);
        }
        
        if (context.containsBean("maxUsers")) {
            Integer maxUsers = context.getBean("maxUsers");
            System.out.println("최대 사용자 수: " + maxUsers);
        }
        
        System.out.println("등록된 Bean 수: " + context.getBeanNames().size());
        System.out.println("=====================================\n");
    }
    
    /**
     * 환경 변수 정보를 출력합니다.
     */
    private void printEnvironmentInfo() {
        System.out.println("\n=== 🌸 환경 정보 ===");
        
        String javaVersion = PrimaveraApplication.getProperty("java.version", "Unknown");
        String osName = PrimaveraApplication.getProperty("os.name", "Unknown");
        String userName = PrimaveraApplication.getProperty("user.name", "Unknown");
        
        System.out.println("Java 버전: " + javaVersion);
        System.out.println("운영체제: " + osName);
        System.out.println("사용자: " + userName);
        
        // application.properties에서 커스텀 속성 읽기
        String customMessage = PrimaveraApplication.getProperty("app.welcome.message", "기본 환영 메시지");
        System.out.println("환영 메시지: " + customMessage);
        
        System.out.println("====================\n");
    }
    
    /**
     * 라이프사이클 서비스를 테스트합니다.
     */
    private void testLifecycleService() {
        System.out.println("\n=== 🌸 라이프사이클 서비스 테스트 ===");
        
        // 서비스 상태 확인
        String status = lifecycleService.getServiceStatus();
        System.out.println("현재 상태: " + status);
        
        // 메시지 처리 테스트
        lifecycleService.processMessage("ApplicationRunner에서 보낸 테스트 메시지");
        lifecycleService.processMessage("@PostConstruct와 @PreDestroy 테스트 중...");
        lifecycleService.processMessage("Bean 라이프사이클 후킹이 정상 작동합니다!");
        
        // 처리된 메시지 확인
        System.out.println("마지막 처리된 메시지: " + lifecycleService.getLastMessage());
        System.out.println("총 처리된 메시지 수: " + lifecycleService.getProcessedMessages().size());
        System.out.println("서비스 실행 시간: " + lifecycleService.getUptimeSeconds() + "초");
        
        System.out.println("=====================================\n");
        
        System.out.println("💡 LifecycleService는 @PostConstruct로 초기화되었고,");
        System.out.println("   애플리케이션 종료 시 @PreDestroy로 정리됩니다.");
        System.out.println("   Ctrl+C로 종료하여 @PreDestroy 메서드 동작을 확인해보세요!\n");
    }
}