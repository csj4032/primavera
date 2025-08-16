# Primavera - Spring Boot 교육용 프로젝트

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.6-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![MariaDB](https://img.shields.io/badge/MariaDB-11.4.7-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

Primavera는 기본 개념부터 고급 마이크로서비스 아키텍처까지 점진적 학습을 제공하는 종합적인 Spring Boot 교육용 프로젝트입니다.

## 🎯 프로젝트 개요

**27개 모듈**로 구성된 점진적 학습용 Spring Boot 교육 프로젝트로, 기초 개념부터 **완전한 마이크로서비스 아키텍처**까지 각 단계별로 독립적인 애플리케이션을 제공하여 체계적인 학습이 가능합니다.

### 📚 학습 단계

```
Phase 1: 기초 (chap01-04) → Phase 2: 웹 개발 (chap05-08) 
    ↓                           ↓
Phase 5: 아키텍처 (chap17-18) ← Phase 4: 고급 (chap13-16) ← Phase 3: 보안 (chap09-12)
```

## 🏗️ 모듈 구조

### Phase 1: 기초 개념 (chap01-chap04)
| 모듈 | 주요 학습 내용 | 기술 스택 |
|------|---------------|----------|
| **[chap01](./chap01)** | Spring Boot 기본, DI 컨테이너 | Spring Boot, Web MVC |
| **[chap02](./chap02)** | 설정 관리, XML/Properties/YAML | Configuration, Web MVC |
| **[chap03](./chap03)** | MVC 패턴, AOP 기초 | Web MVC, AOP, Thymeleaf |
| **[chap04](./chap04)** | 데이터 액세스 기초 | MyBatis, MariaDB, TestContainers |

### Phase 2: 웹 개발 기초 (chap05-chap08)
| 모듈 | 주요 학습 내용 | 기술 스택 |
|------|---------------|----------|
| **[chap05](./chap05)** | MyBatis 심화, 로깅 시스템 | MyBatis, Vault Config |
| **[chap06](./chap06)** | 유효성 검사, XSS 보안, 국제화 | Validation, Lucy Filter, Security |
| **[chap07](./chap07)** | Thymeleaf 템플릿, JPA 매핑 | JPA, Thymeleaf, Security |
| **[chap08](./chap08)** | Spring Security 기초, 필터 체인 | Spring Security, MyBatis |

### Phase 3: 인증/인가 고급 (chap09-chap13)
| 모듈 | 주요 학습 내용 | 기술 스택 |
|------|---------------|----------|
| **[chap09](./chap09)** | Spring Security 기본, 역할 기반 권한 | Spring Security, MyBatis |
| **[chap10](./chap10)** | OAuth2 소셜 로그인, 캐싱 | OAuth2, Redis, Caffeine |
| **[chap11](./chap11)** | 소셜 로그인 고급, 게시판 시스템 | OAuth2, MyBatis |
| **[chap12](./chap12)** | 계층형 댓글, 파일 업로드 | File Upload, OAuth2 |
| **[chap13](./chap13)** | 고급 인가, **Reactive** 기초 | **WebFlux**, MongoDB, Redis |

### Phase 4: 운영 환경 고급 (chap14-chap16)
| 모듈 | 주요 학습 내용 | 기술 스택 |
|------|---------------|----------|
| **[chap14](./chap14)** | JPA 고급, **Reactive** 심화 | **WebFlux**, JPA Envers, MongoDB |
| **[chap15](./chap15)** | JPA 관계 매핑 심화 **(NO WEB)** | **JPA Advanced**, QueryDSL |
| **[chap16](./chap16)** | 파일 처리, S3, Excel, 모니터링 | AWS S3, Excel, Actuator |

### Phase 5: 아키텍처 고급 (chap17-chap18)
| 모듈 | 주요 학습 내용 | 기술 스택 |
|------|---------------|----------|
| **[chap17:common](./chap17/common)** | 공통 도메인 모델 **(NO WEB)** | JPA Entities, 공통 라이브러리 |
| **[chap17:batch](./chap17/batch)** | **Spring Batch** 배치 처리 | **Spring Batch**, GraalVM |
| **[chap17:streaming](./chap17/streaming)** | 실시간 스트리밍, WebSocket | **WebFlux**, WebSocket |
| **[chap18](./chap18)** | **완전한 마이크로서비스 아키텍처** | **Spring Cloud**, **Kafka**, **Multiple DBs** |

#### chap18 마이크로서비스 구성
| 서비스 | 포트 | 주요 학습 내용 | 기술 스택 |
|--------|------|---------------|----------|
| **[configuration](./chap18/configuration)** | 8888 | 중앙화된 설정 관리 | **Spring Cloud Config**, Vault |
| **[account](./chap18/account)** | 8080 | 반응형 사용자 관리 | **WebFlux**, Redis, Reactive Streams |
| **[front](./chap18/front)** | 8081 | API Gateway & 오케스트레이션 | **WebFlux**, Service Orchestration |
| **[order](./chap18/order)** | 8082 | 주문 처리 & 할인 정책 엔진 | **WebFlux**, **R2DBC**, **Kafka**, Strategy Pattern |
| **[product](./chap18/product)** | 8083 | 상품 관리 & 고급 캐싱 | **AOP**, **MongoDB**, Custom Annotations |

### 유틸리티 모듈
| 모듈 | 주요 기능 | 기술 스택 |
|------|----------|----------|
| **[preface](./preface)** | 기본 개념 설명용 | 기본 Spring Boot |
| **[appendix:lucy-filter](./appendix/spring-boot-starter-lucy-filter)** | XSS 보안 커스텀 스타터 | Custom Starter |
| **[appendix:test-containers](./appendix/spring-boot-starter-test-containers)** | TestContainers 커스텀 스타터 | Custom Starter |

## 🚀 빠른 시작

### 시스템 요구사항
- **Java**: 21+ (LTS)
- **Docker**: 최신 버전
- **IDE**: IntelliJ IDEA 또는 VS Code

### 1️⃣ 프로젝트 클론
```bash
git clone https://github.com/your-repo/primavera.git
cd primavera
```

### 2️⃣ 전체 프로젝트 빌드
```bash
./gradlew clean build --parallel
```

### 3️⃣ 특정 모듈 실행
```bash
# 기본 예제 실행
./gradlew :chap01:bootRun

# 데이터베이스 연동 예제 (Docker 필요)
./gradlew :chap04:bootRun

# Reactive 예제
./gradlew :chap13:bootRun

# 마이크로서비스 아키텍처 (인프라 먼저 시작 필요)
./docker-manager.sh start chap18
SPRING_PROFILES_ACTIVE=native ./gradlew :chap18:configuration:bootRun &
VAULT_TOKEN=primavera-vault-token ./gradlew :chap18:account:bootRun
```

## 🛠️ 기술 스택

### 핵심 기술
- **Spring Boot**: 3.3.6
- **Java**: 21+ (최신 기능 활용)
- **데이터베이스**: MariaDB 11.4.7
- **빌드 도구**: Gradle (중앙화된 의존성 관리)
- **테스팅**: JUnit 5, Mockito, TestContainers

### 주요 라이브러리
- **ORM**: JPA/Hibernate, MyBatis 3.0.4, R2DBC (반응형)
- **보안**: Spring Security 6.4.4, OAuth2, Vault (암호화)
- **템플릿**: Thymeleaf 3.4.0
- **캐싱**: Redis, Caffeine, AOP 기반 커스텀 캐싱
- **반응형**: WebFlux (Reactor), Reactive Streams
- **배치**: Spring Batch, 실시간 스트리밍
- **클라우드**: Spring Cloud Config, Vault
- **메시징**: Apache Kafka, 이벤트 기반 아키텍처
- **데이터베이스**: MariaDB 11.4.7, MongoDB 7.0, Redis 7.0

## 🧪 테스트 전략

### 3계층 테스트 접근법
1. **단위 테스트**: Mockito 기반 격리된 테스트
2. **통합 테스트**: TestContainers + 전체 Spring 컨텍스트
3. **수동 테스트**: Postman/curl 스크립트

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :chap04:test

# TestContainers 기반 통합 테스트
./gradlew :chap04:test --tests *IntegrationTest
```

## 📊 인프라 관리

### 중앙화된 Docker 관리
```bash
# 특정 챕터 환경 시작
./docker-manager.sh start chap04

# 모든 환경 시작
./docker-manager.sh start-all

# 상태 확인
./docker-manager.sh status-all
```

### 데이터베이스 포트 할당
- **chap04**: MariaDB 3308
- **chap05**: MariaDB 3309
- **chap06**: MariaDB 3310
- *... (각 모듈별 독립 포트)*
- **chap18**: MariaDB 3306, MongoDB 27017, Redis 6379, Kafka 9092, Vault 8200

## 📖 학습 가이드

### 🎯 권장 학습 순서
1. **기초 과정**: chap01 → chap02 → chap03 → chap04
2. **웹 개발**: chap05 → chap06 → chap07 → chap08
3. **보안 심화**: chap09 → chap10 → chap11 → chap12
4. **고급 기능**: chap13 → chap14 → chap15 → chap16
5. **아키텍처**: chap17 → chap18

### 🔄 점진적 학습 특징
- **독립 모듈**: 각 챕터는 완전히 독립적으로 실행 가능
- **개념 발전**: 이전 개념을 기반으로 점진적 복잡성 증가
- **실전 시뮬레이션**: 실제 프로젝트 발전 과정 재현
- **모던 기술**: Java 21+ 최신 기능 적극 활용

## 🎨 코딩 철학

### SOLID 원칙 준수
- **SRP**: 단일 책임 원칙
- **OCP**: 개방-폐쇄 원칙
- **LSP**: 리스코프 치환 원칙
- **ISP**: 인터페이스 분리 원칙
- **DIP**: 의존관계 역전 원칙

### 모던 Java 활용
- Record 클래스, 패턴 매칭, 텍스트 블록
- Stream API, Optional 체이닝
- 함수형 프로그래밍 패러다임
- 불변 객체 및 순수 함수 선호

## 🚦 아키텍처 패턴

### 전통적 MVC → Reactive → Microservices
```
Traditional MVC (chap01-12)
    ↓ 점진적 복잡성 증가
Reactive Programming (chap13-14)
    ↓ WebFlux, MongoDB, Redis
Batch & Streaming (chap17)
    ↓ 실시간 처리, WebSocket
Complete Microservices (chap18)
    ↓ 5개 서비스, 이벤트 기반 아키텍처
┌─────────────────────────────────┐
│  Front(8081) → Account(8080)    │
│              → Order(8082)      │
│              → Product(8083)    │
│  Config(8888) ← All Services    │
└─────────────────────────────────┘
```

## 📋 주요 명령어

### 개발 명령어
```bash
# 전체 빌드
./gradlew clean build --parallel

# 특정 모듈 빌드
./gradlew :chap04:build

# 애플리케이션 실행
./gradlew :chap04:bootRun

# 테스트 실행
./gradlew :chap04:test --tests PrimaveraServiceTest

# 마이크로서비스 전체 시작 (chap18)
./docker-manager.sh start chap18
SPRING_PROFILES_ACTIVE=native ./gradlew :chap18:configuration:bootRun &
sleep 10 && VAULT_TOKEN=primavera-vault-token ./gradlew :chap18:account:bootRun
```

### Docker 관리
```bash
# 모든 서비스 시작
./docker-manager.sh start-all

# 특정 서비스 중지
./docker-manager.sh stop chap04

# 상태 확인
./docker-manager.sh status-all
```

## 🤝 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 지원

- **Issues**: [GitHub Issues](https://github.com/your-repo/primavera/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-repo/primavera/discussions)
- **Documentation**: 각 모듈의 README.md 참조

---

**Happy Learning! 🎓**

> *"단순한 것에서 복잡한 것으로, 하나씩 차근차근 배워가는 Spring Boot 여정"*