package com.genius.primavera.domain.converter;

import com.genius.primavera.domain.model.user.RoleType;

public class RoleTypeAttributeConverter extends EnumAttributeConverter<RoleType, Integer> {

    @Override
    protected Class<RoleType> enumClass() {
        return RoleType.class;
    }
}
