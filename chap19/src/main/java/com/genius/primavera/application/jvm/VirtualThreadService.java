package com.genius.primavera.application.jvm;

import com.genius.primavera.domain.model.PerformanceMetric;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualThreadService {
    
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final ExecutorService platformExecutor = Executors.newFixedThreadPool(200);
    
    public CompletableFuture<BatchResult> processWithVirtualThreads(List<Task> tasks) {
        var startTime = System.nanoTime();
        
        var futures = tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(() -> processTask(task), virtualExecutor))
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                var results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
                var duration = System.nanoTime() - startTime;
                return BatchResult.builder()
                    .results(results)
                    .durationNanos(duration)
                    .threadType("virtual")
                    .taskCount(tasks.size())
                    .build();
            });
    }
    
    public CompletableFuture<BatchResult> processWithPlatformThreads(List<Task> tasks) {
        var startTime = System.nanoTime();
        
        var futures = tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(() -> processTask(task), platformExecutor))
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                var results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
                var duration = System.nanoTime() - startTime;
                return BatchResult.builder()
                    .results(results)
                    .durationNanos(duration)
                    .threadType("platform")
                    .taskCount(tasks.size())
                    .build();
            });
    }
    
    public Mono<PerformanceComparison> comparePerformance(int taskCount) {
        var tasks = generateTasks(taskCount);
        
        return Mono.zip(
            Mono.fromFuture(processWithVirtualThreads(tasks)),
            Mono.fromFuture(processWithPlatformThreads(tasks))
        ).map(tuple -> {
            var virtualResult = tuple.getT1();
            var platformResult = tuple.getT2();
            
            var improvement = ((double)(platformResult.getDurationNanos() - virtualResult.getDurationNanos()) 
                / platformResult.getDurationNanos()) * 100;
            
            return PerformanceComparison.builder()
                .virtualThreadDuration(virtualResult.getDurationNanos())
                .platformThreadDuration(platformResult.getDurationNanos())
                .improvementPercentage(improvement)
                .taskCount(taskCount)
                .build();
        });
    }
    
    public Flux<ThreadMetrics> streamThreadMetrics() {
        return Flux.interval(Duration.ofSeconds(1))
            .map(tick -> collectThreadMetrics())
            .subscribeOn(Schedulers.fromExecutor(virtualExecutor));
    }
    
    private ThreadMetrics collectThreadMetrics() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        return ThreadMetrics.builder()
            .timestamp(Instant.now())
            .totalThreadCount(threadBean.getThreadCount())
            .peakThreadCount(threadBean.getPeakThreadCount())
            .daemonThreadCount(threadBean.getDaemonThreadCount())
            .virtualThreadCount(estimateVirtualThreadCount())
            .build();
    }
    
    private TaskResult processTask(Task task) {
        try {
            // Simulate I/O operation
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
            
            // Simulate CPU operation
            var result = IntStream.range(0, 1000)
                .map(i -> i * task.getId())
                .sum();
            
            return TaskResult.builder()
                .taskId(task.getId())
                .result(String.valueOf(result))
                .processingTime(System.currentTimeMillis())
                .threadName(Thread.currentThread().getName())
                .isVirtual(Thread.currentThread().isVirtual())
                .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Task processing interrupted", e);
        }
    }
    
    private List<Task> generateTasks(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> Task.builder()
                .id(i)
                .name("Task-" + i)
                .payload("Data-" + i)
                .build())
            .toList();
    }
    
    private int estimateVirtualThreadCount() {
        // Estimate based on thread names containing "virtual"
        return (int) Thread.getAllStackTraces().keySet().stream()
            .filter(Thread::isVirtual)
            .count();
    }
    
    @Data
    @Builder
    public static class Task {
        private int id;
        private String name;
        private String payload;
    }
    
    @Data
    @Builder
    public static class TaskResult {
        private int taskId;
        private String result;
        private long processingTime;
        private String threadName;
        private boolean isVirtual;
    }
    
    @Data
    @Builder
    public static class BatchResult {
        private List<TaskResult> results;
        private long durationNanos;
        private String threadType;
        private int taskCount;
        
        public double getDurationMillis() {
            return durationNanos / 1_000_000.0;
        }
    }
    
    @Data
    @Builder
    public static class PerformanceComparison {
        private long virtualThreadDuration;
        private long platformThreadDuration;
        private double improvementPercentage;
        private int taskCount;
    }
    
    @Data
    @Builder
    public static class ThreadMetrics {
        private Instant timestamp;
        private int totalThreadCount;
        private int peakThreadCount;
        private int daemonThreadCount;
        private int virtualThreadCount;
    }
}