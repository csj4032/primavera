package com.genius.primavera.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPerformanceMetric is a Querydsl query type for PerformanceMetric
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPerformanceMetric extends EntityPathBase<PerformanceMetric> {

    private static final long serialVersionUID = 191204304L;

    public static final QPerformanceMetric performanceMetric = new QPerformanceMetric("performanceMetric");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath tags = createString("tags");

    public final DateTimePath<java.time.Instant> timestamp = createDateTime("timestamp", java.time.Instant.class);

    public final EnumPath<PerformanceMetric.MetricType> type = createEnum("type", PerformanceMetric.MetricType.class);

    public final StringPath unit = createString("unit");

    public final NumberPath<Double> value = createNumber("value", Double.class);

    public QPerformanceMetric(String variable) {
        super(PerformanceMetric.class, forVariable(variable));
    }

    public QPerformanceMetric(Path<? extends PerformanceMetric> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPerformanceMetric(PathMetadata metadata) {
        super(PerformanceMetric.class, metadata);
    }

}

