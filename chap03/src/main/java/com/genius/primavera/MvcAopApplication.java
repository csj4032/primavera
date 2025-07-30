package com.genius.primavera;

import com.genius.primavera.infrastructure.interception.PrimaveraInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@SpringBootApplication
public class MvcAopApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(MvcAopApplication.class)
				.lazyInitialization(true)
				.build()
				.run();
	}

	@NotNull
	private static WebMvcConfigurer getWebMvcConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addInterceptors(InterceptorRegistry interceptorRegistry) {
				interceptorRegistry.addInterceptor(new PrimaveraInterceptor()).addPathPatterns("/*");
			}
		};
	}
}