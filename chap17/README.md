# Chapter 17 - Enterprise Data Pipeline with Spring Batch, Kafka & Debezium

## 개요
Chapter 17은 **엔터프라이즈급 실시간 데이터 파이프라인**을 구현하는 프로젝트입니다. **Spring Batch**로 초기 대용량 인덱싱을 수행하고, **Debezium CDC + Kafka**로 실시간 변경사항을 감지하여 Elasticsearch를 업데이트하는 완전한 데이터 파이프라인을 구축합니다.

## 서브모듈 구조
```
chap17/
├── batch/          # Spring Batch 기반 초기 전체 인덱싱
└── streaming/      # Kafka + Debezium CDC 기반 실시간 업데이트
```

## 아키텍처 개요

### 전체 데이터 파이프라인
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   MariaDB       │    │   Apache Kafka   │    │ Elasticsearch   │
│  (Source DB)    │────│ + Debezium CDC   │────│ (Search Index)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
    ┌────▼────────┐        ┌────▼─────┐         ┌───────▼────────┐
    │ Spring Batch │        │  Kafka   │         │   Search API   │
    │ (초기 인덱싱)  │        │ Consumer │         │    Service     │
    └─────────────┘        └──────────┘         └────────────────┘
```

## Phase 1: Spring Batch 초기 인덱싱 (chap17/batch)

### 주요 기능
- **대용량 전체 인덱싱**: Products + Sellers + Categories 조합
- **Chunk 기반 처리**: 효율적인 메모리 사용
- **Elasticsearch Bulk API**: 고성능 인덱싱

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

## Phase 2: Kafka Streaming 실시간 업데이트 (chap17/streaming)

### 주요 기능
- **Debezium CDC**: MariaDB binlog 변경 감지
- **Kafka Streams**: 실시간 이벤트 처리
- **Delta Indexing**: 변경된 데이터만 Elasticsearch 업데이트

### 기술 스택
- Spring Boot 3.x + Spring Kafka
- Debezium CDC Connector
- Kafka Connect + Kafka Streams
- Elasticsearch Java Client 8.x

### CDC 파이프라인
```
MariaDB binlog → Debezium → Kafka Topics → Spring Kafka Consumer → Elasticsearch
     ↓              ↓           ↓              ↓                    ↓
   데이터 변경      CDC 감지    이벤트 발행    실시간 처리           즉시 업데이트
```

## 인프라 구성 (infrastructure/docker-compose.yml)

### 서비스 구성
- **MariaDB**: 소스 데이터베이스 (binlog 활성화)
- **Zookeeper**: Kafka 메타데이터 관리
- **Kafka**: 메시지 브로커
- **Kafka Connect**: Debezium CDC 커넥터
- **Debezium UI**: CDC 모니터링 대시보드
- **Elasticsearch**: 검색 인덱스

### 포트 구성
```
MariaDB:        3306 (chap17 모듈용)
Kafka:          9092
Kafka Connect:  8083
Debezium UI:    8080
Elasticsearch:  9200, 9300
Zookeeper:      2181
```

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

### 1. 인프라 시작
```bash
cd infrastructure
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

### 2. Debezium Connector 설정
```bash
# MariaDB Connector 등록
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "mariadb-connector",
    "config": {
      "connector.class": "io.debezium.connector.mysql.MySqlConnector",
      "tasks.max": "1",
      "database.hostname": "mariadb",
      "database.port": "3306",
      "database.user": "primavera",
      "database.password": "primavera",
      "database.server.id": "184054",
      "database.server.name": "primavera",
      "database.include.list": "primavera",
      "table.include.list": "primavera.PRODUCTS,primavera.SELLERS,primavera.CATEGORIES",
      "database.history.kafka.bootstrap.servers": "kafka:29092",
      "database.history.kafka.topic": "schema-changes.primavera",
      "include.schema.changes": "true",
      "transforms": "unwrap",
      "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState"
    }
  }'
```

### 3. Phase 1: 초기 인덱싱 실행
```bash
# 전체 상품 데이터 인덱싱
./gradlew :chap17:batch:bootRun
```

### 4. Phase 2: 실시간 스트리밍 시작
```bash
# CDC 기반 실시간 업데이트 시작
./gradlew :chap17:streaming:bootRun
```

### 5. 테스트 및 확인
```bash
# Elasticsearch 인덱스 확인
curl -X GET "localhost:9200/product_catalog_v1/_count"

# 실시간 업데이트 테스트 (MariaDB에서 데이터 변경)
mysql -h localhost -P 3306 -u primavera -p primavera
UPDATE PRODUCTS SET price = 2000000 WHERE id = 1;

# Kafka Topics 확인
docker exec kafka-primavera kafka-topics --list --bootstrap-server localhost:9092

# CDC 이벤트 확인
docker exec kafka-primavera kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic primavera.primavera.PRODUCTS \
  --from-beginning
```

## 모니터링

### Debezium UI
- **URL**: http://localhost:8080
- **기능**: CDC 커넥터 상태, 스키마 변경 추적

### Kafka Topics
- `primavera.primavera.PRODUCTS`: 상품 변경 이벤트
- `primavera.primavera.SELLERS`: 판매자 변경 이벤트  
- `primavera.primavera.CATEGORIES`: 카테고리 변경 이벤트

### Elasticsearch Monitoring
```bash
# 클러스터 상태
curl -X GET "localhost:9200/_cluster/health?pretty"

# 인덱스 통계
curl -X GET "localhost:9200/product_catalog_v1/_stats?pretty"
```

## 확장 가능성

### 1. 멀티 소스 지원
- 여러 데이터베이스의 변경사항 통합
- 다양한 CDC 소스 (Oracle, PostgreSQL 등)

### 2. 스케일링
- Kafka 파티셔닝을 통한 수평 확장
- 여러 Consumer 그룹으로 병렬 처리

### 3. 고급 변환
- Kafka Streams를 활용한 복합 이벤트 처리
- 실시간 집계 및 파생 데이터 생성

## 학습 포인트

### 1. Enterprise Integration Patterns
- **Change Data Capture**: 실시간 데이터 동기화
- **Event Sourcing**: 이벤트 기반 아키텍처
- **CQRS**: 명령과 조회 책임 분리

### 2. Kafka Ecosystem
- **Kafka Connect**: 외부 시스템 연동
- **Kafka Streams**: 스트림 처리
- **Schema Registry**: 스키마 진화 관리

### 3. Operational Excellence
- **모니터링**: 실시간 파이프라인 상태 추적
- **백프레셔**: 시스템 과부하 방지
- **오류 복구**: Dead Letter Queue 패턴

이 프로젝트는 **실무에서 사용되는 엔터프라이즈급 데이터 파이프라인**의 완전한 구현체로, Spring Batch의 배치 처리와 Kafka의 실시간 스트리밍을 조합한 하이브리드 아키텍처를 학습할 수 있습니다.