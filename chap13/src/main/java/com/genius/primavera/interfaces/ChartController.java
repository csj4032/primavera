package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.PrimaveraLog;
import com.genius.primavera.domain.repository.PrimaveraLogRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChartController {

    private final PrimaveraLogRepository primaveraLogRepository;

    @GetMapping(value = "/chart")
    public Flux<PrimaveraLog> chart() {
        return primaveraLogRepository.findAll();
    }
}