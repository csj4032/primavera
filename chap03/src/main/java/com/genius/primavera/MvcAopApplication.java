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
 * 🚀 실행 가이드 (Chapter 03: MVC & AOP)
 * =============================================================================
 * 
 * ✅ Docker 환경 불필요 - MVC/AOP 학습용
 * 
 * 📋 애플리케이션 실행:
 *    ./gradlew :chap03:bootRun
 * 
 * 🔗 접속:
 *    http://localhost:8080
 * 
 * 📊 기능:
 *    - Spring MVC 기본 구조
 *    - AOP (Aspect Oriented Programming)
 *    - 인터셉터 패턴
 *    - 웹 요청 처리
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