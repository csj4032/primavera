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
        log.info("test connection processing test: user = {}", userName);
        
        String greeting = greetingService.sayHello(userName);
        String timeGreeting = greetingService.sayHelloWithTime(userName);
        
        log.info("=== test connection ===");
        log.info(greeting);
        log.info(timeGreeting);
        log.info("================");
        
        log.info("test connection processing completed");
    }

    public void processFarewellMessage(String userName) {
        log.info("test connection processing test: user = {}", userName);
        
        String goodbye = greetingService.sayGoodbye(userName);
        
        log.info("=== test connection ===");
        log.info(goodbye);
        log.info("================");
        
        log.info("test connection processing completed");
    }

    public void processCustomMessage(String message) {
        log.info("connection processing: {}", message);
        
        log.info("=== connection ===");
        log.info(" {}", message);
        log.info("==================");
    }
}