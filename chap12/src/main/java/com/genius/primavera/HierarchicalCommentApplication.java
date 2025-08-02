package com.genius.primavera;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
// import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 12-13: 고급 게시판 & 보안)
 * =============================================================================
 * 
 * 1️⃣ 기존 환경 종료:
 *    cd infrastructure
 *    docker-compose -f docker-compose.mybatis.yml down
 * 
 * 2️⃣ Board + Vault 환경 시작:
 *    docker-compose -f docker-compose.board.yml up -d
 * 
 * 3️⃣ Vault 초기화 확인:
 *    docker-compose -f docker-compose.board.yml logs vault-init
 * 
 * 4️⃣ Vault 토큰 설정:
 *    export VAULT_TOKEN=primavera-dev-token
 *    export VAULT_ADDR=http://localhost:8200
 * 
 * 5️⃣ 애플리케이션 실행:
 *    ./gradlew :chap12:bootRun -Dspring.profiles.active=local
 * 
 * 6️⃣ 웹 접속:
 *    http://localhost:8080 (계층형 댓글 시스템)
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308
 *    - HashiCorp Vault: localhost:8200
 *    - Vault Token: primavera-dev-token
 * 
 * =============================================================================
 */
@SpringBootApplication
public class HierarchicalCommentApplication {

	private static final String APPLICATION = "spring.config.location=classpath:/application-${spring.profiles.active:default}.yml,classpath:/social.yml";

	public static void main(String[] args) {
		new SpringApplicationBuilder(HierarchicalCommentApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.properties(APPLICATION)
				.build()
				.run(args);
	}
}