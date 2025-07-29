# 🧪 Spring Boot Starter Test Container

Spring Boot 프로젝트에서 **TestContainers**를 손쉽게 사용할 수 있도록 **자동 설정**을 제공하는 커스텀 스타터입니다.

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/csj4032/primavera)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![TestContainers](https://img.shields.io/badge/TestContainers-1.20.4-orange.svg)](https://www.testcontainers.org/)
[![MariaDB](https://img.shields.io/badge/MariaDB-11.4.7-brown.svg)](https://mariadb.org/)

## 📋 목차

- [🎯 왜 만들어졌는가?](#-왜-만들어졌는가)
- [✨ 주요 기능](#-주요-기능)
- [🚀 빠른 시작](#-빠른-시작)
- [📖 사용법](#-사용법)
- [⚙️ 설정 옵션](#️-설정-옵션)
- [🏗️ 아키텍처](#️-아키텍처)
- [🔧 고급 설정](#-고급-설정)
- [🐛 문제 해결](#-문제-해결)
- [📊 성능 최적화](#-성능-최적화)

---

## 🎯 왜 만들어졌는가?

### 기존 문제점

**1. TestContainers 설정의 복잡성**
```java
// 기존 방식: 매번 반복적인 보일러플레이트 코드
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class MyIntegrationTest {
    
    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7")
        .withDatabaseName("primavera")
        .withUsername("primavera")
        .withPassword("primavera")
        .withInitScript("sql/schema.sql")
        .withCommand("--default-authentication-plugin=mysql_native_password");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mariadb.getJdbcUrl() + "?allowPublicKeyRetrieval=true&useSSL=false");
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
    }
    
    // 실제 테스트 코드...
}
```

**2. 일관성 부족**
- 프로젝트 내 각 모듈마다 다른 TestContainers 설정
- 데이터베이스 버전, 사용자명, 비밀번호 등이 모듈별로 상이
- 초기화 스크립트 경로와 설정이 통일되지 않음

**3. 유지보수의 어려움**
- 설정 변경 시 모든 테스트 클래스 수정 필요
- MariaDB 버전 업그레이드 시 여러 곳 수정 필요
- 새로운 팀원의 학습 곡선 증가

### 해결책: spring-boot-starter-test-container

**1. 간단한 어노테이션 기반 사용**
```java
// 개선된 방식: 단 한 줄의 어노테이션으로 완료!
@PrimaveraTestContainer
class MyIntegrationTest {
    // 모든 TestContainers 설정이 자동으로 완료
    // 실제 테스트 코드에만 집중 가능
}
```

**2. 중앙 집중식 설정 관리**
- 모든 TestContainers 설정을 한 곳에서 관리
- 일관된 데이터베이스 환경 보장
- 버전 업그레이드 시 한 번의 수정으로 전체 적용

**3. Spring Boot AutoConfiguration 활용**
- Spring Boot의 조건부 자동 설정 활용
- 프로파일 기반 자동 활성화
- Bean 생성 및 의존성 주입 자동화

---

## ✨주요 기능

### 🎯 핵심 기능

| 기능 | 설명 | 장점 |
|------|------|------|
| **@PrimaveraTestContainer** | 단일 어노테이션으로 모든 설정 자동화 | 99% 코드 감소 |
| **자동 DataSource 설정** | JDBC URL, 사용자명, 비밀번호 자동 구성 | 설정 오류 방지 |
| **Profile 기반 활성화** | `test` 프로파일에서만 동작 | 환경 격리 |
| **마리아DB 11.4.7 고정** | 일관된 데이터베이스 버전 사용 | 환경 일관성 |
| **초기화 스크립트 지원** | `schema.sql` 자동 실행 | 테스트 데이터 준비 |

### 🔧 고급 기능

- **컨테이너 재사용**: 테스트 실행 속도 향상
- **동적 프로퍼티 설정**: Runtime에 DataSource 설정 주입
- **HikariCP 최적화**: 테스트 환경에 최적화된 커넥션 풀
- **상속 기반 사용**: AbstractMariaDBContainerTest 상속 지원
- **커스터마이징 지원**: 어노테이션 파라미터를 통한 세부 설정

---

## 🚀 빠른 시작

### 1. 의존성 추가

**Gradle (build.gradle)**
```gradle
dependencies {
    // TestContainers 스타터 추가
    testImplementation project(':appendix:spring-boot-starter-test-container')
    
    // 기본 테스트 의존성들은 스타터가 자동으로 포함
    // - spring-boot-starter-test
    // - testcontainers
    // - mariadb-java-client
}
```

**Maven (pom.xml)**
```xml
<dependency>
    <groupId>com.genius.primavera</groupId>
    <artifactId>spring-boot-starter-test-container</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

### 2. 초기화 스크립트 준비

```sql
-- src/test/resources/sql/schema.sql
CREATE TABLE IF NOT EXISTS USER (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD VARCHAR(255) NOT NULL,
    NICKNAME VARCHAR(50) NOT NULL,
    STATUS VARCHAR(20) DEFAULT 'ACTIVE',
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 테스트 데이터
INSERT INTO USER (EMAIL, PASSWORD, NICKNAME) VALUES
('test@example.com', 'password', 'TestUser'),
('admin@example.com', 'admin123', 'Admin');
```

### 3. 첫 번째 테스트 작성

```java
import com.genius.primavera.testContainer.annotation.PrimaveraTestContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@PrimaveraTestContainer
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 저장 및 조회 테스트")
    void shouldSaveAndRetrieveUser() {
        // Given: 새로운 사용자 생성
        User user = User.builder()
                .email("newuser@example.com")
                .password("password123")
                .nickname("NewUser")
                .build();

        // When: 사용자 저장
        User savedUser = userRepository.save(user);

        // Then: 저장된 사용자 검증
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");

        // When: 저장된 사용자 조회
        Optional<User> foundUser = userRepository.findByEmail("newuser@example.com");

        // Then: 조회 결과 검증
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo("NewUser");
    }
}
```

---

## 📖 사용법

### 1. 가장 간단한 방법: @PrimaveraTestContainer

**기본 사용법**
```java
@PrimaveraTestContainer  // 🎯 이 한 줄이면 모든 설정 완료!
class ArticleServiceIntegrationTest {
    
    @Autowired
    private ArticleService articleService;
    
    @Test
    void shouldCreateArticle() {
        // MariaDB 11.4.7 TestContainer가 자동으로 시작됨
        // DataSource 자동 설정됨
        // schema.sql 자동 실행됨
        
        Article article = articleService.create("Test Title", "Test Content");
        assertThat(article.getId()).isNotNull();
    }
}
```

**커스터마이징된 사용법**
```java
@PrimaveraTestContainer(
    mariadbVersion = "mariadb:11.4.7",     // MariaDB 버전 지정
    databaseName = "test_db",               // 데이터베이스 이름
    username = "test_user",                 // 사용자명
    password = "test_pass",                 // 비밀번호
    initScript = "sql/custom-schema.sql",   // 커스텀 초기화 스크립트
    enableInitScript = true,                // 초기화 스크립트 활성화
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CustomConfigurationTest {
    
    @Test
    void testWithCustomConfiguration() {
        // 커스터마이징된 설정으로 테스트 실행
    }
}
```

### 2. 상속 기반 방법: AbstractMariaDBContainerTest

```java
import com.genius.primavera.testContainer.AbstractMariaDBContainerTest;

class UserServiceTest extends AbstractMariaDBContainerTest {

    @Autowired
    private UserService userService;

    @Test
    void testUserCreation() {
        // mariadb 컨테이너에 직접 접근 가능
        assertThat(mariadb.isRunning()).isTrue();
        assertThat(mariadb.getDatabaseName()).isEqualTo("primavera");

        // 서비스 테스트 수행
        User user = userService.createUser("test@example.com", "password");
        assertThat(user.getId()).isNotNull();
    }

    @Test
    void testDirectDatabaseAccess() {
        // JDBC URL 직접 사용
        String jdbcUrl = mariadb.getJdbcUrl();

        try (Connection connection = DriverManager.getConnection(jdbcUrl,
                mariadb.getUsername(), mariadb.getPassword())) {

            try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM USER")) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                int userCount = rs.getInt(1);
                assertThat(userCount).isGreaterThan(0);
            }
        }
    }
}
```

### 3. 수동 설정 방법: @Import 사용

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(TestContainerAutoConfiguration.class)  // 수동으로 AutoConfiguration 임포트
class ManualConfigurationTest {
    
    @Autowired
    private TestContainerService testContainerService;
    
    @Autowired
    private MariaDBContainer<?> mariaDBContainer;
    
    @Test
    void testManualConfiguration() {
        // TestContainerService를 통한 접근
        assertThat(testContainerService.isRunning()).isTrue();
        assertThat(testContainerService.getJdbcUrl()).startsWith("jdbc:mariadb://");
        
        // MariaDBContainer Bean 직접 사용
        assertThat(mariaDBContainer.isRunning()).isTrue();
        assertThat(mariaDBContainer.getDatabaseName()).isEqualTo("primavera");
    }
    
    @Test
    void testServiceMethods() {
        // TestContainerService의 편의 메서드 활용
        String jdbcUrl = testContainerService.getJdbcUrl();
        String username = testContainerService.getUsername();
        String password = testContainerService.getPassword();
        String host = testContainerService.getHost();
        Integer port = testContainerService.getMappedPort();
        
        assertThat(jdbcUrl).contains("allowPublicKeyRetrieval=true&useSSL=false");
        assertThat(username).isEqualTo("primavera");
        assertThat(password).isEqualTo("primavera");
        assertThat(host).isEqualTo("localhost");
        assertThat(port).isGreaterThan(0);
    }
}
```

### 4. Profile 기반 테스트

```java
@PrimaveraTestContainer
@ActiveProfiles({"test", "integration"})  // 다중 프로파일 활성화
class ProfileBasedTest {
    
    @Value("${app.test.mode:default}")
    private String testMode;
    
    @Test
    void testProfileConfiguration() {
        // test 프로파일이 활성화되어 TestContainers 동작
        assertThat(testMode).isNotEqualTo("default");
    }
}
```

---

## ⚙️ 설정 옵션

### 1. application-test.yml 설정

```yaml
# TestContainers 전역 설정
primavera:
  testcontainers:
    enabled: true  # TestContainers 활성화 (기본값: true)
    service:
      enabled: true  # TestContainerService 활성화 (기본값: true)
    
    # MariaDB 컨테이너 설정
    mariadb:
      image-name: mariadb:11.4.7     # Docker 이미지 태그
      database-name: primavera        # 데이터베이스 이름
      username: primavera             # 사용자명
      password: primavera             # 비밀번호
      reuse: true                     # 컨테이너 재사용 (성능 향상)
      init-script: sql/schema.sql     # 초기화 SQL 스크립트
      
      # JDBC URL 파라미터
      url-params:
        allowPublicKeyRetrieval: true
        useSSL: false
        serverTimezone: UTC
        characterEncoding: UTF-8
        connectTimeout: 60000
        socketTimeout: 60000

# Spring Boot 테스트 설정
spring:
  # JPA/Hibernate 설정 (TestContainers와 함께 사용)
  jpa:
    hibernate:
      ddl-auto: create-drop  # 테스트용 DDL 전략
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        
  # HikariCP 커넥션 풀 테스트 최적화
  datasource:
    hikari:
      maximum-pool-size: 5        # 테스트용 작은 풀 사이즈
      minimum-idle: 1
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000

# 로깅 설정
logging:
  level:
    org.testcontainers: INFO                    # TestContainers 로그 레벨
    com.github.dockerjava: WARN                 # Docker Java 클라이언트 로그
    com.genius.primavera.testContainer: DEBUG           # 커스텀 스타터 로그
    org.springframework.test: INFO              # Spring Test 로그
```

### 2. @PrimaveraTestContainer 어노테이션 파라미터

```java
@PrimaveraTestContainer(
    // SpringBootTest 파라미터 (위임)
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "logging.level.org.hibernate.SQL=DEBUG"
    },
    classes = {TestApplication.class},
    
    // TestContainers 전용 파라미터
    mariadbVersion = "mariadb:11.4.7",         // MariaDB 버전
    databaseName = "test_database",            // 데이터베이스 이름
    username = "test_user",                    // 사용자명
    password = "secure_password",              // 비밀번호
    initScript = "sql/test-data.sql",          // 초기화 스크립트
    enableInitScript = true                    // 초기화 스크립트 활성화 여부
)
class AdvancedConfigurationTest {
    // 테스트 코드...
}
```

### 3. 환경별 설정 파일

**application-test-unit.yml** (단위 테스트용)
```yaml
primavera:
  testcontainers:
    mariadb:
      reuse: true           # 빠른 단위 테스트를 위한 재사용
      init-script: none     # 초기화 스크립트 비활성화
```

**application-test-integration.yml** (통합 테스트용)
```yaml
primavera:
  testcontainers:
    mariadb:
      reuse: false                      # 격리된 환경
      init-script: sql/full-schema.sql  # 완전한 스키마
      url-params:
        profileSQL: true                # SQL 프로파일링 활성화
```

---

## 🏗️ 아키텍처

### 1. 전체 아키텍처 구조

```
┌─────────────────────────────────────────────────────────────┐
│                   @PrimaveraTestContainer                   │
│                     (Entry Point)                          │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│             TestContainerAutoConfiguration                 │
│               (Spring AutoConfiguration)                   │
│  ┌─────────────────┐    ┌──────────────────────────────┐  │
│  │ Condition       │    │  DataSourceConfiguration     │  │
│  │ Checks          │    │  (DataSource Bean Creation)  │  │
│  └─────────────────┘    └──────────────────────────────┘  │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                TestContainerService                        │
│              (Container Management)                        │
│  ┌─────────────────┐    ┌──────────────────────────────┐  │
│  │ Container       │    │  Configuration               │  │
│  │ Lifecycle       │    │  Management                  │  │
│  └─────────────────┘    └──────────────────────────────┘  │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│            MariaDBContainerFactory                         │
│             (Container Creation)                           │
│  ┌─────────────────┐    ┌──────────────────────────────┐  │
│  │ Annotation      │    │  Default Container           │  │
│  │ Based Creation  │    │  Creation                    │  │
│  └─────────────────┘    └──────────────────────────────┘  │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                MariaDBContainer                            │
│              (TestContainers Core)                        │
│  ┌─────────────────┐    ┌──────────────────────────────┐  │
│  │ Docker          │    │  Database                    │  │
│  │ Management      │    │  Initialization              │  │
│  └─────────────────┘    └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 2. 컴포넌트 상세

#### TestContainerAutoConfiguration
```java
@AutoConfiguration
@ConditionalOnClass(MariaDBContainer.class)
@ConditionalOnProperty(name = "primavera.testcontainers.enabled", havingValue = "true", matchIfMissing = true)
@Profile("test")
public class TestContainerAutoConfiguration {
    
    // 조건부 Bean 생성
    @Bean
    @ConditionalOnProperty(name = "primavera.testcontainers.service.enabled", havingValue = "true", matchIfMissing = true)
    public TestContainerService testContainerService() {
        return new TestContainerService();
    }
    
    // MariaDBContainer Bean 생성
    @Bean
    @ConditionalOnBean(TestContainerService.class)
    public MariaDBContainer<?> mariaDBContainer(TestContainerService service) {
        return service.getMariaDBContainer();
    }
    
    // DataSource 자동 설정
    @Configuration
    @ConditionalOnBean(TestContainerService.class)
    public static class DataSourceConfiguration {
        // 동적 프로퍼티 설정 및 DataSource Bean 생성
    }
}
```

#### TestContainerService
```java
@Component
@Getter
public class TestContainerService {
    private final MariaDBContainer<?> mariaDBContainer;
    private final PrimaveraTestContainer config;
    
    public TestContainerService() {
        // 1. 테스트 클래스에서 @PrimaveraTestContainer 어노테이션 검색
        Class<?> testClass = MariaDBContainerFactory.findTestClass();
        this.config = testClass != null ? testClass.getAnnotation(PrimaveraTestContainer.class) : null;
        
        // 2. 어노테이션 기반 또는 기본 설정으로 컨테이너 생성
        this.mariaDBContainer = createMariaDBContainer();
        
        // 3. 컨테이너 시작
        if (!this.mariaDBContainer.isRunning()) {
            this.mariaDBContainer.start();
        }
    }
    
    // JDBC URL에 자동으로 필요한 파라미터 추가
    public String getJdbcUrl() {
        return mariaDBContainer.getJdbcUrl() + "?allowPublicKeyRetrieval=true&useSSL=false";
    }
}
```

#### MariaDBContainerFactory
```java
public class MariaDBContainerFactory {
    
    // 어노테이션 기반 컨테이너 생성
    public static MariaDBContainer<?> createFromAnnotation(Class<?> testClass) {
        PrimaveraTestContainer annotation = findAnnotation(testClass);
        if (annotation != null) return createContainer(annotation);
        return createDefaultContainer();
    }
    
    // 스택 트레이스를 통한 테스트 클래스 자동 탐지
    public static Class<?> findTestClass() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                        return clazz;
                    }
                }
            } catch (ClassNotFoundException e) {
                // 클래스를 찾을 수 없는 경우 다음으로 진행
            }
        }
        return null;
    }
}
```

### 3. 자동 설정 프로세스

```mermaid
sequenceDiagram
    participant Test as @PrimaveraTestContainer
    participant Auto as TestContainerAutoConfiguration
    participant Service as TestContainerService
    participant Factory as MariaDBContainerFactory
    participant Container as MariaDBContainer
    participant Spring as Spring Context

    Test->>Auto: 1. Spring Boot Test 시작
    Auto->>Auto: 2. 조건부 검사 (Profile, Property)
    Auto->>Service: 3. TestContainerService Bean 생성
    Service->>Factory: 4. 테스트 클래스 어노테이션 검색
    Factory->>Factory: 5. @PrimaveraTestContainer 분석
    Factory->>Container: 6. MariaDBContainer 생성 및 구성
    Container->>Container: 7. Docker 컨테이너 시작
    Service->>Auto: 8. 컨테이너 정보 반환
    Auto->>Spring: 9. DataSource 동적 프로퍼티 설정
    Spring->>Spring: 10. DataSource Bean 생성
    Spring->>Test: 11. 테스트 컨텍스트 준비 완료
```

---

## 🔧 고급 설정

### 1. 커스텀 초기화 스크립트

**복합 스크립트 구성**
```sql
-- src/test/resources/sql/advanced-schema.sql

-- 1. 스키마 생성
CREATE SCHEMA IF NOT EXISTS test_schema;
USE test_schema;

-- 2. 사용자 및 권한 설정
CREATE USER IF NOT EXISTS 'test_app'@'%' IDENTIFIED BY 'app_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON test_schema.* TO 'test_app'@'%';

-- 3. 테이블 생성
CREATE TABLE articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    author_id BIGINT NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 테스트 데이터 생성
INSERT INTO articles (title, content, author_id, status) VALUES
('Spring Boot TestContainers 가이드', 'TestContainers를 활용한 통합 테스트 방법론', 1, 'PUBLISHED'),
('MariaDB 11.4.7 새로운 기능', 'MariaDB 최신 버전의 새로운 기능들을 살펴봅니다', 1, 'PUBLISHED'),
('테스트 자동화 전략', '효과적인 테스트 자동화를 위한 전략과 도구들', 2, 'DRAFT');

-- 5. 저장 프로시저 생성
DELIMITER //
CREATE PROCEDURE GetArticlesByStatus(IN article_status VARCHAR(20))
BEGIN
    SELECT * FROM articles WHERE status = article_status ORDER BY created_at DESC;
END //
DELIMITER ;

-- 6. 뷰 생성
CREATE VIEW published_articles AS
SELECT id, title, author_id, created_at
FROM articles
WHERE status = 'PUBLISHED'
ORDER BY created_at DESC;
```

### 2. 다중 데이터베이스 설정

```java
@PrimaveraTestContainer(
    initScript = "sql/multi-database-init-local.sql"
)
class MultiDatabaseTest {
    
    @Autowired
    @Qualifier("primaryDataSource")
    private DataSource primaryDataSource;
    
    @TestConfiguration
    static class MultiDataSourceConfig {
        
        @Bean
        @Primary
        @Qualifier("primaryDataSource")
        public DataSource primaryDataSource(TestContainerService service) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(service.getJdbcUrl().replace("/primavera", "/primary_db"));
            config.setUsername(service.getUsername());
            config.setPassword(service.getPassword());
            return new HikariDataSource(config);
        }
        
        @Bean
        @Qualifier("secondaryDataSource")
        public DataSource secondaryDataSource(TestContainerService service) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(service.getJdbcUrl().replace("/primavera", "/secondary_db"));
            config.setUsername(service.getUsername());
            config.setPassword(service.getPassword());
            return new HikariDataSource(config);
        }
    }
}
```

### 3. 네트워크 설정 및 포트 매핑

```java
@PrimaveraTestContainer
class NetworkConfigurationTest {
    
    @Autowired
    private TestContainerService testContainerService;
    
    @Test
    void testNetworkConfiguration() {
        // 컨테이너 네트워크 정보
        String host = testContainerService.getHost();
        Integer mappedPort = testContainerService.getMappedPort();
        String jdbcUrl = testContainerService.getJdbcUrl();
        
        // 네트워크 연결 테스트
        assertThat(host).isEqualTo("localhost");
        assertThat(mappedPort).isBetween(32768, 65535);  // Dynamic port range
        assertThat(jdbcUrl).containsIgnoringCase("jdbc:mariadb://localhost:");
        
        // 실제 연결 테스트
        try (Connection connection = DriverManager.getConnection(
                jdbcUrl, 
                testContainerService.getUsername(), 
                testContainerService.getPassword())) {
            
            assertThat(connection.isValid(5)).isTrue();
        }
    }
}
```

### 4. 트랜잭션 및 롤백 테스트

```java
@PrimaveraTestContainer
@Transactional
class TransactionTest {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Test
    @Rollback(false)  // 트랜잭션 롤백 비활성화
    void testDataPersistence() {
        Article article = Article.builder()
            .title("Persistent Article")
            .content("This article should persist")
            .build();
        
        Article saved = articleRepository.save(article);
        assertThat(saved.getId()).isNotNull();
    }
    
    @Test
    @Rollback  // 기본값: true, 테스트 후 자동 롤백
    void testTemporaryData() {
        Article article = Article.builder()
            .title("Temporary Article")
            .content("This article will be rolled back")
            .build();
        
        articleRepository.save(article);
        
        // 다른 테스트에 영향 없음 (자동 롤백)
    }
}
```

---

## 🐛 문제 해결

### 1. 일반적인 문제들

#### Docker 관련 문제

**문제: Docker가 실행되지 않음**
```bash
# 에러 메시지
org.testcontainers.dockerclient.DockerClientProviderStrategy: Could not find a valid Docker environment

# 해결 방법
# 1. Docker Desktop 실행 확인
docker ps

# 2. Docker 서비스 상태 확인 (Linux)
sudo systemctl status docker

# 3. Docker 권한 확인
sudo usermod -aG docker $USER
newgrp docker
```

**문제: 포트 충돌**
```bash
# 에러 메시지
Caused by: java.net.BindException: Address already in use

# 해결 방법 - 사용 중인 포트 확인
netstat -tulpn | grep :3306
lsof -i :3306

# 충돌하는 프로세스 종료
sudo kill -9 [PID]
```

#### TestContainers 관련 문제

**문제: 컨테이너 이미지 다운로드 실패**
```java
// 해결 방법 1: 프록시 설정
@PrimaveraTestContainer(
    properties = {
        "testcontainers.docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy"
    }
)

// 해결 방법 2: 대체 이미지 사용
@PrimaveraTestContainer(
    mariadbVersion = "mariadb:11.4.7-jammy"  // 다른 태그 시도
)
```

**문제: 컨테이너 시작 시간 초과**
```yaml
# application-test.yml
primavera:
  testcontainers:
    mariadb:
      url-params:
        connectTimeout: 120000    # 2분
        socketTimeout: 120000     # 2분
        
logging:
  level:
    org.testcontainers: DEBUG
    com.github.dockerjava: DEBUG
```

### 2. 성능 문제 해결

#### 컨테이너 재사용 설정

```java
// ~/.testcontainers.properties 파일 생성
testcontainers.reuse.enable=true

// 또는 코드에서 설정
@PrimaveraTestContainer(
    properties = {
        "testcontainers.reuse.enable=true"
    }
)
class PerformanceOptimizedTest {
    // 테스트 코드
}
```

#### 메모리 최적화

```yaml
# application-test.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 3          # 테스트용 작은 풀
      minimum-idle: 1
      connection-timeout: 10000
      
  jpa:
    hibernate:
      ddl-auto: create-drop         # 빠른 스키마 생성
    properties:
      hibernate:
        jdbc:
          batch_size: 50            # 배치 처리 최적화
        order_inserts: true
        order_updates: true
```

### 3. 디버깅 및 로깅

#### 상세 로깅 설정

```yaml
# application-test.yml
logging:
  level:
    # TestContainers 관련
    org.testcontainers: DEBUG
    com.github.dockerjava: INFO
    
    # 커스텀 스타터 관련
    com.genius.primavera.testContainer: DEBUG
    
    # Spring 관련
    org.springframework.test: INFO
    org.springframework.boot.test: INFO
    
    # 데이터베이스 관련
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    
    # HikariCP 관련
    com.zaxxer.hikari: DEBUG
```

#### 디버깅용 테스트 클래스

```java
@PrimaveraTestContainer
class DebuggingTest {
    
    @Autowired
    private TestContainerService testContainerService;
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    void debugContainerInfo() {
        // 컨테이너 정보 출력
        System.out.println("=== Container Information ===");
        System.out.println("Running: " + testContainerService.isRunning());
        System.out.println("JDBC URL: " + testContainerService.getJdbcUrl());
        System.out.println("Host: " + testContainerService.getHost());
        System.out.println("Port: " + testContainerService.getMappedPort());
        System.out.println("Username: " + testContainerService.getUsername());
        System.out.println("Password: " + testContainerService.getPassword());
        
        // DataSource 정보 출력
        System.out.println("\n=== DataSource Information ===");
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDS = (HikariDataSource) dataSource;
            System.out.println("Pool Name: " + hikariDS.getPoolName());
            System.out.println("JDBC URL: " + hikariDS.getJdbcUrl());
            System.out.println("Max Pool Size: " + hikariDS.getMaximumPoolSize());
            System.out.println("Active Connections: " + hikariDS.getHikariPoolMXBean().getActiveConnections());
        }
    }
    
    @Test
    void debugDatabaseConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            System.out.println("=== Database Information ===");
            System.out.println("Database Product: " + metaData.getDatabaseProductName());
            System.out.println("Database Version: " + metaData.getDatabaseProductVersion());
            System.out.println("Driver Name: " + metaData.getDriverName());
            System.out.println("Driver Version: " + metaData.getDriverVersion());
            System.out.println("URL: " + metaData.getURL());
            System.out.println("Username: " + metaData.getUserName());
            
            // 테이블 목록 출력
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            System.out.println("\n=== Available Tables ===");
            while (tables.next()) {
                System.out.println("- " + tables.getString("TABLE_NAME"));
            }
        }
    }
}
```

---

## 📊 성능 최적화

### 1. 컨테이너 생성 시간 최적화

#### 이미지 캐싱 전략

```bash
# Docker 이미지 사전 다운로드
docker pull mariadb:11.4.7

# 불필요한 이미지 정리
docker image prune -f

# 레이어 캐시 최적화를 위한 .testcontainers.properties
echo "testcontainers.reuse.enable=true" >> ~/.testcontainers.properties
```

#### 빠른 시작을 위한 설정

```java
@PrimaveraTestContainer(
    properties = {
        "spring.jpa.hibernate.ddl-auto=none",  // DDL 생성 비활성화
        "spring.sql.init.mode=always"          // SQL 스크립트만 사용
    },
    initScript = "sql/minimal-schema.sql"      // 최소한의 스키마
)
class FastStartupTest {
    // 빠른 시작을 위한 최적화된 테스트
}
```

### 2. 메모리 사용량 최적화

#### JVM 힙 설정

```bash
# 테스트 실행 시 JVM 옵션
./gradlew test -Dorg.gradle.jvmargs="-Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

#### HikariCP 최적화

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5          # 테스트용 작은 풀
      minimum-idle: 1               # 최소 유휴 연결
      connection-timeout: 20000     # 연결 타임아웃
      idle-timeout: 300000          # 유휴 타임아웃
      max-lifetime: 1200000         # 최대 생명주기
      leak-detection-threshold: 60000  # 누수 감지
```

### 3. 병렬 테스트 실행

#### Gradle 병렬 실행 설정

```gradle
// build.gradle
test {
    useJUnitPlatform()
    
    // 병렬 실행 설정
    systemProperty 'junit.jupiter.execution.parallel.enabled', 'true'
    systemProperty 'junit.jupiter.execution.parallel.mode.default', 'concurrent'
    systemProperty 'junit.jupiter.execution.parallel.config.strategy', 'dynamic'
    
    // 테스트별 최대 병렬 실행 수
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
    
    // 메모리 설정
    minHeapSize = "512m"
    maxHeapSize = "2g"
}
```

#### 테스트 클래스 병렬 실행

```java
@PrimaveraTestContainer
@Execution(ExecutionMode.CONCURRENT)  // 병렬 실행 활성화
class ParallelExecutionTest {
    
    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void parallelTest1() {
        // 병렬로 실행되는 테스트 1
    }
    
    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void parallelTest2() {
        // 병렬로 실행되는 테스트 2
    }
}
```

### 4. CI/CD 환경 최적화

#### GitHub Actions 설정

```yaml
# .github/workflows/test.yml
name: Test with TestContainers

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        
    - name: Cache Docker images
      uses: actions/cache@v3
      with:
        path: /tmp/.docker-cache
        key: ${{ runner.os }}-docker-${{ hashFiles('**/Dockerfile') }}
        
    - name: Start Docker services
      run: |
        docker pull mariadb:11.4.7
        
    - name: Run tests
      run: |
        ./gradlew test --parallel --max-workers=4
        
    - name: Upload test results
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: test-results
        path: build/test-results/
```

### 5. 모니터링 및 메트릭

#### 테스트 성능 측정

```java
@PrimaveraTestContainer
class PerformanceMonitoringTest {
    
    @Test
    void measureContainerStartupTime() {
        long startTime = System.currentTimeMillis();
        
        // 컨테이너 시작 시간 측정
        TestContainerService service = testContainerService;
        assertThat(service.isRunning()).isTrue();
        
        long endTime = System.currentTimeMillis();
        long startupTime = endTime - startTime;
        
        System.out.printf("Container startup time: %d ms%n", startupTime);
        
        // 성능 기준 검증 (예: 30초 이내)
        assertThat(startupTime).isLessThan(30000);
    }
    
    @Test
    void measureDatabaseConnectionTime() {
        long startTime = System.nanoTime();
        
        try (Connection connection = dataSource.getConnection()) {
            connection.isValid(1);
        } catch (SQLException e) {
            fail("Database connection failed", e);
        }
        
        long endTime = System.nanoTime();
        long connectionTime = (endTime - startTime) / 1_000_000; // milliseconds
        
        System.out.printf("Database connection time: %d ms%n", connectionTime);
        
        // 연결 시간 기준 검증 (예: 1초 이내)
        assertThat(connectionTime).isLessThan(1000);
    }
}
```

---

## 🎓 결론

**spring-boot-starter-test-container**는 TestContainers의 복잡한 설정을 간소화하고, Spring Boot의 자동 설정 원칙을 따라 개발자가 테스트 코드 작성에만 집중할 수 있도록 도와주는 강력한 도구입니다.

### 💡 핵심 가치

1. **생산성 향상**: 99% 코드 감소로 개발 속도 향상
2. **일관성 보장**: 프로젝트 전체에서 통일된 테스트 환경
3. **유지보수성**: 중앙 집중식 설정으로 쉬운 관리
4. **확장성**: 다양한 설정 옵션으로 유연한 확장

### 🚀 시작해보세요!

```java
@PrimaveraTestContainer
class YourFirstTest {
    @Test
    void justStartTesting() {
        // 이제 테스트에만 집중하세요!
    }
}
```

---

## 📞 지원 및 기여

- **이슈 리포트**: [GitHub Issues](https://github.com/csj4032/primavera/issues)
- **기능 제안**: [GitHub Discussions](https://github.com/csj4032/primavera/discussions)
- **기여 가이드**: [CONTRIBUTING.md](https://github.com/csj4032/primavera/blob/master/CONTRIBUTING.md)

---

<div align="center">

**🌸 Primavera 프로젝트의 일부입니다 🌸**

[⭐ GitHub에서 스타 주기](https://github.com/csj4032/primavera) | [📖 전체 문서 보기](https://github.com/csj4032/primavera/wiki)

</div>