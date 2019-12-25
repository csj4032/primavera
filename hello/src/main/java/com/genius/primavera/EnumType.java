package com.genius.primavera;

import lombok.Getter;

@Getter
public enum EnumType {
	ABC("abc"),
	DEF("def"),
	GHI("ghi"),
	XYZ("xyz");

	private String alias;

	EnumType(String alias) {
		this.alias = alias;
	}

	public static EnumType of(String alias) {
		return switch (alias) {
			case "abc":
				yield ABC;
			case "def":
				yield DEF;
			case "ghi":
				yield GHI;
			default:
				yield XYZ;
		};
	}
}