package com.genius.primavera.lightweight.example.services;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PrimaveraComponent
public class MessageService {
    
    @PrimaveraAutowired
    private GreetingService greetingService;

    public void processWelcomeMessage(String userName) {
        log.info("translated_text_2 translated_text_3 processing translated_text_2: user = {}", userName);
        
        String greeting = greetingService.sayHello(userName);
        String timeGreeting = greetingService.sayHelloWithTime(userName);
        
        log.info("=== translated_text_2 translated_text_3 ===");
        log.info(greeting);
        log.info(timeGreeting);
        log.info("================");
        
        log.info("translated_text_2 translated_text_3 processing completed");
    }

    public void processFarewellMessage(String userName) {
        log.info("translated_text_2 translated_text_3 processing translated_text_2: user = {}", userName);
        
        String goodbye = greetingService.sayGoodbye(userName);
        
        log.info("=== translated_text_2 translated_text_3 ===");
        log.info(goodbye);
        log.info("================");
        
        log.info("translated_text_2 translated_text_3 processing completed");
    }

    public void processCustomMessage(String message) {
        log.info("translated_text_3 translated_text_3 processing: {}", message);
        
        log.info("=== translated_text_3 translated_text_3 ===");
        log.info(" {}", message);
        log.info("==================");
    }
}