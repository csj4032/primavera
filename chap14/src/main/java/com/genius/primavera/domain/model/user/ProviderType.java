package com.genius.primavera.domain.model.user;

import com.genius.primavera.domain.converter.ConvertedEnum;
import com.genius.primavera.domain.converter.ReverseEnumResolver;

import lombok.Getter;

@Getter
public enum  ProviderType implements ConvertedEnum<Integer> {
    FACEBOOK(1, "file"),
    GITHUB(2, "test"),
    GOOGLE(3, "test");

    private int value;
    private String name;

    ProviderType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public Integer toDbValue() {
        return value;
    }

    public static ProviderType fromDbValue(Integer dbValue) {
        return dbValueResolver.get(dbValue);
    }

    public Integer getDbValue() {
        return value;
    }

    public static final ReverseEnumResolver<ProviderType, Integer> dbValueResolver = new ReverseEnumResolver<>(ProviderType.class, ProviderType::getDbValue);
}