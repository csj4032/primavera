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
    @DisplayName("translated_text_4 translated_text_4 translated_text_2 test")
    public void getPlants() throws IOException {
        Plant mockPlant = new Plant();
        given(plantRepository.getPlant("Oak")).willReturn(Collections.singletonList(mockPlant));
        var plants = plantService.fetchPlants("Oak");
        Assertions.assertNotNull(plants);
        Assertions.assertEquals(1, plants.size());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_4 translated_text_4 translated_text_2 test : MalformedJsonException translated_text_2")
    public void getEmptyResultPlants() throws IOException {
        given(plantRepository.getPlant("abc")).willThrow(new MalformedJsonException("Invalid JSON"));
        Assertions.assertThrows(MalformedJsonException.class, () -> plantService.fetchPlants("abc"));
    }
}