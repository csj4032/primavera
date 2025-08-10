package com.genius.primavera.domain.model;

import lombok.Getter;

@Getter
public enum ProviderType {
	UNKNOWN(0, "test"),
	FACEBOOK(1, "file"),
	GITHUB(2, "test"),
	GOOGLE(3, "test"),
	KAKAO(4, "connection");

	private int value;
	private String name;

	ProviderType(int value, String name) {
		this.value = value;
		this.name = name;
	}
}