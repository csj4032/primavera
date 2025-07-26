# 🌸 Primavera - Spring Boot 종합 학습 프로젝트

[![Build Status](https://travis-ci.org/csj4032/primavera.svg?branch=master)](https://travis-ci.org/csj4032/primavera)
[![Coverage Status](https://coveralls.io/repos/github/csj4032/primavera/badge.svg)](https://coveralls.io/github/csj4032/primavera)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Spring Boot를 이용한 현대적인 웹 애플리케이션 개발을 체계적으로 학습할 수 있는 종합 프로젝트입니다. 기초부터 고급 기술까지 단계별로 구성된 17개 챕터를 통해 실무에 필요한 모든 기술을 습득할 수 있습니다.

## 🛠️ 기술 스택

### Core Framework
- **Java 21** (Switch expressions, Text blocks, Records)
- **Spring Boot 3.5.3** (최신 LTS 버전)
- **Spring Security 6.4.4** with OAuth2 Client
- **Spring Cloud 2024.0.1**
- **Gradle 8.12.1**

### Database & Persistence
- **MySQL/MariaDB 11.4.7** (Primary Database)
- **Redis** (Caching & Session Storage)
- **PostgreSQL** (Advanced Features)
- **H2** (Testing)
- **MyBatis 3.0.4** with Dynamic SQL
- **JPA/Hibernate** (ORM)
- **Flyway** (Database Migration)

### Security & Authentication
- **Spring Security 6.4.4**
- **OAuth2 Client** (Google, Facebook, GitHub, Kakao)
- **Lucy XSS Filter 2.0.1** (XSS Protection)
- **SSL/HTTPS** with PKCS12
- **JWT Token** Support

### Web & Template
- **Thymeleaf 3.4.0** (Server-side Rendering)
- **Bootstrap 5.3.3** (UI Framework)
- **AdminLTE** (Admin Dashboard)
- **WYSIHTML5** (Rich Text Editor)
- **WebFlux** (Reactive Programming)

### Testing & Quality
- **JUnit 5** (Unit Testing)
- **TestContainers** (Integration Testing)
- **MockMvc** (Web Layer Testing)
- **Lombok 1.18.36** (Code Generation)
- **SonarQube** (Code Quality)

### Infrastructure & Deployment
- **Docker** (Containerization)
- **GitHub Actions** (CI/CD)
- **Travis CI** (Legacy CI/CD)
- **AWS EKS** (Kubernetes)
- **ArgoCD** (GitOps)
- **Prometheus & Grafana** (Monitoring)
- **Sentry** (Error Tracking)
- **Undertow** (Embedded Server)

## 🏗️ 프로젝트 아키텍처

```
primavera/
├── 📖 Preface
│   └── preface/            # Spring Boot 서문 및 소개
├── 📚 Learning Modules
│   ├── chap01-05/          # 🎯 기초: Spring Boot 핵심 개념
│   ├── chap06-09/          # 🔧 중급: 데이터, 보안, 템플릿
│   ├── chap10-14/          # 🚀 고급: OAuth2, 마이크로서비스
│   └── chap15-18/          # 💼 실무: 배포, 모니터링, 최적화
├── 🧩 Appendix
│   ├── appendix/
│   │   ├── spring-boot-starter-lucy-filter/    # XSS 보안 필터
│   │   └── spring-boot-starter-social-kakao/   # 카카오 소셜 로그인
└── 🔧 Infrastructure
    ├── config/             # 환경별 설정 파일
    ├── docker/             # Docker 컨테이너 구성
    └── k8s/                # Kubernetes 매니페스트
```

## 📊 데이터베이스 스키마

![Primavera DB Schema](https://github.com/csj4032/primavera/blob/master/primavera.png)

## 🚀 빠른 시작

### 1. 환경 요구사항
```bash
# Java 21 설치 확인
java -version

# Docker 설치 확인
docker --version
docker-compose --version
```

### 2. 프로젝트 클론 및 실행
```bash
# 프로젝트 클론
git clone https://github.com/csj4032/primavera.git
cd primavera

# 데이터베이스 실행 (Docker)
docker-compose up -d mysql redis

# 특정 챕터 실행 (예: chap10)
./gradlew :chap10:bootRun

# 전체 빌드 및 테스트
./gradlew clean build
```

## 🧪 테스팅 환경 가이드

### Profile 기반 자동 데이터베이스 선택
Primavera는 Spring Profile에 따라 **자동으로** 데이터베이스 환경을 선택합니다:

| Profile | 데이터베이스 | 용도 | 실행 방법 |
|---------|-------------|------|-----------|
| **`local`** | 🐳 **localhost Docker MySQL 8.4.0** | 로컬 개발, 디버깅 | `./gradlew :chapXX:bootRun -Dspring.profiles.active=local` |
| **`test`** | 🧪 **TestContainers MySQL 8.4.0** | 자동화 테스트, CI/CD | `./gradlew :chapXX:test` |

### 🏠 로컬 개발 환경 설정

#### 1. Docker MySQL 8.4.0 시작
```bash
# MySQL 컨테이너 실행 (한 번만 실행)
docker run -d \
  --name mysql-primavera-local \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=primavera \
  -e MYSQL_USER=primavera \
  -e MYSQL_PASSWORD=primavera \
  -p 3306:3306 \
  --restart=unless-stopped \
  mysql:8.4.0

# 컨테이너 상태 확인
docker ps | grep mysql-primavera-local
```

#### 2. 로컬 개발 서버 실행
```bash
# 특정 챕터를 로컬 환경에서 실행
./gradlew :chap11:bootRun -Dspring.profiles.active=local

# 애플리케이션 접속
open http://localhost:8080
```

#### 3. IDE 설정 (IntelliJ IDEA)
1. `Run/Debug Configurations` 선택
2. `VM Options`에 추가: `-Dspring.profiles.active=local`
3. 또는 `Program Arguments`에 추가: `--spring.profiles.active=local`

### 🧪 테스트 환경 실행

#### 1. 자동화 테스트 (TestContainers)
```bash
# 모든 테스트 실행 (TestContainers 자동 관리)
./gradlew :chap11:test

# 특정 테스트 클래스 실행
./gradlew :chap11:test --tests ArticleMapperProfileTest

# 테스트 결과 확인
./gradlew :chap11:test --continue
```

#### 2. Profile 기반 통합 테스트 작성
```java
@ProfileBasedIntegrationTest
@ActiveProfiles("test")  // TestContainers MySQL 자동 사용
@DisplayName("Article 통합 테스트")
class ArticleIntegrationTest {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Test
    @DisplayName("게시글 저장 및 조회")
    void shouldSaveAndRetrieveArticle() {
        // 실제 MySQL 8.4.0에서 테스트 (TestContainers)
        Article article = Article.builder()
            .subject("테스트 게시글")
            .status(ArticleStatus.PUBLIC)
            .build();
            
        int result = articleMapper.save(article);
        assertEquals(1, result);
    }
}
```

### 🚀 환경별 실행 요약

#### 로컬 개발 워크플로우
```bash
# 1. MySQL 컨테이너 시작 (최초 1회)
docker start mysql-primavera-local

# 2. 로컬 환경으로 애플리케이션 실행
./gradlew :chap11:bootRun -Dspring.profiles.active=local

# 3. 브라우저에서 확인
# http://localhost:8080
```

#### 테스트 실행 워크플로우  
```bash
# TestContainers가 자동으로 MySQL 컨테이너 관리
./gradlew :chap11:test

# 테스트 완료 후 컨테이너 자동 정리
# (추가 설정 불필요)
```

### 💡 주요 특징

✅ **환경 자동 선택**: Profile만 지정하면 DB 환경 자동 결정  
✅ **Docker 기반**: 모든 환경에서 MySQL 8.4.0 동일 버전 사용  
✅ **CI/CD 친화적**: TestContainers로 외부 의존성 없는 테스트  
✅ **개발 효율성**: 로컬은 빠른 개발, 테스트는 격리된 환경  
✅ **버전 일관성**: 개발/테스트/프로덕션 동일한 MySQL 8.4.0  

### 3. 개발 환경 설정
![IntelliJ, Gradle](https://github.com/csj4032/primavera/blob/master/gradle.png)

## 📚 학습 로드맵

### 🎯 Phase 1: Spring Boot 기초 (chap01-05)

#### **Chapter 01** - Spring Boot 시작하기
- **학습 목표**: Spring Boot 핵심 개념 이해
- **주요 내용**:
  - `@SpringBootApplication` 어노테이션 분석
  - `@EnableAutoConfiguration` 동작 원리
  - `SpringApplicationBuilder` 활용법
  - Domain-Driven Design 기초
- **핵심 파일**: `PrimaveraApplication.java`

#### **Chapter 02** - 설정과 의존성 주입
- **학습 목표**: Spring Boot 설정 시스템 마스터
- **주요 내용**:
  - `@ConfigurationProperties`를 통한 타입 안전한 설정
  - YAML 구성 파일 활용
  - Bean Scope와 라이프사이클
  - 프로필별 환경 설정
- **핵심 클래스**: `PrimaveraProperties`, `PrimaveraConfiguration`

#### **Chapter 03** - MVC와 AOP
- **학습 목표**: 웹 계층 및 관점 지향 프로그래밍 구현
- **주요 내용**:
  - Spring MVC 아키텍처 패턴
  - AOP를 통한 횡단 관심사 분리
  - 인터셉터와 필터 체인
  - `@Aspect`, `@Around` 활용
- **핵심 클래스**: `PrimaveraLoggingAspect`, `PrimaveraInterceptor`

#### **Chapter 04** - 데이터 접근 계층
- **학습 목표**: 데이터베이스 연동 및 트랜잭션 관리
- **주요 내용**:
  - HikariCP 커넥션 풀 최적화
  - JdbcTemplate을 통한 SQL 실행
  - 다중 데이터소스 구성
  - 선언적 트랜잭션 관리
- **핵심 클래스**: `UserDao`, `PrimaveraDao`

#### **Chapter 05** - MyBatis와 로깅
- **학습 목표**: ORM 프레임워크와 로깅 시스템 구축
- **주요 내용**:
  - MyBatis 매퍼 어노테이션 기반 구성
  - 동적 SQL 구현
  - Logback 설정 및 커스터마이징
  - SQL 로깅 및 성능 모니터링
- **주요 설정**: `logback-spring.xml`, MyBatis 매퍼

### 🔧 Phase 2: 중급 웹 개발 (chap06-09)

#### **Chapter 06** - 고급 유효성 검증 ⭐ *Enhanced*
- **학습 목표**: 엔터프라이즈급 데이터 검증 시스템 구축
- **주요 내용**:
  - Jakarta Bean Validation (JSR-380)
  - 커스텀 검증 어노테이션 개발
  - 검증 그룹을 통한 상황별 검증
  - GraalVM JavaScript 통합 검증
  - TestContainers 기반 통합 테스트
- **혁신 기능**:
  - `@ScriptAssert`를 통한 복잡한 비즈니스 규칙 검증
  - Docker 기반 MySQL 테스트 환경
  - 실시간 유효성 검증 피드백
- **핵심 클래스**: `NicknameValidator`, `AbstractContainerTest`

#### **Chapter 07** - Thymeleaf와 JPA
- **학습 목표**: 서버사이드 렌더링과 ORM 구현
- **주요 내용**:
  - Thymeleaf 템플릿 엔진 마스터
  - AdminLTE 기반 관리자 대시보드
  - JPA 엔티티 매핑 및 연관관계 설정
  - Log4Jdbc를 통한 SQL 모니터링
- **UI/UX**: 반응형 관리자 인터페이스 구현

#### **Chapter 08** - 보안 필터와 XSS 방어
- **학습 목표**: 웹 보안 강화 및 필터 체인 구현
- **주요 내용**:
  - Lucy XSS Filter를 통한 XSS 공격 차단
  - 커스텀 서블릿 필터 개발
  - Undertow 서버 최적화
  - 보안 헤더 설정
- **보안 강화**: 다층 보안 아키텍처 구현

#### **Chapter 09** - Spring Security 기초
- **학습 목표**: 인증 및 권한 관리 시스템 구축
- **주요 내용**:
  - Spring Security 필터 체인 이해
  - 인메모리 사용자 관리
  - BCrypt 패스워드 암호화
  - CSRF 보호 메커니즘
  - 커스텀 Spring Boot Starter 개발
- **디자인 패턴**: Chain of Responsibility 패턴 적용

### 🚀 Phase 3: 고급 기능 구현 (chap10-14)

#### **Chapter 10** - OAuth2 소셜 로그인 & HTTPS ⭐ *Currently Active*
- **학습 목표**: 현대적인 인증 시스템 및 보안 통신 구현
- **주요 내용**:
  - **다중 OAuth2 제공자**: Google, Facebook, GitHub, Kakao 통합
  - **HTTPS/SSL 구성**: PKCS12 키스토어 및 자체 서명 인증서
  - **Spring Security 6.4.4**: 최신 보안 설정 및 필터 체인
  - **역할 기반 접근 제어**: USER, MANAGER, ADMINISTRATOR 권한 관리
- **고급 기능**:
  - 소셜 사용자 정보 매핑 및 통합
  - OAuth2 토큰 관리 및 갱신
  - 환경별 클라이언트 자격 증명 관리
  - 도메인 주도 설계 원칙 적용

#### **Chapter 11** - 게시판 시스템
- **학습 목표**: 완전한 CRUD 기능을 가진 게시판 구현
- **주요 내용**:
  - RESTful API 설계
  - 페이징 및 정렬 구현
  - WYSIHTML5 리치 텍스트 에디터
  - 권한별 접근 제어
  - MockMvc 기반 통합 테스트

#### **Chapter 12** - 계층형 댓글 & Flyway
- **학습 목표**: 복잡한 데이터 구조 및 데이터베이스 마이그레이션
- **주요 내용**:
  - 계층형 댓글 시스템 설계
  - 트리 구조 쿼리 최적화
  - Flyway 데이터베이스 마이그레이션
  - 재귀적 데이터 처리 알고리즘

#### **Chapter 13** - 고급 권한 관리
- **학습 목표**: 메서드 수준 보안 및 파일 처리
- **주요 내용**:
  - `@PreAuthorize`, `@PostAuthorize` 활용
  - SpEL을 통한 동적 권한 검사
  - 파일 업로드/다운로드 구현
  - 이미지 리사이징 및 최적화

#### **Chapter 14** - JPA 고급 & 외부 API
- **학습 목표**: 고급 ORM 기능 및 외부 서비스 통합
- **주요 내용**:
  - JPA/Hibernate 고급 매핑
  - JPQL 및 Criteria API
  - ModelMapper를 통한 DTO 변환
  - 카카오 API 클라이언트 구현

### 💼 Phase 4: 실무 및 배포 (chap15-18)

#### **Chapter 15** - 리액티브 프로그래밍
- **학습 목표**: 비동기 및 리액티브 시스템 구현
- **주요 내용**:
  - Spring WebFlux 기반 리액티브 API
  - Mono와 Flux를 통한 비동기 스트림 처리
  - 백프레셔 및 에러 처리
  - 리액티브 데이터베이스 접근

#### **Chapter 16** - 마이크로서비스 아키텍처
- **학습 목표**: 분산 시스템 설계 및 구현
- **주요 내용**:
  - **서비스 분리**: Account, Product, Order, Front, Configuration
  - **Spring Cloud Config**: 중앙 집중식 설정 관리
  - **서비스 간 통신**: OpenFeign, Load Balancer
  - **캐싱**: Redis 기반 분산 캐시
- **아키텍처 패턴**: 마이크로서비스 분해 전략

#### **Chapter 17** - 파일 처리 & 모니터링
- **학습 목표**: 파일 시스템 통합 및 애플리케이션 모니터링
- **주요 내용**:
  - Apache POI를 통한 Excel 파일 처리
  - OpenCSV를 통한 CSV 데이터 처리
  - Sentry 통합 에러 트래킹
  - PostgreSQL 고급 기능 활용

#### **Chapter 18** - CI/CD & 운영
- **학습 목표**: 완전 자동화된 배포 파이프라인 구축
- **주요 내용**:
  - **CI/CD 파이프라인**: GitHub Actions, Travis CI
  - **컨테이너화**: Docker 멀티스테이지 빌드
  - **오케스트레이션**: Kubernetes (EKS) 배포
  - **GitOps**: ArgoCD를 통한 자동 배포
  - **모니터링**: Prometheus, Grafana, Sentry 통합
  - **보안**: 컨테이너 보안 스캔, 시크릿 관리

## 🔐 보안 기능

### 다층 보안 아키텍처
- **HTTPS/SSL**: 전송 계층 암호화
- **OAuth2**: 표준 인증 프로토콜
- **XSS 보호**: Lucy Filter 기반 입력 검증
- **CSRF 보호**: 토큰 기반 요청 검증
- **SQL 인젝션 방어**: MyBatis 파라미터 바인딩

### 인증 및 권한 관리
```yaml
# 역할 계층구조
ADMINISTRATOR:
  - 전체 시스템 관리
  - 사용자 권한 관리
  - 시스템 설정 변경

MANAGER:
  - 콘텐츠 관리
  - 사용자 관리 (제한적)
  - 통계 및 리포트 조회

USER:
  - 개인 프로필 관리
  - 게시글 작성/수정
  - 댓글 참여
```

## 🛠️ 커스텀 Spring Boot Starters

### spring-boot-starter-lucy-filter
XSS 공격 방어를 위한 자동 구성 스타터:
```java
@ConfigurationProperties(prefix = "lucy.xss")
public class LucyXssFilterProperties {
    private boolean enabled = true;
    private String[] excludeUrls = {};
    private String ruleConfigPath = "lucy-xss-servlet-filter-rule.xml";
}
```

### spring-boot-starter-social-kakao
카카오 소셜 로그인 통합 스타터:
```java
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public class KakaoOAuth2Properties {
    private String clientId;
    private String clientSecret;
    private String scope = "profile_nickname,account_email";
}
```

## 📈 성능 최적화

### 데이터베이스 최적화
- **HikariCP**: 고성능 커넥션 풀
- **쿼리 최적화**: 인덱스 전략 및 실행 계획 분석
- **캐싱**: Redis 분산 캐시 활용
- **읽기 전용 복제본**: 읽기 성능 향상

### 애플리케이션 최적화
- **JVM 튜닝**: G1GC 설정 및 힙 메모리 최적화
- **비동기 처리**: `@Async` 및 CompletableFuture 활용
- **이미지 최적화**: 동적 리사이징 및 압축
- **정적 리소스**: CDN 및 브라우저 캐싱

## 🧪 테스트 전략

### 테스트 피라미드 구현
```bash
# 단위 테스트 (70%)
./gradlew test

# 통합 테스트 (20%)
./gradlew integrationTest

# E2E 테스트 (10%)
./gradlew e2eTest
```

### TestContainers 활용
- **데이터베이스**: MySQL, Redis 컨테이너
- **외부 서비스**: WireMock을 통한 API 모킹
- **전체 스택**: 실제 환경과 동일한 테스트

## 🚀 배포 전략

### 환경별 배포
```bash
# 개발 환경
./gradlew :chap10:bootRun --args='--spring.profiles.active=dev'

# 스테이징 환경
docker-compose -f docker-compose.staging.yml up

# 프로덕션 환경 (Kubernetes)
kubectl apply -f k8s/production/
```

### 무중단 배포
- **Blue-Green 배포**: 제로 다운타임 보장
- **카나리 배포**: 점진적 트래픽 전환
- **롤백 전략**: 즉시 이전 버전 복구

## 📊 모니터링 및 관측성

### 메트릭 수집
- **애플리케이션 메트릭**: Spring Boot Actuator
- **비즈니스 메트릭**: Micrometer + Prometheus
- **인프라 메트릭**: Node Exporter, cAdvisor

### 로깅 전략
```yaml
# 구조화된 로깅 (JSON)
logging:
  level:
    com.genius.primavera: DEBUG
    org.springframework.security: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 알럿 및 대시보드
- **Grafana 대시보드**: 실시간 메트릭 시각화
- **프로메테우스 알럿**: 임계값 기반 알림
- **Sentry**: 실시간 에러 트래킹 및 알림

## 🌍 국제화 및 다국어 지원

### 다국어 메시지
```properties
# messages_ko.properties
user.validation.nickname.invalid=올바르지 않은 별명입니다.
user.registration.success=회원가입이 완료되었습니다.

# messages_en.properties
user.validation.nickname.invalid=Invalid nickname format.
user.registration.success=Registration completed successfully.
```

## 🔄 개발 워크플로우

### Git 플로우
```bash
# 기능 브랜치 생성
git checkout -b feature/social-login

# 개발 완료 후 PR 생성
git push origin feature/social-login

# 코드 리뷰 및 자동 테스트 후 머지
# CI/CD 파이프라인 자동 실행
```

### 코드 품질 관리
- **SonarQube**: 정적 코드 분석
- **SpotBugs**: 잠재적 버그 탐지
- **Checkstyle**: 코딩 스타일 일관성
- **JaCoCo**: 테스트 코버리지 측정

## 📚 학습 리소스

### 공식 문서
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Cloud Reference](https://docs.spring.io/spring-cloud/docs/current/reference/html/)

### 추천 도서
- "Spring Boot in Action" - Craig Walls
- "Spring Security in Action" - Laurentiu Spilca
- "Microservices Patterns" - Chris Richardson

## 🤝 기여 가이드

### 기여 방법
1. 프로젝트 포크
2. 기능 브랜치 생성 (`git checkout -b feature/amazing-feature`)
3. 변경사항 커밋 (`git commit -m 'feat: add amazing feature'`)
4. 브랜치 푸시 (`git push origin feature/amazing-feature`)
5. Pull Request 생성

### 커밋 메시지 규칙
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 스타일 변경
refactor: 코드 리팩토링
test: 테스트 코드 추가/수정
chore: 빌드 설정 등 기타 변경
```

## 📄 라이선스

이 프로젝트는 [MIT 라이선스](LICENSE) 하에 배포됩니다.

## 👥 기여자

- [csj4032](https://github.com/csj4032) - 프로젝트 창시자 및 메인 개발자

## 🙏 감사의 말

이 프로젝트는 Spring Boot 커뮤니티와 오픈소스 생태계의 지원으로 만들어졌습니다. 모든 기여자와 사용자에게 감사드립니다.

---

<div align="center">

**⭐ 이 프로젝트가 도움이 되셨다면 스타를 눌러주세요! ⭐**

[🐛 이슈 신고](https://github.com/csj4032/primavera/issues) · [💡 기능 제안](https://github.com/csj4032/primavera/discussions) · [📖 위키](https://github.com/csj4032/primavera/wiki)

</div>