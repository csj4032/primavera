# 🚀 Primavera 애플리케이션 실행 가이드

## 📋 개요
이 가이드는 chap04-chap16 애플리케이션을 Docker와 Vault를 이용하여 로컬 환경에서 실행하는 방법을 안내합니다.

## 🏗️ 아키텍처
```
Application (Spring Boot)
    ↓ (Vault 설정 가져오기)
Vault (설정 관리)
    ↓ (데이터베이스 설정)
MariaDB 11.4.7 (데이터 저장소)
```

## ✅ 완료된 설정

### 🔧 Application 설정 표준화
- **application.yml**: 환경 중립적 기본 설정
- **application-local.yml**: 로컬 환경용 Vault 연동 설정
- **Profile Groups**: 로깅 및 Hikari 설정 조합 가능

### 🐳 Docker 인프라 표준화
- **표준 포트**: MariaDB(3306), Vault(8200)
- **표준 토큰**: `primavera-vault-token`
- **자동 초기화**: Vault에 데이터베이스 설정 자동 저장

## 🚀 실행 방법

### 1️⃣ 기본 실행 (권장)
```bash
# 1. Docker 환경 시작
./infrastructure/scripts/docker-manager.sh start chap04

# 2. 애플리케이션 실행 (local 프로파일 - 환경 변수 방식)
cd chap04 && SPRING_PROFILES_ACTIVE=local ../gradlew bootRun

# 또는 프로젝트 루트에서
SPRING_PROFILES_ACTIVE=local ./gradlew :chap04:bootRun

# 3. 종료 후 정리
./infrastructure/scripts/docker-manager.sh stop chap04
```

### 2️⃣ 고급 프로파일 조합
```bash
# 성능 최적화 Hikari 설정으로 실행 (chap05 예시)
cd chap05 && SPRING_PROFILES_ACTIVE=local,hikari-performance-focused ../gradlew bootRun

# 리소스 제약 환경 설정으로 실행
cd chap05 && SPRING_PROFILES_ACTIVE=local,hikari-resource-constrained ../gradlew bootRun

# 균형 잡힌 설정으로 실행
cd chap05 && SPRING_PROFILES_ACTIVE=local,hikari-balanced ../gradlew bootRun
```

### 3️⃣ 다중 챕터 실행
```bash
# 각 챕터마다 다른 포트 사용
cd chap06 && SPRING_PROFILES_ACTIVE=local SERVER_PORT=8081 ../gradlew bootRun
cd chap07 && SPRING_PROFILES_ACTIVE=local SERVER_PORT=8082 ../gradlew bootRun
```

## 📂 지원 챕터 목록

| 챕터 | 애플리케이션명 | 설명 |
|-------|----------------|------|
| chap04 | DataAccessApplication | JPA 데이터 접근 |
| chap05 | MyBatisLoggingApplication | MyBatis + 로깅 |
| chap06 | VaadinApplication | Vaadin 웹 애플리케이션 |
| chap07 | ThymeleafWebApplication | Thymeleaf 템플릿 |
| chap08 | SecurityFilterApplication | 보안 필터 |
| chap09 | SpringSecurityBasicApplication | Spring Security 기초 |
| chap10 | OAuth2SocialLoginApplication | OAuth2 소셜 로그인 |
| chap11 | BoardSystemApplication | 게시판 시스템 |
| chap12 | HierarchicalCommentApplication | 계층형 댓글 |
| chap13 | AdvancedAuthorizationApplication | 고급 인증/인가 |
| chap14 | AdvancedJpaApplication | 고급 JPA |
| chap15 | JpaAdvancedMappingApplication | JPA 고급 매핑 |
| chap16 | FileProcessingMonitoringApplication | 파일 처리 모니터링 |

## 🔐 Vault 연동 확인

### 애플리케이션 로그에서 확인
```
DEBUG o.s.v.c.e.LeaseAwareVaultPropertySource : Requesting secrets from Vault at secret/DataAccessApplication/local
```
이 로그가 보이면 Vault 연동이 정상 작동하는 것입니다.

### Vault 수동 확인
```bash
# Vault 컨테이너 접속
docker exec -it vault-primavera-chap04 sh

# Vault 내부에서 설정 확인
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='primavera-vault-token'
vault kv get secret/DataAccessApplication/local
```

## 🛠️ 문제 해결

### 1️⃣ Config Server 에러
```
No spring.config.import property has been defined
```
**해결**: 반드시 `SPRING_PROFILES_ACTIVE=local` 환경 변수를 설정해서 실행

### 2️⃣ 포트 충돌 에러
```
Port 8080 was already in use
```
**해결**: 다른 포트 지정 `SERVER_PORT=8081`

### 3️⃣ Vault 연결 에러
```
Connect to http://localhost:8200 failed
```
**해결**: Docker 환경이 정상적으로 시작되었는지 확인
```bash
docker ps | grep vault
```

### 4️⃣ 데이터베이스 연결 에러
```
Failed to determine a suitable driver class
```
**해결**: MariaDB 컨테이너가 정상 시작되었는지 확인
```bash
docker ps | grep mariadb
```

## 🎯 핵심 설정 구조

### application.yml (환경 중립)
```yaml
spring:
  application:
    name: DataAccessApplication
  profiles:
    group:
      local:
        - console-appender
        - file-debug-appender
        - file-error-appender
        - file-info-appender
        - file-warn-appender
  jackson:
    time-zone: UTC
```

### application-local.yml (로컬 환경)
```yaml
spring:
  config:
    activate:
      on-profile: local
    import:
      - "optional:vault:"
      - "optional:configserver:"
  cloud:
    config:
      enabled: true
    vault:
      enabled: true
      host: localhost
      port: 8200
      token: primavera-vault-token
      fail-fast: false
```

## 📈 성능 최적화 팁

1. **로컬 개발**: `hikari-balanced` 프로파일 사용
2. **성능 테스트**: `hikari-performance-focused` 프로파일 사용
3. **리소스 제약**: `hikari-resource-constrained` 프로파일 사용
4. **로깅 최소화**: `test` 프로파일 그룹 사용

## 🚨 중요 사항

1. **프로파일 필수**: 반드시 `SPRING_PROFILES_ACTIVE=local` 환경 변수 설정
2. **Docker 선행**: 애플리케이션 실행 전 Docker 환경 시작 필수
3. **포트 관리**: 동시 실행 시 포트 충돌 방지
4. **정리**: 작업 완료 후 Docker 환경 정리 권장

---

## ✅ 검증된 작업 환경

### 테스트 완료 챕터
- **chap04** (DataAccessApplication): ✅ Docker + Vault 연동 완료
- **chap05** (MyBatisLoggingApplication): ✅ Docker + Vault 연동 완료  
- **chap06** (VaadinApplication): ✅ Docker + Vault 연동 완료
- **chap07-chap16**: 동일한 설정 패턴 적용 완료

### 프로파일 설정 방법
```bash
# ✅ 올바른 방법 (환경 변수)
SPRING_PROFILES_ACTIVE=local ./gradlew :chap04:bootRun

# ❌ 작동하지 않음 (JVM 파라미터)  
./gradlew :chap04:bootRun -Dspring.profiles.active=local
```

**💡 이 가이드를 통해 모든 Primavera 애플리케이션을 안전하고 효율적으로 실행할 수 있습니다!**