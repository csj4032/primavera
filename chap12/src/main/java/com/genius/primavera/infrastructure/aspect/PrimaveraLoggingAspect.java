package com.genius.primavera.infrastructure.aspect;

import com.genius.primavera.application.logging.MongoSequenceGeneratorService;
import com.genius.primavera.application.logging.PrimaveraLogService;
import com.genius.primavera.domain.model.PrimaveraLog;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class PrimaveraLoggingAspect {

    @Autowired
    private PrimaveraLogService primaveraLogService;

    @Autowired
    private MongoSequenceGeneratorService mongoSequenceGeneratorService;

    @Before(value = "@annotation(primaveraLogging)", argNames = "joinPoint, primaveraLogging")
    public void preLogging(JoinPoint joinPoint, PrimaveraLogging primaveraLogging) {
        PrimaveraLog primaveraLog = PrimaveraLog.builder()
                .id(mongoSequenceGeneratorService.generateSequence(PrimaveraLog.SEQUENCE_NAME))
                .type(primaveraLogging.type())
                .kind(joinPoint.getKind())
                .target(joinPoint.getTarget().toString())
                .createDt(Instant.now())
                .build();
        primaveraLogService.save(primaveraLog);
    }
}