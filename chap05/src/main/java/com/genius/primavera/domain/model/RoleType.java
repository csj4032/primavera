package com.genius.primavera.domain.model;

import lombok.Getter;

@Getter
public enum RoleType {
    ADMINISTRATOR(1, "translated_text_5"),
    MANAGER(2, "translated_text_3"),
    USER(3, "user"),
    ETC(4, "translated_text_2");

    private int value;
    private String name;

    RoleType(int value, String name) {
        this.value = value;
        this.name = name;
    }
}