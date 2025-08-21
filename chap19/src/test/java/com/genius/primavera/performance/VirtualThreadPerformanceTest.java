package com.genius.primavera.performance;

import com.genius.primavera.application.jvm.VirtualThreadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Virtual Thread Performance Tests")
class VirtualThreadPerformanceTest {
    
    @Autowired
    private VirtualThreadService virtualThreadService;
    
    @Test
    @DisplayName("Virtual threads should outperform platform threads for I/O intensive tasks")
    void virtualThreadsShouldOutperformPlatformThreads() {
        // Given
        int taskCount = 1000;
        var tasks = generateTasks(taskCount);
        
        // When
        var virtualResult = virtualThreadService.processWithVirtualThreads(tasks).join();
        var platformResult = virtualThreadService.processWithPlatformThreads(tasks).join();
        
        // Then
        assertThat(virtualResult.getDurationNanos())
            .as("Virtual threads should be faster for I/O intensive tasks")
            .isLessThan(platformResult.getDurationNanos());
        
        assertThat(virtualResult.getTaskCount()).isEqualTo(taskCount);
        assertThat(platformResult.getTaskCount()).isEqualTo(taskCount);
        
        var improvement = ((double)(platformResult.getDurationNanos() - virtualResult.getDurationNanos()) 
            / platformResult.getDurationNanos()) * 100;
        
        System.out.printf("Performance improvement: %.2f%%\n", improvement);
        System.out.printf("Virtual threads: %.2f ms\n", virtualResult.getDurationMillis());
        System.out.printf("Platform threads: %.2f ms\n", platformResult.getDurationMillis());
    }
    
    @Test
    @DisplayName("Performance comparison should show consistent results")
    void performanceComparisonShouldShowConsistentResults() {
        // Given
        int taskCount = 500;
        
        // When
        var comparison = virtualThreadService.comparePerformance(taskCount).block();
        
        // Then
        assertThat(comparison).isNotNull();
        assertThat(comparison.getTaskCount()).isEqualTo(taskCount);
        assertThat(comparison.getVirtualThreadDuration()).isPositive();
        assertThat(comparison.getPlatformThreadDuration()).isPositive();
        assertThat(comparison.getImprovementPercentage()).isNotNull();
        
        System.out.println("Performance Comparison:");
        System.out.printf("Task Count: %d\n", comparison.getTaskCount());
        System.out.printf("Virtual Threads: %d ns\n", comparison.getVirtualThreadDuration());
        System.out.printf("Platform Threads: %d ns\n", comparison.getPlatformThreadDuration());
        System.out.printf("Improvement: %.2f%%\n", comparison.getImprovementPercentage());
    }
    
    @Test
    @DisplayName("Thread metrics should be collected properly")
    void threadMetricsShouldBeCollectedProperly() {
        // When
        var metrics = virtualThreadService.streamThreadMetrics()
            .take(3)
            .collectList()
            .block();
        
        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics).hasSize(3);
        
        metrics.forEach(metric -> {
            assertThat(metric.getTotalThreadCount()).isPositive();
            assertThat(metric.getPeakThreadCount()).isPositive();
            assertThat(metric.getTimestamp()).isNotNull();
            
            System.out.printf("Thread Metrics at %s:\n", metric.getTimestamp());
            System.out.printf("  Total: %d\n", metric.getTotalThreadCount());
            System.out.printf("  Peak: %d\n", metric.getPeakThreadCount());
            System.out.printf("  Virtual: %d\n", metric.getVirtualThreadCount());
        });
    }
    
    private List<VirtualThreadService.Task> generateTasks(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> VirtualThreadService.Task.builder()
                .id(i)
                .name("Test-Task-" + i)
                .payload("Test-Payload-" + i)
                .build())
            .toList();
    }
}