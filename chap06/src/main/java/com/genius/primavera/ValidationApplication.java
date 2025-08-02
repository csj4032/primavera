package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 04-09: 기본 환경)
 * =============================================================================
 * 
 * 1️⃣ 인프라 시작:
 *    cd infrastructure
 *    docker-compose -f docker-compose.basic.yml up -d
 * 
 * 2️⃣ Vault 토큰 설정:
 *    export VAULT_TOKEN=primavera-dev-token
 * 
 * 3️⃣ 애플리케이션 실행:
 *    ./gradlew :chap06:bootRun -Dspring.profiles.active=local
 * 
 * 4️⃣ API 테스트:
 *    curl http://localhost:8080/validation/test
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308 (primavera/primavera)
 *    - HashiCorp Vault: localhost:8200
 * 
 * 📊 기능:
 *    - Bean Validation 학습
 *    - 입력 값 검증
 * 
 * =============================================================================
 */
@SpringBootApplication
public class ValidationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValidationApplication.class, args);
	}
}