package com.genius.primavera.application;

import com.google.gson.stream.MalformedJsonException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlantServiceTest {

	@Autowired
	private PlantService plantService;

	@Test
	@Order(1)
	@DisplayName("정상적인 파라미터 요청 테스트")
	public void getPlants() throws IOException {
		var plants = plantService.fetchPlants("Oak");
		Assertions.assertNotNull(plants);
	}

	@Test
	@Order(2)
	@DisplayName("비정상적인 파라미터 요청 테스트 : MalformedJsonException 발생")
	public void getEmptyResultPlants() {
		Assertions.assertThrows(MalformedJsonException.class, () -> plantService.fetchPlants("abc"));
	}
}