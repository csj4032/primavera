package com.genius.primavera.cache;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheKey {
	long order() default -1l;
}
