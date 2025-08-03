package com.genius.primavera.application;

import com.genius.primavera.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundUserException extends RuntimeException {

    public NotFoundUserException(User user) {
        super(user.toString());
    }
}
