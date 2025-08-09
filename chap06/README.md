# Chapter 06 - Validation & Security & XSS Protection

입력 데이터 검증, 기본 보안 설정, XSS 공격 방어를 통합한 안전한 웹 애플리케이션을 구축합니다. Bean Validation, Spring Security, Lucy XSS Filter를 조합하여 다계층 보안 시스템을 구현하고, Thymeleaf를 활용한 동적 웹 페이지 생성을 학습합니다.

## 학습 목표

- **Bean Validation**: JSR-303/380 기반 입력 데이터 검증 시스템 구축
- **Spring Security**: 기본 인증/인가 시스템 구현
- **XSS 방어**: Lucy XSS Filter를 활용한 Cross-Site Scripting 공격 방어
- **Thymeleaf 통합**: 서버사이드 템플릿 엔진과 보안 연동
- **국제화(i18n)**: 다국어 지원 메시지 시스템

## 프로젝트 구조

```
src/main/java/com/genius/primavera/
├── ValidationApplication.java              # 메인 애플리케이션
├── application/                           # 애플리케이션 서비스 계층
│   ├── UserService.java                   # 사용자 비즈니스 로직
│   ├── UserServiceImpl.java              # 사용자 서비스 구현
│   └── validation/                        # 검증 관련 클래스
│       ├── CustomValidator.java          # 커스텀 검증기
│       ├── ValidationGroups.java         # 검증 그룹 정의
│       └── constraints/                  # 커스텀 제약조건
│           ├── EmailDomain.java          # 이메일 도메인 검증
│           ├── Password.java             # 비밀번호 복잡성 검증
│           └── UniqueEmail.java          # 이메일 중복 검증
├── config/                               # 설정 클래스
│   ├── SecurityConfig.java               # Spring Security 설정
│   ├── WebMvcConfig.java                 # Web MVC 설정
│   └── MessageConfig.java                # 국제화 메시지 설정
├── domain/                               # 도메인 계층
│   ├── entity/                          # 엔티티 클래스
│   │   └── User.java                    # 사용자 엔티티 (검증 애너테이션 포함)
│   └── mapper/                          # MyBatis 매퍼
│       └── UserMapper.java              # 사용자 매퍼
└── interfaces/                          # 인터페이스 계층
    ├── UserController.java              # 사용자 REST API
    ├── UserWebController.java           # 웹 페이지 컨트롤러
    └── dto/                             # 데이터 전송 객체
        ├── UserRegistrationDto.java     # 회원가입 DTO
        └── UserUpdateDto.java           # 사용자 수정 DTO

src/main/resources/
├── application-local.yml                # 로컬 개발 설정
├── application.yml                      # 기본 애플리케이션 설정
├── messages.properties                  # 기본 메시지 (한국어)
├── messages_en.properties               # 영어 메시지
├── messages_ja.properties               # 일본어 메시지
├── lucy-xss-servlet-filter-rule.xml     # Lucy XSS Filter 설정
└── templates/                           # Thymeleaf 템플릿
    ├── user.html                        # 사용자 관리 페이지
    ├── ajax.html                        # AJAX 예제 페이지
    └── html.html                        # HTML 처리 예제
```

## 주요 기능

### 1. Bean Validation 적용
```java
@Entity
@Table(name = "USER")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "{validation.user.email.notblank}")
    @Email(message = "{validation.user.email.format}")
    @UniqueEmail(message = "{validation.user.email.unique}")
    @EmailDomain(domains = {"gmail.com", "naver.com"}, 
                 message = "{validation.user.email.domain}")
    private String email;
    
    @NotBlank(message = "{validation.user.nickname.notblank}")
    @Size(min = 2, max = 20, message = "{validation.user.nickname.size}")
    private String nickname;
    
    @Password(message = "{validation.user.password.complexity}")
    private String password;
    
    @Min(value = 14, message = "{validation.user.age.min}")
    @Max(value = 120, message = "{validation.user.age.max}")
    private Integer age;
}
```

### 2. 커스텀 검증 애너테이션
```java
@Documented
@Constraint(validatedBy = EmailDomainValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailDomain {
    String message() default "허용되지 않는 이메일 도메인입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] domains() default {};
}

@Component
public class EmailDomainValidator implements ConstraintValidator<EmailDomain, String> {
    private String[] allowedDomains;
    
    @Override
    public void initialize(EmailDomain constraintAnnotation) {
        this.allowedDomains = constraintAnnotation.domains();
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || !email.contains("@")) return false;
        
        String domain = email.substring(email.lastIndexOf("@") + 1);
        return Arrays.asList(allowedDomains).contains(domain);
    }
}
```

### 3. Spring Security 설정
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/users")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()) // API 테스트용
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### 4. 검증 그룹 활용
```java
public interface ValidationGroups {
    interface Create {}
    interface Update {}
    interface Admin {}
}

@PostMapping("/users")
public ResponseEntity<?> createUser(
    @RequestBody @Validated(ValidationGroups.Create.class) UserRegistrationDto dto
) {
    User user = userService.createUser(dto);
    return ResponseEntity.ok(user);
}

@PutMapping("/users/{id}")
public ResponseEntity<?> updateUser(
    @PathVariable Long id,
    @RequestBody @Validated(ValidationGroups.Update.class) UserUpdateDto dto
) {
    User user = userService.updateUser(id, dto);
    return ResponseEntity.ok(user);
}
```

### 5. XSS 방어 설정
```xml
<!-- lucy-xss-servlet-filter-rule.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.navercorp.com/lucy-xss-servlet">
    <defenders>
        <defender>
            <name>xssPreventerDefender</name>
            <class>com.navercorp.lucy.security.xss.servletfilter.defender.XssPreventerDefender</class>
        </defender>
        
        <defender>
            <name>xssSaxFilterDefender</name>
            <class>com.navercorp.lucy.security.xss.servletfilter.defender.XssSaxFilterDefender</class>
            <init-param>
                <param-value>lucy-xss.xml</param-value>
            </init-param>
        </defender>
    </defenders>

    <default>
        <defender>xssPreventerDefender</defender>
    </default>
    
    <url-rule-set>
        <url-rule>
            <url disable="false">/api/**</url>
            <defender>xssSaxFilterDefender</defender>
        </url-rule>
        
        <url-rule>
            <url disable="true">/css/**</url>
        </url-rule>
        
        <url-rule>
            <url disable="true">/js/**</url>
        </url-rule>
    </url-rule-set>
</config>
```

### 6. 국제화 메시지
```properties
# messages.properties (한국어 - 기본)
validation.user.email.notblank=이메일은 필수입니다
validation.user.email.format=올바른 이메일 형식이 아닙니다
validation.user.email.unique=이미 사용 중인 이메일입니다
validation.user.email.domain=허용된 도메인이 아닙니다 (gmail.com, naver.com만 허용)
validation.user.nickname.notblank=닉네임은 필수입니다
validation.user.nickname.size=닉네임은 2-20자 사이여야 합니다
validation.user.password.complexity=비밀번호는 8자 이상, 대소문자/숫자/특수문자를 포함해야 합니다
validation.user.age.min=14세 이상만 가입 가능합니다
validation.user.age.max=올바른 나이를 입력해주세요

# messages_en.properties (영어)
validation.user.email.notblank=Email is required
validation.user.email.format=Invalid email format
validation.user.email.unique=Email is already taken
validation.user.email.domain=Only gmail.com and naver.com domains are allowed
validation.user.nickname.notblank=Nickname is required
validation.user.nickname.size=Nickname must be between 2 and 20 characters
validation.user.password.complexity=Password must be at least 8 characters with uppercase, lowercase, number, and special character
validation.user.age.min=Must be at least 14 years old
validation.user.age.max=Please enter a valid age
```

## 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| **Spring Boot** | 3.3.6 | 기본 프레임워크 |
| **Spring Security** | 6.4.4 | 인증/인가 보안 |
| **Spring Validation** | 3.3.6 | Bean Validation (JSR-380) |
| **Thymeleaf** | 3.4.0 | 서버사이드 템플릿 엔진 |
| **Lucy XSS Filter** | Custom Starter | XSS 공격 방어 |
| **MyBatis** | 3.0.4 | 데이터베이스 접근 |
| **MariaDB** | 11.4.7 | 관계형 데이터베이스 |
| **BCrypt** | 포함 | 비밀번호 해싱 |

## 실행 방법

### 1. 데이터베이스 준비
```bash
# Docker로 MariaDB 실행
./docker-manager.sh start chap06

# 수동 실행
docker run -d --name mariadb-chap06 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3308:3306 mariadb:11.4.7
```

### 2. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap06:bootRun -Dspring.profiles.active=local

# Lucy XSS Filter 활성화 확인
curl -i http://localhost:8080/actuator/health
```

### 3. 웹 인터페이스 접근
```bash
# 브라우저에서 접근
http://localhost:8080/users    # 사용자 관리 페이지
http://localhost:8080/ajax     # AJAX 예제 페이지
http://localhost:8080/html     # HTML 처리 예제
```

### 4. API 테스트
```bash
# 사용자 등록 (검증 성공)
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@gmail.com",
    "nickname": "TestUser",
    "password": "SecurePass123!",
    "age": 25
  }'

# 사용자 등록 (검증 실패)
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "nickname": "A",
    "password": "123",
    "age": 10
  }'

# XSS 공격 시도
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@gmail.com",
    "nickname": "<script>alert(\"XSS\")</script>",
    "password": "SecurePass123!",
    "age": 25
  }'
```

## 핵심 학습 포인트

### 1. Bean Validation 아키텍처
- **JSR-380 표준**: 자바 표준 검증 API 활용
- **애너테이션 기반**: @NotNull, @Size, @Email 등 내장 제약조건
- **커스텀 검증기**: 비즈니스 규칙에 맞는 검증 로직 구현
- **검증 그룹**: 시나리오별 검증 규칙 분리
- **국제화 메시지**: 다국어 검증 오류 메시지 지원

### 2. Spring Security 통합
- **필터 체인**: SecurityFilterChain을 통한 보안 정책 정의
- **인증/인가**: 사용자 인증과 권한 기반 접근 제어
- **비밀번호 보안**: BCrypt를 통한 안전한 비밀번호 해싱
- **CSRF 보호**: Cross-Site Request Forgery 공격 방어
- **보안 헤더**: XSS, Clickjacking 등 추가 보안 강화

### 3. XSS 방어 시스템
- **다계층 보안**: Spring Security + Lucy XSS Filter 조합
- **입력 검증**: 클라이언트 입력 데이터 필터링
- **출력 인코딩**: HTML 출력 시 안전한 문자로 변환
- **화이트리스트**: 허용된 HTML 태그와 속성만 통과
- **URL별 정책**: 경로에 따른 다른 XSS 방어 정책 적용

### 4. 템플릿 엔진 보안
- **Thymeleaf 보안**: 템플릿에서의 XSS 방어
- **CSRF 토큰**: 폼 기반 요청의 CSRF 보호
- **조건부 렌더링**: 권한에 따른 동적 콘텐츠 표시
- **국제화**: 다국어 메시지 처리

## 테스트 실행

### 단위 테스트
```bash
# 전체 테스트 실행
./gradlew :chap06:test

# 검증 테스트만 실행
./gradlew :chap06:test --tests "*ValidationTest"

# 보안 테스트만 실행
./gradlew :chap06:test --tests "*SecurityTest"
```

### 통합 테스트
```bash
# XSS 필터 통합 테스트
./gradlew :chap06:test --tests "*XssIntegrationTest"

# 전체 웹 MVC 테스트
./gradlew :chap06:test --tests "*WebMvcTest"
```

## 설정 관리

### application-local.yml
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3308/primavera
    username: primavera
    password: primavera
    driver-class-name: org.mariadb.jdbc.Driver
  
  # Lucy XSS Filter 설정
  lucy-filter:
    enabled: true
    name: "lucyXssEscapeServletFilter"
    order: 1
    add-url-patterns:
      - "/*"
  
  # 국제화 설정
  messages:
    basename: messages
    encoding: UTF-8
    cache-duration: 3600

# 로깅 설정
logging:
  level:
    com.genius.primavera: DEBUG
    org.springframework.security: INFO
    com.navercorp.lucy: DEBUG
```

## 주요 애너테이션

| 애너테이션 | 용도 | 예시 |
|------------|------|------|
| `@Validated` | 클래스 레벨 검증 활성화 | `@Validated(ValidationGroups.Create.class)` |
| `@Valid` | 메서드 파라미터 검증 | `public void create(@Valid User user)` |
| `@NotBlank` | 빈 문자열 검증 | `@NotBlank(message = "필수 입력")` |
| `@Email` | 이메일 형식 검증 | `@Email(message = "이메일 형식 오류")` |
| `@Size` | 문자열/컬렉션 크기 검증 | `@Size(min = 2, max = 20)` |
| `@Min/@Max` | 숫자 범위 검증 | `@Min(value = 14)` |
| `@PreAuthorize` | 메서드 실행 전 권한 검사 | `@PreAuthorize("hasRole('ADMIN')")` |

## 학습 순서

1. **Bean Validation 기초**: 기본 제약조건 애너테이션 활용
2. **커스텀 검증기 작성**: 비즈니스 규칙에 맞는 검증 로직 구현  
3. **Spring Security 설정**: 기본 인증/인가 시스템 구축
4. **XSS Filter 통합**: Lucy XSS Filter 설정 및 정책 정의
5. **국제화 메시지**: 다국어 검증 메시지 시스템 구축
6. **Thymeleaf 통합**: 보안이 적용된 웹 페이지 개발
7. **통합 테스트**: 전체 보안 시스템 검증

## 활용 방법

### 1. 보안 강화 전략
- **다계층 보안**: 여러 보안 기술의 조합으로 방어력 강화
- **입력 검증**: 클라이언트와 서버 양쪽에서 데이터 검증
- **출력 인코딩**: XSS 공격 방지를 위한 안전한 데이터 출력
- **보안 헤더**: HTTP 보안 헤더를 통한 추가 보안

### 2. 사용자 경험 개선
- **실시간 검증**: AJAX를 통한 실시간 입력 데이터 검증
- **국제화**: 사용자 언어에 맞는 오류 메시지 제공
- **접근성**: 스크린 리더 호환 오류 메시지 설계

이 모듈은 현대적인 웹 애플리케이션에서 필수인 보안과 검증 시스템을 종합적으로 다루며, 실무에서 바로 적용할 수 있는 보안 개발 패턴을 학습할 수 있습니다.