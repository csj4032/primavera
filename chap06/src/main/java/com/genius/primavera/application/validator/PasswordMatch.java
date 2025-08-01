package com.genius.primavera.application.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
@Documented
public @interface PasswordMatch {
    String message() default "{com.genius.primavera.validate.password.match.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}