package com.genius.primavera.application;

import com.genius.primavera.domain.model.User;

public class NotFoundUserException extends RuntimeException {

    public NotFoundUserException(User user) {
        super(user.toString());
    }
}
