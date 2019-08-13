package com.genius.primavera.domain.converter;

import com.genius.primavera.domain.model.user.UserStatus;

public class UserStatusAttributeConverter extends EnumAttributeConverter<UserStatus, Integer> {

    @Override
    protected Class<UserStatus> enumClass() {
        return UserStatus.class;
    }
}
