# Local vs Test 환경 구분 가이드

## 🎯 환경 구분 전략

### 📍 Local 개발 환경 (localhost MySQL)
- **목적**: 개발자가 로컬에서 애플리케이션 개발 및 디버깅
- **데이터베이스**: localhost MySQL 8.4.0
- **설정 파일**: `application-local.yml`
- **실행 방법**: `./gradlew :chap11:bootRun -Pprofile=local`

### 🧪 Test 환경 (TestContainers)
- **목적**: 자동화된 통합 테스트 실행
- **데이터베이스**: TestContainers MySQL 8.4.0 (Docker)
- **설정 파일**: `application-testcontainer.yml`
- **실행 방법**: `./gradlew :chap11:test`

---

## 🚀 Local 환경 설정

### 1. MySQL 8.4.0 설치 및 실행
```bash
# Docker로 MySQL 실행
docker run -d --name mysql-primavera \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=primavera \
  -e MYSQL_USER=primavera \
  -e MYSQL_PASSWORD=primavera \
  -p 3306:3306 mysql:8.4.0
```

### 2. Local 환경 실행
```bash
# Local 프로파일로 애플리케이션 실행
./gradlew :chap11:bootRun -Pprofile=local

# 또는 IDE에서 다음 VM 옵션 설정
-Dspring.profiles.active=local
```

### 3. Local 환경 설정 파일
**`application-local.yml`**
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/primavera?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: primavera
    password: primavera
  flyway:
    enabled: true  # Flyway로 스키마 관리
```

---

## 🧪 Test 환경 설정

### 1. TestContainers 기반 테스트 실행
```bash
# 모든 테스트 실행
./gradlew :chap11:test

# 특정 테스트 실행
./gradlew :chap11:test --tests ArticleMapperImprovedTest

# 테스트 결과 보기
./gradlew :chap11:test --continue
```

### 2. TestContainers 자동 관리
- **자동 시작**: 테스트 실행 시 MySQL 8.4.0 컨테이너 자동 시작
- **자동 종료**: 테스트 완료 후 컨테이너 자동 정리
- **독립성**: 각 테스트 클래스마다 독립적인 컨테이너 사용
- **스키마**: `schema.sql` 자동 실행으로 테이블 및 테스트 데이터 생성

### 3. Test 환경 설정 파일
**`application-testcontainer.yml`**
```yaml
spring:
  # TestContainers가 동적으로 DataSource 설정
  flyway:
    enabled: false  # schema.sql 사용
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
```

---

## 📝 테스트 작성 방법

### 방법 1: @BaseIntegrationTest 어노테이션 사용
```java
@BaseIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class YourIntegrationTest {
    
    @Autowired
    private YourMapper yourMapper;
    
    @Test
    @Order(1)
    @DisplayName("테스트 설명")
    void testMethod() {
        // 테스트 코드
    }
}
```

### 방법 2: 직접 TestContainers 설정 (권장)
```java
@SpringBootTest(classes = TestApplication.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
@Rollback(false)
class YourIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
        .withDatabaseName("primavera")
        .withUsername("primavera")
        .withPassword("primavera")
        .withInitScript("sql/schema.sql");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }
    
    // 테스트 메서드들...
}
```

---

## 🔧 주요 특징

### ✅ Local 환경 장점
- 실제 개발 환경과 동일한 데이터베이스 사용
- 데이터 영속성 보장 (재시작 후에도 데이터 유지)
- 빠른 애플리케이션 시작
- 실시간 디버깅 가능

### ✅ Test 환경 장점  
- 완전히 독립적인 테스트 환경
- 테스트 실행 시마다 깨끗한 데이터베이스
- CI/CD 파이프라인에서 안정적 실행
- Docker 없이도 MySQL 환경 제공

### 🚨 주의사항
- Local 환경은 localhost:3306 MySQL이 실행 중이어야 함
- Test 환경은 Docker가 실행 중이어야 함 (TestContainers 사용)
- MyBatis 전용 설정으로 JPA 관련 설정은 모두 비활성화됨

---

## 📊 실행 예시

### Local 환경 실행 로그
```
2025-07-26 00:45:00.123  INFO --- [  restartedMain] c.g.p.HierarchicalCommentApplication : Starting application with Local profile
2025-07-26 00:45:00.456  INFO --- [  restartedMain] c.g.p.HierarchicalCommentApplication : Connected to localhost MySQL
```

### Test 환경 실행 로그
```
2025-07-26 00:45:10.123  INFO --- [    Test worker] c.g.p.config.BaseTestConfiguration : TestContainers MySQL started: jdbc:mysql://localhost:54321/primavera
2025-07-26 00:45:15.456  INFO --- [         Thread] ArticleMapperImprovedTest : ✅ 전체 게시글 목록 (총 6개):
2025-07-26 00:45:15.457  INFO --- [         Thread] ArticleMapperImprovedTest :   📄 Level 0 | ID: 1 | Subject: TestContainers 기반 원글 | Author: Genius
```

이제 Local과 Test 환경이 완전히 분리되어 각각의 목적에 맞게 사용할 수 있습니다! 🎉