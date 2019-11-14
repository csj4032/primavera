package com.genius.primavera;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheGet {

	CacheKeyPrefixType keyPrefixType();
}
