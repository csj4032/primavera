package com.genius.primavera.lightweight.example.runners;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.example.services.MessageService;
import com.genius.primavera.lightweight.interfaces.PrimaveraApplicationRunner;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 종료 관련 처리를 하는 Runner 예제
 */
@Slf4j
@PrimaveraComponent
public class ApplicationShutdownRunner implements PrimaveraApplicationRunner {
    
    @PrimaveraAutowired
    private MessageService messageService;
    
    @Override
    public void run() throws Exception {
        log.info("🌸 ApplicationShutdownRunner 실행 시작");
        
        // Shutdown Hook 등록
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🌸 애플리케이션 종료 중...");
            messageService.processFarewellMessage("Primavera 사용자");
            messageService.processCustomMessage("경량 프레임워크가 안전하게 종료되었습니다. 안녕히 가세요!");
            log.info("🌸 애플리케이션 종료 완료");
        }));
        
        log.info("🌸 Shutdown Hook 등록 완료");
        log.info("🌸 ApplicationShutdownRunner 실행 완료");
    }
}