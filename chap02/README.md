# Chapter 02 - Spring Boot 설정 관리와 외부 설정

## 학습 목표

Spring Boot의 다양한 설정 방법과 외부 설정 소스를 활용한 설정 관리를 마스터합니다.

- `@ConfigurationProperties`를 통한 타입 안전한 설정 바인딩
- 프로파일별 설정 분리와 환경별 배포 전략
- XML, Properties, YAML 설정 형식 비교와 활용
- 외부 API 연동을 위한 Retrofit 설정 관리
- Bean 스코프와 생명주기 심화 학습

## 프로젝트 구조

```
chap02/
├── src/main/java/com/genius/primavera/
│   ├── ConfigurationDependencyApplication.java  # 메인 애플리케이션
│   ├── config/
│   │   └── PrimaveraProperties.java             # 설정 프로퍼티 클래스
│   ├── application/
│   │   ├── injection/                           # 의존성 주입 예제
│   │   │   ├── EmailService.java
│   │   │   ├── EmailServiceImpl.java
│   │   │   ├── LoggingService.java
│   │   │   └── LoggingServiceImpl.java
│   │   ├── plant/                               # Plant 도메인 서비스
│   │   │   ├── IPlantService.java
│   │   │   ├── PlantService.java
│   │   │   ├── IPlantRepository.java
│   │   │   ├── PlantRepository.java
│   │   │   └── IPlantRetrofitRepository.java
│   │   ├── RetrofitClient.java                  # 외부 API 클라이언트
│   │   ├── RetrofitClientConfig.java            # Retrofit 설정
│   │   └── ScopeService.java                    # 스코프 테스트 서비스
│   ├── lifecycle/                               # Bean 생명주기 예제
│   │   ├── AnnotationClass.java
│   │   ├── InterfaceImpl.java
│   │   └── XmlBean.java
│   ├── scope/                                   # Bean 스코프 예제
│   │   ├── Singleton.java
│   │   ├── Prototype.java
│   │   └── ScopeRunner.java
│   └── interfaces/
│       └── HelloController.java                 # REST 컨트롤러
└── src/main/resources/
    ├── application.yml                          # 기본 설정 (YAML)
    ├── banner.txt                              # 커스텀 배너
    ├── configuration.xml                        # XML 기반 Bean 설정
    └── templates/
        └── index.html                          # Thymeleaf 템플릿
```

## 기술 스택

- **Spring Boot**: 3.3.6
- **Spring Configuration Processor**: 설정 메타데이터 생성
- **Retrofit 2**: 외부 API 연동
- **Gson**: JSON 직렬화/역직렬화
- **Thymeleaf**: 템플릿 엔진
- **Java**: 21 (Record 활용)

## 주요 기능

### 1. 타입 안전한 설정 바인딩

```java
@ConfigurationProperties(prefix = "primavera")
@Component
public class PrimaveraProperties {
    private String name = "Default Primavera";
    private String version = "1.0.0";
    private Database database = new Database();
    private List<String> allowedOrigins = new ArrayList<>();
    
    public record Database(
        String url,
        String username, 
        String password,
        int maxConnections
    ) {}
    
    // getters and setters...
}
```

### 2. 외부 API 연동 설정

```java
@Configuration
public class RetrofitClientConfig {
    
    @Bean
    public Retrofit retrofit(@Value("${external.api.baseUrl}") String baseUrl) {
        return new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }
    
    @Bean
    public IPlantRetrofitRepository plantRetrofitRepository(Retrofit retrofit) {
        return retrofit.create(IPlantRetrofitRepository.class);
    }
}
```

### 3. XML 기반 Bean 설정

```xml
<!-- configuration.xml -->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    
    <bean id="xmlBean" 
          class="com.genius.primavera.lifecycle.XmlBean"
          init-method="init" 
          destroy-method="destroy">
        <property name="message" value="XML Configuration Bean"/>
    </bean>
</beans>
```

### 4. Bean 스코프 비교 실습

```java
@Component
@Scope("singleton")
public class Singleton {
    private final String id = UUID.randomUUID().toString();
    
    public String getId() { return id; }
}

@Component  
@Scope("prototype")
public class Prototype {
    private final String id = UUID.randomUUID().toString();
    
    public String getId() { return id; }
}
```

## 실행 방법

### 애플리케이션 시작

```bash
# 기본 프로파일로 실행
./gradlew :chap02:bootRun

# 특정 프로파일로 실행
./gradlew :chap02:bootRun -Dspring.profiles.active=dev

# 외부 설정 파일 지정
./gradlew :chap02:bootRun -Dspring.config.location=classpath:/custom-config.yml
```

### API 테스트

```bash
# 설정 정보 확인
curl http://localhost:8080/hello/config

# Bean 스코프 테스트
curl http://localhost:8080/hello/scope

# 외부 API 연동 테스트 (Plant API)
curl http://localhost:8080/hello/plants

# Thymeleaf 템플릿 뷰
curl http://localhost:8080/hello/view
```

## 핵심 학습 포인트

### 1. 설정 파일 우선순위

Spring Boot는 다음 순서로 설정을 로드합니다:

1. **커맨드라인 아규먼트** (`--server.port=9090`)
2. **시스템 프로퍼티** (`-Dserver.port=9090`)
3. **환경 변수** (`SERVER_PORT=9090`)
4. **application-{profile}.properties/yml**
5. **application.properties/yml**
6. **@PropertySource**

### 2. @ConfigurationProperties vs @Value

```java
// @Value 방식 - 간단한 설정
@Component
public class SimpleConfig {
    @Value("${app.name:defaultName}")
    private String appName;
}

// @ConfigurationProperties 방식 - 복합 설정
@ConfigurationProperties(prefix = "app")
@Component
public class ComplexConfig {
    private String name;
    private Database database;
    private List<String> features;
    // 중첩된 설정, 리스트, 검증 등 지원
}
```

### 3. 프로파일별 설정 분리

```yaml
# application.yml (공통 설정)
spring:
  application:
    name: primavera-chap02
    
primavera:
  name: "Primavera Configuration Example"
  version: "2.0.0"

---
# 개발 환경 설정
spring:
  config:
    activate:
      on-profile: dev
      
external:
  api:
    baseUrl: "http://localhost:3000/api/"
    timeout: 5000

---
# 운영 환경 설정  
spring:
  config:
    activate:
      on-profile: prod
      
external:
  api:
    baseUrl: "https://api.production.com/"
    timeout: 30000
```

### 4. Bean 생명주기 관리

```java
@Component
public class LifecycleBean implements InitializingBean, DisposableBean {
    
    // Spring 인터페이스 방식
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("InitializingBean.afterPropertiesSet() 호출");
    }
    
    @Override
    public void destroy() throws Exception {
        log.info("DisposableBean.destroy() 호출"); 
    }
    
    // JSR-250 애너테이션 방식 (권장)
    @PostConstruct
    public void postConstruct() {
        log.info("@PostConstruct 호출");
    }
    
    @PreDestroy  
    public void preDestroy() {
        log.info("@PreDestroy 호출");
    }
}
```

## 설정 검증과 메타데이터

### 1. 설정 검증

```java
@ConfigurationProperties(prefix = "primavera")
@Validated
@Component
public class ValidatedProperties {
    
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;
    
    @Min(1)
    @Max(65535)
    private int port = 8080;
    
    @Valid
    private Database database;
    
    public static class Database {
        @NotNull
        @Pattern(regexp = "^jdbc:.*")
        private String url;
        
        // getters and setters...
    }
}
```

### 2. 메타데이터 생성

Configuration Processor가 자동으로 생성하는 메타데이터:

```json
{
  "properties": [
    {
      "name": "primavera.name",
      "type": "java.lang.String",
      "description": "애플리케이션 이름",
      "defaultValue": "Default Primavera"
    },
    {
      "name": "primavera.database.url", 
      "type": "java.lang.String",
      "description": "데이터베이스 연결 URL"
    }
  ]
}
```

## 테스트 실행

```bash
# 전체 테스트 실행
./gradlew :chap02:test

# 설정 관련 테스트만 실행
./gradlew :chap02:test --tests "*PropertiesTest"

# 프로파일별 테스트
./gradlew :chap02:test -Dspring.profiles.active=test
```

## 주요 애너테이션

| 애너테이션 | 용도 | 예제 |
|-----------|------|------|
| `@ConfigurationProperties` | 설정 프로퍼티 바인딩 | `@ConfigurationProperties(prefix = "app")` |
| `@EnableConfigurationProperties` | 설정 프로퍼티 활성화 | `@EnableConfigurationProperties(AppProperties.class)` |
| `@Value` | 단일 프로퍼티 주입 | `@Value("${app.name}")` |
| `@PropertySource` | 추가 설정 파일 지정 | `@PropertySource("classpath:custom.properties")` |
| `@Profile` | 프로파일별 Bean 등록 | `@Profile("dev")` |
| `@Conditional` | 조건부 Bean 등록 | `@ConditionalOnProperty(name = "feature.enabled")` |
| `@ImportResource` | XML 설정 import | `@ImportResource("classpath:beans.xml")` |

## 실습 과제

### 1. 커스텀 설정 클래스 만들기

```yaml
# application.yml에 추가
myapp:
  features:
    - user-management
    - notification
    - analytics
  cache:
    enabled: true
    ttl: 3600
    provider: redis
  notification:
    email:
      enabled: true
      smtp-host: smtp.gmail.com
      smtp-port: 587
    slack:
      enabled: false
      webhook-url: ""
```

이 설정에 맞는 `@ConfigurationProperties` 클래스를 작성해보세요.

### 2. 조건부 Bean 등록

특정 설정값에 따라 다른 구현체가 등록되도록 구현해보세요:

```java
@ConditionalOnProperty(name = "cache.provider", havingValue = "redis")
@Service
public class RedisCacheService implements CacheService { }

@ConditionalOnProperty(name = "cache.provider", havingValue = "memory")  
@Service
public class MemoryCacheService implements CacheService { }
```

## 학습 순서

1. **설정 파일 형식 비교** - YAML vs Properties vs XML
2. **@ConfigurationProperties 실습** - `PrimaveraProperties.java` 분석
3. **프로파일별 설정** - dev/prod 프로파일 전환 테스트
4. **외부 API 연동** - Retrofit 설정과 Plant API 호출
5. **Bean 스코프 실험** - Singleton vs Prototype 동작 확인
6. **생명주기 관리** - 초기화/소멸 메서드 실행 순서 확인

## 다음 단계 안내

**Chapter 03**에서는 Spring MVC와 AOP의 핵심 개념을 학습합니다:
- MVC 패턴 구현과 계층별 역할 분리
- AOP를 활용한 횡단 관심사 처리
- Thymeleaf 템플릿 엔진 활용
- JSON 파일 기반 Repository 구현
- Filter, Interceptor, Aspect 비교

```bash  
# 다음 챕터로 이동
cd ../chap03
./gradlew :chap03:bootRun
```