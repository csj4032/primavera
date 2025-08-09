# Chapter 04 - 데이터 액세스 기초와 MyBatis

## 학습 목표

MyBatis를 활용한 데이터베이스 연동과 동적 프록시 패턴을 학습하며, 실제 데이터베이스와 연동하는 Spring Boot 애플리케이션을 구현합니다.

- MyBatis를 통한 SQL 매핑과 동적 쿼리 작성
- MariaDB 11.4.7 데이터베이스 연동 및 커넥션 풀 관리
- TestContainers를 활용한 통합 테스트 전략
- 동적 프록시 패턴(Dynamic Proxy) 구현과 활용
- 데이터 액세스 계층 아키텍처 설계
- 프로파일별 데이터베이스 설정 관리

## 프로젝트 구조

```
chap04/
├── src/main/java/com/genius/primavera/
│   ├── DataAccessApplication.java               # 메인 애플리케이션
│   ├── interfaces/                              # Presentation Layer
│   │   ├── PrimaveraController.java            # REST API 컨트롤러
│   │   └── PrimaveraResponseAdvice.java        # Response 후처리
│   ├── application/                             # Application Layer
│   │   ├── PrimaveraService.java               # 비즈니스 서비스
│   │   ├── DoSomething.java                    # 서비스 인터페이스
│   │   └── DoSomethingImpl.java                # 서비스 구현체
│   ├── dao/                                     # Data Access Layer
│   │   └── UserDao.java                        # MyBatis Mapper 인터페이스
│   ├── domain/                                  # Domain Layer
│   │   ├── User.java                           # User 엔티티
│   │   └── UserStatus.java                     # 사용자 상태 Enum
│   └── proxy/                                   # Dynamic Proxy
│       └── dynamic/                             # 동적 프록시 구현
│           ├── ProxyFactory.java               # 프록시 팩토리
│           ├── DynamicInvocationHandler.java    # 인보케이션 핸들러
│           ├── PrimaveraProxy.java             # 프록시 어노테이션
│           ├── ProxyAnnotation.java            # 프록시 메타 어노테이션
│           ├── ProxyPointAnnotation.java       # 프록시 포인트
│           └── ProxyInvocationFailedException.java # 프록시 예외
└── src/main/resources/
    ├── application.yml                          # 기본 설정
    ├── application-local.yml                   # 로컬 환경 설정
    └── sql/                                    # SQL 스크립트
        └── init.sql                           # 테이블 생성 스크립트
```

## 기술 스택

- **MyBatis**: 3.0.4 - SQL 매핑 프레임워크
- **MariaDB**: 11.4.7 - 관계형 데이터베이스
- **HikariCP**: 고성능 커넥션 풀
- **TestContainers**: 통합 테스트용 도커 컨테이너
- **Jackson**: JSON 데이터 바인딩
- **Reflections**: 리플렉션 유틸리티
- **Spring Boot Test**: 통합 테스트 지원

## 주요 기능

### 1. MyBatis를 통한 데이터 액세스

```java
@Mapper
public interface UserDao {
    
    @Select("SELECT * FROM USER WHERE id = #{id}")
    User findById(@Param("id") Long id);
    
    @Select("SELECT * FROM USER WHERE status = #{status}")
    List<User> findByStatus(@Param("status") UserStatus status);
    
    @Insert("INSERT INTO USER (name, email, status) VALUES (#{name}, #{email}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE USER SET name = #{name}, email = #{email}, status = #{status} WHERE id = #{id}")
    int update(User user);
    
    @Delete("DELETE FROM USER WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    @Select("SELECT COUNT(*) FROM USER")
    int count();
}
```

### 2. 동적 프록시 패턴 구현

```java
@ProxyAnnotation
public interface DoSomething {
    @ProxyPointAnnotation
    String doSomething();
}

@Component
@RequiredArgsConstructor
public class ProxyFactory {
    
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceType, Object target) {
        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class[]{interfaceType},
            new DynamicInvocationHandler(target)
        );
    }
}

public class DynamicInvocationHandler implements InvocationHandler {
    private final Object target;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(ProxyPointAnnotation.class)) {
            log.info("Before method execution: {}", method.getName());
            
            try {
                Object result = method.invoke(target, args);
                log.info("After method execution: {}", method.getName());
                return result;
            } catch (Exception e) {
                log.error("Exception in method execution: {}", method.getName(), e);
                throw new ProxyInvocationFailedException("Proxy execution failed", e);
            }
        }
        
        return method.invoke(target, args);
    }
}
```

### 3. 프로파일별 데이터베이스 설정

```yaml
# application.yml (기본 설정)
spring:
  application:
    name: primavera-chap04

mybatis:
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 100
    default-statement-timeout: 30

---
# Local 환경 설정
spring:
  config:
    activate:
      on-profile: local
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3308/primavera
    username: primavera
    password: primavera
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      validation-query: SELECT 1
      
---
# Test 환경 설정 (TestContainers 자동 설정)
spring:
  config:
    activate:
      on-profile: test
      
primavera:
  testcontainers:
    mariadb:
      enabled: true
      dockerImageName: mariadb:11.4.7
      databaseName: primavera
      username: primavera
      password: primavera
      initScript: sql/init.sql
```

### 4. 서비스 계층 구현

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrimaveraService {
    private final UserDao userDao;
    
    public List<User> getAllUsers() {
        return userDao.findByStatus(UserStatus.ACTIVE);
    }
    
    public User getUserById(Long id) {
        return Optional.ofNullable(userDao.findById(id))
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
    
    @Transactional
    public User createUser(User user) {
        user.setStatus(UserStatus.ACTIVE);
        userDao.insert(user);
        return user;
    }
    
    @Transactional
    public User updateUser(User user) {
        User existing = getUserById(user.getId());
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        userDao.update(existing);
        return existing;
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userDao.deleteById(id);
    }
}
```

### 5. Response 후처리

```java
@RestControllerAdvice
public class PrimaveraResponseAdvice implements ResponseBodyAdvice<Object> {
    
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }
    
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                 MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                 ServerHttpRequest request, ServerHttpResponse response) {
        
        if (body instanceof String) {
            return body; // 문자열은 그대로 반환
        }
        
        // API 응답을 표준 형태로 래핑
        return Map.of(
            "success", true,
            "data", body,
            "timestamp", LocalDateTime.now(),
            "path", request.getURI().getPath()
        );
    }
}
```

## 실행 방법

### 데이터베이스 준비

```bash
# Docker로 MariaDB 시작
docker run -d --name primavera-mariadb \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3308:3306 \
  mariadb:11.4.7

# 또는 Docker Manager 사용
./docker-manager.sh start chap04
```

### 애플리케이션 시작

```bash
# 로컬 프로파일로 실행 (MariaDB 필요)
./gradlew :chap04:bootRun -Dspring.profiles.active=local

# 테스트용 실행 (TestContainers 자동 관리)
./gradlew :chap04:test
```

### API 테스트

```bash
# 사용자 목록 조회
curl http://localhost:8080/users

# 특정 사용자 조회
curl http://localhost:8080/users/1

# 사용자 생성
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"김철수","email":"kim@example.com"}'

# 사용자 수정
curl -X PUT http://localhost:8080/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"김철수","email":"kim.updated@example.com"}'

# 사용자 삭제
curl -X DELETE http://localhost:8080/users/1

# 프록시 패턴 테스트
curl http://localhost:8080/proxy-test
```

## 핵심 학습 포인트

### 1. MyBatis 설정과 활용

```java
// Mapper 인터페이스에서 어노테이션 기반 SQL
@Mapper
public interface UserDao {
    
    // 동적 쿼리 - #{} 파라미터 바인딩
    @Select("SELECT * FROM USER WHERE id = #{id}")
    User findById(@Param("id") Long id);
    
    // 조건부 쿼리
    @Select({
        "<script>",
        "SELECT * FROM USER",
        "WHERE 1=1",
        "<if test='status != null'>",
        "  AND status = #{status}",
        "</if>",
        "<if test='name != null'>",
        "  AND name LIKE CONCAT('%', #{name}, '%')",
        "</if>",
        "</script>"
    })
    List<User> findByConditions(@Param("status") UserStatus status, 
                               @Param("name") String name);
    
    // 자동 생성 키 반환
    @Insert("INSERT INTO USER (name, email, status) VALUES (#{name}, #{email}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
```

### 2. HikariCP 커넥션 풀 설정

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10          # 최대 커넥션 수
      minimum-idle: 5                # 최소 유휴 커넥션 수
      idle-timeout: 300000           # 유휴 커넥션 타임아웃 (5분)
      connection-timeout: 20000      # 커넥션 획득 타임아웃 (20초)
      max-lifetime: 1800000          # 커넥션 최대 수명 (30분)
      validation-query: SELECT 1     # 커넥션 검증 쿼리
      leak-detection-threshold: 60000 # 커넥션 누수 감지 임계값 (1분)
      pool-name: PrimaveraCP         # 풀 이름
```

### 3. TestContainers 통합 테스트

```java
@SpringBootTest
@EnablePrimaveraTestcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User 데이터 액세스 통합 테스트")
class UserDaoIntegrationTest {
    
    @Autowired
    private UserDao userDao;
    
    @Test
    @Order(1)
    @DisplayName("사용자 생성 테스트")
    void shouldCreateUser() {
        // Given
        User user = User.builder()
            .name("테스트 사용자")
            .email("test@example.com")
            .status(UserStatus.ACTIVE)
            .build();
        
        // When
        int result = userDao.insert(user);
        
        // Then
        assertThat(result).isEqualTo(1);
        assertThat(user.getId()).isNotNull();
        
        User found = userDao.findById(user.getId());
        assertThat(found.getName()).isEqualTo("테스트 사용자");
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
    
    @Test
    @Order(2)
    @DisplayName("상태별 사용자 조회 테스트")
    void shouldFindUsersByStatus() {
        // Given - 테스트 데이터가 init.sql에 의해 준비됨
        
        // When
        List<User> activeUsers = userDao.findByStatus(UserStatus.ACTIVE);
        List<User> inactiveUsers = userDao.findByStatus(UserStatus.INACTIVE);
        
        // Then
        assertThat(activeUsers).isNotEmpty();
        assertThat(inactiveUsers).isEmpty(); // 초기 데이터에 비활성 사용자 없음
    }
}
```

### 4. 동적 프록시 활용 사례

```java
// 인터페이스 정의
@ProxyAnnotation
public interface CacheableService {
    
    @ProxyPointAnnotation
    String getCachedData(String key);
    
    @ProxyPointAnnotation
    void invalidateCache(String key);
}

// 구현체
@Component
public class CacheableServiceImpl implements CacheableService {
    
    @Override
    public String getCachedData(String key) {
        // 실제 데이터 조회 로직
        return "data-" + key;
    }
    
    @Override
    public void invalidateCache(String key) {
        // 캐시 무효화 로직
        log.info("Cache invalidated for key: {}", key);
    }
}

// 프록시 생성 및 사용
@Service
@RequiredArgsConstructor
public class ProxyExampleService {
    private final ProxyFactory proxyFactory;
    private final CacheableServiceImpl cacheableService;
    
    @PostConstruct
    public void init() {
        CacheableService proxy = proxyFactory.createProxy(
            CacheableService.class, 
            cacheableService
        );
        
        // 프록시를 통해 메서드 호출 - 로깅이 자동으로 추가됨
        String data = proxy.getCachedData("user-123");
    }
}
```

### 5. 트랜잭션 관리

```java
@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용
public class UserService {
    
    // 조회 메서드 - 읽기 전용 트랜잭션 사용
    public User getUser(Long id) {
        return userDao.findById(id);
    }
    
    // 생성/수정 메서드 - 읽기/쓰기 트랜잭션 사용
    @Transactional
    public User createUser(User user) {
        validateUser(user);
        userDao.insert(user);
        sendWelcomeEmail(user); // 트랜잭션 범위에 포함
        return user;
    }
    
    // 복합 작업 - 모든 작업이 하나의 트랜잭션으로 처리
    @Transactional
    public void transferUserData(Long fromUserId, Long toUserId) {
        User fromUser = userDao.findById(fromUserId);
        User toUser = userDao.findById(toUserId);
        
        // 복잡한 비즈니스 로직...
        userDao.update(fromUser);
        userDao.update(toUser);
        
        // 모든 작업이 성공하면 커밋, 하나라도 실패하면 롤백
    }
}
```

## 테스트 실행

```bash
# 전체 테스트 실행 (TestContainers 사용)
./gradlew :chap04:test

# 특정 테스트 클래스 실행
./gradlew :chap04:test --tests "UserDaoIntegrationTest"

# 테스트 커버리지 리포트 생성
./gradlew :chap04:jacocoTestReport

# 테스트 리포트 확인
open chap04/build/reports/tests/test/index.html
```

## 주요 애너테이션

| 애너테이션 | 용도 | 예제 |
|-----------|------|------|
| `@Mapper` | MyBatis 매퍼 인터페이스 | `@Mapper public interface UserDao` |
| `@Select` | SQL SELECT 쿼리 매핑 | `@Select("SELECT * FROM USER WHERE id = #{id}")` |
| `@Insert` | SQL INSERT 쿼리 매핑 | `@Insert("INSERT INTO USER ...")` |
| `@Update` | SQL UPDATE 쿼리 매핑 | `@Update("UPDATE USER SET name = #{name}")` |
| `@Delete` | SQL DELETE 쿼리 매핑 | `@Delete("DELETE FROM USER WHERE id = #{id}")` |
| `@Options` | SQL 실행 옵션 설정 | `@Options(useGeneratedKeys = true)` |
| `@Param` | 파라미터 이름 지정 | `User findById(@Param("id") Long id)` |
| `@Transactional` | 트랜잭션 경계 설정 | `@Transactional(readOnly = true)` |

## 실습 과제

### 1. 동적 쿼리 작성

조건에 따라 다른 쿼리를 실행하는 동적 쿼리를 작성해보세요:

```java
@Select({
    "<script>",
    "SELECT * FROM USER",
    "WHERE 1=1",
    "<if test='name != null and name != \"\"'>",
    "  AND name LIKE CONCAT('%', #{name}, '%')",
    "</if>",
    "<if test='status != null'>",
    "  AND status = #{status}",
    "</if>",
    "<if test='startDate != null and endDate != null'>",
    "  AND created_at BETWEEN #{startDate} AND #{endDate}",
    "</if>",
    "ORDER BY created_at DESC",
    "</script>"
})
List<User> searchUsers(@Param("name") String name,
                       @Param("status") UserStatus status,
                       @Param("startDate") LocalDateTime startDate,
                       @Param("endDate") LocalDateTime endDate);
```

### 2. 배치 처리 구현

여러 데이터를 한 번에 처리하는 배치 작업을 구현해보세요:

```java
@Insert({
    "<script>",
    "INSERT INTO USER (name, email, status) VALUES",
    "<foreach collection='users' item='user' separator=','>",
    "  (#{user.name}, #{user.email}, #{user.status})",
    "</foreach>",
    "</script>"
})
int insertBatch(@Param("users") List<User> users);
```

### 3. 복합 조회 쿼리

JOIN을 사용한 복합 조회 기능을 구현해보세요:

```java
@Select({
    "SELECT u.*, r.name as role_name",
    "FROM USER u",
    "LEFT JOIN USER_ROLE ur ON u.id = ur.user_id",
    "LEFT JOIN ROLE r ON ur.role_id = r.id",
    "WHERE u.id = #{id}"
})
@Results({
    @Result(property = "id", column = "id"),
    @Result(property = "name", column = "name"),
    @Result(property = "email", column = "email"),
    @Result(property = "roleName", column = "role_name")
})
UserWithRole findUserWithRole(@Param("id") Long id);
```

## 학습 순서

1. **데이터베이스 설정** - MariaDB 연결과 HikariCP 설정 확인
2. **MyBatis 매퍼** - `UserDao` 인터페이스의 SQL 어노테이션 분석
3. **서비스 계층** - `PrimaveraService`의 트랜잭션 처리 방식 학습
4. **동적 프록시** - `ProxyFactory`와 `DynamicInvocationHandler` 구현 분석
5. **통합 테스트** - TestContainers 기반 테스트 실행과 검증
6. **API 테스트** - RESTful API를 통한 CRUD 작업 확인

## 다음 단계 안내

**Chapter 05**에서는 로깅과 모니터링을 학습합니다:
- Logback을 활용한 구조화된 로깅 시스템
- 파일 기반 로그 관리와 로그 레벨별 분리
- 애플리케이션 모니터링과 성능 추적
- HikariCP 커넥션 풀 최적화
- CSV 파일 처리와 배치 데이터 연동

```bash
# 다음 챕터로 이동
cd ../chap05
./gradlew :chap05:bootRun -Dspring.profiles.active=local
```