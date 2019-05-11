package com.genius.primavera.domain.model;

import lombok.Getter;

@Getter
public enum  ProviderType {
    FACEBOOK(1, "페이스북"),
    GITHUB(2, "깃헙"),
    GOOGLE(3, "구글");

    private int value;
    private String name;

    ProviderType(int value, String name) {
        this.value = value;
        this.name = name;
    }
}