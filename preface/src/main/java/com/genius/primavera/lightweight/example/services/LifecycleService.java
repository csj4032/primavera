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

@Slf4j
@PrimaveraComponent
public class LifecycleService {
    
    @PrimaveraAutowired
    private GreetingService greetingService;
    
    private ScheduledExecutorService scheduler;
    private List<String> processedMessages;
    private LocalDateTime startTime;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PrimaveraPostConstruct
    public void initialize() {
        log.info(" LifecycleService translated_text_3 translated_text_2...");

        startTime = LocalDateTime.now();

        processedMessages = new ArrayList<>();

        scheduler = Executors.newScheduledThreadPool(1);

        startPeriodicHealthCheck();

        String welcomeMessage = greetingService.sayHello("LifecycleService");
        processedMessages.add("translated_text_3: " + welcomeMessage);
        
        log.info(" LifecycleService translated_text_3 completed! translated_text_2 translated_text_2: {}", startTime.format(formatter));

        log.info("=== LifecycleService translated_text_3 completed ===");
        log.info("translated_text_2 translated_text_2: {}", startTime.format(formatter));
        log.info("translated_text_2 translated_text_3 translated_text_1: {}", processedMessages.size());
        log.info("================================");
    }

    @PrimaveraPreDestroy
    public void cleanup() {
        log.info("🧹 LifecycleService translated_text_2 translated_text_2...");
        
        LocalDateTime endTime = LocalDateTime.now();
        long uptime = java.time.Duration.between(startTime, endTime).toSeconds();

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                log.info("🧹 translated_text_4 translated_text_2 translated_text_2");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("🧹 translated_text_4 translated_text_2 translated_text_2");
            }
        }

        String farewellMessage = greetingService.sayGoodbye("LifecycleService");
        processedMessages.add("translated_text_2: " + farewellMessage);

        log.info("=== LifecycleService translated_text_2 completed ===");
        log.info("translated_text_2 translated_text_2: {}", endTime.format(formatter));
        log.info("translated_text_1 execution translated_text_2: {}translated_text_1", uptime);
        log.info("translated_text_11 translated_text_3 translated_text_1: {}", processedMessages.size());
        log.info("translated_text_3 translated_text_3: {}", getLastMessage());
        log.info("===============================");
        
        log.info("🧹 LifecycleService translated_text_2 completed! translated_text_1 execution translated_text_2: {}translated_text_1, translated_text_11 translated_text_3: {}translated_text_1", 
                uptime, processedMessages.size());
    }

    private void startPeriodicHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String statusMessage = String.format("translated_text_2 translated_text_2 - translated_text_2 translated_text_2: %s, translated_text_3 translated_text_1: %d", 
                        LocalDateTime.now().format(formatter), 
                        processedMessages.size());
                
                processedMessages.add(statusMessage);
                log.debug(" {}", statusMessage);

                if (processedMessages.size() > 50) {
                    int removed = processedMessages.size() - 25;
                    processedMessages = processedMessages.subList(processedMessages.size() - 25, processedMessages.size());
                    log.debug("🧹 translated_text_3 translated_text_3 {}translated_text_1 translated_text_2", removed);
                }
                
            } catch (Exception e) {
                log.error(" translated_text_2 translated_text_2 translated_text_1 error translated_text_2", e);
            }
        }, 10, 30, TimeUnit.SECONDS);
        
        log.info(" translated_text_2 translated_text_2 translated_text_2 translated_text_2 translated_text_2 (30translated_text_1 translated_text_2)");
    }

    public String getServiceStatus() {
        if (startTime == null) {
            return "translated_text_8 translated_text_2 translated_text_3 translated_text_5.";
        }
        
        long uptime = java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds();
        return String.format("service execution translated_text_1 - execution translated_text_2: %dtranslated_text_1, translated_text_11 translated_text_3: %dtranslated_text_1", 
                uptime, processedMessages.size());
    }

    public void processMessage(String message) {
        String processedMessage = String.format("[%s] %s", 
                LocalDateTime.now().format(formatter), message);
        
        processedMessages.add(processedMessage);
        log.info(" translated_text_3 processing: {}", processedMessage);
        
        log.debug(" {}", processedMessage);
    }

    public List<String> getProcessedMessages() {
        return new ArrayList<>(processedMessages);
    }

    public String getLastMessage() {
        return processedMessages.isEmpty() ? "translated_text_11 translated_text_3 translated_text_4." : 
               processedMessages.get(processedMessages.size() - 1);
    }

    public long getUptimeSeconds() {
        if (startTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds();
    }
}