package com.genius.primavera.application.validator;

import com.genius.primavera.domain.model.User;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, User> {

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext context) {
        if (user == null) {
            return true;
        }
        
        String password = user.getPassword();
        String passwordConfirm = user.getPasswordConfirm();

        if (passwordConfirm == null) {
            return true;
        }

        if (password != null && passwordConfirm != null) {
            return password.equals(passwordConfirm);
        }

        return false;
    }
}