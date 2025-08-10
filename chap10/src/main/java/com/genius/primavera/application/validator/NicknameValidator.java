package com.genius.primavera.application.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.Serializable;

public class NicknameValidator implements ConstraintValidator<Nickname, String> , Serializable {

	@Override
	public void initialize(Nickname constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) return false;
		return value.matches("^[0-9a-zA-Ztranslated_text_1-translated_text_1]{2,20}$");
	}
}