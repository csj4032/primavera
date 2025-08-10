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
        log.info(" ApplicationShutdownRunner execution translated_text_2");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info(" translated_text_6 translated_text_2 translated_text_1...");
            messageService.processFarewellMessage("Primavera user");
            messageService.processCustomMessage("translated_text_2 translated_text_6 translated_text_4 translated_text_2. translated_text_3 translated_text_3!");
            log.info(" translated_text_6 translated_text_2 completed");
        }));
        
        log.info(" Shutdown Hook registration completed");
        log.info(" ApplicationShutdownRunner execution completed");
    }
}