package com.genius.primavera.application;

import com.genius.primavera.domain.Plant;

import java.io.IOException;
import java.util.List;

public interface IPlantService {

	List<Plant> fetchPlants(String combinedName) throws IOException;

}
