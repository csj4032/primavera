package com.genius.primavera.application.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/*
 * NicknameValidator 클래스는 사용자가 입력한 닉네임이 올바른지 확인하는 역할을 합니다.
 * 닉네임은 2자 이상 20자 이하의 한글, 영어, 숫자로만 이루어져야 합니다.
 * 닉네임이 null이거나 조건에 맞지 않으면 유효하지 않은 것으로 간주합니다.
 * 이 클래스는 @Nickname 어노테이션과 함께 사용되며,
 * ConstraintValidator 인터페이스의 isValid 메서드를 통해 닉네임의 유효성을 검사합니다.
 */
public class NicknameValidator implements ConstraintValidator<Nickname, String> {

    @Override
    public void initialize(Nickname constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return value.matches("^[0-9a-zA-Z가-힣]{2,20}$");
    }
}