package com.genius.primavera.application;

import com.genius.primavera.domain.model.PrimaveraLog;

import reactor.core.publisher.Flux;

public interface ChartService {

    Object getLogChartData();

    Flux<PrimaveraLog> getLogs();
}
