# Chapter 17 - Enterprise Data Pipeline with Spring Batch & Debezium Embedded

## 개요
Chapter 17은 **엔터프라이즈급 데이터 파이프라인**을 구현하는 프로젝트입니다. **Spring Batch**로 초기 대용량 인덱싱을 수행하고, **Debezium Embedded**로 실시간 변경사항을 감지하여 Elasticsearch를 업데이트하는 완전한 데이터 파이프라인을 구축합니다.

## 서브모듈 구조
```
chap17/
├── batch/          # Spring Batch 기반 초기 전체 인덱싱
└── streaming/      # Debezium Embedded 기반 실시간 CDC 업데이트
```

## 아키텍처 개요

### 전체 데이터 파이프라인
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   MariaDB       │    │ Debezium         │    │ Elasticsearch   │
│  (Source DB)    │────│ Embedded CDC     │────│ (Search Index)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
    ┌────▼────────┐        ┌────▼─────┐         ┌───────▼────────┐
    │ Spring Batch │        │  CDC     │         │   Search API   │
    │ (초기 인덱싱)  │        │ Engine   │         │    Service     │
    └─────────────┘        └──────────┘         └────────────────┘
```

## Phase 1: Spring Batch 초기 인덱싱 (chap17/batch)

### 주요 기능
- **대용량 전체 인덱싱**: Products + Sellers + Categories 조합
- **Chunk 기반 처리**: 효율적인 메모리 사용
- **Elasticsearch Bulk API**: 고성능 인덱싱
- **재시작 가능**: 실패 시 중단 지점부터 재개

### 기술 스택
- Spring Boot 3.x + Spring Batch 5.x
- Spring Data JPA
- Elasticsearch Java Client 8.x
- MariaDB 11.x

### 처리 흐름
```
Database JOIN Query → ItemReader → ItemProcessor → ItemWriter → Elasticsearch
     ↓                   ↓              ↓             ↓            ↓
   관계형 데이터        데이터 읽기    비정규화 변환  Bulk 인덱싱    검색 준비
```

## Phase 2: Debezium Embedded 실시간 업데이트 (chap17/streaming)

### 주요 기능
- **Debezium Embedded Engine**: Kafka 없이 직접 CDC 처리
- **MariaDB binlog 모니터링**: 실시간 변경 감지
- **Delta Indexing**: 변경된 데이터만 Elasticsearch 업데이트
- **경량 아키텍처**: 별도의 Kafka 인프라 불필요

### 기술 스택
- Spring Boot 3.x
- Debezium Embedded Engine
- Debezium MariaDB Connector
- Elasticsearch Java Client 8.x
- Kafka Connect API (Debezium 내부 사용)

### CDC 파이프라인
```
MariaDB binlog → Debezium Embedded → Change Events → Event Handler → Elasticsearch
     ↓              ↓                    ↓               ↓              ↓
   데이터 변경      CDC 감지           이벤트 생성      실시간 처리     즉시 업데이트
```

### Debezium Embedded 장점
- **인프라 단순화**: Kafka 클러스터 불필요
- **낮은 지연시간**: 직접 이벤트 처리
- **운영 간소화**: 관리 포인트 감소
- **리소스 효율**: 메모리/CPU 사용량 최소화

## 데이터 모델

### 원본 테이블 (MariaDB)
```sql
PRODUCTS        - 상품 기본 정보
├── id, name, description, price, status
├── seller_id   (FK → SELLERS)
├── category_id (FK → CATEGORIES)
└── created_at, updated_at

SELLERS         - 판매자 정보  
├── id, name, email, phone
├── business_number, rating
└── created_at

CATEGORIES      - 카테고리 정보
├── id, name, parent_id
├── level, path, is_active
└── created_at
```

### Elasticsearch Document
```json
{
  "productId": 1,
  "name": "고성능 게이밍 노트북",
  "description": "최신 RTX 그래픽카드 탑재",
  "price": 1500000,
  "status": "ACTIVE",
  
  "seller": {
    "id": 101,
    "name": "테크스토어",
    "email": "contact@techstore.com",
    "rating": 4.8
  },
  
  "category": {
    "id": 301,
    "name": "노트북",
    "fullPath": "전자제품 > 컴퓨터 > 노트북",
    "level": 3
  },
  
  "searchKeywords": ["게이밍", "노트북", "RTX"],
  "priceRange": "HIGH",
  "combinedText": "고성능 게이밍 노트북...",
  "indexedAt": "2025-01-27T10:30:00Z",
  "lastModified": "2025-01-27T10:30:00Z"
}
```

## 실행 방법

### 1. MariaDB 설정
```bash
# MariaDB binlog 활성화 확인
docker exec -it mariadb-primavera mysql -u root -p

# binlog 설정 확인
SHOW VARIABLES LIKE 'log_bin';
SHOW VARIABLES LIKE 'binlog_format';
SHOW VARIABLES LIKE 'server_id';

# 필요시 설정 (my.cnf)
[mysqld]
log-bin=mysql-bin
binlog-format=ROW
server-id=1
```

### 2. Elasticsearch 시작
```bash
# Docker로 Elasticsearch 실행
docker run -d --name elasticsearch-primavera \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  elasticsearch:8.12.0
```

### 3. Phase 1: 초기 인덱싱 실행
```bash
# 전체 상품 데이터 인덱싱
./gradlew :chap17:batch:bootRun
```

### 4. Phase 2: 실시간 CDC 시작
```bash
# Debezium Embedded 기반 실시간 업데이트
./gradlew :chap17:streaming:bootRun
```

### 5. 테스트 및 확인
```bash
# Elasticsearch 인덱스 확인
curl -X GET "localhost:9200/product_catalog_v1/_count"

# 실시간 업데이트 테스트 (MariaDB에서 데이터 변경)
mysql -h localhost -P 3306 -u primavera -p primavera
UPDATE PRODUCTS SET price = 2000000 WHERE id = 1;

# Elasticsearch에서 변경 확인
curl -X GET "localhost:9200/product_catalog_v1/_doc/1"
```

## Debezium Embedded 설정

### application.yml 설정 예시
```yaml
debezium:
  name: "mariadb-embedded-connector"
  connector:
    class: "io.debezium.connector.mariadb.MariaDbConnector"
  offset:
    storage: "org.apache.kafka.connect.storage.FileOffsetBackingStore"
    storage.file.filename: "/tmp/offsets.dat"
    flush.interval.ms: 60000
  database:
    hostname: "localhost"
    port: 3306
    user: "primavera"
    password: "primavera"
    server.id: 184054
    server.name: "primavera"
    include.list: "primavera"
    table.include.list: "primavera.PRODUCTS,primavera.SELLERS,primavera.CATEGORIES"
    history: "io.debezium.relational.history.FileDatabaseHistory"
    history.file.filename: "/tmp/dbhistory.dat"
```

### 이벤트 핸들러 구현
```java
@Component
public class DebeziumChangeEventHandler {
    
    @EventListener
    public void handleChangeEvent(ChangeEvent<String, String> event) {
        String key = event.key();
        String value = event.value();
        Operation operation = event.operation();
        
        switch (operation) {
            case CREATE, UPDATE -> indexToElasticsearch(value);
            case DELETE -> deleteFromElasticsearch(key);
        }
    }
}
```

## 모니터링

### JMX Metrics
- **처리된 이벤트 수**: debezium.streaming.events.processed
- **처리 지연시간**: debezium.streaming.lag.ms
- **오류 발생률**: debezium.streaming.errors.rate

### 로그 모니터링
```bash
# Debezium 이벤트 로그
tail -f logs/spring.log | grep "io.debezium"

# Elasticsearch 인덱싱 로그
tail -f logs/spring.log | grep "elasticsearch.indexing"
```

### 상태 확인 엔드포인트
```bash
# 애플리케이션 헬스체크
curl http://localhost:8080/actuator/health

# CDC 상태 확인
curl http://localhost:8080/actuator/cdc/status
```

## 확장 가능성

### 1. 멀티 소스 지원
- 여러 데이터베이스의 변경사항 통합
- 다양한 CDC 소스 (PostgreSQL, MongoDB 등)

### 2. 고급 변환
- 이벤트 필터링 및 변환 로직
- 복합 이벤트 처리 및 집계

### 3. 오류 처리
- Dead Letter Queue 패턴
- 재시도 및 복구 메커니즘

## 학습 포인트

### 1. Change Data Capture (CDC)
- **binlog 기반 CDC**: 데이터베이스 변경 실시간 감지
- **이벤트 기반 아키텍처**: 느슨한 결합과 확장성
- **최종 일관성**: 분산 시스템의 데이터 동기화

### 2. Debezium Embedded
- **경량 CDC**: Kafka 없이 CDC 구현
- **라이브러리 통합**: Spring Boot와 원활한 통합
- **오프셋 관리**: 재시작 시 이어서 처리

### 3. 운영 최적화
- **백프레셔 처리**: 시스템 과부하 방지
- **배치와 스트리밍 조합**: 초기 로드와 실시간 업데이트
- **모니터링과 알림**: 실시간 파이프라인 관리

이 프로젝트는 **경량화된 엔터프라이즈급 데이터 파이프라인**의 구현으로, Spring Batch의 배치 처리와 Debezium Embedded의 실시간 CDC를 조합한 효율적인 아키텍처를 학습할 수 있습니다.