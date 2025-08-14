# Chapter 18 개선 사항

## 📋 개선 내역

### 1. Configuration Service 설정 정리 ✅
- **문제점**: `application.yml`과 `application-local.yml`의 설정 충돌
- **해결**: 
  - 기본 프로파일을 `native`로 유지
  - `application-local.yml`에서 composite 설정 명확화
  - 각 소스의 우선순위(order) 지정

### 2. 서비스 포트 충돌 해결 ✅
- **문제점**: account와 front 서비스가 동일한 포트(8081) 사용
- **해결**:
  - Account Service: 8081
  - Order Service: 8082
  - Product Service: 8083
  - Front Gateway: 8080
  - Configuration Server: 8888

### 3. Kafka 인프라 추가 ✅
- **추가 컴포넌트**:
  - Zookeeper (포트 2181)
  - Kafka (포트 9092)
  - Kafka UI (포트 8090) - 모니터링용
- **토픽 자동 생성**: `order-events`, `inventory-events`

### 4. MongoDB 인프라 추가 ✅
- Product Service용 MongoDB 추가 (포트 27017)
- 인증 설정 및 초기 데이터베이스 생성

### 5. 서비스 설정 표준화 ✅
- 모든 서비스에 health check 엔드포인트 추가
- Kafka 설정 추가 (Order, Product Service)
- Management 엔드포인트 노출 설정 통일

### 6. 실행 스크립트 개선 ✅
- `start-services.sh`: 전체 서비스 자동 시작
- `stop-services.sh`: 전체 서비스 종료
- `kafka-init.sh`: Kafka 토픽 초기화

## 🚀 실행 방법

### 전체 시스템 시작
```bash
# 1. 인프라 및 모든 서비스 시작
./start-services.sh

# 2. 서비스 상태 확인
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8081/actuator/health  # Account Service
curl http://localhost:8082/actuator/health  # Order Service
curl http://localhost:8083/actuator/health  # Product Service
curl http://localhost:8080/actuator/health  # Front Gateway
```

### 개별 인프라 관리
```bash
# 전체 인프라 시작 (Kafka, MongoDB 포함)
cd infrastructure
docker-compose -f docker-compose-full.yml up -d

# 기본 인프라만 시작 (MariaDB, Redis, Vault)
docker-compose up -d

# 인프라 중지
docker-compose -f docker-compose-full.yml down

# 인프라 및 데이터 완전 삭제
docker-compose -f docker-compose-full.yml down -v
```

### 개별 서비스 실행
```bash
# Configuration Server (필수 - 먼저 실행)
SPRING_PROFILES_ACTIVE=native ./gradlew :chap18:configuration:bootRun

# 각 마이크로서비스
./gradlew :chap18:account:bootRun
./gradlew :chap18:order:bootRun
./gradlew :chap18:product:bootRun
./gradlew :chap18:front:bootRun
```

## 📊 아키텍처 개선

### Before
```
Client → Front(8081❌) → Services
         Config Server (설정 충돌)
         No Kafka/MongoDB
```

### After
```
Client → Front(8080✅) → Account(8081)
                      → Order(8082) ← Kafka → Product(8083)
                      → Product(8083) ← MongoDB
         
Config Server(8888) → Native + Vault (우선순위 명확)
```

## 🔧 남은 개선 사항

### 우선순위 높음
1. Circuit Breaker 패턴 구현 (Resilience4j)
2. Service Discovery 추가 (Eureka/Consul)
3. API Gateway 기능 강화 (Spring Cloud Gateway)
4. 분산 트레이싱 (Zipkin/Jaeger)

### 우선순위 중간
1. RestTemplate → WebClient 전환 (Front Service)
2. 로그 집중화 (ELK Stack)
3. 메트릭 수집 (Prometheus + Grafana)
4. 계약 테스트 추가 (Spring Cloud Contract)

### 우선순위 낮음
1. OrderRouter @Deprecated 제거
2. 로그 메시지 정리
3. 환경별 Docker Compose 파일 분리
4. Kubernetes 배포 설정 추가

## 📝 설정 파일 구조

```
chap18/
├── configuration/
│   └── src/main/resources/
│       ├── application.yml (native 프로파일)
│       ├── application-local.yml (composite 설정)
│       └── config/
│           ├── account.yml (포트 8081)
│           ├── order.yml (포트 8082, Kafka 설정)
│           ├── product.yml (포트 8083, MongoDB/Kafka 설정)
│           └── front.yml (포트 8080, 서비스 URL 설정)
├── infrastructure/
│   ├── docker-compose.yml (기본 인프라)
│   ├── docker-compose-full.yml (전체 인프라)
│   └── kafka-init.sh (토픽 초기화)
├── start-services.sh (전체 시작)
└── stop-services.sh (전체 종료)
```

## 🎯 학습 포인트

1. **마이크로서비스 설정 관리**: Spring Cloud Config Server를 통한 중앙화
2. **이벤트 기반 아키텍처**: Kafka를 통한 서비스 간 비동기 통신
3. **다양한 데이터 저장소**: MariaDB(RDB), Redis(캐시), MongoDB(NoSQL)
4. **반응형 프로그래밍**: WebFlux, R2DBC를 통한 비동기 처리
5. **인프라 자동화**: Docker Compose를 통한 환경 구성