package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.PrimaveraLog;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface PrimaveraLogRepository extends ReactiveMongoRepository<PrimaveraLog, Long> {

    Flux<PrimaveraLog> findByType(String type);

}
