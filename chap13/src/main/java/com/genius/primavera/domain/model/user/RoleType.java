package com.genius.primavera.domain.model.user;

import com.genius.primavera.domain.converter.ConvertedEnum;
import com.genius.primavera.domain.converter.ReverseEnumResolver;

import lombok.Getter;

@Getter
public enum RoleType implements ConvertedEnum<Integer> {
	ADMINISTRATOR(1, "최고관리자"),
	MANAGER(2, "관리자"),
	USER(3, "사용자");

	private int value;
	private String name;

	RoleType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public Integer toDbValue() {
		return value;
	}

	public static RoleType fromDbValue(Integer dbValue) {
		return dbValueResolver.get(dbValue);
	}

	public Integer getDbValue() {
		return value;
	}

	public static final ReverseEnumResolver<RoleType, Integer> dbValueResolver = new ReverseEnumResolver<>(RoleType.class, RoleType::getDbValue);
}