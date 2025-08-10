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
        log.info(" LifecycleService connection test...");

        startTime = LocalDateTime.now();

        processedMessages = new ArrayList<>();

        scheduler = Executors.newScheduledThreadPool(1);

        startPeriodicHealthCheck();

        String welcomeMessage = greetingService.sayHello("LifecycleService");
        processedMessages.add("connection: " + welcomeMessage);
        
        log.info(" LifecycleService connection completed! test: {}", startTime.format(formatter));

        log.info("=== LifecycleService connection completed ===");
        log.info("test: {}", startTime.format(formatter));
        log.info("test connection should: {}", processedMessages.size());
        log.info("================================");
    }

    @PrimaveraPreDestroy
    public void cleanup() {
        log.info("🧹 LifecycleService test...");
        
        LocalDateTime endTime = LocalDateTime.now();
        long uptime = java.time.Duration.between(startTime, endTime).toSeconds();

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                log.info("🧹 file test");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("🧹 file test");
            }
        }

        String farewellMessage = greetingService.sayGoodbye("LifecycleService");
        processedMessages.add("test: " + farewellMessage);

        log.info("=== LifecycleService test completed ===");
        log.info("test: {}", endTime.format(formatter));
        log.info("should execution test: {}should", uptime);
        log.info("processing connection should: {}", processedMessages.size());
        log.info("connection: {}", getLastMessage());
        log.info("===============================");
        
        log.info("🧹 LifecycleService test completed! should execution test: {}should, processing connection: {}should", 
                uptime, processedMessages.size());
    }

    private void startPeriodicHealthCheck() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String statusMessage = String.format("test - test: %s, connection should: %d", 
                        LocalDateTime.now().format(formatter), 
                        processedMessages.size());
                
                processedMessages.add(statusMessage);
                log.debug(" {}", statusMessage);

                if (processedMessages.size() > 50) {
                    int removed = processedMessages.size() - 25;
                    processedMessages = processedMessages.subList(processedMessages.size() - 25, processedMessages.size());
                    log.debug("🧹 connection {}should test", removed);
                }
                
            } catch (Exception e) {
                log.error(" test failed with error", e);
            }
        }, 10, 30, TimeUnit.SECONDS);
        
        log.info(" test test test (30should test)");
    }

    public String getServiceStatus() {
        if (startTime == null) {
            return "configuration test connection Endpoint.";
        }
        
        long uptime = java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds();
        return String.format("service execution should - execution test: %dshould, processing connection: %dshould", 
                uptime, processedMessages.size());
    }

    public void processMessage(String message) {
        String processedMessage = String.format("[%s] %s", 
                LocalDateTime.now().format(formatter), message);
        
        processedMessages.add(processedMessage);
        log.info(" connection processing: {}", processedMessage);
        
        log.debug(" {}", processedMessage);
    }

    public List<String> getProcessedMessages() {
        return new ArrayList<>(processedMessages);
    }

    public String getLastMessage() {
        return processedMessages.isEmpty() ? "processing connection file." : 
               processedMessages.get(processedMessages.size() - 1);
    }

    public long getUptimeSeconds() {
        if (startTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds();
    }
}