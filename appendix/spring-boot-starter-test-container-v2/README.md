# Spring Boot TestContainer V2

JUnit Extension 기반의 개선된 TestContainer 통합 라이브러리

## 주요 기능

- **PER_METHOD 모드**: 자동으로 모든 것 처리
- **PER_CLASS 모드**: `@DynamicPropertySource` 또는 `AutoDynamicPropertySource` 상속으로 지원
- **application.yml 설정 지원**: 자동으로 설정 파일 로딩
- **다양한 컨테이너 지원**: MariaDB, MySQL, PostgreSQL, Redis, Kafka, Elasticsearch, MongoDB

## 사용법

### PER_METHOD (기본)

```java
@SpringBootTest
@EnableTestContainers(containers = {ContainerType.MARIADB})
class MyTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    void test() {
        // 자동으로 컨테이너 시작 및 설정
    }
}
```

### PER_CLASS 모드

#### 방법 1: AutoDynamicPropertySource 상속 (권장)

```java
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableTestContainers(containers = {ContainerType.MARIADB})
class MyTest extends AutoDynamicPropertySource {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    void test() {
        // @DynamicPropertySource가 자동으로 처리됨
    }
}
```

#### 방법 2: @DynamicPropertySource 직접 사용

```java
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableTestContainers(containers = {ContainerType.MARIADB})
class MyTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        DynamicContainerSupport.configureContainers(MyTest.class, registry);
    }
    
    @Test
    void test() {
        // 정상 작동
    }
}
```

## 설정

`application-test.yml`:

```yaml
primavera:
  testcontainers:
    lifecycleMode: PER_METHOD
    mariadb:
      enabled: true
      dockerImageName: mariadb:11.4.7
      databaseName: primavera
      username: primavera
      password: primavera
      initScript: sql/init.sql
```

## 지원 컨테이너

- `MARIADB`
- `MYSQL` 
- `POSTGRESQL`
- `REDIS`
- `KAFKA`
- `ELASTICSEARCH`
- `MONGODB`