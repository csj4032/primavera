package com.genius.primavera.domain.model.user;

import lombok.Getter;

@Getter
public enum UserStatus {
	ON(1),
	BLOCK(2),
	DORMANT(3),
	LEAVE(4);

	private int value;

	UserStatus(int value) {
		this.value = value;
	}
}
