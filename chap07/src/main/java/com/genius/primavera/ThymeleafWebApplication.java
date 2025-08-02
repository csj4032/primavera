package com.genius.primavera;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 06-11: 웹 개발 & MyBatis)
 * =============================================================================
 * 
 * 1️⃣ MyBatis 환경 확인:
 *    cd infrastructure
 *    docker-compose -f docker-compose.mybatis.yml ps
 * 
 * 2️⃣ 애플리케이션 실행:
 *    ./gradlew :chap07:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ 웹 접속:
 *    http://localhost:8080
 *    http://localhost:8080/users
 * 
 * 📊 기능:
 *    - Thymeleaf 템플릿 엔진
 *    - 웹 페이지 렌더링
 *    - Layout Dialect 지원
 * 
 * =============================================================================
 */
@Configuration
@SpringBootApplication
public class ThymeleafWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThymeleafWebApplication.class, args);
	}

	@Bean
	public LayoutDialect layoutDialect() {
		return new LayoutDialect();
	}
}