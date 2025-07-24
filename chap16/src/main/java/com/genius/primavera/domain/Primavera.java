package com.genius.primavera.domain;

public class Primavera {

	private static final Primavera ANONYMOUS = new Primavera("Anonymous");

	private final String name;

	public Primavera(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static Primavera getAnonymous() {
		return ANONYMOUS;
	}
}
