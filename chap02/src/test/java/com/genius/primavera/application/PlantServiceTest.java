package com.genius.primavera.application;

import com.genius.primavera.application.plant.IPlantRepository;
import com.genius.primavera.application.plant.PlantService;
import com.genius.primavera.domain.Plant;
import com.google.gson.stream.MalformedJsonException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlantServiceTest {

	@Mock
	private IPlantRepository plantRepository;

	@InjectMocks
	private PlantService plantService;

	@Test
	@Order(1)
	@DisplayName("정상적인 파라미터 요청 테스트")
	public void getPlants() throws IOException {
		// Given
		Plant mockPlant = new Plant();
		given(plantRepository.getPlant("Oak")).willReturn(Collections.singletonList(mockPlant));
		
		// When
		var plants = plantService.fetchPlants("Oak");
		
		// Then
		Assertions.assertNotNull(plants);
		Assertions.assertEquals(1, plants.size());
	}

	@Test
	@Order(2)
	@DisplayName("비정상적인 파라미터 요청 테스트 : MalformedJsonException 발생")
	public void getEmptyResultPlants() throws IOException {
		// Given
		given(plantRepository.getPlant("abc")).willThrow(new MalformedJsonException("Invalid JSON"));
		
		// When & Then
		Assertions.assertThrows(MalformedJsonException.class, () -> plantService.fetchPlants("abc"));
	}
}