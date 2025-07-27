# Chapter 18: Microservices Architecture - Complete System

## 📋 전체 개요

Chapter 18은 Primavera 교육 프로젝트의 최종 단계로, **완전한 마이크로서비스 아키텍처**를 구현합니다. 5개의 독립적인 서비스가 협력하여 실제 운영 환경에서 사용 가능한 분산 시스템을 구축하며, 현대적인 마이크로서비스 패턴과 기술스택을 종합적으로 학습할 수 있습니다.

## 🏗️ 시스템 아키텍처

### 마이크로서비스 구성
```
┌─────────────────────────────────────────────────────────────────┐
│                    Primavera Microservices                     │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   Front     │  │   Account   │  │    Order    │              │
│  │  Gateway    │  │   Service   │  │   Service   │              │
│  │   :8080     │  │    :8081    │  │    :8082    │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│         │                │                │                     │
│         └────────────────┼────────────────┘                     │
│                         │                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   Product   │  │    Config   │  │   External  │              │
│  │   Service   │  │   Server    │  │  Services   │              │
│  │    :8083    │  │    :8888    │  │             │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘

        ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
        │   MariaDB   │  │    Redis    │  │     Git     │
        │    :3306    │  │    :6379    │  │  Repository │
        └─────────────┘  └─────────────┘  └─────────────┘
```

## 🚀 마이크로서비스 상세

### 1. Configuration Service (핵심 인프라) - Port 8888
**역할**: 중앙집중식 설정 관리 및 배포  
**기술스택**: Spring Cloud Config Server, Git Integration

**주요 기능**:
- 모든 마이크로서비스의 설정 중앙 관리
- Git 저장소 기반 설정 버전 관리
- 환경별 설정 분리 (dev/test/prod)
- 런타임 설정 갱신 (`/refresh` 엔드포인트)
- 설정 암호화 및 보안 관리

**핵심 패턴**:
- **Configuration as Code**: Git을 통한 설정 버전 관리
- **Environment Isolation**: 환경별 설정 분리
- **Dynamic Configuration**: 무중단 설정 변경

### 2. Account Service (사용자 관리) - Port 8081
**역할**: 사용자 계정 관리 및 세션 처리  
**기술스택**: Spring WebFlux, Redis, Reactive Streams

**주요 기능**:
- 반응형 사용자 CRUD 작업
- Redis 기반 세션 관리  
- 비동기 데이터 처리 (Mono/Flux)
- RESTful API 제공

**핵심 패턴**:
- **Reactive Programming**: 완전한 비동기 처리
- **NoSQL Session Store**: Redis 기반 분산 세션
- **Event-Driven Architecture**: 반응형 이벤트 처리

### 3. Order Service (주문 관리) - Port 8082
**역할**: 주문 처리 및 할인 정책 엔진  
**기술스택**: Spring WebFlux, R2DBC, Functional Routing

**주요 기능**:
- 완전한 반응형 주문 처리
- R2DBC 기반 비동기 데이터베이스 액세스
- 함수형 라우팅 (RouterFunction)
- 전략 패턴 기반 할인 정책 엔진

**핵심 패턴**:
- **Functional Reactive**: RouterFunction + WebFlux
- **Strategy Pattern**: 유연한 할인 정책 구현
- **Domain-Driven Design**: 비즈니스 로직 중심 설계

### 4. Product Service (상품 관리) - Port 8083
**역할**: 상품 관리 및 고급 캐싱  
**기술스택**: Spring Boot, AOP, Custom Annotations

**주요 기능**:
- 고급 캐싱 시스템 (AOP 기반)
- 커스텀 애노테이션 기반 캐시 키 생성
- 상품 할인 정책 연계
- 성능 최적화 및 모니터링

**핵심 패턴**:
- **Aspect-Oriented Programming**: 횡단 관심사 처리
- **Custom Annotations**: 도메인 특화 애노테이션
- **Caching Strategy**: 다층 캐싱 전략

### 5. Front Service (API Gateway) - Port 8080
**역할**: API Gateway 및 서비스 오케스트레이션  
**기술스택**: Spring WebFlux, RestTemplate, Service Orchestration

**주요 기능**:
- 단일 진입점 (API Gateway)
- 여러 서비스 응답 집계 및 변환
- 서킷 브레이커 및 장애 격리
- 서비스 간 통신 조율

**핵심 패턴**:
- **API Gateway Pattern**: 단일 진입점
- **Service Orchestration**: 서비스 조합
- **Circuit Breaker**: 장애 전파 방지

## 📊 기술스택 매트릭스

| 서비스 | 웹 프레임워크 | 데이터 저장소 | 주요 패턴 | 특화 기술 |
|--------|-------------|-------------|----------|----------|
| **Configuration** | Spring Boot | Git Repository | Config as Code | Spring Cloud Config |
| **Account** | Spring WebFlux | Redis | Reactive Programming | Redis Hash, Session Management |
| **Order** | Spring WebFlux | MariaDB (R2DBC) | Functional Reactive | RouterFunction, Strategy Pattern |
| **Product** | Spring Boot | In-Memory/Cache | AOP & Caching | Custom Annotations, AspectJ |
| **Front** | Spring WebFlux | - | Service Orchestration | RestTemplate, Response Aggregation |

## 🌐 서비스 간 통신 플로우

### 1. 사용자 주문 조회 시나리오
```
Client Request → Front Service (8080)
                      ↓
    ┌─────────────────┼─────────────────┐
    ↓                 ↓                 ↓
Account Service   Order Service    Product Service
   (8081)           (8082)            (8083)
    ↓                 ↓                 ↓
   Redis           MariaDB            Cache
    ↓                 ↓                 ↓
    └─────────────────┼─────────────────┘
                      ↓
                 Aggregated Response
                      ↓
                   Client
```

### 2. 설정 관리 플로우
```
Git Repository → Config Server (8888)
                       ↓
      ┌────────────────┼────────────────┐
      ↓                ↓                ↓
  Account          Order           Product
  Service         Service         Service
    ↓                ↓                ↓
 Application     Application     Application
  Context         Context         Context
```

## 🔧 실행 방법

### 1. 인프라 준비
```bash
# Docker Compose로 통합 인프라 시작 (권장)
cd infrastructure
docker-compose up -d

# 개별 컨테이너 실행 (대안)
# MariaDB, Redis, Vault가 통합 설정으로 제공됩니다
# 상세 설정은 infrastructure/README.md 참조
```

### 2. 서비스 순차 시작 (의존성 순서)
```bash
# 1단계: 설정 서버 (다른 서비스들이 의존)
./gradlew :chap18:configuration:bootRun &
sleep 30  # 설정 서버 시작 대기

# 2단계: 백엔드 서비스들 (병렬 시작 가능)
./gradlew :chap18:account:bootRun &
./gradlew :chap18:order:bootRun &  
./gradlew :chap18:product:bootRun &
sleep 20  # 백엔드 서비스 시작 대기

# 3단계: 프론트 게이트웨이 (백엔드 서비스들에 의존)
./gradlew :chap18:front:bootRun &

# 전체 시스템 상태 확인
curl http://localhost:8888/actuator/health  # Config
curl http://localhost:8081/actuator/health  # Account  
curl http://localhost:8082/actuator/health  # Order
curl http://localhost:8083/actuator/health  # Product
curl http://localhost:8080/actuator/health  # Front
```

### 3. 통합 시나리오 테스트
```bash
# 사용자 생성 (Account Service)
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"name":"test_user"}'

# 상품 조회 (Product Service)  
curl http://localhost:8083/products/1

# 주문 생성 (Order Service)
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"amount":15000}'

# 통합 정보 조회 (Front Service)
curl http://localhost:8080/users/1/orders/full

# 할인 적용 테스트
curl -X POST http://localhost:8082/orders/1/discount-check \
  -H "Content-Type: application/json" \
  -d '["AMOUNT","EVENT"]'
```

## 📈 모니터링 및 관찰성

### 1. 헬스 체크 엔드포인트
```bash
# 전체 서비스 상태 모니터링 스크립트
#!/bin/bash
services=("8888:Config" "8081:Account" "8082:Order" "8083:Product" "8080:Front")

for service in "${services[@]}"; do
    port=$(echo $service | cut -d: -f1)
    name=$(echo $service | cut -d: -f2)
    
    status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health)
    if [ $status -eq 200 ]; then
        echo "✅ $name Service (Port $port): UP"
    else
        echo "❌ $name Service (Port $port): DOWN"
    fi
done
```

### 2. 로그 집계 설정
```yaml
# 각 서비스의 로깅 설정
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  level:
    com.genius.primavera: INFO
    org.springframework.cloud: DEBUG
```

### 3. 메트릭 수집
```java
// 공통 메트릭 수집 설정
@Configuration
public class MetricsConfiguration {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
}
```

## 🛡️ 보안 및 운영

### 1. 보안 설정
```yaml
# 프로덕션 보안 설정 예시
security:
  jwt:
    secret: ${JWT_SECRET:default-secret}
    expiration: 86400000  # 24시간
  
  cors:
    allowed-origins: 
      - http://localhost:3000
      - https://primavera.example.com
    allowed-methods: GET,POST,PUT,DELETE
    
  rate-limiting:
    enabled: true
    requests-per-minute: 100
```

### 2. 환경별 설정 관리
```bash
# 환경별 설정 파일 구조
config-repo/
├── account/
│   ├── account.yml          # 공통 설정
│   ├── account-dev.yml      # 개발 환경
│   ├── account-test.yml     # 테스트 환경  
│   └── account-prod.yml     # 운영 환경
├── front/
├── order/
└── product/
```

### 3. 운영 체크리스트
- [ ] 모든 서비스 헬스 체크 통과
- [ ] 데이터베이스 연결 정상
- [ ] Redis 캐시 동작 확인
- [ ] 서비스 간 통신 정상
- [ ] 설정 서버 동기화 완료
- [ ] 로그 레벨 운영 환경에 맞게 설정
- [ ] 보안 설정 검토 완료

## 🧪 테스트 전략

### 1. 단위 테스트 (각 서비스)
```bash
# 서비스별 단위 테스트 실행
./gradlew :chap18:account:test
./gradlew :chap18:order:test  
./gradlew :chap18:product:test
./gradlew :chap18:front:test
```

### 2. 통합 테스트
```bash
# TestContainers 기반 통합 테스트
./gradlew :chap18:integrationTest
```

### 3. 계약 테스트 (Contract Testing)
```java
// Pact 기반 계약 테스트 예시
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "account-service")
class FrontServiceContractTest {
    
    @Pact(consumer = "front-service")
    public RequestResponsePact userServicePact(PactDslWithProvider builder) {
        return builder
            .given("user exists")
            .uponReceiving("get user by id")
            .path("/users/1")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body("""
                {
                    "id": 1,
                    "name": "test_user",
                    "createDate": "2024-01-15T10:30:00Z"
                }
                """)
            .toPact();
    }
}
```

### 4. E2E 테스트 시나리오
```java
@SpringBootTest
@Testcontainers
class MicroservicesE2ETest {
    
    @Test
    void completeOrderWorkflowTest() {
        // 1. 사용자 생성
        // 2. 상품 조회  
        // 3. 주문 생성
        // 4. 통합 조회
        // 5. 할인 적용
        // 전체 워크플로우 검증
    }
}
```

## 📚 학습 목표 및 성과

### 🎯 핵심 학습 목표

1. **마이크로서비스 아키텍처 이해**
   - 서비스 분해 전략 및 경계 설정
   - 서비스 간 통신 패턴
   - 데이터 일관성 및 분산 트랜잭션

2. **Spring Cloud 생태계 활용**
   - Config Server를 통한 중앙집중식 설정 관리
   - Service Discovery 및 Load Balancing
   - Circuit Breaker 및 Resilience Patterns

3. **반응형 프로그래밍 마스터**
   - WebFlux를 통한 비동기 웹 개발
   - R2DBC를 통한 반응형 데이터베이스 액세스
   - Reactive Streams 및 백프레셔 제어

4. **고급 Spring 기법**
   - AOP를 활용한 횡단 관심사 처리
   - 커스텀 애노테이션 및 메타프로그래밍
   - 전략 패턴을 통한 유연한 비즈니스 로직

5. **운영 및 모니터링**
   - 분산 시스템 모니터링 및 로깅
   - 성능 최적화 및 캐싱 전략
   - 장애 처리 및 복구 메커니즘

### 🏆 학습 성과물

이 Chapter 18을 완료하면 다음과 같은 실무 역량을 확보할 수 있습니다:

- **Enterprise급 마이크로서비스** 설계 및 구현 능력
- **Spring Cloud** 기반 분산 시스템 구축 경험
- **반응형 프로그래밍** 패러다임의 실제 적용
- **고성능 캐싱** 시스템 설계 및 최적화
- **분산 시스템 운영** 및 문제 해결 능력

### 🔄 실무 적용 가능성

Chapter 18에서 학습한 패턴과 기술들은 다음과 같은 실제 프로젝트에 직접 적용할 수 있습니다:

- **대규모 이커머스 플랫폼**
- **금융 서비스 시스템**  
- **IoT 데이터 처리 플랫폼**
- **실시간 추천 시스템**
- **마이크로서비스 기반 SaaS 플랫폼**

## 📖 추가 학습 자료

### 권장 도서
- "Microservices Patterns" by Chris Richardson
- "Building Microservices" by Sam Newman  
- "Reactive Spring" by Josh Long
- "Spring in Action" by Craig Walls

### 온라인 리소스
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Spring WebFlux Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Microservices.io](https://microservices.io/)
- [Reactive Streams Specification](https://www.reactive-streams.org/)

---

**Chapter 18**은 Primavera 프로젝트의 최종 집대성으로, 현대적인 마이크로서비스 아키텍처의 모든 핵심 요소를 실습할 수 있는 완벽한 학습 환경을 제공합니다. 각 서비스의 독립적인 특성을 이해하면서도 전체 시스템의 조화로운 동작을 경험함으로써, 실무에서 바로 활용 가능한 분산 시스템 개발 역량을 획득할 수 있습니다.