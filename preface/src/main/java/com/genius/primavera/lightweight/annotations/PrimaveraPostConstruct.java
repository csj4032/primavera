package com.genius.primavera.lightweight.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean이 생성되고 의존성 주입이 완료된 후 실행될 메서드에 붙이는 어노테이션
 * Spring의 @PostConstruct와 유사한 역할을 합니다.
 * 
 * 이 어노테이션이 붙은 메서드는 다음 조건을 만족해야 합니다:
 * - 매개변수가 없어야 함
 * - 반환 타입은 void 또는 무시됨
 * - static이 아니어야 함
 * - final이 아니어야 함
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaveraPostConstruct {
}