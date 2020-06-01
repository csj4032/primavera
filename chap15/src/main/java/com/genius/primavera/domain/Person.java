package com.genius.primavera.domain;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record Person(String name, int age) implements Serializable {

	public Person {
		if (name == null || age < 0) {
			throw new IllegalArgumentException("Invalid Value");
		}
	}

	public Person(String name) {
		this(name, 0);
	}
}
