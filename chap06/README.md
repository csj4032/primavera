# Chapter 06: 고급 유효성 검증 및 웹 API 개발

## 📋 개요

이 챕터에서는 Spring Boot와 MyBatis를 활용한 고급 유효성 검증 시스템과 RESTful API 개발을 다룹니다. 사용자 관리 시스템을 통해 복잡한 비즈니스 규칙 검증, 커스텀 Validator 구현, 그리고 TestContainers를 활용한 통합 테스트를 학습합니다.

## 🏗️ 아키텍처

### 계층 구조
```
┌─────────────────────────────────────┐
│        Interface Layer              │  ← REST Controllers, AJAX Endpoints
├─────────────────────────────────────┤
│        Application Layer            │  ← Business Services, Custom Validators
├─────────────────────────────────────┤
│        Domain Layer                 │  ← Models, Mappers, Type Handlers
└─────────────────────────────────────┘
```

### 주요 컴포넌트
- **Domain Models**: 풍부한 유효성 검증이 포함된 엔티티
- **MyBatis Mappers**: 어노테이션 기반 SQL 매핑
- **Custom Validators**: 비즈니스 규칙에 특화된 검증 로직
- **REST Controllers**: 그룹 기반 유효성 검증을 지원하는 API

## 🛠️ 기술 스택

### 핵심 기술
- **Spring Boot 3.5.3**: 메인 프레임워크
- **MyBatis 3.x**: SQL 매핑 및 동적 쿼리
- **Jakarta Bean Validation**: 선언적 유효성 검증
- **GraalVM JavaScript**: 복잡한 검증 로직 스크립팅
- **BCrypt**: 비밀번호 암호화

### 데이터베이스
- **MySQL 8.0**: 주 데이터베이스
- **TestContainers**: 통합 테스트용 컨테이너

### 테스트 도구
- **JUnit 5**: 테스트 프레임워크
- **TestRestTemplate**: REST API 테스트
- **TestContainers**: 데이터베이스 통합 테스트

## 📁 프로젝트 구조

```
chap05/
├── src/main/java/com/genius/primavera/
│   ├── PrimaveraApplication.java                    # 메인 애플리케이션
│   ├── application/                                 # 응용 계층
│   │   ├── UserService.java                        # 서비스 인터페이스
│   │   ├── UserServiceImpl.java                    # 서비스 구현체
│   │   └── validator/                              # 커스텀 검증자
│   │       ├── Nickname.java                       # 닉네임 검증 어노테이션
│   │       └── NicknameValidator.java              # 닉네임 검증 로직
│   ├── domain/                                     # 도메인 계층
│   │   ├── model/                                  # 도메인 모델
│   │   │   ├── User.java                          # 사용자 엔티티
│   │   │   ├── Role.java                          # 권한 엔티티
│   │   │   ├── UserRole.java                      # 사용자-권한 연관
│   │   │   ├── RoleType.java                      # 권한 타입 enum
│   │   │   ├── UserStatus.java                    # 사용자 상태 enum
│   │   │   ├── TypeHandlerException.java          # 타입 핸들러 예외
│   │   │   └── typehandler/                       # MyBatis 타입 핸들러
│   │   │       ├── RoleTypeHandler.java           # 권한 타입 변환
│   │   │       └── UserStatusTypeHandler.java     # 상태 변환
│   │   └── mapper/                                 # 데이터 접근 계층
│   │       ├── UserMapper.java                    # 사용자 매퍼
│   │       ├── UserRoleMapper.java                # 권한 매퍼
│   │       └── support/                           # 매퍼 지원 클래스
│   │           └── UserTableSupport.java          # MyBatis Dynamic SQL 지원
│   └── interfaces/                                 # 인터페이스 계층
│       ├── UserController.java                    # 사용자 REST API
│       └── AjaxController.java                    # AJAX 엔드포인트
├── src/test/java/com/genius/primavera/
│   ├── domain/                                     # 테스트 인프라
│   │   ├── AbstractContainerTest.java             # 컨테이너 테스트 기반 클래스
│   │   └── AbstractJpaContainerTest.java          # JPA 테스트 기반 클래스
│   └── interfaces/                                 # 인터페이스 테스트
│       ├── AjaxControllerTest.java                # AJAX API 테스트
│       ├── UserSaveValidationTest.java            # 사용자 등록 검증 테스트
│       └── UserUpdateValidationTest.java          # 사용자 수정 검증 테스트
└── src/test/resources/
    └── sql/
        └── init-db.sql                              # 테스트 DB 스키마
```

## 🔍 주요 기능

### 1. 고급 유효성 검증 시스템

#### 다층 검증 구조
```java
@Getter @Setter @Builder
@ScriptAssert(lang = "graal.js", 
    script = "_this.isComplex(_this.createdAt, _this.updatedAt)", 
    message = "등록일자와 수정일자는 필수 입니다.")
public class User {
    @Min(value = 1, groups = UpdateGroup.class)
    private long id;
    
    @Email
    private String email;
    
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,20}$")
    private String password;
    
    @Nickname  // 커스텀 검증자
    private String nickname;
    
    @NotNull(groups = UpdateGroup.class)
    private UserStatus status;
}
```

#### 커스텀 검증자
```java
@Constraint(validatedBy = NicknameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nickname {
    String message() default "올바르지 않은 별명입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### 검증 그룹 활용
```java
@PostMapping("/save")
@Validated(User.SaveGroup.class)
public ResponseEntity<User> save(@Valid @RequestBody User user) {
    // 저장 시에만 적용되는 검증 규칙
}

@PostMapping("/update")  
@Validated(User.UpdateGroup.class)
public ResponseEntity<User> update(@Valid @RequestBody User user) {
    // 수정 시에만 적용되는 검증 규칙 (ID 필수)
}
```

### 2. MyBatis 고급 매핑

#### 어노테이션 기반 매핑
```java
@Mapper
public interface UserMapper {
    @Results(id = "USER_WITH_ROLES", value = {
        @Result(property = "id", column = "ID"),
        @Result(property = "email", column = "EMAIL"),
        @Result(property = "roles", javaType = List.class, column = "ID", 
                many = @Many(select = "com.genius.primavera.domain.mapper.UserRoleMapper.findByUserId"))
    })
    @Select("SELECT ID, EMAIL, NICKNAME, PASSWORD, STATUS, CREATED_AT, UPDATED_AT FROM USERS WHERE ID = #{id}")
    User findByIdWithRoles(@Param("id") long id);
}
```

#### 커스텀 타입 핸들러
```java
@Component
public class UserStatusTypeHandler extends BaseTypeHandler<UserStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserStatus parameter, JdbcType jdbcType) 
            throws SQLException {
        ps.setInt(i, parameter.getValue());
    }
    
    @Override
    public UserStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return UserStatus.findByValue(rs.getInt(columnName));
    }
}
```

### 3. TestContainers 통합 테스트

#### 기존 방식 - 컨테이너 기반 테스트 설정
```java
@Testcontainers
public abstract class AbstractContainerTest {
    @Container
    protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init-db.sql")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }
}
```

#### 새로운 방식 - @EnablePrimaveraTestcontainers 사용
```java
@SpringBootTest
@EnablePrimaveraTestcontainers
public class UserSaveValidationTest {
    // TestContainers 설정이 자동으로 완료
    // MariaDB 11.4.7이 자동으로 시작되고 DataSource가 설정됨
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testUserValidation() {
        User invalidUser = User.builder()
            .email("invalid-email")  // 잘못된 이메일 형식
            .password("weak")        // 약한 비밀번호
            .build();
            
        ResponseEntity<User> response = restTemplate.postForEntity(
            "/api/users/save", invalidUser, User.class);
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
```

#### @EnablePrimaveraTestcontainers 장점
- **코드 간소화**: 복잡한 TestContainers 설정 코드 제거
- **일관성**: 모든 테스트에서 동일한 MariaDB 11.4.7 환경
- **자동 설정**: DataSource, 초기화 스크립트 자동 처리
- **빠른 시작**: 어노테이션 하나로 테스트 환경 구성 완료

#### 포괄적인 검증 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("사용자 등록 유효성 검증 테스트")
public class UserSaveValidationTest extends AbstractContainerTest {
    
    @Test
    @DisplayName("잘못된 이메일 형식 유효성 검증")
    public void saveAndReturnUserIllegalEmail() {
        User source = User.builder()
            .email("genius@")  // 잘못된 이메일 형식
            .password("Secret0!")
            .nickname("genius")
            .roles(List.of(new Role(1, RoleType.USER)))
            .build();
        
        ResponseEntity<User> response = restTemplate.postForEntity("/users/save", source, User.class);
        assertEquals(400, response.getStatusCodeValue());
    }
}
```

## 🚀 실행 방법

### 1. 환경 요구사항
- **Java 17+**
- **Docker** (TestContainers 사용)
- **MySQL 8.0** (로컬 실행 시)

### 2. 애플리케이션 실행
```bash
# 1. 프로젝트 루트에서 빌드
./gradlew :chap05:build

# 2. 애플리케이션 실행
./gradlew :chap05:bootRun

# 3. 테스트 실행
./gradlew :chap05:test
```

### 3. API 엔드포인트

#### 사용자 관리 API
```http
# 사용자 조회
GET /users/{id}

# 사용자 등록 (SaveGroup 검증)
POST /users/save
Content-Type: application/json
{
    "email": "user@example.com",
    "password": "Complex1!",
    "nickname": "nickname",
    "roles": [{"id": 1, "type": "USER"}]
}

# 사용자 수정 (UpdateGroup 검증)
POST /users/update
Content-Type: application/json
{
    "id": 1,
    "nickname": "newNickname",
    "status": "ON"
}
```

#### AJAX API
```http
# AJAX 테스트 페이지
GET /ajax

# HTML 응답
GET /ajax/html

# JSON 응답
GET /ajax/form

# 파라미터 처리
GET /ajax/form/data?id=1&email=test@example.com
```

## 🧪 테스트 전략

### 1. 유효성 검증 테스트
- **이메일 형식 검증**: 정규표현식 기반 검증
- **비밀번호 복잡성**: 대소문자, 숫자, 특수문자 조합
- **닉네임 검증**: 한글, 영문, 숫자 조합 및 길이 제한
- **권한 검증**: 필수 권한 및 유효한 권한 ID 검증
- **날짜 로직 검증**: GraalVM JavaScript를 통한 복잡한 날짜 검증

### 2. 통합 테스트
- **TestContainers**: 실제 MySQL 컨테이너 사용
- **전체 애플리케이션 컨텍스트**: 모든 레이어 통합 테스트
- **HTTP 클라이언트 테스트**: TestRestTemplate 활용

### 3. 테스트 데이터
```sql
-- 테스트용 사용자 데이터
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT) VALUES 
(1, 'genius@gmail.com', '$2a$10$N8kKAJz4rT8d.JLZ8QqC6O8.YhJQrGeFGRqF2QhPZKJf3ZcJwQq7e', 
 'genius', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 권한 데이터
INSERT INTO ROLES (ID, TYPE) VALUES 
(1, 1), -- ADMINISTRATOR
(2, 2), -- MANAGER  
(3, 3); -- USER
```

## 📚 학습 포인트

### 1. 고급 유효성 검증
- **검증 그룹**: 상황별 다른 검증 규칙 적용
- **커스텀 검증자**: 비즈니스 로직에 특화된 검증
- **스크립트 검증**: JavaScript를 활용한 복잡한 검증 로직

### 2. MyBatis 고급 기능
- **중첩 결과 매핑**: 연관 관계 매핑
- **타입 핸들러**: 커스텀 타입 변환
- **동적 SQL**: MyBatis Dynamic SQL 활용

### 3. 테스트 자동화
- **컨테이너 기반 테스트**: 실제 환경과 동일한 테스트
- **테스트 격리**: 각 테스트 독립적 실행
- **포괄적 커버리지**: 다양한 검증 시나리오 테스트

## 🔧 설정

### application-local.yml
```yaml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mysql://localhost:3306/primavera
    username: primavera
    password: primavera
    hikari:
      auto-commit: false
      data-source-properties:
        cachePrepStmts: false
        useServerPrepStmts: false
        useLocalSessionState: false
        cacheResultSetMetadata: false
        preparedStatementCacheQueries: 0
  aop:
    proxy-target-class: true

mybatis:
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 1000
    default-statement-timeout: 30
    cache-enabled: false
    local-cache-scope: statement
  type-aliases-package: com.genius.primavera.domain
  type-handlers-package: com.genius.primavera.domain
```

### build.gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-devtools'
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter'
    implementation 'org.graalvm.js:js:20.2.0'
    implementation 'org.graalvm.js:js-scriptengine:20.2.0'
    implementation 'com.mysql:mysql-connector-j'
    
    testImplementation 'org.testcontainers:mysql'
    testImplementation 'org.testcontainers:junit-jupiter'
}
```

## 🎯 주요 특징

- ✅ **다층 유효성 검증**: 어노테이션, 커스텀 검증자, 스크립트 검증
- ✅ **검증 그룹 지원**: 상황별 검증 규칙 적용
- ✅ **MyBatis 고급 매핑**: 중첩 결과, 타입 핸들러
- ✅ **컨테이너 기반 테스트**: TestContainers 활용
- ✅ **RESTful API**: 표준 HTTP 상태 코드 활용
- ✅ **한국어 지원**: 다국어 메시지 및 한글 닉네임 검증
- ✅ **LiveReload**: 개발 생산성 향상을 위한 실시간 새로고침

## 📖 참고 자료

- [Spring MVC Test Framework](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#spring-mvc-test-framework)
- [Spring-boot-data-source-decorator](https://github.com/gavlyukovskiy/spring-boot-data-source-decorator)
- [Migration from Nashorn to GraalVM JavaScript](https://golb.hplar.ch/2020/04/java-javascript-engine.html)
- [Hibernate Validator](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/)
- [LiveReload Extensions](http://livereload.com/extensions/)

이 챕터를 통해 실무에서 요구되는 견고한 유효성 검증 시스템과 통합 테스트 기법을 습득할 수 있습니다.