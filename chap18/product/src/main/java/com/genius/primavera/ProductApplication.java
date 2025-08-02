package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 18: 마이크로서비스)
 * =============================================================================
 * 
 * 1️⃣ 마이크로서비스 환경 확인:
 *    cd infrastructure
 *    docker-compose -f docker-compose.microservices.yml ps
 * 
 * 2️⃣ Product Service 실행:
 *    ./gradlew :chap18:product:bootRun -Dspring.profiles.active=local -Dserver.port=8082
 * 
 * 3️⃣ API 테스트:
 *    curl http://localhost:8082/api/products
 *    curl http://localhost:8082/api/products/search?category=electronics
 * 
 * 📊 기능:
 *    - MongoDB 기반 상품 관리
 *    - Elasticsearch 검색
 *    - 리액티브 웹플럭스
 * 
 * 🔗 연동 서비스:
 *    - MongoDB: localhost:27017
 *    - Elasticsearch: localhost:9200
 *    - Kafka: localhost:9092 (이벤트 발행)
 * 
 * =============================================================================
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class ProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApplication.class, args);
	}
}