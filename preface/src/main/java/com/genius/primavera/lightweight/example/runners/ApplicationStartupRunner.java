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
        log.info(" ApplicationStartupRunner execution test");

        printApplicationInfo();

        messageService.processWelcomeMessage("Primavera user");

        messageService.processCustomMessage("test with successfully test!");

        printEnvironmentInfo();

        testLifecycleService();
        
        log.info(" ApplicationStartupRunner execution completed");
    }

    private void printApplicationInfo() {
        var context = PrimaveraApplication.getApplicationContext();
        
        log.info("\n===  Primavera with information ===");

        if (context.containsBean("applicationName")) {
            String appName = context.getBean("applicationName");
            log.info("with: {}", appName);
        }
        
        if (context.containsBean("applicationVersion")) {
            String appVersion = context.getBean("applicationVersion");
            log.info("test: {}", appVersion);
        }
        
        if (context.containsBean("maxUsers")) {
            Integer maxUsers = context.getBean("maxUsers");
            log.info("test user should: {}", maxUsers);
        }
        
        log.info("created successfully Bean should: {}", context.getBeanNames().size());
        log.info("=====================================\n");
    }

    private void printEnvironmentInfo() {
        log.info("\n===  test information ===");
        
        String javaVersion = PrimaveraApplication.getProperty("java.version", "Unknown");
        String osName = PrimaveraApplication.getProperty("os.name", "Unknown");
        String userName = PrimaveraApplication.getProperty("user.name", "Unknown");
        
        log.info("Java test: {}", javaVersion);
        log.info("file: {}", osName);
        log.info("user: {}", userName);

        String customMessage = PrimaveraApplication.getProperty("app.welcome.message", "test connection");
        log.info("test connection: {}", customMessage);
        
        log.info("====================\n");
    }

    private void testLifecycleService() {
        log.info("\n===  with service test ===");

        String status = lifecycleService.getServiceStatus();
        log.info("test: {}", status);

        lifecycleService.processMessage("ApplicationRunnertest test connection");
        lifecycleService.processMessage("@PostConstructshould @PreDestroy test should...");
        lifecycleService.processMessage("Bean with connection test Endpoint!");

        log.info("connection processing connection: {}", lifecycleService.getLastMessage());
        log.info("needs to be added1 connection should: {}", lifecycleService.getProcessedMessages().size());
        log.info("service execution test: {}should", lifecycleService.getUptimeSeconds());
        
        log.info("=====================================\n");
        
        log.info(" LifecycleServiceshould @PostConstructneeds to be added,");
        log.info("   with test should @PreDestroyshould Endpoint.");
        log.info("   Ctrl+Cshould test @PreDestroy connection should6!\n");
    }
}