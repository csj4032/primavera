package com.genius.primavera.application.plant;

import com.genius.primavera.domain.Plant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlantRepository implements IPlantRepository {

	private final Retrofit retrofit;

	@Override
	public List<Plant> getPlant(String combinedName) throws IOException {
		//Retrofit retrofit = RetrofitClient.getInstance();
		IPlantRetrofitRepository plantRetrofitRepository = retrofit.create(IPlantRetrofitRepository.class);
		Call<List<Plant>> allPlants = plantRetrofitRepository.getPlants(combinedName);
		Response<List<Plant>> execute = allPlants.execute();
		List<Plant> plants = execute.body();
		return plants;
	}
}
