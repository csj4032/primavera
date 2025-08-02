package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 10-12: MyBatis + Redis)
 * =============================================================================
 * 
 * 1️⃣ 기존 환경 종료:
 *    cd infrastructure
 *    docker-compose -f docker-compose.basic.yml down
 * 
 * 2️⃣ MyBatis + Redis 환경 시작:
 *    docker-compose -f docker-compose.mybatis.yml up -d
 * 
 * 3️⃣ Vault 토큰 설정:
 *    export VAULT_TOKEN=primavera-dev-token
 * 
 * 4️⃣ 애플리케이션 실행:
 *    ./gradlew :chap10:bootRun -Dspring.profiles.active=local
 * 
 * 5️⃣ 웹 접속:
 *    http://localhost:8080
 *    OAuth2 로그인 지원: Google, Facebook, GitHub, Kakao
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308
 *    - Redis: localhost:6380 (OAuth2 토큰 캐싱)
 *    - HashiCorp Vault: localhost:8200
 * 
 * 📊 기능:
 *    - OAuth2 소셜 로그인
 *    - Redis 토큰 캐싱
 *    - 다중 소셜 제공자 지원
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