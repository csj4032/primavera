package com.genius.primavera.scope;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Singleton {

	@Autowired
	private Prototype prototype;

	public Prototype getPrototype() {
		return prototype;
	}
}
