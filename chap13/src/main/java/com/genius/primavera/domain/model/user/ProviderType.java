package com.genius.primavera.domain.model.user;

import lombok.Getter;

@Getter
public enum  ProviderType {
    FACEBOOK(1, "translated_text_4"),
    GITHUB(2, "translated_text_2"),
    GOOGLE(3, "translated_text_2");

    private int value;
    private String name;

    ProviderType(int value, String name) {
        this.value = value;
        this.name = name;
    }
}