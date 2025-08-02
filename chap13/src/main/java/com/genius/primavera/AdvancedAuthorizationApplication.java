package com.genius.primavera;

import com.genius.primavera.application.storage.StorageService;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 13: 고급 인증/인가)
 * =============================================================================
 * 
 * 1️⃣ 기존 환경 종료:
 *    cd infrastructure
 *    docker-compose -f docker-compose.mybatis.yml down
 * 
 * 2️⃣ 고급 보안 환경 시작:
 *    docker-compose -f docker-compose.board.yml up -d
 * 
 * 3️⃣ Vault 토큰 설정 (필수):
 *    export VAULT_TOKEN=primavera-dev-token
 *    export VAULT_ADDR=http://localhost:8200
 * 
 * 4️⃣ 애플리케이션 실행:
 *    ./gradlew :chap13:bootRun --args='--server.ssl.enabled=false --server.port=8013 --spring.profiles.active=local'
 * 
 * 5️⃣ 웹 접속:
 *    http://localhost:8013/login
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308
 *    - MongoDB: localhost:27017
 *    - HashiCorp Vault: localhost:8200
 * 
 * 📊 기능:
 *    - 고급 인증/인가 시스템
 *    - Vault 기반 설정 관리
 *    - MongoDB 리액티브 저장소
 *    - 파일 업로드/다운로드
 * 
 * 🔐 Vault 설정 확인:
 *    curl -H "X-Vault-Token: primavera-dev-token" http://localhost:8200/v1/sys/health
 * 
 * =============================================================================
 */
@SpringBootApplication
@EnableMongoAuditing
@EnableReactiveMongoRepositories
@EnableConfigurationProperties(ThymeleafProperties.class)
public class AdvancedAuthorizationApplication {

	private static final String APPLICATION = "spring.config.location=classpath:/application-${spring.profiles.active:local}.yml,classpath:/social.yml";

	public static void main(String[] args) {
		new SpringApplicationBuilder(AdvancedAuthorizationApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.properties(APPLICATION)
				.build()
				.run(args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}
}