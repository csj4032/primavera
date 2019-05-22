package com.genius.primavera.interfaces;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockPrimaveraUserSecurityContextFactory.class)
public @interface WithMockPrimaveraUserDetails {

    String username() default "Genius Choi";

    String name() default "Choi";
}
