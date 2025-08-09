# Chapter 03 - Spring MVC와 AOP 기초

## 학습 목표

Spring MVC 패턴의 구현과 AOP(관점 지향 프로그래밍)의 기본 개념을 실습하며 웹 애플리케이션의 계층별 역할을 이해합니다.

- MVC 패턴 구현과 계층별 역할 분리 (Controller-Service-Repository)
- AOP를 활용한 횡단 관심사 처리 (로깅, 성능 측정)
- Filter, Interceptor, Aspect의 차이점과 활용
- JSON 파일 기반 Repository 구현
- Thymeleaf 템플릿 엔진을 통한 뷰 렌더링
- 글로벌 예외 처리 전략

## 프로젝트 구조

```
chap03/
├── src/main/java/com/genius/primavera/
│   ├── MvcAopApplication.java                       # 메인 애플리케이션
│   ├── interfaces/                                  # Presentation Layer
│   │   └── HelloController.java                     # REST/View 컨트롤러
│   ├── applicaiton/                                # Application Layer
│   │   ├── HelloService.java                       # 서비스 인터페이스
│   │   ├── HelloServiceImpl.java                   # 서비스 구현체
│   │   ├── UserRepository.java                     # JSON 기반 Repository
│   │   ├── GlobalExceptionHandler.java             # 전역 예외 처리
│   │   └── OopsException.java                      # 커스텀 예외
│   ├── domain/                                      # Domain Layer
│   │   └── User.java                               # User 도메인 모델
│   └── infrastructure/                              # Infrastructure Layer
│       ├── aspect/                                  # AOP 관련
│       │   ├── PrimaveraLogging.java               # 커스텀 로깅 어노테이션
│       │   └── PrimaveraLoggingAspect.java         # AOP Aspect 구현체
│       ├── interception/                           # Request/Response 처리
│       │   ├── PrimaveraFilter.java                # 서블릿 필터
│       │   ├── PrimaveraInterceptor.java           # MVC 인터셉터
│       │   └── ResettableStream*.java              # Request/Response 래퍼
│       └── WebMvcConfig.java                       # MVC 설정
└── src/main/resources/
    ├── application.yml                              # 애플리케이션 설정
    ├── data/
    │   └── users.json                              # JSON 데이터 파일
    └── templates/                                  # Thymeleaf 템플릿
        ├── hello.html
        └── world.html
```

## 기술 스택

- **Spring Web MVC**: RESTful API 및 웹 MVC 지원
- **Spring AOP**: 관점 지향 프로그래밍 
- **Thymeleaf**: 서버사이드 템플릿 엔진
- **Apache Commons IO**: 파일 I/O 유틸리티
- **Jackson**: JSON 데이터 바인딩
- **Java**: 21 (Record, Pattern Matching 활용)

## 주요 기능

### 1. MVC 패턴 구현

```java
@RestController
@RequestMapping("/hello")
public class HelloController {
    private final HelloService helloService;
    
    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = helloService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/view")
    public String viewTemplate(Model model) {
        model.addAttribute("users", helloService.getAllUsers());
        return "hello";
    }
}
```

### 2. JSON 파일 기반 Repository

```java
@Repository
public class UserRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<User> findAll() {
        try {
            ClassPathResource resource = new ClassPathResource("data/users.json");
            String jsonContent = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            return objectMapper.readValue(jsonContent, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users from JSON", e);
        }
    }
}
```

### 3. AOP를 통한 로깅

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaveraLogging {
    String value() default "";
}

@Aspect
@Component
public class PrimaveraLoggingAspect {
    
    @Around("@annotation(primaveraLogging)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, PrimaveraLogging primaveraLogging) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        Object result = joinPoint.proceed();
        
        long endTime = System.currentTimeMillis();
        log.info("Method [{}] executed in {} ms - {}", 
            joinPoint.getSignature().getName(), 
            endTime - startTime, 
            primaveraLogging.value());
            
        return result;
    }
}
```

### 4. 전역 예외 처리

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(OopsException.class)
    public ResponseEntity<Map<String, Object>> handleOopsException(OopsException e) {
        Map<String, Object> response = Map.of(
            "error", "OOPS_EXCEPTION",
            "message", e.getMessage(),
            "timestamp", LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        Map<String, Object> response = Map.of(
            "error", "INTERNAL_SERVER_ERROR",
            "message", "An unexpected error occurred",
            "timestamp", LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

## 실행 방법

### 애플리케이션 시작

```bash
# Gradle을 사용한 실행
./gradlew :chap03:bootRun

# JAR 빌드 후 실행
./gradlew :chap03:build
java -jar chap03/build/libs/chap03.jar
```

### API 테스트

```bash
# JSON API - 사용자 목록 조회
curl http://localhost:8080/hello/users

# AOP 로깅 테스트
curl http://localhost:8080/hello/logging-test

# 예외 처리 테스트
curl http://localhost:8080/hello/error-test

# Thymeleaf 뷰 렌더링
curl http://localhost:8080/hello/view

# 정적 리소스 테스트
open http://localhost:8080/hello/world
```

## 핵심 학습 포인트

### 1. Spring MVC 요청 처리 흐름

```
Client Request
      ↓
DispatcherServlet (Front Controller)
      ↓
HandlerMapping (URL 매핑)
      ↓
HandlerInterceptor (preHandle)
      ↓
Controller (비즈니스 로직 호출)
      ↓
Service Layer (비즈니스 로직 처리)
      ↓
Repository Layer (데이터 액세스)
      ↓
HandlerInterceptor (postHandle)
      ↓
ViewResolver (뷰 이름 → 뷰 객체)
      ↓
View (렌더링)
      ↓
HandlerInterceptor (afterCompletion)
      ↓
Client Response
```

### 2. Filter vs Interceptor vs AOP

| 구분 | Filter | Interceptor | AOP |
|------|--------|-------------|-----|
| **실행 시점** | 서블릿 전/후 | 컨트롤러 전/후 | 메서드 실행 전/후/예외 |
| **적용 범위** | 모든 요청 | Spring MVC 범위 | 특정 메서드/클래스 |
| **용도** | 인코딩, 보안, 로깅 | 인증, 권한, 로케일 | 트랜잭션, 로깅, 성능측정 |

```java
// Filter 구현
@Component
public class PrimaveraFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        // 요청 전 처리
        long startTime = System.currentTimeMillis();
        
        chain.doFilter(request, response);
        
        // 응답 후 처리
        long duration = System.currentTimeMillis() - startTime;
        log.info("Request processed in {} ms", duration);
    }
}

// Interceptor 구현
@Component
public class PrimaveraInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("Pre-handle: {}", request.getRequestURI());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        log.info("Post-handle: {}", request.getRequestURI());
    }
}
```

### 3. JSON 데이터 처리

```java
// users.json 구조
[
  {
    "id": 1,
    "name": "김철수",
    "email": "kim@example.com",
    "age": 30
  },
  {
    "id": 2,
    "name": "이영희", 
    "email": "lee@example.com",
    "age": 25
  }
]

// Repository에서 JSON 파일 읽기
@Repository
public class UserRepository {
    
    @PrimaveraLogging("사용자 데이터 로딩")
    public List<User> findAll() {
        try {
            ClassPathResource resource = new ClassPathResource("data/users.json");
            String content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            return objectMapper.readValue(content, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load user data", e);
        }
    }
}
```

### 4. Thymeleaf 템플릿 처리

```html
<!-- hello.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Primavera Users</title>
    <meta charset="UTF-8">
</head>
<body>
    <h1>사용자 목록</h1>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>이름</th>
                <th>이메일</th>
                <th>나이</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="user : ${users}">
                <td th:text="${user.id}"></td>
                <td th:text="${user.name}"></td>
                <td th:text="${user.email}"></td>
                <td th:text="${user.age}"></td>
            </tr>
        </tbody>
    </table>
</body>
</html>
```

### 5. 계층별 책임 분리

```java
// Controller Layer - 요청/응답 처리
@RestController
@RequestMapping("/hello")
public class HelloController {
    private final HelloService helloService;
    
    @GetMapping("/users")
    @PrimaveraLogging("사용자 목록 조회 API")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = helloService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}

// Service Layer - 비즈니스 로직 처리
@Service
public class HelloServiceImpl implements HelloService {
    private final UserRepository userRepository;
    
    @Override
    @PrimaveraLogging("사용자 비즈니스 로직")
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        // 비즈니스 로직 처리 (필터링, 정렬, 변환 등)
        return users.stream()
            .filter(user -> user.getAge() >= 18)
            .collect(Collectors.toList());
    }
}

// Repository Layer - 데이터 액세스
@Repository
public class UserRepository {
    
    @PrimaveraLogging("데이터 액세스")
    public List<User> findAll() {
        // JSON 파일에서 사용자 데이터 로드
        return loadUsersFromJson();
    }
}
```

## 테스트 실행

```bash
# 전체 테스트 실행
./gradlew :chap03:test

# 특정 테스트 클래스 실행
./gradlew :chap03:test --tests "HelloControllerTest"

# AOP 관련 테스트 실행
./gradlew :chap03:test --tests "*AspectTest"

# 테스트 커버리지 리포트 생성
./gradlew :chap03:jacocoTestReport
```

## 주요 애너테이션

| 애너테이션 | 용도 | 예제 |
|-----------|------|------|
| `@RestController` | REST API 컨트롤러 | `@RestController public class HelloController` |
| `@RequestMapping` | URL 매핑 | `@RequestMapping("/hello")` |
| `@GetMapping` | HTTP GET 매핑 | `@GetMapping("/users")` |
| `@Service` | 서비스 계층 빈 | `@Service public class HelloService` |
| `@Repository` | 데이터 액세스 계층 빈 | `@Repository public class UserRepository` |
| `@Aspect` | AOP Aspect 정의 | `@Aspect @Component public class LoggingAspect` |
| `@Around` | Around Advice | `@Around("@annotation(log)")` |
| `@ControllerAdvice` | 전역 예외 처리 | `@ControllerAdvice public class GlobalExceptionHandler` |

## 실습 과제

### 1. 커스텀 AOP 어노테이션 만들기

성능 측정을 위한 `@PerformanceMonitoring` 어노테이션을 만들어보세요:

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceMonitoring {
    String value() default "";
    long threshold() default 1000; // 임계값 (ms)
}

@Aspect
@Component
public class PerformanceAspect {
    // Around advice 구현
    // threshold 초과시 WARNING 로그 출력
}
```

### 2. JSON 데이터 CRUD Repository 구현

읽기 전용이 아닌 CRUD 기능을 지원하는 Repository를 구현해보세요:

```java
@Repository
public class JsonUserRepository {
    public List<User> findAll() { /* JSON 파일 읽기 */ }
    public User findById(Long id) { /* ID로 사용자 찾기 */ }
    public User save(User user) { /* 사용자 저장 */ }
    public void deleteById(Long id) { /* 사용자 삭제 */ }
}
```

### 3. 다중 예외 처리 핸들러

다양한 예외에 대한 세부적인 처리를 구현해보세요:

```java
@ControllerAdvice
public class DetailedExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        // 검증 실패 처리
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        // 리소스 없음 처리
    }
}
```

## 학습 순서

1. **MVC 패턴 이해** - Controller, Service, Repository의 역할 분리 학습
2. **JSON 데이터 처리** - `users.json` 파일을 읽어 User 객체 변환 과정 확인
3. **AOP 실습** - `@PrimaveraLogging` 어노테이션과 Aspect 동작 확인
4. **요청 처리 흐름** - Filter → Interceptor → Controller → Service → Repository 흐름 추적
5. **템플릿 렌더링** - Thymeleaf를 사용한 서버사이드 렌더링 실습
6. **예외 처리** - 글로벌 예외 핸들러 동작 확인

## 다음 단계 안내

**Chapter 04**에서는 데이터베이스 연동과 MyBatis를 학습합니다:
- MyBatis를 통한 SQL 매핑과 동적 쿼리
- MariaDB 데이터베이스 연동 및 커넥션 풀 관리
- TestContainers를 활용한 통합 테스트
- 동적 프록시 패턴 구현
- 데이터 액세스 계층 아키텍처

```bash
# 다음 챕터로 이동
cd ../chap04
./gradlew :chap04:bootRun -Dspring.profiles.active=local
```