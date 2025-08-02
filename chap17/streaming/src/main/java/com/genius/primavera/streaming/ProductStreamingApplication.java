package com.genius.primavera.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * =============================================================================
 * 🐳 Docker Compose 실행 가이드 (Chapter 17: 데이터 파이프라인)
 * =============================================================================
 * 
 * 1️⃣ 데이터 파이프라인 환경 확인:
 *    cd infrastructure
 *    docker-compose -f docker-compose.data-pipeline.yml ps
 * 
 * 2️⃣ 스트리밍 애플리케이션 실행:
 *    ./gradlew :chap17:streaming:bootRun -Dspring.profiles.active=local
 * 
 * 3️⃣ 스트리밍 API 테스트:
 *    http://localhost:8080/stream/products
 *    http://localhost:8080/stream/events
 * 
 * 📊 기능:
 *    - 실시간 데이터 스트리밍
 *    - MariaDB 변경 감지 (CDC)
 *    - Elasticsearch 실시간 인덱싱
 *    - Server-Sent Events (SSE)
 * 
 * 📈 실시간 모니터링:
 *    curl http://localhost:9200/_cluster/health
 *    curl http://localhost:8080/search/products?q=keyword
 * 
 * =============================================================================
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductStreamingApplication.class, args);
    }
}