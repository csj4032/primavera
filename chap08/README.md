# Chapter 08: Spring Security 기초, 필터 체인

## 모듈 소개

**SecurityFilterApplication**은 Spring Security의 필터 체인 구조와 커스텀 보안 필터를 학습하는 모듈입니다. Chain of Responsibility 패턴을 활용한 필터 설계와 Lucy XSS Filter 통합을 통해 웹 애플리케이션의 보안 아키텍처를 체계적으로 구현합니다.

## 학습 목표

- Spring Security 필터 체인 구조 이해 및 구현
- 커스텀 보안 필터 개발 및 Chain of Responsibility 패턴 적용
- Lucy XSS Filter와 Spring Security 통합
- SecurityContext와 Authentication 객체 활용
- Undertow 웹 서버와 보안 설정 최적화
- 보안 필터의 실행 순서와 우선순위 관리

## 프로젝트 구조

```
chap08/
├── src/main/java/com/genius/primavera/
│   ├── SecurityFilterApplication.java          # 메인 애플리케이션
│   ├── application/                            # 비즈니스 로직 계층
│   │   ├── UserService.java                    # 사용자 서비스 인터페이스
│   │   ├── UserServiceImpl.java                # 사용자 서비스 구현체
│   │   └── validator/                          # 커스텀 검증자
│   │       ├── Nickname.java                   # 닉네임 검증 어노테이션
│   │       └── NicknameValidator.java          # 닉네임 검증 로직
│   ├── domain/                                 # 도메인 모델 계층
│   │   ├── mapper/                             # MyBatis 매퍼
│   │   │   ├── UserMapper.java                 # 사용자 매퍼
│   │   │   ├── UserRoleMapper.java             # 사용자 권한 매퍼
│   │   │   └── support/UserTableSupport.java   # 테이블 지원 유틸
│   │   ├── model/                              # 도메인 모델
│   │   │   ├── User.java                       # 사용자 엔티티
│   │   │   ├── Role.java                       # 권한 엔티티
│   │   │   └── UserRole.java                   # 사용자-권한 연관 엔티티
│   │   └── typehandler/                        # 커스텀 타입 핸들러
│   │       ├── RoleTypeHandler.java            # 권한 타입 핸들러
│   │       └── UserStatusTypeHandler.java      # 사용자 상태 핸들러
│   ├── infrastructure/                         # 인프라 설정
│   │   ├── ApplicationConfiguration.java       # 애플리케이션 구성
│   │   └── filter/                             # 커스텀 필터
│   │       └── PrimaveraFilter.java            # 보안 필터 구현
│   └── interfaces/                             # 인터페이스 계층
│       ├── FilterController.java               # 필터 테스트 컨트롤러
│       ├── LoginController.java                # 로그인 컨트롤러
│       └── UserController.java                 # 사용자 REST API
├── src/main/resources/
│   ├── application.yml                         # 메인 설정
│   ├── application-local.yml                   # 로컬 환경 설정
│   ├── lucy-xss*.xml                           # Lucy XSS 필터 설정
│   ├── static/                                 # 정적 리소스
│   │   ├── bower_components/                   # 프론트엔드 라이브러리
│   │   ├── dist/                               # AdminLTE 배포 파일
│   │   └── plugins/                            # AdminLTE 플러그인
│   └── templates/                              # Thymeleaf 템플릿
│       ├── fragments/                          # 템플릿 조각
│       ├── layouts/layout.html                 # 기본 레이아웃
│       ├── index.html                          # 메인 페이지
│       └── login.html                          # 로그인 페이지
└── src/test/                                   # 테스트 코드
    ├── java/com/genius/primavera/
    │   └── infrastructure/filter/               # 필터 테스트
    │       └── PrimaveraFilterTest.java         # 보안 필터 테스트
    └── resources/
        ├── application-test.yml                # 테스트 환경 설정
        ├── lucy-xss*.xml                       # 테스트용 XSS 필터 설정
        └── sql/init.sql                        # 테스트 DB 초기화
```

## 주요 기능

### 1. Spring Security 커스텀 필터 구현

#### PrimaveraFilter - 보안 필터 체인 구현
```java
@Component
@Slf4j
@ToString
public class PrimaveraFilter extends OncePerRequestFilter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws IOException, ServletException {
        
        // 로그인 경로에 대한 특별 처리
        if (antPathMatcher.match("/login", request.getRequestURI())) {
            response.setHeader("primavera", "filter");
            
            if ("POST".equals(request.getMethod())) {
                // 인증 토큰 생성 및 SecurityContext 설정
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        request.getParameter("email"), 
                        request.getParameter("password"));
                
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authToken);
                
                // 세션에 SecurityContext 저장
                HttpSession session = request.getSession(true);
                session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);
            }
        }
        
        // 다음 필터로 제어권 전달
        filterChain.doFilter(request, response);
    }
}
```

#### 필터 체인에서의 역할과 순서
```java
@Configuration
public class ApplicationConfiguration {
    
    @Bean
    public FilterRegistrationBean<PrimaveraFilter> primaveraFilterRegistration() {
        FilterRegistrationBean<PrimaveraFilter> registration = 
            new FilterRegistrationBean<>();
        registration.setFilter(new PrimaveraFilter());
        registration.addUrlPatterns("/login/*", "/users/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
```

### 2. Lucy XSS Filter 통합

#### XSS 필터 설정 및 등록
```yaml
# application.yml
lucy-filter:
  enabled: true
  name: lucyXssFilter
  order: 1
  addUrlPatterns: /*
```

#### Lucy 필터 규칙 설정
```xml
<!-- lucy-xss-servlet-filter-rule.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.nhncorp.com/lucy-xss-servlet-filter">
    <defenders>
        <defender>
            <name>xssEscapeDefender</name>
            <class>com.nhncorp.lucy.security.xss.servletfilter.defender.XssEscapeServletFilterDefender</class>
        </defender>
    </defenders>
    
    <default>
        <defender>xssEscapeDefender</defender>
    </default>
    
    <url-rule-set>
        <url-rule>
            <url disable="true">/static/*</url>
        </url-rule>
        <url-rule>
            <url>/users/*</url>
            <param name="email">
                <defender>xssEscapeDefender</defender>
            </param>
            <param name="nickname">
                <defender>xssEscapeDefender</defender>
            </param>
        </url-rule>
    </url-rule-set>
</config>
```

### 3. SecurityContext와 Authentication 관리

#### 인증 정보 생성 및 관리
```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final UserService userService;

    @PostMapping("/login")
    public String logIn(Model model, HttpSession session, HttpServletResponse response,
                       @RequestParam String email, @RequestParam String password) {
        
        boolean isAuthenticated = userService.signIn(email, password);
        
        if (isAuthenticated) {
            // 사용자 정보를 세션에 저장
            User user = userService.findByEmail(email);
            session.setAttribute("user", user);
            
            // SecurityContext에 인증 정보 저장 (PrimaveraFilter에서 처리)
            response.addHeader("auth", "success");
            model.addAttribute("message", "success");
            return "redirect:/";
        } else {
            model.addAttribute("message", "failure");
            return "login";
        }
    }
}
```

### 4. Chain of Responsibility 패턴 구현

#### 필터 체인의 실행 순서와 책임 전가
```java
// 필터 실행 순서 (낮은 숫자 = 높은 우선순위)
// 1. CharacterEncodingFilter (Spring Boot 기본)
// 2. HiddenHttpMethodFilter (Spring Boot 기본) 
// 3. FormContentFilter (Spring Boot 기본)
// 4. RequestContextFilter (Spring Boot 기본)
// 5. LucyXssFilter (order = 1)
// 6. PrimaveraFilter (order = HIGHEST_PRECEDENCE + 10)
// 7. Spring Security Filter Chain

public class FilterChainAnalysis {
    /*
     * 각 필터의 책임:
     * - CharacterEncodingFilter: 문자 인코딩 설정
     * - LucyXssFilter: XSS 공격 방어
     * - PrimaveraFilter: 커스텀 인증 로직
     * - Spring Security Filters: 보안 검증 및 인가
     */
}
```

### 5. Undertow 웹 서버 최적화

#### Undertow 설정 및 필터 통합
```gradle
// build.gradle
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-web') {
        exclude module: "spring-boot-starter-tomcat"
    }
    implementation 'org.springframework.boot:spring-boot-starter-undertow'
}
```

```yaml
# application-local.yml
server:
  undertow:
    threads:
      io: 4
      worker: 20
    buffer-size: 1024
    direct-buffers: true
```

### 6. 필터 테스트 컨트롤러

#### 필터 동작 확인을 위한 테스트 엔드포인트
```java
@RestController
@RequestMapping("/filter")
@Slf4j
public class FilterController {

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testFilter(HttpServletRequest request) {
        
        // 요청 헤더에서 필터 실행 여부 확인
        String primaveraHeader = request.getHeader("primavera");
        
        // SecurityContext에서 인증 정보 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> result = Map.of(
            "filterExecuted", primaveraHeader != null,
            "headerValue", primaveraHeader != null ? primaveraHeader : "none",
            "authenticationExists", auth != null,
            "principal", auth != null ? auth.getName() : "anonymous",
            "requestUri", request.getRequestURI(),
            "method", request.getMethod()
        );
        
        log.info("Filter test result: {}", result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/xss-test")
    public ResponseEntity<Map<String, Object>> testXssFilter(
            @RequestParam String userInput,
            @RequestParam(required = false) String email) {
        
        // Lucy Filter에 의해 XSS 스크립트가 무력화되었는지 확인
        Map<String, Object> result = Map.of(
            "originalInput", userInput,
            "processedInput", userInput, // Lucy Filter에 의해 이스케이프 처리됨
            "email", email != null ? email : "none",
            "xssFiltered", !userInput.contains("<script>") || userInput.contains("&lt;script&gt;")
        );
        
        return ResponseEntity.ok(result);
    }
}
```

## 기술 스택

### 핵심 기술
- **Spring Boot**: 3.3.6
- **Spring Security**: 6.4.4 (필터 체인 및 보안)
- **MyBatis**: 3.0.4 (데이터 접근)
- **Thymeleaf**: 3.4.0 (템플릿 엔진)

### 웹 서버 및 보안
- **Undertow**: 고성능 웹 서버 (Tomcat 대신 사용)
- **Lucy XSS Filter**: NHN의 XSS 방어 라이브러리
- **Spring Security Filter Chain**: 보안 필터 체인

### UI 및 정적 리소스
- **AdminLTE**: 3.x (관리자 템플릿)
- **Bootstrap**: 4.x (UI 프레임워크)

## 실행 방법

### 1. 로컬 환경 실행
```bash
# MariaDB 실행 (Docker)
./docker-manager.sh start chap08

# 애플리케이션 실행
./gradlew :chap08:bootRun -Dspring.profiles.active=local
```

### 2. 필터 체인 테스트

#### 기본 필터 동작 확인
```bash
# 메인 페이지 접속 (필터 체인 통과)
curl -v http://localhost:8080/

# 로그인 페이지 접속 (PrimaveraFilter 실행)
curl -v http://localhost:8080/login

# 필터 테스트 엔드포인트
curl -v http://localhost:8080/filter/test
```

#### 로그인 프로세스 테스트 (POST)
```bash
# 로그인 시도 (PrimaveraFilter에서 SecurityContext 생성)
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=genius@gmail.com&password=genius" \
  -v

# 응답 헤더에서 'primavera: filter' 확인
```

#### XSS 필터 테스트
```bash
# XSS 공격 시도 (Lucy Filter가 자동 차단)
curl -X POST http://localhost:8080/filter/xss-test \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "userInput=<script>alert('XSS Attack')</script>&email=test@example.com"

# 예상 결과: 스크립트 태그가 이스케이프 처리됨
# <script> → &lt;script&gt;
```

### 3. 웹 페이지 테스트
```bash
# 브라우저에서 접속하여 UI 테스트
open http://localhost:8080

# 로그인 페이지에서 필터 동작 확인
open http://localhost:8080/login

# 개발자 도구에서 Network 탭으로 응답 헤더 'primavera: filter' 확인
```

## 핵심 학습 포인트

### 1. Spring Security 필터 체인 구조
```java
// OncePerRequestFilter를 상속하여 요청당 한 번만 실행되는 필터 구현
public class PrimaveraFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        // 1. 요청 전 처리
        // 2. 다음 필터로 제어권 전달
        filterChain.doFilter(request, response);
        // 3. 응답 후 처리 (필요시)
    }
}
```

### 2. SecurityContext 관리 패턴
```java
// 인증 정보 생성 및 SecurityContext 설정
UsernamePasswordAuthenticationToken authToken = 
    new UsernamePasswordAuthenticationToken(username, password);

SecurityContext securityContext = SecurityContextHolder.getContext();
securityContext.setAuthentication(authToken);

// 세션에 SecurityContext 저장 (세션 기반 인증)
HttpSession session = request.getSession(true);
session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                    securityContext);
```

### 3. 필터 등록 및 순서 관리
```java
@Bean
public FilterRegistrationBean<CustomFilter> customFilterRegistration() {
    FilterRegistrationBean<CustomFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new CustomFilter());
    registration.addUrlPatterns("/api/*", "/admin/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // 실행 순서 조정
    return registration;
}
```

### 4. Chain of Responsibility 패턴 활용
```java
// 각 필터는 자신의 책임을 수행한 후 다음 필터에게 제어권 전달
public void doFilter(ServletRequest request, ServletResponse response, 
                    FilterChain chain) {
    // 1. 현재 필터의 책임 수행
    if (shouldProcess(request)) {
        processRequest(request);
    }
    
    // 2. 다음 필터로 제어권 전달
    chain.doFilter(request, response);
    
    // 3. 후처리 (필요시)
    postProcess(response);
}
```

## 테스트 실행

### 단위 테스트
```bash
# 전체 테스트 실행
./gradlew :chap08:test

# 필터 테스트만 실행
./gradlew :chap08:test --tests PrimaveraFilterTest
```

### 통합 테스트
```bash
# 웹 계층 통합 테스트 (필터 체인 포함)
./gradlew :chap08:test --tests "*IntegrationTest"
```

### 보안 테스트 (수동)
1. **XSS 방어 테스트**: 브라우저에서 스크립트 입력 시도
2. **필터 체인 순서 확인**: 로그에서 필터 실행 순서 검증
3. **인증 플로우 테스트**: 로그인 → SecurityContext 생성 → 세션 저장 확인

## 학습 순서

### 1단계: Spring Security 필터 기초
1. Servlet Filter 인터페이스 이해
2. OncePerRequestFilter 상속 구현
3. FilterChain의 역할과 doFilter 메서드

### 2단계: 커스텀 필터 개발
1. PrimaveraFilter 구현 및 등록
2. AntPathMatcher를 활용한 URL 패턴 매칭
3. 요청/응답 헤더 조작

### 3단계: SecurityContext 관리
1. Authentication 객체 생성
2. SecurityContextHolder 활용
3. 세션 기반 인증 구현

### 4단계: 필터 체인 통합
1. Lucy XSS Filter와의 협력
2. Spring Boot 기본 필터들과의 순서 조정
3. Chain of Responsibility 패턴 적용

### 5단계: 보안 테스트 및 검증
1. 필터 동작 여부 확인
2. XSS 공격 차단 검증
3. 인증 플로우 테스트

## 주요 애너테이션

### Spring Security 관련
- `@EnableWebSecurity`: 웹 보안 설정 활성화 (다음 챕터에서 활용)
- `@Component`: 필터를 Spring Bean으로 등록

### 필터 관련
- `OncePerRequestFilter`: 요청당 한 번 실행되는 필터 기반 클래스
- `FilterRegistrationBean`: 필터 등록 및 설정

### 컨트롤러 관련
- `@RestController`: REST API 컨트롤러
- `@RequestParam`: 요청 파라미터 바인딩

### 테스트 관련
- `@SpringBootTest`: 통합 테스트 환경
- `@TestContainers`: 테스트 컨테이너 사용

## 다음 단계 안내

**chap09**에서는 Spring Security의 고급 기능과 권한 기반 접근 제어를 학습하게 됩니다:

### 예상 학습 내용
- Spring Security 설정 고도화
- 권한 기반 접근 제어 (RBAC)
- OAuth2 및 소셜 로그인 연동
- 메소드 레벨 보안 적용
- JWT 토큰 기반 인증

### 연계 학습 포인트
- 커스텀 필터 → Spring Security 표준 필터 활용
- SecurityContext 관리 → 인증/인가 체계 구축
- 필터 체인 → 보안 설정 및 정책 적용
- XSS 방어 → 종합적인 웹 보안 구현

이 모듈을 통해 Spring Security의 필터 아키텍처와 보안 필터 개발의 핵심 개념을 체득할 수 있습니다.