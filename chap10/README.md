# Chapter 10: OAuth2 소셜 로그인과 멀티 레벨 캐싱 전략

## 프로젝트 개요

**OAuth2 Social Login Application**는 Spring Boot 3.x와 Spring Security 6.x를 기반으로 한 소셜 로그인 통합 시스템입니다. Google, Facebook, GitHub, Kakao 4개 소셜 로그인 프로바이더를 지원하며, Redis + Caffeine 하이브리드 캐싱 전략과 Spring Boot Actuator를 통한 실시간 모니터링을 구현합니다.

### 보안 학습 목표
- OAuth2 인증 플로우 이해 및 구현
- 소셜 로그인 통합 및 사용자 매핑
- 다중 프로바이더 토큰 관리
- 멀티 레벨 캐싱 전략 구현
- Actuator를 통한 보안 모니터링

## 프로젝트 구조

```
chap10/
├── src/main/java/com/genius/primavera/
│   ├── OAuth2SocialLoginApplication.java           # 메인 애플리케이션
│   ├── domain/
│   │   ├── model/
│   │   │   ├── User.java                          # 사용자 도메인 모델
│   │   │   ├── UserConnection.java                # 소셜 연동 정보
│   │   │   ├── ProviderType.java                  # 소셜 프로바이더 타입
│   │   │   ├── RoleType.java                      # 역할 타입
│   │   │   └── UserStatus.java                    # 사용자 상태
│   │   └── mapper/
│   │       ├── UserMapper.java                    # 사용자 매퍼
│   │       ├── UserRoleMapper.java                # 역할 매퍼
│   │       └── UserConnectionMapper.java          # 소셜 연동 매퍼
│   ├── application/
│   │   ├── UserService.java                       # 사용자 서비스
│   │   └── UserServiceImpl.java                   # 서비스 구현
│   ├── infrastructure/
│   │   ├── cache/                                 # 캐싱 전략
│   │   │   ├── CacheConfiguration.java            # 캐시 설정
│   │   │   ├── OAuth2TokenCacheService.java       # 토큰 캐시
│   │   │   ├── UserProfileCacheService.java       # 프로필 캐시
│   │   │   └── CacheEvictionStrategy.java         # 무효화 전략
│   │   ├── security/
│   │   │   ├── PrimaveraSecurityConfiguration.java # Spring Security 설정
│   │   │   ├── PrimaveraUserDetailsService.java   # 일반 사용자 인증
│   │   │   ├── PrimaveraSocialUserDetailsService.java # 소셜 사용자 인증
│   │   │   ├── PrimaveraUserDetails.java          # 커스텀 UserDetails
│   │   │   └── social/                            # 소셜 인증 설정
│   │   │       ├── PrimaveraSocialConfiguration.java
│   │   │       └── provider/                      # 프로바이더별 설정
│   │   │           ├── GoogleUserDetails.java
│   │   │           ├── FacebookUserDetails.java
│   │   │           ├── GithubUserDetails.java
│   │   │           └── KakaoUserDetails.java
│   │   └── filter/
│   │       └── PrimaveraFilter.java              # 커스텀 필터
│   └── interfaces/
│       ├── LoginController.java                   # 로그인 컨트롤러
│       ├── UserController.java                    # 사용자 컨트롤러
│       └── CacheManagementController.java         # 캐시 관리 API
├── src/main/resources/
│   ├── application.yml                            # 메인 설정
│   ├── application-local.yml                     # 로컬 개발 설정
│   └── templates/                                 # Thymeleaf 템플릿
└── src/test/resources/
    ├── application-test.yml                      # 테스트 설정
    └── sql/init.sql                             # 테스트 데이터
```

## 보안 기능 및 OAuth2 인증

### 1. OAuth2 소셜 로그인 설정

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class PrimaveraSecurityConfiguration {
    
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMINISTRATOR")
                .requestMatchers("/manager/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler())
                .userInfoEndpoint(userInfo -> 
                    userInfo.userService(oauth2UserService)
                )
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .build();
    }
}
```

### 2. 소셜 프로바이더별 사용자 정보 처리

```java
@Service
public class PrimaveraSocialUserDetailsService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegateOAuth2UserService.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
            
        return createSocialUser(registrationId, oauth2User, userNameAttributeName);
    }
    
    private OAuth2User createSocialUser(String registrationId, OAuth2User oauth2User, String userNameAttributeName) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleUserDetails(oauth2User.getAttributes(), userNameAttributeName);
            case "facebook" -> new FacebookUserDetails(oauth2User.getAttributes(), userNameAttributeName);
            case "github" -> new GithubUserDetails(oauth2User.getAttributes(), userNameAttributeName);
            case "kakao" -> new KakaoUserDetails(oauth2User.getAttributes(), userNameAttributeName);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };
    }
}
```

### 3. 소셜 프로바이더 설정 (application.yml)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${OAUTH2_GOOGLE_CLIENTID}
            client-secret: ${OAUTH2_GOOGLE_CLIENTSECRET}
            scope:
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
          facebook:
            client-id: ${OAUTH2_FACEBOOK_CLIENTID}
            client-secret: ${OAUTH2_FACEBOOK_CLIENTSECRET}
            scope:
              - email
              - public_profile
          github:
            client-id: ${OAUTH2_GITHUB_CLIENTID}
            client-secret: ${OAUTH2_GITHUB_CLIENTSECRET}
            scope:
              - user:email
              - read:user
          kakao:
            client-id: ${OAUTH2_KAKAO_CLIENTID}
            client-secret: ${OAUTH2_KAKAO_CLIENTSECRET:}
            authorization-grant-type: authorization_code
            scope:
              - profile_nickname
              - account_email
        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
```

## 멀티 레벨 캐싱 전략

### 1. 하이브리드 캐시 설정

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        compositeCacheManager.setCacheManagers(
            caffeineCacheManager(),
            redisCacheManager()
        );
        compositeCacheManager.setFallbackToNoOpCache(false);
        return compositeCacheManager;
    }
    
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats()
        );
        return cacheManager;
    }
    
    @Bean
    public CacheManager redisCacheManager() {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
            
        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(config)
            .build();
    }
}
```

### 2. OAuth2 토큰 캐싱 서비스

```java
@Service
@CacheConfig(cacheNames = "oauth2Tokens")
public class OAuth2TokenCacheService {
    
    @Cacheable(key = "#userId + ':' + #provider")
    public Optional<TokenCacheEntry> getValidAccessToken(String userId, String provider) {
        return findToken(userId, provider)
            .filter(entry -> !isTokenExpired(entry));
    }
    
    @CachePut(key = "#userId + ':' + #provider")
    public TokenCacheEntry cacheToken(String userId, String provider, 
                                    String accessToken, Instant expiresAt) {
        return TokenCacheEntry.builder()
            .userId(userId)
            .provider(provider)
            .accessToken(accessToken)
            .expiresAt(expiresAt)
            .cachedAt(Instant.now())
            .build();
    }
    
    @CacheEvict(key = "#userId + ':' + #provider")
    public void evictToken(String userId, String provider) {
        // 토큰 무효화
    }
    
    @Scheduled(fixedRate = 300000) // 5분마다
    public void cleanupExpiredTokens() {
        // 만료된 토큰 정리
    }
}
```

### 3. 사용자 프로필 캐싱

```java
@Service
@CacheConfig(cacheNames = "userProfiles")
public class UserProfileCacheService {
    
    @Cacheable(key = "#userId")
    public Optional<UserProfile> getUserProfile(Long userId) {
        return userService.findById(userId)
            .map(this::mapToUserProfile);
    }
    
    @CachePut(key = "#userProfile.userId")
    public UserProfile updateUserProfile(UserProfile userProfile) {
        return userService.updateProfile(userProfile);
    }
    
    @Cacheable(key = "#email + ':social'")
    public Optional<SocialUserProfile> getSocialUserProfile(String email) {
        return userConnectionMapper.findByEmail(email)
            .map(this::buildSocialProfile);
    }
}
```

## 기술 스택

- **Spring Boot**: 3.3.6
- **Spring Security**: 6.4.4
- **OAuth2 Client**: 소셜 로그인 통합
- **Spring Boot Actuator**: 애플리케이션 모니터링
- **Spring Cache**: 캐싱 추상화
- **Redis**: 분산 캐시
- **Caffeine**: 로컬 캐시
- **MyBatis**: SQL 매핑 프레임워크
- **MariaDB**: 관계형 데이터베이스
- **TestContainers**: 통합 테스트 컨테이너

## 실행 방법

### 1. 환경 변수 설정
```bash
# OAuth2 클라이언트 정보 설정
export OAUTH2_GOOGLE_CLIENTID=your-google-client-id
export OAUTH2_GOOGLE_CLIENTSECRET=your-google-client-secret
export OAUTH2_FACEBOOK_CLIENTID=your-facebook-client-id
export OAUTH2_FACEBOOK_CLIENTSECRET=your-facebook-client-secret
export OAUTH2_GITHUB_CLIENTID=your-github-client-id
export OAUTH2_GITHUB_CLIENTSECRET=your-github-client-secret
export OAUTH2_KAKAO_CLIENTID=your-kakao-client-id
```

### 2. Docker 인프라 시작
```bash
# MariaDB + Redis 시작
./docker-manager.sh start chap10

# 상태 확인
./docker-manager.sh status chap10
```

### 3. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap10:bootRun -Dspring.profiles.active=local

# 또는 IDE에서 실행 시
-Dspring.profiles.active=local
```

### 4. 웹 접속
```
http://localhost:8080
```

## 보안 테스트 실행 방법

### 1. 전체 테스트
```bash
./gradlew :chap10:test
```

### 2. OAuth2 통합 테스트
```bash
./gradlew :chap10:test --tests "*OAuth2*"
```

### 3. 캐시 성능 테스트
```bash
./gradlew :chap10:test --tests "*Cache*"
```

## 핵심 보안 학습 포인트

### 1. OAuth2 인증 플로우

```java
@Component
public class PrimaveraAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
        OAuth2User oauth2User = oauth2Token.getPrincipal();
        
        // 사용자 정보 동기화
        UserConnection userConnection = syncUserConnection(registrationId, oauth2User);
        
        // 토큰 캐싱
        cacheOAuth2Token(userConnection);
        
        // 성공 페이지로 리다이렉트
        response.sendRedirect("/dashboard");
    }
}
```

### 2. 프로바이더별 사용자 정보 매핑

```java
public class GoogleUserDetails implements SocialUserDetails {
    
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    
    @Override
    public String getEmail() {
        return getAttribute("email");
    }
    
    @Override
    public String getName() {
        return getAttribute("name");
    }
    
    @Override
    public String getImageUrl() {
        return getAttribute("picture");
    }
    
    @Override
    public ProviderType getProvider() {
        return ProviderType.GOOGLE;
    }
}
```

### 3. 캐시 무효화 전략

```java
@Component
public class CacheEvictionStrategy {
    
    @Scheduled(cron = "0 0 * * * *") // 매시간
    public void cleanupExpiredTokens() {
        oauth2TokenCacheService.cleanupExpired();
    }
    
    @EventListener
    public void handleUserLogout(LogoutSuccessEvent event) {
        String userId = event.getAuthentication().getName();
        userProfileCacheService.evictUserProfile(userId);
        oauth2TokenCacheService.evictAllUserTokens(userId);
    }
    
    @Scheduled(fixedRate = 300000) // 5분마다
    public void monitorCacheHealth() {
        CacheStats stats = caffeineCacheManager.getCache("userProfiles").getNativeCache().stats();
        if (stats.hitRate() < 0.8) {
            // 캐시 히트율이 80% 미만일 경우 알림
            notificationService.sendCacheHealthAlert(stats);
        }
    }
}
```

### 4. Actuator 모니터링

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, caches, oauth2tokens
  endpoint:
    health:
      show-details: always
    caches:
      enabled: true
  metrics:
    cache:
      instrument: true
```

## 학습 순서

1. **OAuth2 기본 개념**
   - Authorization Code Grant 플로우
   - Client Credentials vs Authorization Code
   - Access Token vs Refresh Token

2. **소셜 로그인 구현**
   - Spring Security OAuth2 Client 설정
   - 프로바이더별 사용자 정보 처리
   - 사용자 매핑 및 동기화

3. **캐싱 전략 구현**
   - Redis + Caffeine 하이브리드 설정
   - 토큰 캐싱 및 만료 관리
   - 캐시 무효화 전략

4. **보안 강화**
   - CSRF 보호 설정
   - 세션 관리 및 고정 보호
   - XSS 방어

5. **모니터링 및 운영**
   - Actuator를 통한 상태 모니터링
   - 캐시 성능 메트릭
   - 보안 이벤트 로깅

## 주요 보안 애너테이션

### OAuth2 관련 애너테이션
- `@EnableOAuth2Client`: OAuth2 클라이언트 활성화
- `@RegisteredOAuth2AuthorizedClient`: 인증된 클라이언트 주입
- `@AuthenticationPrincipal`: OAuth2 사용자 정보 주입

### 캐싱 애너테이션
- `@EnableCaching`: 캐싱 활성화
- `@Cacheable`: 캐시 조회
- `@CachePut`: 캐시 갱신
- `@CacheEvict`: 캐시 무효화
- `@CacheConfig`: 캐시 설정

### 모니터링 애너테이션
- `@Timed`: 메트릭 수집
- `@Counted`: 카운터 메트릭
- `@EventListener`: 이벤트 처리

## 다음 단계 안내

Chapter 10을 완료한 후에는 **Chapter 11 (게시판 시스템)**으로 진행하여 다음 내용을 학습합니다:

- OAuth2 기반 게시판 시스템
- 페이징 및 검색 기능
- 파일 업로드 및 다운로드
- 권한 기반 컨텐츠 관리
- Thymeleaf 템플릿 고급 활용

---

OAuth2 소셜 로그인과 캐싱 전략을 마스터한 후 실전 웹 애플리케이션 개발로 도전해보세요!