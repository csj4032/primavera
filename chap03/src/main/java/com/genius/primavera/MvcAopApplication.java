package com.genius.primavera;

import com.genius.primavera.infrastructure.interception.PrimaveraInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 01-05: Spring Boot 기초)
 * =============================================================================
 * 
 * 1️⃣ 인프라 시작:
 *    cd infrastructure
 *    docker-compose -f docker-compose.basic.yml up -d
 * 
 * 2️⃣ 애플리케이션 실행:
 *    ./gradlew :chap03:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ 접속:
 *    http://localhost:8080
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308 (primavera/primavera)
 * 
 * =============================================================================
 */
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