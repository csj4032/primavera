package com.genius.primavera.lightweight.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Primavera 경량 프레임워크에서 사용하는 컴포넌트 어노테이션
 * Spring의 @Component와 유사한 역할을 합니다.
 * 
 * 이 어노테이션이 붙은 클래스는 자동으로 Bean으로 등록됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaveraComponent {
    
    /**
     * Bean의 이름을 지정합니다. 
     * 기본값은 클래스명의 첫 글자를 소문자로 변경한 값입니다.
     */
    String value() default "";
}