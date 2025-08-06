# Chapter 02: 설정과 의존성 관리

## 📋 개요
Spring Boot의 설정 시스템과 의존성 주입(Dependency Injection) 메커니즘을 마스터하는 챕터입니다. `@ConfigurationProperties`를 통한 타입 안전한 설정 관리와 다양한 Bean Scope, 생성자 기반 의존성 주입의 모든 것을 학습합니다.

## 🎯 학습 목표
- **@ConfigurationProperties**를 통한 타입 안전한 설정 관리
- **Bean Scope와 라이프사이클** 완전 이해
- **생성자 기반 의존성 주입** 마스터
- **프로파일별 환경 설정** 전략 구축
- **YAML vs Properties** 차이점과 활용법

## 🛠️ 핵심 기술 스택
- **Spring Boot 3.3.6** - Configuration Management
- **Spring Context** - IoC Container & DI
- **YAML Configuration** - 계층적 설정 관리
- **Lombok** - 코드 간소화 도구
- **JUnit 5** - 현대적 테스트 프레임워크

## 📚 주요 학습 내용

### 1. 순환 의존성 문제와 해결 🔄
- **BooService ↔ FooService** 실제 순환 참조 분석
- **생성자 주입 vs 필드 주입** 순환 참조 차이점
- **@Lazy, ApplicationContextAware** 등 다양한 해결 방법
- **구조 개선을 통한 근본적 해결** 전략

### 2. Spring Boot 환경 설정

#### 개발 환경 구축
```bash
# JDK 21 설치 확인
java -version

# Gradle 프로젝트 초기화
spring init --build=gradle --java-version=21 \
    --dependencies=web,configuration-processor \
    --groupId=com.genius.primavera primavera

# Gradle Wrapper 업데이트
./gradlew wrapper --gradle-version 8.12.1
```

#### Gradle 설정 (build.gradle)
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.6'
    id 'io.spring.dependency-management' version '1.1.6'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
}
```

### 2. YAML 기반 설정 관리

#### application.yml 구조화
```yaml
# 기본 프로파일 설정
spring:
  profiles:
    default: local
  application:
    name: Primavera
  banner:
    charset: UTF-8
    location: classpath:primavera.txt

# 로깅 설정
logging:
  level:
    org.springframework: info
    com.genius.primavera: debug
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# 커스텀 설정
com:
  genius:
    primavera:
      database:
        username: primavera
        password: primavera
        url: jdbc:mysql://localhost:3306/primavera
        tables: [user, role, article]
      search:
        params:
          keyword: genius
          page: 1
          sort: desc
      users:
        - id: 1
          email: genius@primavera.com
        - id: 2
          email: admin@primavera.com

---
# 개발 환경 설정
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mysql://localhost:3306/primavera_dev
    username: dev_user
    password: dev_pass
    
logging:
  level:
    org.springframework.web: debug
    org.hibernate.SQL: debug

---
# 운영 환경 설정
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mysql://prod-db:3306/primavera
    username: ${DB_USERNAME:prod_user}
    password: ${DB_PASSWORD:prod_pass}
    
logging:
  level:
    org.springframework: warn
    com.genius.primavera: info
```

### 3. @ConfigurationProperties 타입 안전한 설정

#### 설정 클래스 정의
```java
@Data
@Component
@ConfigurationProperties(prefix = "com.genius.primavera")
public class PrimaveraProperties {
    
    private Database database = new Database();
    private Search search = new Search();
    private List<User> users = new ArrayList<>();
    
    @Data
    public static class Database {
        private String username;
        private String password;
        private String url;
        private List<String> tables = new ArrayList<>();
    }
    
    @Data
    public static class Search {
        private Params params = new Params();
        
        @Data
        public static class Params {
            private String keyword;
            private Integer page = 1;
            private String sort = "asc";
        }
    }
    
    @Data
    public static class User {
        private Long id;
        private String email;
    }
}
```

#### 설정 검증 (Validation)
```java
@Data
@Component
@ConfigurationProperties(prefix = "com.genius.primavera.database")
@Validated
public class DatabaseProperties {
    
    @NotBlank(message = "데이터베이스 사용자명은 필수입니다")
    private String username;
    
    @NotBlank(message = "데이터베이스 비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;
    
    @NotBlank(message = "데이터베이스 URL은 필수입니다")
    @Pattern(regexp = "^jdbc:.*", message = "올바른 JDBC URL 형식이 아닙니다")
    private String url;
    
    @NotEmpty(message = "최소 하나의 테이블 설정이 필요합니다")
    private List<String> tables = new ArrayList<>();
}
```

### 4. Bean 스코프와 라이프사이클

#### Bean Scope 정의
```java
@Configuration
public class BeanScopeConfiguration {
    
    // 싱글톤 스코프 (기본값)
    @Bean
    @Scope("singleton")
    public DatabaseService singletonService() {
        return new DatabaseService();
    }
    
    // 프로토타입 스코프
    @Bean
    @Scope("prototype")
    public RequestHandler prototypeHandler() {
        return new RequestHandler();
    }
    
    // 웹 스코프 (Request)
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, 
           proxyMode = ScopedProxyMode.TARGET_CLASS)
    public UserContext requestScopedContext() {
        return new UserContext();
    }
    
    // 웹 스코프 (Session)
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION,
           proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ShoppingCart sessionScopedCart() {
        return new ShoppingCart();
    }
}
```

#### Bean 라이프사이클 관리
```java
@Component
public class LifecycleBean implements InitializingBean, DisposableBean {
    
    private static final Logger log = LoggerFactory.getLogger(LifecycleBean.class);
    
    // 의존성 주입 완료 후 초기화
    @PostConstruct
    public void postConstruct() {
        log.info("@PostConstruct: 빈 초기화 시작");
    }
    
    // InitializingBean 인터페이스 구현
    @Override
    public void afterPropertiesSet() {
        log.info("afterPropertiesSet: 모든 프로퍼티 설정 완료");
    }
    
    // 커스텀 초기화 메서드
    @Bean(initMethod = "customInit")
    public void customInit() {
        log.info("customInit: 커스텀 초기화 메서드 실행");
    }
    
    // 빈 소멸 전 정리 작업
    @PreDestroy
    public void preDestroy() {
        log.info("@PreDestroy: 빈 소멸 준비");
    }
    
    // DisposableBean 인터페이스 구현
    @Override
    public void destroy() {
        log.info("destroy: 빈 소멸 처리");
    }
}
```

### 5. 의존성 주입 패턴

#### 생성자 기반 의존성 주입 (권장)
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // 생성자가 하나인 경우 @Autowired 생략 가능
    // Lombok의 @RequiredArgsConstructor가 생성자 자동 생성
    
    public User createUser(UserCreateRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .build();
        
        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(savedUser.getEmail());
        
        return savedUser;
    }
}
```

#### 순환 의존성 해결

##### 🔄 실제 순환 참조 예제: BooService ↔ FooService

프로젝트의 `com.genius.primavera.application.injection` 패키지에는 실제 순환 의존성 문제를 보여주는 예제가 있습니다:

```java
// FooServiceImpl.java - 정상 동작 (BooService에 의존)
@Component
@RequiredArgsConstructor
public class FooServiceImpl implements FooService {
    
    private final BooService booService;  // BooService 의존성
    
    @Override
    public String foo() {
        booService.boo();  // BooService 호출
        return "foo";
    }
}

// BooServiceImpl.java - 순환 참조 문제 (현재 주석 처리됨)
@Component
@RequiredArgsConstructor
public class BooServiceImpl implements BooService {
    
    // ❌ 생성자 순환참조 경고 발생
    // "Requested bean is currently in creation: Is there an unresolvable circular reference?"
    // private final FooService fooService;
    
    // ❌ 필드 주입시 StackOverflowError 발생
    // @Autowired
    // private FooService fooService;
    
    @Override
    public String boo() {
        // fooService.foo();  // 호출하면 무한 루프
        return "boo";
    }
}
```

##### 🚨 순환 의존성 문제점

1. **생성자 주입시**: `BeanCurrentlyInCreationException` 발생
   ```
   Error creating bean with name 'booServiceImpl': 
   Requested bean is currently in creation: 
   Is there an unresolvable circular reference?
   ```

2. **필드 주입시**: `StackOverflowError` 발생
   ```
   java.lang.StackOverflowError
   at FooServiceImpl.foo() -> BooServiceImpl.boo() -> FooServiceImpl.foo() -> ...
   ```

##### ✅ 순환 의존성 해결 방법들

**방법 1: 구조 개선 (권장)**
```java
// 공통 인터페이스 추출
public interface MessageProcessor {
    String process(String input);
}

@Service
@RequiredArgsConstructor
public class FooServiceImpl implements FooService {
    private final MessageProcessor messageProcessor; // 인터페이스 의존
    
    @Override
    public String foo() {
        return messageProcessor.process("foo");
    }
}

@Service
public class BooServiceImpl implements BooService, MessageProcessor {
    @Override
    public String boo() {
        return "boo";
    }
    
    @Override
    public String process(String input) {
        return "processed: " + input;
    }
}
```

**방법 2: @Lazy 어노테이션 사용**
```java
@Component
@RequiredArgsConstructor
public class BooServiceImpl implements BooService {
    
    @Lazy  // 지연 로딩으로 순환 참조 해결
    private final FooService fooService;
    
    @Override
    public String boo() {
        fooService.foo();
        return "boo";
    }
}
```

**방법 3: ApplicationContextAware 사용**
```java
@Component
public class BooServiceImpl implements BooService, ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public String boo() {
        FooService fooService = applicationContext.getBean(FooService.class);
        fooService.foo();
        return "boo";
    }
}
```

**방법 4: @PostConstruct 사용**
```java
@Component
public class BooServiceImpl implements BooService {
    
    @Autowired
    private ApplicationContext applicationContext;
    private FooService fooService;
    
    @PostConstruct
    public void init() {
        this.fooService = applicationContext.getBean(FooService.class);
    }
    
    @Override
    public String boo() {
        fooService.foo();
        return "boo";
    }
}
```

##### 🧪 순환 의존성 테스트 전략

```java
@ExtendWith(MockitoExtension.class)
public class BooServiceTest {
    
    @Mock
    private FooService fooService;  // Mock으로 순환 참조 해결
    
    @InjectMocks
    private BooServiceImpl booService;
    
    @Test
    @DisplayName("순환 참조 문제가 발생하지 않아야 한다")
    void circularDependencyShouldBeHandled() {
        // given
        when(fooService.foo()).thenReturn("mock-foo");
        
        // when
        String result = booService.boo();
        
        // then
        assertEquals("boo", result);
        verify(fooService, times(1)).foo();
    }
}
```

##### 📊 순환 의존성 해결방법 비교

| 방법 | 장점 | 단점 | 사용 상황 |
|------|------|------|-----------|
| **구조 개선** | 근본적 해결, 명확한 책임 분리 | 설계 변경 필요 | 새 프로젝트, 리팩토링 가능 |
| **@Lazy** | 간단한 적용 | 지연 로딩 성능 이슈 | 임시 해결, 레거시 코드 |
| **ApplicationContextAware** | 유연한 Bean 접근 | Spring 컨텍스트 강결합 | 복잡한 의존성 관리 |
| **@PostConstruct** | 생성 후 초기화 | 초기화 시점 지연 | 조건부 의존성 |

##### 🔬 순환 의존성 재현 방법

**1단계: 순환 의존성 활성화**
```java
// BooServiceImpl.java에서 주석 해제
@Component
@RequiredArgsConstructor
public class BooServiceImpl implements BooService {
    
    private final FooService fooService; // 주석 해제
    
    @Override
    public String boo() {
        fooService.foo(); // 주석 해제
        return "boo";
    }
}
```

**2단계: 애플리케이션 실행**
```bash
./gradlew :chap02:bootRun
```

**3단계: 예상 에러 확인**
```
***************************
APPLICATION FAILED TO START
***************************

Description:
The dependencies of some of the beans in the application context form a cycle:

┌─────┐
|  booServiceImpl defined in file [BooServiceImpl.class]
↑     ↓
|  fooServiceImpl defined in file [FooServiceImpl.class]
└─────┘
```

**4단계: @Lazy로 해결 테스트**
```java
@Component
@RequiredArgsConstructor
public class BooServiceImpl implements BooService {
    
    @Lazy // 추가
    private final FooService fooService;
    
    @Override
    public String boo() {
        fooService.foo();
        return "boo";
    }
}
```

##### 🎯 권장 사항

1. **설계 단계에서 순환 의존성 방지**가 최우선
2. **인터페이스 분리**와 **의존성 역전 원칙** 적용
3. **단방향 의존성 구조** 설계
4. 불가피한 경우에만 **@Lazy** 사용
5. **단위 테스트에서 Mock 활용**하여 의존성 분리

```java
// 잘못된 예: 양방향 의존성
@Service
public class OrderService {
    @Autowired
    private PaymentService paymentService; // PaymentService가 OrderService 참조
}

@Service
public class PaymentService {
    @Autowired
    private OrderService orderService; // 순환 의존성!
}

// 올바른 해결방법 1: 구조 개선
@Service
@RequiredArgsConstructor
public class OrderService {
    private final PaymentProcessor paymentProcessor; // 인터페이스 사용
}

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentProcessor {
    // OrderService 의존성 제거
}

// 올바른 해결방법 2: @Lazy 사용 (임시방편)
@Service
@RequiredArgsConstructor
public class OrderService {
    @Lazy
    private final PaymentService paymentService;
}
```

### 6. 프로파일별 환경 설정

#### 프로파일별 Bean 등록
```java
@Configuration
public class ProfileConfiguration {
    
    @Bean
    @Profile("local")
    public DataSource localDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:mysql://localhost:3306/primavera_local")
                .username("sa")
                .password("")
                .build();
    }
    
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.mariadb.jdbc.Driver")
                .url("jdbc:mysql://dev-db:3306/primavera")
                .username("dev_user")
                .password("dev_pass")
                .build();
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.mariadb.jdbc.Driver")
                .url("jdbc:mysql://prod-db:3306/primavera")
                .username("${DB_USERNAME}")
                .password("${DB_PASSWORD}")
                .build();
    }
}
```

## 🔧 실습 예제

### Hello World Controller
```java
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class HelloController {
    
    private final PrimaveraProperties properties;
    private final Environment environment;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> helloWorld() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello Primavera!");
        response.put("activeProfiles", environment.getActiveProfiles());
        response.put("database", properties.getDatabase());
        response.put("users", properties.getUsers());
        
        log.info("Hello World 요청 처리 완료");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/config")
    public ResponseEntity<PrimaveraProperties> getConfiguration() {
        return ResponseEntity.ok(properties);
    }
}
```

### Configuration 테스트
```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class ConfigurationTest {
    
    @Autowired
    private PrimaveraProperties properties;
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    @Order(1)
    @DisplayName("설정 프로퍼티가 올바르게 바인딩되는지 확인")
    void testConfigurationBinding() {
        // Given & When & Then
        assertThat(properties.getDatabase().getUsername()).isEqualTo("primavera");
        assertThat(properties.getDatabase().getTables()).contains("user", "role");
        assertThat(properties.getSearch().getParams().getPage()).isEqualTo(1);
        assertThat(properties.getUsers()).hasSize(2);
    }
    
    @Test
    @Order(2)
    @DisplayName("Bean이 올바른 스코프로 등록되는지 확인")
    void testBeanScope() {
        // Singleton 빈 테스트
        Object bean1 = context.getBean("singletonService");
        Object bean2 = context.getBean("singletonService");
        assertThat(bean1).isSameAs(bean2);
        
        // Prototype 빈 테스트
        Object prototype1 = context.getBean("prototypeHandler");
        Object prototype2 = context.getBean("prototypeHandler");
        assertThat(prototype1).isNotSameAs(prototype2);
    }
}
```

## 🧪 테스트 전략

### JUnit 5 설정
```gradle
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
    testImplementation 'org.junit.jupiter:junit-jupiter-params'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'
}

test {
    useJUnitPlatform()
}
```

### 테스트 프로파일 설정
```java
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "com.genius.primavera.database.username=test_user",
    "com.genius.primavera.database.password=test_pass"
})
class ProfileTest {
    
    @Test
    void testProfileSpecificConfiguration() {
        // 테스트 전용 설정 검증
    }
}
```

## 📊 Bean Scope 비교표

| 스코프 | 생명주기 | 사용 사례 | 주의사항 |
|-------|---------|----------|---------|
| **singleton** | 컨테이너당 하나 | 상태가 없는 서비스 | 스레드 안전성 고려 |
| **prototype** | 요청시마다 생성 | 상태를 가진 객체 | 메모리 누수 주의 |
| **request** | HTTP 요청당 하나 | 웹 요청 컨텍스트 | 웹 환경에서만 사용 |
| **session** | HTTP 세션당 하나 | 사용자 세션 데이터 | 세션 만료 고려 |
| **application** | ServletContext당 하나 | 애플리케이션 전역 | 웹 애플리케이션 레벨 |

## 🚀 애플리케이션 실행

### 개발 환경 실행
```bash
# 기본 프로파일로 실행
./gradlew bootRun

# 특정 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# JAR 파일 빌드 및 실행
./gradlew build
java -jar build/libs/chap01-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker 환경 실행
```bash
# 다중 프로파일 지원
docker run -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e DB_USERNAME=prod_user \
    -e DB_PASSWORD=prod_pass \
    primavera:latest
```

## 🐳 인프라 설정

### Docker Compose 환경 설정

이 챕터는 **기초 학습용 인프라**를 사용합니다:

```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# 기초 학습용 Docker Compose 실행 (MariaDB)
docker-compose -f docker-compose.basic.yml up -d

# 서비스 상태 확인
docker-compose -f docker-compose.basic.yml ps

# 정리 (컨테이너 및 볼륨 삭제)
docker-compose -f docker-compose.basic.yml down -v
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308)
- 기본 데이터베이스 스키마 자동 생성

**애플리케이션 실행:**
```bash
# 인프라 시작 후 애플리케이션 실행
./gradlew :chap02:bootRun -Dspring.profiles.active=local
```

## 📖 참고 자료

### 공식 문서
- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Framework IoC Container](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans)
- [Bean Scopes](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-scopes)

### 모범 사례
- [Constructor-based vs Setter-based DI](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection)
- [Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties)
- [Profile-specific Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)

## 🚀 다음 단계

다음 Chapter에서는 **MVC와 AOP**를 학습합니다:
- Spring MVC 아키텍처 패턴 구현
- AOP를 통한 횡단 관심사 분리
- 인터셉터와 필터 체인 활용
- 관점 지향 프로그래밍 실습

---

**🎓 학습 포인트**: 생성자 기반 의존성 주입은 불변성, 테스트 용이성, 순환 의존성 방지에 핵심적입니다. @ConfigurationProperties와 프로파일을 활용한 환경별 설정 관리는 실무에서 매우 중요한 기술입니다.