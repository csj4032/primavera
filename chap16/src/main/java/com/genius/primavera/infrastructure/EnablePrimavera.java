package com.genius.primavera.infrastructure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PrimaveraAutoConfiguration.class)
@Documented
public @interface EnablePrimavera {
}
