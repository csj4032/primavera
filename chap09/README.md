# Chapter 09: Spring Security 기본 인증과 역할 기반 권한 관리

## 프로젝트 개요

**Spring Security Basic Authentication**는 Spring Boot에서 Spring Security의 기본 보안 기능을 학습하는 모듈입니다. 커스텀 인증, 역할 기반 권한 관리, 보안 설정의 핵심 개념을 다룹니다.

### 보안 학습 목표
- Spring Security 기본 아키텍처 이해
- UserDetailsService를 통한 커스텀 인증 구현
- 역할 기반 접근 제어 (RBAC) 구현
- 패스워드 암호화 및 보안 설정
- SecurityFilterChain을 통한 보안 필터 체인 구성

## 프로젝트 구조

```
chap09/
├── src/main/java/com/genius/primavera/
│   ├── SpringSecurityBasicApplication.java          # 메인 애플리케이션
│   ├── domain/
│   │   ├── model/
│   │   │   ├── User.java                           # 사용자 도메인 모델
│   │   │   ├── Role.java                          # 역할 도메인 모델
│   │   │   ├── UserRole.java                      # 사용자-역할 매핑
│   │   │   ├── RoleType.java                      # 역할 타입 Enum
│   │   │   └── UserStatus.java                    # 사용자 상태 Enum
│   │   ├── mapper/
│   │   │   ├── UserMapper.java                    # 사용자 데이터 매퍼
│   │   │   └── UserRoleMapper.java                # 사용자 역할 매퍼
│   │   └── typehandler/                           # MyBatis 타입 핸들러
│   ├── application/
│   │   ├── UserService.java                       # 사용자 서비스 인터페이스
│   │   ├── UserServiceImpl.java                   # 사용자 서비스 구현
│   │   └── validator/                             # 검증 로직
│   ├── infrastructure/
│   │   ├── security/
│   │   │   ├── PrimaveraSecurityConfig.java       # 보안 설정
│   │   │   ├── PrimaveraUserDetailsService.java   # 커스텀 UserDetailsService
│   │   │   ├── PrimaveraUserDetails.java          # 커스텀 UserDetails
│   │   │   ├── PrimaveraPasswordEncoder.java      # 커스텀 패스워드 인코더
│   │   │   └── PrimaveraAuthenticationSuccessHandler.java # 인증 성공 핸들러
│   │   └── filter/
│   │       └── PrimaveraFilter.java              # 커스텀 보안 필터
│   └── interfaces/
│       ├── LoginController.java                   # 로그인 컨트롤러
│       ├── UserController.java                    # 사용자 컨트롤러
│       └── FilterController.java                  # 필터 테스트 컨트롤러
├── src/main/resources/
│   ├── application.yml                            # 메인 설정
│   ├── application-local.yml                     # 로컬 개발 설정
│   ├── templates/                                 # Thymeleaf 템플릿
│   │   ├── login.html                            # 로그인 페이지
│   │   ├── index.html                            # 메인 페이지
│   │   ├── admin.html                            # 관리자 페이지
│   │   └── manager.html                          # 매니저 페이지
│   └── static/                                   # 정적 리소스
└── src/test/resources/
    ├── application-test.yml                      # 테스트 설정
    └── sql/init.sql                             # 테스트 데이터
```

## 보안 기능 및 인증/인가

### 1. 커스텀 인증 (Authentication)

```java
@Service
@RequiredArgsConstructor
public class PrimaveraUserDetailsService implements UserDetailsService {
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
            
        List<UserRole> userRoles = userRoleMapper.findByUserId(user.getId());
        
        return PrimaveraUserDetails.builder()
            .user(user)
            .roles(userRoles)
            .build();
    }
}
```

### 2. 역할 기반 권한 관리 (Authorization)

```java
@Configuration
@EnableWebSecurity
public class PrimaveraSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMINISTRATOR")
                .requestMatchers("/manager/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                .requestMatchers("/user/**").hasAnyRole("ADMINISTRATOR", "MANAGER", "USER")
                .requestMatchers("/", "/login", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
            )
            .build();
    }
}
```

### 3. 패스워드 암호화

```java
@Component
public class PrimaveraPasswordEncoder {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

## 기술 스택

- **Spring Boot**: 3.3.6
- **Spring Security**: 6.4.4
- **Spring Web**: RESTful API 및 웹 컨트롤러
- **Spring Validation**: 입력 데이터 검증
- **MyBatis**: SQL 매핑 프레임워크
- **Thymeleaf**: 템플릿 엔진
- **Thymeleaf Security**: 보안 확장 기능
- **MariaDB**: 관계형 데이터베이스
- **TestContainers**: 통합 테스트 컨테이너
- **Commons Codec**: 암호화 유틸리티

## 실행 방법

### 1. Docker 인프라 시작
```bash
# MariaDB 시작
./docker-manager.sh start chap09

# 상태 확인
./docker-manager.sh status chap09
```

### 2. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap09:bootRun -Dspring.profiles.active=local

# 또는 IDE에서 실행 시
-Dspring.profiles.active=local
```

### 3. 웹 접속
```
http://localhost:8080
```

### 4. 테스트 계정
- **관리자**: admin@primavera.com / password (ADMINISTRATOR 권한)
- **매니저**: manager@primavera.com / password (MANAGER 권한)
- **사용자**: user@primavera.com / password (USER 권한)

## 보안 테스트 실행 방법

### 1. 단위 테스트
```bash
./gradlew :chap09:test
```

### 2. 보안 테스트
```bash
# Spring Security 테스트 포함
./gradlew :chap09:test --tests "*Security*"
```

### 3. 통합 테스트
```bash
# TestContainers를 사용한 전체 테스트
./gradlew :chap09:test --tests "*Integration*"
```

## 핵심 보안 학습 포인트

### 1. SecurityFilterChain 설정

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // CORS 설정
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // CSRF 설정 (기본 활성화)
        .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        // 세션 관리
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
        )
        // 권한 설정
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").hasRole("ADMINISTRATOR")
            .requestMatchers("/manager/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
            .anyRequest().authenticated()
        )
        .build();
}
```

### 2. 커스텀 UserDetails 구현

```java
@Getter
@Builder
public class PrimaveraUserDetails implements UserDetails {
    
    private final User user;
    private final List<UserRole> roles;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRoleType().name()))
            .collect(Collectors.toList());
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
```

### 3. 메서드 레벨 보안

```java
@Controller
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserController {
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String adminPage(Model model, Authentication authentication) {
        model.addAttribute("user", authentication.getPrincipal());
        return "admin";
    }
    
    @PostAuthorize("returnObject.owner == authentication.name or hasRole('ADMINISTRATOR')")
    public User getUserProfile(Long userId) {
        return userService.findById(userId);
    }
}
```

## 학습 순서

1. **Spring Security 기본 개념**
   - SecurityFilterChain 이해
   - Authentication과 Authorization 차이
   - UserDetails와 UserDetailsService

2. **커스텀 인증 구현**
   - PrimaveraUserDetailsService 구현
   - 데이터베이스 기반 사용자 인증
   - 패스워드 암호화

3. **권한 기반 접근 제어**
   - 역할 기반 URL 보안
   - 메서드 레벨 보안
   - 조건부 권한 검사

4. **보안 설정 심화**
   - CORS 설정
   - CSRF 보호
   - 세션 관리

5. **보안 테스트**
   - @WithMockUser 활용
   - 보안 테스트 작성
   - 통합 테스트

## 주요 보안 애너테이션

### Spring Security 애너테이션
- `@EnableWebSecurity`: 웹 보안 활성화
- `@EnableMethodSecurity`: 메서드 레벨 보안 활성화
- `@PreAuthorize`: 메서드 실행 전 권한 검사
- `@PostAuthorize`: 메서드 실행 후 권한 검사
- `@Secured`: 역할 기반 메서드 보안
- `@RolesAllowed`: JSR-250 역할 기반 보안

### 테스트 애너테이션
- `@WithMockUser`: 목 사용자로 테스트
- `@WithUserDetails`: 실제 UserDetailsService 사용
- `@WithSecurityContext`: 커스텀 보안 컨텍스트

## 다음 단계 안내

Chapter 09를 완료한 후에는 **Chapter 10 (OAuth2 소셜 로그인)**으로 진행하여 다음 내용을 학습합니다:

- OAuth2 클라이언트 구성
- 소셜 로그인 (Google, Facebook, GitHub, Kakao)
- 캐싱 전략 (Redis, Caffeine)
- Actuator를 통한 보안 모니터링
- 고급 보안 설정

---

Spring Security의 기본기를 탄탄히 다진 후 소셜 로그인과 고급 보안 기능으로 확장해보세요!