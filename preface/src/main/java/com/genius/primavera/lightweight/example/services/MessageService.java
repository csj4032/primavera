package com.genius.primavera.lightweight.example.services;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지 처리 서비스 예제
 * GreetingService에 의존성을 가지고 있습니다.
 */
@Slf4j
@PrimaveraComponent
public class MessageService {
    
    @PrimaveraAutowired
    private GreetingService greetingService;
    
    /**
     * 환영 메시지를 처리합니다.
     */
    public void processWelcomeMessage(String userName) {
        log.info("환영 메시지 처리 시작: 사용자 = {}", userName);
        
        String greeting = greetingService.sayHello(userName);
        String timeGreeting = greetingService.sayHelloWithTime(userName);
        
        System.out.println("=== 환영 메시지 ===");
        System.out.println(greeting);
        System.out.println(timeGreeting);
        System.out.println("================");
        
        log.info("환영 메시지 처리 완료");
    }
    
    /**
     * 작별 메시지를 처리합니다.
     */
    public void processFarewellMessage(String userName) {
        log.info("작별 메시지 처리 시작: 사용자 = {}", userName);
        
        String goodbye = greetingService.sayGoodbye(userName);
        
        System.out.println("=== 작별 메시지 ===");
        System.out.println(goodbye);
        System.out.println("================");
        
        log.info("작별 메시지 처리 완료");
    }
    
    /**
     * 커스텀 메시지를 처리합니다.
     */
    public void processCustomMessage(String message) {
        log.info("커스텀 메시지 처리: {}", message);
        
        System.out.println("=== 커스텀 메시지 ===");
        System.out.println("🌸 " + message);
        System.out.println("==================");
    }
}