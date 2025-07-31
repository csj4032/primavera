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
        log.info("🔍 [TEST-BEFORE] 메서드 실행 전: {} with args: {}", methodName, args);
    }

    @After("helloServiceGetUserById()")
    public void afterGetUserById(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("🔚 [TEST-AFTER] 메서드 실행 완료: {}", methodName);
    }

    @AfterReturning(pointcut = "helloServiceGetUserById()", returning = "result")
    public void afterReturningGetUserById(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("✅ [TEST-AFTER-RETURNING] 메서드 정상 완료: {} -> 결과: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "helloServiceGetUserById()", throwing = "exception")
    public void afterThrowingGetUserById(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        log.error("❌ [TEST-AFTER-THROWING] 메서드 예외 발생: {} -> 예외: {}", methodName, exception.getMessage());
    }

    @Around("helloServiceGetUserById()")
    public Object aroundGetUserById(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        log.info("🚀 [TEST-AROUND-BEFORE] 메서드 실행 시작: {}", methodName);
        
        try {
            Object result = proceedingJoinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            log.info("⏱️ [TEST-AROUND-AFTER] 메서드 실행 완료: {} (소요시간: {}ms)", 
                    methodName, endTime - startTime);
            
            return result;
        } catch (Exception e) {
            log.error("💥 [TEST-AROUND-ERROR] 메서드 실행 중 예외 발생: {} -> {}", methodName, e.getMessage());
            throw e;
        }
    }
}