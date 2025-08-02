package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
 *    ./gradlew :chap08:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ API 테스트:
 *    curl http://localhost:8080/api/users
 *    curl http://localhost:8080/security/test
 * 
 * 📊 기능:
 *    - 보안 필터 체인
 *    - 커스텀 보안 설정
 *    - 요청 인증/인가
 * 
 * =============================================================================
 */
@SpringBootApplication
public class SecurityFilterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityFilterApplication.class, args);
    }
}