package com.genius.primavera.application;

public class NoRollbackForClass extends RuntimeException {

	public NoRollbackForClass(String message) {
		super(message);
	}
}
