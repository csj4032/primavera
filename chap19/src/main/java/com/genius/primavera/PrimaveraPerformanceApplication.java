package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableJpaRepositories
@SpringBootApplication
public class PrimaveraPerformanceApplication {

    public static void main(String[] args) {
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "1000");
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "10000");
        SpringApplication.run(PrimaveraPerformanceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("==============================================");
        log.info("Performance Optimization Module Started");
        log.info("Virtual Threads: Enabled");
        log.info("Multi-layer Caching: Active");
        log.info("JVM Monitoring: Running");
        log.info("==============================================");
    }
}