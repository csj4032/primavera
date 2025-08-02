package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Data Access Application
 * 
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 01-05: Spring Boot 기초)
 * =============================================================================
 * 
 * 1️⃣ 인프라 시작:
 *    cd infrastructure
 *    docker-compose -f docker-compose.basic.yml up -d
 * 
 * 2️⃣ 애플리케이션 실행:
 *    ./gradlew :chap04:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ API 테스트:
 *    curl http://localhost:8080/users
 *    curl http://localhost:8080/products
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308 (primavera/primavera)
 *    - 데이터베이스: primavera, primavera_basic
 * 
 * =============================================================================
 */
@Slf4j
@SpringBootApplication
public class DataAccessApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataAccessApplication.class, args);
    }
}