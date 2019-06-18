package com.genius.primavera;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(value = "value=primavera", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PrimaveraApplicationTest {

	@Value("${value}")
	private String primavera;

	@Test
	public void applicationTest() {
		Assertions.assertEquals("primavera", primavera);
	}
}