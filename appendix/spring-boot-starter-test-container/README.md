# Spring Boot Starter Test Container

Spring Boot 프로젝트에서 TestContainers를 쉽게 사용할 수 있도록 자동 설정을 제공하는 스타터입니다.

## 기능

- MariaDB 11.4.7 TestContainers 자동 설정
- Spring Boot AutoConfiguration 지원
- 커스텀 어노테이션으로 간편한 사용
- 설정 파일을 통한 세부 조정 가능

## 사용법

### 1. 의존성 추가

```gradle
testImplementation project(':appendix:spring-boot-starter-test-container')
```

### 2. 간단한 사용법 - @PrimaveraTestContainer 어노테이션

```java
import com.genius.primavera.test.annotation.PrimaveraTestContainer;

@PrimaveraTestContainer
class YourIntegrationTest {
    
    @Test
    void testDatabaseConnection() {
        // TestContainers MariaDB가 자동으로 시작됩니다
        // DataSource가 자동으로 설정됩니다
    }
}
```

### 3. 기존 방식 - AbstractMariaDBContainerTest 상속

```java
import com.genius.primavera.test.AbstractMariaDBContainerTest;

public class YourTest extends AbstractMariaDBContainerTest {
    
    @Test
    void testSomething() {
        // mariadb 컨테이너에 접근 가능
        assertTrue(mariadb.isRunning());
    }
}
```

### 4. 수동 설정 방식

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

## 설정 옵션

application-test.yml에서 다음과 같이 설정할 수 있습니다:

```yaml
primavera:
  testcontainers:
    enabled: true  # TestContainers 활성화 (기본값: true)
    mariadb:
      image-name: mariadb:11.4.7  # Docker 이미지
      database-name: primavera     # 데이터베이스 이름
      username: primavera          # 사용자명
      password: primavera          # 비밀번호
      reuse: true                  # 컨테이너 재사용
      init-script: sql/schema.sql  # 초기화 SQL 스크립트
      url-params:
        allowPublicKeyRetrieval: true
        useSSL: false
        serverTimezone: UTC
        characterEncoding: UTF-8
```

## 자동 설정 기능

### 1. 데이터소스 자동 설정
- spring.datasource.url
- spring.datasource.username
- spring.datasource.password
- spring.datasource.driver-class-name

### 2. 테스트 환경 최적화
- HikariCP 커넥션 풀 크기 조정
- Hibernate DDL 자동 생성
- TestContainers 로깅 레벨 조정

### 3. URL 파라미터 자동 추가
- allowPublicKeyRetrieval=true
- useSSL=false
- 기타 커스텀 파라미터

## 주의사항

1. Docker가 실행 중이어야 합니다
2. 테스트 프로파일(`@ActiveProfiles("test")`)이 활성화되어야 합니다
3. 초기화 스크립트는 src/test/resources 경로에 위치해야 합니다

## 문제 해결

### 컨테이너가 시작되지 않는 경우
```properties
logging.level.org.testcontainers=DEBUG
logging.level.com.github.dockerjava=DEBUG
```

### 연결 문제가 발생하는 경우
URL 파라미터를 확인하고 필요시 추가 설정:
```yaml
primavera:
  testcontainers:
    mariadb:
      url-params:
        connectTimeout: 60000
        socketTimeout: 60000
```