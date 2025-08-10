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
        log.info(" [TEST-BEFORE] connection execution should: {} with args: {}", methodName, args);
    }

    @After("helloServiceGetUserById()")
    public void afterGetUserById(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info(" [TEST-AFTER] connection execution completed: {}", methodName);
    }

    @AfterReturning(pointcut = "helloServiceGetUserById()", returning = "result")
    public void afterReturningGetUserById(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info(" [TEST-AFTER-RETURNING] connection test completed: {} -> result: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "helloServiceGetUserById()", throwing = "exception")
    public void afterThrowingGetUserById(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        log.error(" [TEST-AFTER-THROWING] connection exception test: {} -> exception: {}", methodName, exception.getMessage());
    }

    @Around("helloServiceGetUserById()")
    public Object aroundGetUserById(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        log.info(" [TEST-AROUND-BEFORE] connection execution test: {}", methodName);
        
        try {
            Object result = proceedingJoinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            log.info("⏱ [TEST-AROUND-AFTER] connection execution completed: {} (file: {}ms)", 
                    methodName, endTime - startTime);
            
            return result;
        } catch (Exception e) {
            log.error(" [TEST-AROUND-ERROR] connection execution should exception test: {} -> {}", methodName, e.getMessage());
            throw e;
        }
    }
}