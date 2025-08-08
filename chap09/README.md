## chap09 - Spring Security 기본 구현

### 개요
Spring Security를 활용한 기본 인증/인가 시스템 구현 모듈로, 사용자 인증, 권한 관리, 세션 보안 등의 핵심 보안 기능을 포함합니다.

### 주요 기능
- Spring Security 6.x 기반 보안 설정
- 사용자 인증 및 권한 관리 (USER, MANAGER, ADMINISTRATOR)
- 커스텀 UserDetails 및 UserDetailsService 구현
- Form 기반 로그인/로그아웃
- Lucy XSS Filter 통합
- Chain of Responsibility 패턴을 활용한 검증 프로세스
- MyBatis 기반 사용자 관리

### 기술 스택
- Spring Boot 3.x
- Spring Security 6.x
- MyBatis
- MariaDB 11.4.7
- Thymeleaf + Spring Security Dialect
- Lucy XSS Filter
- BCrypt Password Encoder

### build.gradle 주요 의존성
```gradle
implementation "org.springframework.security:spring-security-config:${springSecurityVersion}"
implementation "org.springframework.security:spring-security-core:${springSecurityVersion}"
implementation "org.springframework.security:spring-security-crypto:${springSecurityVersion}"
implementation "org.springframework.security:spring-security-web:${springSecurityVersion}"
implementation "org.thymeleaf.extras:thymeleaf-extras-springsecurity6:${thymeleafExtrasSpringSecurity6Version}"
implementation project(":appendix:spring-boot-starter-lucy-filter")
```

### Logging default.xml 변경
```
<logger name="org.hibernate.validator.internal.util.Version" level="ERROR"/>
<logger name="org.springframework.web" level="DEBUG"/>
<logger name="org.springframework.security" level="DEBUG"/>
```

### SecurityConfig 파일 추가

### DelegatingFilterProxy
* Proxy for a standard Servlet Filter, delegating to a Spring-managed bean that implements the Filter interface.

### Flow 
* org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
* org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

### FilterChainProxy log

```
2019-04-29 15:50:33.894 DEBUG 37625 --- [  XNIO-1 task-1] io.undertow.request.security             : Attempting to authenticate /, authentication required: false
2019-04-29 15:50:33.897 DEBUG 37625 --- [  XNIO-1 task-1] io.undertow.request.security             : Authentication outcome was NOT_ATTEMPTED with method io.undertow.security.impl.CachedAuthenticatedSessionMechanism@988e188 for /
2019-04-29 15:50:33.898 DEBUG 37625 --- [  XNIO-1 task-1] io.undertow.request.security             : Authentication result was ATTEMPTED for /
2019-04-29 15:50:33.900  INFO 37625 --- [  XNIO-1 task-1] io.undertow.servlet                      : Initializing Spring DispatcherServlet 'dispatcherServlet'
2019-04-29 15:50:33.901  INFO 37625 --- [  XNIO-1 task-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2019-04-29 15:50:33.913  INFO 37625 --- [  XNIO-1 task-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 12 ms
2019-04-29 15:50:33.925 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 1 of 15 in additional filter chain; firing Filter: 'WebAsyncManagerIntegrationFilter'
2019-04-29 15:50:33.925 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 2 of 15 in additional filter chain; firing Filter: 'SecurityContextPersistenceFilter'
2019-04-29 15:50:33.926 DEBUG 37625 --- [  XNIO-1 task-1] w.c.HttpSessionSecurityContextRepository : No HttpSession currently exists
2019-04-29 15:50:33.926 DEBUG 37625 --- [  XNIO-1 task-1] w.c.HttpSessionSecurityContextRepository : No SecurityContext was available from the HttpSession: null. A new one will be created.
2019-04-29 15:50:33.928 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 3 of 15 in additional filter chain; firing Filter: 'HeaderWriterFilter'
2019-04-29 15:50:33.929 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 4 of 15 in additional filter chain; firing Filter: 'CsrfFilter'
2019-04-29 15:50:33.930 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 5 of 15 in additional filter chain; firing Filter: 'LogoutFilter'
2019-04-29 15:50:33.930 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.u.matcher.AntPathRequestMatcher  : Request 'GET /' doesn't match 'POST /logout'
2019-04-29 15:50:33.930 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 6 of 15 in additional filter chain; firing Filter: 'UsernamePasswordAuthenticationFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.u.matcher.AntPathRequestMatcher  : Request 'GET /' doesn't match 'POST /login'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 7 of 15 in additional filter chain; firing Filter: 'DefaultLoginPageGeneratingFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 8 of 15 in additional filter chain; firing Filter: 'DefaultLogoutPageGeneratingFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.u.matcher.AntPathRequestMatcher  : Checking match of request : '/'; against '/logout'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 9 of 15 in additional filter chain; firing Filter: 'BasicAuthenticationFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 10 of 15 in additional filter chain; firing Filter: 'RequestCacheAwareFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.s.HttpSessionRequestCache        : saved request doesn't match
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 11 of 15 in additional filter chain; firing Filter: 'SecurityContextHolderAwareRequestFilter'
2019-04-29 15:50:33.932 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 12 of 15 in additional filter chain; firing Filter: 'AnonymousAuthenticationFilter'
2019-04-29 15:50:33.933 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.a.AnonymousAuthenticationFilter  : Populated SecurityContextHolder with anonymous token: 'org.springframework.security.authentication.AnonymousAuthenticationToken@284a7a90: Principal: anonymousUser; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@b364: RemoteIpAddress: 0:0:0:0:0:0:0:1; SessionId: null; Granted Authorities: ROLE_ANONYMOUS'
2019-04-29 15:50:33.933 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 13 of 15 in additional filter chain; firing Filter: 'SessionManagementFilter'
2019-04-29 15:50:33.933 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.session.SessionManagementFilter  : Requested session ID gYaORwNBFNAPNla4SKJWVlxUD-w_qMVyd0dGLku6 is invalid.
2019-04-29 15:50:33.933 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 14 of 15 in additional filter chain; firing Filter: 'ExceptionTranslationFilter'
2019-04-29 15:50:33.934 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 15 of 15 in additional filter chain; firing Filter: 'FilterSecurityInterceptor'
2019-04-29 15:50:33.934 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.a.i.FilterSecurityInterceptor    : Secure object: FilterInvocation: URL: /; Attributes: [authenticated]
2019-04-29 15:50:33.934 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.a.i.FilterSecurityInterceptor    : Previously Authenticated: org.springframework.security.authentication.AnonymousAuthenticationToken@284a7a90: Principal: anonymousUser; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@b364: RemoteIpAddress: 0:0:0:0:0:0:0:1; SessionId: null; Granted Authorities: ROLE_ANONYMOUS
2019-04-29 15:50:33.939 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.access.vote.AffirmativeBased       : Voter: org.springframework.security.web.access.expression.WebExpressionVoter@3cd75674, returned: -1
2019-04-29 15:50:33.942 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.a.ExceptionTranslationFilter     : Access is denied (user is anonymous); redirecting to authentication entry point
```

### WebAsyncManagerIntegrationFilter
* WebSecurityConfigurerAdapter getHttp 메소드에서 등록

### SecurityContextPersistenceFilter
* SecurityContextRepository 에서 SecurityContext 를 로드하고 저장하는 일을 담당
* SecurityContext 는 사용자의 보호되고 인증된 세션 

### HeaderWriterFilter

### CsrfFilter

### LogoutFilter

### UsernamePasswordAuthenticationFilter
* UsernamePasswordAuthenticationFilter attemptAuthentication()
* ProviderManager > authenticate()
* AbstractUserDetailsAuthenticationProvider > authenticate() > retrieveUser()
* DaoAuthenticationProvider > getUserDetailsService()

### DefaultLoginPageGeneratingFilter

### DefaultLogoutPageGeneratingFilter

### BasicAuthenticationFilter

### RequestCacheAwareFilter

### SecurityContextHolderAwareRequestFilter

### AnonymousAuthenticationFilter

### SessionManagementFilter

### ExceptionTranslationFilter
* 보호된 요청을 처리하는 동안 발생할 수 있는 기대한 예외의 기본 라우팅과 위임을 처리

### FilterSecurityInterceptor
* 권한부여와 관련한 결정을 AccessDecisionManager 에게 위임해 권한부여 결정 및 접근 제어 결정을 쉽게 만듬

## 설정 (SecurityConfig)

### Spring Security 6.x 기반 설정
Spring Boot 3.x와 Spring Security 6.x로 업그레이드되면서 설정 방식이 변경되었습니다:
- `WebSecurityConfigurerAdapter` 대신 `SecurityFilterChain` 빈 사용
- Lambda DSL 기반 설정
- `antMatchers()` → `requestMatchers()`로 변경

```java
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class PrimaveraSecurityConfig {

    private final AuthenticationSuccessHandler successHandler = (request, response, authentication) -> log.info("success : {}", request.getContextPath());
    private final AuthenticationFailureHandler failureHandler = (request, response, authentication) -> log.info("failure : {}", request.getContextPath());

    private final PrimaveraUserDetailsService primaveraUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    configuration.setMaxAge(3600L);
                    configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                    return configuration;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.GET, "/resources/**", "/bower_components/**", "/dist/**", "/plugins/**", "/favicon.ico").permitAll()
                                .requestMatchers(HttpMethod.GET, "/login", "/login/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterAfter(new PrimaveraFilter(), UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/signin")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .defaultSuccessUrl("/index", true)
                        .failureHandler(failureHandler)
                        .failureUrl("/login?error=true"))
                .logout(logout -> logout
                        .logoutUrl("/signout")
                        .deleteCookies("JSESSIONID"))
                .httpBasic(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(primaveraUserDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }
}
```

### DaoAuthenticationProvider
* additionalAuthenticationChecks 메소드에서 저장된 비밀번호와 로그인 화면에서 입력하 비밀번호를 확인

### Thymeleaf + Security
```html
<li class="dropdown user user-menu">
    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
        <img src="dist/img/user2-160x160.jpg" class="user-image" alt="User Image">
        <span sec:authentication="name" class="hidden-xs">Alexander Pierce</span>
    </a>
    <ul class="dropdown-menu">
        <!-- User image -->
        <li class="user-header">
            <img src="dist/img/user2-160x160.jpg" class="img-circle" alt="User Image">
            <p>
                <span sec:authentication="name">Alexander Pierce</span>
                <span sec:authorize="hasRole('ROLE_USER')">- Web Design</span>
                <span sec:authorize="hasRole('ROLE_MANAGER')">- Web Developer</span>
                <span sec:authorize="hasRole('ROLE_ADMINISTRATOR')">- Web Master</span>
                <small>Member since Nov. 2019</small>
            </p>
        </li>
        <!-- Menu Body -->
        <li class="user-body">
            <div class="row">
                <div class="col-xs-4 text-center">
                    <a href="#">Followers</a>
                </div>
                <div class="col-xs-4 text-center">
                    <a href="#">Sales</a>
                </div>
                <div class="col-xs-4 text-center">
                    <a href="#">Friends</a>
                </div>
            </div>
            <!-- /.row -->
        </li>
        <!-- Menu Footer-->
        <li class="user-footer">
            <div class="pull-left">
                <a href="#" class="btn btn-default btn-flat">Profile</a>
            </div>
            <div class="pull-right">
                <a th:href="@{/signout}" href="/signout" class="btn btn-default btn-flat">Sign out</a>
            </div>
        </li>
    </ul>
</li>
```

### PasswordEncoding Test
```java
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PasswordEncoderTest {

    private static PasswordEncoder encoder;
    private static String rawPassword = "password";
    private static String bcrype = "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.";
    private static String noop = "{noop}password";

    @BeforeAll
    public static void setUp() {
        encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Test
    @Order(1)
    @DisplayName("bcrype 방식")
    public void bcrypeEncoder() {
        String encodedPassword = encoder.encode(rawPassword);
        log.info(encodedPassword);
        Assertions.assertNotEquals(bcrype, encodedPassword);
        Assertions.assertTrue(encoder.matches(rawPassword, encodedPassword));
        Assertions.assertTrue(encoder.matches(rawPassword, bcrype));
    }

    @Test
    @Order(2)
    @DisplayName("noop 방식")
    public void noopEncoder() {
        Assertions.assertTrue(encoder.matches(rawPassword, noop));
    }
}
```

## 핵심 구현 요소

### 1. 사용자 인증 체계
- **PrimaveraUserDetails**: Spring Security의 UserDetails 인터페이스 구현
  - User 엔티티를 Spring Security가 이해할 수 있는 형태로 변환
  - 권한(Authorities)을 ROLE_ 접두사와 함께 반환
  - 계정 상태(만료, 잠금, 활성화) 관리

- **PrimaveraUserDetailsService**: 데이터베이스에서 사용자 정보 로드
  - UserService를 통해 이메일로 사용자 검색
  - UsernameNotFoundException 처리

### 2. 보안 필터 체인
- **PrimaveraFilter**: UsernamePasswordAuthenticationFilter 이후에 추가되는 커스텀 필터
- **Lucy XSS Filter**: XSS 공격 방지를 위한 필터 통합

### 3. Chain of Responsibility 패턴
- **ProcessChain**: 유효성 검증을 위한 책임 연쇄 패턴 구현
- **Process 구현체들**:
  - FromValidationProcess
  - ToValidationProcess
  - MessageValidationProcess

### 4. 도메인 모델
- **User**: 사용자 엔티티 (Validation Group 포함)
- **Role**: 권한 정보 (RoleType enum)
- **UserStatus**: 사용자 상태 (ON, OFF, DORMANT, BLOCK, LEAVE)
- **UserRole**: User와 Role의 다대다 관계 매핑

### 5. MyBatis TypeHandler
- **RoleTypeHandler**: RoleType enum 처리
- **UserStatusTypeHandler**: UserStatus enum 처리

### 6. 사용자 정의 Validator
- **@Nickname**: 닉네임 검증 어노테이션
- **NicknameValidator**: 닉네임 검증 로직 구현

## 실행 방법

### 1. 데이터베이스 설정
```bash
# MariaDB 11.4.7 실행
docker run -d --name mariadb-primavera \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 1109:3306 mariadb:11.4.7
```

### 2. 애플리케이션 실행
```bash
./gradlew :chap09:bootRun
```

### 3. 접속 정보
- URL: http://localhost:8080
- 테스트 계정:
  - Email: Genius@gmail.com / Password: secret (USER, MANAGER, ADMINISTRATOR 권한)
  - 데이터베이스 기반 사용자도 로그인 가능

## 실행 방법

### 🚀 Spring Boot 애플리케이션 실행

#### 1. 환경 변수 방식 (권장)
```bash
# 로컬 환경으로 실행  
SPRING_PROFILES_ACTIVE=local ./gradlew :chap09:bootRun
```

#### 2. Program Arguments 방식
```bash
# 기본 실행
./gradlew :chap09:bootRun --args='--spring.profiles.active=local'
```

#### 3. IDE 설정 방식
- IntelliJ IDEA: Run Configuration → VM Options 또는 Program Arguments 설정
- VM Options: `-Dspring.profiles.active=local`
- Program Arguments: `--spring.profiles.active=local`

## 🐳 인프라 설정

### Docker Compose 환경 설정

이 챕터는 **MyBatis + 보안 인프라**를 사용합니다:

```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# MyBatis + 보안 학습용 Docker Compose 실행 (MariaDB)
docker-compose -f docker-compose.mybatis.yml up -d

# 서비스 상태 확인
docker-compose -f docker-compose.mybatis.yml ps

# 정리 (컨테이너 및 볼륨 삭제)
docker-compose -f docker-compose.mybatis.yml down -v
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308)
- MyBatis 전용 데이터베이스 스키마 자동 생성

**애플리케이션 실행:**
```bash
# 인프라 시작 후 애플리케이션 실행
./gradlew :chap09:bootRun -Dspring.profiles.active=local
```

## ✅ 최근 테스트 개선사항

### TestContainers 현대화 마이그레이션 완료

**Spring Boot 3.x 표준 방식으로 Spring Security 테스트 현대화:**

#### 마이그레이션된 테스트 파일들:
- `SecurityLoginPageTest`: Spring Security 로그인 페이지 및 인증 플로우 통합 테스트

#### 새로운 TestContainers 패턴 (현재 방식)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Spring Security 로그인 페이지 테스트")
class SecurityLoginPageTest {

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
    @DisplayName("인증되지 않은 사용자 로그인 페이지 리다이렉트")
    void unauthenticatedUserRedirectToLogin() {
        ResponseEntity<String> response = restTemplate.getForEntity("/secured", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation().toString()).contains("/login");
    }
}
```

#### 마이그레이션의 주요 개선 효과:
- **Spring Security 통합 검증**: 인증/인가 플로우 전체 테스트
- **로그인 페이지 렌더링**: Thymeleaf와 Spring Security 통합 확인
- **세션 관리 테스트**: 인증 세션 생성 및 유지 검증
- **접근 권한 제어**: URL 패턴별 접근 권한 검증
- **CSRF 보호 검증**: Cross-Site Request Forgery 방어 메커니즘 테스트

### 참고
* Spring Security3 (피터 뮬라리엔)
* https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#introduction
* https://docs.spring.io/spring-security/site/docs/current/guides/html5/helloworld-boot.html