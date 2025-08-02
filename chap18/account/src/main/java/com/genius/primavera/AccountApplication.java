package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 18: 마이크로서비스)
 * =============================================================================
 * 
 * 1️⃣ 기존 환경 종료:
 *    cd infrastructure
 *    docker-compose -f docker-compose.jpa.yml down
 * 
 * 2️⃣ 마이크로서비스 전체 환경 시작 (시간 소요):
 *    docker-compose -f docker-compose.microservices.yml up -d
 * 
 * 3️⃣ 모든 서비스 시작 대기 (약 2-3분):
 *    docker-compose -f docker-compose.microservices.yml logs -f
 * 
 * 4️⃣ Account Service 실행:
 *    ./gradlew :chap18:account:bootRun -Dspring.profiles.active=local -Dserver.port=8081
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308
 *    - Redis: localhost:6380
 *    - MongoDB: localhost:27017
 *    - Kafka: localhost:9092
 *    - Elasticsearch: localhost:9200
 *    - Vault: localhost:8200
 * 
 * 🔗 마이크로서비스 포트:
 *    - Account Service: 8081
 *    - Product Service: 8082
 *    - Order Service: 8083
 *    - Frontend Gateway: 8080
 * 
 * =============================================================================
 */
@Slf4j
@SpringBootApplication
public class AccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountApplication.class, args);
	}
}