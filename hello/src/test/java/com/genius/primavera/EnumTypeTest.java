package com.genius.primavera;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTypeTest {

	@Test
	public void of() {
		Assertions.assertEquals(EnumType.ABC, EnumType.of("abc"));
		Assertions.assertEquals(EnumType.DEF, EnumType.of("def"));
		Assertions.assertEquals(EnumType.GHI, EnumType.of("ghi"));
		Assertions.assertEquals(EnumType.XYZ, EnumType.of("aaa"));
	}
}