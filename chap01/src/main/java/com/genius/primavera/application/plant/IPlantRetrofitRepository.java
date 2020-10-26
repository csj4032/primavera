package com.genius.primavera.application.plant;

import com.genius.primavera.domain.Plant;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface IPlantRetrofitRepository {

	@GET("/perl/mobile/viewplantsjsonarray.pl")
	Call<List<Plant>> getPlants(@Query("Combined_Name") String combinedName);



}