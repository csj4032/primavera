package com.genius.primavera.lightweight.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Primavera 경량 프레임워크에서 사용하는 설정 클래스 어노테이션
 * Spring의 @Configuration과 유사한 역할을 합니다.
 * 
 * 이 어노테이션이 붙은 클래스는 설정 정보를 담고 있는 클래스로 인식됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaveraConfiguration {
    
    /**
     * 설정 클래스의 이름을 지정합니다.
     */
    String value() default "";
}