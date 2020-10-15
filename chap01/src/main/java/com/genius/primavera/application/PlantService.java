package com.genius.primavera.application;

import com.genius.primavera.domain.Plant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService implements IPlantService {

	private final PlantRepository plantRepository;

	@Override
	public List<Plant> fetchPlants(String combinedName) throws IOException {
		return plantRepository.getPlant(combinedName);
	}
}