package com.genius.primavera.cache;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheGet {

	CacheKeyPrefixType keyPrefixType();
}
