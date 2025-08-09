# Chapter 14: JPA 고급과 Reactive 심화

Spring Boot에서 JPA 고급 기능과 Reactive 프로그래밍을 심화 학습하는 고급 모듈입니다. JPA Envers 감사 시스템, QueryDSL 동적 쿼리, MongoDB Reactive 고급 패턴, 그리고 하이브리드 아키텍처를 통한 실무적 기술 전환 과정을 구현합니다.

## 🎯 고급 학습 목표

- **JPA Envers**: 엔티티 변경 이력 추적과 감사 시스템 구현
- **QueryDSL Integration**: 타입 안전 동적 쿼리와 복합 검색 조건
- **Reactive Advanced**: 고급 Reactive 패턴과 비동기 처리 최적화
- **Hybrid Architecture**: 전통적 JPA와 Reactive의 효과적 결합
- **Multi-DataSource**: MariaDB, MongoDB, Redis 멀티 데이터소스 통합
- **Advanced Caching**: 다층 캐싱 전략과 성능 최적화
- **External API Integration**: Retrofit2를 통한 외부 API 연동

## 📁 프로젝트 구조

```
chap14/
├── src/main/java/com/genius/primavera/
│   ├── AdvancedJpaApplication.java
│   ├── application/                    # 비즈니스 로직 계층
│   │   ├── cache/                      # 캐싱 전략
│   │   │   ├── LocalCache.java         # Caffeine 로컬 캐시
│   │   │   └── RedisCache.java         # Redis 분산 캐시
│   │   ├── article/                    # 게시글 서비스 (JPA + Auditing)
│   │   ├── logging/                    # MongoDB Reactive 로깅
│   │   ├── post/                       # 포스팅 서비스
│   │   ├── storage/                    # 파일 저장소 서비스
│   │   └── user/                       # 사용자 관리 서비스
│   ├── domain/                         # 도메인 모델과 비즈니스 규칙
│   │   ├── converter/                  # JPA Attribute Converters
│   │   │   ├── EnumAttributeConverter.java       # 기반 Enum 컨버터
│   │   │   ├── UserStatusAttributeConverter.java # 사용자 상태 변환
│   │   │   ├── ArticleStatusAttributeConverter.java # 게시글 상태 변환
│   │   │   └── ConvertedEnumResolver.java        # Enum 변환 유틸
│   │   ├── model/                      # 도메인 모델들
│   │   │   ├── BaseEntity.java         # JPA Auditing 기반 엔티티
│   │   │   ├── article/                # 게시글 도메인 (Envers 적용)
│   │   │   ├── post/                   # 포스트 도메인
│   │   │   ├── user/                   # 사용자 도메인
│   │   │   └── kakao/                  # 카카오 API 연동 모델
│   │   └── repository/                 # 저장소 인터페이스
│   │       ├── UserRepository.java     # JPA Repository
│   │       ├── PrimaveraLogRepository.java # MongoDB Reactive Repository
│   │       ├── article/                # QueryDSL 지원 게시글 저장소
│   │       │   ├── ArticleRepository.java
│   │       │   ├── ArticleSupportRepository.java # QueryDSL 인터페이스
│   │       │   └── ArticleSupportRepositoryImpl.java # QueryDSL 구현
│   │       └── kakao/                  # 카카오 친구 저장소
│   ├── infrastructure/                 # 인프라스트럭처 계층
│   │   ├── ApplicationConfiguration.java # 메인 설정
│   │   ├── KakaoRetrofitClientConfiguration.java # Retrofit 설정
│   │   ├── aspect/                     # AOP 기반 로깅
│   │   ├── security/                   # OAuth2 + JWT 보안
│   │   ├── serializer/                 # Redis 직렬화 최적화
│   │   │   ├── KryoRedisSerializer.java # Kryo 직렬화
│   │   │   └── SnappyRedisSerializer.java # 압축 직렬화
│   │   └── filter/                     # 커스텀 필터
│   └── interfaces/                     # 웹 인터페이스 계층
│       ├── ArticleController.java      # 게시글 API
│       ├── ChartController.java        # Reactive 차트 API
│       ├── GreetingController.java     # Reactive 예제 API
│       └── UserController.java         # 사용자 API
└── src/main/resources/
    ├── application.yml                 # 기본 설정
    ├── application-local.yml           # 로컬 환경 설정
    ├── application-test.yml            # 테스트 환경 설정
    └── sql/                           # 데이터베이스 초기화 스크립트
```

## 🔧 고급 기술 기능

### 1. JPA Envers 감사 시스템
```java
@Entity
@Audited  // Envers 감사 활성화
@Table(name = "ARTICLE")
public class Article extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "SUBJECT", nullable = false)
    private String subject;
    
    @Convert(converter = ArticleStatusAttributeConverter.class)
    private ArticleStatus status;
    
    // 감사 정보는 BaseEntity에서 자동 관리
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(name = "REG_DT", updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "MOD_DT")
    private LocalDateTime lastModifiedDate;
    
    @CreatedBy
    @Column(name = "CREATED_BY", updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "MODIFIED_BY")
    private String lastModifiedBy;
}
```

### 2. QueryDSL 동적 쿼리
```java
@Repository
public class ArticleSupportRepositoryImpl implements ArticleSupportRepository {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<Article> findArticlesWithDynamicConditions(Search search) {
        QArticle article = QArticle.article;
        QUser user = QUser.user;
        
        return queryFactory
            .selectFrom(article)
            .join(article.author, user).fetchJoin()
            .where(
                titleContains(search.getKeyword()),
                statusEquals(search.getStatus()),
                dateAfter(search.getStartDate())
            )
            .orderBy(article.createdDate.desc())
            .limit(search.getLimit())
            .fetch();
    }
    
    private BooleanExpression titleContains(String keyword) {
        return keyword != null ? QArticle.article.subject.containsIgnoreCase(keyword) : null;
    }
    
    private BooleanExpression statusEquals(ArticleStatus status) {
        return status != null ? QArticle.article.status.eq(status) : null;
    }
}
```

### 3. Advanced Reactive Patterns
```java
@Service
public class ChartHandler {
    
    private final PrimaveraLogRepository logRepository;
    
    public Flux<ChartData> getRealtimeChartData() {
        return Flux.interval(Duration.ofSeconds(1))
            .flatMap(tick -> logRepository.findRecentStats())
            .map(this::convertToChartData)
            .onBackpressureBuffer(100)
            .retry(3)
            .doOnError(error -> log.error("Chart data streaming error", error));
    }
    
    public Mono<DashboardData> getDashboardData() {
        Mono<Long> totalUsers = userRepository.count();
        Mono<Long> totalArticles = articleRepository.count(); 
        Mono<List<PrimaveraLog>> recentLogs = logRepository.findTop10ByOrderByCreateDtDesc().collectList();
        
        return Mono.zip(totalUsers, totalArticles, recentLogs)
            .map(tuple -> DashboardData.builder()
                .totalUsers(tuple.getT1())
                .totalArticles(tuple.getT2())
                .recentLogs(tuple.getT3())
                .build());
    }
}
```

### 4. Multi-Level Caching Strategy
```java
@Service
public class LocalCache {
    
    private final Cache<String, Object> caffeine = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();
    
    @Cacheable(value = "articles", unless = "#result == null")
    public Article getArticle(Long id) {
        return caffeine.get("article:" + id, key -> {
            // L1 Cache Miss -> L2 Cache (Redis) 확인
            return redisCache.get(key).orElseGet(() -> {
                // L2 Cache Miss -> Database 조회
                Article article = articleRepository.findById(id).orElse(null);
                if (article != null) {
                    redisCache.put(key, article, Duration.ofMinutes(30));
                }
                return article;
            });
        });
    }
}
```

### 5. JPA Custom Attribute Converters
```java
@Converter
public class ArticleStatusAttributeConverter extends EnumAttributeConverter<ArticleStatus> {
    
    public ArticleStatusAttributeConverter() {
        super(ArticleStatus.class, false);
    }
}

public abstract class EnumAttributeConverter<E extends Enum<E> & ConvertedEnum> 
    implements AttributeConverter<E, String> {
    
    private final Class<E> enumClass;
    private final boolean nullable;
    
    @Override
    public String convertToDatabaseColumn(E attribute) {
        if (attribute == null) {
            return nullable ? null : getDefaultValue();
        }
        return attribute.getCode();
    }
    
    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null && nullable) {
            return null;
        }
        return ConvertedEnumResolver.fromCode(enumClass, dbData);
    }
}
```

### 6. Retrofit2 External API Integration
```java
@Configuration
public class KakaoRetrofitClientConfiguration {
    
    @Bean
    public KakaoApiService kakaoApiService() {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://kapi.kakao.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createOkHttpClient())
            .build();
            
        return retrofit.create(KakaoApiService.class);
    }
    
    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new AuthorizationInterceptor())
            .build();
    }
}

public interface KakaoApiService {
    
    @GET("v1/api/talk/friends")
    Call<KakaoFriendResponse> getFriends(
        @Header("Authorization") String token,
        @Query("offset") int offset,
        @Query("limit") int limit
    );
}
```

## 🛠️ 기술 스택

### Core Framework
- **Spring Boot**: 3.3.6
- **Spring Data JPA**: JPA Repository + QueryDSL
- **Spring WebFlux**: Reactive 컴포넌트
- **Hibernate Envers**: 엔티티 감사 시스템
- **QueryDSL**: 4.4.0 (타입 안전 쿼리)

### Database & Persistence
- **MariaDB**: 11.4.7 (주 데이터베이스 - JPA)
- **MongoDB**: 7.0 (로깅 - Reactive)
- **Redis**: 7.2 (캐싱 및 세션)
- **JPA Auditing**: 생성/수정 시간 자동 관리

### Advanced Features
- **Caffeine**: 로컬 메모리 캐싱
- **Kryo + Snappy**: 직렬화 및 압축 최적화
- **Retrofit2**: HTTP 클라이언트
- **jOOλ**: 함수형 프로그래밍 지원

### Testing & Development
- **TestContainers**: 통합 테스트 환경
- **Reactor Test**: Reactive 스트림 테스트
- **JUnit 5**: 단위 테스트 프레임워크

## 🚀 실행 방법

### 1. 인프라 환경 설정
```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# JPA + 캐싱 + 검색용 Docker Compose 실행
docker-compose -f docker-compose.jpa.yml up -d

# 서비스 상태 확인
docker-compose -f docker-compose.jpa.yml ps
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308) - 주 데이터베이스
- **Redis 7** (포트: 6380) - 캐싱 및 세션 저장소
- **Elasticsearch 8.12.0** (포트: 9200, 9300) - 검색 엔진

### 2. MongoDB 추가 설정
```bash
# MongoDB 실행 (로깅용)
docker run --name mongodb-reactive -d -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=primavera \
  -e MONGO_INITDB_ROOT_PASSWORD=primavera \
  mongo:7.0
```

### 3. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap14:bootRun -Dspring.profiles.active=local

# 또는 환경 변수 방식
SPRING_PROFILES_ACTIVE=local ./gradlew :chap14:bootRun
```

### 4. API 테스트
```bash
# QueryDSL 동적 검색 테스트
curl "http://localhost:8080/articles/search?keyword=spring&status=PUBLISHED&startDate=2024-01-01"

# Reactive 차트 스트림
curl -N http://localhost:8080/api/charts/stream

# JPA Envers 감사 이력 조회
curl http://localhost:8080/articles/1/history

# 카카오 친구 목록 (OAuth 토큰 필요)
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/kakao/friends

# 다층 캐싱 테스트
curl http://localhost:8080/articles/1  # DB 조회 후 캐시 저장
curl http://localhost:8080/articles/1  # 캐시에서 조회
```

## 🎓 핵심 고급 학습 포인트

### 1. JPA Advanced Features
- **Envers Auditing**: 엔티티 변경 이력 자동 추적
- **Custom Converters**: Enum을 데이터베이스 코드로 변환
- **Entity Listeners**: 생성/수정 시점 이벤트 처리
- **Batch Processing**: 대량 데이터 효율적 처리

### 2. QueryDSL Integration
- **Type-Safe Queries**: 컴파일 타임 쿼리 검증
- **Dynamic Conditions**: 런타임 조건 생성
- **Complex Joins**: 복합 연관관계 쿼리
- **Projection**: 필요한 필드만 선택적 조회

### 3. Reactive Advanced Patterns
- **Backpressure Handling**: 데이터 플로우 제어
- **Error Recovery**: 재시도 및 복구 전략
- **Parallel Processing**: 병렬 스트림 처리
- **Resource Management**: 비동기 리소스 관리

### 4. Hybrid Architecture Benefits
- **Selective Adoption**: 필요한 부분만 Reactive 적용
- **Performance Optimization**: 각 패러다임의 장점 활용
- **Migration Strategy**: 점진적 아키텍처 전환
- **Risk Mitigation**: 검증된 기술과 신기술의 조화

### 5. Multi-DataSource Management
- **Transaction Coordination**: 분산 트랜잭션 관리
- **Data Consistency**: 데이터 일관성 보장
- **Performance Tuning**: 각 데이터소스 최적화
- **Monitoring**: 멀티 데이터소스 모니터링

## 🧪 테스트 실행

### 단위 테스트
```bash
./gradlew :chap14:test
```

### 통합 테스트 (TestContainers)
```bash
./gradlew :chap14:test --tests="*IntegrationTest"
```

### JPA Envers 감사 테스트
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("JPA Envers 감사 시스템 테스트")
class ArticleEnversTest {

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
    }

    @Test
    @DisplayName("게시글 수정 시 감사 이력 생성 검증")
    void shouldCreateAuditHistoryOnArticleUpdate() {
        // Given: 게시글 생성
        Article article = Article.builder()
            .subject("Original Title")
            .content("Original Content")
            .build();
        Article saved = articleRepository.save(article);

        // When: 게시글 수정
        saved.setSubject("Updated Title");
        articleRepository.save(saved);

        // Then: 감사 이력 확인
        List<Number> revisions = auditReader.getRevisions(Article.class, saved.getId());
        assertThat(revisions).hasSize(2); // 생성 + 수정

        Article revision1 = auditReader.find(Article.class, saved.getId(), revisions.get(0));
        Article revision2 = auditReader.find(Article.class, saved.getId(), revisions.get(1));

        assertThat(revision1.getSubject()).isEqualTo("Original Title");
        assertThat(revision2.getSubject()).isEqualTo("Updated Title");
    }
}
```

### QueryDSL 동적 쿼리 테스트
```java
@Test
@DisplayName("QueryDSL 복합 조건 검색 테스트")
void shouldFindArticlesWithMultipleConditions() {
    // Given: 테스트 데이터 생성
    createTestArticles();

    // When: 복합 조건 검색
    Search search = Search.builder()
        .keyword("Spring")
        .status(ArticleStatus.PUBLISHED)
        .startDate(LocalDate.now().minusDays(7))
        .limit(10)
        .build();

    List<Article> results = articleSupportRepository.findArticlesWithDynamicConditions(search);

    // Then: 결과 검증
    assertThat(results).isNotEmpty();
    assertThat(results).allMatch(article -> 
        article.getSubject().contains("Spring") && 
        article.getStatus() == ArticleStatus.PUBLISHED
    );
}
```

### Multi-Level Cache 테스트
```java
@Test
@DisplayName("멀티레벨 캐시 체인 동작 검증")
void shouldWorkWithMultiLevelCacheChain() {
    Long articleId = 1L;

    // First access: Database -> L2 Cache -> L1 Cache
    Article first = cacheService.getArticle(articleId);
    assertThat(first).isNotNull();

    // Second access: L1 Cache hit
    Article second = cacheService.getArticle(articleId);
    assertThat(second).isSameAs(first);

    // Verify cache statistics
    CacheStats stats = caffeine.stats();
    assertThat(stats.hitRate()).isGreaterThan(0.0);
}
```

## 📚 학습 순서

1. **JPA Envers 감사 시스템**
   - @Audited 애너테이션 활용
   - 커스텀 감사 정보 설정
   - 이력 조회 및 분석

2. **QueryDSL 고급 활용**
   - Q 클래스 생성 및 활용
   - 동적 쿼리 작성 패턴
   - 복잡한 조인과 서브쿼리

3. **JPA Custom Converters**
   - Enum 변환 최적화
   - JSON 데이터 매핑
   - 커스텀 타입 처리

4. **Advanced Reactive Patterns**
   - 에러 복구 전략
   - 병렬 처리 최적화
   - 리소스 관리

5. **Multi-DataSource Integration**
   - 트랜잭션 경계 관리
   - 데이터 일관성 보장
   - 성능 모니터링

6. **Caching Strategy**
   - 다층 캐시 설계
   - TTL 및 무효화 정책
   - 성능 측정

## 🔧 주요 고급 애너테이션

| 애너테이션 | 용도 | 사용 예시 |
|-----------|------|----------|
| `@Audited` | JPA Envers 감사 활성화 | 엔티티 클래스 |
| `@Convert(converter = CustomConverter.class)` | 커스텀 컨버터 적용 | 엔티티 필드 |
| `@Query(nativeQuery = true)` | 네이티브 쿼리 실행 | Repository 메서드 |
| `@EntityListeners(AuditingEntityListener.class)` | 감사 이벤트 리스너 | 엔티티 클래스 |
| `@CreatedDate`, `@LastModifiedDate` | 자동 시간 관리 | 감사 필드 |
| `@Modifying` | 수정 쿼리 표시 | Repository 메서드 |

## 🔄 다음 단계

**Chapter 15 (JPA 관계 매핑 심화)**로 진행하여 다음 내용을 학습하세요:

- 복잡한 엔티티 관계 매핑 (1:1, 1:N, N:M)
- 상속 관계 매핑과 다형성 처리
- 임베디드 타입과 값 객체 설계
- 고급 성능 최적화 기법
- 도메인 주도 설계 패턴 적용

---

이 모듈을 통해 JPA의 고급 기능을 마스터하고, Reactive 프로그래밍의 심화된 패턴을 익힐 수 있습니다. 특히 실무에서 자주 마주치는 하이브리드 아키텍처와 멀티 데이터소스 환경에서의 개발 경험을 쌓을 수 있습니다.