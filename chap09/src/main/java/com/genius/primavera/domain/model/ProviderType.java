package com.genius.primavera.domain.model;

import lombok.Getter;

@Getter
public enum  ProviderType {
    GOOGLE(1, "구글"),
    FACEBOOK(2, "페이스북"),
    GITHUB(3, "깃헙");

    private int value;
    private String name;

    ProviderType(int value, String name) {
        this.value = value;
        this.name = name;
    }
}