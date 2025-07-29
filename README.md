# 🌸 Primavera - Spring Boot 종합 학습 프로젝트

[![Build Status](https://travis-ci.org/csj4032/primavera.svg?branch=master)](https://travis-ci.org/csj4032/primavera)
[![Coverage Status](https://coveralls.io/repos/github/csj4032/primavera/badge.svg)](https://coveralls.io/github/csj4032/primavera)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Spring Boot를 이용한 현대적인 웹 애플리케이션 개발을 체계적으로 학습할 수 있는 종합 프로젝트입니다. 기초부터 고급 기술까지 단계별로 구성된 18개 챕터를 통해 실무에 필요한 모든 기술을 습득할 수 있습니다.

## 🎯 최신 업데이트 (2025년 1월)

### 📊 Chapter 17 - 엔터프라이즈 데이터 파이프라인
- **Spring Batch + Debezium Embedded**를 활용한 하이브리드 데이터 처리
- **경량 CDC 아키텍처**: Kafka 인프라 없이 실시간 변경 감지
- **Elasticsearch 통합**: 검색 최적화된 문서 인덱싱

### 🔄 Chapter 18 - 이벤트 기반 마이크로서비스
- **WebFlux + R2DBC**로 완전한 리액티브 스택 구현
- **Kafka 이벤트 시스템**: 주문-재고 처리 실시간 연동
- **Saga 패턴**: 분산 트랜잭션 및 보상 처리 자동화

## 🛠️ 기술 스택

### Core Framework
- **Java 21** (Switch expressions, Text blocks, Records)
- **Spring Boot 3.3.6** (현재 안정 LTS 버전)
- **Spring Security 6.4.4** with OAuth2 Client
- **Spring Cloud 2023.0.4** (Leyton)
- **Gradle 8.12.1**

## 📦 공통 의존성 관리 시스템

Primavera 프로젝트는 **공통 의존성 그룹** 시스템을 통해 모든 서브모듈의 라이브러리를 중앙에서 관리합니다. 이를 통해 버전 일관성을 보장하고 중복을 제거하여 유지보수성을 크게 향상시켰습니다.

### 🎯 공통 의존성 그룹 목록

| 그룹명 | 포함 라이브러리 | 사용 목적 |
|--------|----------------|-----------|
| **`databaseDependencies`** | spring-boot-starter-jdbc, mariadb-java-client | 데이터베이스 연결 |
| **`aopDependencies`** | spring-boot-starter-aop | 관점 지향 프로그래밍 |
| **`mybatisDependencies`** | mybatis-spring-boot-starter, mybatis-dynamic-sql | MyBatis ORM |
| **`thymeleafDependencies`** | spring-boot-starter-thymeleaf, thymeleaf-layout-dialect | 템플릿 엔진 |
| **`securityDependencies`** | spring-security-* (config, core, crypto, web) | Spring Security |
| **`testContainersDependencies`** | testcontainers (junit-jupiter, mariadb) | 통합 테스트 |
| **`validationDependencies`** | spring-boot-starter-validation, jakarta.validation-api | Bean Validation |
| **`developmentDependencies`** | spring-boot-devtools | 개발 도구 |
| **`loggingDependencies`** | p6spy-spring-boot-starter, log4jdbc-log4j2 | SQL 로깅 |
| **`webUiDependencies`** | bootstrap, graalvm-js | 웹 UI 라이브러리 |

### 💡 의존성 그룹 사용법

각 서브모듈의 `build.gradle`에서 필요한 기능별 의존성 그룹을 선택적으로 적용할 수 있습니다:

```gradle
// 기본 plugins 및 dependencies는 root build.gradle에서 자동 상속

dependencies {
    // AOP 기능이 필요한 경우
    aopDependencies.each { dep -> implementation dep }
    
    // Database 연결이 필요한 경우  
    databaseDependencies.each { dep -> implementation dep }
    
    // MyBatis가 필요한 경우
    mybatisDependencies.each { dep -> implementation dep }
    
    // Thymeleaf 템플릿이 필요한 경우
    thymeleafDependencies.each { dep -> implementation dep }
    
    // Spring Security가 필요한 경우
    securityDependencies.each { dep -> implementation dep }
    
    // TestContainers가 필요한 경우
    testContainersDependencies.each { dep -> testImplementation dep }
    
    // Bean Validation이 필요한 경우
    validationDependencies.each { dep -> implementation dep }
    
    // Development Tools가 필요한 경우
    developmentDependencies.each { dep -> implementation dep }
    
    // SQL Logging이 필요한 경우
    loggingDependencies.each { dep -> implementation dep }
    
    // Web UI 기능이 필요한 경우
    webUiDependencies.each { dep -> implementation dep }
    
    // ==========================================
    // 모듈별 전용 Dependencies만 추가
    // ==========================================
    implementation "your.custom:library:version"
}
```

### 📋 실제 사용 예시

#### **chap01** - 간단한 웹 애플리케이션
```gradle
dependencies {
    // 기본 dependencies는 root에서 자동 제공
    // 추가 필요한 dependencies만 선언
    implementation("jakarta.annotation:jakarta.annotation-api")
}
```

#### **chap03** - AOP + Database 기능
```gradle
dependencies {
    // AOP 기능 사용
    aopDependencies.each { dep -> implementation dep }
    
    // Database 연결 기능 사용
    databaseDependencies.each { dep -> implementation dep }
    
    // TestContainers 기능 사용
    testContainersDependencies.each { dep -> implementation dep }
    
    // 모듈 전용 dependencies
    implementation "commons-io:commons-io:${commonsIoVersion}"
}
```

#### **chap07** - 복합 기능 웹 애플리케이션
```gradle
dependencies {
    // 여러 기능을 조합하여 사용
    aopDependencies.each { dep -> implementation dep }
    databaseDependencies.each { dep -> implementation dep }
    mybatisDependencies.each { dep -> implementation dep }
    thymeleafDependencies.each { dep -> implementation dep }
    validationDependencies.each { dep -> implementation dep }
    developmentDependencies.each { dep -> implementation dep }
    loggingDependencies.each { dep -> implementation dep }
    webUiDependencies.each { dep -> implementation dep }
    testContainersDependencies.each { dep -> testImplementation dep }
    
    // 모듈 전용 dependencies
    implementation "org.springframework.security:spring-security-crypto:${springSecurityVersion}"
    implementation "org.hibernate:hibernate-core:${hibernateVersion}"
}
```

### ✅ 공통 의존성 시스템의 장점

1. **🎯 중복 제거**: 반복되는 dependencies 선언 최소화
2. **📐 버전 일관성**: 모든 모듈에서 동일한 라이브러리 버전 사용
3. **🔧 유지보수성**: `gradle.properties`에서 중앙 집중식 버전 관리
4. **⚡ 개발 효율성**: 필요한 기능만 선택적으로 빠르게 적용
5. **🛡️ 의존성 충돌 방지**: 검증된 라이브러리 조합 사용
6. **📚 학습 친화성**: 기능별로 그룹화되어 이해하기 쉬움

### 🔄 버전 업데이트 워크플로우

```bash
# 1. gradle.properties에서 원하는 라이브러리 버전 업데이트
vi gradle.properties

# 2. 전체 프로젝트 빌드 테스트
./gradlew clean build

# 3. 개별 모듈 테스트
./gradlew :chap07:test

# 4. 변경사항 커밋
git add gradle.properties
git commit -m "deps: update spring boot to 3.5.4"
```

### Database & Persistence
- **MariaDB 11.4.7** (Primary Database & Testing)
- **MongoDB** (Document Database for Product Service)
- **Elasticsearch 8.12.0** (Search Engine & Document Store)
- **Redis** (Caching & Session Storage)
- **Docker TestContainers** (Automated Testing Environment)
- **MyBatis 3.0.4** with Dynamic SQL
- **JPA/Hibernate** (ORM)
- **Spring Data R2DBC** (Reactive Database Access)
- **Flyway** (Database Migration)
- **spring-boot-starter-test-container** (Custom TestContainers Starter)

### Security & Authentication
- **Spring Security 6.4.4**
- **OAuth2 Client** (Google, Facebook, GitHub, Kakao)
- **HashiCorp Vault** (중앙집중식 민감정보 관리)
- **Lucy XSS Filter 2.0.1** (XSS Protection)
- **SSL/HTTPS** with PKCS12
- **JWT Token** Support

### Event-Driven & Messaging
- **Apache Kafka 3.6.1** (Event Streaming Platform)
- **Debezium 2.7.2** (Change Data Capture)
- **Spring Kafka** (Kafka Integration)
- **Kafka Streams** (Stream Processing)

### Web & Template
- **Spring WebFlux** (Reactive Web Framework)
- **Thymeleaf 3.4.0** (Server-side Rendering)
- **Bootstrap 5.3.3** (UI Framework)
- **AdminLTE** (Admin Dashboard)
- **WYSIHTML5** (Rich Text Editor)

### Testing & Quality
- **JUnit 5** (Unit Testing)
- **TestContainers with MariaDB 11.4.7** (Docker-based Integration Testing)
- **spring-boot-starter-test-container** (Custom TestContainers AutoConfiguration)
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
│   ├── chap15-16/          # 💼 실무: 리액티브, 마이크로서비스
│   ├── chap17/             # 📊 데이터 파이프라인
│   │   ├── batch/          # Spring Batch 초기 인덱싱
│   │   └── streaming/      # Debezium CDC 실시간 업데이트
│   └── chap18/             # 🔄 이벤트 기반 마이크로서비스
│       ├── order/          # 주문 서비스 (WebFlux + R2DBC + Kafka)
│       ├── product/        # 상품 서비스 (WebFlux + MongoDB + Kafka)
│       ├── account/        # 계정 서비스 (Redis)
│       ├── front/          # 프론트엔드 서비스
│       └── configuration/  # 설정 서버
├── 🧩 Appendix
│   ├── appendix/
│   │   ├── spring-boot-starter-lucy-filter/    # XSS 보안 필터
│   │   └── spring-boot-starter-social-kakao/   # 카카오 소셜 로그인
└── 🔧 Infrastructure
    ├── config/             # 환경별 설정 파일
    ├── docker/             # Docker 컨테이너 구성
    └── k8s/                # Kubernetes 매니페스트
```

### 🔄 마이크로서비스 이벤트 아키텍처 (chap18)

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Order API     │    │   Apache Kafka   │    │ Product Service │
│ (WebFlux+R2DBC) │────│   Event Bus      │────│ (WebFlux+Mongo) │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
    ┌────▼────────┐        ┌────▼─────┐         ┌───────▼────────┐
    │ OrderCreated │        │  Event   │         │ InventoryCheck │
    │   Event      │        │ Topics   │         │ & Deduction    │
    └─────────────┘        └──────────┘         └────────────────┘
                                │
                         ┌─────▼──────┐
                         │ Saga Pattern│
                         │ Orchestrator│
                         └────────────┘
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

### 2. Infrastructure Docker 환경 구성 ⚡ **2025년 1월 업데이트**

#### 📦 Infrastructure 디렉토리 구성
```
infrastructure/
├── docker-compose.yml    # 통합 인프라 컨테이너 설정
├── init-local.sql             # 초기 데이터베이스 스크립트
├── vault/               # HashiCorp Vault 설정
└── vault-init.sh        # Vault 초기화 스크립트
```

#### 🔧 Infrastructure 서비스 구성

| 서비스 | 포트 | 용도 | 비고 |
|--------|------|------|------|
| **MariaDB** | 1109 → 3306 | 메인 관계형 데이터베이스 | 모든 챕터 공통 사용 |
| **HashiCorp Vault** | 8200 | 설정 관리 및 시크릿 저장 | chap10+ 보안 설정용 |
| **Redis** | 6379 | 캐싱 및 세션 저장소 | 마이크로서비스 세션 공유 |
| **MongoDB** | 27017 | 문서 데이터베이스 | chap18 Product Service용 |
| **Apache Kafka** | 9092 | 메시지 브로커 | chap18 이벤트 스트리밍 |
| **Zookeeper** | 2181 | Kafka 코디네이터 | Kafka 의존성 |
| **Elasticsearch** | 9200, 9300 | 검색 엔진 | chap17 데이터 파이프라인 |
| **Sentry** | 9000 | 에러 모니터링 | Self-hosted 에러 추적 |

#### 🚀 Docker Compose 실행 (필수)
chap03 이상의 모든 모듈 실행 전에 반드시 통합 인프라 환경을 구성해야 합니다.

```bash
# 1. 프로젝트 클론
git clone https://github.com/csj4032/primavera.git
cd primavera

# 2. Infrastructure 디렉토리로 이동
cd infrastructure

# 3. Docker Compose로 전체 인프라 시작
docker-compose up -d

# 4. 컨테이너 상태 확인
docker-compose ps

# 출력 예시:
#     Name                   Command               State            Ports
# --------------------------------------------------------------------------
# mariadb-primavera      docker-entrypoint.sh mysqld   Up      0.0.0.0:1109->3306/tcp
# vault-primavera        vault server -dev             Up      0.0.0.0:8200->8200/tcp
# redis-primavera        redis-server --appendonly     Up      0.0.0.0:6379->6379/tcp
# mongodb-primavera      mongod                         Up      0.0.0.0:27017->27017/tcp
# kafka-primavera        /etc/confluent/docker/run      Up      0.0.0.0:9092->9092/tcp
# elasticsearch-primavera /bin/tini -- /usr/local/bin/  Up      0.0.0.0:9200->9200/tcp
# sentry-web-primavera   sentry run web                 Up      0.0.0.0:9000->9000/tcp

# 5. 특정 서비스 로그 확인
docker-compose logs -f mariadb
docker-compose logs -f sentry-web
```

#### 💡 Infrastructure 업데이트 내용 (2025년 1월)

**✅ 추가된 서비스:**
- **Self-hosted Sentry**: 온프레미스 에러 모니터링 시스템
- **HashiCorp Vault**: 보안 설정 및 시크릿 관리
- **Redis**: 고성능 캐싱 및 세션 저장소
- **MongoDB**: 문서 지향 데이터베이스
- **Kafka + Zookeeper**: 이벤트 스트리밍 플랫폼
- **Elasticsearch**: 검색 및 분석 엔진

**🗑️ 제거된 서비스:**
- **Kafka Connect + Debezium UI**: 포트 충돌 및 미사용으로 제거
  - chap17에서는 Debezium Embedded 사용
  - chap18 Product Service (포트 8083)와 충돌 해결

**🎯 장점:**
- **통합 관리**: 모든 인프라를 단일 docker-compose로 관리
- **포트 최적화**: 서비스 간 포트 충돌 완전 해결
- **리소스 효율성**: 실제 사용되는 서비스만 구성

#### 🔍 MariaDB 컨테이너 상세 확인

**컨테이너 정보 조회:**
```bash
# 실행 중인 컨테이너 확인
docker ps | grep mariadb-primavera

# 컨테이너 상세 정보 확인
docker inspect mariadb-primavera

# 네트워크 정보 확인
docker network ls
docker network inspect infrastructure_default
```

**포트 및 연결 확인:**
```bash
# 포트 1109 바인딩 확인
netstat -an | grep 1109
# 또는
lsof -i :1109

# 예상 결과:
# tcp46  *:1109  *:*  LISTEN

# MariaDB 서비스 접근성 테스트
telnet localhost 1109
# 연결 성공 시: Connected to localhost.
```

#### 💽 MariaDB 직접 접속 및 확인

**CLI를 통한 접속:**
```bash
# 방법 1: 컨테이너 내부 접속
docker exec -it mariadb-primavera mysql -u primavera -p
# 비밀번호: primavera

# 방법 2: 호스트에서 직접 접속 (MariaDB 클라이언트 설치 시)
mysql -h localhost -P 1109 -u primavera -p primavera
# 비밀번호: primavera

# 방법 3: root 계정으로 접속
docker exec -it mariadb-primavera mysql -u root -p
# 비밀번호: root
```

**기본 데이터베이스 확인:**
```sql
-- 접속 성공 후 실행할 쿼리들

-- 1. 버전 정보 확인
SELECT VERSION();
-- 결과: 11.4.7-MariaDB

-- 2. 생성된 데이터베이스 확인
SHOW DATABASES;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| primavera          |
| primavera_test     |
+--------------------+

-- 3. primavera 데이터베이스 선택 및 문자셋 확인
USE primavera;
SELECT @@character_set_database, @@collation_database;
-- 결과: utf8mb4, utf8mb4_unicode_ci

-- 4. 권한 확인
SHOW GRANTS FOR 'primavera'@'%';
-- 결과: GRANT ALL PRIVILEGES ON `primavera`.* TO `primavera`@`%`

-- 5. 테이블 확인 (초기에는 비어있음)
SHOW TABLES;
-- Empty set (0.00 sec)

-- 6. 연결 정보 확인
SELECT USER(), DATABASE(), CONNECTION_ID();
-- 결과: primavera@%, primavera, [connection_id]
```

**Docker 컨테이너 관리 명령어:**
```bash
# === 컨테이너 라이프사이클 관리 ===

# 컨테이너 중지
docker-compose stop
# 또는 특정 서비스만: docker-compose stop mariadb

# 컨테이너 시작 (이미 생성된 경우)
docker-compose start

# 컨테이너 재시작
docker-compose restart

# 컨테이너 완전 삭제 (데이터 보존)
docker-compose down

# 컨테이너 및 볼륨까지 삭제 (데이터 삭제)
docker-compose down -v

# === 상태 모니터링 ===

# 서비스 상태 확인
docker-compose ps

# 실시간 로그 모니터링
docker-compose logs -f mariadb

# 최근 로그 100줄 확인
docker-compose logs --tail=100 mariadb

# 리소스 사용량 확인
docker stats mariadb-primavera

# === 문제 해결 ===

# 컨테이너 강제 재생성
docker-compose down
docker-compose up -d --force-recreate

# 모든 정지된 컨테이너 정리
docker container prune

# 사용하지 않는 볼륨 정리
docker volume prune

# 사용하지 않는 네트워크 정리
docker network prune
```

#### 🔧 외부 도구를 통한 접속

**DBeaver, HeidiSQL, MySQL Workbench 등:**
```
Connection Settings:
┌─────────────────────┬──────────────────┐
│ Host                │ localhost        │
│ Port                │ 1109             │
│ Username            │ primavera        │
│ Password            │ primavera        │
│ Database            │ primavera        │
│ Driver              │ MariaDB/MySQL    │
└─────────────────────┴──────────────────┘
```

**IntelliJ IDEA Database Tool:**
1. Database Tool Window 열기
2. '+' → Data Source → MariaDB
3. 위 설정 정보 입력
4. Test Connection → Success 확인

### 3. 프로젝트 실행

#### 일반적인 챕터 실행
```bash
# Infrastructure가 정상 실행된 후 메인 디렉토리로 이동
cd ../

# 특정 챕터 실행 (예: chap10)
./gradlew :chap10:bootRun

# 전체 빌드 및 테스트
./gradlew clean build
```

#### chap17 - 데이터 파이프라인 실행
```bash
# 1. Elasticsearch 시작
docker run -d --name elasticsearch-primavera \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  elasticsearch:8.12.0

# 2. 초기 인덱싱 실행
./gradlew :chap17:batch:bootRun

# 3. 실시간 CDC 시작
./gradlew :chap17:streaming:bootRun
```

#### chap18 - 마이크로서비스 + Kafka 실행
```bash
# 1. Kafka 시작
docker run -d --name kafka-primavera \
  -p 9092:9092 \
  apache/kafka:latest

# 2. MongoDB 시작 (Product Service용)
docker run -d --name mongodb-primavera \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=primavera \
  mongo:latest

# 3. 서비스 실행 (각각 별도 터미널에서)
./gradlew :chap18:order:bootRun      # 포트 8082
./gradlew :chap18:product:bootRun    # 포트 8083

# 4. 주문 생성 테스트
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "items": [
      {
        "productId": "1",
        "quantity": 2,
        "unitPrice": 25000,
        "productName": "테스트 상품"
      }
    ]
  }'
```

#### ⚠️ 중요 주의사항

**필수 사전 작업:**
- chap03 이상의 모든 모듈은 MariaDB 데이터베이스가 필요합니다
- Infrastructure Docker 환경이 구동된 상태에서만 애플리케이션 실행 가능
- 포트 1109가 사용 중이지 않은지 확인 필요

**문제 해결:**
```bash
# 포트 충돌 시
sudo lsof -i :1109
# 결과에서 PID 확인 후 종료: sudo kill -9 [PID]

# 컨테이너 시작 실패 시
docker-compose down -v  # 완전 정리
docker system prune     # Docker 시스템 정리
docker-compose up -d    # 재시작

# 데이터베이스 연결 실패 시
docker-compose logs mariadb  # 로그 확인
docker exec -it mariadb-primavera mysqladmin ping  # 서비스 확인
```

## 🧪 테스팅 환경 가이드

### Profile 기반 자동 데이터베이스 선택
Primavera는 Spring Profile에 따라 **자동으로** 데이터베이스 환경을 선택합니다:

| Profile | 데이터베이스 | 용도 | 실행 방법 |
|---------|-------------|------|-----------|
| **`local`** | 🐳 **localhost Docker MariaDB 11.4.7** | 로컬 개발, 디버깅 | `./gradlew :chapXX:bootRun -Dspring.profiles.active=local` |
| **`test`** | 🧪 **TestContainers MariaDB 11.4.7** | 자동화 테스트, CI/CD | `./gradlew :chapXX:test` |

### 🏠 로컬 개발 환경 설정

#### 1. Docker MariaDB 11.4.7 시작
```bash
# MariaDB 컨테이너 실행 (한 번만 실행)
docker run -d \
  --name mariadb-primavera-local \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3306:3306 \
  --restart=unless-stopped \
  mariadb:11.4.7

# 컨테이너 상태 확인
docker ps | grep mariadb-primavera-local
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

#### 2. spring-boot-starter-test-container 사용법

**간단한 어노테이션 기반 사용:**
```java
@PrimaveraTestContainer  // 자동으로 MariaDB TestContainer 설정
@DisplayName("Article 통합 테스트")
class ArticleIntegrationTest {
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Test
    @DisplayName("게시글 저장 및 조회")
    void shouldSaveAndRetrieveArticle() {
        // TestContainers MariaDB 11.4.7에서 자동 테스트
        Article article = Article.builder()
            .subject("테스트 게시글")
            .status(ArticleStatus.PUBLIC)
            .build();
            
        int result = articleMapper.save(article);
        assertEquals(1, result);
    }
}
```

**수동 설정 방식:**
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(TestContainerAutoConfiguration.class)
class ManualConfigTest {
    
    @Autowired
    private MariaDBContainer<?> mariaDBContainer;
    
    @Test
    void testWithContainer() {
        assertNotNull(mariaDBContainer);
        assertTrue(mariaDBContainer.isRunning());
    }
}
```

#### 3. TestContainers 설정 커스터마이징
```yaml
# application-test.yml
primavera:
  testcontainers:
    enabled: true
    mariadb:
      image-name: mariadb:11.4.7
      database-name: primavera
      username: primavera
      password: primavera
      reuse: true  # 컨테이너 재사용으로 테스트 속도 향상
      init-script: sql/schema.sql
      url-params:
        allowPublicKeyRetrieval: true
        useSSL: false
        serverTimezone: UTC
        characterEncoding: UTF-8
```

### 🚀 환경별 실행 요약

#### 로컬 개발 워크플로우
```bash
# 1. MariaDB 컨테이너 시작 (최초 1회)
docker start mariadb-primavera-local

# 2. 로컬 환경으로 애플리케이션 실행
./gradlew :chap11:bootRun -Dspring.profiles.active=local

# 3. 브라우저에서 확인
# http://localhost:8080
```

#### 테스트 실행 워크플로우  
```bash
# TestContainers가 자동으로 MariaDB 컨테이너 관리
./gradlew :chap11:test

# spring-boot-starter-test-container가 자동으로:
# 1. MariaDB 11.4.7 Docker 이미지 다운로드 (최초 1회)
# 2. 테스트용 컨테이너 시작
# 3. 데이터소스 자동 설정 (URL, 사용자명, 비밀번호)
# 4. 초기화 스크립트 실행 (schema.sql)
# 5. 테스트 완료 후 컨테이너 자동 정리
```

### 🐳 TestContainers 상세 설명

**TestContainers란?**
- **정의**: 실제 데이터베이스나 서비스를 Docker 컨테이너로 실행하여 테스트하는 Java 라이브러리
- **목적**: 통합 테스트에서 실제 환경과 동일한 조건으로 테스트 수행
- **장점**: 
  - ✅ **격리된 환경**: 각 테스트가 독립적인 데이터베이스 환경에서 실행
  - ✅ **일관성**: 로컬, CI/CD 어디서나 동일한 데이터베이스 버전 사용
  - ✅ **자동 관리**: 컨테이너 생성, 시작, 정리가 자동으로 수행
  - ✅ **실제 환경**: 인메모리 DB가 아닌 실제 MariaDB로 테스트

**Primavera의 TestContainers 구성:**
```java
// spring-boot-starter-test-container가 자동으로 생성하는 설정
@Container
static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7")
    .withDatabaseName("primavera")
    .withUsername("primavera") 
    .withPassword("primavera")
    .withInitScript("sql/schema.sql")
    .withReuse(true);  // 성능 향상을 위한 컨테이너 재사용
```

**자동 데이터소스 설정:**
```yaml
# TestContainers가 자동으로 설정하는 값들
spring:
  datasource:
    url: jdbc:mariadb://localhost:${random-port}/primavera?allowPublicKeyRetrieval=true&useSSL=false
    username: primavera
    password: primavera
    driver-class-name: org.mariadb.jdbc.Driver
```

### 💡 주요 특징

✅ **환경 자동 선택**: Profile만 지정하면 DB 환경 자동 결정  
✅ **Docker 기반**: 모든 환경에서 MariaDB 11.4.7 동일 버전 사용  
✅ **CI/CD 친화적**: TestContainers로 외부 의존성 없는 테스트  
✅ **개발 효율성**: 로컬은 빠른 개발, 테스트는 격리된 환경  
✅ **버전 일관성**: 개발/테스트/프로덕션 동일한 MariaDB 11.4.7  
✅ **커스텀 스타터**: spring-boot-starter-test-container로 간편한 설정  

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

#### **Chapter 10** - OAuth2 소셜 로그인 & HTTPS
- **학습 목표**: 현대적인 인증 시스템 및 보안 통신 구현
- **주요 내용**:
  - Spring Security OAuth2 Client로 4개 소셜 로그인 통합 (Google, Facebook, GitHub, Kakao)
  - PKCS12 인증서 기반 HTTPS/SSL 보안 통신
  - Lucy XSS Filter 통합으로 웹 보안 강화
  - 소셜 계정과 내부 사용자 시스템 연동
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

#### **Chapter 17** - 엔터프라이즈 데이터 파이프라인 ⭐ *New*
- **학습 목표**: Spring Batch + Debezium Embedded를 활용한 실시간 데이터 파이프라인 구축
- **주요 내용**:
  - **Spring Batch**: 대용량 초기 인덱싱 (Products + Sellers + Categories)
  - **Debezium Embedded**: Kafka 없이 MariaDB CDC 실시간 처리
  - **Elasticsearch**: 검색 최적화된 문서 저장소
  - **경량 아키텍처**: 별도 Kafka 인프라 불필요한 CDC 구현
- **서브모듈 구조**:
  - `chap17/batch`: 초기 전체 데이터 인덱싱
  - `chap17/streaming`: 실시간 CDC 업데이트
- **혁신 기능**:
  - Chunk 기반 배치 처리로 메모리 효율성 극대화
  - binlog 기반 실시간 변경 감지
  - 배치와 스트리밍 조합으로 최종 일관성 보장

#### **Chapter 18** - 마이크로서비스 + Kafka 이벤트 시스템 ⭐ *Enhanced*
- **학습 목표**: 이벤트 기반 마이크로서비스 아키텍처 및 Saga 패턴 구현
- **주요 내용**:
  - **마이크로서비스 분해**: Order, Product, Account, Front, Configuration 서비스
  - **Kafka 이벤트 기반 통신**: 주문 생성 → 재고 처리 → 주문 취소 시나리오
  - **WebFlux + R2DBC**: 완전한 리액티브 스택
  - **Saga 패턴**: 분산 트랜잭션 및 보상 트랜잭션 처리
- **실시간 이벤트 플로우**:
  ```
  주문 생성 API → OrderCreatedEvent → 재고 확인 → InventoryReservedEvent/InsufficientEvent → 주문 확정/취소
  ```
- **고급 패턴**:
  - Event Sourcing을 통한 이벤트 기반 상태 관리
  - CQRS로 명령과 조회 책임 분리
  - 실패 시나리오별 보상 트랜잭션 자동 처리
- **기술 스택**:
  - **Order Service**: Spring WebFlux + R2DBC MariaDB + Kafka Producer/Consumer
  - **Product Service**: Spring WebFlux + MongoDB + Kafka Consumer/Producer
  - **Infrastructure**: Apache Kafka, MariaDB 11.4.7, MongoDB

## 🔐 보안 기능

### 다층 보안 아키텍처
- **HTTPS/SSL**: 전송 계층 암호화
- **OAuth2**: 표준 인증 프로토콜
- **HashiCorp Vault**: 민감정보 중앙집중식 보안 관리
- **XSS 보호**: Lucy Filter 기반 입력 검증
- **CSRF 보호**: 토큰 기반 요청 검증
- **SQL 인젝션 방어**: MyBatis 파라미터 바인딩

### 민감정보 관리 (HashiCorp Vault)
Primavera는 데이터베이스 패스워드, OAuth2 클라이언트 시크릿, JWT 시크릿 등 모든 민감정보를 HashiCorp Vault를 통해 중앙집중식으로 관리합니다.

#### 주요 특징
- **중앙집중식 관리**: 모든 환경(local, test, prod)의 민감정보를 Vault에서 통합 관리
- **정책 기반 접근 제어**: 애플리케이션용(읽기 전용), 개발자용(전체 권한) 토큰 분리
- **자동 토큰 생성**: Docker Compose 실행 시 토큰 자동 생성 및 저장
- **환경별 시크릿 분리**: local/test/prod 환경별 독립적인 시크릿 관리

#### 사용 예시
```bash
# 토큰 파일 확인
cat infrastructure/vault/app-token.txt

# Spring Boot에서 Vault 설정 활용
./gradlew :chap04:bootRun \
  -Dspring.profiles.active=vault,local \
  -Dvault.token=$(cat infrastructure/vault/app-token.txt)
```

#### 지원되는 시크릿 경로
- `secret/primavera/common`: 공통 설정 (DB 드라이버, 암호화 알고리즘 등)
- `secret/primavera/local/*`: 로컬 개발 환경 설정
- `secret/primavera/test/*`: 테스트 환경 설정  
- `secret/primavera/prod/*`: 프로덕션 환경 설정
- `secret/primavera/*/security`: OAuth2 클라이언트 시크릿, JWT 시크릿 등

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
- **데이터베이스**: MariaDB 11.4.7 컨테이너 (spring-boot-starter-test-container)
- **Redis**: Redis 컨테이너 기반 캐시 테스트
- **외부 서비스**: WireMock을 통한 API 모킹
- **전체 스택**: 실제 환경과 동일한 테스트
- **자동 구성**: @PrimaveraTestContainer 어노테이션으로 간편 설정

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