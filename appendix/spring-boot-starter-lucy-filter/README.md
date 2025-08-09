# Spring Boot Starter Lucy Filter

Jakarta EE 9+ 환경을 지원하는 Lucy XSS Filter의 Spring Boot 3.x 커스텀 스타터입니다. NHN의 Lucy XSS Filter를 Spring Boot 3.x와 호환되도록 포팅하고, Auto Configuration을 통해 손쉬운 통합을 제공합니다.

## 학습 목표

- **커스텀 스타터 제작**: Spring Boot Auto Configuration 원리 이해
- **Jakarta EE 마이그레이션**: javax.servlet에서 jakarta.servlet로의 포팅 과정
- **XSS 방어 시스템**: 웹 애플리케이션 보안 강화 방법 학습
- **필터 체인 통합**: Servlet Filter의 Spring Boot 통합 방법

## 배경 및 필요성

### Jakarta EE 전환 문제
Spring Boot 3.x부터 Jakarta EE 9+로 전환되면서 패키지명이 변경되었습니다:
```java
// 기존 (Spring Boot 2.x)
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;

// 변경 (Spring Boot 3.x)
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
```

기존 Lucy XSS Filter는 javax.servlet 패키지를 사용하여 Spring Boot 3.x 환경에서 직접 사용할 수 없는 문제가 발생했습니다.

## 프로젝트 구조

```
src/main/java/com/genius/primavera/lucy/
├── config/                                    # 자동 설정 클래스
│   ├── LucyFilterAutoConfiguration.java       # 메인 자동 설정
│   ├── LucyFilterApplicationConfiguration.java # 애플리케이션 설정
│   └── LucyFilterApplicationConfigurationAdapter.java # 설정 어댑터
├── properties/                                # 설정 프로퍼티
│   └── LucyFilterDelegatingProperties.java    # 설정 바인딩 클래스
├── servlet/                                  # 서블릿 필터
│   └── XssEscapeServletFilter.java          # Jakarta EE 호환 XSS 필터
├── defender/                                # 방어 구현체
│   ├── XssPreventerDefender.java           # 기본 XSS 방어기
│   └── XssSaxFilterDefender.java           # SAX 기반 방어기
└── annotation/                              # 애너테이션
    └── EnableLucyFilter.java                # 필터 활성화 애너테이션

src/main/resources/
├── META-INF/
│   ├── spring.factories                     # Auto Configuration 등록
│   └── spring.provides                      # 제공 기능 명시
├── lucy-xss-default.xml                     # 기본 XSS 설정
├── lucy-xss-default-sax.xml                 # SAX 필터 설정
└── lucy-xss-servlet-filter-rule.xml         # 서블릿 필터 규칙
```

## 주요 기능

### 1. Auto Configuration
```java
@Configuration
@ConditionalOnClass(XssEscapeServletFilter.class)
@EnableConfigurationProperties(LucyFilterDelegatingProperties.class)
@ConditionalOnProperty(prefix = "spring.lucy-filter", name = "enabled", 
                      havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(DispatcherServletAutoConfiguration.class)
@AutoConfigureBefore(SecurityAutoConfiguration.class)
public class LucyFilterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XssEscapeServletFilter xssEscapeServletFilter() {
        return new XssEscapeServletFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<XssEscapeServletFilter> lucyFilterRegistration(
            XssEscapeServletFilter xssEscapeServletFilter,
            LucyFilterDelegatingProperties properties) {
        
        FilterRegistrationBean<XssEscapeServletFilter> registration = 
            new FilterRegistrationBean<>();
        registration.setFilter(xssEscapeServletFilter);
        registration.setName(properties.getName());
        registration.setOrder(properties.getOrder());
        registration.addUrlPatterns(properties.getAddUrlPatterns()
            .toArray(new String[0]));
        
        return registration;
    }
}
```

### 2. Configuration Properties
```java
@ConfigurationProperties(prefix = "spring.lucy-filter")
@Data
public class LucyFilterDelegatingProperties {
    
    /**
     * Lucy XSS Filter 활성화 여부
     */
    private boolean enabled = true;
    
    /**
     * 필터 이름
     */
    private String name = "lucyXssEscapeServletFilter";
    
    /**
     * 필터 실행 순서 (낮을수록 먼저 실행)
     */
    private int order = Ordered.LOWEST_PRECEDENCE - 100;
    
    /**
     * 필터가 적용될 URL 패턴 목록
     */
    private List<String> addUrlPatterns = Arrays.asList("/*");
    
    /**
     * 필터에서 제외할 URL 패턴 목록
     */
    private List<String> excludePatterns = new ArrayList<>();
}
```

### 3. Jakarta EE 호환 XSS 필터
```java
public class XssEscapeServletFilter implements Filter {
    
    private boolean isInitialized = false;
    private XssFilterConfiguration configuration;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            this.configuration = XssFilterConfigurationProvider
                .getConfiguration(filterConfig.getServletContext());
            this.isInitialized = true;
            
            log.info("Lucy XSS Filter 초기화 완료 - 설정 파일: {}", 
                    configuration.getConfigFile());
                    
        } catch (Exception e) {
            log.error("Lucy XSS Filter 초기화 실패", e);
            throw new ServletException("XSS Filter 초기화 실패", e);
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        if (!isInitialized) {
            chain.doFilter(request, response);
            return;
        }
        
        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        String requestUri = httpRequest.getRequestURI();
        XssFilterRule rule = configuration.getRule(requestUri);
        
        if (rule.isDisabled()) {
            chain.doFilter(request, response);
            return;
        }
        
        // XSS 방어가 적용된 Request Wrapper 생성
        HttpServletRequest wrappedRequest = new XssFilterRequestWrapper(
            httpRequest, rule.getDefender());
            
        chain.doFilter(wrappedRequest, response);
    }
    
    @Override
    public void destroy() {
        log.info("Lucy XSS Filter 종료");
        this.isInitialized = false;
        this.configuration = null;
    }
}
```

### 4. XSS 방어 Request Wrapper
```java
public class XssFilterRequestWrapper extends HttpServletRequestWrapper {
    
    private final XssDefender defender;
    
    public XssFilterRequestWrapper(HttpServletRequest request, XssDefender defender) {
        super(request);
        this.defender = defender;
    }
    
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return value != null ? defender.defend(value, getRequestURI()) : null;
    }
    
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        
        return Arrays.stream(values)
            .map(value -> defender.defend(value, getRequestURI()))
            .toArray(String[]::new);
    }
    
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalMap = super.getParameterMap();
        Map<String, String[]> defendedMap = new HashMap<>();
        
        originalMap.forEach((key, values) -> {
            String[] defendedValues = Arrays.stream(values)
                .map(value -> defender.defend(value, getRequestURI()))
                .toArray(String[]::new);
            defendedMap.put(key, defendedValues);
        });
        
        return defendedMap;
    }
    
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        // 특정 헤더에 대해서만 XSS 방어 적용
        if (DEFENSIVE_HEADERS.contains(name.toLowerCase())) {
            return value != null ? defender.defend(value, getRequestURI()) : null;
        }
        return value;
    }
    
    private static final Set<String> DEFENSIVE_HEADERS = Set.of(
        "user-agent", "referer", "x-requested-with"
    );
}
```

### 5. @EnableLucyFilter 애너테이션
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LucyFilterApplicationConfiguration.class)
public @interface EnableLucyFilter {
    
    /**
     * 필터 활성화 여부
     */
    boolean enabled() default true;
    
    /**
     * 필터 실행 순서
     */
    int order() default Ordered.LOWEST_PRECEDENCE - 100;
    
    /**
     * 적용할 URL 패턴
     */
    String[] urlPatterns() default {"/*"};
}
```

## 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| **Spring Boot** | 3.3.6 | 기본 프레임워크 |
| **Jakarta Servlet API** | 6.0+ | 서블릿 API |
| **Lucy XSS Filter Core** | Custom Port | XSS 방어 핵심 로직 |
| **Spring Boot Auto Configuration** | 3.3.6 | 자동 설정 기능 |

## 사용 방법

### 1. 의존성 추가

#### Gradle
```gradle
dependencies {
    implementation project(':appendix:spring-boot-starter-lucy-filter')
}
```

#### Maven
```xml
<dependency>
    <groupId>com.genius.primavera</groupId>
    <artifactId>spring-boot-starter-lucy-filter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 애플리케이션 설정

#### 방법 1: 자동 활성화 (기본)
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        // Lucy Filter가 자동으로 활성화됩니다
    }
}
```

#### 방법 2: @EnableLucyFilter 애너테이션 사용
```java
@SpringBootApplication
@EnableLucyFilter(
    enabled = true,
    order = 1,
    urlPatterns = {"/*", "/api/*"}
)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. 설정 파일

#### application.yml
```yaml
spring:
  lucy-filter:
    enabled: true
    name: "lucyXssEscapeServletFilter"
    order: 1
    add-url-patterns:
      - "/*"
      - "/api/*"
      - "/admin/*"
    exclude-patterns:
      - "/static/**"
      - "/css/**"
      - "/js/**"
      - "/images/**"
```

#### application.properties
```properties
# Lucy Filter 기본 설정
spring.lucy-filter.enabled=true
spring.lucy-filter.name=lucyXssEscapeServletFilter
spring.lucy-filter.order=1

# URL 패턴 설정
spring.lucy-filter.add-url-patterns[0]=/*
spring.lucy-filter.add-url-patterns[1]=/api/*
spring.lucy-filter.add-url-patterns[2]=/admin/*

# 제외 패턴 설정
spring.lucy-filter.exclude-patterns[0]=/static/**
spring.lucy-filter.exclude-patterns[1]=/webjars/**
```

### 4. 고급 설정: lucy-xss-servlet-filter-rule.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.navercorp.com/lucy-xss-servlet">
    <defenders>
        <!-- 기본 XSS 방어기 -->
        <defender>
            <name>xssPreventerDefender</name>
            <class>com.genius.primavera.lucy.defender.XssPreventerDefender</class>
        </defender>
        
        <!-- SAX 기반 방어기 -->
        <defender>
            <name>xssSaxFilterDefender</name>
            <class>com.genius.primavera.lucy.defender.XssSaxFilterDefender</class>
            <init-param>
                <param-value>lucy-xss-default-sax.xml</param-value>
            </init-param>
        </defender>
    </defenders>

    <!-- 기본 방어 정책 -->
    <default>
        <defender>xssPreventerDefender</defender>
    </default>
    
    <!-- URL별 세부 규칙 -->
    <url-rule-set>
        <!-- API 엔드포인트 - 엄격한 필터링 -->
        <url-rule>
            <url disable="false">/api/**</url>
            <defender>xssSaxFilterDefender</defender>
        </url-rule>
        
        <!-- 관리자 영역 - 매우 엄격한 필터링 -->
        <url-rule>
            <url disable="false">/admin/**</url>
            <defender>xssSaxFilterDefender</defender>
        </url-rule>
        
        <!-- 정적 리소스 - 필터링 제외 -->
        <url-rule>
            <url disable="true">/static/**</url>
        </url-rule>
        
        <url-rule>
            <url disable="true">/css/**</url>
        </url-rule>
        
        <url-rule>
            <url disable="true">/js/**</url>
        </url-rule>
        
        <!-- Actuator 엔드포인트 - 필터링 제외 -->
        <url-rule>
            <url disable="true">/actuator/**</url>
        </url-rule>
    </url-rule-set>
</config>
```

## 실제 사용 예시

### 1. 컨트롤러에서 자동 필터링 확인
```java
@RestController
@RequestMapping("/api/test")
public class XssTestController {
    
    @PostMapping("/echo")
    public ResponseEntity<Map<String, String>> echo(
            @RequestBody Map<String, String> request) {
        
        // Lucy Filter가 자동으로 XSS 공격 코드를 필터링함
        String userInput = request.get("content");
        
        Map<String, String> response = new HashMap<>();
        response.put("original_received", userInput);
        response.put("processed", userInput); // 이미 필터링된 상태
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/param")
    public ResponseEntity<String> testParam(@RequestParam String data) {
        // 쿼리 파라미터도 자동으로 필터링됨
        return ResponseEntity.ok("받은 데이터: " + data);
    }
}
```

### 2. XSS 공격 테스트
```bash
# 악성 스크립트가 포함된 요청
curl -X POST http://localhost:8080/api/test/echo \
  -H "Content-Type: application/json" \
  -d '{
    "content": "<script>alert(\"XSS Attack!\")</script>Hello World"
  }'

# 응답 (자동으로 필터링됨)
{
  "original_received": "&lt;script&gt;alert(&quot;XSS Attack!&quot;)&lt;/script&gt;Hello World",
  "processed": "&lt;script&gt;alert(&quot;XSS Attack!&quot;)&lt;/script&gt;Hello World"
}

# 쿼리 파라미터 XSS 테스트
curl "http://localhost:8080/api/test/param?data=<img src=x onerror=alert(1)>"

# 응답
받은 데이터: &lt;img src=x&gt;
```

## 핵심 학습 포인트

### 1. Spring Boot Auto Configuration 패턴
- **@ConditionalOnClass**: 클래스 존재 여부에 따른 조건부 설정
- **@ConditionalOnProperty**: 프로퍼티 값에 따른 조건부 활성화
- **@EnableConfigurationProperties**: 타입 안전한 설정 바인딩
- **spring.factories**: Auto Configuration 클래스 등록 메커니즘

### 2. Jakarta EE 마이그레이션 전략
- **패키지 포팅**: javax.servlet → jakarta.servlet 변환
- **API 호환성**: 기존 API 시그니처 유지하면서 내부 구현 변경
- **설정 호환성**: 기존 XML 설정 파일 포맷 유지
- **기능 호환성**: 기존 기능과 동일한 XSS 방어 효과

### 3. Servlet Filter 통합 패턴
- **FilterRegistrationBean**: Spring Boot에서 Filter 등록 방법
- **Filter 순서 관리**: @Order를 통한 Filter Chain 순서 제어
- **URL 패턴 매핑**: 특정 경로에만 Filter 적용하는 방법
- **예외 처리**: Filter에서 발생하는 예외의 적절한 처리

### 4. XSS 방어 메커니즘
- **입력 필터링**: 사용자 입력에서 악성 스크립트 제거
- **화이트리스트 방식**: 허용된 태그와 속성만 통과
- **컨텍스트 인식**: URL 경로별 다른 방어 정책 적용
- **성능 최적화**: 불필요한 처리를 피하는 효율적 필터링

## 테스트

### 단위 테스트
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.lucy-filter.enabled=true",
    "spring.lucy-filter.add-url-patterns[0]=/*"
})
class LucyFilterAutoConfigurationTest {
    
    @Autowired
    private XssEscapeServletFilter xssFilter;
    
    @Test
    void shouldCreateLucyFilterBean() {
        assertThat(xssFilter).isNotNull();
    }
    
    @Test
    void shouldFilterXssAttack() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");
        request.addParameter("data", "<script>alert('xss')</script>");
        
        XssFilterRequestWrapper wrapper = new XssFilterRequestWrapper(
            request, new XssPreventerDefender());
        
        String filteredValue = wrapper.getParameter("data");
        assertThat(filteredValue).doesNotContain("<script>");
        assertThat(filteredValue).contains("&lt;script&gt;");
    }
}
```

### 통합 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class LucyFilterIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldFilterXssInPostRequest() throws Exception {
        String xssPayload = "{\"content\": \"<script>alert('xss')</script>\"}";
        
        mockMvc.perform(post("/api/test/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(xssPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed")
                    .value(not(containsString("<script>"))));
    }
    
    @Test
    void shouldFilterXssInQueryParam() throws Exception {
        mockMvc.perform(get("/api/test/param")
                .param("data", "<img src=x onerror=alert(1)>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<img"))));
    }
}
```

## 성능 고려사항

### 1. 필터 순서 최적화
```yaml
spring:
  lucy-filter:
    order: 1  # 다른 보안 필터보다 먼저 실행
```

### 2. 제외 패턴 설정
```yaml
spring:
  lucy-filter:
    exclude-patterns:
      - "/static/**"    # 정적 리소스 제외
      - "/actuator/**"  # 모니터링 엔드포인트 제외
      - "/api/health"   # 헬스체크 제외
```

### 3. 방어기 선택
- **XssPreventerDefender**: 빠른 성능, 기본적인 XSS 방어
- **XssSaxFilterDefender**: 정교한 필터링, 약간의 성능 오버헤드

## 활용 방법

### 1. 개발 환경에서 테스트
```yaml
# application-dev.yml
spring:
  lucy-filter:
    enabled: true
logging:
  level:
    com.genius.primavera.lucy: DEBUG  # 필터 동작 로그 확인
```

### 2. 운영 환경 최적화
```yaml
# application-prod.yml
spring:
  lucy-filter:
    enabled: true
    order: 1
    exclude-patterns:
      - "/static/**"
      - "/webjars/**"
      - "/favicon.ico"
```

이 커스텀 스타터는 Jakarta EE 전환 문제를 해결하면서도 강력한 XSS 방어 기능을 제공하며, Spring Boot 3.x 환경에서 Lucy XSS Filter를 손쉽게 사용할 수 있게 해줍니다.