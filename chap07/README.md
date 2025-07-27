## chap07 - Thymeleaf, AdminLTE, Log4jdbc

### 개요
Thymeleaf 템플릿 엔진과 AdminLTE 관리자 템플릿을 통합하여 웹 UI를 구성하고, Log4jdbc를 활용한 SQL 로깅을 구현한 모듈입니다. MVC 패턴과 RESTful API를 함께 제공합니다.

### 주요 기능
- Thymeleaf Layout Dialect를 활용한 템플릿 레이아웃 구성
- AdminLTE 3.x 기반 관리자 UI
- Session 기반 로그인/로그아웃 구현
- Log4jdbc를 통한 SQL 쿼리 및 결과 로깅
- RESTful API와 웹 페이지 동시 제공
- JPA MappedSuperclass 활용

### 기술 스택
- Spring Boot 3.x
- Thymeleaf + Layout Dialect
- AdminLTE 3.x
- MyBatis
- Log4jdbc
- MariaDB 11.4.7
- Bootstrap 4.x

### Thymeleaf and AdminLTE
* Thymeleaf 의존성, Layout 설정

```
thymeleaf:
  cache: false
  enabled: true
  prefix: classpath:/templates/
  suffix: .html
```

```
implementation('org.springframework.boot:spring-boot-starter-thymeleaf')
compile('nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.3.0')
```

* AdminLTE 추가
* 로그인, 로그아웃 기능 추가

### Log4Jdbc

* build.gradle
```
compile('org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4.1:1.16')
```

* application.yml
```
datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: net.sf.log4jdbc.sql.jdbcapi.DriverSpy
    url: jdbc:log4jdbc:mariadb://localhost:3306/primavera
    username: primavera
    password: primavera
    hikari:
      connection-test-query: SELECT 1 FROM DUAL
```

* log4jdbc.log4j2.properties
```
log4jdbc.spylogdelegator.name = net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator
log4jdbc.dump.sql.maxlinelength = 0
```
* logback.xml
```    
<logger name="jdbc.sqlonly" level="DEBUG"/>
<logger name="jdbc.resultsettable" level="DEBUG"/>
```

### JPA Relation Advance
```sql
CREATE DATABASE advance DEFAULT CHARACTER SET utf8mb4;
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER ON advance.* TO 'relation'@'%';
```

## 핵심 구현 요소

### 1. Thymeleaf Layout 구성
- **LayoutDialect Bean 등록**: ThymeleafJpaApplication에서 설정
- **레이아웃 구조**:
  - `layouts/layout.html`: 기본 레이아웃 템플릿
  - `fragments/`: header, aside, footer 조각
  - `index.html`, `login.html`: 개별 페이지
- **Thymeleaf 설정**:
  ```yaml
  thymeleaf:
    cache: false  # 개발 환경에서 캐시 비활성화
    enabled: true
    prefix: classpath:/templates/
    suffix: .html
  ```

### 2. Controller 구현
- **LoginController**: 세션 기반 인증 처리
  - GET /login: 로그인 페이지
  - POST /login: 로그인 처리
  - GET /logout: 로그아웃 처리
  - HttpSession을 통한 사용자 정보 관리

- **UserController**: RESTful API
  - GET /users: 전체 사용자 조회
  - GET /users/{id}: 특정 사용자 조회
  - POST /users/save: 사용자 저장 (Validation Group: SaveGroup)
  - POST /users/update: 사용자 수정 (Validation Group: UpdateGroup)

- **GreetingController**: 메인 페이지 처리

### 3. Log4jdbc 설정
- **데이터소스 설정**:
  ```yaml
  driver-class-name: net.sf.log4jdbc.sql.jdbcapi.DriverSpy
  url: jdbc:log4jdbc:mariadb://localhost:1109/primavera
  ```
- **log4jdbc.log4j2.properties**:
  ```properties
  log4jdbc.spylogdelegator.name = net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator
  log4jdbc.dump.sql.maxlinelength = 0  # SQL 전체 출력
  ```
- **로깅 레벨 설정**:
  - jdbc.sqlonly: SQL 쿼리만 로깅
  - jdbc.resultsettable: 결과셋을 테이블 형태로 로깅

### 4. JPA Entity 설계
- **@MappedSuperclass 활용**:
  - BaseEntity: 공통 필드 정의 (id, name, regDt, modDt)
  - Student, Professor: BaseEntity 상속
- **persistence.xml**: JPA 설정 파일

### 5. Validation 구현
- **커스텀 Validator**:
  - @Nickname 어노테이션
  - NicknameValidator 구현체
- **Validation Group**:
  - SaveGroup: 저장 시 검증
  - UpdateGroup: 수정 시 검증

### 6. AdminLTE 통합
- **정적 리소스**: `/static/` 하위에 AdminLTE 리소스 배치
- **플러그인 포함**:
  - iCheck: 체크박스/라디오 버튼 스타일링
  - Bootstrap WYSIHTML5: 텍스트 에디터
  - Input Mask: 입력 마스킹
  - jVectorMap: 벡터 지도
  - Pace: 페이지 로딩 표시

## 프로젝트 구조

### 패키지 구조
```
com.genius.primavera
├── application/          # 서비스 계층
│   ├── UserService
│   ├── UserServiceImpl
│   └── validator/
├── domain/              # 도메인 계층
│   ├── mapper/         # MyBatis 매퍼
│   ├── model/          # 도메인 모델
│   │   └── mapped/     # JPA 엔티티
│   └── typehandler/    # MyBatis TypeHandler
└── interfaces/         # 프레젠테이션 계층
    ├── GreetingController
    ├── LoginController
    └── UserController
```

### 템플릿 구조
```
templates/
├── fragments/          # 재사용 가능한 조각
│   ├── aside.html     # 사이드바
│   ├── footer.html    # 푸터
│   └── header.html    # 헤더
├── layouts/           # 레이아웃
│   └── layout.html    # 기본 레이아웃
├── index.html         # 메인 페이지
└── login.html         # 로그인 페이지
```

## 실행 방법

### 1. 데이터베이스 설정
```bash
# MariaDB 11.4.7 실행
docker run -d --name mariadb-primavera \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 1109:3306 mariadb:11.4.7

# JPA 테스트용 데이터베이스 (선택사항)
CREATE DATABASE advance DEFAULT CHARACTER SET utf8mb4;
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER ON advance.* TO 'relation'@'%';
```

### 2. 애플리케이션 실행
```bash
./gradlew :chap07:bootRun
```

### 3. 접속 정보
- URL: http://localhost:8080
- 로그인 페이지: http://localhost:8080/login
- API 엔드포인트: http://localhost:8080/users

### 4. SQL 로그 확인
콘솔에서 다음과 같은 형태로 SQL 로그 확인 가능:
- 실행된 SQL 쿼리
- 바인딩된 파라미터
- 결과셋 테이블 형식 출력

### ETC
* thymeleaf [참고](https://www.thymeleaf.org/)
* adminLTE [참고](https://adminlte.io)