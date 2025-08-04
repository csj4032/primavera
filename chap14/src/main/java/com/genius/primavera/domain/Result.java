package com.genius.primavera.domain;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Railway Oriented Programming을 위한 Result 타입
 * Java 21+ Pattern Matching과 Sealed Interface 활용
 * 
 * @param <T> 성공 시 반환할 타입
 * @param <E> 실패 시 에러 타입
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {
    
    /**
     * 성공 결과를 나타내는 Record
     */
    record Success<T, E>(T value) implements Result<T, E> {
        public static <T, E> Success<T, E> of(T value) {
            return new Success<>(value);
        }
    }
    
    /**
     * 실패 결과를 나타내는 Record
     */
    record Failure<T, E>(E error) implements Result<T, E> {
        public static <T, E> Failure<T, E> of(E error) {
            return new Failure<>(error);
        }
    }
    
    /**
     * 성공 케이스 생성
     */
    static <T, E> Result<T, E> success(T value) {
        return Success.of(value);
    }
    
    /**
     * 실패 케이스 생성  
     */
    static <T, E> Result<T, E> failure(E error) {
        return Failure.of(error);
    }
    
    /**
     * 성공 여부 확인
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }
    
    /**
     * 실패 여부 확인
     */
    default boolean isFailure() {
        return !isSuccess();
    }
    
    /**
     * 성공 시 값 추출 (Optional로 안전하게)
     */
    default Optional<T> getValue() {
        if (this instanceof Success<T, E> success) {
            return Optional.of(success.value());
        }
        return Optional.empty();
    }
    
    /**
     * 실패 시 에러 추출 (Optional로 안전하게)
     */
    default Optional<E> getError() {
        if (this instanceof Failure<T, E> failure) {
            return Optional.of(failure.error());
        }
        return Optional.empty();
    }
    
    /**
     * 함수형 변환 (Functor map)
     */
    default <U> Result<U, E> map(Function<T, U> mapper) {
        if (this instanceof Success<T, E> success) {
            return Result.success(mapper.apply(success.value()));
        }
        return Result.failure(((Failure<T, E>) this).error());
    }
    
    /**
     * 모나딕 바인딩 (flatMap)
     */
    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
        if (this instanceof Success<T, E> success) {
            return mapper.apply(success.value());
        }
        return Result.failure(((Failure<T, E>) this).error());
    }
    
    /**
     * 성공 시 실행할 액션
     */
    default Result<T, E> onSuccess(Consumer<T> action) {
        if (this instanceof Success<T, E> success) {
            action.accept(success.value());
        }
        return this;
    }
    
    /**
     * 실패 시 실행할 액션
     */
    default Result<T, E> onFailure(Consumer<E> action) {
        if (this instanceof Failure<T, E> failure) {
            action.accept(failure.error());
        }
        return this;
    }
    
    /**
     * 기본값과 함께 값 추출
     */
    default T getOrElse(T defaultValue) {
        if (this instanceof Success<T, E> success) {
            return success.value();
        }
        return defaultValue;
    }
    
    /**
     * 예외를 던지며 값 추출
     */
    default T getOrThrow() throws RuntimeException {
        if (this instanceof Success<T, E> success) {
            return success.value();
        }
        throw new RuntimeException("Result is failure: " + ((Failure<T, E>) this).error());
    }
}