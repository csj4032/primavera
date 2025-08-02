package com.genius.primavera.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 17: 데이터 파이프라인)
 * =============================================================================
 * 
 * 1️⃣ 기존 환경 종료:
 *    cd infrastructure
 *    docker-compose -f docker-compose.board.yml down
 * 
 * 2️⃣ 데이터 파이프라인 환경 시작:
 *    docker-compose -f docker-compose.data-pipeline.yml up -d
 * 
 * 3️⃣ Elasticsearch 상태 확인:
 *    curl http://localhost:9200/_cluster/health
 * 
 * 4️⃣ 배치 애플리케이션 실행:
 *    ./gradlew :chap17:batch:bootRun -Dspring.profiles.active=local
 * 
 * 5️⃣ 배치 작업 실행:
 *    http://localhost:8080/batch/products/import
 *    http://localhost:8080/batch/products/export
 * 
 * 📊 사용 가능한 서비스:
 *    - MariaDB: localhost:3308
 *    - Elasticsearch: localhost:9200
 * 
 * 📊 기능:
 *    - Spring Batch 배치 처리
 *    - MariaDB → Elasticsearch 데이터 전송
 *    - 대용량 데이터 처리
 * 
 * 📈 데이터 확인:
 *    curl http://localhost:9200/_cat/indices
 *    curl http://localhost:9200/products/_search
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