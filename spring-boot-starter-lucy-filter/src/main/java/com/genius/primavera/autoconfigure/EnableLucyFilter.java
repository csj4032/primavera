package com.genius.primavera.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(LucyFilterConfiguration.class)
public @interface EnableLucyFilter {
}
