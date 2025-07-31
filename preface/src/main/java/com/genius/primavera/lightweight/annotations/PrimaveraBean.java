package com.genius.primavera.lightweight.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Primavera 경량 프레임워크에서 사용하는 Bean 생성 메서드 어노테이션
 * Spring의 @Bean과 유사한 역할을 합니다.
 * 
 * @PrimaveraConfiguration이 붙은 클래스 내의 메서드에 이 어노테이션을 붙이면
 * 해당 메서드가 반환하는 객체가 Bean으로 등록됩니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaveraBean {
    
    /**
     * Bean의 이름을 지정합니다.
     * 기본값은 메서드명입니다.
     */
    String value() default "";
}