package com.genius.primavera.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "performance_metrics")
public class PerformanceMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Instant timestamp;
    
    @Column(name = "metric_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MetricType type;
    
    @Column(name = "metric_name", nullable = false)
    private String name;
    
    @Column(name = "metric_value", nullable = false)
    private Double value;
    
    @Column(name = "unit")
    private String unit;
    
    @Column(name = "tags")
    private String tags;
    
    public enum MetricType {
        JVM_MEMORY,
        JVM_THREAD,
        JVM_GC,
        DATABASE,
        CACHE,
        HTTP_REQUEST,
        VIRTUAL_THREAD,
        CUSTOM
    }
}