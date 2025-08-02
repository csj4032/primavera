package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

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
 *    ./gradlew :chap10:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ 웹 접속:
 *    http://localhost:8080
 *    OAuth2 로그인 지원: Google, Facebook, GitHub, Kakao
 * 
 * 📊 기능:
 *    - OAuth2 소셜 로그인
 *    - 다중 소셜 제공자 지원
 *    - 사용자 정보 자동 동기화
 * 
 * =============================================================================
 */
@Slf4j
@SpringBootApplication
public class OAuth2SocialLoginApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplicationBuilder(OAuth2SocialLoginApplication.class)
                .bannerMode(Banner.Mode.CONSOLE)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("🛑 Primavera Application Shutting Down...");
            log.info("👋 Goodbye! Thank you for using Primavera Community Platform!");
        }));
        app.run(args);
    }
}