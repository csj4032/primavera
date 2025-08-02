package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 06-11: 웹 개발 & MyBatis)
 * =============================================================================
 * 
 * 1️⃣ 기존 환경 종료:
 *    cd infrastructure
 *    docker-compose -f docker-compose.basic.yml down
 * 
 * 2️⃣ MyBatis 환경 시작:
 *    docker-compose -f docker-compose.mybatis.yml up -d
 * 
 * 3️⃣ 애플리케이션 실행:
 *    ./gradlew :chap06:bootRun -Dspring.profiles.active=local
 * 
 * 4️⃣ API 테스트:
 *    curl http://localhost:8080/validation/test
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308 (primavera/primavera) 
 *    - 데이터베이스: primavera_mybatis, primavera_mybatis_board
 * 
 * =============================================================================
 */
@SpringBootApplication
public class ValidationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValidationApplication.class, args);
	}
}