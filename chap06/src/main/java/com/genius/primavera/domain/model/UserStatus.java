package com.genius.primavera.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE(1),
    INACTIVE(2),
    DORMANT(3),
    LEAVE(4);

	private int value;

	UserStatus(int value) {
		this.value = value;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	@JsonCreator
	public static UserStatus fromValue(int value) {
		for (UserStatus status : UserStatus.values()) {
			if (status.value == value) {
				return status;
			}
		}
		throw new IllegalArgumentException("Invalid UserStatus value: " + value);
	}
}
