package com.genius.primavera;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 14-17: JPA 고급 & 파일처리)
 * =============================================================================
 * 
 * 1️⃣ 기존 환경 종료:
 *    cd infrastructure
 *    docker-compose -f docker-compose.board.yml down
 * 
 * 2️⃣ JPA + Redis 환경 시작:
 *    docker-compose -f docker-compose.jpa.yml up -d
 * 
 * 3️⃣ Redis 상태 확인:
 *    docker exec -it redis-primavera-jpa redis-cli ping
 * 
 * 4️⃣ 애플리케이션 실행:
 *    ./gradlew :chap14:bootRun -Dspring.profiles.active=local
 * 
 * 5️⃣ API 테스트:
 *    http://localhost:8080/api/companies
 *    http://localhost:8080/api/departments
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308
 *    - Redis: localhost:6380
 *    - 데이터베이스: primavera_jpa_advanced, primavera_jpa_board
 * 
 * 📈 기능:
 *    - JPA 고급 매핑 (OneToMany, ManyToOne, ManyToMany)
 *    - Redis 캐싱
 *    - 복잡한 엔티티 관계
 * 
 * =============================================================================
 */
@SpringBootApplication
@EnableConfigurationProperties(ThymeleafProperties.class)
public class AdvancedJpaApplication {

	private static final String APPLICATION = "spring.config.location=classpath:/application-${spring.profiles.active:default}.yml,classpath:/social.yml";

	public static void main(String[] args) {
		new SpringApplicationBuilder(AdvancedJpaApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.properties(APPLICATION)
				.build()
				.run(args);
	}
}