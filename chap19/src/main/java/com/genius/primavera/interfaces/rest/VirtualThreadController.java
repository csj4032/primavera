package com.genius.primavera.interfaces.rest;

import com.genius.primavera.application.jvm.VirtualThreadService;
import com.genius.primavera.application.jvm.VirtualThreadService.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/virtual-threads")
@RequiredArgsConstructor
public class VirtualThreadController {
    
    private final VirtualThreadService virtualThreadService;
    
    @GetMapping("/process/virtual/{count}")
    public Mono<BatchResult> processWithVirtualThreads(@PathVariable int count) {
        var tasks = generateTasks(count);
        return Mono.fromFuture(virtualThreadService.processWithVirtualThreads(tasks));
    }
    
    @GetMapping("/process/platform/{count}")
    public Mono<BatchResult> processWithPlatformThreads(@PathVariable int count) {
        var tasks = generateTasks(count);
        return Mono.fromFuture(virtualThreadService.processWithPlatformThreads(tasks));
    }
    
    @GetMapping("/compare/{count}")
    public Mono<PerformanceComparison> comparePerformance(@PathVariable int count) {
        return virtualThreadService.comparePerformance(count);
    }
    
    @GetMapping(value = "/metrics/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ThreadMetrics> streamThreadMetrics() {
        return virtualThreadService.streamThreadMetrics();
    }
    
    @PostMapping("/benchmark")
    public Mono<BenchmarkResult> runBenchmark(@RequestBody BenchmarkRequest request) {
        return Flux.range(0, request.iterations())
            .flatMap(i -> virtualThreadService.comparePerformance(request.taskCount()))
            .collectList()
            .map(comparisons -> {
                var avgVirtualTime = comparisons.stream()
                    .mapToLong(PerformanceComparison::getVirtualThreadDuration)
                    .average()
                    .orElse(0);
                
                var avgPlatformTime = comparisons.stream()
                    .mapToLong(PerformanceComparison::getPlatformThreadDuration)
                    .average()
                    .orElse(0);
                
                var avgImprovement = comparisons.stream()
                    .mapToDouble(PerformanceComparison::getImprovementPercentage)
                    .average()
                    .orElse(0);
                
                return new BenchmarkResult(
                    request.taskCount(),
                    request.iterations(),
                    avgVirtualTime / 1_000_000.0,
                    avgPlatformTime / 1_000_000.0,
                    avgImprovement
                );
            });
    }
    
    private List<Task> generateTasks(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> Task.builder()
                .id(i)
                .name("Task-" + i)
                .payload("Payload-" + i)
                .build())
            .toList();
    }
    
    public record BenchmarkRequest(int taskCount, int iterations) {
        public BenchmarkRequest {
            if (taskCount <= 0 || taskCount > 10000) {
                throw new IllegalArgumentException("Task count must be between 1 and 10000");
            }
            if (iterations <= 0 || iterations > 100) {
                throw new IllegalArgumentException("Iterations must be between 1 and 100");
            }
        }
    }
    
    public record BenchmarkResult(
        int taskCount,
        int iterations,
        double avgVirtualTimeMs,
        double avgPlatformTimeMs,
        double avgImprovementPercent
    ) {}
}