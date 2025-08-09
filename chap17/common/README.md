# Chapter 17 Common - 멀티모듈 공통 도메인 라이브러리

Spring Boot 교육용 프로젝트 Primavera의 Chapter 17 공통 모듈입니다. 멀티모듈 아키텍처에서 공유되는 도메인 모델을 제공하는 라이브러리 모듈입니다.

## 🎯 학습 목표

- **멀티모듈 아키텍처**: 공통 도메인 모델의 분리와 공유
- **도메인 주도 설계**: 순수 도메인 객체의 설계
- **의존성 관리**: 라이브러리 모듈의 의존성 최소화
- **공통 DTO**: 모듈 간 데이터 전송 객체 설계

## 📁 프로젝트 구조

```
chap17/common/
├── src/main/java/com/genius/primavera/common/
│   ├── domain/           # JPA 엔티티 (도메인 객체)
│   │   ├── Product.java      # 상품 엔티티
│   │   ├── Category.java     # 카테고리 엔티티
│   │   ├── Seller.java       # 판매자 엔티티
│   │   └── ProductStatus.java # 상품 상태 Enum
│   └── dto/              # 데이터 전송 객체
│       └── ProductDocument.java # Elasticsearch 문서 DTO
└── build.gradle         # 라이브러리 모듈 설정
```

## 🏗 아키텍처 특성

### 1. 라이브러리 모듈 (NO WEB)
```gradle
plugins {
    id 'java-library'  // 웹 애플리케이션이 아닌 라이브러리
}
```

### 2. 최소 의존성 원칙
- **JPA만 포함**: `spring-boot-starter-data-jpa`
- **웹 의존성 제외**: 순수 도메인 모델만 제공
- **비즈니스 로직 없음**: 데이터 구조만 정의

### 3. 공통 도메인 객체
```java
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Seller seller;
}
```

## 🎯 핵심 기능

### 1. 도메인 엔티티
- **Product**: 상품 정보 (가격, 상태, 카테고리, 판매자)
- **Category**: 상품 카테고리
- **Seller**: 판매자 정보
- **ProductStatus**: 상품 상태 (ACTIVE, INACTIVE, SOLD_OUT)

### 2. DTO 객체
- **ProductDocument**: Elasticsearch 인덱싱용 문서 구조
- **중첩 클래스**: CategoryInfo, SellerInfo (비정규화된 데이터)

## 🛠 기술 스택

- **Java**: 21 (LTS)
- **Spring Boot**: 3.3.6
- **JPA**: 도메인 엔티티 매핑
- **Jackson**: JSON 직렬화/역직렬화
- **Lombok**: 코드 생성

## 🚀 빌드 및 사용

### 라이브러리 빌드
```bash
# 공통 모듈 빌드 (다른 모듈에서 사용 가능)
./gradlew :chap17:common:build

# JAR 파일 생성
./gradlew :chap17:common:jar
```

### 의존성 사용 예시
```gradle
// 다른 모듈에서 사용
dependencies {
    implementation project(':chap17:common')
}
```

## 📋 테스트 실행

```bash
# 단위 테스트 실행
./gradlew :chap17:common:test

# 테스트 결과 확인
./gradlew :chap17:common:test --info
```

## 🎓 핵심 학습 포인트

### 1. 멀티모듈 아키텍처 패턴
- **공통 모듈 분리**: 중복 코드 제거와 일관성 확보
- **의존성 방향**: 상위 모듈이 공통 모듈에 의존
- **라이브러리 vs 애플리케이션**: 실행 가능한 JAR vs 라이브러리 JAR

### 2. 도메인 주도 설계 (DDD)
- **순수 도메인 객체**: 외부 기술에 독립적인 비즈니스 모델
- **엔티티**: 식별자를 가진 도메인 객체
- **값 객체**: ProductStatus와 같은 불변 값

### 3. 아키텍처 제약
```gradle
bootJar {
    enabled = false  // 실행 가능한 JAR 생성 안함
}

jar {
    enabled = true   // 라이브러리 JAR만 생성
    archiveClassifier = ''
}
```

## 📚 주요 애너테이션

### JPA 관련
- `@Entity`: JPA 엔티티 선언
- `@Id`, `@GeneratedValue`: 기본 키 설정
- `@ManyToOne`: 다대일 연관관계
- `@Enumerated`: Enum 타입 매핑

### Lombok
- `@Builder`: 빌더 패턴 자동 생성
- `@NoArgsConstructor`, `@AllArgsConstructor`: 생성자 자동 생성

### Jackson
- `@JsonProperty`: JSON 직렬화 필드명 지정
- `@JsonFormat`: 날짜/시간 포맷 지정

## 🔄 다음 단계

1. **chap17:batch** - Spring Batch를 사용한 배치 처리 모듈
2. **chap17:streaming** - WebFlux를 사용한 실시간 스트리밍 모듈
3. **chap18** - 마이크로서비스 아키텍처로의 확장

## 📖 관련 문서

- [Spring Boot Multi-Module Projects](https://spring.io/guides/gs/multi-module/)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)
- [JPA Entity Relationships](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)