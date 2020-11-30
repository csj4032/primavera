package com.genius.primavera.infrastructure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class KakaoRetrofitClientConfiguration {

	@Value("${kakao.api.url}")
	private String baseUrl;

	@Bean
	@Qualifier("kakaoRetrofit")
	public Retrofit getRetrofitInstance() {
		return new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(JacksonConverterFactory.create())
				.build();
	}
}