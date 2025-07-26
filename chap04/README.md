# Chapter 04 - Data Access & Dynamic Proxy

## 개요
Chapter 04는 Spring Boot의 데이터 접근 계층과 동적 프록시 패턴을 학습하는 모듈입니다. JDBC를 활용한 데이터베이스 연동, Spring AOP의 기반이 되는 동적 프록시 구현, 그리고 다양한 데이터소스 설정 방법을 다룹니다.

## 주요 기능
- **JDBC 데이터 접근**: Spring JdbcTemplate을 활용한 데이터베이스 CRUD 작업
- **동적 프록시 패턴**: Java Reflection API를 활용한 동적 프록시 구현
- **데이터소스 설정**: HikariCP 커넥션 풀 설정 및 최적화
- **AOP 기초**: 프록시 패턴을 통한 횡단 관심사 분리
- **보안**: Spring Security를 활용한 비밀번호 암호화

## 기술 스택

### 핵심 프레임워크
- Spring Boot 3.x
- Spring Web
- Spring JDBC
- Spring AOP
- Spring Security

### 데이터베이스
- MariaDB 11.x
- MyBatis Spring Boot Starter 3.x

### 테스트
- TestContainers (MariaDB)
- JUnit 5
- Spring Boot Test

### 유틸리티
- Lombok
- Commons IO
- Reflections

## 프로젝트 구조

```
chap04/
├── src/main/java/com/genius/primavera/
│   ├── DataAccessApplication.java         # 메인 애플리케이션
│   ├── UserDao.java                      # JDBC를 활용한 사용자 DAO
│   ├── PrimaveraDao.java                 # 기본 DAO 인터페이스
│   ├── application/                       # 비즈니스 로직
│   │   ├── DoSomething.java             # 서비스 인터페이스
│   │   ├── DoSomethingImpl.java         # 서비스 구현체
│   │   └── PrimaveraService.java        # 기본 서비스
│   ├── domain/                           # 도메인 모델
│   │   └── User.java                    # 사용자 엔티티
│   ├── interfaces/                       # 웹 계층
│   │   ├── PrimaveraController.java     # REST 컨트롤러
│   │   └── PrimaveraResponseAdvice.java # 응답 처리 어드바이스
│   └── proxy/dynamic/                    # 동적 프록시 구현
│       ├── ProxyFactory.java            # 프록시 팩토리
│       ├── DynamicInvocationHandler.java # 동적 호출 핸들러
│       ├── PrimaveraProxy.java          # 프록시 래퍼
│       ├── ProxyAnnotation.java         # 프록시 대상 어노테이션
│       ├── ProxyPointAnnotation.java    # 프록시 포인트 어노테이션
│       └── ProxyInvocationFailedException.java # 예외 클래스
└── src/main/resources/
    ├── application.yml                   # 애플리케이션 설정
    └── primavera.txt                    # 배너 파일
```

## 주요 컴포넌트

### 1. 데이터 접근 계층
- **UserDao**: JdbcTemplate을 사용한 사용자 데이터 CRUD
  - 사용자 저장, 조회, 삭제 기능
  - PreparedStatement를 활용한 SQL Injection 방지

### 2. 동적 프록시 시스템
- **ProxyFactory**: Reflection을 사용한 프록시 객체 생성
  - 어노테이션 기반 프록시 대상 선택
  - 런타임 프록시 객체 생성 및 관리
- **DynamicInvocationHandler**: 메소드 호출 가로채기
  - 메소드 실행 전/후 처리
  - 예외 처리 및 로깅

### 3. 서비스 계층
- **DoSomething/DoSomethingImpl**: 비즈니스 로직 인터페이스 및 구현
- **@ProxyAnnotation**: 프록시 적용 대상 표시

### 4. 웹 계층
- **PrimaveraController**: REST API 엔드포인트
- **PrimaveraResponseAdvice**: 전역 응답 처리

## 데이터베이스 스키마

### USER 테이블
```sql
CREATE TABLE `USER` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `EMAIL` varchar(45) COLLATE utf8mb4_bin DEFAULT NULL,
  `PASSWORD` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL,
  `NICK_NAME` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `STATUS` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL,
  `REG_DT` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

## 설정

### 데이터소스 설정
```yaml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:1109/primavera?allowPublicKeyRetrieval=true&useSSL=false
    username: primavera
    password: primavera
```

### HikariCP 커넥션 풀 속성

| 속성 | 설명 |
|---|---|
| maxActive | 동시에 사용할 수 있는 최대 커넥션 개수 |
| maxIdle | Connection Pool에 반납할 때 최대로 유지될 수 있는 커넥션 개수 |
| minIdle | 최소한으로 유지할 커넥션 개수 |
| initialSize | 최초로 getConnection() Method를 통해 커넥션 풀에 채워 넣을 커넥션 개수 |

## 실행 방법

### 로컬 환경 실행
```bash
./gradlew :chap04:bootRun
```

### 테스트 실행
```bash
./gradlew :chap04:test
```

## 테스트

### 주요 테스트 클래스
- **HikariDataSourceTest**: HikariCP 설정 및 동작 검증
- **SpringDataSourceTest**: Spring Boot 자동 설정 테스트
- **SpringJdbcTest**: JdbcTemplate 동작 검증
- **UserDaoTest**: 사용자 CRUD 기능 테스트
- **ProxyFactoryTest**: 동적 프록시 생성 테스트
- **EnhancerTest**: CGLIB 프록시 테스트

### 테스트 설정 주의사항
- `@AutoConfigureTestDatabase`의 기본 설정값인 Replace.Any를 Replace.NONE으로 변경
- 또는 application.yml에 `spring.test.database.replace: NONE` 설정
- TestContainers를 활용한 격리된 테스트 환경 구성

## 학습 포인트

1. **JDBC와 Spring JDBC**
   - 순수 JDBC vs JdbcTemplate의 차이점
   - 데이터소스 설정 및 커넥션 풀 관리
   - SQL 실행 및 결과 매핑

2. **동적 프록시 패턴**
   - Java Reflection API 활용
   - InvocationHandler를 통한 메소드 가로채기
   - 프록시 패턴과 AOP의 관계

3. **데이터소스 최적화**
   - HikariCP 설정 파라미터 이해
   - 커넥션 풀 크기 조정
   - 성능 모니터링

4. **테스트 전략**
   - TestContainers를 활용한 통합 테스트
   - 데이터베이스 격리 전략
   - 트랜잭션 롤백 테스트

## 특징

1. **실무 중심 설계**: 실제 프로젝트에서 사용하는 데이터 접근 패턴 구현
2. **성능 최적화**: HikariCP를 통한 효율적인 커넥션 관리
3. **보안 고려**: PreparedStatement 사용, 비밀번호 암호화
4. **테스트 용이성**: TestContainers를 통한 일관된 테스트 환경
5. **확장 가능성**: 프록시 패턴을 통한 횡단 관심사 처리

## 참고 자료
- [Spring Boot Application Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
- [HikariCP](https://github.com/brettwooldridge/HikariCP)
- [Tomcat DataSource](https://tomcat.apache.org/tomcat-9.0-doc/jdbc-pool.html)
- [DBCP2 DataSource](https://commons.apache.org/proper/commons-dbcp/)

## 주의사항

1. 로컬 환경에서는 포트 1109의 MariaDB 사용
2. 프로덕션 환경에서는 적절한 커넥션 풀 크기 설정 필요
3. 동적 프록시는 인터페이스 기반으로만 동작
4. 비밀번호는 반드시 암호화하여 저장