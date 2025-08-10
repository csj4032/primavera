package com.genius.primavera.lightweight.example.runners;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.example.services.MessageService;
import com.genius.primavera.lightweight.interfaces.PrimaveraApplicationRunner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PrimaveraComponent
public class ApplicationShutdownRunner implements PrimaveraApplicationRunner {
    
    @PrimaveraAutowired
    private MessageService messageService;
    
    @Override
    public void run() throws Exception {
        log.info(" ApplicationShutdownRunner execution test");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info(" with test should...");
            messageService.processFarewellMessage("Primavera user");
            messageService.processCustomMessage("test with file test. connection!");
            log.info(" with test completed");
        }));
        
        log.info(" Shutdown Hook registration completed");
        log.info(" ApplicationShutdownRunner execution completed");
    }
}