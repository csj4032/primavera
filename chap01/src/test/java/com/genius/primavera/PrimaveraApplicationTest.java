package com.genius.primavera;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PrimaveraApplicationTest {

	@Test
	@DisplayName(value = "스프링부트가 정상 작동하는지 알아보자.")
	public void helloWorld() {

	}
}