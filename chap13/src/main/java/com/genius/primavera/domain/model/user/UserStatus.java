package com.genius.primavera.domain.model.user;

import com.genius.primavera.domain.converter.ConvertedEnum;
import com.genius.primavera.domain.converter.ReverseEnumResolver;

import lombok.Getter;

@Getter
public enum UserStatus implements ConvertedEnum<Integer> {
    ON(1),
    BLOCK(2),
    DORMANT(3),
    LEAVE(4);

    private int value;

    UserStatus(int value) {
        this.value = value;
    }

    @Override
    public Integer toDbValue() {
        return value;
    }

    public static UserStatus fromDbValue(Integer dbValue) {
        return dbValueResolver.get(dbValue);
    }

    public Integer getDbValue() {
        return value;
    }

    public static final ReverseEnumResolver<UserStatus, Integer> dbValueResolver = new ReverseEnumResolver<>(UserStatus.class, UserStatus::getDbValue);

}
