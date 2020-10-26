package com.genius.primavera.application;

public class RollbackForClass extends RuntimeException {

	public RollbackForClass(String message) {
		super(message);
	}
}
