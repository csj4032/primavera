package com.genius.primavera.domain.model.user;

import java.util.Optional;

public record UserDto(
    Long id,
    String email,
    String nickname,
    UserStatus status
) {

    public static UserDto of(Long id, String email, String nickname, UserStatus status) {
        return new UserDto(id, email, nickname, status);
    }

    public boolean isActive() {
        return status == UserStatus.ON;
    }

    public Optional<String> safeEmail() {
        return Optional.ofNullable(email);
    }
}