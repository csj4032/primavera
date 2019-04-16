package com.genius.primavera.domain.model;

import lombok.Getter;

@Getter
public enum RoleType {
	ADMINISTRATOR(1, "최고관리자"),
	MANAGER(2, "관리자"),
	USER(3, "사용자");

	private int value;
	private String name;

	RoleType(int value, String name) {
		this.value = value;
		this.name = name;
	}
}