package com.genius.primavera.lightweight.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Primavera 경량 프레임워크에서 사용하는 의존성 주입 어노테이션
 * Spring의 @Autowired와 유사한 역할을 합니다.
 * 
 * 이 어노테이션이 붙은 필드나 생성자에 자동으로 Bean이 주입됩니다.
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaveraAutowired {
    
    /**
     * 의존성 주입이 필수인지 여부를 나타냅니다.
     * false로 설정하면 해당 Bean이 없어도 예외가 발생하지 않습니다.
     */
    boolean required() default true;
}