package com.genius.primavera.application;

import com.genius.primavera.domain.Plant;

import java.io.IOException;
import java.util.List;

public interface IPlantRepository {

	List<Plant> getPlant(String combinedName) throws IOException;
}
