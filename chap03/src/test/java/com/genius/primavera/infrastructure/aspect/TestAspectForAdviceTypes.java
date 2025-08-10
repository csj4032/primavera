package com.genius.primavera.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TestAspectForAdviceTypes {

    @Pointcut("execution(* com.genius.primavera.applicaiton.HelloService.getUserById(..))")
    public void helloServiceGetUserById() {}

    @Before("helloServiceGetUserById()")
    public void beforeGetUserById(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        log.info(" [TEST-BEFORE] translated_text_3 execution translated_text_1: {} with args: {}", methodName, args);
    }

    @After("helloServiceGetUserById()")
    public void afterGetUserById(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info(" [TEST-AFTER] translated_text_3 execution completed: {}", methodName);
    }

    @AfterReturning(pointcut = "helloServiceGetUserById()", returning = "result")
    public void afterReturningGetUserById(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info(" [TEST-AFTER-RETURNING] translated_text_3 translated_text_2 completed: {} -> result: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "helloServiceGetUserById()", throwing = "exception")
    public void afterThrowingGetUserById(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        log.error(" [TEST-AFTER-THROWING] translated_text_3 exception translated_text_2: {} -> exception: {}", methodName, exception.getMessage());
    }

    @Around("helloServiceGetUserById()")
    public Object aroundGetUserById(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        log.info(" [TEST-AROUND-BEFORE] translated_text_3 execution translated_text_2: {}", methodName);
        
        try {
            Object result = proceedingJoinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            log.info("⏱ [TEST-AROUND-AFTER] translated_text_3 execution completed: {} (translated_text_4: {}ms)", 
                    methodName, endTime - startTime);
            
            return result;
        } catch (Exception e) {
            log.error(" [TEST-AROUND-ERROR] translated_text_3 execution translated_text_1 exception translated_text_2: {} -> {}", methodName, e.getMessage());
            throw e;
        }
    }
}