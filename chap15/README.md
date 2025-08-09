# Chapter 15: JPA 관계 매핑 심화 (WEB 의존성 제거)

Spring Boot에서 JPA 고급 관계 매핑과 도메인 모델링을 심화 학습하는 **순수 JPA 모듈**입니다. 웹 의존성을 완전히 제거하고 JPA 자체의 복잡한 관계 매핑 패턴과 고급 영속성 기능에 집중합니다. HashiCorp Vault를 통한 설정 관리도 포함합니다.

## 🎯 고급 학습 목표

- **Complex Entity Relationships**: 복잡한 1:1, 1:N, N:M 관계 매핑 완전 정복
- **Inheritance Mapping**: 상속 관계 매핑 전략과 다형성 처리
- **Embedded Types**: 임베디드 타입과 값 객체 설계 패턴
- **Advanced JPA Features**: 컴포지트 키, 커스텀 타입, 고급 쿼리 기법
- **Domain-Driven Design**: DDD 패턴을 JPA로 구현하는 실무 기법
- **Vault Configuration**: 민감 정보의 중앙집중식 관리

## 📁 프로젝트 구조

```
chap15/
├── src/main/java/com/genius/primavera/
│   ├── JpaAdvancedMappingApplication.java    # 메인 애플리케이션 (WEB 없음)
│   ├── domain/                               # 순수 도메인 모델
│   │   ├── hierarchy/                        # 상속 관계 매핑
│   │   │   ├── Item.java                    # 추상 부모 엔티티
│   │   │   ├── Album.java                   # 앨범 상품 (단일 테이블)
│   │   │   ├── Book.java                    # 도서 상품 (조인 전략)
│   │   │   ├── Movie.java                   # 영화 상품 (테이블별 클래스)
│   │   │   ├── Family.java                  # 생물 분류 계층
│   │   │   ├── Canidae.java                 # 개과 (상속)
│   │   │   ├── Felidae.java                 # 고양이과 (상속)
│   │   │   └── Scincidae.java               # 도마뱀과 (상속)
│   │   └── relation/                        # 연관 관계 매핑
│   │       ├── oneToOne/                    # 1:1 관계 완전정복
│   │       │   ├── Member.java              # 회원 (주 테이블 FK)
│   │       │   ├── Address.java             # 주소 (대상 테이블 FK)
│   │       │   ├── Book.java                # 도서
│   │       │   ├── ISBN.java                # 국제표준도서번호 (1:1)
│   │       │   ├── Article.java             # 게시글
│   │       │   ├── Content.java             # 본문 (1:1 양방향)
│   │       │   ├── Product.java             # 상품
│   │       │   └── Serial.java              # 시리얼번호 (1:1 단방향)
│   │       ├── oneToMany/                   # 1:N 관계 패턴
│   │       │   ├── Customer.java            # 고객
│   │       │   ├── Contact.java             # 연락처 (1:N 단방향)
│   │       │   ├── Professor.java           # 교수
│   │       │   └── Student.java             # 학생 (1:N 양방향)
│   │       ├── manyToOne/                   # N:1 관계 최적화
│   │       │   ├── Employee.java            # 직원
│   │       │   ├── Department.java          # 부서 (N:1 양방향)
│   │       │   ├── Player.java              # 선수
│   │       │   └── Team.java                # 팀 (N:1 단방향)
│   │       └── manyToMany/                  # N:N 관계 고급패턴
│   │           ├── Publisher.java           # 출판사
│   │           ├── Subscriber.java          # 구독자 (N:N 단순)
│   │           ├── Buyer.java               # 구매자
│   │           ├── Seller.java              # 판매자
│   │           ├── Contract.java            # 계약서 (N:N 연결엔티티)
│   │           ├── Origin.java              # 발신지
│   │           ├── Destination.java         # 목적지
│   │           ├── Letter.java              # 편지 (복합키 연결엔티티)
│   │           ├── LetterId.java            # 편지 복합키
│   │           ├── Sender.java              # 발송자
│   │           └── Recipient.java           # 수신자
│   └── infrastructure/                      # 설정 및 인프라
│       └── configuration/
│           └── PrimaveraProperties.java     # Vault 연동 설정
└── src/main/resources/
    ├── application.yml                      # Vault 통합 설정
    ├── application-local.yml               # 로컬 개발 환경
    └── sql/                                # JPA DDL 스크립트
```

## 🔧 고급 기술 기능

### 1. 상속 관계 매핑 전략

#### 단일 테이블 전략 (SINGLE_TABLE)
```java
@Entity
@Table(name = "ITEM")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING)
public abstract class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "NAME")
    private String name;
    
    @Column(name = "PRICE")
    private BigDecimal price;
    
    // 공통 속성들...
}

@Entity
@DiscriminatorValue("ALBUM")
public class Album extends Item {
    
    @Column(name = "ARTIST")
    private String artist;
    
    @Column(name = "GENRE")
    private String genre;
    
    // 앨범 특화 속성들...
}

@Entity
@DiscriminatorValue("BOOK")
public class Book extends Item {
    
    @Column(name = "AUTHOR")
    private String author;
    
    @Column(name = "ISBN")
    private String isbn;
    
    // 도서 특화 속성들...
}
```

#### 조인 전략 (JOINED)
```java
@Entity
@Table(name = "FAMILY")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Family {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "SCIENTIFIC_NAME")
    private String scientificName;
    
    @Column(name = "COMMON_NAME")
    private String commonName;
}

@Entity
@Table(name = "CANIDAE")
@PrimaryKeyJoinColumn(name = "FAMILY_ID")
public class Canidae extends Family {
    
    @Column(name = "PACK_BEHAVIOR")
    private Boolean packBehavior;
    
    @Column(name = "HUNTING_STYLE")
    private String huntingStyle;
}

@Entity
@Table(name = "FELIDAE")
@PrimaryKeyJoinColumn(name = "FAMILY_ID")
public class Felidae extends Family {
    
    @Column(name = "RETRACTABLE_CLAWS")
    private Boolean retractableClaws;
    
    @Column(name = "NIGHT_VISION")
    private Boolean nightVision;
}
```

### 2. 1:1 관계 고급 매핑

#### 주 테이블에 외래키 (양방향)
```java
@Entity
@Table(name = "MEMBER")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "USERNAME")
    private String username;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADDRESS_ID", unique = true)
    private Address address;
    
    // 연관관계 편의 메서드
    public void setAddress(Address address) {
        this.address = address;
        if (address != null) {
            address.setMember(this);
        }
    }
}

@Entity
@Table(name = "ADDRESS")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "CITY")
    private String city;
    
    @Column(name = "STREET")
    private String street;
    
    @OneToOne(mappedBy = "address", fetch = FetchType.LAZY)
    private Member member;
}
```

#### 대상 테이블에 외래키
```java
@Entity
@Table(name = "ARTICLE")
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "TITLE")
    private String title;
    
    @OneToOne(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Content content;
}

@Entity
@Table(name = "CONTENT")
public class Content {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Lob
    @Column(name = "BODY", columnDefinition = "LONGTEXT")
    private String body;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ARTICLE_ID", unique = true)
    private Article article;
}
```

### 3. N:M 관계 연결 엔티티 패턴

#### 복합키를 가진 연결 엔티티
```java
@Entity
@Table(name = "LETTER")
public class Letter {
    
    @EmbeddedId
    private LetterId id = new LetterId();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("senderId")
    @JoinColumn(name = "SENDER_ID")
    private Sender sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipientId")
    @JoinColumn(name = "RECIPIENT_ID")
    private Recipient recipient;
    
    @Column(name = "SENT_DATE")
    private LocalDateTime sentDate;
    
    @Column(name = "SUBJECT")
    private String subject;
    
    @Lob
    @Column(name = "CONTENT")
    private String content;
    
    // 연관관계 편의 메서드
    public void setSender(Sender sender) {
        this.sender = sender;
        this.id.setSenderId(sender.getId());
        sender.getSentLetters().add(this);
    }
    
    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
        this.id.setRecipientId(recipient.getId());
        recipient.getReceivedLetters().add(this);
    }
}

@Embeddable
public class LetterId implements Serializable {
    
    @Column(name = "SENDER_ID")
    private Long senderId;
    
    @Column(name = "RECIPIENT_ID")
    private Long recipientId;
    
    // equals, hashCode 구현 필수
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LetterId)) return false;
        LetterId letterId = (LetterId) o;
        return Objects.equals(senderId, letterId.senderId) &&
               Objects.equals(recipientId, letterId.recipientId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(senderId, recipientId);
    }
}
```

### 4. 임베디드 타입 (Embedded Types)
```java
@Entity
@Table(name = "CONTACT")
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "NAME")
    private String name;
    
    @Embedded
    private Address homeAddress;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "city", column = @Column(name = "WORK_CITY")),
        @AttributeOverride(name = "street", column = @Column(name = "WORK_STREET")),
        @AttributeOverride(name = "zipcode", column = @Column(name = "WORK_ZIPCODE"))
    })
    private Address workAddress;
    
    @Embedded
    private Email email;
    
    @Embedded
    private Mobile mobile;
}

@Embeddable
public class Address {
    
    @Column(name = "CITY")
    private String city;
    
    @Column(name = "STREET")
    private String street;
    
    @Column(name = "ZIPCODE")
    private String zipcode;
    
    // 생성자, 메서드들...
}

@Embeddable
public class Email {
    
    @Column(name = "EMAIL")
    private String address;
    
    public boolean isValid() {
        return address != null && address.contains("@");
    }
}
```

### 5. HashiCorp Vault 설정 관리
```java
@ConfigurationProperties("primavera")
@Component
public class PrimaveraProperties {
    
    private Database database = new Database();
    private Security security = new Security();
    
    public static class Database {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        // getters/setters...
    }
    
    public static class Security {
        private String jwtSecret;
        private Long jwtExpiration;
        // getters/setters...
    }
}
```

## 🛠️ 기술 스택

### Core Framework (WEB 제외)
- **Spring Boot**: 3.3.6 (spring-boot-starter-web 제외)
- **Spring Data JPA**: 고급 관계 매핑
- **Hibernate**: JPA 구현체
- **Spring Boot Starter AOP**: 관점 지향 프로그래밍

### Configuration Management
- **Spring Cloud Vault Config**: 설정 중앙화
- **HashiCorp Vault**: 민감 정보 관리

### Database & Persistence
- **MariaDB**: 11.4.7 (관계형 데이터베이스)
- **HikariCP**: 커넥션 풀

### Development & Testing
- **Lombok**: 보일러플레이트 코드 제거
- **TestContainers**: 통합 테스트 환경
- **JUnit 5**: 단위 테스트 프레임워크

## 🚀 실행 방법

### 1. HashiCorp Vault 설정
```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# JPA + Vault 인프라 실행
docker-compose -f docker-compose.vault.yml up -d

# Vault 초기화 확인
docker logs vault-primavera

# 시크릿 저장
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=primavera-dev-token

vault kv put secret/primavera/chap15 \
  database.password=primavera \
  security.jwt-secret=my-secret-key-for-jwt-token
```

### 2. 데이터베이스 설정
```bash
# MariaDB 실행 (docker-compose.vault.yml에 포함)
# 포트: 3308
# 데이터베이스: primavera
# 사용자: primavera/primavera
```

### 3. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행 (WEB 없음)
./gradlew :chap15:bootRun -Dspring.profiles.active=local

# 또는 환경 변수 방식
SPRING_PROFILES_ACTIVE=local ./gradlew :chap15:bootRun
```

### 4. JPA 테스트 실행
```bash
# 전체 테스트 실행
./gradlew :chap15:test

# 특정 관계 매핑 테스트
./gradlew :chap15:test --tests="*OneToOneTest"
./gradlew :chap15:test --tests="*ManyToManyTest"
./gradlew :chap15:test --tests="*HierarchyTest"
```

## 🎓 핵심 고급 학습 포인트

### 1. 상속 관계 매핑 전략
- **SINGLE_TABLE**: 성능 최적화, 단순 구조
- **JOINED**: 정규화 우선, 복잡한 상속 구조
- **TABLE_PER_CLASS**: 각 클래스별 독립적 테이블

### 2. 연관 관계 설계 원칙
- **단방향 우선**: 양방향은 꼭 필요한 경우만
- **지연 로딩 기본**: 즉시 로딩은 신중하게
- **연관 관계 주인**: 외래키를 관리하는 엔티티

### 3. N:N 관계 처리 전략
- **단순 N:N**: @JoinTable 사용
- **연결 엔티티**: 추가 속성이 필요한 경우
- **복합키 연결 엔티티**: 복잡한 비즈니스 로직

### 4. 임베디드 타입 활용
- **값 객체 설계**: DDD의 Value Object 구현
- **재사용성**: 공통 속성들의 모듈화
- **캡슐화**: 관련 속성들의 논리적 그룹핑

### 5. 성능 최적화
- **fetch 전략 최적화**: LAZY vs EAGER
- **N+1 문제 해결**: @EntityGraph, JOIN FETCH
- **배치 크기 조정**: @BatchSize 활용

### 6. Vault 설정 관리
- **민감 정보 보호**: DB 패스워드, API 키 중앙 관리
- **환경별 설정**: 개발/스테이징/운영 설정 분리
- **동적 설정 갱신**: 애플리케이션 재시작 없이 설정 변경

## 🧪 테스트 실행

### 단위 테스트
```bash
./gradlew :chap15:test
```

### 통합 테스트 (TestContainers)
```bash
./gradlew :chap15:test --tests="*IntegrationTest"
```

### JPA 관계 매핑 테스트
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("JPA 복합 관계 매핑 테스트")
class ComplexRelationMappingTest {

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
    }

    @Test
    @DisplayName("복합키를 가진 N:N 연결 엔티티 테스트")
    void shouldHandleCompositeKeyManyToManyRelation() {
        // Given
        Sender sender = new Sender("sender@example.com");
        Recipient recipient = new Recipient("recipient@example.com");
        
        Letter letter = new Letter();
        letter.setSender(sender);
        letter.setRecipient(recipient);
        letter.setSubject("Test Letter");
        letter.setSentDate(LocalDateTime.now());
        
        // When
        letterRepository.save(letter);
        
        // Then
        Letter savedLetter = letterRepository.findById(letter.getId()).orElseThrow();
        assertThat(savedLetter.getSender().getEmail()).isEqualTo("sender@example.com");
        assertThat(savedLetter.getRecipient().getEmail()).isEqualTo("recipient@example.com");
        assertThat(savedLetter.getSubject()).isEqualTo("Test Letter");
    }
    
    @Test
    @DisplayName("상속 관계 매핑 다형성 쿼리 테스트")
    void shouldQueryPolymorphicEntities() {
        // Given
        Album album = new Album("Jazz Collection", new BigDecimal("29.99"));
        album.setArtist("Miles Davis");
        
        Book book = new Book("Clean Code", new BigDecimal("39.99"));
        book.setAuthor("Robert Martin");
        
        itemRepository.save(album);
        itemRepository.save(book);
        
        // When: 다형성 쿼리 실행
        List<Item> items = itemRepository.findAll();
        
        // Then
        assertThat(items).hasSize(2);
        assertThat(items).hasExactlyElementsOfTypes(Album.class, Book.class);
    }
}
```

### 임베디드 타입 테스트
```java
@Test
@DisplayName("임베디드 타입 매핑 테스트")
void shouldMapEmbeddedTypes() {
    // Given
    Contact contact = new Contact();
    contact.setName("John Doe");
    
    Address homeAddress = new Address("Seoul", "Gangnam-gu", "12345");
    Address workAddress = new Address("Seoul", "Jongno-gu", "54321");
    
    contact.setHomeAddress(homeAddress);
    contact.setWorkAddress(workAddress);
    
    Email email = new Email("john@example.com");
    Mobile mobile = new Mobile("010-1234-5678");
    
    contact.setEmail(email);
    contact.setMobile(mobile);
    
    // When
    contactRepository.save(contact);
    
    // Then
    Contact savedContact = contactRepository.findById(contact.getId()).orElseThrow();
    assertThat(savedContact.getHomeAddress().getCity()).isEqualTo("Seoul");
    assertThat(savedContact.getWorkAddress().getStreet()).isEqualTo("Jongno-gu");
    assertThat(savedContact.getEmail().getAddress()).isEqualTo("john@example.com");
}
```

## 📚 학습 순서

1. **상속 관계 매핑 마스터**
   - 각 전략별 특성과 적용 상황
   - 다형성 쿼리와 성능 특성
   - 구분자 컬럼과 조인 전략

2. **복잡한 연관 관계 설계**
   - 1:1, 1:N, N:1, N:M 모든 패턴
   - 양방향 연관관계 관리
   - 연관관계 편의 메서드 작성

3. **임베디드 타입 활용**
   - 값 객체(Value Object) 설계
   - @AttributeOverride 활용
   - 재사용 가능한 컴포넌트 설계

4. **고급 매핑 기법**
   - 복합키와 식별 관계
   - 상속과 연관관계 조합
   - 성능을 고려한 매핑 전략

5. **실무 설계 패턴**
   - DDD의 엔티티/값객체 구분
   - 애그리거트 루트 설계
   - 도메인 서비스 패턴

## 🔧 주요 고급 애너테이션

| 애너테이션 | 용도 | 사용 예시 |
|-----------|------|----------|
| `@Inheritance(strategy = InheritanceType.JOINED)` | 상속 전략 정의 | 부모 엔티티 |
| `@DiscriminatorColumn` | 구분자 컬럼 정의 | 단일 테이블 전략 |
| `@PrimaryKeyJoinColumn` | 기본키 조인 컬럼 | 조인 전략 |
| `@EmbeddedId` | 복합키 임베디드 | 연결 엔티티 |
| `@MapsId` | 식별 관계 매핑 | 복합키 연관관계 |
| `@AttributeOverride` | 임베디드 속성 재정의 | 임베디드 타입 |
| `@JoinTable` | 조인 테이블 정의 | N:N 관계 |

## 🔄 다음 단계

**Chapter 16 (파일 처리, S3, Excel, 모니터링)**로 진행하여 다음 내용을 학습하세요:

- AWS S3 연동과 클라우드 파일 저장소
- Excel 파일 처리와 대용량 데이터 핸들링
- Spring Boot Actuator 모니터링
- 파일 업로드/다운로드 최적화
- 클라우드 네이티브 애플리케이션 패턴

---

이 모듈을 통해 JPA의 가장 복잡한 관계 매핑 패턴을 완전히 이해하고, 실무에서 마주치는 복잡한 도메인 모델을 효과적으로 설계할 수 있는 역량을 기를 수 있습니다. 특히 웹 레이어 없이 순수 도메인 로직에만 집중할 수 있어 JPA 자체의 깊이 있는 학습이 가능합니다.