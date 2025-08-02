package com.genius.primavera.domain;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE(1),
    INACTIVE(2),
    DORMANT(3),
    LEAVE(4);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }
}
