package com.genius.primavera.application.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NicknameValidator.class)
public @interface Nickname {

	String message() default "{com.genius.primavera.validate.nickname.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
