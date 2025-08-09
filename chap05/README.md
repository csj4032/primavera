# Chapter 05 - MyBatis & Advanced Logging

MyBatis를 활용한 데이터베이스 접근과 고급 로깅 시스템을 구축하는 방법을 학습합니다. Spring Boot와 MyBatis의 통합, 구조화된 로깅 시스템 설계, 그리고 HashiCorp Vault를 활용한 보안 설정 관리를 다룹니다.

## 학습 목표

- **MyBatis 통합**: Spring Boot와 MyBatis의 완전한 통합 구현
- **고급 로깅**: 계층별 로깅 시스템과 파일 기반 로깅 구현
- **Vault 설정 관리**: HashiCorp Vault를 통한 보안 설정 중앙화
- **트랜잭션 관리**: @Transactional을 활용한 선언적 트랜잭션 처리

## 프로젝트 구조

```
src/main/java/com/genius/primavera/
├── MyBatisLoggingApplication.java          # 메인 애플리케이션
├── application/                           # 애플리케이션 서비스 계층
│   ├── WinnerService.java                 # 당첨자 비즈니스 로직
│   ├── WinnerServiceImpl.java            # 당첨자 서비스 구현
│   ├── NoRollbackForClass.java           # 트랜잭션 롤백 제외 설정
│   └── account/                          # 계정 관련 모듈
│       ├── AccountProcessor.java         # 계정 데이터 처리기
│       ├── AccountAnalyzer.java          # 계정 데이터 분석기
│       ├── AccountCSVParser.java         # CSV 파일 파서
│       ├── AccountParser.java            # 파서 인터페이스
│       ├── AccountGodClass.java          # 리팩토링 대상 클래스
│       └── Category.java                 # 카테고리 enum
├── domain/                               # 도메인 계층
│   ├── entity/                          # 엔티티 클래스
│   │   ├── Winner.java                  # 당첨자 엔티티
│   │   └── Account.java                 # 계정 엔티티
│   └── mapper/                          # MyBatis 매퍼
│       ├── WinnerMapper.java            # 당첨자 매퍼 인터페이스
│       └── AccountMapper.java           # 계정 매퍼 인터페이스
└── interfaces/                          # 인터페이스 계층
    └── WinnerController.java            # 당첨자 REST API

src/main/resources/
├── application-local.yml                # 로컬 개발 설정
├── application.yml                      # 기본 애플리케이션 설정
├── logback-spring.xml                   # Logback 로깅 설정
├── logging/logback/                     # 로깅 설정 모듈
│   ├── console-appender.xml             # 콘솔 출력 설정
│   ├── file-debug-appender.xml          # 디버그 파일 로깅
│   ├── file-info-appender.xml           # 정보 파일 로깅
│   ├── file-warn-appender.xml           # 경고 파일 로깅
│   └── file-error-appender.xml          # 오류 파일 로깅
└── accountInfo.csv                      # 테스트용 계정 데이터
```

## 주요 기능

### 1. MyBatis 통합 설정
```java
@SpringBootApplication
@MapperScan("com.genius.primavera.domain.mapper")
public class MyBatisLoggingApplication {
    public static void main(String[] args) {
        log.debug("PrimaveraApplication Start Debug");
        log.info("PrimaveraApplication Start Info");  
        log.warn("PrimaveraApplication Start Warn");
        log.error("PrimaveraApplication Start Error");
        SpringApplication.run(MyBatisLoggingApplication.class, args);
    }
}
```

### 2. MyBatis 매퍼 인터페이스
```java
@Mapper
public interface WinnerMapper {
    @Select("SELECT * FROM WINNER WHERE ID = #{id}")
    Optional<Winner> findById(@Param("id") Long id);
    
    @Insert("INSERT INTO WINNER (EMAIL, NICKNAME, CREATED_AT) " +
            "VALUES (#{email}, #{nickname}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Winner winner);
    
    @Update("UPDATE WINNER SET NICKNAME = #{nickname} WHERE ID = #{id}")
    int update(Winner winner);
    
    @Delete("DELETE FROM WINNER WHERE ID = #{id}")
    int delete(@Param("id") Long id);
    
    @Select("SELECT * FROM WINNER ORDER BY CREATED_AT DESC")
    List<Winner> findAll();
}
```

### 3. 트랜잭션 관리
```java
@Service
@Transactional(readOnly = true)
public class WinnerServiceImpl implements WinnerService {
    
    @Transactional
    @Override
    public Winner createWinner(Winner winner) {
        winnerMapper.insert(winner);
        return winner;
    }
    
    @Transactional(noRollbackFor = NoRollbackForClass.class)
    @Override
    public Winner updateWinnerWithSpecialHandling(Winner winner) {
        // 특정 예외에 대해서는 롤백하지 않음
        return winnerMapper.update(winner);
    }
}
```

### 4. 고급 로깅 시스템
```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!prod">
        <include resource="logging/logback/console-appender.xml"/>
    </springProfile>
    
    <include resource="logging/logback/file-debug-appender.xml"/>
    <include resource="logging/logback/file-info-appender.xml"/>
    <include resource="logging/logback/file-warn-appender.xml"/>
    <include resource="logging/logback/file-error-appender.xml"/>
    
    <logger name="com.genius.primavera" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE_DEBUG"/>
        <appender-ref ref="FILE_INFO"/>
        <appender-ref ref="FILE_WARN"/>
        <appender-ref ref="FILE_ERROR"/>
    </logger>
</configuration>
```

### 5. CSV 데이터 처리
```java
@Component
public class AccountCSVParser implements AccountParser {
    
    @Override
    public List<Account> parse(String filePath) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return reader.lines()
                .skip(1) // 헤더 스킵
                .map(this::parseAccountLine)
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("CSV 파일 파싱 오류: {}", filePath, e);
            throw new RuntimeException("CSV 파싱 실패", e);
        }
    }
    
    private Account parseAccountLine(String line) {
        String[] fields = line.split(",");
        return Account.builder()
            .date(LocalDate.parse(fields[0]))
            .description(fields[1])
            .category(Category.valueOf(fields[2]))
            .amount(new BigDecimal(fields[3]))
            .build();
    }
}
```

## 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| **Spring Boot** | 3.3.6 | 기본 프레임워크 |
| **Spring Web** | 3.3.6 | REST API 개발 |
| **MyBatis Spring Boot Starter** | 3.0.4 | ORM 프레임워크 |
| **MariaDB** | 11.4.7 | 관계형 데이터베이스 |
| **HikariCP** | 5.1.0 | 커넥션 풀 |
| **Spring Cloud Config** | 최신 | 외부 설정 관리 |
| **Vault Config** | 최신 | 보안 설정 관리 |
| **Logback** | 포함 | 로깅 프레임워크 |
| **TestContainers** | 1.21.3 | 통합 테스트 |

## 실행 방법

### 1. 데이터베이스 준비
```bash
# Docker로 MariaDB 실행
./docker-manager.sh start chap05

# 또는 수동 실행
docker run -d --name mariadb-chap05 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3308:3306 mariadb:11.4.7
```

### 2. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap05:bootRun -Dspring.profiles.active=local

# 또는 JAR로 실행
./gradlew :chap05:build
java -jar chap05/build/libs/chap05.jar --spring.profiles.active=local
```

### 3. API 테스트
```bash
# 모든 당첨자 조회
curl -X GET http://localhost:8080/api/winners

# 새 당첨자 생성
curl -X POST http://localhost:8080/api/winners \
  -H "Content-Type: application/json" \
  -d '{"email": "winner@test.com", "nickname": "TestWinner"}'

# 특정 당첨자 조회
curl -X GET http://localhost:8080/api/winners/1

# 당첨자 정보 수정
curl -X PUT http://localhost:8080/api/winners/1 \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "email": "winner@test.com", "nickname": "UpdatedWinner"}'
```

## 핵심 학습 포인트

### 1. MyBatis 통합 패턴
- **@MapperScan**: 매퍼 인터페이스 자동 스캔 설정
- **@Mapper**: 매퍼 인터페이스 표시 애너테이션
- **어노테이션 기반 매핑**: @Select, @Insert, @Update, @Delete
- **파라미터 바인딩**: @Param을 통한 명시적 파라미터 매핑

### 2. 로깅 아키텍처
- **계층별 로깅**: DEBUG, INFO, WARN, ERROR 레벨별 파일 분리
- **프로파일별 설정**: 환경에 따른 로깅 전략 분리
- **비동기 로깅**: 성능 최적화를 위한 비동기 어펜더 활용
- **로그 파일 로테이션**: 날짜별 로그 파일 관리

### 3. 트랜잭션 관리
- **선언적 트랜잭션**: @Transactional을 통한 트랜잭션 경계 설정
- **읽기 전용 최적화**: readOnly = true 설정
- **예외 기반 롤백 제어**: noRollbackFor를 통한 세밀한 제어
- **트랜잭션 전파**: REQUIRED, REQUIRES_NEW 등 전파 속성 이해

### 4. 데이터 처리 패턴
- **Strategy 패턴**: 다양한 파서 구현체 지원
- **Builder 패턴**: 복잡한 객체 생성 단순화
- **Stream API**: 함수형 스타일의 데이터 처리
- **예외 처리**: 체크 예외를 런타임 예외로 변환

## 테스트 실행

### 단위 테스트
```bash
# 전체 테스트 실행
./gradlew :chap05:test

# 특정 테스트 클래스
./gradlew :chap05:test --tests "*WinnerServiceTest"

# 테스트 커버리지 포함
./gradlew :chap05:test :chap05:jacocoTestReport
```

### 통합 테스트 (TestContainers)
```bash
# TestContainers를 사용한 통합 테스트
./gradlew :chap05:test --tests "*IntegrationTest"
```

## 설정 관리

### application-local.yml
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3308/primavera
    username: primavera
    password: primavera
    driver-class-name: org.mariadb.jdbc.Driver
  
mybatis:
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 100
    default-statement-timeout: 30

logging:
  level:
    com.genius.primavera: DEBUG
    org.springframework.web: INFO
    org.mybatis: DEBUG
```

### Vault 설정 (선택사항)
```yaml
spring:
  cloud:
    vault:
      uri: http://localhost:8200
      token: ${VAULT_TOKEN}
      database:
        enabled: true
        role: primavera-role
        backend: database
```

## 주요 애너테이션

| 애너테이션 | 용도 | 예시 |
|------------|------|------|
| `@MapperScan` | 매퍼 패키지 스캔 | `@MapperScan("com.genius.primavera.domain.mapper")` |
| `@Mapper` | MyBatis 매퍼 인터페이스 | `@Mapper public interface WinnerMapper` |
| `@Select` | SELECT 쿼리 정의 | `@Select("SELECT * FROM WINNER")` |
| `@Insert` | INSERT 쿼리 정의 | `@Insert("INSERT INTO WINNER...")` |
| `@Transactional` | 트랜잭션 경계 | `@Transactional(readOnly = true)` |
| `@Param` | 파라미터 바인딩 | `findById(@Param("id") Long id)` |

## 학습 순서

1. **MyBatis 기본 설정**: 의존성 추가 및 기본 설정
2. **매퍼 인터페이스 작성**: CRUD operations 구현
3. **서비스 계층 구현**: 비즈니스 로직과 트랜잭션 관리
4. **REST API 개발**: 컨트롤러를 통한 API 엔드포인트 제공
5. **로깅 시스템 구축**: 계층별 로깅 및 파일 관리
6. **데이터 처리**: CSV 파싱 및 배치 처리
7. **테스트 작성**: 단위 테스트 및 통합 테스트

## 활용 방법

### 1. 실무 적용
- **대용량 데이터 처리**: MyBatis의 배치 처리 활용
- **복잡한 쿼리**: 동적 SQL 및 결과 매핑 최적화
- **모니터링**: 로깅을 통한 애플리케이션 모니터링
- **보안**: Vault를 통한 민감 정보 관리

### 2. 성능 최적화
- **커넥션 풀 튜닝**: HikariCP 설정 최적화
- **쿼리 최적화**: MyBatis 2차 캐시 및 지연 로딩 활용
- **로깅 최적화**: 비동기 로깅으로 성능 향상

이 모듈은 MyBatis를 활용한 데이터 접근 계층 구축과 운영 환경에서 필요한 고급 로깅 시스템을 학습할 수 있는 종합적인 예제입니다.