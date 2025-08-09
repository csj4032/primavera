# Chapter 01 - Spring Boot 기본과 의존성 주입

## 학습 목표

Spring Boot의 핵심 개념과 의존성 주입(DI) 컨테이너의 기본 원리를 이해하고 실습합니다.

- Spring Boot 애플리케이션의 기본 구조 이해
- 의존성 주입과 IoC 컨테이너의 동작 원리
- Bean의 생명주기와 스코프 관리
- 기본적인 REST API 개발

## 프로젝트 구조

```
chap01/
├── src/main/java/com/genius/primavera/
│   ├── SpringBootStarterApplication.java     # 메인 애플리케이션 클래스
│   ├── application/                          # 서비스 계층
│   │   ├── HelloService.java
│   │   ├── HelloServiceImpl.java
│   │   ├── WorldService.java
│   │   └── WorldServiceImpl.java
│   ├── interfaces/                           # 컨트롤러 계층
│   │   ├── HelloController.java
│   │   └── WorldController.java
│   ├── domain/                              # 도메인 모델
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── Plant.java
│   └── basics/                              # DI 기본 예제들
│       ├── DependencyInjectionExample.java
│       ├── BeanLifecycleExample.java
│       ├── BeanScopeExample.java
│       ├── ConfigurationExample.java
│       └── WebBasicsController.java
└── src/main/resources/
    ├── application.yml                      # 애플리케이션 설정
    └── banner.txt                          # 시작 배너
```

## 기술 스택

- **Spring Boot**: 3.3.6
- **Spring Web MVC**: REST API 개발
- **Spring Validation**: 데이터 검증
- **Java**: 21 (Record, Pattern Matching 활용)
- **Lombok**: 코드 간소화

## 주요 기능

### 1. 의존성 주입 기본 패턴

```java
@RestController
@RequestMapping("/hello")
public class HelloController {
    private final HelloService helloService;
    
    public HelloController(HelloService helloService) {
        this.helloService = helloService;  // 생성자 주입
    }
    
    @GetMapping
    public String hello() {
        return helloService.sayHello();
    }
}
```

### 2. Bean 생명주기 관리

```java
@Component
public class BeanLifecycleExample {
    
    @PostConstruct
    public void init() {
        log.info("Bean 초기화 완료");
    }
    
    @PreDestroy
    public void destroy() {
        log.info("Bean 소멸 준비");
    }
}
```

### 3. Bean 스코프 비교

```java
@Component
@Scope("singleton")  // 기본값: 애플리케이션 전체에서 하나의 인스턴스
public class SingletonBean { }

@Component
@Scope("prototype")  // 요청할 때마다 새 인스턴스 생성
public class PrototypeBean { }
```

## 실행 방법

### 애플리케이션 시작

```bash
# Gradle을 사용한 실행
./gradlew :chap01:bootRun

# 또는 JAR 빌드 후 실행
./gradlew :chap01:build
java -jar chap01/build/libs/chap01.jar
```

### API 테스트

```bash
# Hello API 테스트
curl http://localhost:8080/hello

# World API 테스트
curl http://localhost:8080/world

# 기본 DI 예제 테스트
curl http://localhost:8080/basics/di

# Bean 스코프 비교 테스트
curl http://localhost:8080/basics/scope
```

## 핵심 학습 포인트

### 1. @SpringBootApplication 어노테이션

```java
@SpringBootApplication
public class SpringBootStarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterApplication.class, args);
    }
}
```

`@SpringBootApplication`은 다음 세 개의 어노테이션을 포함합니다:
- `@SpringBootConfiguration`: Spring Boot 설정 클래스
- `@EnableAutoConfiguration`: 자동 설정 활성화
- `@ComponentScan`: 컴포넌트 스캔 범위 지정

### 2. 의존성 주입 방법 비교

```java
@RestController
public class InjectionExampleController {
    
    // 1. 생성자 주입 (권장)
    private final HelloService helloService;
    
    public InjectionExampleController(HelloService helloService) {
        this.helloService = helloService;
    }
    
    // 2. 필드 주입 (테스트용으로만 사용)
    @Autowired
    private WorldService worldService;
    
    // 3. Setter 주입 (선택적 의존성)
    private OptionalService optionalService;
    
    @Autowired(required = false)
    public void setOptionalService(OptionalService optionalService) {
        this.optionalService = optionalService;
    }
}
```

### 3. Bean 등록 방법

```java
// 방법 1: 컴포넌트 스캔을 통한 자동 등록
@Component
@Service
@Repository
@Controller
public class AutoRegisteredBean { }

// 방법 2: @Bean을 통한 수동 등록
@Configuration
public class BeanConfiguration {
    
    @Bean
    public CustomService customService() {
        return new CustomServiceImpl();
    }
}
```

## 테스트 실행

```bash
# 전체 테스트 실행
./gradlew :chap01:test

# 특정 테스트 클래스 실행
./gradlew :chap01:test --tests "HelloControllerTest"

# 테스트 리포트 확인
open chap01/build/reports/tests/test/index.html
```

## 주요 애너테이션

| 애너테이션 | 용도 | 예제 |
|-----------|------|------|
| `@SpringBootApplication` | 메인 애플리케이션 클래스 | `@SpringBootApplication public class App` |
| `@RestController` | REST API 컨트롤러 | `@RestController public class HelloController` |
| `@Service` | 서비스 계층 빈 | `@Service public class HelloService` |
| `@Component` | 일반 컴포넌트 빈 | `@Component public class UtilityClass` |
| `@Autowired` | 의존성 자동 주입 | `@Autowired private HelloService service` |
| `@PostConstruct` | 초기화 메서드 | `@PostConstruct public void init()` |
| `@PreDestroy` | 소멸 메서드 | `@PreDestroy public void cleanup()` |

## 학습 순서

1. **Spring Boot 기본 구조 이해** - `SpringBootStarterApplication.java` 분석
2. **의존성 주입 실습** - `DependencyInjectionExample.java` 실행
3. **Bean 생명주기 확인** - `BeanLifecycleExample.java` 로그 확인
4. **Bean 스코프 비교** - `BeanScopeExample.java`로 싱글톤과 프로토타입 차이 체험
5. **REST API 개발** - `HelloController`, `WorldController` 구현 이해
6. **설정 관리** - `application.yml` 설정 파일 구조 파악

## 다음 단계 안내

**Chapter 02**에서는 Spring Boot의 설정 관리를 심도 있게 학습합니다:
- `@ConfigurationProperties`를 통한 타입 안전한 설정 바인딩
- 프로파일별 설정 관리 (local, test, production)
- XML, Properties, YAML 설정 파일 형식 비교
- 외부 설정 소스 활용법

```bash
# 다음 챕터로 이동
cd ../chap02
./gradlew :chap02:bootRun
```