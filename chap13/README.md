# Chapter 13: 고급 인가와 Reactive 기초

Spring Boot에서 Reactive 프로그래밍과 고급 인가 시스템을 학습하는 고급 모듈입니다. WebFlux를 통한 비동기 처리, MongoDB Reactive, Redis Reactive, 그리고 OAuth2 기반의 소셜 로그인을 통합한 고급 인가 시스템을 구현합니다.

## 🎯 고급 학습 목표

- **Reactive Programming**: WebFlux를 활용한 비동기 논블로킹 애플리케이션 개발
- **MongoDB Reactive**: 비동기 NoSQL 데이터 처리와 로깅 시스템 구현
- **Redis Reactive**: 비동기 캐싱과 세션 관리
- **OAuth2 Integration**: 소셜 로그인과 고급 인가 시스템
- **Chart & Visualization**: Reactive 데이터를 활용한 실시간 시각화
- **File Storage**: 비동기 파일 업로드/다운로드 시스템

## 📁 프로젝트 구조

```
chap13/
├── src/main/java/com/genius/primavera/
│   ├── AdvancedAuthorizationApplication.java
│   ├── application/                    # 비즈니스 로직 계층
│   │   ├── AttachmentService.java      # 파일 첨부 서비스
│   │   ├── ChartService.java           # 차트 데이터 서비스 (Reactive)
│   │   ├── logging/                    # MongoDB 로깅 시스템
│   │   ├── storage/                    # 파일 저장소 서비스
│   │   ├── article/                    # 게시글 관리 서비스
│   │   └── user/                       # 사용자 관리 서비스
│   ├── domain/                         # 도메인 모델과 비즈니스 규칙
│   │   ├── mapper/                     # MyBatis 매퍼 인터페이스
│   │   ├── model/                      # 도메인 모델들
│   │   │   ├── article/                # 게시글 관련 모델
│   │   │   ├── post/                   # 포스트 관련 모델
│   │   │   └── user/                   # 사용자 관련 모델
│   │   ├── repository/                 # MongoDB Repository (Reactive)
│   │   └── typehandler/                # MyBatis 타입 핸들러
│   ├── infrastructure/                 # 인프라스트럭처 계층
│   │   ├── aspect/                     # AOP 기반 로깅
│   │   ├── security/                   # OAuth2 보안 설정
│   │   └── filter/                     # 요청 필터링
│   └── interfaces/                     # 웹 인터페이스 계층
│       ├── ArticleController.java      # 게시글 API (Reactive)
│       ├── ChartController.java        # 차트 API (WebFlux)
│       └── UserController.java         # 사용자 API
└── src/main/resources/
    ├── application.yml                 # Reactive 설정
    ├── application-local.yml           # 로컬 개발 환경
    ├── mapper/                         # MyBatis SQL 매퍼
    └── sql/                           # 데이터베이스 초기화 스크립트
```

## 🔧 고급 기술 기능

### 1. Reactive Programming (WebFlux)
```java
@RestController
@RequestMapping("/api/reactive")
public class ChartController {
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChartData> streamChartData() {
        return chartService.getRealtimeData()
            .delayElements(Duration.ofSeconds(1))
            .doOnNext(data -> log.info("Streaming data: {}", data));
    }
    
    @PostMapping("/articles")
    public Mono<ResponseEntity<Article>> createArticle(@RequestBody ArticleDto dto) {
        return articleService.createArticle(dto)
            .map(article -> ResponseEntity.ok(article))
            .onErrorResume(error -> 
                Mono.just(ResponseEntity.badRequest().build()));
    }
}
```

### 2. MongoDB Reactive Integration
```java
@Repository
public interface PrimaveraLogRepository extends ReactiveMongoRepository<PrimaveraLog, String> {
    
    Flux<PrimaveraLog> findByUserIdAndCreatedAtBetween(
        String userId, LocalDateTime start, LocalDateTime end);
    
    Mono<Long> countByActionAndCreatedAtGreaterThan(
        String action, LocalDateTime timestamp);
}
```

### 3. Redis Reactive Caching
```java
@Service
public class ChartServiceImpl implements ChartService {
    
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    public Flux<ChartData> getCachedChartData(String key) {
        return redisTemplate.opsForList()
            .range(key, 0, -1)
            .cast(ChartData.class)
            .switchIfEmpty(loadAndCacheData(key));
    }
}
```

### 4. AOP 기반 시스템 로깅
```java
@Slf4j
@Aspect
@Component
public class PrimaveraLoggingAspect {

    @Autowired
    private PrimaveraLogService primaveraLogService;

    @Autowired
    private MongoSequenceGeneratorService mongoSequenceGeneratorService;

    @Before(value = "@annotation(primaveraLogging)", argNames = "joinPoint, primaveraLogging")
    public void preLogging(JoinPoint joinPoint, PrimaveraLogging primaveraLogging) {
        PrimaveraLog primaveraLog = PrimaveraLog.builder()
                .id(mongoSequenceGeneratorService.generateSequence(PrimaveraLog.SEQUENCE_NAME))
                .type(primaveraLogging.type())
                .kind(joinPoint.getKind())
                .target(joinPoint.getTarget())
                .createDt(Instant.now())
                .build();
        primaveraLogService.save(primaveraLog);
    }
}
```

### 5. OAuth2 소셜 로그인
```java
@Configuration
@EnableWebFluxSecurity
public class PrimaveraSecurityConfiguration {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .oauth2Login(oauth2 -> oauth2
                .authenticationSuccessHandler(successHandler())
                .userInfoEndpoint(userInfo -> 
                    userInfo.userService(socialUserDetailsService())))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .pathMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .anyExchange().permitAll())
            .build();
    }
}
```

## 🛠️ 기술 스택

### Core Framework
- **Spring Boot**: 3.3.6
- **Spring WebFlux**: Reactive Web Framework
- **Spring Data JPA**: 관계형 데이터 접근
- **Spring Data MongoDB Reactive**: 비동기 NoSQL 처리
- **Spring Data Redis Reactive**: 비동기 캐싱

### Database & Persistence
- **MariaDB**: 11.4.7 (관계형 데이터)
- **MongoDB**: 비동기 로깅 및 분석 데이터
- **Redis**: 비동기 캐싱 및 세션 관리
- **MyBatis**: SQL 매핑 프레임워크

### Security & Authentication
- **Spring Security**: 6.4.4
- **OAuth2 Client**: 소셜 로그인 연동
- **Thymeleaf Security**: 템플릿 보안 통합

### Testing & Development
- **Reactor Test**: Reactive 스트림 테스트
- **TestContainers**: 통합 테스트 환경
- **JUnit 5**: 단위 테스트 프레임워크

## 🚀 실행 방법

### 1. 인프라 환경 설정
```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# 게시판 + 중앙 설정 관리용 Docker Compose 실행 (MariaDB + Vault)
docker-compose -f docker-compose.board.yml up -d

# 서비스 상태 확인
docker-compose -f docker-compose.board.yml ps

# Vault 초기화 확인
docker logs vault-init-primavera-board
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308)
- **HashiCorp Vault 1.15** (포트: 8200) - 중앙집중식 설정 관리
- 게시판 전용 데이터베이스 스키마 자동 생성

### 2. MongoDB & Redis 설정
```bash
# MongoDB 실행
docker run --name mongo -d -p 27017-27019:27017-27019 \
  -e MONGO_INITDB_ROOT_USERNAME=primavera \
  -e MONGO_INITDB_ROOT_PASSWORD=primavera mongo

# Redis 실행
docker run -d --name redis-reactive -p 6379:6379 redis:alpine
```

### 3. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap13:bootRun -Dspring.profiles.active=local

# 또는 환경 변수 방식
SPRING_PROFILES_ACTIVE=local ./gradlew :chap13:bootRun
```

### 4. Reactive API 테스트
```bash
# Server-Sent Events 스트림 테스트
curl -N http://localhost:8080/api/reactive/stream

# 비동기 게시글 생성
curl -X POST http://localhost:8080/api/reactive/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"Reactive Article","content":"WebFlux Content"}'

# MongoDB 로그 조회
curl http://localhost:8080/api/logs/recent

# 차트 데이터 조회 (캐시 적용)
curl http://localhost:8080/api/charts/dashboard
```

## 🎓 핵심 고급 학습 포인트

### 1. Reactive Programming Patterns
- **Mono vs Flux**: 단일 값과 스트림 처리의 차이점 이해
- **Backpressure**: 데이터 흐름 제어와 메모리 관리
- **Error Handling**: onErrorResume, onErrorReturn 등 리액티브 에러 처리
- **Threading Model**: 이벤트 루프 기반 비동기 처리 모델

### 2. MongoDB Reactive Operations
- **Reactive Queries**: 비동기 쿼리 실행과 결과 스트리밍
- **Aggregation Pipeline**: 복잡한 데이터 집계를 Reactive로 처리
- **Change Streams**: MongoDB 변경 사항 실시간 감지

### 3. AOP 횡단 관심사
- **@PrimaveraLogging**: 커스텀 애너테이션 기반 로깅
- **포인트컷 표현식**: 메서드 패턴 매칭과 어드바이스 적용
- **크로스커팅**: 비즈니스 로직과 분리된 시스템 로깅

### 4. Security Integration
- **Reactive Security**: WebFlux 환경에서의 보안 설정
- **JWT + OAuth2**: 토큰 기반 인증과 소셜 로그인 연동
- **Method Security**: @PreAuthorize를 활용한 메서드 레벨 보안

## 🧪 테스트 실행

### 단위 테스트
```bash
./gradlew :chap13:test
```

### 통합 테스트 (TestContainers)
```bash
./gradlew :chap13:test --tests="*IntegrationTest"
```

### AOP 로깅 시스템 테스트
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("AOP 로깅 시스템 통합 테스트")
class WriteArticleServiceTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

    @Test
    @DisplayName("게시글 작성 시 AOP 로깅 동작 검증")
    void articleCreationWithLogging() {
        Article article = Article.builder()
            .title("AOP 테스트 게시글")
            .content("시스템 로그 저장 테스트")
            .build();
            
        Article saved = writeArticleService.save(article);
        
        // AOP Aspect가 실행되어 시스템 로그가 저장되었는지 검증
        assertThat(saved.getId()).isNotNull();
        // 추가로 로그 테이블에서 로그 엔트리 확인
    }
}
```

### Reactive 스트림 테스트
```java
@Test
void shouldStreamChartDataReactively() {
    StepVerifier.create(chartService.getRealtimeData())
        .expectNextCount(5)
        .expectComplete()
        .verify(Duration.ofSeconds(10));
}
```

## 📚 학습 순서

1. **Reactive 기초 이해**
   - WebFlux vs Spring MVC 차이점
   - Mono, Flux 기본 개념
   - Reactive Streams 스펙

2. **MongoDB Reactive 연동**
   - ReactiveMongoRepository 사용법
   - 비동기 CRUD 연산
   - Aggregation 파이프라인

3. **AOP 시스템 로깅**
   - @PrimaveraLogging 커스텀 애너테이션
   - Aspect 기반 횡단 관심사 처리
   - MongoDB를 활용한 로그 저장

4. **Redis Reactive 캐싱**
   - ReactiveRedisTemplate 활용
   - 비동기 캐시 전략
   - 세션 관리

5. **보안 시스템 구축**
   - OAuth2 소셜 로그인
   - JWT 토큰 관리
   - 역할 기반 접근 제어

6. **실시간 기능 구현**
   - Server-Sent Events
   - WebSocket 대안
   - 실시간 차트 데이터

## 🔧 주요 고급 애너테이션

| 애너테이션 | 용도 | 사용 예시 |
|-----------|------|----------|
| `@EnableWebFluxSecurity` | WebFlux 보안 활성화 | 보안 설정 클래스 |
| `@EnableReactiveMongoRepositories` | Reactive MongoDB 활성화 | 설정 클래스 |
| `@PrimaveraLogging` | 커스텀 로깅 AOP | 서비스 메서드 |
| `@GetMapping(produces = TEXT_EVENT_STREAM_VALUE)` | SSE 엔드포인트 | 스트리밍 API |
| `@Cacheable` | 비동기 캐싱 | 서비스 메서드 |

## 🗃️ 데이터베이스 스키마

### 관계형 데이터 (MariaDB)
```sql
CREATE TABLE IF NOT EXISTS ARTICLE (
    ID BIGINT(20) NOT NULL AUTO_INCREMENT,
    P_ID BIGINT(20) NOT NULL DEFAULT 0,
    REFERENCE BIGINT(20) NOT NULL,
    STEP INT(11) NOT NULL,
    LEVEL INT(11) NOT NULL,
    AUTHOR BIGINT(20) NOT NULL,
    SUBJECT VARCHAR(200) NOT NULL,
    STATUS TINYINT(3) NOT NULL,
    HIT BIGINT(20) NOT NULL DEFAULT 0,
    RECOMMEND BIGINT(20) NOT NULL DEFAULT 0,
    DISAPPROVE BIGINT(20) NOT NULL DEFAULT 0,
    REG_DT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    MOD_DT TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (ID),
    KEY FK_WRITER_ID_IDX (AUTHOR),
    CONSTRAINT FK_ARTICLE_AUTHOR_ID FOREIGN KEY (AUTHOR) REFERENCES USERS (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
```

### NoSQL 데이터 (MongoDB)
```javascript
// PrimaveraLog Collection
{
  "_id": ObjectId("..."),
  "sequenceId": NumberLong(1),
  "type": "ARTICLE_CREATE",
  "kind": "method-execution",
  "target": "WriteArticleServiceImpl",
  "createDt": ISODate("2024-03-15T10:30:00Z"),
  "userId": "user123",
  "action": "CREATE_ARTICLE"
}
```

## 🔄 다음 단계

**Chapter 14 (JPA 고급, Reactive 심화)**로 진행하여 다음 내용을 학습하세요:

- JPA Envers를 통한 엔티티 감사 시스템
- QueryDSL을 활용한 동적 쿼리 작성
- 고급 Reactive 패턴 (병렬 처리, 에러 복구)
- 복잡한 비동기 비즈니스 로직 구현
- 성능 모니터링과 메트릭 수집

---

이 모듈을 통해 Reactive 프로그래밍의 기초를 다지고, AOP를 활용한 횡단 관심사 처리, 그리고 실제 운영 환경에서 사용할 수 있는 비동기 애플리케이션 개발 역량을 기를 수 있습니다.