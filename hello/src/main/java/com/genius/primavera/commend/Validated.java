package com.genius.primavera.commend;

import com.genius.primavera.EnumType;
import lombok.Getter;

@Getter
public class Validated implements Entry {

	private EnumType enumType;

	public Validated(EnumType enumType) {
		this.enumType = enumType;
	}
}
