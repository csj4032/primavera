package com.genius.primavera.application.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.Serializable;

public class NicknameValidator implements ConstraintValidator<Nickname, String> , Serializable {

	@Override
	public void initialize(Nickname constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) return false;
		return value.matches("^[0-9a-zA-Z가-힣]{2,20}$");
	}
}