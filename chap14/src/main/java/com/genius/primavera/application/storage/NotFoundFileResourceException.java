package com.genius.primavera.application.storage;

public class NotFoundFileResourceException extends RuntimeException {
    public NotFoundFileResourceException(String message) {
        super(message);
    }
}
