package com.genius.primavera.domain.model;

import lombok.Getter;

@Getter
public enum RoleType {
    ADMINISTRATOR(1, "Endpoint"),
    MANAGER(2, "connection"),
    USER(3, "user"),
    ETC(4, "test");

    private int value;
    private String name;

    RoleType(int value, String name) {
        this.value = value;
        this.name = name;
    }
}