package com.genius.primavera;

import feign.Contract;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;

@EnableCaching
@EnableHystrix
@EnableFeignClients
@SpringBootApplication
@EnableHystrixDashboard
public class HelloApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(HelloApplication.class).build().run(args);
	}

	@Bean
	public Contract feignContract() {
		return new Contract.Default();
	}
}