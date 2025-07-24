# Primavera
스프링부트를 이용한 커뮤니티 사이트 개발

[![Build Status](https://travis-ci.org/csj4032/primavera.svg?branch=master)](https://travis-ci.org/csj4032/primavera)
[![Coverage Status](https://coveralls.io/repos/github/csj4032/primavera/badge.svg)](https://coveralls.io/github/csj4032/primavera)

## Technical Specification
* Java 21 (Switch expressions, text blocks, records)
* Gradle 8.12.1
* IntelliJ IDEA (2024.2.5)
* MariaDB 10.3.14
* Spring Boot 3.4.4 (https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)
* Spring Security 6.4.4 with OAuth2 Client Support
* Spring Cloud 2024.0.1
* Thymeleaf 3.4.0 with Spring Security Integration
* MyBatis 3.0.4 with Dynamic SQL
* JPA with Hibernate
* Lombok 1.18.36
* Lucy XSS Filter 2.0.1
* Flyway Database Migration
* Bootstrap 5.3.3
* Undertow (Embedded Server)
* SSL/HTTPS Support with PKCS12
* Sentry Integration
* Log4jdbc for SQL Logging

## Requirements Specification
* **Social Authentication**: OAuth2 기반 회원 가입 및 로그인 (Google, Facebook, GitHub, Kakao)
* **Security**: HTTPS/SSL 보안 통신 및 역할 기반 접근 제어
* **User Management**: 소셜 정보를 이용한 로그인, 로그아웃, 탈퇴
* **Content Management**: 게시글 등록, 수정, 삭제, 조회 기능
* **Hierarchical Comments**: 게시글 답글 및 댓글, 대댓글 시스템
* **File Management**: 게시글 관련 첨부파일 등록, 삭제
* **Rich Text Editing**: WYSIHTML5 기반 게시글 편집 기능
* **XSS Protection**: Lucy XSS Filter를 통한 보안 강화

## Project Architecture
```
primavera/
├── chap00-16/          # 단계별 학습 모듈
├── config/             # 구성 파일 저장소
├── docker/             # Docker 컨테이너 구성 파일
├── hello/              # Spring Cloud 튜토리얼
├── spring-boot-starter-lucy-filter/  # 사용자 정의 스프링 부트 스타터: Lucy XSS 필터
└── spring-boot-starter-social-kakao/ # 사용자 정의 스프링 부트 스타터: 카카오 소셜 로그인
```

## Database Schema
![Primavera DB Schema](https://github.com/csj4032/primavera/blob/master/primavera.png)

## Launch
![IntelliJ, Gradle](https://github.com/csj4032/primavera/blob/master/gradle.png)

## Docker 실행 방법
```bash
# MariaDB 컨테이너 실행
docker-compose up -d mariadb

# 전체 서비스 실행 (개발 환경)
docker-compose -f docker-compose.yml up
```

## 챕터별 주요 내용

## chap00 - Spring Boot 기초
* **빌드 도구**: Gradle, Maven 비교 및 설정
* **프로젝트 초기화**: Spring Initializr를 이용한 프로젝트 생성
* **필수 플러그인**: Lombok 설정 및 활용
* **Spring Boot 핵심 개념**: 
  * Domain 구조 설계
  * @SpringBootConfiguration 설정
  * @EnableAutoConfiguration 동작 원리
  * Application Context 초기화 과정
  * SpringApplicationBuilder를 통한 애플리케이션 구성

## chap01 - Spring Boot 시작
* **첫 애플리케이션**: Hello World 구현
* **구성 관리**: 
  * application.properties/yml 활용
  * 프로필 기반 구성
  * 외부 구성 주입
* **Bean 라이프사이클**: 컨테이너 내 빈 생성 및 관리

## chap02 - 테스트와 AOP
* **Spring Boot Test**: 
  * 단위 테스트와 통합 테스트
  * @SpringBootTest 어노테이션
  * MockMvc를 이용한 컨트롤러 테스트
* **AOP(Aspect-Oriented Programming)**: 
  * 관점 지향 프로그래밍 구현
  * @Aspect, @Pointcut, @Around 활용
  * 로깅, 성능 측정, 트랜잭션 관리 적용

## chap03 - 데이터 접근
* **DataSource 구성**: 
  * JDBC 연결 설정
  * 다중 DataSource 관리
* **JdbcTemplate**: SQL 쿼리 실행 및 결과 처리
* **커넥션 풀**: Hikari CP 최적화 설정
* **트랜잭션 관리**: 선언적 트랜잭션 처리

## chap04 - 트랜잭션과 MyBatis
* **Spring Transaction**: 
  * 트랜잭션 경계 설정
  * 트랜잭션 전파 속성
  * 롤백 정책
* **MyBatis 통합**: 
  * SqlSessionFactory 구성
  * 매퍼 인터페이스 활용
  * 동적 SQL 작성
* **로깅 구성**: Logback을 이용한 SQL 로그 설정

## chap05 - 데이터 검증과 처리
* **입력 검증**: 
  * Bean Validation (JSR-380)
  * 사용자 정의 유효성 검증
* **비동기 처리**: AJAX 요청 및 응답 처리
* **함수형 프로그래밍**: JOOL 라이브러리 활용
* **동적 쿼리**: MyBatis DSL로 복잡한 쿼리 구성

## chap06 - 뷰 템플릿과 인증
* **Thymeleaf 템플릿 엔진**: 
  * 레이아웃 구성
  * 표현식과 유틸리티 객체
  * 조건부 렌더링
* **사용자 인증**: 
  * 로그인 폼 구현
  * 인증 처리 로직
  * 세션 관리

## chap07 - 필터와 로깅
* **웹 필터**: 
  * Filter 인터페이스 구현
  * FilterRegistrationBean 설정
* **보안 필터**: Lucy-xss-filter로 XSS 공격 방어
* **SQL 로깅**: Log4jdbc 설정 및 활용
* **웹 서버**: Undertow 서버 최적화 설정

## chap08 - 디자인 패턴과 보안
* **책임 연쇄 패턴**: Chain of Responsibility 구현
* **Spring Security**: 
  * 보안 구성
  * 인증 및 권한 부여
  * CSRF 보호
* **커스텀 스타터**: Spring Boot Starter 개발 방법론

## chap09 - 소셜 로그인과 HTTPS ⭐ *Currently Active*
* **HTTPS/SSL 구성**: 
  * PKCS12 키스토어 생성
  * 자체 서명 인증서 설정
  * Undertow HTTPS 설정
* **OAuth2 소셜 로그인**: 
  * Google, Facebook, GitHub, Kakao 통합
  * OAuth2 클라이언트 설정
  * 소셜 사용자 정보 매핑
* **Spring Security 6.4.4**: 
  * 역할 기반 접근 제어 (USER, MANAGER, ADMINISTRATOR)
  * SecurityFilterChain 설정
  * 인증 성공/실패 핸들러
* **도메인 주도 설계**: 레이어 분리와 클린 코드 구조
* **환경 구성**: 환경 변수를 통한 OAuth2 클라이언트 자격 증명 관리

## chap10 - 게시판 구현
* **게시글 관리**: 
  * CRUD 기능 구현
  * 권한별 접근 제어
* **페이징 처리**: 
  * 커스텀 페이지네이션 구현
  * 정렬 및 필터링
* **리치 텍스트 에디터**: WYSIHTML5 통합

## chap11 - 계층형 댓글 시스템
* **계층형 게시글**: 
  * 트리 구조 설계
  * 재귀적 쿼리 처리
* **답글 시스템**: 
  * 대댓글 구현
  * 댓글 정렬 알고리즘

## chap12 - 권한 관리와 파일 업로드
* **Spring Security 어노테이션**: 
  * @PreAuthorize, @PostAuthorize
  * @Secured, @RolesAllowed
* **메서드 수준 보안**: SpEL을 이용한 동적 권한 검사
* **파일 업로드**: 
  * MultipartFile 처리
  * 저장 및 다운로드 구현
  * 이미지 리사이징

## chap13 - JPA와 외부 API
* **JPA/Hibernate**: 
  * 엔티티 매핑
  * 연관 관계 설정
  * JPQL 쿼리
* **객체 매핑**: ModelMapper를 통한 DTO 변환
* **카카오 API**: 
  * 메시지 발송 기능
  * API 클라이언트 구현

## chap14 - 리액티브 프로그래밍과 마이크로서비스
* **WebFlux**: 
  * 리액티브 프로그래밍 패러다임
  * Mono와 Flux 활용
  * 비동기 API 구현
* **Spring Cloud Config Server**: 
  * 중앙 집중식 구성 관리
  * 동적 구성 업데이트
* **마이크로서비스 아키텍처**: 
  * 서비스 분리 (Account, Order, Product, Front, Configuration)
  * 서비스 간 통신

## chap15 - 고급 기능
* **캐싱**: 
  * Spring Cache 추상화
  * Redis 캐시 구현
* **스케줄링**: 
  * @Scheduled 어노테이션
  * Quartz 스케줄러 통합
* **이벤트 기반 프로그래밍**: 
  * Spring Events
  * 비동기 이벤트 처리

## chap16 - 배포 및 모니터링
* **도커 컨테이너화**: 
  * Dockerfile 작성
  * 멀티스테이지 빌드
* **CI/CD 파이프라인**: 
  * Travis CI 설정
  * 자동 테스트 및 배포
* **모니터링**: 
  * Actuator 엔드포인트
  * Prometheus 메트릭
  * Grafana 대시보드

## Config Directory

The `config` directory contains environment-specific and domain-specific configuration files. This structure ensures better separation of concerns, easier maintenance, and enhanced security.

### Directory Structure
```
config/
├── dev/
│   ├── payment.yml
│   ├── display.yml
│   └── search.yml
├── stage/
│   ├── payment.yml
│   ├── display.yml
│   └── search.yml
└── prod/
    ├── payment.yml
    ├── display.yml
    └── search.yml
```

### Purpose
1. **Environment-Specific Configurations**:
   - Separate configurations for `dev`, `stage`, and `prod` environments to avoid conflicts and ensure proper isolation.

2. **Domain-Specific Configurations**:
   - Each domain (e.g., `payment`, `display`, `search`) has its own configuration file for better modularity and maintainability.

3. **Security and Automation**:
   - Sensitive information is isolated per environment, reducing the risk of accidental exposure.
   - CI/CD pipelines can automatically load the appropriate configuration based on the target environment.

### Example Configuration
**`config/dev/payment.yml`**:
```yaml
payment:
  database:
    url: jdbc:mysql://dev-payment-db:3306/payment
    username: dev_user
    password: dev_password
  api:
    key: dev-payment-api-key
example.property: "I AM IN THE DEV"
```

## Custom Spring Boot Starters

### spring-boot-starter-lucy-filter
XSS 공격을 방어하기 위한 Lucy XSS 필터의 Spring Boot Starter 구현:
* **자동 구성**: 필터 자동 등록 및 설정
* **커스터마이징**: 속성 기반 필터 동작 제어
* **프로필 지원**: 환경별 필터 규칙 관리

### spring-boot-starter-social-kakao
카카오 소셜 로그인을 위한 Spring Boot Starter 구현:
* **OAuth2 통합**: Spring Security OAuth2 클라이언트와 통합
* **사용자 정보 매핑**: 카카오 사용자 프로필 데이터 처리
* **토큰 관리**: 액세스 토큰 및 리프레시 토큰 처리

## Contributors
- [csj4032](https://github.com/csj4032) - Creator and main developer

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
