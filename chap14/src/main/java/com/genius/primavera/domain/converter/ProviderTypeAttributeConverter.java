package com.genius.primavera.domain.converter;

import com.genius.primavera.domain.model.user.ProviderType;

public class ProviderTypeAttributeConverter extends EnumAttributeConverter<ProviderType, Integer> {

    @Override
    protected Class<ProviderType> enumClass() {
        return ProviderType.class;
    }
}