# Chapter 18: Complete Microservices Architecture

## 📋 프로젝트 개요

Chapter 18은 Primavera 교육 프로젝트의 최종 단계로, **완전한 마이크로서비스 아키텍처**를 구현합니다. 5개의 독립적인 서비스가 협력하여 실제 운영 환경에서 사용 가능한 분산 시스템을 구축하며, 현대적인 마이크로서비스 패턴과 기술스택을 종합적으로 학습할 수 있습니다.

## 🏗️ 마이크로서비스 아키텍처

### 시스템 구성도
```
┌─────────────────────────────────────────────────────────────────┐
│                    Primavera Microservices                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Client Request                                                │
│         ↓                                                      │
│  ┌─────────────┐                                               │
│  │    Front    │ ← API Gateway (Port 8080)                     │
│  │   Gateway   │                                               │
│  └─────────────┘                                               │
│         ↓                                                      │
│         ├──────────────────┬──────────────────┐                │
│         ↓                  ↓                  ↓                │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │   Account   │    │    Order    │    │   Product   │         │
│  │   Service   │    │   Service   │    │   Service   │         │
│  │    :8080    │    │    :8082    │    │    :8083    │         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
│         ↓                  ↓                  ↓                │
│      Redis            MariaDB             MongoDB              │
│     (Cache)           (R2DBC)            (NoSQL)               │
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │    Config   │    │    Kafka    │    │    Vault    │         │
│  │   Server    │    │  Message    │    │  Security   │         │
│  │    :8888    │    │   Queue     │    │  Manager    │         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
└─────────────────────────────────────────────────────────────────┘
```

### 서비스별 포트 및 역할

| 서비스 | 포트 | 역할 | 주요 기술 | 데이터 저장소 |
|--------|------|------|----------|--------------|
| **Configuration** | 8888 | 중앙화된 설정 관리 | Spring Cloud Config, Native Profile | Git Repository |
| **Account** | 8080 | 사용자 계정 관리 | Spring WebFlux, Reactive Streams | Redis |
| **Front** | 8081 | API Gateway & 서비스 오케스트레이션 | Spring WebFlux, RestTemplate | - |
| **Order** | 8082 | 주문 처리 & 할인 정책 | Spring WebFlux, R2DBC | MariaDB |
| **Product** | 8083 | 상품 관리 & 고급 캐싱 | Spring Boot, AOP, Custom Annotations | MongoDB |

## 🚀 주요 기능 및 패턴

### 1. Configuration Service (설정 서버) - Port 8888
**역할**: 중앙집중식 설정 관리 및 동적 설정 업데이트

**핵심 기능**:
- Native 프로파일 기반 설정 관리
- Vault 통합으로 보안 설정 암호화
- 런타임 설정 갱신 (`/refresh` 엔드포인트)
- 환경별 설정 분리 (local, test, prod)

**핵심 패턴**:
- **Configuration as Code**: Git 기반 설정 버전 관리
- **Environment Isolation**: 환경별 설정 분리
- **Dynamic Configuration**: 무중단 설정 변경

### 2. Account Service (사용자 관리) - Port 8080
**역할**: 반응형 사용자 관리 및 Redis 기반 세션 처리

**핵심 기능**:
- 완전한 반응형 사용자 CRUD (Mono/Flux)
- Redis Hash를 이용한 분산 세션 관리
- 비동기 데이터 처리 및 스트림 연산
- RESTful API 및 RouterFunction 지원

**핵심 패턴**:
- **Reactive Programming**: 완전한 비동기 처리
- **NoSQL Session Store**: Redis 기반 분산 세션
- **Event-Driven Architecture**: 반응형 이벤트 처리

### 3. Front Service (API Gateway) - Port 8081
**역할**: 단일 진입점 및 서비스 오케스트레이션

**핵심 기능**:
- API Gateway 패턴으로 단일 진입점 제공
- 여러 서비스 응답 집계 및 변환
- 서비스 URL 동적 설정 관리
- 부하 분산 및 장애 격리

**핵심 패턴**:
- **API Gateway Pattern**: 단일 진입점
- **Service Orchestration**: 서비스 조합
- **Response Aggregation**: 응답 집계 및 변환

### 4. Order Service (주문 관리) - Port 8082
**역할**: 주문 처리 및 전략 패턴 기반 할인 시스템

**핵심 기능**:
- R2DBC 기반 완전한 반응형 데이터베이스 액세스
- 함수형 라우팅 (RouterFunction) 구현
- 전략 패턴 기반 할인 정책 엔진
- Kafka를 통한 이벤트 기반 통신

**핵심 패턴**:
- **Functional Reactive**: RouterFunction + WebFlux
- **Strategy Pattern**: 유연한 할인 정책 구현
- **Event-Driven Messaging**: Kafka 기반 비동기 통신

### 5. Product Service (상품 관리) - Port 8083
**역할**: 상품 관리 및 AOP 기반 고급 캐싱 시스템

**핵심 기능**:
- AOP 기반 선언적 캐싱 시스템
- 커스텀 애노테이션을 통한 캐시 키 자동 생성
- MongoDB를 이용한 NoSQL 데이터 관리
- 성능 최적화 및 캐시 전략

**핵심 패턴**:
- **Aspect-Oriented Programming**: 횡단 관심사 처리
- **Custom Annotations**: 도메인 특화 애노테이션
- **Multi-layered Caching**: 다층 캐싱 전략

## 📊 인프라 구성

### Docker 컨테이너 서비스
```yaml
services:
  - MariaDB 11.4.7 (Port 3306) - Order 서비스 데이터
  - MongoDB 7.0 (Port 27017) - Product 서비스 데이터
  - Redis 7.0 (Port 6379) - Account 서비스 캐시
  - Apache Kafka 7.6.0 (Port 9092) - 메시지 큐
  - Apache Zookeeper (Port 2181) - Kafka 코디네이션
  - HashiCorp Vault (Port 8200) - 보안 설정 관리
```

### Kafka 토픽 구성
- `order-events`: 주문 생성/수정 이벤트
- `inventory-events`: 재고 변동 이벤트
- `order-cancelled-events`: 주문 취소 이벤트

## 🛠️ 최근 개선 사항

### 1. ✅ Configuration Service 설정 정리
**문제점**: application.yml과 application-local.yml의 설정 충돌
**해결책**:
- 기본 프로파일을 `native`로 통일
- Composite 설정에서 우선순위(order) 명확히 지정
- Vault 통합으로 보안 설정 중앙화

### 2. ✅ 서비스 포트 충돌 해결
**이전**: account와 front 서비스가 동일한 포트 사용
**개선**:
- Configuration Server: 8888
- Account Service: 8080 → Redis 연동
- Front Gateway: 8081 → API Gateway
- Order Service: 8082 → MariaDB + Kafka
- Product Service: 8083 → MongoDB + Kafka

### 3. ✅ 이벤트 기반 아키텍처 구축
**추가된 컴포넌트**:
- Apache Kafka + Zookeeper 클러스터
- 자동 토픽 생성 및 파티션 관리
- Producer/Consumer 설정 표준화
- Dead Letter Queue 패턴 구현

### 4. ✅ MongoDB NoSQL 통합
**Product Service 전용**:
- MongoDB 7.0 컨테이너 자동 배포
- 인증 및 초기 데이터베이스 설정
- Spring Data MongoDB Reactive 통합

### 5. ✅ 표준화된 모니터링
**모든 서비스 공통**:
- Spring Boot Actuator health check
- Management 엔드포인트 표준화
- 로깅 설정 통일 (Logback)

## 🚦 실행 방법

### 1. 인프라 환경 시작
```bash
# Docker 인프라 시작 (필수)
./docker-manager.sh start chap18

# 인프라 상태 확인
./docker-manager.sh status chap18
```

### 2. 서비스 순차 시작
```bash
# 1단계: Configuration Server (최우선)
SPRING_PROFILES_ACTIVE=native ./gradlew :chap18:configuration:bootRun &
sleep 10  # 설정 서버 시작 대기

# 2단계: 백엔드 마이크로서비스 (병렬 가능)
VAULT_TOKEN=primavera-vault-token SPRING_PROFILES_ACTIVE=local ./gradlew :chap18:account:bootRun &
VAULT_TOKEN=primavera-vault-token SPRING_PROFILES_ACTIVE=local ./gradlew :chap18:order:bootRun &
VAULT_TOKEN=primavera-vault-token SPRING_PROFILES_ACTIVE=local ./gradlew :chap18:product:bootRun &
sleep 15  # 백엔드 서비스 시작 대기

# 3단계: Front Gateway (마지막)
VAULT_TOKEN=primavera-vault-token SPRING_PROFILES_ACTIVE=local ./gradlew :chap18:front:bootRun &

# 서비스 상태 확인
curl -s http://localhost:8888/actuator/health | jq .  # Config Server
curl -s http://localhost:8080/actuator/health | jq .  # Account Service  
curl -s http://localhost:8081/actuator/health | jq .  # Front Gateway
curl -s http://localhost:8082/actuator/health | jq .  # Order Service
curl -s http://localhost:8083/actuator/health | jq .  # Product Service
```

### 3. 통합 API 테스트
```bash
# 사용자 생성 (Account Service)
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"test_user","email":"test@example.com"}'

# 상품 조회 (Product Service)
curl http://localhost:8083/products/1

# 주문 생성 (Order Service)
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":2,"amount":25000}'

# 통합 주문 정보 (Front Gateway)
curl http://localhost:8081/orders/1/full

# 할인 정책 적용 테스트
curl -X POST http://localhost:8082/orders/1/apply-discount \
  -H "Content-Type: application/json" \
  -d '["AMOUNT","EVENT","LEGAL"]'
```

## 📈 모니터링 및 관찰성

### 1. 서비스 상태 모니터링
```bash
#!/bin/bash
# health-check.sh - 전체 시스템 상태 확인

services=(
  "8888:Configuration Server"
  "8080:Account Service" 
  "8081:Front Gateway"
  "8082:Order Service"
  "8083:Product Service"
)

echo "🔍 Primavera Microservices Health Check"
echo "========================================"

for service in "${services[@]}"; do
  port=$(echo $service | cut -d: -f1)
  name=$(echo $service | cut -d: -f2)
  
  status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health || echo "000")
  
  if [ "$status" = "200" ]; then
    echo "✅ $name (Port $port): UP"
  else
    echo "❌ $name (Port $port): DOWN (HTTP $status)"
  fi
done

echo ""
echo "📊 Infrastructure Services"
echo "=========================="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep primavera-chap18
```

### 2. Kafka 모니터링
```bash
# Kafka 토픽 상태 확인
docker exec kafka-primavera-chap18 kafka-topics.sh \
  --bootstrap-server localhost:9092 --list

# 특정 토픽의 메시지 소비
docker exec kafka-primavera-chap18 kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
```

### 3. 로그 집계 설정
```bash
# 전체 마이크로서비스 로그 실시간 모니터링
tail -f logs/{account,front,order,product}/{info,error}/*.log

# 특정 서비스 로그 필터링
grep -E "(ERROR|WARN)" logs/order/info/info-$(date +%Y-%m-%d).log
```

## 🧪 테스트 전략

### 1. 단위 테스트 (각 서비스별)
```bash
# 서비스별 개별 테스트 실행
./gradlew :chap18:configuration:test
./gradlew :chap18:account:test
./gradlew :chap18:front:test  
./gradlew :chap18:order:test
./gradlew :chap18:product:test

# 전체 마이크로서비스 테스트
./gradlew :chap18:test
```

### 2. 통합 테스트 (TestContainers)
```java
@SpringBootTest
@Testcontainers
@EnablePrimaveraTestcontainers
class MicroservicesIntegrationTest {
    
    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7");
    
    @Container 
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Test
    void completeOrderWorkflowTest() {
        // 1. 사용자 생성 → Account Service
        // 2. 상품 조회 → Product Service
        // 3. 주문 생성 → Order Service  
        // 4. 통합 조회 → Front Gateway
        // 전체 마이크로서비스 워크플로우 검증
    }
}
```

### 3. Contract Testing (계약 테스트)
```java
// 서비스 간 계약 검증
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "account-service")
class FrontGatewayContractTest {
    
    @Pact(consumer = "front-gateway")
    public RequestResponsePact createUserPact(PactDslWithProvider builder) {
        return builder
            .given("user service is available")
            .uponReceiving("create user request")
            .path("/users")
            .method("POST")
            .willRespondWith()
            .status(201)
            .body("{\"id\": 1, \"name\": \"test_user\"}")
            .toPact();
    }
}
```

## 🏆 학습 목표 및 성과

### 🎯 핵심 학습 목표

1. **마이크로서비스 아키텍처 패턴**
   - 서비스 분해 전략 및 경계 설정
   - 서비스 간 통신 및 데이터 일관성
   - 분산 시스템의 복잡성 관리

2. **Spring Cloud 생태계 마스터**
   - Config Server를 통한 중앙집중식 설정
   - Service Discovery 및 Load Balancing
   - Circuit Breaker 패턴 및 Resilience

3. **반응형 프로그래밍 실무 적용**
   - WebFlux를 통한 비동기 웹 개발
   - R2DBC 반응형 데이터베이스 액세스
   - Reactive Streams 백프레셔 제어

4. **이벤트 기반 아키텍처**
   - Apache Kafka를 통한 비동기 메시징
   - Event Sourcing 및 CQRS 패턴
   - Saga 패턴을 통한 분산 트랜잭션

5. **운영 및 모니터링**
   - 분산 시스템 관찰성 (Observability)
   - 성능 최적화 및 캐싱 전략
   - 장애 처리 및 복구 메커니즘

### 🏆 실무 적용 역량

이 Chapter 18을 완주하면 다음과 같은 실무 수준의 역량을 확보할 수 있습니다:

- **Enterprise급 마이크로서비스** 설계 및 구현
- **대용량 트래픽 처리**를 위한 반응형 시스템 구축
- **클라우드 네이티브** 환경에서의 서비스 배포 및 운영
- **이벤트 기반 아키텍처**를 통한 확장 가능한 시스템 설계
- **DevOps** 관점에서의 마이크로서비스 운영 및 모니터링

### 🌟 실제 적용 가능 프로젝트

- **대규모 이커머스 플랫폼** (주문, 결제, 배송 시스템)
- **금융 서비스 시스템** (계좌, 거래, 정산 처리)
- **IoT 플랫폼** (디바이스 관리, 데이터 수집, 분석)
- **실시간 추천 시스템** (사용자 행동 분석, 개인화)
- **SaaS 플랫폼** (멀티테넌트, 확장성, 보안)

## 🔧 추가 개선 사항

### 우선순위 높음
- [ ] **Circuit Breaker** 패턴 구현 (Resilience4j)
- [ ] **Service Discovery** 추가 (Eureka 또는 Consul)
- [ ] **API Gateway** 고급 기능 (Rate Limiting, Authentication)
- [ ] **분산 트레이싱** 통합 (Zipkin 또는 Jaeger)

### 우선순위 중간  
- [ ] **RestTemplate → WebClient** 전환
- [ ] **로그 집중화** (ELK Stack 통합)
- [ ] **메트릭 수집** (Prometheus + Grafana)
- [ ] **계약 테스트** 확장 (Spring Cloud Contract)

### 우선순위 낮음
- [ ] **Kubernetes** 배포 설정 추가
- [ ] **환경별 Docker Compose** 분리
- [ ] **보안 강화** (OAuth2, JWT 통합)
- [ ] **성능 테스트** 자동화 (JMeter, K6)

---

**Chapter 18**은 Primavera 프로젝트의 최종 집대성으로, 현대적인 마이크로서비스 아키텍처의 모든 핵심 요소를 실전에서 학습할 수 있는 완벽한 환경을 제공합니다. 각 서비스의 독립성과 전체 시스템의 조화로운 협력을 통해 실무에서 바로 활용 가능한 분산 시스템 개발 역량을 완성할 수 있습니다.