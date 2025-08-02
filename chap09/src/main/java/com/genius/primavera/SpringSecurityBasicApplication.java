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
 *    ./gradlew :chap09:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ 웹 접속:
 *    http://localhost:8080/login
 *    기본 계정: admin@primavera.com / password
 * 
 * 📊 기능:
 *    - Spring Security 기본 인증
 *    - 로그인/로그아웃
 *    - 역할 기반 접근 제어
 * 
 * =============================================================================
 */
@SpringBootApplication
public class SpringSecurityBasicApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityBasicApplication.class, args);
	}
}