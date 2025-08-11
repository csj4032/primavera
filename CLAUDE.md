# CLAUDE.md

이 파일은 Claude Code가 이 저장소에서 작업할 때 참고하는 가이드라인입니다.

# Primavera - Spring Boot 교육용 프로젝트

## 프로젝트 개요

Primavera는 기본 개념부터 고급 마이크로서비스 아키텍처까지 점진적 학습을 보여주는 종합적인 Spring Boot 교육용 프로젝트입니다. 단계별 학습 진행을 위해 설계된 18개 이상의 모듈(chap01-chap18 + 유틸리티)로 구성되어 있습니다.

## 빠른 개발 명령어

### 빌드 및 테스트

```bash
# 전체 프로젝트 빌드
./gradlew clean build

# 특정 모듈 빌드
./gradlew :chap04:build

# 특정 모듈 테스트 실행
./gradlew :chap04:test

# 특정 테스트 클래스 실행
./gradlew :chap04:test --tests PrimaveraServiceTest

# 애플리케이션 실행 (특정 챕터)
./gradlew :chap04:bootRun

# 병렬 빌드 실행
./gradlew build --parallel
```

### 중앙화된 Docker 인프라 관리 (2025년 8월 업데이트)

```bash
# 특정 챕터 Docker 환경 시작
./docker-manager.sh start chap04

# 특정 챕터 Docker 환경 중지
./docker-manager.sh stop chap04

# 모든 챕터 Docker 환경 시작
./docker-manager.sh start-all

# 모든 챕터 Docker 환경 상태 확인
./docker-manager.sh status-all

# 사용 가능한 챕터 목록
./docker-manager.sh list

# TestContainers가 자동으로 MariaDB 11.4.7 테스트 관리
```

### 데이터베이스 작업 (기존 호환성 유지)

```bash
# Docker로 MariaDB 11.4.7 시작 (수동 방법)
docker run -d --name mariadb-primavera \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3308:3306 mariadb:11.4.7

# 전체 설정을 위한 docker-compose 사용 (기존 방법)
docker-compose up -d
```

## 아키텍처 및 철학

### 교육용 점진적 학습 구조

- **chap01-04**: Spring Boot 핵심 기본 개념 (DI, 설정, 데이터 접근)
- **chap05-08**: 웹 개발, 템플릿, 보안 기초
- **chap09-13**: 고급 기능 (OAuth2, 보안, 복잡한 데이터 구조)
- **chap14-17**: 운영 관련 사항 (리액티브, 마이크로서비스, 모니터링)
- **chap18**: 완전한 마이크로서비스 아키텍처
- **appendix**: 커스텀 Spring Boot 스타터 및 유틸리티

### 핵심 아키텍처 원칙

- **DRY보다 교육적 중복**: 각 모듈은 개념 진화를 보여주기 위해 의도적으로 완전한 독립 구현을 가짐
- **모듈 독립성**: 각 챕터는 다른 챕터에 의존하지 않고 독립적으로 빌드하고 실행 가능
- **점진적 복잡성**: 개념이 모듈을 거쳐 단순한 것에서 복잡한 것으로 발전
- **실제 상황 시뮬레이션**: 실제 코드베이스가 시간이 지나면서 어떻게 발전하는지 보여줌

## 기술 스택

### 핵심 기술

- **Spring Boot**: 3.3.6 (최신 안정 버전)
- **Java**: 21+ (모던 기능을 가진 LTS)
- **데이터베이스**: MariaDB 11.4.7 (모든 모듈에서 표준화)
- **빌드 도구**: 중앙화된 의존성 관리를 가진 Gradle
- **테스팅**: JUnit 5, Mockito, TestContainers

### 주요 의존성 (gradle.properties에서 중앙화)

- **Lombok**: 1.18.36 (코드 생성)
- **MariaDB Driver**: 3.5.4
- **MyBatis**: 3.0.4 (SQL 매핑 프레임워크)
- **TestContainers**: 1.21.3 (통합 테스트)
- **Spring Security**: 6.4.4
- **Thymeleaf**: 3.4.0 (템플릿 엔진)

### 커스텀 Spring Boot 스타터

- **spring-boot-starter-test-containers**: 자동화된 TestContainers 설정
- **spring-boot-starter-lucy-filter**: XSS 보호
- **spring-boot-starter-social-kakao**: 카카오 OAuth2 연동

## 개발 가이드라인

### 코드 품질 표준

- **모던 Java 기능**: Record, 패턴 매칭, 텍스트 블록, Optional 체이닝 사용
- **함수형 프로그래밍**: Stream API, 불변 객체, 순수 함수 선호
- **클린 아키텍처**: Controller-Service-Repository 계층 간 명확한 분리
- **테스트 주도 개발**: 구현 전에 테스트 작성
- **주석 가이드**: 절대로 주석을 남기지 말고, 명확한 네이밍과 구조로 코드 이해 가능하게 작성

### 소프트웨어 개발 원칙

- **DRY (Don't Repeat Yourself)**: 코드 중복을 피하고 재사용 가능한 컴포넌트 작성
- **KISS (Keep It Simple, Stupid)**: 단순하고 이해하기 쉬운 솔루션 우선
- **YAGNI (You Aren't Gonna Need It)**: 현재 필요하지 않은 기능은 구현하지 않음
- **DIY (Do It Yourself)**: 외부 의존성보다는 자체 구현을 통한 제어권 확보
- **테스트 우선 개발**: Red-Green-Refactor 사이클로 품질 높은 코드 작성

### SOLID 원칙 준수

- **SRP (Single Responsibility Principle)**: 클래스는 단 하나의 책임만 가져야 함
- **OCP (Open/Closed Principle)**: 확장에는 열려있고 수정에는 닫혀있어야 함
- **LSP (Liskov Substitution Principle)**: 하위 타입은 상위 타입을 완전히 대체 가능해야 함
- **ISP (Interface Segregation Principle)**: 클라이언트는 사용하지 않는 인터페이스에 의존하면 안됨
- **DIP (Dependency Inversion Principle)**: 고수준 모듈은 저수준 모듈에 의존하면 안됨

### 테스트 전략 (3계층 접근법)

1. **단위 테스트**: Mockito 기반 격리된 테스트
2. **통합 테스트**: 전체 Spring 컨텍스트를 가진 TestContainers
3. **수동 테스트**: API 검증을 위한 Postman/curl 스크립트

### 데이터베이스 전략

- **운영**: MariaDB 11.4.7
- **테스트**: MariaDB 11.4.7를 가진 TestContainers (환경 일관성)
- **스키마 관리**: 테스트용 init.sql, 마이그레이션용 Flyway
- **명명 규칙**: 대문자 테이블/컬럼명 (예: USER, ROLE, ARTICLE)

## 테스트 설정

### TestContainers 통합

```yaml
# application-test.yml
primavera:
  testcontainers:
    mariadb:
      enabled: true
      dockerImageName: mariadb:11.4.7
      driverClassName: org.mariadb.jdbc.Driver
      databaseName: primavera
      username: primavera
      password: primavera
      initScript: sql/init.sql
```

### 테스트 클래스 구조

```java

@SpringBootTest
@EnablePrimaveraTestcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("통합 테스트 설명")
class YourIntegrationTest {
    // TestContainers 자동 설정
    // MariaDB 11.4.7 컨테이너 자동 시작/중지
}
```

## 환경 설정

### 프로파일 기반 설정

- **local**: 개발 환경 (localhost Docker MariaDB)
- **test**: 테스트 환경 (TestContainers MariaDB)
- **default**: 운영 설정

### 애플리케이션 실행

```bash
# 프로파일을 사용한 로컬 개발
./gradlew :chap04:bootRun -Dspring.profiles.active=local

# TestContainers를 사용한 테스트 (자동)
./gradlew :chap04:test
```

## 모듈별 가이드라인

### 버전 관리

모든 의존성 버전은 `gradle.properties`에서 중앙화:

- 적절한 카테고리에 새 버전을 알파벳순으로 추가
- build.gradle 파일에서 `${versionVariableName}` 사용하여 참조
- 개별 모듈 build.gradle 파일에서 버전 하드코딩 금지

### 빌드 설정

각 모듈 포함 사항:

- Spring Boot Gradle 플러그인
- 코드 커버리지를 위한 Jacoco
- 중앙화된 의존성 관리
- TestContainers 통합

### 보안 구현

- **다계층 보안**: 전송 계층(HTTPS), 인증(OAuth2), XSS 보호
- **역할 기반 접근 제어**: ADMINISTRATOR → MANAGER → USER
- **국제화**: 한국어(기본) 및 영어 지원

## 코딩 표준

### 메서드 및 클래스 설계

- **간결성 우선**: 읽기 쉽기보다는 간결함을 우선하여 코드 작성
- **최신 Java 기술 활용**: 최신 Java 기능을 적극적으로 사용하여 코드 간소화
- **마이크로 메서드**: 복잡한 로직을 작고 조합 가능한 메서드로 분해
- **한 줄 선호**: 메서드 체이닝, 람다, 삼항 연산자, 스트림 API 적극 사용, if 조건 한 줄 가능하면 한 줄 코딩
- **불변성**: final 키워드, Record, 불변 컬렉션 선호
- **Null 안전성**: null 검사 대신 Optional 광범위하게 사용
- **표현식 우선**: if문보다 삼항연산자, for문보다 스트림 API 선호
- **함수형 프로그래밍**: 람다, 메서드 참조, 고차 함수 사용
- **의존성 주입**: 생성자 주입 선호, 필드 주입은 피함
- **로그 관리**: 로그는 slf4j, Logback 사용, 이모지 사용 금지, 영어로 작성

### 메서드 순서 규칙

**클래스 내 메서드는 접근 제한자 순서로 정렬합니다:**

1. **public 메서드**: 외부에서 사용하는 공개 인터페이스
2. **protected 메서드**: 상속 관계에서 사용하는 메서드
3. **private 메서드**: 내부 구현을 위한 헬퍼 메서드

```java
public class UserService {
    // public 메서드들
    public User createUser(UserDto userDto) { ...}

    public List<User> findAllUsers() { ...}

    public Optional<User> findUserById(Long id) { ...}

    // protected 메서드들
    protected void validateUser(User user) { ...}

    protected User buildUserFromDto(UserDto dto) { ...}

    // private 메서드들
    private void logUserCreation(User user) { ...}

    private boolean isValidEmail(String email) { ...}
}
```

**특별한 경우:**

- **생성자**: 클래스 최상단 (필드 선언 다음)
- **정적 메서드**: 해당 접근 제한자 그룹 내에서 인스턴스 메서드보다 앞에 배치
- **라이프사이클 메서드** (@PostConstruct, @PreDestroy): 해당 접근 제한자 그룹 내에서 명시적으로 표시

### 네이밍 및 컨벤션 규칙

**최대한 네이밍과 컨벤션으로 코드를 이해할 수 있도록 작성합니다:**

#### 클래스 네이밍

- **의도를 명확히 표현**: `UserAuthenticationService` (O) vs `UserService` (△)
- **역할 기반 명명**: `EmailValidator`, `PasswordEncoder`, `TokenGenerator`
- **계층별 접미사**: `Controller`, `Service`, `Repository`, `Dto`, `Entity`
- **패턴 기반 명명**: `UserFactory`, `PaymentBuilder`, `OrderObserver`

#### 메서드 네이밍

- **동작을 명확히 표현**: `calculateTotalPrice()` (O) vs `calculate()` (X)
- **부울 반환 메서드**: `isValid()`, `hasPermission()`, `canAccess()`
- **컬렉션 처리**: `findActiveUsers()`, `filterByStatus()`, `mapToDto()`
- **상태 변경**: `activateUser()`, `deactivateAccount()`, `markAsCompleted()`

#### 변수 네이밍

- **구체적이고 명확한 이름**: `activeUserCount` (O) vs `count` (X)
- **단위 포함**: `timeoutInMillis`, `maxRetryAttempts`, `cacheSizeInMB`
- **컨텍스트 명시**: `userEmail` (O) vs `email` (△), `orderCreatedAt` (O) vs `createdAt` (△)
- **컬렉션 복수형**: `users`, `orders`, `validationResults`

#### 상수 및 Enum 네이밍

- **대문자 스네이크 케이스**: `MAX_RETRY_ATTEMPTS`, `DEFAULT_TIMEOUT_MILLIS`
- **Enum 값은 의미 명확히**: `OrderStatus.PENDING`, `UserRole.ADMINISTRATOR`
- **접두사로 그룹화**: `ERROR_USER_NOT_FOUND`, `ERROR_INVALID_PASSWORD`

#### 패키지 및 파일 구조

- **도메인 중심 구조**: `com.genius.primavera.user.domain`, `com.genius.primavera.order.application`
- **계층별 분리**: `interfaces/`, `application/`, `domain/`, `infrastructure/`
- **기능별 하위 패키지**: `user/authentication/`, `order/payment/`, `notification/email/`

### 주석 정책

프로젝트 주석은 절대 작성하지 않는다. 클래스, 메서드명으로 코드의 흐름을 파악하도록 네이밍을 하고 주의가 필요한 부분에만 주석을 넣어주세요.

## Git 워크플로

### 원자적 커밋

- 커밋당 하나의 논리적 변경
- 쉬운 리뷰를 위한 작고 집중된 커밋
- "왜"를 설명하는 의미 있는 커밋 메시지

### 커밋 메시지 형식

```
feat: OAuth2 소셜 로그인 통합 추가
fix: MariaDB 연결 타임아웃 문제 해결
docs: API 문서 업데이트
test: 사용자 서비스 통합 테스트 추가
refactor: 결제 처리 로직 추출
```

### 파일 관리 정책

- **Git을 주요 버전 관리로**: 수동 백업 파일 생성 금지
- **백업 파일 없음**: *.bak, *.backup, *.old, *_backup 파일 피하기
- **Git 기능 사용**: 실험용 브랜치, 릴리스용 태그, 복구용 히스토리

## 성능 및 품질

### 품질 게이트

- 빌드 성공 전에 모든 테스트 통과 필수
- 정적 분석 도구 통합
- 보안 취약점 스캔
- 코드 커버리지 기준 (예: 80% 이상)
- 코드 컴파일 에러 및 경고 없음

### 데이터베이스 최적화

- HikariCP 연결 풀링
- 적절한 인덱싱 전략
- MyBatis를 사용한 쿼리 최적화
- MariaDB 11.4.7 특정 기능 (JSON, CTE, 윈도우 함수)

## 모범 사례

### 교육용 프로젝트 고려사항

- **효율성보다 학습**: 전통적인 DRY 원칙보다 교육적 가치 우선
- **개념 시연**: 서로 다른 모듈에서 같은 문제에 대한 여러 접근법 보여주기
- **독립 모듈**: 각 모듈은 해당 복잡성 수준에서 완전하고 작동하는 예제여야 함

### 모던 개발 패턴

- **철도 지향 프로그래밍**: 오류 처리를 위한 Result/Either 패턴 사용
- **이벤트 주도 아키텍처**: Spring 이벤트를 통한 느슨한 결합 구현
- **코드로서의 설정**: 설정 외부화, 타입 안전한 바인딩 사용

## 환경별 설정 세부사항

### 로컬 개발 환경

- **목적**: 대화형 개발, 디버깅, 개발자에 의한 수동 테스트
- **데이터베이스**: localhost MariaDB 11.4.7 (Docker 또는 네이티브 설치)
- **설정**: 고정된 localhost 연결 설정을 가진 `application-local.yml`
- **데이터 지속성**: 개발 연속성을 위해 애플리케이션 재시작 간 데이터 유지
- **프로파일 활성화**: 로컬 개발자 환경용 `local` 프로파일

### 테스트 환경 (빌드/CI)

- **목적**: 자동화된 통합 테스트, 지속적 통합, 빌드 검증
- **데이터베이스**: TestContainers MariaDB 11.4.7 (자동 관리되는 Docker 컨테이너)
- **설정**: 동적 TestContainers 속성을 가진 `application-test.yml`
- **데이터 격리**: 각 테스트가 새롭고 격리된 데이터베이스 인스턴스 획득
- **스키마 관리**: 빠른 테스트 설정을 위한 `init.sql` 초기화 스크립트

### 프로파일별 실행 방법

```bash
# 로컬 개발 (localhost Docker MariaDB)
./gradlew :chap04:bootRun -Dspring.profiles.active=local

# 테스트 (TestContainers MariaDB - 자동)
./gradlew :chap04:test

# IDE 설정에서 프로파일 지정
-Dspring.profiles.active=local
```

이 프로젝트는 기본 개념부터 운영 준비된 마이크로서비스 아키텍처까지 Spring Boot 개발을 위한 종합적인 학습 자료로 사용됩니다.