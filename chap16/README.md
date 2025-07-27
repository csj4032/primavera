# Chapter 16 - Reactive Programming & Advanced Integration

## 개요
Chapter 16은 리액티브 프로그래밍과 고급 시스템 통합을 다루는 Spring Boot 애플리케이션입니다. Spring WebFlux, MongoDB Reactive, Redis 캐싱, 외부 API 통합(Kakao), 그리고 다양한 데이터 저장소 간의 복합적인 데이터 처리를 구현합니다.

## 주요 기능
- **리액티브 프로그래밍**: Spring WebFlux를 활용한 비동기 논블로킹 웹 개발
- **멀티 데이터소스**: MariaDB, MongoDB, Redis 통합 운영
- **외부 API 통합**: Retrofit2를 활용한 Kakao API 연동
- **고급 캐싱**: Redis와 Local Cache를 활용한 다층 캐싱 전략
- **파일 처리**: 첨부파일 업로드/다운로드 및 스토리지 관리
- **실시간 차트**: Reactive WebFlux 기반 스트리밍 차트 데이터
- **OAuth2 보안**: 소셜 로그인 및 JWT 기반 인증
- **로깅 시스템**: MongoDB를 활용한 구조화된 로그 저장

## 기술 스택

### 핵심 프레임워크
- Spring Boot 3.x
- Spring WebFlux (Reactive Web)
- Spring Data JPA
- Spring Data MongoDB Reactive
- Spring Data Redis
- Spring Security OAuth2

### 데이터베이스
- MariaDB 11.x (주 데이터베이스)
- MongoDB (로그 및 NoSQL 데이터)
- Redis (캐싱 및 세션)

### 리액티브 스택
- Reactor Netty
- MongoDB Reactive Driver
- WebFlux Reactive Thymeleaf

### 외부 통합
- Retrofit2 (HTTP Client)
- Kakao API 연동
- OAuth2 Social Login

### 성능 최적화
- Caffeine (Local Cache)
- Kryo (직렬화)
- Snappy (압축)
- jOOλ (함수형 프로그래밍)

### 프론트엔드
- Thymeleaf 템플릿
- AdminLTE UI Framework
- jQuery UI Components
- Chart.js 시각화

## 프로젝트 구조

```
chap16/
├── src/main/java/com/genius/primavera/
│   ├── ReactiveProgrammingApplication.java         # 메인 애플리케이션
│   ├── application/                                # 비즈니스 로직
│   │   ├── cache/                                  # 캐싱 전략
│   │   │   ├── LocalCache.java                   # Caffeine 로컬 캐시
│   │   │   └── RedisCache.java                   # Redis 분산 캐시
│   │   ├── article/                                # 게시글 서비스
│   │   │   ├── WriteArticleService.java          # 게시글 작성 서비스
│   │   │   └── WriteArticleServiceImpl.java      # 구현체
│   │   ├── post/                                   # 포스트 서비스
│   │   │   ├── PostingService.java               # 포스팅 서비스
│   │   │   └── PostingServiceImpl.java           # 구현체
│   │   ├── storage/                                # 파일 스토리지
│   │   │   ├── StorageService.java               # 스토리지 인터페이스
│   │   │   ├── FileSystemStorageService.java     # 파일시스템 구현
│   │   │   └── StorageProperties.java            # 설정 속성
│   │   ├── logging/                                # 로깅 시스템
│   │   │   ├── PrimaveraLogService.java          # 로그 서비스
│   │   │   ├── PrimaveraLogServiceImpl.java      # 구현체
│   │   │   └── MongoSequenceGeneratorService.java # MongoDB 시퀀스
│   │   ├── user/                                   # 사용자 서비스
│   │   │   ├── UserService.java                  # 사용자 서비스
│   │   │   └── UserServiceImpl.java              # 구현체
│   │   └── validator/                              # 검증 로직
│   │       ├── Nickname.java                     # 닉네임 검증 어노테이션
│   │       └── NicknameValidator.java            # 검증기 구현
│   ├── domain/                                     # 도메인 모델
│   │   ├── model/                                  # 엔티티 모델
│   │   │   ├── BaseEntity.java                   # 공통 엔티티
│   │   │   ├── user/                              # 사용자 도메인
│   │   │   │   ├── User.java                     # 사용자 엔티티
│   │   │   │   ├── Role.java                     # 역할 엔티티
│   │   │   │   ├── UserConnection.java           # 소셜 연결
│   │   │   │   └── UserStatus.java               # 사용자 상태
│   │   │   ├── article/                           # 게시글 도메인
│   │   │   │   ├── Article.java                  # 게시글 엔티티
│   │   │   │   ├── Comment.java                  # 댓글 엔티티
│   │   │   │   ├── Attachment.java               # 첨부파일
│   │   │   │   └── ArticleStatus.java            # 게시글 상태
│   │   │   ├── post/                              # 포스트 도메인
│   │   │   │   ├── Post.java                     # 포스트 엔티티
│   │   │   │   └── PostStatus.java               # 포스트 상태
│   │   │   ├── kakao/                             # 카카오 도메인
│   │   │   │   └── KakaoFriend.java              # 카카오 친구
│   │   │   └── PrimaveraLog.java                 # 로그 엔티티 (MongoDB)
│   │   ├── converter/                              # JPA 컨버터
│   │   │   ├── EnumAttributeConverter.java       # Enum 컨버터 기반
│   │   │   ├── UserStatusAttributeConverter.java  # 사용자 상태 컨버터
│   │   │   └── ArticleStatusAttributeConverter.java # 게시글 상태 컨버터
│   │   └── repository/                             # 저장소 인터페이스
│   │       ├── UserRepository.java               # 사용자 저장소
│   │       ├── PrimaveraLogRepository.java       # 로그 저장소 (MongoDB)
│   │       ├── article/                           # 게시글 저장소
│   │       │   ├── ArticleRepository.java        # 게시글 저장소
│   │       │   └── CommentRepository.java        # 댓글 저장소
│   │       └── post/                              # 포스트 저장소
│   │           └── PostRepository.java           # 포스트 저장소
│   ├── infrastructure/                             # 인프라스트럭처
│   │   ├── ApplicationConfiguration.java          # 애플리케이션 설정
│   │   ├── KakaoRetrofitClientConfiguration.java  # Kakao API 설정
│   │   ├── aspect/                                # AOP 관점
│   │   │   ├── PrimaveraLogging.java             # 로깅 어노테이션
│   │   │   └── PrimaveraLoggingAspect.java       # 로깅 관점
│   │   ├── security/                              # 보안 설정
│   │   │   ├── PrimaveraSecurityConfiguration.java # 보안 설정
│   │   │   ├── PrimaveraUserDetailsService.java   # 사용자 세부정보 서비스
│   │   │   └── PrimaveraAuthenticationSuccessHandler.java # 인증 성공 핸들러
│   │   ├── serializer/                            # 직렬화
│   │   │   ├── KryoRedisSerializer.java          # Kryo Redis 직렬화
│   │   │   └── SnappyRedisSerializer.java        # Snappy 압축 직렬화
│   │   └── filter/                                # 필터
│   │       └── PrimaveraFilter.java              # 커스텀 필터
│   └── interfaces/                                 # 웹 계층
│       ├── ArticleController.java                 # 게시글 컨트롤러
│       ├── PostingController.java                 # 포스팅 컨트롤러
│       ├── ChartController.java                   # 차트 컨트롤러 (Reactive)
│       ├── AttachmentController.java              # 첨부파일 컨트롤러
│       ├── UserController.java                    # 사용자 컨트롤러
│       └── LoginController.java                   # 로그인 컨트롤러
└── src/main/resources/
    ├── application-default.yml                    # 기본 설정
    ├── application-local.yml                      # 로컬 환경 설정
    ├── social.yml                                 # 소셜 로그인 설정
    ├── templates/                                 # Thymeleaf 템플릿
    │   ├── layouts/                               # 레이아웃
    │   ├── fragments/                             # 프래그먼트
    │   ├── article/                               # 게시글 템플릿
    │   ├── post/                                  # 포스트 템플릿
    │   └── chart.html                            # 리액티브 차트
    └── static/                                    # 정적 리소스
        ├── plugins/                               # jQuery 플러그인
        │   ├── bootstrap-slider/
        │   ├── iCheck/
        │   └── jvectormap/
        └── dist/                                  # 빌드된 자산
```

## 주요 컴포넌트

### 1. 리액티브 웹 시스템
- **ChartController**: WebFlux 기반 스트리밍 차트 데이터
- **MongoDB Reactive**: 비동기 NoSQL 데이터 처리
- **Thymeleaf Reactive**: 리액티브 템플릿 렌더링

### 2. 다층 캐싱 시스템
- **LocalCache (Caffeine)**: JVM 레벨 고속 캐싱
- **RedisCache**: 분산 캐싱 및 세션 저장소
- **Cache Chain**: 로컬 → Redis → DB 순차 조회 최적화

### 3. 멀티 데이터소스 통합
- **MariaDB**: 주요 관계형 데이터 (JPA)
- **MongoDB**: 로그 및 NoSQL 데이터 (Reactive)
- **Redis**: 캐싱 및 세션 관리

### 4. 외부 시스템 통합
- **Kakao API**: Retrofit2를 통한 친구 목록 조회
- **OAuth2 Social Login**: Google, Facebook, GitHub, Kakao 로그인
- **File Storage**: 첨부파일 업로드/다운로드 시스템

### 5. 고급 데이터 처리
- **QueryDSL**: 타입 안전 동적 쿼리
- **JPA Envers**: 엔티티 변경 이력 추적
- **Custom Converters**: Enum 기반 상태 변환

## 설정

### 멀티 데이터소스 설정
```yaml
spring:
  datasource:                    # MariaDB
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/primavera
  data:
    mongodb:                     # MongoDB
      host: localhost
      port: 27017
      database: primavera
    redis:                       # Redis
      host: localhost
      port: 6379
```

### HashiCorp Vault 설정

#### 개발 환경 Vault 구성
```yaml
spring:
  cloud:
    vault:
      host: localhost
      port: 8200
      scheme: http
      authentication: TOKEN
      token: primavera-dev-token
      kv:
        enabled: true
        backend: secret
        profile-separator: '/'
```

#### Vault 시크릿 엔진 초기화
```bash
# Key-Value v2 시크릿 엔진 활성화
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=primavera-dev-token

vault secrets enable -path=secret kv-v2

# 애플리케이션 시크릿 저장
vault kv put secret/primavera/chap16 \
  datasource.password=primavera \
  mongodb.password=primavera \
  oauth2.google.client-secret=your-google-secret \
  oauth2.kakao.client-secret=your-kakao-secret \
  jwt.secret=your-jwt-secret-key

# 환경별 시크릿 저장
vault kv put secret/primavera/chap16/local \
  datasource.url=jdbc:mariadb://localhost:3306/primavera \
  mongodb.host=localhost

vault kv put secret/primavera/chap16/prod \
  datasource.url=jdbc:mariadb://prod-db:3306/primavera \
  mongodb.host=prod-mongo
```

#### Vault 시크릿 조회 및 관리
```bash
# 저장된 시크릿 조회
vault kv get secret/primavera/chap16
vault kv get secret/primavera/chap16/local

# 시크릿 버전 확인
vault kv metadata get secret/primavera/chap16

# 시크릿 업데이트
vault kv patch secret/primavera/chap16 jwt.secret=new-secret-key

# 시크릿 삭제
vault kv delete secret/primavera/chap16
```

### 리액티브 Thymeleaf 설정
```yaml
spring:
  thymeleaf:
    reactive:
      chunked-mode-view-names: chart
      max-chunk-size: 8192
```

### Kakao API 설정
```yaml
kakao:
  api:
    url: https://kapi.kakao.com
    talk-social:
      friend-list: v1/api/talk/friends
```

### SSL/HTTPS 설정
```yaml
server:
  ssl:
    key-alias: primavera
    key-store: chap10/primavera.p12
    key-store-type: PKCS12
    enabled: true
  port: 8443
```

### Spring Cloud Vault 통합 설정

#### application-vault.yml
```yaml
spring:
  cloud:
    vault:
      host: localhost
      port: 8200
      scheme: http
      authentication: TOKEN
      token: primavera-dev-token
      connection-timeout: 5000
      read-timeout: 15000
      config:
        order: -10
      generic:
        enabled: false
      kv:
        enabled: true
        backend: secret
        profile-separator: '/'
        default-context: primavera/chap16
        application-name: primavera
        profiles: local,prod
  config:
    import: vault://
```

#### VaultConfiguration.java 예시
```java
@Configuration
@EnableConfigurationProperties
public class VaultConfiguration {
    
    @Value("${spring.cloud.vault.token}")
    private String vaultToken;
    
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.create("localhost", 8200);
        endpoint.setScheme("http");
        
        ClientAuthentication authentication = new TokenAuthentication(vaultToken);
        
        return new VaultTemplate(endpoint, authentication);
    }
    
    @ConfigurationProperties("datasource")
    @Component
    public static class DatabaseProperties {
        private String url;
        private String username;
        private String password;
        // getters/setters
    }
    
    @ConfigurationProperties("oauth2")
    @Component
    public static class OAuth2Properties {
        private Google google = new Google();
        private Kakao kakao = new Kakao();
        
        public static class Google {
            private String clientId;
            private String clientSecret;
            // getters/setters
        }
        
        public static class Kakao {
            private String clientId;
            private String clientSecret;
            // getters/setters
        }
    }
}
```

## 실행 방법

### 로컬 환경 실행
```bash
./gradlew :chap16:bootRun -Dspring.profiles.active=local
```

### 필수 서비스 시작
```bash
# MariaDB 시작
docker run -d --name mariadb-primavera -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  mariadb:11.4.7

# MongoDB 시작
docker run -d --name mongodb-primavera -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=primavera \
  -e MONGO_INITDB_ROOT_PASSWORD=primavera \
  mongo:7.0

# Redis 시작
docker run -d --name redis-primavera -p 6379:6379 redis:7.2

# HashiCorp Vault 시작 (개발 모드)
docker run -d --name vault-primavera -p 8200:8200 \
  -e VAULT_DEV_ROOT_TOKEN_ID=primavera-dev-token \
  -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 \
  --cap-add=IPC_LOCK \
  hashicorp/vault:1.15

# Vault 초기화 확인
curl -H "X-Vault-Token: primavera-dev-token" \
  http://localhost:8200/v1/sys/health
```

### 테스트 실행
```bash
./gradlew :chap16:test
```

## API 엔드포인트

### 게시글 관리
```
GET    /articles          # 게시글 목록
GET    /articles/{id}     # 게시글 상세
POST   /articles          # 게시글 작성
PUT    /articles/{id}     # 게시글 수정
DELETE /articles/{id}     # 게시글 삭제
```

### 리액티브 차트
```
GET    /chart             # 리액티브 차트 페이지 (SSE)
```

### 첨부파일
```
POST   /attachments       # 파일 업로드
GET    /attachments/{id}  # 파일 다운로드
```

### 사용자 관리
```
GET    /users             # 사용자 목록
GET    /users/profile     # 프로필 조회
POST   /users/register    # 회원가입
```

## 테스트

### 주요 테스트 클래스
- **CacheChainTest**: 다층 캐싱 전략 테스트
- **KakaoFriendTest**: Kakao API 통합 테스트
- **RedisMultiInsertTest**: Redis 대량 삽입 성능 테스트
- **ArticleRepositoryTest**: 게시글 저장소 테스트
- **PostingControllerTest**: 포스팅 컨트롤러 통합 테스트

### TestContainers 활용
```java
@SpringBootTest
@Testcontainers
class AbstractJpaContainerTest {
    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7");
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2");
}
```

## 성능 최적화

### 캐싱 전략
1. **L1 Cache (Caffeine)**: JVM 내 고속 메모리 캐시
2. **L2 Cache (Redis)**: 분산 환경 공유 캐시
3. **Cache Aside Pattern**: 캐시 미스 시 DB 조회 후 캐시 저장

### 직렬화 최적화
- **Kryo**: 바이너리 직렬화로 성능 향상
- **Snappy**: 고속 압축으로 네트워크 대역폭 절약

### 리액티브 스트리밍
- **Backpressure**: 데이터 생산/소비 속도 조절
- **Non-blocking I/O**: 높은 동시성 처리

## 특징

1. **하이브리드 아키텍처**: 전통적 MVC와 리액티브 패러다임 결합
2. **멀티 스토어 전략**: 각 데이터 특성에 맞는 최적 저장소 선택
3. **고가용성 캐싱**: 장애 상황에서도 안정적인 데이터 제공
4. **실시간 데이터**: WebFlux 기반 실시간 차트 및 알림
5. **외부 시스템 통합**: 다양한 외부 API와의 안정적 연동
6. **보안 강화**: OAuth2 + JWT 기반 다중 인증 체계
7. **확장 가능**: 마이크로서비스 전환 가능한 모듈 구조

## 민감정보 관리

### HashiCorp Vault 보안 가이드라인

#### 프로덕션 환경 설정
```bash
# 프로덕션용 Vault 서버 실행 (TLS 활성화)
docker run -d --name vault-prod \
  -p 8200:8200 \
  -v vault-data:/vault/data \
  -v vault-config:/vault/config \
  --cap-add=IPC_LOCK \
  hashicorp/vault:1.15 \
  vault server -config=/vault/config/vault.hcl

# 프로덕션용 설정 파일 (vault.hcl)
storage "file" {
  path = "/vault/data"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_cert_file = "/vault/config/vault.crt"
  tls_key_file = "/vault/config/vault.key"
}

api_addr = "https://vault.primavera.com:8200"
cluster_addr = "https://vault.primavera.com:8201"
ui = true
```

#### 시크릿 로테이션 전략
```bash
# 데이터베이스 패스워드 로테이션
vault write secret/primavera/chap16 \
  datasource.password=$(openssl rand -base64 32) \
  mongodb.password=$(openssl rand -base64 32)

# JWT 시크릿 로테이션 (주기적 실행)
vault kv patch secret/primavera/chap16 \
  jwt.secret=$(openssl rand -base64 64)

# API 키 로테이션
vault kv patch secret/primavera/chap16 \
  oauth2.google.client-secret=new-google-secret \
  oauth2.kakao.client-secret=new-kakao-secret
```

#### 접근 정책 설정
```bash
# 애플리케이션용 정책 생성
vault policy write primavera-app - <<EOF
path "secret/data/primavera/chap16/*" {
  capabilities = ["read"]
}
path "secret/metadata/primavera/chap16/*" {
  capabilities = ["list", "read"]
}
EOF

# 개발자용 정책 생성
vault policy write primavera-dev - <<EOF
path "secret/data/primavera/chap16/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
EOF

# 토큰 생성
vault token create -policy="primavera-app" -ttl=24h
vault token create -policy="primavera-dev" -ttl=8h
```

## 주의사항

1. 로컬 환경에서는 MariaDB (포트 3306), MongoDB (포트 27017), Redis (포트 6379), Vault (포트 8200) 필요
2. Kakao API 연동 시 유효한 API 키를 Vault에 저장하여 관리
3. HTTPS 사용 시 인증서 파일(primavera.p12) 경로 확인 필요
4. 리액티브 스택과 전통적 JPA의 혼용으로 트랜잭션 관리 주의
5. 캐시 일관성 보장을 위한 적절한 TTL 및 무효화 전략 설정 필요
6. **Vault 보안**: 프로덕션에서는 반드시 TLS 활성화 및 토큰 기반 인증 사용
7. **시크릿 로테이션**: 정기적인 패스워드 및 API 키 로테이션 정책 수립
8. **접근 제어**: 최소 권한 원칙에 따른 Vault 정책 설정