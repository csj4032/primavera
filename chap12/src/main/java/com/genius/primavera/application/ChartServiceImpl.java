package com.genius.primavera.application;

import com.genius.primavera.domain.model.PrimaveraLog;
import com.genius.primavera.domain.repository.PrimaveraLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChartServiceImpl implements ChartService {

    @Autowired
    private PrimaveraLogRepository primaveraLogRepository;

    @Override
    public Object getLogChartData() {
        return null;
    }

    @Override
    public Flux<PrimaveraLog> getLogs() {
        return primaveraLogRepository.findAll();
    }
}
