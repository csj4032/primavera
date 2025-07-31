package com.genius.primavera.lightweight.example.services;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.annotations.PrimaveraPostConstruct;
import com.genius.primavera.lightweight.annotations.PrimaveraPreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Bean 라이프사이클을 시연하는 서비스 예제
 * @PostConstruct와 @PreDestroy 어노테이션의 동작을 보여줍니다.
 */
@Slf4j
@PrimaveraComponent
public class LifecycleService {
    
    @PrimaveraAutowired
    private GreetingService greetingService;
    
    private ScheduledExecutorService scheduler;
    private List<String> processedMessages;
    private LocalDateTime startTime;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Bean 생성 및 의존성 주입 완료 후 호출되는 초기화 메서드
     */
    @PrimaveraPostConstruct
    public void initialize() {
        log.info("🌱 LifecycleService 초기화 시작...");
        
        // 시작 시간 기록
        startTime = LocalDateTime.now();
        
        // 메시지 저장소 초기화
        processedMessages = new ArrayList<>();
        
        // 스케줄러 초기화
        scheduler = Executors.newScheduledThreadPool(1);
        
        // 정기적인 상태 체크 작업 시작
        startPeriodicHealthCheck();
        
        // 의존성 주입된 서비스 활용
        String welcomeMessage = greetingService.sayHello("LifecycleService");
        processedMessages.add("초기화: " + welcomeMessage);
        
        log.info("🌱 LifecycleService 초기화 완료! 시작 시간: {}", startTime.format(formatter));
        
        // 초기화 완료 메시지 출력
        System.out.println("=== LifecycleService 초기화 완료 ===");
        System.out.println("시작 시간: " + startTime.format(formatter));
        System.out.println("초기 메시지 수: " + processedMessages.size());
        System.out.println("================================");
    }
    
    /**
     * Bean 소멸 전에 호출되는 정리 메서드
     */
    @PrimaveraPreDestroy
    public void cleanup() {
        log.info("🧹 LifecycleService 정리 시작...");
        
        LocalDateTime endTime = LocalDateTime.now();
        long uptime = java.time.Duration.between(startTime, endTime).toSeconds();
        
        // 스케줄러 종료
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                log.info("🧹 스케줄러 정상 종료");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("🧹 스케줄러 강제 종료");
            }
        }
        
        // 작별 메시지 생성
        String farewellMessage = greetingService.sayGoodbye("LifecycleService");
        processedMessages.add("종료: " + farewellMessage);
        
        // 정리 완료 메시지 출력
        System.out.println("=== LifecycleService 정리 완료 ===");
        System.out.println("종료 시간: " + endTime.format(formatter));
        System.out.println("총 실행 시간: " + uptime + "초");
        System.out.println("처리된 메시지 수: " + processedMessages.size());
        System.out.println("마지막 메시지: " + getLastMessage());
        System.out.println("===============================");
        
        log.info("🧹 LifecycleService 정리 완료! 총 실행 시간: {}초, 처리된 메시지: {}개", 
                uptime, processedMessages.size());
    }
    
    /**
     * 정기적인 상태 체크 작업을 시작합니다.
     */
    private void startPeriodicHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String statusMessage = String.format("상태 체크 - 현재 시간: %s, 메시지 수: %d", 
                        LocalDateTime.now().format(formatter), 
                        processedMessages.size());
                
                processedMessages.add(statusMessage);
                log.debug("💚 {}", statusMessage);
                
                // 너무 많은 메시지가 쌓이면 정리
                if (processedMessages.size() > 50) {
                    int removed = processedMessages.size() - 25;
                    processedMessages = processedMessages.subList(processedMessages.size() - 25, processedMessages.size());
                    log.debug("🧹 오래된 메시지 {}개 정리", removed);
                }
                
            } catch (Exception e) {
                log.error("💔 상태 체크 중 오류 발생", e);
            }
        }, 10, 30, TimeUnit.SECONDS);
        
        log.info("💚 정기 상태 체크 작업 시작 (30초 간격)");
    }
    
    /**
     * 현재 서비스 상태를 반환합니다.
     */
    public String getServiceStatus() {
        if (startTime == null) {
            return "서비스가 아직 초기화되지 않았습니다.";
        }
        
        long uptime = java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds();
        return String.format("서비스 실행 중 - 실행 시간: %d초, 처리된 메시지: %d개", 
                uptime, processedMessages.size());
    }
    
    /**
     * 메시지를 처리합니다.
     */
    public void processMessage(String message) {
        String processedMessage = String.format("[%s] %s", 
                LocalDateTime.now().format(formatter), message);
        
        processedMessages.add(processedMessage);
        log.info("📝 메시지 처리: {}", processedMessage);
        
        System.out.println("💬 " + processedMessage);
    }
    
    /**
     * 처리된 메시지 목록을 반환합니다.
     */
    public List<String> getProcessedMessages() {
        return new ArrayList<>(processedMessages);
    }
    
    /**
     * 마지막 처리된 메시지를 반환합니다.
     */
    public String getLastMessage() {
        return processedMessages.isEmpty() ? "처리된 메시지가 없습니다." : 
               processedMessages.get(processedMessages.size() - 1);
    }
    
    /**
     * 현재 시간까지의 서비스 실행 시간을 반환합니다.
     */
    public long getUptimeSeconds() {
        if (startTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds();
    }
}