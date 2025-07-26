package com.genius.primavera.application.logging;

import com.genius.primavera.domain.model.PrimaveraLog;

import reactor.core.publisher.Flux;

public interface PrimaveraLogService {

    Flux<PrimaveraLog> findAll();

    void save(PrimaveraLog primaveraLog);
}
