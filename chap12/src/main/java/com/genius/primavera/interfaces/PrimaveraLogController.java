package com.genius.primavera.interfaces;

import com.genius.primavera.application.logging.PrimaveraLogService;
import com.genius.primavera.domain.model.PrimaveraLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class PrimaveraLogController {

    @Autowired
    private PrimaveraLogService primaveraLogService;

    @GetMapping(value = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PrimaveraLog> logs() {
        return primaveraLogService.findAll();
    }
}