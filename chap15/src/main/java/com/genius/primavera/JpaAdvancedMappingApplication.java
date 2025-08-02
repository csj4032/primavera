package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 14-17: JPA 고급 & 파일처리)
 * =============================================================================
 * 
 * 1️⃣ JPA + Redis 환경 확인:
 *    cd infrastructure
 *    docker-compose -f docker-compose.jpa.yml ps
 * 
 * 2️⃣ 애플리케이션 실행:
 *    ./gradlew :chap15:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ API 테스트:
 *    http://localhost:8080/api/reactive/users
 *    http://localhost:8080/api/reactive/stream-users
 * 
 * 📊 기능:
 *    - 리액티브 프로그래밍 (WebFlux)
 *    - 비동기 데이터 스트리밍
 *    - 백프레셔 처리
 * 
 * 📈 모니터링:
 *    redis-cli -p 6380 ping
 *    curl http://localhost:8080/actuator/health
 * 
 * =============================================================================
 */
@SpringBootApplication
public class JpaAdvancedMappingApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpaAdvancedMappingApplication.class, args);
	}
}