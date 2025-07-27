# Account Service - User Management Microservice

## 📋 Overview

Account Service는 Primavera 마이크로서비스 아키텍처에서 사용자 계정 관리를 담당하는 핵심 서비스입니다. Spring WebFlux와 Redis를 활용한 반응형 프로그래밍 패턴으로 구현되어 높은 성능과 확장성을 제공합니다.

## 🏗️ 아키텍처 특성

### Core Technologies
- **Spring Boot 3.3.6**: 최신 스프링 부트 프레임워크
- **Spring WebFlux**: 비동기 반응형 웹 프레임워크
- **Spring Data Redis Reactive**: 반응형 Redis 데이터 액세스
- **Spring Cloud Config**: 중앙집중식 설정 관리
- **Spring AOP**: 횡단 관심사 처리

### Reactive Programming Pattern
```java
@RedisHash("USER")
public class User implements Serializable {
    @Id
    private long id;
    private String name;
    
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private Instant createDate;
}
```

## 🚀 주요 기능

### 1. 사용자 계정 관리
- **사용자 등록/조회/수정/삭제**: RESTful API를 통한 CRUD 작업
- **반응형 스트림**: Mono/Flux를 활용한 비동기 데이터 처리
- **Redis 세션 관리**: 분산 환경에서의 세션 상태 관리

### 2. 데이터 저장소
- **Redis Hash 구조**: `USER:{id}` 패턴으로 사용자 데이터 저장
- **자동 타임스탬프**: `@CreatedDate`를 통한 생성 시간 자동 기록
- **JSON 직렬화**: Jackson을 통한 효율적인 데이터 변환

### 3. 반응형 웹 계층
```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public Mono<User> getUser(@PathVariable String id) {
        return userRepository.findById(id);
    }
    
    @PostMapping("/users")
    public Mono<User> createUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}
```

## 🔧 설정 및 구성

### 애플리케이션 설정
```yaml
spring:
  application:
    name: account
  cloud:
    config:
      uri: http://localhost:8888  # Config Server 연결

server:
  port: 8081
  tomcat:
    threads:
      max: 1                     # 단일 스레드 모델 (WebFlux)
```

### Redis 설정
```java
@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        // Redis 연결 설정
    }
}
```

## 📊 성능 최적화

### 1. 반응형 스트림 처리
- **Non-blocking I/O**: WebFlux를 통한 비동기 요청 처리
- **백프레저 제어**: Reactor 패턴으로 시스템 안정성 확보
- **메모리 효율성**: 스트림 기반 데이터 처리로 메모리 사용량 최적화

### 2. Redis 캐싱 전략
- **Hash 데이터 구조**: 효율적인 메모리 사용
- **TTL 설정**: 자동 만료를 통한 메모리 관리
- **연결 풀링**: HikariCP와 유사한 Redis 연결 관리

### 3. 단일 스레드 모델
```yaml
server:
  tomcat:
    threads:
      max: 1  # WebFlux 특성상 적은 스레드로 높은 성능
```

## 🔍 Redis 데이터 구조

### 사용자 데이터 저장 패턴
```shell
# Redis CLI 예제 명령어
redis-cli

# 사용자 데이터 저장
HMSET USER:1 id 1 name "genius" createDate "2024-01-15T10:30:00Z"

# 사용자 데이터 조회
HGETALL USER:1

# 특정 필드 조회
HMGET USER:1 id name createDate

# 필드 수 확인
HLEN USER:1

# 필드 삭제
HDEL USER:1 device

# 존재하지 않는 경우에만 설정
HSETNX USER:1 status "active"
```

### 데이터 스키마
```json
{
  "id": 1,
  "name": "genius",
  "createDate": "2024-01-15T10:30:00Z"
}
```

## 🌐 API 엔드포인트

### 사용자 관리 API
```http
# 사용자 생성
POST /users
Content-Type: application/json
{
  "name": "new_user"
}

# 사용자 조회
GET /users/{id}

# 사용자 목록 조회
GET /users

# 사용자 수정
PUT /users/{id}
Content-Type: application/json
{
  "name": "updated_user"
}

# 사용자 삭제
DELETE /users/{id}
```

## 🏃‍♂️ 실행 방법

### 1. Redis 서버 시작
```bash
# Docker로 Redis 실행
docker run -d --name redis-account \
  -p 6379:6379 \
  redis:7-alpine

# Redis 연결 확인
redis-cli ping
```

### 2. Config Server 시작
```bash
# Configuration 서비스 먼저 실행
./gradlew :chap18:configuration:bootRun
```

### 3. Account Service 시작
```bash
# Account 서비스 실행
./gradlew :chap18:account:bootRun

# 또는 직접 JAR 실행
java -jar account/build/libs/account.jar
```

### 4. 서비스 상태 확인
```bash
# Health Check
curl http://localhost:8081/actuator/health

# 사용자 생성 테스트
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"name":"test_user"}'
```

## 🔗 서비스 간 연계

### Config Server 의존성
- **설정 중앙화**: 모든 설정을 Config Server에서 관리
- **동적 설정 갱신**: `@RefreshScope`를 통한 실시간 설정 변경
- **환경별 설정**: dev/test/prod 환경별 설정 분리

### 다른 마이크로서비스와의 통신
```java
// Front Service에서 Account Service 호출 예제
@Service
public class FrontServiceImpl implements FrontService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Mono<User> getUserInfo(String userId) {
        return restTemplate.getForObject(
            "http://localhost:8081/users/{id}", 
            User.class, 
            userId
        );
    }
}
```

## 📈 모니터링 및 로깅

### 로깅 설정
```yaml
logging:
  level:
    org.springframework: DEBUG
    com.genius.primavera: INFO
```

### 주요 로그 포인트
- 사용자 생성/수정/삭제 이벤트
- Redis 연결 상태
- Config Server 연결 상태
- 예외 및 에러 상황

## 🧪 테스트 전략

### 단위 테스트
```java
@Test
void shouldCreateUser() {
    // Given
    User user = User.builder()
        .name("test_user")
        .build();
    
    // When
    Mono<User> result = userRepository.save(user);
    
    // Then
    StepVerifier.create(result)
        .expectNextMatches(saved -> saved.getId() != null)
        .verifyComplete();
}
```

### 통합 테스트
- **TestContainers Redis**: 실제 Redis 환경에서 테스트
- **WebFlux Test**: WebTestClient를 통한 API 테스트
- **Reactive Test**: StepVerifier를 통한 반응형 스트림 테스트

## 🛡️ 보안 고려사항

### 데이터 보호
- **Redis AUTH**: Redis 서버 인증 설정
- **SSL/TLS**: Redis 연결 암호화
- **데이터 검증**: Bean Validation을 통한 입력 데이터 검증

### 설정 보안
- **Config Server 암호화**: 민감한 설정 정보 암호화
- **Vault 통합**: HashiCorp Vault를 통한 시크릿 관리

## 📚 학습 포인트

이 Account Service는 다음과 같은 현대적인 마이크로서비스 패턴들을 학습할 수 있습니다:

1. **반응형 프로그래밍**: WebFlux와 Reactor 패턴
2. **NoSQL 데이터베이스**: Redis를 활용한 빠른 데이터 액세스
3. **중앙집중식 설정**: Spring Cloud Config 패턴
4. **마이크로서비스 통신**: REST API 기반 서비스 간 통신
5. **반응형 테스트**: StepVerifier를 통한 비동기 테스트

Account Service는 사용자 관리의 핵심 기능을 제공하면서 최신 Spring 생태계의 반응형 프로그래밍 패러다임을 실습할 수 있는 완벽한 예제입니다.