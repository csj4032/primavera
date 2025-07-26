package com.genius.primavera.application.logging;

import com.genius.primavera.domain.model.PrimaveraLog;
import com.genius.primavera.domain.repository.PrimaveraLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class PrimaveraLogServiceImpl implements PrimaveraLogService {

    @Autowired
    private PrimaveraLogRepository primaveraLogRepository;

    @Override
    public Flux<PrimaveraLog> findAll() {
        return primaveraLogRepository.findAll();
    }

    @Override
    public void save(PrimaveraLog primaveraLog) {
        primaveraLogRepository.save(primaveraLog).subscribe();
    }
}
