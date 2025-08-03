# Spring Boot Starter TestContainer

## 📚 개요

Primavera 프로젝트에서 사용하는 커스텀 Spring Boot 스타터입니다. TestContainers를 통한 통합 테스트를 간편하게 설정할 수 있도록 자동 구성을 제공합니다.

## 🎯 주요 기능

- **자동 TestContainer 구성**: `@EnablePrimaveraTestcontainers` 어노테이션으로 간단 설정
- **다중 데이터베이스 지원**: MariaDB, MySQL, PostgreSQL, Redis, Kafka, Elasticsearch
- **라이프사이클 관리**: PER_CLASS, PER_TEST, REUSE 모드 지원
- **자동 프로퍼티 주입**: DataSource 설정 자동화
- **초기화 스크립트**: SQL 파일 자동 실행

## 🔄 최신 업데이트 - 로깅 시스템 개선

**System.out.println을 SLF4J 로깅으로 전환:**

### 변경된 파일들:
- `PrimaveraTestcontainersConfiguration`: TestContainers 초기화 로그 개선
- `PrimaveraTestcontainersInitializer`: 컨테이너 시작/중지 로그 개선  
- `ContainerManager`: 컨테이너 관리 상태 로그 개선
- 모든 Container Strategy 클래스들: 컨테이너별 설정 로그 개선

### 개선 효과:
- **테스트 디버깅**: 컨테이너 라이프사이클을 명확하게 추적 가능
- **성능 모니터링**: 컨테이너 시작 시간 및 리소스 사용량 측정
- **문제 해결**: TestContainers 관련 이슈 진단 용이
- **CI/CD 최적화**: 테스트 환경에서 로그 레벨 조정으로 성능 향상

## 🛠️ 사용 방법

### 1. 의존성 추가

```gradle
dependencies {
    testImplementation project(':appendix:spring-boot-starter-test-container')
}
```

### 2. 테스트 클래스 설정

```java
@SpringBootTest
@EnablePrimaveraTestcontainers
@ActiveProfiles("test")
class MyIntegrationTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    void testUserCreation() {
        User user = new User();
        user.setEmail("test@example.com");
        userMapper.insert(user);
        
        assertThat(user.getId()).isNotNull();
    }
}
```

### 3. 설정 파일 (application-test.yml)

```yaml
primavera:
  testcontainers:
    lifecycle-mode: PER_CLASS
    mariadb:
      enabled: true
      docker-image-name: mariadb:11.4.7
      driver-class-name: org.mariadb.jdbc.Driver
      database-name: primavera
      username: primavera
      password: primavera
      init-script: sql/init.sql
```

## 🔧 지원하는 컨테이너

### MariaDB
```yaml
primavera:
  testcontainers:
    mariadb:
      enabled: true
      docker-image-name: mariadb:11.4.7
      database-name: primavera
      username: primavera  
      password: primavera
      init-script: sql/init.sql
```

### MySQL
```yaml
primavera:
  testcontainers:
    mysql:
      enabled: true
      docker-image-name: mysql:8.0
      database-name: primavera
      username: primavera
      password: primavera
      init-script: sql/init.sql
```

### PostgreSQL
```yaml
primavera:
  testcontainers:
    postgresql:
      enabled: true
      docker-image-name: postgres:15
      database-name: primavera
      username: primavera
      password: primavera
      init-script: sql/init.sql
```

### Redis
```yaml
primavera:
  testcontainers:
    redis:
      enabled: true
      docker-image-name: redis:7-alpine
```

### Kafka
```yaml
primavera:
  testcontainers:
    kafka:
      enabled: true
      docker-image-name: confluentinc/cp-kafka:latest
```

### Elasticsearch
```yaml
primavera:
  testcontainers:
    elasticsearch:
      enabled: true
      docker-image-name: elasticsearch:8.12.0
```

## 🔄 라이프사이클 모드

### PER_CLASS (기본값)
- 테스트 클래스당 하나의 컨테이너 인스턴스
- 동일 클래스 내 테스트 메서드들이 컨테이너 공유
- 빠른 테스트 실행

### PER_TEST
- 테스트 메서드마다 새로운 컨테이너 생성
- 완전한 격리 보장
- 느린 실행 속도

### REUSE
- 전체 테스트 수트에서 컨테이너 재사용
- 가장 빠른 실행 속도
- 테스트 간 데이터 의존성 주의 필요

## 📊 컨테이너 관리

### ContainerManager
- 컨테이너 인스턴스 중앙 관리
- 라이프사이클별 시작/중지 제어
- 상태 모니터링 및 로깅

### ContainerKey
- 컨테이너 유니크 식별자
- 테스트 클래스 + 컨테이너 타입 조합
- 효율적인 컨테이너 검색 및 관리

## 🧪 테스트 예제

### 단일 컨테이너 테스트
```java
@SpringBootTest
@EnablePrimaveraTestcontainers(MariaDB.class)
class SingleContainerTest {
    
    @Test
    void testMariaDBConnection() {
        // MariaDB 컨테이너만 시작됨
    }
}
```

### 다중 컨테이너 테스트
```java
@SpringBootTest
@EnablePrimaveraTestcontainers({MariaDB.class, Redis.class})
class MultipleContainerTest {
    
    @Test
    void testMultipleContainers() {
        // MariaDB + Redis 컨테이너 동시 시작
    }
}
```

### 모든 컨테이너 테스트
```java
@SpringBootTest
@EnablePrimaveraTestcontainers
class AllContainerTest {
    
    @Test
    void testAllContainers() {
        // 활성화된 모든 컨테이너 시작
    }
}
```

## 🐳 Docker 리소스 최적화

### 컨테이너 재사용 전략
```java
@SpringBootTest
@EnablePrimaveraTestcontainers
@TestMethodOrder(OrderAnnotation.class)
class ReuseLifecycleTest {
    
    @Test
    @Order(1)
    void firstTest() {
        // 첫 번째 테스트에서 컨테이너 시작
    }
    
    @Test
    @Order(2) 
    void secondTest() {
        // 동일한 컨테이너 재사용
    }
}
```

### 리소스 모니터링
```java
@Test
void monitorContainerResources() {
    // 메모리, CPU, 네트워크 포트 사용량 체크
    ContainerManager.printStatusReport();
}
```

## 🔍 디버깅 및 모니터링

### 로그 활성화
```yaml
logging:
  level:
    com.genius.primavera.testcontainer: DEBUG
    org.testcontainers: INFO
```

### 컨테이너 상태 확인
```java
@Test
void checkContainerStatus() {
    ContainerManager.printStatusReport();
    // 실행 중인 모든 컨테이너 상태 출력
}
```

## 🚀 성능 최적화 팁

1. **적절한 라이프사이클 선택**: 격리 요구사항과 성능의 균형
2. **필요한 컨테이너만 활성화**: enabled: false로 불필요한 컨테이너 비활성화  
3. **경량 도커 이미지 사용**: alpine 태그 활용
4. **컨테이너 재사용**: REUSE 모드로 전체 테스트 수트 최적화
5. **초기화 스크립트 최소화**: 필수 데이터만 포함한 경량 init.sql

## 📚 참고 자료

- [TestContainers 공식 문서](https://www.testcontainers.org/)
- [Spring Boot Test 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Docker Compose TestContainers](https://www.testcontainers.org/modules/docker_compose/)