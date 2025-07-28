package com.genius.primavera.test.condition;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class PrimaveraTestContainerCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // @PrimaveraTestContainer 어노테이션 확인 (필수 조건)
        boolean hasAnnotation = hasPrimaveraTestContainerAnnotation();
        
        log.info("=== PrimaveraTestContainerCondition 평가 시작 ===");
        log.info("hasAnnotation: {}", hasAnnotation);
        
        // 현재 활성 프로파일 로깅
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        log.info("활성 프로파일: {}", java.util.Arrays.toString(activeProfiles));
        
        // 스택 트레이스 로깅
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        log.info("현재 스택 트레이스 (상위 5개):");
        for (int i = 0; i < Math.min(5, stackTrace.length); i++) {
            log.info("  {}: {}", i, stackTrace[i]);
        }
        
        // @PrimaveraTestContainer 어노테이션이 있어야만 활성화
        // (ActiveProfiles는 별도 조건이 아님 - 어노테이션이 있으면 어떤 프로필이든 활성화)
        boolean shouldActivate = hasAnnotation;
        log.info("TestContainers 조건 만족: {}", shouldActivate);
        log.info("=== PrimaveraTestContainerCondition 평가 종료 ===");
        
        return shouldActivate;
    }
    
    // @PrimaveraTestContainer 어노테이션이 있는지 확인하는 메서드
    private boolean hasPrimaveraTestContainerAnnotation() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                if (clazz.isAnnotationPresent(PrimaveraTestContainer.class)) {
                    log.debug("@PrimaveraTestContainer 어노테이션 발견: {}", clazz.getName());
                    return true;
                }
            } catch (ClassNotFoundException e) {
                // 무시
            }
        }
        return false;
    }
}