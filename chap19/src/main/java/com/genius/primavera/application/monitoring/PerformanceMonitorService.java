package com.genius.primavera.application.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.lang.management.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.sun.management.GarbageCollectionNotificationInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceMonitorService {
    
    private final MeterRegistry meterRegistry;
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(2);
    
    @PostConstruct
    public void startMonitoring() {
        log.info("Starting performance monitoring...");
        
        // Start JVM metrics collection
        scheduler.scheduleAtFixedRate(this::collectJvmMetrics, 0, 10, TimeUnit.SECONDS);
        
        // Register GC listener
        registerGCListener();
        
        log.info("Performance monitoring started");
    }
    
    @PreDestroy
    public void stopMonitoring() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Performance monitoring stopped");
    }
    
    @Scheduled(fixedDelay = 10000)
    public void collectMetrics() {
        collectJvmMetrics();
        collectThreadMetrics();
        collectMemoryMetrics();
        collectGCMetrics();
    }
    
    private void collectJvmMetrics() {
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        
        // Memory metrics
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        meterRegistry.gauge("jvm.memory.total", totalMemory);
        meterRegistry.gauge("jvm.memory.free", freeMemory);
        meterRegistry.gauge("jvm.memory.used", usedMemory);
        meterRegistry.gauge("jvm.memory.max", maxMemory);
        meterRegistry.gauge("jvm.memory.usage.percent", (double) usedMemory / maxMemory * 100);
        
        // Heap metrics
        MemoryUsage heapUsage = memory.getHeapMemoryUsage();
        meterRegistry.gauge("jvm.memory.heap.used", heapUsage.getUsed());
        meterRegistry.gauge("jvm.memory.heap.max", heapUsage.getMax());
        meterRegistry.gauge("jvm.memory.heap.committed", heapUsage.getCommitted());
        
        // Non-heap metrics
        MemoryUsage nonHeapUsage = memory.getNonHeapMemoryUsage();
        meterRegistry.gauge("jvm.memory.nonheap.used", nonHeapUsage.getUsed());
        meterRegistry.gauge("jvm.memory.nonheap.max", nonHeapUsage.getMax());
        meterRegistry.gauge("jvm.memory.nonheap.committed", nonHeapUsage.getCommitted());
        
        log.debug("JVM metrics collected - Used: {}MB, Free: {}MB", 
            usedMemory / 1024 / 1024, freeMemory / 1024 / 1024);
    }
    
    private void collectThreadMetrics() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        int threadCount = threadBean.getThreadCount();
        int daemonThreadCount = threadBean.getDaemonThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();
        long totalStartedThreadCount = threadBean.getTotalStartedThreadCount();
        
        meterRegistry.gauge("jvm.threads.total", threadCount);
        meterRegistry.gauge("jvm.threads.daemon", daemonThreadCount);
        meterRegistry.gauge("jvm.threads.peak", peakThreadCount);
        meterRegistry.gauge("jvm.threads.total.started", totalStartedThreadCount);
        
        // Virtual threads estimate
        long virtualThreadCount = estimateVirtualThreadCount();
        meterRegistry.gauge("jvm.threads.virtual", virtualThreadCount);
        
        log.debug("Thread metrics collected - Total: {}, Virtual: {}, Daemon: {}", 
            threadCount, virtualThreadCount, daemonThreadCount);
    }
    
    private void collectMemoryMetrics() {
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        
        for (MemoryPoolMXBean pool : memoryPools) {
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                String poolName = pool.getName().toLowerCase().replace(" ", "_");
                
                meterRegistry.gauge("jvm.memory.pool.used", 
                    List.of(Tag.of("pool", poolName)), usage.getUsed());
                meterRegistry.gauge("jvm.memory.pool.max", 
                    List.of(Tag.of("pool", poolName)), usage.getMax());
                meterRegistry.gauge("jvm.memory.pool.committed", 
                    List.of(Tag.of("pool", poolName)), usage.getCommitted());
            }
        }
    }
    
    private void collectGCMetrics() {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        for (GarbageCollectorMXBean gc : gcBeans) {
            String gcName = gc.getName().toLowerCase().replace(" ", "_");
            
            meterRegistry.gauge("jvm.gc.collections", 
                List.of(Tag.of("gc", gcName)), gc.getCollectionCount());
            meterRegistry.gauge("jvm.gc.time", 
                List.of(Tag.of("gc", gcName)), gc.getCollectionTime());
        }
    }
    
    private void registerGCListener() {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            if (gcBean instanceof NotificationEmitter emitter) {
                NotificationListener listener = (notification, handback) -> {
                    if (notification.getType().equals(
                        GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                        
                        GarbageCollectionNotificationInfo info = 
                            GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                        
                        String gcName = info.getGcName();
                        String gcAction = info.getGcAction();
                        String gcCause = info.getGcCause();
                        long duration = info.getGcInfo().getDuration();
                        
                        meterRegistry.timer("jvm.gc.pause")
                            .record(duration, TimeUnit.MILLISECONDS);
                        
                        log.debug("GC occurred - Name: {}, Action: {}, Cause: {}, Duration: {}ms", 
                            gcName, gcAction, gcCause, duration);
                        
                        // Alert on long GC pauses
                        if (duration > 200) {
                            log.warn("Long GC pause detected: {}ms ({})", duration, gcName);
                        }
                    }
                };
                
                emitter.addNotificationListener(listener, null, null);
            }
        }
    }
    
    private long estimateVirtualThreadCount() {
        return Thread.getAllStackTraces().keySet().stream()
            .mapToLong(thread -> thread.isVirtual() ? 1 : 0)
            .sum();
    }
    
    public SystemMetrics getSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        return SystemMetrics.builder()
            .availableProcessors(runtime.availableProcessors())
            .totalMemory(runtime.totalMemory())
            .freeMemory(runtime.freeMemory())
            .maxMemory(runtime.maxMemory())
            .threadCount(threadBean.getThreadCount())
            .peakThreadCount(threadBean.getPeakThreadCount())
            .heapMemoryUsage(memoryBean.getHeapMemoryUsage())
            .nonHeapMemoryUsage(memoryBean.getNonHeapMemoryUsage())
            .virtualThreadCount(estimateVirtualThreadCount())
            .build();
    }
    
    public List<GCInfo> getGCStatistics() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
            .map(gc -> GCInfo.builder()
                .name(gc.getName())
                .collectionCount(gc.getCollectionCount())
                .collectionTime(gc.getCollectionTime())
                .memoryPoolNames(List.of(gc.getMemoryPoolNames()))
                .build())
            .toList();
    }
    
    @Data
    @Builder
    public static class SystemMetrics {
        private int availableProcessors;
        private long totalMemory;
        private long freeMemory;
        private long maxMemory;
        private int threadCount;
        private int peakThreadCount;
        private MemoryUsage heapMemoryUsage;
        private MemoryUsage nonHeapMemoryUsage;
        private long virtualThreadCount;
    }
    
    @Data
    @Builder
    public static class GCInfo {
        private String name;
        private long collectionCount;
        private long collectionTime;
        private List<String> memoryPoolNames;
    }
}