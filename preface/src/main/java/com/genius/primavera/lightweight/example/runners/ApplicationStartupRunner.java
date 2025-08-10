package com.genius.primavera.lightweight.example.runners;

import com.genius.primavera.lightweight.annotations.PrimaveraAutowired;
import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.example.services.MessageService;
import com.genius.primavera.lightweight.example.services.LifecycleService;
import com.genius.primavera.lightweight.framework.PrimaveraApplication;
import com.genius.primavera.lightweight.interfaces.PrimaveraApplicationRunner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PrimaveraComponent
public class ApplicationStartupRunner implements PrimaveraApplicationRunner {
    
    @PrimaveraAutowired
    private MessageService messageService;
    
    @PrimaveraAutowired
    private LifecycleService lifecycleService;
    
    @Override
    public void run() throws Exception {
        log.info(" ApplicationStartupRunner execution translated_text_2");

        printApplicationInfo();

        messageService.processWelcomeMessage("Primavera user");

        messageService.processCustomMessage("translated_text_2 translated_text_6 translated_text_10 translated_text_2!");

        printEnvironmentInfo();

        testLifecycleService();
        
        log.info(" ApplicationStartupRunner execution completed");
    }

    private void printApplicationInfo() {
        var context = PrimaveraApplication.getApplicationContext();
        
        log.info("\n===  Primavera translated_text_6 information ===");

        if (context.containsBean("applicationName")) {
            String appName = context.getBean("applicationName");
            log.info("translated_text_6: {}", appName);
        }
        
        if (context.containsBean("applicationVersion")) {
            String appVersion = context.getBean("applicationVersion");
            log.info("translated_text_2: {}", appVersion);
        }
        
        if (context.containsBean("maxUsers")) {
            Integer maxUsers = context.getBean("maxUsers");
            log.info("translated_text_2 user translated_text_1: {}", maxUsers);
        }
        
        log.info("translated_text_13 Bean translated_text_1: {}", context.getBeanNames().size());
        log.info("=====================================\n");
    }

    private void printEnvironmentInfo() {
        log.info("\n===  translated_text_2 information ===");
        
        String javaVersion = PrimaveraApplication.getProperty("java.version", "Unknown");
        String osName = PrimaveraApplication.getProperty("os.name", "Unknown");
        String userName = PrimaveraApplication.getProperty("user.name", "Unknown");
        
        log.info("Java translated_text_2: {}", javaVersion);
        log.info("translated_text_4: {}", osName);
        log.info("user: {}", userName);

        String customMessage = PrimaveraApplication.getProperty("app.welcome.message", "translated_text_2 translated_text_2 translated_text_3");
        log.info("translated_text_2 translated_text_3: {}", customMessage);
        
        log.info("====================\n");
    }

    private void testLifecycleService() {
        log.info("\n===  translated_text_6 service test ===");

        String status = lifecycleService.getServiceStatus();
        log.info("translated_text_2 translated_text_2: {}", status);

        lifecycleService.processMessage("ApplicationRunnertranslated_text_2 translated_text_2 test translated_text_3");
        lifecycleService.processMessage("@PostConstructtranslated_text_1 @PreDestroy test translated_text_1...");
        lifecycleService.processMessage("Bean translated_text_6 translated_text_3 translated_text_2 translated_text_5!");

        log.info("translated_text_3 translated_text_11 translated_text_3: {}", lifecycleService.getLastMessage());
        log.info("translated_text_1 translated_text_11 translated_text_3 translated_text_1: {}", lifecycleService.getProcessedMessages().size());
        log.info("service execution translated_text_2: {}translated_text_1", lifecycleService.getUptimeSeconds());
        
        log.info("=====================================\n");
        
        log.info(" LifecycleServicetranslated_text_1 @PostConstructtranslated_text_1 translated_text_1,");
        log.info("   translated_text_6 translated_text_2 translated_text_1 @PreDestroytranslated_text_1 translated_text_5.");
        log.info("   Ctrl+Ctranslated_text_1 translated_text_2 @PreDestroy translated_text_3 translated_text_3 translated_text_16!\n");
    }
}