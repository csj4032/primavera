package com.genius.primavera.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@Testcontainers
@SpringJUnitConfig
public @interface ProfileBasedIntegrationTest {
    
    @AliasFor(annotation = SpringBootTest.class)
    Class<?>[] classes() default {};
    
    @AliasFor(annotation = SpringBootTest.class)
    String[] properties() default {};
}