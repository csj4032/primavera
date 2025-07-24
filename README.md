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

## Launch
![IntelliJ, Gradle](https://github.com/csj4032/primavera/blob/master/gradle.png)

## chap00
* Gradle : https://gradle.org
* Maven : https://maven.apache.org/
* Spring Initializr : https://start.spring.io
* Lombok Plugin
* Domain, @SpringBootConfiguration, @EnableAutoConfiguration, initializers
* SpringApplicationBuilder

## chap01
* Spring Boot Start, Hello World, Configuration

## chap02
* Spring Boot Test, AOP

## chap03
* Spring Boot DataSource, JdbcTemplate, Hikari

## chap04
* Spring Transaction, Mybatis, Logback

## chap05
* Validation, AJAX, JOOL, Mybatis DSL

## chap06
* Thymeleaf, Sign in

## chap07
* Filter, Lucy-xss-filter
* Log4jdbc
* Undertow

## chap08
* Chain of Responsibility
* Spring Security
* Spring Boot Starter

## chap09 ⭐ *Currently Active*
* **HTTPS/SSL Configuration**: PKCS12 keystore with self-signed certificate
* **OAuth2 Social Login**: Google, Facebook, GitHub, Kakao integration
* **Spring Security 6.4.4**: Role-based access control (USER, MANAGER, ADMINISTRATOR)
* **Domain-Driven Architecture**: Clean code structure with separate layers
* **Environment Configuration**: OAuth2 client credentials via environment variables
* **Custom Authentication**: Social user details services and success handlers
* **Server**: Undertow embedded server running on HTTPS port 8443

## chap10
* Post, Pagination, WYSIHTML5

## chap11
* Hierarchy Article Contents, Reply

## chap12
* Spring Security Annotation, File Upload

## chap13
* JPA, ModelMapper
* 카카오 API 메세지 보내기

## chap14
* WebFlux, Spring Config Server

## chap15
* Excel Import Export
* Sentry & Slack

## hello
* Hystrix, Openfeign, Turbine

## Current Project Status
**Active Module**: chap09 (OAuth2 Social Login with HTTPS)

**Recent Updates**:
- ✅ OAuth2 integration with 4 social providers
- ✅ SSL/HTTPS configuration with PKCS12 keystore
- ✅ Spring Security 6.4.4 with role-based access control
- ✅ Domain-driven architecture implementation
- ✅ Lucy XSS Filter for security
- ✅ Comprehensive logging configuration

**Setup Requirements**:
```bash
# Environment Variables for OAuth2
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
export FACEBOOK_CLIENT_ID=your_facebook_client_id
export FACEBOOK_CLIENT_SECRET=your_facebook_client_secret
export GITHUB_CLIENT_ID=your_github_client_id
export GITHUB_CLIENT_SECRET=your_github_client_secret
export KAKAO_CLIENT_ID=your_kakao_client_id
export KAKAO_CLIENT_SECRET=your_kakao_client_secret
```

**Access URLs**:
- Main Application: https://localhost:8443
- Login Page: https://localhost:8443/login
- OAuth2 Callbacks:
  - Google: https://localhost:8443/login/google
  - Facebook: https://localhost:8443/login/facebook
  - GitHub: https://localhost:8443/login/github
  - Kakao: https://localhost:8443/login/kakao

## Library Versions
* Lombok: 1.18.36
* Logback: 1.2.3
* SLF4J: 2.0.17
* Spring Boot: 3.4.4
* Spring Security: 6.4.4
* Spring Cloud: 2024.0.1
* MyBatis: 3.0.4
* MariaDB Connector: 3.5.2
* Bootstrap: 5.3.3
* Thymeleaf: 3.4.0

## ERD
![ERD](https://github.com/csj4032/primavera/blob/master/primavera.png)

## Database Setup

### 🐳 Docker Quick Start (Recommended)

가장 빠르고 쉬운 방법으로 데이터베이스를 설정할 수 있습니다.

#### MySQL 8.0 사용
```bash
# MySQL 컨테이너 시작 (포트 3306)
docker-compose up -d primavera-mysql

# 데이터베이스 접속 확인
docker exec -it primavera-mysql mysql -u primavera -pprimavera primavera
```

#### MariaDB 10.11 사용
```bash
# MariaDB 컨테이너 시작 (포트 3307) 
docker-compose up -d primavera-mariadb

# 데이터베이스 접속 확인
docker exec -it primavera-mariadb mysql -u primavera -pprimavera primavera
```

#### 컨테이너 관리
```bash
# 모든 데이터베이스 컨테이너 시작
docker-compose up -d

# 컨테이너 중지
docker-compose down

# 데이터 볼륨까지 완전 삭제
docker-compose down -v

# 로그 확인
docker-compose logs -f primavera-mysql
docker-compose logs -f primavera-mariadb
```

#### Spring Boot 연결 설정

**MySQL 사용시** (`application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/primavera?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    username: primavera
    password: primavera
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**MariaDB 사용시** (`application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3307/primavera?characterEncoding=UTF-8
    username: primavera
    password: primavera
    driver-class-name: org.mariadb.jdbc.Driver
```

**📋 사전 구성된 데이터**:
- **4명의 테스트 사용자** (비밀번호: `password123`)
  - genius@primavera.com (모든 권한)
  - son@primavera.com (모든 권한)  
  - messi@primavera.com (관리자, 매니저)
  - ronaldo@primavera.com (사용자)
- **샘플 게시글 및 댓글**
- **OAuth2 소셜 연동 테스트 데이터**

### 📊 Application Startup Logs

Primavera는 상세한 시작 로그를 제공하여 애플리케이션 상태를 쉽게 파악할 수 있습니다:

#### 🎨 시작시 표시되는 정보:
```
🌸 Spring Boot Community Platform with OAuth2 Social Login 🌸
🚀 Primavera Application Starting...
📅 Startup Time: 2024-07-24T10:30:45.123
🏠 Working Directory: /Users/genius/Workspace/primavera
☕ Java Version: 21.0.2 (Eclipse Adoptium)
🖥️  Operating System: Mac OS X 14.1.0 (aarch64)
✅ Spring Boot Application Started Successfully!
📦 Application Name: Primavera
🏷️  Active Profiles: []
🌐 Server Port: 8443
🔒 SSL Enabled: true
🎯 Application URL: https://localhost:8443
🗄️  Database URL: jdbc:mariadb://localhost:3306/primavera
👤 Database User: primavera
```

#### 🔑 OAuth2 구성 상태:
```
🔑 OAuth2 Social Login Configuration:
   • Google OAuth2: ✅ Configured / ❌ Not Configured
   • Facebook OAuth2: ✅ Configured / ❌ Not Configured  
   • GitHub OAuth2: ✅ Configured / ❌ Not Configured
   • Kakao OAuth2: ✅ Configured / ❌ Not Configured
```

#### 🛡️ 보안 기능 요약:
```
🛡️  Security Features:
   • Spring Security with OAuth2 Social Login
   • Role-based Access Control (USER, MANAGER, ADMINISTRATOR)
   • Lucy XSS Filter Protection
   • HTTPS/SSL Security
```

#### 🌍 사용 가능한 엔드포인트:
```
🌍 Available Endpoints:
   • Application: https://localhost:8443
   • Login Page: https://localhost:8443/login
   • Admin Panel: https://localhost:8443/admin
   • Manager Panel: https://localhost:8443/manager
```

#### 📊 로그 파일 구조:
```
📊 Monitoring & Logs:
   • Application Logs: logs/primavera.log
   • Startup Logs: logs/startup.log  
   • Security Logs: logs/security.log
   • OAuth2 Logs: logs/oauth2.log
```

**로그 레벨 설정**:
- `com.genius.primavera`: DEBUG
- `org.springframework.security`: DEBUG  
- `org.springframework.boot`: INFO
- `jdbc.sqlonly`: INFO (SQL 쿼리 로깅)
- `jdbc.sqltiming`: INFO (SQL 실행 시간)

## Spring Boot Configuration Architecture

### @SpringBootConfiguration

Primavera 프로젝트는 Spring Boot의 강력한 설정 시스템을 활용하여 엔터프라이즈급 애플리케이션을 구성합니다.

#### 🏗️ 주요 설정 클래스 구조

**1. 메인 애플리케이션 설정**
```java
@Slf4j
@SpringBootApplication  // @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
public class PrimaveraApplication {
    // SpringApplicationBuilder를 통한 커스텀 설정
    // Graceful shutdown hook 구현
    // ANSI 컬러 로깅 활성화
}
```

**핵심 컴포지트 어노테이션 `@SpringBootApplication`**:
- **@SpringBootConfiguration**: `@Configuration`의 특수화된 형태로 메인 설정 클래스임을 명시
- **@EnableAutoConfiguration**: 클래스패스 기반 자동 설정 활성화
- **@ComponentScan**: 컴포넌트 스캔 범위 자동 설정

**2. 핵심 인프라 설정** (`ApplicationConfiguration.java:42`)
```java
@Configuration
@Slf4j
public class ApplicationConfiguration implements WebMvcConfigurer {
    
    @Bean
    public XssEscapeServletFilter xssEscapeServletFilter() {
        // Lucy XSS 필터 설정
    }
    
    @Bean 
    @Primary
    public ObjectMapper objectMapper() {
        // Jackson JSON 설정
    }
}
```

**3. 보안 설정** (`PrimaveraSecurityConfiguration.java:52`)
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class PrimaveraSecurityConfiguration {
    
    // 인메모리 사용자 인증 (3가지 역할: USER, MANAGER, ADMINISTRATOR)
    // OAuth2 소셜 로그인 통합
    // SSL/HTTPS 설정
    // 커스텀 인증 필터 체인
}
```

**4. OAuth2 소셜 로그인 설정** (`PrimaveraSocialConfiguration.java:36`)
```java
@Configuration
@EnableOAuth2Client
public class PrimaveraSocialConfiguration {
    
    @ConfigurationProperties("google")
    @Bean
    public ClientResources google() { /* Google OAuth2 */ }
    
    @ConfigurationProperties("facebook") 
    @Bean
    public ClientResources facebook() { /* Facebook OAuth2 */ }
    
    @ConfigurationProperties("github")
    @Bean 
    public ClientResources github() { /* GitHub OAuth2 */ }
    
    @ConfigurationProperties("kakao")
    @Bean
    public ClientResources kakao() { /* Kakao OAuth2 */ }
}
```

#### 🚀 자동 설정 (Auto-Configuration)

**커스텀 Spring Boot Starter**: `spring-boot-starter-lucy-filter`
```java
@Configuration
@ConditionalOnClass(XssEscapeServletFilter.class)
@ConditionalOnProperty(name = "lucy.xss.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LucyFilterProperties.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class LucyFilterAutoConfiguration {
    // XSS 필터 자동 설정
}
```

**META-INF/spring.factories**:
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.genius.primavera.autoconfigure.LucyFilterAutoConfiguration
```

#### 📋 설정 바인딩 (@ConfigurationProperties)

**외부 설정 파일 연동**:
```yaml
# application.yml
google:
  client:
    clientId: ${OAUTH2_GOOGLE_CLIENTID}
    clientSecret: ${OAUTH2_GOOGLE_CLIENTSECRET}
    
server:
  ssl:
    key-store: chap09/primavera.p12
    key-store-password: primavera
    enabled: true
  port: 8443
```

#### 🔧 고급 설정 패턴

**1. 조건부 Bean 등록**:
```java
@ConditionalOnClass(XssEscapeServletFilter.class)
@ConditionalOnProperty(name = "lucy.xss.enabled")
```

**2. 프로파일 기반 설정**:
- `application.yml` (기본)
- `application-local.yml` (로컬 개발)

**3. 환경변수 기반 설정**:
```bash
OAUTH2_GOOGLE_CLIENTID=your_client_id
OAUTH2_GOOGLE_CLIENTSECRET=your_client_secret
```

**4. 멀티 레이어 설정 아키텍처**:
- **Infrastructure Layer**: `ApplicationConfiguration`
- **Security Layer**: `PrimaveraSecurityConfiguration` 
- **OAuth2 Layer**: `PrimaveraSocialConfiguration`
- **Auto-Configuration**: `LucyFilterAutoConfiguration`

#### 🎯 설정 진화 과정 (chap00 → chap09)

| Chapter | 설정 방식 | 주요 특징 |
|---------|-----------|-----------|
| **chap00** | 기본 @Configuration | XML 설정 병행 (`@ImportResource`) |
| **chap01** | @SpringBootApplication | Hello World + 기본 설정 |
| **chap09** | 엔터프라이즈급 설정 | OAuth2 + Security + SSL + 자동설정 |

#### 🛡️ 보안 설정 통합

**다중 인증 시스템**:
- **Form-based Authentication**: 기본 로그인 폼
- **OAuth2 Social Login**: 4개 소셜 플랫폼
- **Role-based Access Control**: 3단계 권한 체계
- **XSS Protection**: Lucy 필터 자동 적용

**SSL/HTTPS 설정**:
- PKCS12 인증서 지원
- 개발/운영 환경별 설정 분리
- 포트 8443에서 HTTPS 서비스

이러한 설정 아키텍처를 통해 Primavera는 확장 가능하고 유지보수가 용이한 엔터프라이즈급 Spring Boot 애플리케이션으로 발전했습니다.

### Manual Database Configuration
```sql
CREATE DATABASE primavera DEFAULT CHARACTER SET utf8mb4;

CREATE USER 'primavera'@'localhost' IDENTIFIED BY 'primavera';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER ON primavera.* TO 'primavera'@'localhost';

CREATE TABLE IF NOT EXISTS USER (
    ID       BIGINT(20)   NOT NULL AUTO_INCREMENT,
    EMAIL    VARCHAR(50)  NOT NULL,
    PASSWORD VARCHAR(100) NOT NULL,
    NICKNAME VARCHAR(45)  NOT NULL,
    STATUS   CHAR(1)      NOT NULL DEFAULT 'A',
    REG_DT   DATETIME     NOT NULL,
    MOD_DT   DATETIME              DEFAULT NULL,
    PRIMARY KEY (ID),
    UNIQUE KEY EMAIL_UNIQUE (EMAIL)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS USER_CONNECTION (
    ID           BIGINT(20)   NOT NULL AUTO_INCREMENT,
    EMAIL        VARCHAR(50)  NOT NULL,
    PROVIDER     TINYINT(11)  NOT NULL,
    PROVIDER_ID  VARCHAR(45)   NOT NULL,
    DISPLAY_NAME VARCHAR(45)  DEFAULT NULL,
    PROFILE_URL  VARCHAR(200) DEFAULT NULL,
    IMAGE_URL    VARCHAR(200) DEFAULT NULL,
    ACCESS_TOKEN VARCHAR(200) NOT NULL,
    EXPIRE_TIME  BIGINT(20)   DEFAULT NULL,
    PRIMARY KEY (ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS USER_ROLE (
    USER_ID BIGINT(20) NOT NULL,
    ROLE_ID INT(11)    NOT NULL,
    PRIMARY KEY (USER_ID, ROLE_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS ROLE (
    ID   INT(11)    NOT NULL AUTO_INCREMENT,
    TYPE TINYINT(3) NOT NULL,
    PRIMARY KEY (ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS POST (
    ID        BIGINT(20)   NOT NULL AUTO_INCREMENT,
    WRITER_ID BIGINT(20)   NOT NULL,
    SUBJECT   VARCHAR(200) NOT NULL,
    CONTENTS  TEXT         NOT NULL,
    STATUS    TINYINT(3)   NOT NULL,
    REG_DT    DATETIME DEFAULT NULL,
    MOD_DT    DATETIME DEFAULT NULL,
    PRIMARY KEY (ID),
    KEY FK_WRITER_ID (WRITER_ID),
    CONSTRAINT FK_POST_WRITER_ID FOREIGN KEY (WRITER_ID) REFERENCES USER (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS ARTICLE (
    ID BIGINT(20) NOT NULL AUTO_INCREMENT,
    P_ID BIGINT(20) NOT NULL DEFAULT 0,
    REFERENCE BIGINT(20) NOT NULL,
    STEP INT(11) NOT NULL,
    LEVEL INT(11) NOT NULL,
    AUTHOR BIGINT(20) NOT NULL,
    SUBJECT VARCHAR(200) NOT NULL,
    STATUS TINYINT(3) NOT NULL,
    HIT BIGINT(20) NOT NULL DEFAULT 0,
    RECOMMEND BIGINT(20) NOT NULL DEFAULT 0,
    DISAPPROVE BIGINT(20) NOT NULL DEFAULT 0,
    REG_DT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    MOD_DT TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (ID),
    KEY FK_WRITER_ID_IDX (AUTHOR),
    CONSTRAINT FK_ARTICLE_AUTHOR_ID FOREIGN KEY (AUTHOR) REFERENCES USER (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ARTICLE_ATTACHMENT (
    ID BIGINT(20) NOT NULL AUTO_INCREMENT,
    ARTICLE_ID BIGINT(20) NOT NULL,
    NAME VARCHAR(100) NOT NULL,
    PATH VARCHAR(200) NOT NULL,
    SIZE INT(11) NOT NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE  IF NOT EXISTS ARTICLE_COMMENT (
    ID BIGINT(20) NOT NULL AUTO_INCREMENT,
    ARTICLE_ID BIGINT(20) NOT NULL,
    LEVEL INT(11) NOT NULL,
    STEP INT(11) NOT NULL,
    COMMENT TEXT NOT NULL,
    AUTHOR VARCHAR(45) NOT NULL,
    STATUS TINYINT(3) NOT NULL,
    REG_DT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    MOD_DT TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (ID)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ARTICLE_CONTENT (
    ID         BIGINT(20) NOT NULL AUTO_INCREMENT,
    ARTICLE_ID BIGINT(20) DEFAULT NULL,
    CONTENTS   text       DEFAULT NULL,
    PRIMARY KEY (ID),
    KEY FK_AUTHOR_IDX (ARTICLE_ID),
    CONSTRAINT FK_ARTICLE_ID FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS WINNER (
    ID int(11) NOT NULL AUTO_INCREMENT,
    USER_ID int(45) NOT NULL,
    WINNER enum('WINNER','LOSER') NOT NULL,
    REG_DT datetime NOT NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO ROLE (TYPE)
VALUES (1);
INSERT INTO ROLE (TYPE)
VALUES (2);
INSERT INTO ROLE (TYPE)
VALUES (3);

INSERT INTO `USER`(EMAIL, PASSWORD, NICKNAME, STATUS, REG_DT)
VALUES ('Genius Choi', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Genius', 1, NOW());
INSERT INTO `USER_CONNECTION`(EMAIL, PROVIDER, PROVIDER_ID, DISPLAY_NAME, PROFILE_URL, IMAGE_URL, ACCESS_TOKEN, EXPIRE_TIME)
VALUES ('Genius Choi', 1, 123456789, 'Genius', 'PROFILE', 'IMAGE', '1234567890', -1);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (1, 1);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (1, 2);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (1, 3);

INSERT INTO `USER`(`EMAIL`, `PASSWORD`, `NICKNAME`, `STATUS`, `REG_DT`)
VALUES ('Son Heung-min', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Son', 1, NOW());
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (2, 1);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (2, 2);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (2, 3);

INSERT INTO `USER`(`EMAIL`, `PASSWORD`, `NICKNAME`, `STATUS`, `REG_DT`)
VALUES ('Lionel Messi', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Messi', 1, NOW());
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (3, 1);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (3, 2);

INSERT INTO `USER`(`EMAIL`, `PASSWORD`, `NICKNAME`, `STATUS`, `REG_DT`)
VALUES ('Cristiano Ronaldo', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Ronaldo', 1,NOW());
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (4, 1);

```

### Sample Data
위 SQL에는 테스트용 사용자 데이터가 포함되어 있습니다:
- **Genius Choi**: ADMINISTRATOR, MANAGER, USER 권한
- **Son Heung-min**: ADMINISTRATOR, MANAGER, USER 권한  
- **Lionel Messi**: ADMINISTRATOR, MANAGER 권한
- **Cristiano Ronaldo**: USER 권한

### JPA Entity Schema (Alternative)

```sql
CREATE TABLE `ARTICLE` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `P_ID` bigint(20) NOT NULL DEFAULT 0,
  `REFERENCE` bigint(20) NOT NULL,
  `STEP` int(11) NOT NULL,
  `LEVEL` int(11) NOT NULL,
  `AUTHOR` bigint(20) NOT NULL,
  `SUBJECT` varchar(200) NOT NULL,
  `STATUS` tinyint(3) NOT NULL,
  `HIT` bigint(20) DEFAULT 0,
  `RECOMMEND` bigint(20) DEFAULT 0,
  `DISAPPROVE` bigint(20) DEFAULT 0,
  `CONTENT_ID` bigint(20) DEFAULT NULL,
  `REG_DT` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `MOD_DT` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ARTICLE_ATTACHMENT` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ARTICLE_ID` bigint(20) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `PATH` varchar(200) NOT NULL,
  `SIZE` int(11) NOT NULL,
  `REG_DT` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `MOD_DT` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ARTICLE_COMMENT` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ARTICLE_ID` bigint(20) NOT NULL,
  `LEVEL` int(11) NOT NULL,
  `STEP` int(11) NOT NULL,
  `COMMENT` text NOT NULL,
  `AUTHOR` varchar(45) NOT NULL,
  `STATUS` tinyint(3) NOT NULL,
  `REG_DT` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `MOD_DT` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ARTICLE_CONTENT` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CONTENTS` text DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `POST` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `WRITER_ID` bigint(20) NOT NULL,
  `SUBJECT` varchar(200) NOT NULL,
  `CONTENTS` text NOT NULL,
  `STATUS` tinyint(3) NOT NULL,
  `REG_DT` timestamp NULL DEFAULT NULL,
  `MOD_DT` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ROLE` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `TYPE` int(11) NOT NULL COMMENT 'ADMINISTRATOR(1, "최고관리자"),\nMANAGER(2, "관리자"),\nUSER(3, "사용자");',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `USER` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CONNECTION_ID` bigint(20) DEFAULT NULL,
  `EMAIL` varchar(50) NOT NULL,
  `PASSWORD` varchar(200) NOT NULL,
  `NICKNAME` varchar(45) NOT NULL,
  `STATUS` char(1) NOT NULL DEFAULT 'A',
  `REG_DT` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `MOD_DT` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `EMAIL_UNIQUE` (`EMAIL`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `USER_CONNECTION` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `EMAIL` varchar(50) NOT NULL,
  `PROVIDER` int(11) NOT NULL,
  `PROVIDER_ID` varchar(45) NOT NULL,
  `DISPLAY_NAME` varchar(45) NOT NULL,
  `PROFILE_URL` varchar(200) DEFAULT NULL,
  `IMAGE_URL` varchar(200) DEFAULT NULL,
  `ACCESS_TOKEN` varchar(200) NOT NULL,
  `EXPIRE_TIME` bigint(20) DEFAULT NULL,
  `REG_DT` timestamp NULL DEFAULT NULL,
  `MOD_DT` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `USER_ROLE` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USER_ID` bigint(20) NOT NULL,
  `ROLE_ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `WINNER` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID` int(45) NOT NULL,
  `WINNER` enum('WINNER','LOSER') NOT NULL,
  `REG_DT` datetime NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO ROLE (TYPE)
VALUES (1);
INSERT INTO ROLE (TYPE)
VALUES (2);
INSERT INTO ROLE (TYPE)
VALUES (3);

INSERT INTO `USER`(EMAIL, PASSWORD, NICKNAME, STATUS, REG_DT)
VALUES ('Genius Choi', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Genius', 1, NOW());
INSERT INTO `USER_CONNECTION`(EMAIL, PROVIDER, PROVIDER_ID, DISPLAY_NAME, PROFILE_URL, IMAGE_URL, ACCESS_TOKEN, EXPIRE_TIME)
VALUES ('Genius Choi', 1, 123456789, 'Genius', 'PROFILE', 'IMAGE', '1234567890', -1);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (1, 1);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (1, 2);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (1, 3);

INSERT INTO `USER`(`EMAIL`, `PASSWORD`, `NICKNAME`, `STATUS`, `REG_DT`)
VALUES ('Son Heung-min', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Son', 1, NOW());
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (2, 1);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (2, 2);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (2, 3);

INSERT INTO `USER`(`EMAIL`, `PASSWORD`, `NICKNAME`, `STATUS`, `REG_DT`)
VALUES ('Lionel Messi', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Messi', 1, NOW());
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (3, 1);
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (3, 2);

INSERT INTO `USER`(`EMAIL`, `PASSWORD`, `NICKNAME`, `STATUS`, `REG_DT`)
VALUES ('Cristiano Ronaldo', '{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.', 'Ronaldo', 1,NOW());
INSERT INTO `USER_ROLE`(`USER_ID`, ROLE_ID)
VALUES (4, 1);
```
