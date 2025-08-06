package com.genius.primavera.domain;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Result<T, E> permits Result.Success, Result.Failure {
    
    record Success<T, E>(T value) implements Result<T, E> {
        public static <T, E> Success<T, E> of(T value) {
            return new Success<>(value);
        }
    }
    
    record Failure<T, E>(E error) implements Result<T, E> {
        public static <T, E> Failure<T, E> of(E error) {
            return new Failure<>(error);
        }
    }
    
    static <T, E> Result<T, E> success(T value) {
        return Success.of(value);
    }
    
    static <T, E> Result<T, E> failure(E error) {
        return Failure.of(error);
    }
    
    default boolean isSuccess() {
        return this instanceof Success;
    }
    
    default boolean isFailure() {
        return !isSuccess();
    }
    
    default Optional<T> getValue() {
        if (this instanceof Success<T, E> success) {
            return Optional.of(success.value());
        }
        return Optional.empty();
    }
    
    default Optional<E> getError() {
        if (this instanceof Failure<T, E> failure) {
            return Optional.of(failure.error());
        }
        return Optional.empty();
    }
    
    default <U> Result<U, E> map(Function<T, U> mapper) {
        if (this instanceof Success<T, E> success) {
            return Result.success(mapper.apply(success.value()));
        }
        return Result.failure(((Failure<T, E>) this).error());
    }
    
    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        if (this instanceof Success<T, E> success) {
            return mapper.apply(success.value());
        }
        return Result.failure(((Failure<T, E>) this).error());
    }
    
    default Result<T, E> onSuccess(Consumer<T> action) {
        if (this instanceof Success<T, E> success) {
            action.accept(success.value());
        }
        return this;
    }
    
    default Result<T, E> onFailure(Consumer<E> action) {
        if (this instanceof Failure<T, E> failure) {
            action.accept(failure.error());
        }
        return this;
    }
    
    default T getOrElse(T defaultValue) {
        if (this instanceof Success<T, E> success) {
            return success.value();
        }
        return defaultValue;
    }
    
    default T getOrThrow() throws RuntimeException {
        if (this instanceof Success<T, E> success) {
            return success.value();
        }
        throw new RuntimeException("Result is failure: " + ((Failure<T, E>) this).error());
    }
}