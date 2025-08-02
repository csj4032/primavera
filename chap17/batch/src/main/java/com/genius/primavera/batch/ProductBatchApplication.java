package com.genius.primavera.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

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
 *    ./gradlew :chap17:batch:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ 배치 작업 실행:
 *    http://localhost:8080/batch/products/import
 *    http://localhost:8080/batch/products/export
 * 
 * 📊 기능:
 *    - Spring Batch 배치 처리
 *    - 대용량 데이터 처리
 *    - Job 실행 및 모니터링
 * 
 * 📈 배치 모니터링:
 *    curl http://localhost:8080/actuator/metrics/spring.batch
 *    curl http://localhost:8080/batch/jobs/status
 * 
 * =============================================================================
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductBatchApplication.class, args);
    }
}