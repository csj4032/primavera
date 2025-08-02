package com.genius.primavera;

import com.genius.primavera.infrastructure.sentry.EnableSentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.ArrayList;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 14-17: JPA 고급 & 파일처리)
 * =============================================================================
 * 
 * 1️⃣ JPA + Redis 환경 확인:
 *    cd infrastructure
 *    docker-compose -f docker-compose.jpa.yml ps
 * 
 * 2️⃣ 애플리케이션 실행:
 *    ./gradlew :chap16:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ 기능 테스트:
 *    http://localhost:8080/files/upload (파일 업로드)
 *    http://localhost:8080/actuator/metrics (모니터링)
 *    http://localhost:8080/actuator/health
 * 
 * 📊 기능:
 *    - 파일 업로드/다운로드
 *    - 바이러스 스캔 시뮬레이션
 *    - Sentry 오류 모니터링
 *    - Spring Boot Actuator 메트릭
 * 
 * 📈 모니터링 확인:
 *    curl http://localhost:8080/actuator/metrics/files.uploaded
 *    curl http://localhost:8080/actuator/prometheus
 * 
 * =============================================================================
 */
@Slf4j
@EnableSentry
@SpringBootApplication
public class FileProcessingMonitoringApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(FileProcessingMonitoringApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.build()
				.run(args);
	}
}