# Spring Boot Starter Lucy Filter 🛡️

## 📋 개요

**Spring Boot Starter Lucy Filter**는 NHN(구 NHN Corporation)에서 개발한 **Lucy XSS Filter**를 Spring Boot 3.x (Jakarta EE 9+) 환경에서 손쉽게 사용할 수 있도록 만든 **커스텀 Spring Boot Starter**입니다.

## 🎯 개발 배경 및 필요성

### 1. **Jakarta EE 전환 문제**

Spring Boot 3.x와 Spring 6.x부터 **Jakarta EE 9+**로 전환되면서 패키지명이 변경되었습니다:

```
기존: javax.servlet.*
변경: jakarta.servlet.*
```

### 2. **Lucy XSS Filter 호환성 문제**

기존 Lucy XSS Filter는 **javax.servlet** 패키지를 사용하여 개발되었기 때문에:
- Spring Boot 3.x 환경에서 직접 사용 불가
- ClassNotFoundException 발생
- 의존성 충돌 문제

### 3. **보안 필요성**

웹 애플리케이션에서 XSS(Cross-Site Scripting) 공격 방어는 필수적입니다:
- 사용자 입력 데이터의 악성 스크립트 필터링
- HTML 태그 및 속성 화이트리스트 기반 필터링
- 안전한 콘텐츠만 허용하는 보안 체계

### 4. **Spring Boot Starter의 장점**

- **Auto Configuration**: 복잡한 설정 없이 자동 구성
- **Configuration Properties**: application.yml을 통한 쉬운 설정
- **조건부 빈 등록**: 필요할 때만 활성화
- **Spring Boot 생태계 통합**: 기존 프로젝트에 쉽게 추가

## 🛠️ 기술적 해결 방안

### 1. **패키지 포팅 (Package Porting)**

기존 Lucy XSS Filter의 핵심 로직을 **jakarta.servlet**로 포팅:

```java
// 기존 (javax.servlet)
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;

// 포팅 후 (jakarta.servlet)  
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
```

### 2. **Spring Boot Auto Configuration 구현**

```java
@Configuration
@ConditionalOnClass(XssEscapeServletFilter.class)
@EnableConfigurationProperties(LucyFilterDelegatingProperties.class)
@ConditionalOnProperty(prefix = "spring.lucy-filter", name = "enabled", 
                      havingValue = "true", matchIfMissing = true)
public class LucyFilterAutoConfiguration {
    // 자동 설정 로직
}
```

### 3. **FilterRegistrationBean 자동 등록**

```java
private FilterRegistrationBean<XssEscapeServletFilter> filterRegistrationBean() {
    FilterRegistrationBean<XssEscapeServletFilter> filterRegistration = 
        new FilterRegistrationBean<>();
    filterRegistration.setFilter(xssEscapeServletFilter);
    filterRegistration.setName(properties.getName());
    filterRegistration.setOrder(properties.getOrder());
    filterRegistration.addUrlPatterns(properties.getAddUrlPatterns());
    return filterRegistration;
}
```

## 🚀 주요 기능

### 1. **XSS 공격 방어**
- 악성 스크립트 태그 필터링
- HTML 태그 화이트리스트 기반 허용
- 속성값 검증 및 정화

### 2. **유연한 설정**
- XML 기반 세밀한 필터링 규칙 설정
- 허용할 HTML 태그 및 속성 커스터마이징
- URL 패턴별 필터 적용/제외

### 3. **Spring Boot 통합**
- Auto Configuration을 통한 자동 설정
- @EnableLucyFilter 어노테이션 지원
- Configuration Properties 바인딩

## 📦 사용법

### 1. **의존성 추가**

#### Gradle
```gradle
dependencies {
    implementation project(":appendix:spring-boot-starter-lucy-filter")
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

### 2. **애플리케이션 설정**

#### 방법 1: @EnableLucyFilter 어노테이션 사용

```java
@SpringBootApplication
@EnableLucyFilter  // Lucy XSS Filter 활성화
public class PrimaveraApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrimaveraApplication.class, args);
    }
}
```

#### 방법 2: Auto Configuration (자동 설정)

별도 설정 없이 **자동으로 활성화**됩니다. (기본값: enabled = true)

### 3. **Configuration Properties 설정**

#### application.yml 설정

```yaml
spring:
  lucy-filter:
    enabled: true                    # Lucy Filter 활성화 (기본값: true)
    name: "lucyXssEscapeServletFilter"  # 필터 이름
    order: 1                        # 필터 실행 순서 (낮을수록 먼저 실행)
    add-url-patterns:               # 필터 적용 URL 패턴
      - "/*"                        # 모든 URL에 적용
      - "/api/*"                    # API 경로에 적용
      - "/admin/*"                  # 관리자 경로에 적용
```

#### application.properties 설정

```properties
# Lucy Filter 기본 설정
spring.lucy-filter.enabled=true
spring.lucy-filter.name=lucyXssEscapeServletFilter
spring.lucy-filter.order=1

# URL 패턴 설정
spring.lucy-filter.add-url-patterns[0]=/*
spring.lucy-filter.add-url-patterns[1]=/api/*
spring.lucy-filter.add-url-patterns[2]=/admin/*
```

### 4. **고급 설정 - lucy-xss-servlet-filter-rule.xml**

세밀한 XSS 필터링 규칙을 설정하려면 `lucy-xss-servlet-filter-rule.xml` 파일을 생성:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.navercorp.com/lucy-xss-servlet">
    <defenders>
        <!-- XssPreventer 사용 (기본) -->
        <defender>
            <name>xssPreventerDefender</name>
            <class>com.navercorp.lucy.security.xss.servletfilter.defender.XssPreventerDefender</class>
        </defender>
        
        <!-- XssSaxFilter 사용 (고급) -->
        <defender>
            <name>xssSaxFilterDefender</name>
            <class>com.navercorp.lucy.security.xss.servletfilter.defender.XssSaxFilterDefender</class>
            <init-param>
                <param-value>lucy-xss-default-sax.xml</param-value>
            </init-param>
        </defender>
    </defenders>

    <!-- URL별 필터 규칙 -->
    <default>
        <defender>xssPreventerDefender</defender>
    </default>
    
    <!-- 특정 URL 패턴에 대한 세부 설정 -->
    <url-rule-set>
        
        <!-- 관리자 페이지 - 엄격한 필터링 -->
        <url-rule>
            <url disable="false">/admin/*</url>
            <defender>xssSaxFilterDefender</defender>
        </url-rule>
        
        <!-- API 엔드포인트 - 기본 필터링 -->
        <url-rule>
            <url disable="false">/api/*</url>
            <defender>xssPreventerDefender</defender>
        </url-rule>
        
        <!-- 정적 리소스 - 필터링 제외 -->
        <url-rule>
            <url disable="true">/static/*</url>
        </url-rule>
        
        <url-rule>
            <url disable="true">/css/*</url>
        </url-rule>
        
        <url-rule>
            <url disable="true">/js/*</url>
        </url-rule>
        
        <url-rule>
            <url disable="true">/images/*</url>
        </url-rule>
        
    </url-rule-set>
</config>
```

## 🔧 실제 적용 예시

### 1. **컨트롤러에서 XSS 필터링 확인**

```java
@RestController
@RequestMapping("/api/test")
public class XssTestController {
    
    @PostMapping("/echo")
    public ResponseEntity<Map<String, String>> echo(@RequestBody Map<String, String> request) {
        String userInput = request.get("content");
        
        // Lucy Filter가 자동으로 XSS 공격 코드를 필터링함
        // 예: <script>alert('xss')</script> → &lt;script&gt;alert('xss')&lt;/script&gt;
        
        Map<String, String> response = new HashMap<>();
        response.put("original", userInput);
        response.put("filtered", userInput); // 이미 필터링된 상태
        
        return ResponseEntity.ok(response);
    }
}
```

### 2. **테스트 예시**

```bash
# XSS 공격 코드 포함된 요청
curl -X POST http://localhost:8080/api/test/echo \
  -H "Content-Type: application/json" \
  -d '{"content": "<script>alert(\"XSS Attack\")</script>Hello World"}'

# 응답 (자동으로 필터링됨)
{
  "original": "&lt;script&gt;alert(&quot;XSS Attack&quot;)&lt;/script&gt;Hello World",
  "filtered": "&lt;script&gt;alert(&quot;XSS Attack&quot;)&lt;/script&gt;Hello World"
}
```

### 3. **로그 확인**

```
2024-01-20 10:30:15.123 INFO  --- LucyFilterConfiguration
2024-01-20 10:30:15.125 INFO  --- LucyFilterApplicationConfiguration  
2024-01-20 10:30:15.127 INFO  --- LucyFilterApplicationConfigurationAdapter
2024-01-20 10:30:15.129 INFO  --- FilterRegistrationBean : {...}
```

## 🛡️ 보안 효과

### 1. **XSS 공격 차단**

| 입력 | 필터링 후 출력 |
|------|---------------|
| `<script>alert('xss')</script>` | `&lt;script&gt;alert('xss')&lt;/script&gt;` |
| `<img src="x" onerror="alert(1)">` | `<img src="x">` |
| `javascript:alert(1)` | `alert(1)` |
| `<iframe src="malicious.com">` | `(제거됨)` |

### 2. **허용되는 안전한 태그**

```html
<!-- 허용되는 기본 HTML 태그들 -->
<p>안전한 문단</p>
<div>안전한 영역</div>
<b>굵은 글씨</b>
<i>기울임 글씨</i>
<ul><li>목록</li></ul>
<a href="https://safe-site.com">안전한 링크</a>
```

## 📊 성능 고려사항

### 1. **필터 실행 순서**

```yaml
spring:
  lucy-filter:
    order: 1  # 다른 보안 필터보다 먼저 실행되도록 설정
```

### 2. **제외 URL 패턴 최적화**

```xml
<!-- 성능 최적화를 위해 정적 리소스는 필터링 제외 -->
<url-rule>
    <url disable="true">/static/*</url>
</url-rule>
<url-rule>
    <url disable="true">/webjars/*</url>
</url-rule>
<url-rule>
    <url disable="true">/actuator/*</url>
</url-rule>
```

## 🧪 테스트 가이드

### 1. **단위 테스트**

```java
@SpringBootTest
@AutoConfigureTestDatabase
class LucyFilterAutoConfigurationTest {
    
    @Autowired
    private XssEscapeServletFilter xssEscapeServletFilter;
    
    @Test
    @DisplayName("Lucy Filter가 정상적으로 Bean으로 등록되는지 확인")
    void shouldCreateLucyFilterBean() {
        assertThat(xssEscapeServletFilter).isNotNull();
    }
}
```

### 2. **통합 테스트**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class XssFilterIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("XSS 공격 코드가 필터링되는지 확인")
    void shouldFilterXssAttack() throws Exception {
        String xssPayload = "<script>alert('xss')</script>";
        
        mockMvc.perform(post("/api/test/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + xssPayload + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filtered").value(not(containsString("<script>"))));
    }
}
```

## 📚 참고 자료

### 1. **Lucy XSS Filter 공식 문서**
- [GitHub - Lucy XSS Filter](https://github.com/naver/lucy-xss-filter)
- [Lucy XSS Filter 설정 가이드](https://github.com/naver/lucy-xss-filter/wiki)

### 2. **Spring Boot Auto Configuration**
- [Spring Boot Auto Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
- [Creating Your Own Starter](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration.custom-starter)

### 3. **웹 보안 참고자료**
- [OWASP XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [Jakarta EE Migration Guide](https://eclipse-ee4j.github.io/jakartaee-platform/jakartaee9/JakartaEEMigrationGuide)

## 🔄 업그레이드 가이드

### 기존 Lucy Filter에서 마이그레이션

#### 1. **기존 설정 제거**

```xml
<!-- 기존 web.xml 설정 제거 -->
<filter>
    <filter-name>XSSFilter</filter-name>
    <filter-class>com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter</filter-class>
</filter>
```

#### 2. **새로운 Starter 적용**

```gradle
// 기존 의존성 제거
// implementation 'com.navercorp.lucy:lucy-xss-servlet:2.0.1'

// 새로운 starter 추가
implementation project(":appendix:spring-boot-starter-lucy-filter")
```

#### 3. **설정 파일 업데이트**

```yaml
# 기존 설정을 Spring Boot Properties로 변환
spring:
  lucy-filter:
    enabled: true
    name: "lucyXssEscapeServletFilter"
    order: 1
    add-url-patterns: ["/*"]
```

## 🚀 확장 기능

### 1. **커스텀 Defender 구현**

```java
@Component
public class CustomXssDefender implements Defender {
    
    @Override
    public String defend(String dirty, String uri) {
        // 커스텀 XSS 필터링 로직 구현
        return customClean(dirty);
    }
    
    private String customClean(String input) {
        // 비즈니스 요구사항에 맞는 필터링 로직
        return input.replaceAll("<script[^>]*>.*?</script>", "");
    }
}
```

### 2. **동적 설정 변경**

```java
@RestController
@RequestMapping("/admin/security")
public class SecurityConfigController {
    
    @Autowired
    private LucyFilterDelegatingProperties properties;
    
    @PostMapping("/lucy-filter/toggle")
    public ResponseEntity<String> toggleLucyFilter(@RequestParam boolean enabled) {
        // 런타임에 Lucy Filter 설정 변경 (재시작 필요)
        return ResponseEntity.ok("Lucy Filter enabled: " + enabled);
    }
}
```

---

**🎓 핵심 포인트**: 이 커스텀 Spring Boot Starter는 Jakarta EE 전환 문제를 해결하면서도 Lucy XSS Filter의 강력한 보안 기능을 그대로 제공합니다. Auto Configuration을 통해 복잡한 설정 없이 XSS 공격을 효과적으로 방어할 수 있습니다.
