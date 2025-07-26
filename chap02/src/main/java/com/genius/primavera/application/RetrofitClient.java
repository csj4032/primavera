package com.genius.primavera.application;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

	private static final String BASE_URL = "https://www.plantplaces.com";
	private static Retrofit retrofitClient;

	private RetrofitClient() {

	}

	public static Retrofit getInstance() {
		if (retrofitClient == null) {
			retrofitClient = new Retrofit.Builder()
					.baseUrl(BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		return retrofitClient;
	}
}
