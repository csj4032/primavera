package com.genius.primavera.application;

import com.genius.primavera.domain.Plant;
import org.springframework.stereotype.Repository;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.List;

@Repository
public class PlantRepository implements IPlantRepository {

	@Override
	public List<Plant> getPlant(String combinedName) throws IOException {
		Retrofit retrofit = RetrofitClient.getInstance();
		IPlantRetrofitRepository plantRetrofitRepository = retrofit.create(IPlantRetrofitRepository.class);
		Call<List<Plant>> allPlants = plantRetrofitRepository.getPlants(combinedName);
		Response<List<Plant>> execute = allPlants.execute();
		List<Plant> plants = execute.body();
		return plants;
	}
}
