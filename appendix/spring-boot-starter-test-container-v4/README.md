# Spring Boot Starter TestContainer V4

TestContainers와 Spring Boot의 완벽한 통합을 제공하는 커스텀 Spring Boot Starter입니다. 어노테이션 기반 선언으로 테스트 클래스별 컨테이너 격리와 다중 컨테이너 지원을 제공합니다.

## 주요 특징

- 🎯 **어노테이션 기반 선언**: `@EnableTestContainers`로 간편한 컨테이너 설정
- 🔄 **완벽한 테스트 격리**: 테스트 클래스별 독립적인 컨테이너 생명주기
- 🚀 **병렬 컨테이너 시작**: CompletableFuture를 통한 성능 최적화
- 🎛️ **다중/이중화 컨테이너**: 동일한 타입의 여러 컨테이너 지원
- 🔧 **자동 Bean 등록**: Spring Context에 자동으로 DataSource, RedisTemplate 등 등록
- 📊 **JUnit 5 완벽 지원**: PER_CLASS/PER_METHOD 라이프사이클 모두 지원
- ⚡ **병렬 테스트 실행**: CONCURRENT 실행 모드 지원
- 🏗️ **확장 가능한 아키텍처**: Factory Pattern 기반 새 컨테이너 타입 추가 용이

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
testcontainer:
  containers:
    testDb:
      image: "mariadb:11.4.7"
      database: "testdb"
      username: "test"
      password: "test"
      startupTimeout: 60
      environment:
        MARIADB_CHARACTER_SET_SERVER: "utf8mb4"
        MARIADB_COLLATION_SERVER: "utf8mb4_unicode_ci"
    testCache:
      image: "redis:7-alpine"
      password: "test123"
      startupTimeout: 30
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
testcontainer:
  containers:
    advancedDb:
      image: "mariadb:11.4.7"
      database: "advanced_test"
      username: "advanced_user"
      password: "secure_password"
      startupTimeout: 120
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

## 문제 해결

### 자주 발생하는 문제

#### 1. 컨테이너 시작 실패
```
org.testcontainers.containers.ContainerLaunchException: Container startup failed
```
**해결방안**:
- Docker 데몬 실행 확인
- 이미지 이름 확인
- startupTimeout 증가

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