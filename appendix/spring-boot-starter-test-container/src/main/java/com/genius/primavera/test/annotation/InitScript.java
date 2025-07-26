package com.genius.primavera.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitScript {
    
    /**
     * 초기화 스크립트 경로
     * @return 스크립트 경로 (빈 문자열이면 스크립트 미사용)
     */
    String value() default "";
    
    /**
     * 초기화 스크립트 사용 여부
     * @return true면 사용, false면 미사용
     */
    boolean enabled() default true;
}