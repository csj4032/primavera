package com.genius.primavera.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PrimaveraLoggingAspect {

	@Before(value = "@annotation(primaveraLogging)", argNames = "joinPoint, primaveraLogging")
	public void preLogging(JoinPoint joinPoint, PrimaveraLogging primaveraLogging) {
		log.info("Before jointPoint : {}, type : {}", joinPoint, primaveraLogging.type());
	}

	@After(value = "@annotation(primaveraLogging)", argNames = "primaveraLogging")
	public void sufLogging(PrimaveraLogging primaveraLogging) {
		log.info("After type : {}", primaveraLogging.type());
	}
}