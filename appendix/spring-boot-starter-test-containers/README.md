# Spring Boot Starter Test-Containers

TestContainers를 Spring Boot 테스트 환경에서 쉽게 사용할 수 있도록 하는 커스텀 스타터입니다. 어노테이션 기반의 설정을 통해 다양한 데이터베이스와 미들웨어 컨테이너를 자동으로 관리하며, 초기화 스크립트 지원과 YAML 설정 통합을 제공합니다.

## 주요 기능

- **어노테이션 기반 컨테이너 관리**: `@EnableTestContainers`를 통한 선언적 컨테이너 설정
- **다중 컨테이너 지원**: MariaDB, PostgreSQL, Redis, Kafka 등 다양한 컨테이너 타입
- **YAML 설정 통합**: `application-test.yml`을 통한 세밀한 컨테이너 구성
- **자동 데이터소스 바인딩**: Spring의 `@Qualifier`를 통한 컨테이너별 데이터소스 자동 등록
- **초기화 스크립트 지원**: 데이터베이스 컨테이너의 스키마 및 데이터 초기화
- **라이프사이클 관리**: 테스트 클래스 단위의 컨테이너 시작/중지
- **병렬 컨테이너 시작**: CompletableFuture를 통한 성능 최적화
- **테스트 격리**: 테스트 클래스별 독립적인 컨테이너 생명주기

## 지원하는 컨테이너 타입

| 컨테이너 | Bean 타입 | 기본 이미지 | 기본 포트 |
|---------|-----------|-------------|-----------|
| MariaDB | `DataSource` | `mariadb:11.4.7` | 3306 |
| MySQL | `DataSource` | `mysql:8.0` | 3306 |
| PostgreSQL | `DataSource` | `postgres:16` | 5432 |
| Redis | `RedisTemplate<String, Object>` | `redis:7-alpine` | 6379 |
| Kafka | `KafkaTemplate<String, Object>` | `confluentinc/cp-kafka:latest` | 9092/9093 |
| MongoDB | `String` (연결 문자열) | `mongo:7` | 27017 |
| Elasticsearch | `Map<String, Object>` (설정) | `docker.elastic.co/elasticsearch/elasticsearch:8.11.0` | 9200 |

## 빠른 시작

### 1. Gradle 의존성 추가

```gradle
dependencies {
    testImplementation project(':appendix:spring-boot-starter-test-container-v4')
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
    void testDatabaseAndCache() {
        // 테스트 코드 작성
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
      database: "testdb"
      username: "test"
      password: "test"
      startup-timeout: 60
      init-script: ./sql/init.sql
      environment:
        MARIADB_CHARACTER_SET_SERVER: "utf8mb4"
        MARIADB_COLLATION_SERVER: "utf8mb4_unicode_ci"
    testCache:
      image: "redis:7-alpine"
      password: "test123"
      startup-timeout: 30
```

## 설정 파일 요구사항 및 제약사항

### 1. 설정 파일 우선순위

스타터는 다음 순서로 설정 파일을 탐색합니다:

```
1. application-test.yml (우선)
2. application-test.yaml 
3. application-test.properties (후순위)
```

**⚠️ 중요한 제약사항:**
- 최소 하나의 설정 파일이 **반드시 존재**해야 합니다 (`src/test/resources/` 하위)
- YAML 파일이 존재하면 properties 파일은 무시됩니다
- 설정 파일이 없으면 기본값으로 동작하지만, 컨테이너 세부 설정이 불가능합니다
- `testcontainers` 프로퍼티 prefix를 반드시 사용해야 합니다

### 2. YAML 설정 구조 및 규칙

```yaml
spring:
  config:
    activate:
      on-profile: test  # 필수: test 프로파일 활성화

testcontainers:  # 필수: 정확한 prefix 사용
  containers:    # 필수: containers 하위에 컨테이너 정의
    [컨테이너명]:  # 컨테이너명은 알파벳, 숫자, 언더스코어만 허용
      image: "[Docker 이미지]"              # 선택사항 - 기본값 사용 가능
      database: "[데이터베이스명]"          # 데이터베이스 컨테이너만 해당
      username: "[사용자명]"               # 기본값: "test"  
      password: "[비밀번호]"               # 기본값: "test"
      startup-timeout: [초단위]            # 기본값: 60초, kebab-case 필수
      init-script: "[초기화 스크립트 경로]" # 데이터베이스만 지원
      environment:                         # 환경변수 (선택사항)
        KEY: "value"
      network-aliases:                     # 네트워크 별칭 (선택사항)
        - "alias1"
        - "alias2"
```

### 3. Properties 설정 형식

```properties
# 기본 컨테이너 설정
testcontainers.containers.[컨테이너명].image=mariadb:11.4.7
testcontainers.containers.[컨테이너명].database=testdb
testcontainers.containers.[컨테이너명].username=testuser
testcontainers.containers.[컨테이너명].password=testpass
testcontainers.containers.[컨테이너명].startup-timeout=120
testcontainers.containers.[컨테이너명].init-script=./sql/init.sql

# 환경변수 설정
testcontainers.containers.[컨테이너명].environment.MYSQL_CHARSET=utf8mb4
testcontainers.containers.[컨테이너명].environment.MYSQL_COLLATION=utf8mb4_unicode_ci

# 네트워크 별칭 (Properties에서는 지원 제한)
testcontainers.containers.[컨테이너명].network-aliases[0]=alias1
testcontainers.containers.[컨테이너명].network-aliases[1]=alias2
```

### 4. 프로퍼티 네이밍 규칙

**⚠️ 중요한 제약사항:**
- YAML에서는 **kebab-case 필수**: `startup-timeout`, `init-script`, `network-aliases`
- Properties에서는 **camelCase 또는 kebab-case**: `startupTimeout` 또는 `startup-timeout`
- **잘못된 네이밍**: `startupTimeout` (YAML에서), `startup_timeout` (언더스코어 사용)

### 5. 초기화 스크립트 제약사항

**지원되는 경로 형식:**
| 형식 | 예시 | 설명 |
|------|------|------|
| 상대경로 | `./sql/init.sql` | `src/test/resources/sql/init.sql` |
| classpath 접두사 | `classpath:./sql/init.sql` | 클래스패스 기준 경로 |
| 직접 경로 | `sql/schema.sql` | `src/test/resources/sql/schema.sql` |

**⚠️ 초기화 스크립트 제약사항:**
- 초기화 스크립트는 **데이터베이스 컨테이너**에서만 지원 (MariaDB, PostgreSQL)
- 스크립트 파일은 **반드시** `src/test/resources/` 하위에 위치
- 스크립트는 컨테이너 시작 시 **한 번만** 실행
- 여러 SQL 문 포함 가능하지만, **트랜잭션은 스크립트 작성자가 관리**
- 스크립트 실행 실패 시 컨테이너 시작 실패

### 6. 네이밍 및 식별자 규칙

**컨테이너명 규칙:**
- **허용**: 알파벳, 숫자, 언더스코어 (`primaryDb`, `cache_store`, `db1`)
- **금지**: 하이픈, 특수문자, 공백 (`primary-db`, `cache@store`, `db 1`)
- **추천**: camelCase 사용 (`primaryDb`, `analyticsCache`)

**데이터소스 빈명:**
- 컨테이너명과 동일한 이름으로 자동 등록
- `@Qualifier("컨테이너명")`으로 주입

### 7. 환경변수 제약사항

**일반 규칙:**
- 모든 환경변수 값은 **문자열로 처리**
- YAML에서 특수문자 포함 시 **따옴표로 감싸야 함**
- Boolean/숫자 값도 문자열로 설정: `"true"`, `"123"`

**컨테이너별 지원 환경변수:**
```yaml
# MariaDB/MySQL
environment:
  MYSQL_CHARSET: "utf8mb4"
  MYSQL_COLLATION: "utf8mb4_unicode_ci"
  MYSQL_INNODB_BUFFER_POOL_SIZE: "256M"

# Redis  
environment:
  REDIS_MAXMEMORY: "128mb"
  REDIS_MAXMEMORY_POLICY: "allkeys-lru"

# PostgreSQL
environment:
  POSTGRES_INITDB_ARGS: "--encoding=UTF-8"
```

## 고급 사용법

### 다중 컨테이너 구성

```java
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primaryDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "secondaryDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "sessionCache"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "dataCache"),
    @EnableTestContainers.TestContainer(type = ContainerType.KAFKA, name = "eventBus")
})
class MultiContainerTest {
    
    @Autowired @Qualifier("primaryDb") private DataSource primaryDataSource;
    @Autowired @Qualifier("secondaryDb") private DataSource secondaryDataSource;
    @Autowired @Qualifier("sessionCache") private RedisTemplate<String, Object> sessionRedis;
    @Autowired @Qualifier("dataCache") private RedisTemplate<String, Object> dataRedis;
    @Autowired @Qualifier("eventBus") private KafkaTemplate<String, Object> kafkaTemplate;
}
```

### 상세 컨테이너 설정

```yaml
testcontainers:
  containers:
    advancedDb:
      image: "mariadb:11.4.7"
      database: "advanced_test"
      username: "advanced_user"
      password: "secure_password"
      startup-timeout: 120
      init-script: ./sql/advanced_init.sql
      environment:
        MARIADB_CHARACTER_SET_SERVER: "utf8mb4"
        MARIADB_COLLATION_SERVER: "utf8mb4_unicode_ci"
        MARIADB_INNODB_BUFFER_POOL_SIZE: "256M"
      networkAliases:
        - "database-server"
        - "db-primary"
    
    advancedCache:
      image: "redis:7-alpine"
      password: "redis_secure_password"
      startupTimeout: 60
      environment:
        REDIS_MAXMEMORY: "128mb"
        REDIS_MAXMEMORY_POLICY: "allkeys-lru"
      networkAliases:
        - "cache-server"
```

### 초기화 스크립트 사용법

#### 1. 기본 사용법

```yaml
testcontainers:
  containers:
    testDb:
      image: "mariadb:11.4.7"
      database: "testdb" 
      username: "testuser"
      password: "testpass"
      init-script: ./sql/init.sql  # 초기화 스크립트 지정
```

#### 2. 스크립트 파일 작성 예시

```sql
-- src/test/resources/sql/init.sql
CREATE TABLE IF NOT EXISTS USERS
(
    ID         BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL      VARCHAR(100) UNIQUE NOT NULL,
    PASSWORD   VARCHAR(255),
    NICKNAME   VARCHAR(50) NOT NULL,
    STATUS     INT DEFAULT 1,
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX IDX_USERS_EMAIL (EMAIL)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 기본 테스트 데이터 삽입
INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS) VALUES 
('admin@test.com', '{noop}admin', 'Administrator', 1),
('user@test.com', '{noop}user', 'TestUser', 1)
ON DUPLICATE KEY UPDATE EMAIL = VALUES(EMAIL);
```

#### 3. 테스트에서 초기화된 데이터 사용

```java
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "testDb")
})
class InitScriptTest {
    
    @Autowired
    @Qualifier("testDb")
    private DataSource dataSource;
    
    private JdbcTemplate jdbcTemplate;
    
    @BeforeEach
    void setup() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    @Test
    void testInitializedData() {
        // 초기화 스크립트로 생성된 사용자 수 확인
        Integer userCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(2, userCount);
        
        // 특정 사용자 존재 확인
        String email = jdbcTemplate.queryForObject(
            "SELECT EMAIL FROM USERS WHERE NICKNAME = ?", 
            String.class, "Administrator");
        assertEquals("admin@test.com", email);
    }
}
```

#### 4. 복잡한 스크립트 구조화

```
src/test/resources/
├── sql/
│   ├── schema/
│   │   ├── 01_users.sql
│   │   ├── 02_orders.sql
│   │   └── 03_products.sql
│   ├── data/
│   │   ├── test-users.sql
│   │   ├── test-orders.sql
│   │   └── test-products.sql
│   └── init.sql  # 메인 스크립트
```

```sql
-- src/test/resources/sql/init.sql
-- 스키마 생성
SOURCE schema/01_users.sql;
SOURCE schema/02_orders.sql;
SOURCE schema/03_products.sql;

-- 테스트 데이터 삽입
SOURCE data/test-users.sql;
SOURCE data/test-orders.sql;
SOURCE data/test-products.sql;
```

### 런타임 컨테이너 정보 접근

```java
@Test
void testContainerRuntimeInfo() {
    ContainerManager manager = ContainerRegistry.get();
    ContainerInfo dbInfo = manager.getContainer("testDb");
    
    String jdbcUrl = dbInfo.getJdbcUrl();
    String host = dbInfo.getHost();
    int port = dbInfo.getMappedPort();
    
    // 환경 변수로도 접근 가능
    String runtimeHost = environment.getProperty("testcontainer.runtime.testDb.host");
    String runtimePort = environment.getProperty("testcontainer.runtime.testDb.port");
}
```

### 병렬 테스트 실행

```java
// junit-platform.properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
```

```java
@Execution(ExecutionMode.CONCURRENT)
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "parallelDb")
})
class ParallelTest1 {
    // 테스트 1
}

@Execution(ExecutionMode.CONCURRENT)
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "parallelDb")
})
class ParallelTest2 {
    // 테스트 2 - 완전히 독립적인 컨테이너 사용
}
```

## 컨테이너별 상세 설정

### MariaDB / MySQL / PostgreSQL

```yaml
testcontainer:
  containers:
    myDb:
      image: "mariadb:11.4.7"
      database: "myapp"           # 기본: "test"
      username: "myuser"          # 기본: "test"
      password: "mypassword"      # 기본: "test"
      startupTimeout: 60          # 기본: 60초
```

**생성되는 Bean**: `DataSource` (HikariCP)

### Redis

```yaml
testcontainer:
  containers:
    myCache:
      image: "redis:7-alpine"
      password: "redis123"        # 선택사항, 설정 시 인증 활성화
      startupTimeout: 30          # 기본: 60초
```

**생성되는 Bean**: `RedisTemplate<String, Object>`

### Kafka

```yaml
testcontainer:
  containers:
    myKafka:
      image: "confluentinc/cp-kafka:latest"
      startupTimeout: 120         # 기본: 60초
```

**생성되는 Bean**: `KafkaTemplate<String, Object>`

### MongoDB

```yaml
testcontainer:
  containers:
    myMongo:
      image: "mongo:7"
      startupTimeout: 60          # 기본: 60초
```

**생성되는 Bean**: `String` (MongoDB 연결 문자열)

### Elasticsearch

```yaml
testcontainer:
  containers:
    myElastic:
      image: "docker.elastic.co/elasticsearch/elasticsearch:8.11.0"
      startupTimeout: 120         # 기본: 60초
      environment:
        discovery.type: "single-node"
        xpack.security.enabled: "false"
```

**생성되는 Bean**: `Map<String, Object>` (연결 설정)

## 아키텍처

### 핵심 컴포넌트

```
┌─────────────────────────────────────────────────┐
│                @EnableTestContainers            │
├─────────────────────────────────────────────────┤
│              TestContainerExtension             │
│         (JUnit 5 Extension 구현)                │
├─────────────────────────────────────────────────┤
│           TestContainerContextInitializer       │
│        (ApplicationContext 초기화)               │
├─────────────────────────────────────────────────┤
│              ContainerManager                   │
│           (컨테이너 생명주기 관리)                 │
├─────────────────────────────────────────────────┤
│    ContainerFactory    │    ContainerBeanRegistrar │
│   (컨테이너 생성)       │      (Spring Bean 등록)    │
├─────────────────────────────────────────────────┤
│  Factory Package       │     Bean Package        │
│  - MariaDBFactory      │  - DataSourceCreator    │
│  - RedisFactory        │  - RedisCreator         │
│  - KafkaFactory        │  - KafkaCreator         │
└─────────────────────────────────────────────────┘
```

### 디자인 패턴

- **Factory Method Pattern**: 컨테이너 타입별 생성 로직 분리
- **Registry Pattern**: 팩토리와 Bean 생성자 등록 및 관리
- **Strategy Pattern**: 컨테이너 타입에 따른 다른 생성 전략
- **Template Method**: 공통 컨테이너 설정 로직 재사용

## 제약 사항 및 한계

### 기술적 제약사항

#### 1. JUnit 5 전용
- **제약**: JUnit 4 지원 불가
- **이유**: JUnit 5 Extension API 사용
- **해결방안**: JUnit 5로 마이그레이션 필요

#### 2. Spring Boot 3.x 이상 필수
- **제약**: Spring Boot 2.x 지원 불가
- **이유**: Spring Framework 6.x 기능 사용
- **해결방안**: Spring Boot 3.x로 업그레이드 필요

#### 3. Docker 환경 의존
- **제약**: Docker가 설치되지 않은 환경에서 실행 불가
- **이유**: TestContainers가 Docker 기반
- **해결방안**: CI/CD 환경에 Docker 설치 필요

#### 4. 컨테이너 생성 시간
- **제약**: 첫 번째 테스트 실행 시 이미지 다운로드로 인한 지연
- **영향**: 초기 테스트 실행 시간 증가 (이미지당 1-5분)
- **해결방안**: CI 환경에서 이미지 사전 캐싱

### 메모리 및 리소스 제약

#### 1. 메모리 사용량
- **최소 요구사항**: 4GB RAM
- **권장 사양**: 8GB 이상
- **이유**: 각 컨테이너당 평균 100-500MB 메모리 사용

#### 2. 포트 충돌
- **제약**: 동일 포트를 사용하는 로컬 서비스와 충돌 가능
- **해결**: TestContainers가 자동으로 사용 가능한 포트 할당

#### 3. 디스크 공간
- **요구사항**: 이미지당 500MB-2GB
- **권장**: 최소 10GB 여유 공간

### 플랫폼 제약사항

#### 1. macOS ARM64 (M1/M2) 호환성
- **제약**: 일부 이미지가 ARM64를 지원하지 않을 수 있음
- **해결방안**: `--platform linux/amd64` 플래그 사용

```yaml
testcontainer:
  containers:
    myDb:
      image: "mariadb:11.4.7"
      environment:
        DOCKER_DEFAULT_PLATFORM: "linux/amd64"
```

#### 2. Windows 지원
- **제약**: Docker Desktop for Windows 필요
- **주의사항**: WSL2 백엔드 사용 권장

### 테스트 격리 한계

#### 1. 클래스 레벨 격리만 지원
- **제약**: 메서드 레벨 컨테이너 격리 불가
- **이유**: 성능상의 이유로 클래스 레벨로 제한
- **영향**: 동일 클래스 내 테스트 메서드는 컨테이너 공유

#### 2. 트랜잭션 롤백 미지원
- **제약**: `@Transactional` + `@Rollback` 자동 지원 안함
- **해결방안**: 수동으로 데이터 정리 또는 별도 정리 메서드 구현

```java
@AfterEach
void cleanUp() {
    jdbcTemplate.execute("TRUNCATE TABLE test_table");
}
```

### 설정 제약사항

#### 1. 동적 설정 변경 불가
- **제약**: 테스트 실행 중 컨테이너 설정 변경 불가
- **이유**: 컨테이너 생명주기가 클래스 레벨로 고정
- **해결방안**: 설정별로 별도 테스트 클래스 작성

#### 2. 네트워크 격리
- **제약**: 서로 다른 테스트 클래스의 컨테이너 간 통신 불가
- **이유**: 각 테스트별 독립적인 Docker 네트워크 사용
- **해결방안**: 통신이 필요한 컨테이너는 동일 테스트 클래스에 선언

### 성능 제약사항

#### 1. 병렬 실행 시 리소스 경합
- **제약**: 다수의 병렬 테스트 실행 시 시스템 리소스 부족 가능
- **권장**: 동시 실행 테스트 수를 시스템 사양에 맞게 조절

```properties
# junit-platform.properties
junit.jupiter.execution.parallel.config.dynamic.factor=0.5
```

#### 2. 컨테이너 시작 시간
- **평균 시작 시간**:
  - MariaDB/MySQL: 10-15초
  - PostgreSQL: 8-12초
  - Redis: 2-5초
  - Kafka: 20-30초
  - MongoDB: 8-15초
  - Elasticsearch: 30-60초

### CI/CD 환경 고려사항

#### 1. 빌드 시간 증가
- **영향**: 통합 테스트로 인한 빌드 시간 2-5배 증가
- **해결방안**: 단위 테스트와 통합 테스트 분리 실행

#### 2. 리소스 할당
- **권장 CI 환경 사양**:
  - CPU: 4 코어 이상
  - RAM: 8GB 이상
  - 디스크: 50GB 이상

## 모범 사례

### 1. 테스트 분리
```java
// ✅ 좋은 예: 기능별 테스트 클래스 분리
@EnableTestContainers({@EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "userDb")})
class UserServiceTest { }

@EnableTestContainers({@EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "sessionCache")})
class SessionServiceTest { }
```

### 2. 리소스 정리
```java
@AfterEach
void cleanUp() {
    // 테스트 데이터 정리
    testDataCleanupService.cleanAll();
}
```

### 3. 설정 외부화
```java
// ✅ application-test.yml 사용
// ❌ 코드에서 직접 설정하지 말 것
```

### 4. 적절한 라이프사이클 선택
```java
// 빠른 테스트가 필요한 경우
@TestInstance(TestInstance.Lifecycle.PER_METHOD)

// 성능이 중요한 경우 (권장)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
```

### 5. 초기화 스크립트 베스트 프랙티스

```sql
-- ✅ 좋은 예: 멱등성 보장
CREATE TABLE IF NOT EXISTS USERS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO USERS (EMAIL, NICKNAME) VALUES 
('admin@test.com', 'Admin')
ON DUPLICATE KEY UPDATE NICKNAME = VALUES(NICKNAME);

-- ❌ 나쁜 예: 멱등성 없음
CREATE TABLE USERS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    EMAIL VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO USERS VALUES (1, 'admin@test.com', 'Admin');
```

```yaml
# ✅ 좋은 예: 명확한 경로 지정
testcontainers:
  containers:
    testDb:
      init-script: ./sql/init.sql

# ❌ 나쁜 예: 절대 경로 또는 잘못된 형식
testcontainers:
  containers:
    testDb:
      init-script: /absolute/path/init.sql  # 절대 경로 사용 금지
      init-script: init.sql                # ./ 접두사 없음
```

## 문제 해결

### 1. 설정 파일 관련 오류

#### BindException 발생
```
org.springframework.boot.context.properties.bind.BindException: 
Failed to bind properties under 'testcontainers'
```

**원인 및 해결방법:**
- `application-test.yml` 파일이 올바른 위치(`src/test/resources/`)에 있는지 확인
- YAML 문법 확인 (들여쓰기, 콜론 뒤 공백 등)
- 프로퍼티명이 kebab-case인지 확인 (`startup-timeout`, `init-script`)
- `testcontainers` prefix가 정확한지 확인

#### 컨버터 오류
```
No converter found capable of converting from type [java.lang.String] to type [...ContainerConfiguration]
```

**해결방법:**
- YAML 파일에서 ResourcePropertySource 대신 YamlPropertySourceLoader 사용 확인
- 프로퍼티 바인딩을 위한 @ConfigurationProperties 어노테이션 확인

### 2. 초기화 스크립트 관련 오류

#### 스크립트 파일을 찾을 수 없음
```
Resource not found: sql/init.sql
```

**해결방법:**
- 스크립트 파일이 `src/test/resources/` 하위에 있는지 확인
- 경로가 올바른지 확인 (`./sql/init.sql` 또는 `sql/init.sql`)
- 파일 확장자가 `.sql`인지 확인

#### SQL 실행 오류
```
SQL syntax error in init script
```

**해결방법:**
- SQL 문법이 해당 데이터베이스 타입에 맞는지 확인
- 문자 인코딩 문제인지 확인 (UTF-8 권장)
- 테이블/데이터베이스가 이미 존재하는 경우 `IF NOT EXISTS` 사용

### 3. 컨테이너 시작 관련 오류

#### 컨테이너 시작 실패
```
org.testcontainers.containers.ContainerLaunchException: Container startup failed
```
**해결방안**:
- Docker 데몬 실행 확인
- 이미지 이름 확인
- startup-timeout 증가 (기본값: 60초)

#### 2. 포트 충돌
```
Caused by: java.net.BindException: Address already in use
```
**해결방안**: TestContainers가 자동 포트 할당하므로 고정 포트 사용 중단

#### 3. 메모리 부족
```
java.lang.OutOfMemoryError: Docker container failed to start
```
**해결방안**:
- Docker 메모리 할당량 증가
- 동시 실행 테스트 수 감소

## 로드맵

### v4.1 (계획 중)
- [ ] Spring Boot 3.4 지원
- [ ] TestContainers 1.22 지원
- [ ] 메서드 레벨 컨테이너 격리 (실험적)

### v4.2 (검토 중)
- [ ] Podman 지원
- [ ] 컨테이너 헬스체크 강화
- [ ] 자원 사용량 모니터링

### v5.0 (장기)
- [ ] Cloud 네이티브 테스트 지원
- [ ] Kubernetes 기반 테스트 환경
- [ ] 분산 테스트 지원

## 라이선스

이 프로젝트는 Primavera 교육용 프로젝트의 일부입니다.

---

**📝 참고**: 이 문서는 spring-boot-starter-test-container-v4의 상세 가이드입니다. 추가 질문이나 이슈가 있다면 프로젝트 이슈 트래커를 이용해 주세요.