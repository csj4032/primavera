# Spring Boot Starter TestContainers

TestContainers를 Spring Boot 테스트 환경에서 선언적으로 쉽게 사용할 수 있도록 하는 커스텀 스타터입니다. 어노테이션 기반 설정을 통해 다양한 데이터베이스와 미들웨어 컨테이너를 자동으로 관리하며, YAML 설정 통합과 초기화 스크립트를 지원합니다.

## 학습 목표

- **커스텀 스타터 제작**: TestContainers 통합을 위한 Spring Boot 스타터 개발
- **JUnit 5 Extension**: 테스트 라이프사이클과 컨테이너 생명주기 통합
- **다중 컨테이너 관리**: 여러 종류의 컨테이너를 하나의 테스트에서 관리
- **자동 설정 바인딩**: YAML 설정과 Spring Bean 자동 생성 연동

## 배경 및 필요성

### TestContainers 설정의 복잡성
기존 TestContainers 사용 시 다음과 같은 문제들이 있었습니다:
- **반복적인 컨테이너 설정**: 각 테스트마다 컨테이너 생성 코드 중복
- **설정 관리의 어려움**: 하드코딩된 컨테이너 설정으로 인한 유지보수성 저하
- **다중 컨테이너 관리**: 여러 컨테이너의 생명주기 관리 복잡성
- **Spring 통합 부족**: TestContainers와 Spring Boot의 자연스러운 통합 부족

### 해결 방안
- **어노테이션 기반 설정**: `@EnableTestContainers`를 통한 선언적 컨테이너 관리
- **YAML 설정 통합**: `application-test.yml`을 통한 중앙화된 설정 관리
- **자동 Bean 등록**: 컨테이너별 적절한 Spring Bean 자동 생성
- **JUnit 5 Extension**: 테스트 라이프사이클과의 완벽한 통합

## 프로젝트 구조

```
src/main/java/com/genius/primavera/testcontainers/
├── annotation/                           # 어노테이션 정의
│   └── EnableTestContainers.java         # 메인 활성화 어노테이션
├── config/                              # 설정 관리
│   ├── TestContainerProperties.java     # 설정 바인딩 클래스
│   └── ContainerConfiguration.java      # 개별 컨테이너 설정
├── extension/                           # JUnit 5 Extension
│   └── TestContainerExtension.java      # 테스트 라이프사이클 관리
├── initializer/                         # Spring Context 초기화
│   └── TestContainerContextInitializer.java # ApplicationContext 초기화
├── manager/                             # 컨테이너 관리
│   ├── ContainerManager.java           # 컨테이너 생명주기 관리
│   ├── ContainerRegistry.java          # 컨테이너 등록 및 조회
│   └── ContainerInfo.java              # 컨테이너 런타임 정보
├── factory/                             # 컨테이너 생성 팩토리
│   ├── ContainerFactory.java           # 팩토리 인터페이스
│   ├── MariaDBContainerFactory.java    # MariaDB 컨테이너 팩토리
│   ├── PostgreSQLContainerFactory.java  # PostgreSQL 컨테이너 팩토리
│   ├── RedisContainerFactory.java      # Redis 컨테이너 팩토리
│   ├── KafkaContainerFactory.java      # Kafka 컨테이너 팩토리
│   ├── MongoDBContainerFactory.java    # MongoDB 컨테이너 팩토리
│   └── ElasticsearchContainerFactory.java # Elasticsearch 팩토리
├── bean/                                # Spring Bean 생성
│   ├── ContainerBeanRegistrar.java     # Bean 등록 관리자
│   ├── DataSourceCreator.java          # DataSource Bean 생성
│   ├── RedisCreator.java               # Redis Bean 생성
│   ├── KafkaCreator.java               # Kafka Bean 생성
│   └── MongoCreator.java               # MongoDB Bean 생성
└── enums/                               # 열거형 정의
    └── ContainerType.java               # 지원하는 컨테이너 타입

src/main/resources/
├── META-INF/spring.factories             # Auto Configuration 등록
└── testcontainer-defaults.yml           # 기본 컨테이너 설정
```

## 주요 기능

### 1. @EnableTestContainers 어노테이션
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TestContainerExtension.class)
@ContextConfiguration(initializers = TestContainerContextInitializer.class)
public @interface EnableTestContainers {
    
    /**
     * 활성화할 컨테이너 목록
     */
    TestContainer[] value();
    
    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestContainer {
        /**
         * 컨테이너 타입
         */
        ContainerType type();
        
        /**
         * 컨테이너 이름 (Spring Bean 이름으로 사용)
         */
        String name();
        
        /**
         * 컨테이너 시작 타임아웃 (초)
         */
        int startupTimeout() default 60;
        
        /**
         * 초기화 스크립트 경로 (데이터베이스 컨테이너만 해당)
         */
        String initScript() default "";
    }
}
```

### 2. TestContainerExtension (JUnit 5)
```java
public class TestContainerExtension implements BeforeAllCallback, AfterAllCallback, 
                                              BeforeEachCallback, AfterEachCallback {
    
    private final ContainerManager containerManager = new ContainerManager();
    private final ContainerBeanRegistrar beanRegistrar = new ContainerBeanRegistrar();
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        EnableTestContainers annotation = testClass.getAnnotation(EnableTestContainers.class);
        
        if (annotation == null) return;
        
        log.info("TestContainer Extension 시작 - 테스트 클래스: {}", testClass.getSimpleName());
        
        // 병렬로 컨테이너 시작
        List<CompletableFuture<Void>> futures = Arrays.stream(annotation.value())
            .map(this::startContainerAsync)
            .collect(Collectors.toList());
        
        // 모든 컨테이너 시작 완료까지 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .get(300, TimeUnit.SECONDS);
            
        log.info("모든 TestContainer 시작 완료 - 총 {}개", annotation.value().length);
    }
    
    private CompletableFuture<Void> startContainerAsync(EnableTestContainers.TestContainer config) {
        return CompletableFuture.runAsync(() -> {
            try {
                GenericContainer<?> container = createContainer(config);
                container.start();
                
                containerManager.register(config.name(), container, config.type());
                
                log.info("Container 시작 완료 - 이름: {}, 타입: {}, 포트: {}", 
                    config.name(), config.type(), container.getMappedPort(getDefaultPort(config.type())));
                    
            } catch (Exception e) {
                log.error("Container 시작 실패 - 이름: {}, 타입: {}", config.name(), config.type(), e);
                throw new RuntimeException("Container 시작 실패", e);
            }
        });
    }
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        containerManager.stopAll();
        log.info("모든 TestContainer 종료 완료");
    }
    
    private GenericContainer<?> createContainer(EnableTestContainers.TestContainer config) {
        ContainerFactory factory = getFactory(config.type());
        ContainerConfiguration configuration = loadConfiguration(config);
        
        return factory.create(configuration);
    }
}
```

### 3. 컨테이너 팩토리 패턴
```java
public interface ContainerFactory {
    GenericContainer<?> create(ContainerConfiguration config);
    ContainerType getType();
}

@Component
public class MariaDBContainerFactory implements ContainerFactory {
    
    @Override
    public GenericContainer<?> create(ContainerConfiguration config) {
        MariaDBContainer<?> container = new MariaDBContainer<>(config.getImage())
            .withDatabaseName(config.getDatabase())
            .withUsername(config.getUsername())
            .withPassword(config.getPassword())
            .withStartupTimeout(Duration.ofSeconds(config.getStartupTimeout()));
            
        // 환경 변수 설정
        config.getEnvironment().forEach(container::withEnv);
        
        // 네트워크 별칭 설정
        config.getNetworkAliases().forEach(container::withNetworkAliases);
        
        // 초기화 스크립트 설정
        if (config.getInitScript() != null && !config.getInitScript().isEmpty()) {
            String scriptPath = resolveScriptPath(config.getInitScript());
            container.withInitScript(scriptPath);
            log.info("초기화 스크립트 설정: {}", scriptPath);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getType() {
        return ContainerType.MARIADB;
    }
    
    private String resolveScriptPath(String initScript) {
        // classpath: 접두사 제거
        if (initScript.startsWith("classpath:")) {
            return initScript.substring("classpath:".length());
        }
        
        // ./ 접두사 제거
        if (initScript.startsWith("./")) {
            return initScript.substring(2);
        }
        
        return initScript;
    }
}
```

### 4. Spring Bean 자동 생성
```java
@Component
public class DataSourceCreator implements ContainerBeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo, ContainerConfiguration config) {
        if (!isDatabaseContainer(containerInfo.getType())) {
            return null;
        }
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(containerInfo.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(getDriverClassName(containerInfo.getType()));
        
        // HikariCP 최적화 설정
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        
        return new HikariDataSource(hikariConfig);
    }
    
    @Override
    public Class<?> getBeanType() {
        return DataSource.class;
    }
    
    @Override
    public boolean supports(ContainerType type) {
        return type == ContainerType.MARIADB || 
               type == ContainerType.MYSQL || 
               type == ContainerType.POSTGRESQL;
    }
}

@Component
public class RedisCreator implements ContainerBeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo, ContainerConfiguration config) {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
            containerInfo.getHost(), containerInfo.getMappedPort());
            
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            connectionFactory.setPassword(config.getPassword());
        }
        
        connectionFactory.afterPropertiesSet();
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        
        return template;
    }
    
    @Override
    public Class<?> getBeanType() {
        return RedisTemplate.class;
    }
    
    @Override
    public boolean supports(ContainerType type) {
        return type == ContainerType.REDIS;
    }
}
```

### 5. YAML 설정 통합
```java
@ConfigurationProperties(prefix = "testcontainers")
@Data
public class TestContainerProperties {
    
    /**
     * 컨테이너별 설정 맵
     */
    private Map<String, ContainerConfiguration> containers = new HashMap<>();
    
    @Data
    public static class ContainerConfiguration {
        
        /**
         * Docker 이미지 이름
         */
        private String image;
        
        /**
         * 데이터베이스명 (데이터베이스 컨테이너만 해당)
         */
        private String database = "test";
        
        /**
         * 사용자명
         */
        private String username = "test";
        
        /**
         * 비밀번호
         */
        private String password = "test";
        
        /**
         * 시작 타임아웃 (초)
         */
        @JsonProperty("startup-timeout")
        private int startupTimeout = 60;
        
        /**
         * 초기화 스크립트 경로
         */
        @JsonProperty("init-script")
        private String initScript;
        
        /**
         * 환경 변수
         */
        private Map<String, String> environment = new HashMap<>();
        
        /**
         * 네트워크 별칭
         */
        @JsonProperty("network-aliases")
        private List<String> networkAliases = new ArrayList<>();
    }
}
```

## 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| **Spring Boot** | 3.3.6 | 기본 프레임워크 |
| **TestContainers** | 1.21.3 | 컨테이너 기반 테스트 |
| **JUnit 5** | 5.10+ | 테스트 프레임워크 |
| **Spring Test** | 3.3.6 | Spring 테스트 통합 |
| **Docker** | 필수 | 컨테이너 런타임 |
| **HikariCP** | 5.1.0 | 데이터베이스 커넥션 풀 |
| **Lettuce** | 포함 | Redis 클라이언트 |

## 사용 방법

### 1. 의존성 추가
```gradle
dependencies {
    testImplementation project(':appendix:spring-boot-starter-test-containers')
}
```

### 2. 기본 사용법
```java
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "testDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "testCache")
})
class MyIntegrationTest {
    
    @Autowired
    @Qualifier("testDb")
    private DataSource dataSource;
    
    @Autowired
    @Qualifier("testCache")
    private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(1)).isTrue();
        }
    }
    
    @Test
    void testRedisConnection() {
        redisTemplate.opsForValue().set("test-key", "test-value");
        String value = (String) redisTemplate.opsForValue().get("test-key");
        assertThat(value).isEqualTo("test-value");
    }
}
```

### 3. application-test.yml 설정
```yaml
spring:
  config:
    activate:
      on-profile: test

testcontainers:
  containers:
    testDb:
      image: "mariadb:11.4.7"
      database: "integration_test"
      username: "test_user"
      password: "test_pass"
      startup-timeout: 120
      init-script: ./sql/integration-test-schema.sql
      environment:
        MARIADB_CHARACTER_SET_SERVER: "utf8mb4"
        MARIADB_COLLATION_SERVER: "utf8mb4_unicode_ci"
        MARIADB_INNODB_BUFFER_POOL_SIZE: "256M"
      network-aliases:
        - "test-database"
        - "integration-db"
    
    testCache:
      image: "redis:7-alpine"
      password: "redis_test_password"
      startup-timeout: 30
      environment:
        REDIS_MAXMEMORY: "128mb"
        REDIS_MAXMEMORY_POLICY: "allkeys-lru"
      network-aliases:
        - "test-redis"
```

### 4. 초기화 스크립트 사용
```sql
-- src/test/resources/sql/integration-test-schema.sql
CREATE TABLE IF NOT EXISTS USERS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD VARCHAR(255) NOT NULL,
    NICKNAME VARCHAR(50) NOT NULL,
    STATUS INT DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX IDX_USERS_EMAIL (EMAIL),
    INDEX IDX_USERS_STATUS (STATUS)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 테스트용 기본 데이터
INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES 
('admin@test.com', '{noop}admin123', 'Administrator', 1),
('user@test.com', '{noop}user123', 'TestUser', 1),
('inactive@test.com', '{noop}inactive123', 'InactiveUser', 0)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);

CREATE TABLE IF NOT EXISTS POSTS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID BIGINT NOT NULL,
    TITLE VARCHAR(200) NOT NULL,
    CONTENT TEXT,
    VIEW_COUNT INT DEFAULT 0,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (USER_ID) REFERENCES USERS(ID) ON DELETE CASCADE,
    INDEX IDX_POSTS_USER_ID (USER_ID),
    INDEX IDX_POSTS_CREATED_AT (CREATED_AT)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
```

### 5. 다중 컨테이너 고급 설정
```java
@SpringBootTest
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primaryDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.POSTGRESQL, name = "analyticsDb"),  
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "sessionCache"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "dataCache"),
    @EnableTestContainers.TestContainer(type = ContainerType.KAFKA, name = "eventBus"),
    @EnableTestContainers.TestContainer(type = ContainerType.MONGODB, name = "documentStore"),
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "searchEngine")
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComplexIntegrationTest {
    
    @Autowired @Qualifier("primaryDb") 
    private DataSource primaryDataSource;
    
    @Autowired @Qualifier("analyticsDb") 
    private DataSource analyticsDataSource;
    
    @Autowired @Qualifier("sessionCache") 
    private RedisTemplate<String, Object> sessionRedis;
    
    @Autowired @Qualifier("dataCache") 
    private RedisTemplate<String, Object> dataRedis;
    
    @Autowired @Qualifier("eventBus") 
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired @Qualifier("documentStore") 
    private String mongoConnectionString;
    
    @Autowired @Qualifier("searchEngine") 
    private Map<String, Object> elasticsearchConfig;
    
    @Test
    @Order(1)
    @DisplayName("모든 컨테이너 연결 상태 확인")
    void shouldConnectToAllContainers() {
        // 데이터베이스 연결 확인
        assertThat(primaryDataSource).isNotNull();
        assertThat(analyticsDataSource).isNotNull();
        
        // Redis 연결 확인
        assertThat(sessionRedis).isNotNull();
        assertThat(dataRedis).isNotNull();
        
        // Kafka 연결 확인
        assertThat(kafkaTemplate).isNotNull();
        
        // MongoDB 연결 정보 확인
        assertThat(mongoConnectionString).startsWith("mongodb://");
        
        // Elasticsearch 설정 확인
        assertThat(elasticsearchConfig).containsKey("host");
        assertThat(elasticsearchConfig).containsKey("port");
    }
    
    @Test
    @Order(2) 
    @DisplayName("데이터 파이프라인 통합 테스트")
    void shouldProcessCompleteDataPipeline() throws Exception {
        // 1. 메인 데이터베이스에 데이터 삽입
        try (Connection conn = primaryDataSource.getConnection()) {
            conn.createStatement().execute(
                "INSERT INTO USERS (EMAIL, NICKNAME) VALUES ('pipeline@test.com', 'PipelineTest')");
        }
        
        // 2. Redis 세션에 사용자 정보 캐싱
        sessionRedis.opsForValue().set("user:pipeline", Map.of(
            "email", "pipeline@test.com",
            "nickname", "PipelineTest"
        ));
        
        // 3. Kafka로 이벤트 발행
        kafkaTemplate.send("user.created", Map.of(
            "userId", "pipeline",
            "event", "USER_CREATED"
        ));
        
        // 4. MongoDB에 문서 저장 (MongoTemplate 사용)
        // 실제로는 mongoConnectionString을 사용해 MongoClient 생성 후 사용
        
        // 5. 모든 데이터가 정상적으로 처리되었는지 검증
        Object cachedUser = sessionRedis.opsForValue().get("user:pipeline");
        assertThat(cachedUser).isNotNull();
    }
}
```

## 지원하는 컨테이너 타입

| 컨테이너 | 기본 이미지 | 생성되는 Bean 타입 | 기본 포트 |
|----------|-------------|-------------------|----------|
| **MARIADB** | `mariadb:11.4.7` | `DataSource` | 3306 |
| **MYSQL** | `mysql:8.0` | `DataSource` | 3306 |
| **POSTGRESQL** | `postgres:16` | `DataSource` | 5432 |
| **REDIS** | `redis:7-alpine` | `RedisTemplate<String, Object>` | 6379 |
| **KAFKA** | `confluentinc/cp-kafka:latest` | `KafkaTemplate<String, Object>` | 9092 |
| **MONGODB** | `mongo:7` | `String` (연결 문자열) | 27017 |
| **ELASTICSEARCH** | `docker.elastic.co/elasticsearch/elasticsearch:8.11.0` | `Map<String, Object>` (설정) | 9200 |

## 핵심 학습 포인트

### 1. 커스텀 스타터 아키텍처
- **Factory Pattern**: 컨테이너 타입별 생성 로직 분리
- **Registry Pattern**: 컨테이너 인스턴스 중앙 관리
- **Extension Point**: JUnit 5 Extension API 활용
- **Configuration Properties**: 타입 안전한 YAML 설정 바인딩

### 2. TestContainers 통합 패턴
- **Lifecycle Management**: 테스트 클래스 단위 컨테이너 생명주기
- **Parallel Startup**: CompletableFuture를 통한 병렬 컨테이너 시작
- **Resource Cleanup**: 테스트 종료 시 자동 리소스 정리
- **Network Isolation**: 테스트 간 네트워크 격리

### 3. Spring Boot 통합
- **Bean Auto-Registration**: 컨테이너별 적절한 Spring Bean 자동 등록
- **Qualifier Support**: @Qualifier를 통한 다중 인스턴스 구분
- **Profile Integration**: test 프로파일과의 자연스러운 통합
- **Property Binding**: Spring Boot의 @ConfigurationProperties 활용

### 4. 테스트 최적화
- **Container Reuse**: 같은 설정의 컨테이너 재사용으로 성능 향상
- **Init Script Support**: 데이터베이스 스키마 및 데이터 자동 초기화
- **Health Check**: 컨테이너 준비 상태 자동 확인
- **Error Handling**: 컨테이너 시작 실패 시 명확한 오류 메시지

## 테스트 실행

### 단위 테스트
```bash
# 전체 테스트 실행
./gradlew test

# 특정 통합 테스트 실행
./gradlew test --tests "*IntegrationTest"

# 병렬 테스트 실행
./gradlew test -Djunit.jupiter.execution.parallel.enabled=true
```

### 성능 테스트
```bash
# 컨테이너 시작 시간 측정
./gradlew test --tests "*ContainerStartupPerformanceTest"

# 메모리 사용량 모니터링
./gradlew test -Djvm.args="-XX:+PrintGCDetails -XX:+PrintMemoryUsage"
```

## 설정 최적화

### 1. 컨테이너별 최적화
```yaml
testcontainers:
  containers:
    # 고성능 데이터베이스 설정
    performanceDb:
      image: "mariadb:11.4.7"
      startup-timeout: 180
      environment:
        MARIADB_INNODB_BUFFER_POOL_SIZE: "512M"
        MARIADB_INNODB_LOG_FILE_SIZE: "256M"
        MARIADB_INNODB_FLUSH_LOG_AT_TRX_COMMIT: "2"
    
    # 제한된 리소스 환경
    lightweightCache:
      image: "redis:7-alpine"
      startup-timeout: 15
      environment:
        REDIS_MAXMEMORY: "64mb"
        REDIS_MAXMEMORY_POLICY: "allkeys-lru"
```

### 2. CI/CD 환경 최적화
```yaml
# application-ci.yml
testcontainers:
  containers:
    ciDb:
      image: "mariadb:11.4.7"
      startup-timeout: 300  # CI 환경에서 더 긴 타임아웃
      environment:
        MARIADB_INNODB_BUFFER_POOL_SIZE: "128M"  # 메모리 제한
```

## 활용 방법

### 1. 마이크로서비스 테스트
```java
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "userDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "sessionStore"),
    @EnableTestContainers.TestContainer(type = ContainerType.KAFKA, name = "eventStream")
})
class MicroserviceIntegrationTest {
    // 마이크로서비스 간 통신 테스트
}
```

### 2. 데이터 파이프라인 테스트
```java
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.KAFKA, name = "inputTopic"),
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "searchIndex"),
    @EnableTestContainers.TestContainer(type = ContainerType.MONGODB, name = "dataLake")
})
class DataPipelineIntegrationTest {
    // ETL 파이프라인 통합 테스트
}
```

이 커스텀 스타터는 TestContainers의 강력함과 Spring Boot의 편의성을 결합하여, 복잡한 통합 테스트를 간단하고 선언적으로 작성할 수 있게 해줍니다.