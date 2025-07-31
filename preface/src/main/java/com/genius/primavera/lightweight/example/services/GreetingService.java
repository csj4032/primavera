package com.genius.primavera.lightweight.example.services;

import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 인사말을 제공하는 서비스 예제
 */
@Slf4j
@PrimaveraComponent
public class GreetingService {
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 기본 인사말을 반환합니다.
     */
    public String sayHello(String name) {
        String greeting = String.format("🌸 안녕하세요, %s님! Primavera에 오신 것을 환영합니다!", name);
        log.info("인사말 생성: {}", greeting);
        return greeting;
    }
    
    /**
     * 시간이 포함된 인사말을 반환합니다.
     */
    public String sayHelloWithTime(String name) {
        String currentTime = LocalDateTime.now().format(formatter);
        String greeting = String.format("🌸 안녕하세요, %s님! 현재 시간은 %s입니다.", name, currentTime);
        log.info("시간 포함 인사말 생성: {}", greeting);
        return greeting;
    }
    
    /**
     * 작별 인사를 반환합니다.
     */
    public String sayGoodbye(String name) {
        String goodbye = String.format("🌸 안녕히 가세요, %s님! 다음에 또 뵙겠습니다!", name);
        log.info("작별 인사 생성: {}", goodbye);
        return goodbye;
    }
}