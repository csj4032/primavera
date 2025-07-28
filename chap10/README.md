## chap10 - OAuth2 Social Login Application

### 개요
Spring Boot 3.x와 Spring Security 6.x를 활용한 소셜 로그인(OAuth2) 통합 애플리케이션입니다. Google, Facebook, GitHub, Kakao 4개 소셜 로그인 프로바이더를 지원하며, HTTPS(SSL/TLS) 보안 연결과 XSS 방어를 위한 Lucy Filter가 적용되어 있습니다.

### 주요 기능
- **OAuth2 소셜 로그인**: Google, Facebook, GitHub, Kakao 계정으로 로그인
- **통합 사용자 관리**: 소셜 계정을 내부 사용자 시스템과 연동
- **보안 강화**: HTTPS 필수 적용, Lucy XSS Filter 통합
- **세션 관리**: 소셜 로그인 토큰 관리 및 자동 갱신
- **사용자 프로필 동기화**: 소셜 프로필 정보 자동 업데이트

### 아키텍처 특징

#### 1. OAuth2 통합 구조
- **Spring Security OAuth2 Client**: 표준 OAuth2 클라이언트 구현
- **커스텀 OAuth2UserService**: 각 프로바이더별 사용자 정보 처리
- **UserConnection 모델**: 소셜 계정과 내부 사용자 연결 관리
- **Provider별 UserDetails**: Google, Facebook, GitHub, Kakao 전용 모델

#### 2. 보안 설정
- **SSL/TLS 필수**: PKCS12 인증서 기반 HTTPS 적용
- **CSRF 보호**: 상태 변경 요청에 대한 CSRF 토큰 검증
- **XSS 방어**: Lucy Filter를 통한 악성 스크립트 차단
- **접근 제어**: URL 패턴 기반 권한 관리

#### 3. 모듈 구조
```
chap10/
├── infrastructure/
│   ├── security/
│   │   ├── PrimaveraSecurityConfiguration.java    # Spring Security 설정
│   │   ├── PrimaveraUserDetailsService.java       # 일반 로그인 처리
│   │   └── social/
│   │       ├── PrimaveraSocialConfiguration.java  # OAuth2 설정
│   │       └── provider별 UserDetails 클래스
│   └── filter/
│       └── PrimaveraFilter.java                   # 커스텀 필터
├── domain/
│   └── model/
│       └── UserConnection.java                    # 소셜 연동 정보
└── interfaces/
    └── LoginController.java                       # 로그인 화면 제어
```

### build.gradle 의존성 추가

```gradle
dependencies {
    // Spring Security OAuth2 Client
    implementation "org.springframework.boot:spring-boot-starter-oauth2-client"
    implementation "org.springframework.security:spring-security-oauth2-client"
    implementation "org.springframework.security:spring-security-oauth2-jose"
    
    // Thymeleaf Security 통합
    implementation "org.thymeleaf.extras:thymeleaf-extras-springsecurity6"
    
    // XSS 방어
    implementation project(":appendix:spring-boot-starter-lucy-filter")
}
```

### application.yml 설정
```yaml
# SSL/TLS 설정
server:
  ssl:
    key-alias: primavera
    key-store: infrastructure/certs/primavera.p12
    key-store-type: PKCS12
    key-store-password: primavera
    enabled: true
  port: 8443

# Spring Security OAuth2 Client 설정 (Spring Boot 3.x 방식)
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
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
          github:
            client-id: ${OAUTH2_GITHUB_CLIENTID}
            client-secret: ${OAUTH2_GITHUB_CLIENTSECRET}
            scope:
              - user:email
              - read:user
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
          kakao:
            client-id: ${OAUTH2_KAKAO_CLIENTID}
            client-secret: ${OAUTH2_KAKAO_CLIENTSECRET:}
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
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

### OAuth2 인증 흐름

#### 1. 로그인 프로세스
1. 사용자가 소셜 로그인 버튼 클릭
2. OAuth2 프로바이더 인증 페이지로 리다이렉트
3. 사용자 인증 및 권한 동의
4. Authorization Code와 함께 콜백 URL로 리다이렉트
5. Authorization Code를 Access Token으로 교환
6. Access Token으로 사용자 정보 조회
7. 내부 사용자 시스템과 연동 및 세션 생성

#### 2. 핵심 컴포넌트
- **PrimaveraSocialConfiguration**: OAuth2UserService 빈 정의
- **OAuth2UserService**: 프로바이더별 사용자 정보 처리
- **UserConnection**: 소셜 계정 연동 정보 저장
- **PrimaveraSocialUserDetailsService**: 소셜 사용자 인증 처리

#### 3. 프로바이더별 처리
- **Google**: email, name, picture 정보 수집
- **Facebook**: id, email, name 정보 수집  
- **GitHub**: login, email, name, avatar_url 정보 수집
- **Kakao**: id, kakao_account(email, profile) 정보 수집

### SSL/TLS 설정

#### 인증서 생성
```bash
# infrastructure/certs 디렉토리에서 실행
keytool -genkeypair -alias primavera -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore primavera.p12 -validity 3650
```

#### 로컬 개발 환경 설정
hosts 파일 수정 (선택사항):
```
127.0.0.1       local.primavera.com
```

#### 소셜 프로바이더 콜백 URL 설정
각 프로바이더의 개발자 콘솔에서 다음 콜백 URL을 등록해야 합니다:
- **Google**: `https://localhost:8443/login/oauth2/code/google`
- **Facebook**: `https://localhost:8443/login/oauth2/code/facebook`
- **GitHub**: `https://localhost:8443/login/oauth2/code/github`
- **Kakao**: `https://localhost:8443/login/oauth2/code/kakao`

### 환경 변수 설정
OAuth2 클라이언트 정보는 환경 변수로 관리합니다:
```bash
export OAUTH2_GOOGLE_CLIENTID=your-google-client-id
export OAUTH2_GOOGLE_CLIENTSECRET=your-google-client-secret
export OAUTH2_FACEBOOK_CLIENTID=your-facebook-client-id
export OAUTH2_FACEBOOK_CLIENTSECRET=your-facebook-client-secret
export OAUTH2_GITHUB_CLIENTID=your-github-client-id
export OAUTH2_GITHUB_CLIENTSECRET=your-github-client-secret
export OAUTH2_KAKAO_CLIENTID=your-kakao-client-id
```

### 실행 방법
```bash
# MariaDB 컨테이너 실행 (infrastructure 디렉토리)
docker-compose up -d mariadb

# 애플리케이션 실행
./gradlew :chap10:bootRun --args='--spring.profiles.active=local'

# 브라우저에서 접속
https://localhost:8443
```

### 데이터베이스 스키마
소셜 로그인 정보를 저장하는 USER_CONNECTION 테이블:
```sql
CREATE TABLE USER_CONNECTION (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL VARCHAR(255) NOT NULL,
    PROVIDER VARCHAR(100) NOT NULL,
    PROVIDER_USER_ID VARCHAR(255) NOT NULL,
    DISPLAY_NAME VARCHAR(255),
    PROFILE_URL VARCHAR(512),
    IMAGE_URL VARCHAR(512),
    ACCESS_TOKEN VARCHAR(512) NOT NULL,
    EXPIRE_TIME BIGINT,
    REG_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    MOD_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY UK_USER_CONNECTION (EMAIL, PROVIDER)
);
```

### 주의사항
- **HTTPS 필수**: OAuth2 프로바이더는 보안상 HTTPS 콜백 URL만 허용
- **환경 변수**: 클라이언트 시크릿은 절대 코드에 하드코딩하지 말 것
- **토큰 관리**: Access Token은 암호화하여 저장 권장
- **세션 보안**: 소셜 로그인 후 새로운 세션 ID 생성 필요

### 참고 자료
- [Spring Security OAuth2 공식 문서](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Google OAuth2 개발자 가이드](https://developers.google.com/identity/protocols/oauth2)
- [Facebook 로그인 구현 가이드](https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow/)
- [GitHub OAuth Apps 가이드](https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps)
- [Kakao 로그인 API 문서](https://developers.kakao.com/docs/latest/ko/kakaologin/common)
- [Spring Security 6.x 마이그레이션 가이드](https://github.com/spring-projects/spring-security/wiki/OAuth-2.0-Migration-Guide)