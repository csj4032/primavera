package com.genius.primavera.domain.model;

import lombok.Getter;

@Getter
public enum ProviderType {
	UNKNOWN(0, "translated_text_2"),
	FACEBOOK(1, "translated_text_4"),
	GITHUB(2, "translated_text_2"),
	GOOGLE(3, "translated_text_2"),
	KAKAO(4, "translated_text_3");

	private int value;
	private String name;

	ProviderType(int value, String name) {
		this.value = value;
		this.name = name;
	}
}