# Chapter 15: JPA 고급 매핑 (JPA Advanced Mapping)

## 📚 학습 목표

이 장에서는 JPA의 고급 매핑 전략을 심화 학습합니다:
- **상속 관계 매핑**: 객체의 상속 구조를 데이터베이스 테이블로 매핑하는 다양한 전략
- **연관 관계 매핑**: 1:1, 1:N, N:1, N:N 관계의 세밀한 매핑 방법 완전 정복
- **복합 키 매핑**: 복합 기본키와 복합 외래키 처리
- **고급 JPA 패턴**: 실무에서 사용되는 복잡한 도메인 모델링 기법

## 🏗️ 프로젝트 구조

```
chap15/
├── src/main/java/com/genius/primavera/
│   ├── JpaAdvancedMappingApplication.java    # 메인 애플리케이션
│   └── domain/
│       ├── hierarchy/                        # 상속 관계 매핑
│       │   ├── Item.java                    # 추상 부모 엔티티
│       │   ├── Album.java                   # 앨범 상품
│       │   ├── Book.java                    # 도서 상품  
│       │   ├── Movie.java                   # 영화 상품
│       │   └── ...                          # 기타 상속 엔티티들
│       └── relation/                         # 연관 관계 매핑
│           ├── oneToOne/                    # 1:1 관계
│           ├── oneToMany/                   # 1:N 관계
│           ├── manyToOne/                   # N:1 관계
│           └── manyToMany/                  # N:N 관계
└── src/test/java/
    ├── BaseJpaTest.java                     # 공통 테스트 기반 클래스
    └── domain/
        ├── hierarchy/                       # 상속 관계 테스트
        └── relation/                        # 연관 관계 테스트
```

## 🔗 상속 관계 매핑 (Inheritance Mapping)

### 1. 단일 테이블 전략 (Single Table Strategy)

```java
@Entity
@Table(name = "items")
@DiscriminatorColumn(name = "dtype")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private int price;
    private Integer stockQuantity = 0;
}

@Entity
@DiscriminatorValue("A")
public class Album extends Item {
    private String artist;
    // ...
}
```

**특징:**
- ✅ **장점**: 조인이 필요 없어 조회 성능이 빠름, 쿼리가 단순
- ❌ **단점**: 자식 엔티티 컬럼은 모두 null 허용, 테이블이 커질 수 있음
- 🔍 **구분 컬럼**: `@DiscriminatorColumn` 필수 사용

### 2. 조인 전략 (Joined Strategy)

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Item {
    // 부모 테이블
}

@Entity
@Table(name = "ALBUM")
@PrimaryKeyJoinColumn(name = "ALBUM_ID")
public class Album extends Item {
    // 자식 테이블 (조인으로 연결)
}
```

**특징:**
- ✅ **장점**: 테이블 정규화, 외래키 참조 무결성 제약조건 활용 가능
- ❌ **단점**: 조인으로 인한 성능 저하, 복잡한 쿼리

### 3. 구현 클래스마다 테이블 전략 (Table Per Class)

```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Item {
    // 각 구현 클래스마다 별도 테이블
}
```

**특징:**
- ✅ **장점**: 서브 타입 구별해서 처리할 때 효과적
- ❌ **단점**: 여러 자식 테이블 함께 조회할 때 성능 저하 (UNION SQL)

## 🔗 연관 관계 매핑 (Association Mapping)

### 1. 일대일 (1:1) 관계

#### 주 테이블에 외래 키 (단방향)
```java
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;
}
```

#### 대상 테이블에 외래 키 (양방향)
```java
@Entity
public class Address {
    @Id @GeneratedValue
    private Long id;
    
    @OneToOne(mappedBy = "address")
    private Member member;
}
```

### 2. 일대다 (1:N) 관계

#### 단방향
```java
@Entity
public class Professor {
    @OneToMany
    @JoinColumn(name = "professor_id")
    private List<Student> students = new ArrayList<>();
}
```

#### 양방향 (권장)
```java
@Entity
public class Customer {
    @OneToMany(mappedBy = "customer")
    private List<Contact> contacts = new ArrayList<>();
}

@Entity
public class Contact {
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
```

### 3. 다대일 (N:1) 관계

#### 단방향
```java
@Entity
public class Player {
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
```

#### 양방향
```java
@Entity
public class Employee {
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}

@Entity
public class Department {
    @OneToMany(mappedBy = "department")
    private List<Employee> employees = new ArrayList<>();
}
```

### 4. 다대다 (N:N) 관계

#### 단방향
```java
@Entity
public class Publisher {
    @ManyToMany
    @JoinTable(
        name = "PUBLISHER_SUBSCRIBER",
        joinColumns = @JoinColumn(name = "PUBLISHER_ID"),
        inverseJoinColumns = @JoinColumn(name = "SUBSCRIBER_ID")
    )
    private List<Subscriber> subscribers = new ArrayList<>();
}
```

#### 복합 키를 가진 연결 엔티티 (권장)
```java
@Entity
public class Letter {
    @EmbeddedId
    private LetterId id;
    
    @ManyToOne
    @MapsId("senderId")
    @JoinColumn(name = "sender_id")
    private Sender sender;
    
    @ManyToOne
    @MapsId("recipientId")
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;
    
    private LocalDateTime sentDate;
}

@Embeddable
public class LetterId implements Serializable {
    private Long senderId;
    private Long recipientId;
}
```

## 🧪 테스트 전략

### 계층별 테스트 구조

```java
// 공통 기반 클래스
public abstract class BaseJpaTest {
    protected EntityManager entityManager;
    protected EntityTransaction entityTransaction;
    
    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7")
        .withDatabaseName("primavera")
        .withUsername("primavera")
        .withPassword("primavera")
        .withInitScript("sql/init.sql");
}

// 상속 관계 테스트
@DisplayName("단일 테이블 전략 매핑")
public class SingleTableStrategyTest extends BaseHierarchyJpaTest {
    
    @Test
    @DisplayName("아이템 저장")
    public void save() {
        var album = new Album("앨범", 100, "artist");
        entityTransaction.begin();
        entityManager.persist(album);
        entityTransaction.commit();
    }
}
```

### 테스트 범위

1. **상속 관계 테스트**
   - 각 상속 전략별 CRUD 동작 검증
   - 다형성 쿼리 테스트
   - 성능 특성 검증

2. **연관 관계 테스트**
   - 연관 관계 저장/조회 테스트
   - 지연/즉시 로딩 테스트
   - 영속성 전이 테스트

3. **복합 키 테스트**
   - 복합 기본키 저장/조회
   - 복합 외래키 관계 테스트

## ⚙️ 설정 및 환경

### 데이터베이스 설정

```yaml
# application.yml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/primavera
    username: primavera
    password: primavera
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        format_sql: true
        use_sql_comments: true
    show-sql: true
```

### 의존성

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.mariadb.jdbc:mariadb-java-client:3.5.4'
    
    testImplementation 'org.testcontainers:mariadb:1.20.4'
    testImplementation 'org.testcontainers:junit-jupiter:1.20.4'
}
```

## 🚀 실행 방법

### 1. MariaDB 시작
```bash
# Docker Compose 사용 (권장)
docker-compose up -d

# 또는 직접 실행
docker run -d --name mariadb-primavera \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3306:3306 mariadb:11.4.7
```

### 2. 애플리케이션 실행
```bash
# 애플리케이션 실행
./gradlew :chap14:bootRun

# 테스트 실행
./gradlew :chap14:test

# 특정 테스트 실행
./gradlew :chap14:test --tests "SingleTableStrategyTest"
```

## 📊 성능 고려사항

### 상속 전략별 성능 특성

| 전략 | 조회 성능 | 저장 성능 | 테이블 정규화 | 추천 상황 |
|------|-----------|-----------|---------------|-----------|
| SINGLE_TABLE | ⭐⭐⭐ | ⭐⭐⭐ | ❌ | 단순한 상속, 빠른 조회 필요 |
| JOINED | ⭐⭐ | ⭐⭐ | ⭐⭐⭐ | 복잡한 상속, 데이터 무결성 중요 |
| TABLE_PER_CLASS | ⭐ | ⭐⭐ | ⭐⭐ | 상속 클래스 간 차이가 큰 경우 |

### 연관 관계 최적화

1. **지연 로딩 활용**
   ```java
   @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
   private List<Child> children = new ArrayList<>();
   ```

2. **배치 크기 설정**
   ```java
   @BatchSize(size = 100)
   @OneToMany(mappedBy = "parent")
   private List<Child> children = new ArrayList<>();
   ```

3. **페치 조인 사용**
   ```java
   @Query("SELECT p FROM Parent p JOIN FETCH p.children")
   List<Parent> findAllWithChildren();
   ```

## 🎯 핵심 학습 포인트

### 1. 상속 관계 매핑 전략 선택
- **단일 테이블**: 성능 우선, 단순한 구조
- **조인 전략**: 정규화 우선, 복잡한 구조
- **구현 클래스별 테이블**: 클래스 간 독립성 중요

### 2. 연관 관계 설계 원칙
- **단방향 관계 우선**: 복잡도 감소
- **양방향 관계**: 꼭 필요한 경우만
- **연관 관계 주인**: 외래키가 있는 곳이 주인

### 3. N:N 관계 처리
- **직접 매핑**: 단순한 경우
- **연결 엔티티**: 추가 속성이 필요한 경우 (권장)

### 4. 성능 최적화
- **적절한 페치 전략**: 지연 vs 즉시 로딩
- **배치 페치**: N+1 문제 해결
- **쿼리 최적화**: 페치 조인 활용

## 실행 방법

### 🚀 Spring Boot 애플리케이션 실행

#### 1. 환경 변수 방식 (권장)
```bash
# 로컬 환경으로 실행  
SPRING_PROFILES_ACTIVE=local ./gradlew :chap15:bootRun
```

#### 2. Program Arguments 방식
```bash
# 기본 실행
./gradlew :chap15:bootRun --args='--spring.profiles.active=local'
```

#### 3. IDE 설정 방식
- IntelliJ IDEA: Run Configuration → VM Options 또는 Program Arguments 설정
- VM Options: `-Dspring.profiles.active=local`
- Program Arguments: `--spring.profiles.active=local`

## 🐳 인프라 설정

### Docker Compose 환경 설정

이 챕터는 **JPA + 캐싱 + 검색 인프라**를 사용합니다:

```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# JPA + 캐싱 + 검색용 Docker Compose 실행 (MariaDB + Redis + Elasticsearch)
docker-compose -f docker-compose.jpa.yml up -d

# 서비스 상태 확인
docker-compose -f docker-compose.jpa.yml ps

# 정리 (컨테이너 및 볼륨 삭제)
docker-compose -f docker-compose.jpa.yml down -v
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308) - 주 데이터베이스
- **Redis 7** (포트: 6380) - 캐싱 및 세션 저장소  
- **Elasticsearch 8.12.0** (포트: 9200, 9300) - 검색 엔진
- JPA 전용 데이터베이스 스키마 자동 생성

**애플리케이션 실행:**
```bash
# 인프라 시작 후 애플리케이션 실행
./gradlew :chap15:bootRun -Dspring.profiles.active=local
```

## 🔍 추가 학습 자료

- [JPA 공식 문서](https://jakarta.ee/specifications/persistence/)
- [Hibernate 공식 문서](https://hibernate.org/orm/documentation/)
- [Spring Data JPA 레퍼런스](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

---

**💡 주요 개념 요약**
- 객체의 상속 관계를 관계형 데이터베이스에 효율적으로 매핑
- 엔티티 간의 다양한 연관 관계를 JPA 어노테이션으로 표현
- 성능과 유지보수성을 고려한 매핑 전략 선택
- TestContainers를 활용한 실제 데이터베이스 환경에서의 테스트

## ✅ 최근 테스트 개선사항

### TestContainers 현대화 마이그레이션 완료

**Spring Boot 3.x 표준 방식으로 JPA 연관 관계 매핑 테스트 현대화:**

#### 마이그레이션된 테스트 파일들:
- `BaseHierarchyJpaTest`: 계층형 데이터 구조 JPA 매핑 테스트
- `BaseJpaTest`: JPA 기본 CRUD 및 영속성 컨텍스트 테스트
- `BaseManyToManyJpaTest`: 다대다 연관 관계 매핑 테스트  
- `BaseManyToOneJpaTest`: 다대일 연관 관계 매핑 테스트
- `BaseOneToManyJpaTest`: 일대다 연관 관계 매핑 테스트
- `BaseOneToOneJpaTest`: 일대일 연관 관계 매핑 테스트

#### 새로운 TestContainers 패턴 (현재 방식)
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("JPA 연관 관계 매핑 통합 테스트")
class BaseManyToManyJpaTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

    @Test
    @DisplayName("다대다 연관 관계 저장 및 조회 검증")
    void manyToManyRelationshipMapping() {
        User user = User.builder().email("test@example.com").build();
        Role role = Role.builder().name("ADMIN").build();
        
        user.addRole(role);
        userRepository.save(user);
        
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getName()).isEqualTo("ADMIN");
    }
}
```

#### 마이그레이션의 주요 개선 효과:
- **JPA 연관 관계 검증**: @OneToOne, @OneToMany, @ManyToOne, @ManyToMany 매핑 테스트
- **지연 로딩 검증**: FetchType.LAZY 및 프록시 객체 동작 확인
- **영속성 컨텍스트 검증**: 1차 캐시, 변경 감지, 쓰기 지연 동작 테스트
- **계층형 데이터 검증**: Self-referencing 관계 및 트리 구조 매핑 테스트
- **N+1 문제 해결 검증**: @EntityGraph, JOIN FETCH 쿼리 최적화 테스트