package com.genius.primavera;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

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
 *    ./gradlew :chap11:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ 웹 접속:
 *    http://localhost:8080
 *    http://localhost:8080/board
 * 
 * 📊 기능:
 *    - 게시판 시스템
 *    - CRUD 게시글 관리
 *    - 사용자 인증 통합
 * 
 * =============================================================================
 */
@SpringBootApplication
public class BoardSystemApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(BoardSystemApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.build()
				.run(args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}