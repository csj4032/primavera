# Chapter 18 Account - 계정 관리 마이크로서비스

Spring Boot 교육용 프로젝트 Primavera의 Chapter 18 Account 모듈입니다. Spring WebFlux를 사용한 리액티브 계정 관리 마이크로서비스로, JWT 토큰 기반 인증과 Redis를 활용한 세션 관리를 학습합니다.

## 🎯 학습 목표

- **리액티브 마이크로서비스**: WebFlux를 사용한 비동기 서비스 구현
- **JWT 토큰 인증**: 마이크로서비스 간 안전한 인증 메커니즘
- **Redis 세션 관리**: 분산 환경에서의 세션 상태 관리
- **Spring Cloud Config**: 중앙화된 설정 관리 활용
- **API Gateway 연동**: 외부 요청의 진입점 역할

## 📁 프로젝트 구조

```
chap18/account/
├── src/main/java/com/genius/primavera/
│   ├── AccountApplication.java          # 메인 애플리케이션
│   ├── User.java                       # 사용자 엔티티
│   ├── UserController.java             # REST 컨트롤러
│   ├── UserRepository.java             # JPA 리포지터리
│   ├── AccountHandler.java             # WebFlux 핸들러
│   ├── AccountRouter.java              # 라우터 설정
│   ├── AccountRepository.java          # 계정 리포지터리
│   ├── RedisConfiguration.java         # Redis 설정
│   └── DataInitializer.java           # 초기 데이터 설정
├── src/main/resources/
│   └── application.yaml                # 애플리케이션 설정
└── build.gradle                        # WebFlux + JPA 의존성
```

## 🏗 아키텍처 특성

### 1. 리액티브 웹 스택
```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<User>> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/users")
    public Mono<User> createUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}
```

### 2. 함수형 라우팅
```java
@Configuration
public class AccountRouter {
    
    @Bean
    public RouterFunction<ServerResponse> routes(AccountHandler handler) {
        return RouterFunctions
            .route(GET("/api/accounts/{id}"), handler::getAccount)
            .andRoute(POST("/api/accounts"), handler::createAccount)
            .andRoute(PUT("/api/accounts/{id}"), handler::updateAccount)
            .andRoute(DELETE("/api/accounts/{id}"), handler::deleteAccount);
    }
}
```

### 3. Redis 세션 관리
```java
@Configuration
public class RedisConfiguration {
    
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializer<String> stringSerializer = RedisSerializer.string();
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer();
            
        RedisSerializationContext<String, Object> context = 
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(stringSerializer)
                .value(jsonSerializer)
                .build();
                
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
```

## 🎯 핵심 기능

### 1. 사용자 관리
```java
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### 2. JWT 토큰 기반 인증
```java
@Component
public class JwtTokenProvider {
    
    public Mono<String> generateToken(User user) {
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.claims().setSubject(user.getUsername());
            claims.put("role", user.getRole().name());
            
            return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
        });
    }
    
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
                return true;
            } catch (JwtException | IllegalArgumentException e) {
                return false;
            }
        });
    }
}
```

### 3. 리액티브 세션 관리
```java
@Service
public class SessionService {
    
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    public Mono<Void> storeSession(String sessionId, UserSession session) {
        return redisTemplate.opsForValue()
            .set("session:" + sessionId, session, Duration.ofHours(24))
            .then();
    }
    
    public Mono<UserSession> getSession(String sessionId) {
        return redisTemplate.opsForValue()
            .get("session:" + sessionId)
            .cast(UserSession.class);
    }
    
    public Mono<Boolean> invalidateSession(String sessionId) {
        return redisTemplate.delete("session:" + sessionId)
            .map(count -> count > 0);
    }
}
```

### 4. 비즈니스 로직 처리
```java
@Component
public class AccountHandler {
    
    public Mono<ServerResponse> createAccount(ServerRequest request) {
        return request.bodyToMono(CreateAccountRequest.class)
            .flatMap(this::validateAccountRequest)
            .flatMap(accountService::createAccount)
            .flatMap(account -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(account))
            .onErrorResume(this::handleError);
    }
    
    private Mono<ServerResponse> handleError(Throwable error) {
        if (error instanceof ValidationException) {
            return ServerResponse.badRequest()
                .bodyValue(ErrorResponse.of("VALIDATION_ERROR", error.getMessage()));
        }
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .bodyValue(ErrorResponse.of("INTERNAL_ERROR", "서버 오류가 발생했습니다"));
    }
}
```

## 🛠 기술 스택

### 핵심 기술
- **Java**: 21
- **Spring Boot**: 3.3.6
- **Spring WebFlux**: 리액티브 웹 프레임워크
- **Spring Data JPA**: 데이터 액세스
- **Spring Cloud Config**: 설정 관리

### 데이터 저장
- **MariaDB**: 관계형 데이터베이스
- **Redis**: 세션 및 캐시 저장소

### 인증 및 보안
- **JWT**: JSON Web Token
- **Spring Security**: 보안 프레임워크
- **BCrypt**: 패스워드 암호화

## 🚀 실행 방법

### 1. 의존 서비스 시작
```bash
# Config Server 시작
./gradlew :chap18:configuration:bootRun

# Redis 서버 시작 (Docker)
docker run -d --name redis-account \
  -p 6379:6379 \
  redis:7-alpine

# MariaDB 시작
./docker-manager.sh start chap18
```

### 2. Account 서비스 시작
```bash
# Account 마이크로서비스 시작
./gradlew :chap18:account:bootRun

# 특정 프로파일로 실행
./gradlew :chap18:account:bootRun -Dspring.profiles.active=local
```

### 3. API 테스트
```bash
# 사용자 등록
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'

# 로그인 (JWT 토큰 발급)
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# 사용자 정보 조회 (JWT 토큰 필요)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/users/1
```

## 📋 테스트 실행

### 리액티브 테스트
```bash
# 전체 테스트 실행
./gradlew :chap18:account:test

# WebFlux 컨트롤러 테스트
./gradlew :chap18:account:test --tests "*ControllerTest"

# 리액티브 레포지터리 테스트
./gradlew :chap18:account:test --tests "*RepositoryTest"
```

### 통합 테스트 예시
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.profiles.active=test")
class UserControllerIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldCreateUser() {
        CreateUserRequest request = CreateUserRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .build();
            
        webTestClient.post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(User.class)
            .value(user -> {
                assertThat(user.getUsername()).isEqualTo("testuser");
                assertThat(user.getId()).isNotNull();
            });
    }
}
```

## 🎓 핵심 학습 포인트

### 1. 리액티브 프로그래밍 패턴
```java
// 체인 방식의 리액티브 파이프라인
public Mono<User> createUserWithValidation(CreateUserRequest request) {
    return validateUser(request)
        .flatMap(this::checkDuplicateUser)
        .flatMap(this::encodePassword)
        .flatMap(userRepository::save)
        .doOnSuccess(user -> log.info("User created: {}", user.getId()))
        .doOnError(error -> log.error("Failed to create user", error));
}
```

### 2. 에러 처리 전략
```java
public Mono<ServerResponse> handleUserCreation(ServerRequest request) {
    return request.bodyToMono(CreateUserRequest.class)
        .flatMap(userService::createUser)
        .flatMap(user -> ServerResponse.created(URI.create("/users/" + user.getId()))
            .bodyValue(user))
        .onErrorResume(DuplicateUserException.class, 
            error -> ServerResponse.status(HttpStatus.CONFLICT)
                .bodyValue(ErrorResponse.of("DUPLICATE_USER", error.getMessage())))
        .onErrorResume(ValidationException.class,
            error -> ServerResponse.badRequest()
                .bodyValue(ErrorResponse.of("VALIDATION_ERROR", error.getMessage())));
}
```

### 3. 마이크로서비스 통신
```java
@Component
public class UserServiceClient {
    
    private final WebClient webClient;
    
    public Mono<User> getUserById(Long id) {
        return webClient.get()
            .uri("/users/{id}", id)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError,
                response -> Mono.error(new UserNotFoundException("User not found: " + id)))
            .bodyToMono(User.class)
            .timeout(Duration.ofSeconds(5))
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)));
    }
}
```

### 4. 설정 외부화
```yaml
# Config Server에서 관리되는 설정
jwt:
  secret: ${JWT_SECRET:default-secret-key}
  expiration: ${JWT_EXPIRATION:86400000}  # 24시간

redis:
  host: ${REDIS_HOST:localhost}
  port: ${REDIS_PORT:6379}
  timeout: ${REDIS_TIMEOUT:2000}

spring:
  datasource:
    url: ${DB_URL:jdbc:mariadb://localhost:3308/primavera}
    username: ${DB_USERNAME:primavera}
    password: ${DB_PASSWORD:primavera}
```

## 📚 주요 애너테이션

### WebFlux 관련
- `@RestController`: 리액티브 REST 컨트롤러
- `@RequestBody`: 요청 바디를 Mono/Flux로 변환
- `@PathVariable`: 경로 변수 바인딩

### 함수형 라우팅
- `@Bean RouterFunction`: 함수형 라우팅 설정
- `@Component Handler`: 핸들러 클래스

### 리액티브 테스트
- `@WebFluxTest`: WebFlux 계층 테스트
- `WebTestClient`: 리액티브 웹 클라이언트 테스트

## 🔧 운영 및 모니터링

### 1. 애플리케이션 메트릭
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. 분산 추적
```java
@Component
public class TracingConfiguration {
    
    @Bean
    public Tracer tracer() {
        return GlobalTracer.get();
    }
}
```

## 🔄 다음 단계

1. **chap18:product** - 상품 관리 마이크로서비스
2. **chap18:order** - 주문 처리 및 이벤트 기반 아키텍처
3. **chap18:front** - API Gateway 및 서비스 오케스트레이션
4. **서비스 메시** - Istio를 활용한 마이크로서비스 메시

## 📖 관련 문서

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [JWT Introduction](https://jwt.io/introduction)
- [Redis Documentation](https://redis.io/documentation)
- [Microservices Patterns](https://microservices.io/patterns/)