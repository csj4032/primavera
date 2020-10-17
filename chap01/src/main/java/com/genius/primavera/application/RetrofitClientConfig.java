package com.genius.primavera.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class RetrofitClientConfig {

	@Value("${plantPlaces.api.url}")
	private String baseUrl;

	@Bean
	@Qualifier("retrofit")
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public Retrofit getRetrofitInstance() {
		return new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
	}
}
