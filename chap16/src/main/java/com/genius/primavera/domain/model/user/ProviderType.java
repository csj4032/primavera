package com.genius.primavera.domain.model.user;

import com.genius.primavera.domain.converter.ConvertedEnum;
import com.genius.primavera.domain.converter.ReverseEnumResolver;

import lombok.Getter;

@Getter
public enum  ProviderType implements ConvertedEnum<Integer> {
    FACEBOOK(1, "페이스북"),
    GITHUB(2, "깃헙"),
    GOOGLE(3, "구글");

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