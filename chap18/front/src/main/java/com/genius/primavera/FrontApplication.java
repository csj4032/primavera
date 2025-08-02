package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 18: 마이크로서비스)
 * =============================================================================
 * 
 * 1️⃣ 마이크로서비스 환경 확인:
 *    cd infrastructure
 *    docker-compose -f docker-compose.microservices.yml ps
 * 
 * 2️⃣ Frontend Gateway 실행:
 *    ./gradlew :chap18:front:bootRun -Dspring.profiles.active=local -Dserver.port=8080
 * 
 * 3️⃣ 웹 접속:
 *    http://localhost:8080 (메인 게이트웨이)
 *    http://localhost:8080/api/services (서비스 레지스트리)
 * 
 * 📊 기능:
 *    - API 게이트웨이
 *    - 서비스 디스커버리
 *    - 로드 밸런싱
 *    - 서킷 브레이커
 * 
 * 🔗 백엔드 서비스 연동:
 *    - Account Service: 8081
 *    - Product Service: 8082
 *    - Order Service: 8083
 * 
 * 📈 헬스체크:
 *    curl http://localhost:8080/actuator/health
 *    curl http://localhost:8080/api/services
 * 
 * =============================================================================
 */
@RefreshScope
@SpringBootApplication
@EnableConfigurationProperties({Config.class})
public class FrontApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontApplication.class, args);
	}

	@Bean
	public RestTemplateBuilder restTemplateBuilder() {
		return new RestTemplateBuilder();
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofMillis(1000))
				.setReadTimeout(Duration.ofMillis(3000))
				.build();
	}
}