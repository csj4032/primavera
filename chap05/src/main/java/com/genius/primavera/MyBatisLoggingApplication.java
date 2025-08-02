package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
 *    ./gradlew :chap05:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ API 테스트:
 *    curl http://localhost:8080/users
 *    curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name":"test","email":"test@test.com"}'
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308 (primavera/primavera)
 *    - MyBatis 로깅 활성화
 * 
 * =============================================================================
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.genius.primavera.domain.mapper")
public class MyBatisLoggingApplication {

	public static void main(String[] args) {
		log.debug("PrimaveraApplication Start Debug");
		log.info("PrimaveraApplication Start Info");  
		log.warn("PrimaveraApplication Start Warn");
		log.error("PrimaveraApplication Start Error");
		SpringApplication.run(MyBatisLoggingApplication.class, args);
	}
}