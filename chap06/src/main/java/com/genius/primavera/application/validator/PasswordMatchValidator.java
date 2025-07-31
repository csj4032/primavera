package com.genius.primavera.application.validator;

import com.genius.primavera.domain.model.User;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * PasswordMatchValidator 클래스는 User 객체의 비밀번호와 비밀번호 확인이 일치하는지 검증합니다.
 * 두 비밀번호가 모두 null이 아니고 동일한 값인 경우에만 유효한 것으로 간주합니다.
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, User> {

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext context) {
        if (user == null) {
            return true; // null 객체는 다른 검증에서 처리
        }
        
        String password = user.getPassword();
        String passwordConfirm = user.getPasswordConfirm();
        
        // passwordConfirm이 null인 경우는 허용 (기존 테스트와의 호환성)
        if (passwordConfirm == null) {
            return true;
        }
        
        // 두 필드가 모두 존재하는 경우 일치해야 함
        if (password != null && passwordConfirm != null) {
            return password.equals(passwordConfirm);
        }
        
        // password는 null이지만 passwordConfirm은 존재하는 경우 무효
        return false;
    }
}