# Chapter 07 - JPA & MyBatis Hybrid Architecture

Spring Data JPA와 MyBatis를 함께 사용하는 하이브리드 데이터 접근 아키텍처를 구축합니다. 각 기술의 장점을 살려 복잡한 쿼리는 MyBatis로, 간단한 CRUD는 JPA로 처리하는 실용적인 접근법을 학습하고, Thymeleaf 템플릿과 Admin LTE를 활용한 관리자 대시보드를 개발합니다.

## 학습 목표

- **하이브리드 아키텍처**: JPA와 MyBatis를 함께 사용하는 설계 패턴 학습
- **Spring Data JPA**: Repository 패턴과 Query Methods 활용
- **JPA 심화**: Entity 관계 매핑, 영속성 컨텍스트, 트랜잭션 관리
- **복합 뷰 시스템**: Thymeleaf 레이아웃과 Admin LTE 통합
- **관리자 대시보드**: 실무형 백오피스 시스템 구축

## 프로젝트 구조

```
src/main/java/com/genius/primavera/
├── ThymeleafWebApplication.java           # 메인 애플리케이션
├── application/                         # 애플리케이션 서비스 계층
│   ├── UserService.java                 # 사용자 비즈니스 로직 (JPA 활용)
│   ├── PostService.java                 # 게시글 비즈니스 로직 (MyBatis 활용)
│   ├── DashboardService.java            # 대시보드 통계 서비스
│   └── hybrid/                          # 하이브리드 패턴 구현
│       ├── HybridUserService.java       # JPA + MyBatis 조합 서비스
│       └── TransactionManagerConfig.java # 복합 트랜잭션 설정
├── config/                              # 설정 클래스
│   ├── JpaConfig.java                   # JPA 설정
│   ├── MyBatisConfig.java              # MyBatis 설정
│   ├── WebSecurityConfig.java          # 보안 설정
│   └── ThymeleafConfig.java            # 템플릿 엔진 설정
├── domain/                              # 도메인 계층
│   ├── entity/                         # JPA 엔티티
│   │   ├── User.java                   # 사용자 엔티티 (JPA)
│   │   ├── Post.java                   # 게시글 엔티티 (JPA)
│   │   ├── Category.java               # 카테고리 엔티티 (JPA)
│   │   └── Comment.java                # 댓글 엔티티 (JPA)
│   ├── repository/                     # JPA Repository
│   │   ├── UserRepository.java         # 사용자 Repository
│   │   ├── PostRepository.java         # 게시글 Repository
│   │   └── CommentRepository.java      # 댓글 Repository
│   ├── mapper/                         # MyBatis 매퍼
│   │   ├── PostStatisticsMapper.java   # 게시글 통계 조회
│   │   ├── UserStatisticsMapper.java   # 사용자 통계 조회
│   │   └── DashboardMapper.java        # 대시보드 데이터 조회
│   └── dto/                            # 복합 조회용 DTO
│       ├── PostStatisticsDto.java      # 게시글 통계 DTO
│       ├── UserStatisticsDto.java      # 사용자 통계 DTO
│       └── DashboardDto.java           # 대시보드 DTO
└── interfaces/                         # 인터페이스 계층
    ├── api/                            # REST API 컨트롤러
    │   ├── UserApiController.java      # 사용자 API
    │   ├── PostApiController.java      # 게시글 API
    │   └── DashboardApiController.java # 대시보드 API
    └── web/                            # 웹 페이지 컨트롤러
        ├── HomeController.java         # 메인 페이지
        ├── UserWebController.java      # 사용자 관리 페이지
        ├── PostWebController.java      # 게시글 관리 페이지
        └── AdminController.java        # 관리자 대시보드

src/main/resources/
├── META-INF/persistence.xml             # JPA 설정
├── application-local.yml                # 로컬 개발 설정
├── application.yml                      # 기본 애플리케이션 설정
├── static/                             # 정적 리소스 (Admin LTE)
│   ├── bower_components/               # 의존성 라이브러리
│   ├── dist/                          # Admin LTE 배포판
│   └── plugins/                       # 플러그인
└── templates/                          # Thymeleaf 템플릿
    ├── layouts/                        # 레이아웃 템플릿
    │   └── layout.html                 # 기본 레이아웃
    ├── fragments/                      # 공통 프래그먼트
    │   ├── header.html                 # 헤더
    │   ├── aside.html                  # 사이드바
    │   └── footer.html                 # 푸터
    ├── index.html                      # 대시보드 메인
    └── login.html                      # 로그인 페이지
```

## 주요 기능

### 1. JPA Entity 관계 매핑
```java
@Entity
@Table(name = "POST")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Lob
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

@Entity
@Table(name = "COMMENT")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Lob
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID")
    private Post post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User author;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### 2. Spring Data JPA Repository
```java
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    // Query Methods
    List<Post> findByAuthorId(Long authorId);
    
    List<Post> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);
    
    Page<Post> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(
        String title, Pageable pageable);
    
    // @Query 어노테이션 활용
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startDate")
    List<Post> findRecentPosts(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT p FROM Post p JOIN FETCH p.author JOIN FETCH p.category WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);
    
    // 네이티브 쿼리
    @Query(value = "SELECT * FROM POST WHERE MATCH(title, content) AGAINST(:keyword IN NATURAL LANGUAGE MODE)", 
           nativeQuery = true)
    List<Post> findByFullTextSearch(@Param("keyword") String keyword);
    
    // 사용자 정의 메서드
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") Long id);
}
```

### 3. MyBatis 복잡 쿼리 처리
```java
@Mapper
public interface PostStatisticsMapper {
    
    @Select("""
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as postCount,
            COUNT(DISTINCT user_id) as uniqueAuthors
        FROM POST 
        WHERE created_at >= #{startDate}
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
        """)
    List<PostStatisticsDto> getDailyPostStatistics(@Param("startDate") LocalDate startDate);
    
    @Select("""
        SELECT 
            c.name as categoryName,
            COUNT(p.id) as postCount,
            AVG(p.view_count) as avgViewCount,
            MAX(p.created_at) as lastPostDate
        FROM POST p
        JOIN CATEGORY c ON p.category_id = c.id
        WHERE p.created_at >= #{startDate}
        GROUP BY c.id, c.name
        ORDER BY postCount DESC
        """)
    List<CategoryStatisticsDto> getCategoryStatistics(@Param("startDate") LocalDate startDate);
}
```

### 4. 하이브리드 서비스 구현
```java
@Service
@Transactional(readOnly = true)
public class HybridUserService {
    
    private final UserRepository userRepository; // JPA Repository
    private final UserStatisticsMapper userStatisticsMapper; // MyBatis Mapper
    
    @Transactional
    public User createUser(User user) {
        // JPA로 간단한 CRUD 처리
        return userRepository.save(user);
    }
    
    public UserStatisticsDto getUserStatistics(Long userId) {
        // MyBatis로 복잡한 통계 조회
        return userStatisticsMapper.getUserDetailStatistics(userId);
    }
    
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        // JPA Specification으로 동적 검색
        Specification<User> spec = (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(
                cb.like(cb.lower(root.get("nickname")), "%" + keyword.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")
            );
        };
        
        return userRepository.findAll(spec, pageable);
    }
    
    @Transactional
    public void deactivateInactiveUsers(int daysSinceLastLogin) {
        // MyBatis로 복잡한 조건의 배치 업데이트
        userStatisticsMapper.deactivateInactiveUsers(daysSinceLastLogin);
    }
}
```

### 5. Thymeleaf 레이아웃 시스템
```html
<!-- layouts/layout.html -->
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org" 
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title th:text="${title ?: 'Primavera Admin'}">Admin LTE 3</title>
    
    <!-- Google Font: Source Sans Pro -->
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,400i,700&display=fallback">
    <!-- Font Awesome -->
    <link rel="stylesheet" th:href="@{/plugins/fontawesome-free/css/all.min.css}">
    <!-- Theme style -->
    <link rel="stylesheet" th:href="@{/dist/css/adminlte.min.css}">
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">
    
    <!-- 헤더 -->
    <div th:replace="~{fragments/header :: header}"></div>
    
    <!-- 사이드바 -->
    <div th:replace="~{fragments/aside :: aside}"></div>
    
    <!-- 콘텐츠 래퍼 -->
    <div class="content-wrapper">
        <div layout:fragment="content">
            <!-- 페이지별 콘텐츠가 여기에 삽입됩니다 -->
        </div>
    </div>
    
    <!-- 푸터 -->
    <div th:replace="~{fragments/footer :: footer}"></div>
</div>

<!-- jQuery -->
<script th:src="@{/plugins/jquery/jquery.min.js}"></script>
<!-- Bootstrap 4 -->
<script th:src="@{/plugins/bootstrap/js/bootstrap.bundle.min.js}"></script>
<!-- AdminLTE App -->
<script th:src="@{/dist/js/adminlte.min.js}"></script>
</body>
</html>
```

### 6. 대시보드 페이지
```html
<!-- index.html -->
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org" 
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layouts/layout}">

<div layout:fragment="content">
    <!-- Content Header -->
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-6">
                    <h1 class="m-0">대시보드</h1>
                </div>
            </div>
        </div>
    </div>

    <!-- Main content -->
    <section class="content">
        <div class="container-fluid">
            <!-- Small boxes (Stat box) -->
            <div class="row">
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-info">
                        <div class="inner">
                            <h3 th:text="${dashboardStats.totalUsers}">150</h3>
                            <p>전체 사용자</p>
                        </div>
                        <div class="icon">
                            <i class="fas fa-users"></i>
                        </div>
                        <a href="/admin/users" class="small-box-footer">
                            더보기 <i class="fas fa-arrow-circle-right"></i>
                        </a>
                    </div>
                </div>
                
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-success">
                        <div class="inner">
                            <h3 th:text="${dashboardStats.totalPosts}">53</h3>
                            <p>전체 게시글</p>
                        </div>
                        <div class="icon">
                            <i class="fas fa-file-alt"></i>
                        </div>
                        <a href="/admin/posts" class="small-box-footer">
                            더보기 <i class="fas fa-arrow-circle-right"></i>
                        </a>
                    </div>
                </div>
            </div>

            <!-- 차트 섹션 -->
            <div class="row">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">월별 게시글 통계</h3>
                        </div>
                        <div class="card-body">
                            <canvas id="monthlyPostChart" width="400" height="200"></canvas>
                        </div>
                    </div>
                </div>
                
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">카테고리별 분포</h3>
                        </div>
                        <div class="card-body">
                            <canvas id="categoryChart" width="400" height="200"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
</div>

<script layout:fragment="scripts">
<script th:src="@{/plugins/chart.js/Chart.min.js}"></script>
<script th:inline="javascript">
/*<![CDATA[*/
var monthlyData = /*[[${monthlyPostData}]]*/ [];
var categoryData = /*[[${categoryData}]]*/ [];

// 월별 게시글 차트
var ctx1 = document.getElementById('monthlyPostChart').getContext('2d');
var monthlyChart = new Chart(ctx1, {
    type: 'line',
    data: {
        labels: monthlyData.map(item => item.month),
        datasets: [{
            label: '게시글 수',
            data: monthlyData.map(item => item.count),
            borderColor: 'rgb(75, 192, 192)',
            backgroundColor: 'rgba(75, 192, 192, 0.1)',
            tension: 0.4
        }]
    }
});
/*]]>*/
</script>
</div>
</html>
```

## 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| **Spring Boot** | 3.3.6 | 기본 프레임워크 |
| **Spring Data JPA** | 3.3.6 | JPA 기반 데이터 접근 |
| **Hibernate** | 6.6.3 | JPA 구현체 |
| **MyBatis** | 3.0.4 | SQL 매핑 프레임워크 |
| **Thymeleaf** | 3.4.0 | 서버사이드 템플릿 |
| **Admin LTE** | 3.x | 관리자 대시보드 템플릿 |
| **MariaDB** | 11.4.7 | 관계형 데이터베이스 |
| **HikariCP** | 5.1.0 | 커넥션 풀 |

## 실행 방법

### 1. 데이터베이스 준비
```bash
# Docker로 MariaDB 실행
./docker-manager.sh start chap07

# 수동 실행
docker run -d --name mariadb-chap07 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3308:3306 mariadb:11.4.7
```

### 2. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap07:bootRun -Dspring.profiles.active=local

# JPA DDL 자동 생성 활성화
./gradlew :chap07:bootRun -Dspring.profiles.active=local \
  -Dspring.jpa.hibernate.ddl-auto=create
```

### 3. 웹 인터페이스 접근
```bash
# 브라우저에서 접근
http://localhost:8080/         # 대시보드 메인
http://localhost:8080/login    # 로그인 페이지
http://localhost:8080/admin    # 관리자 페이지
```

### 4. API 테스트
```bash
# 게시글 생성 (JPA)
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Post",
    "content": "This is a test post content",
    "authorId": 1,
    "categoryId": 1
  }'

# 게시글 통계 조회 (MyBatis)
curl -X GET "http://localhost:8080/api/statistics/posts/daily?startDate=2024-01-01"

# 사용자 검색 (JPA Specification)
curl -X GET "http://localhost:8080/api/users/search?keyword=admin&page=0&size=10"
```

## 핵심 학습 포인트

### 1. 하이브리드 아키텍처 설계
- **적재적소 활용**: 간단한 CRUD는 JPA, 복잡한 쿼리는 MyBatis
- **트랜잭션 통합**: 두 기술을 하나의 트랜잭션으로 통합 관리
- **성능 최적화**: 각 기술의 장점을 살린 성능 최적화
- **코드 일관성**: 하이브리드 환경에서의 코딩 표준 정립

### 2. JPA 고급 기능
- **엔티티 관계 매핑**: @OneToMany, @ManyToOne 등 연관관계 매핑
- **영속성 컨텍스트**: 1차 캐시, 변경 감지, 지연 로딩 이해
- **Query Methods**: 메서드명으로 쿼리 자동 생성
- **Specification**: 동적 쿼리 생성을 위한 JPA Criteria API
- **커스텀 Repository**: 복잡한 비즈니스 로직을 위한 커스텀 구현

### 3. 템플릿 시스템 아키텍처
- **Layout Dialect**: Thymeleaf Layout Dialect를 통한 레이아웃 시스템
- **Fragment 재사용**: 공통 UI 컴포넌트의 모듈화
- **데이터 바인딩**: 서버 데이터와 템플릿의 효율적 연동
- **국제화 지원**: 다국어 관리자 인터페이스 구축

### 4. 관리자 대시보드 패턴
- **Admin LTE 통합**: 현업에서 많이 사용하는 관리자 템플릿 활용
- **실시간 통계**: AJAX를 활용한 실시간 데이터 업데이트
- **Chart.js 연동**: 데이터 시각화를 위한 차트 라이브러리 통합
- **반응형 디자인**: 다양한 디바이스 지원

## 테스트 실행

### 단위 테스트
```bash
# 전체 테스트 실행
./gradlew :chap07:test

# JPA 관련 테스트만 실행
./gradlew :chap07:test --tests "*JpaTest"

# MyBatis 관련 테스트만 실행
./gradlew :chap07:test --tests "*MyBatisTest"
```

### 통합 테스트
```bash
# 하이브리드 서비스 테스트
./gradlew :chap07:test --tests "*HybridServiceTest"

# 웹 MVC 테스트
./gradlew :chap07:test --tests "*WebMvcTest"
```

## 설정 관리

### application-local.yml
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3308/primavera
    username: primavera
    password: primavera
    driver-class-name: org.mariadb.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: create-drop  # 개발 시에만 사용
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        format_sql: true
        show_sql: true
        use_sql_comments: true
    show-sql: true
    
mybatis:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
    lazy-loading-enabled: true
    aggressive-lazy-loading: false
    
logging:
  level:
    com.genius.primavera: DEBUG
    org.springframework.orm.jpa: DEBUG
    org.mybatis: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## 주요 애너테이션

| 애너테이션 | 용도 | 예시 |
|------------|------|------|
| `@Entity` | JPA 엔티티 클래스 | `@Entity @Table(name = "USER")` |
| `@ManyToOne` | 다대일 관계 매핑 | `@ManyToOne(fetch = FetchType.LAZY)` |
| `@OneToMany` | 일대다 관계 매핑 | `@OneToMany(cascade = CascadeType.ALL)` |
| `@Query` | JPQL/네이티브 쿼리 | `@Query("SELECT u FROM User u WHERE...")` |
| `@Modifying` | 데이터 수정 쿼리 | `@Modifying @Query("UPDATE User...")` |
| `@Mapper` | MyBatis 매퍼 인터페이스 | `@Mapper public interface UserMapper` |

## 학습 순서

1. **JPA 기본 설정**: Entity, Repository 기본 구성
2. **MyBatis 설정**: Mapper 인터페이스 및 설정 구성
3. **하이브리드 서비스 구현**: 두 기술을 조합한 서비스 계층 구축
4. **엔티티 관계 매핑**: JPA 연관관계 설정 및 최적화
5. **복잡 쿼리 구현**: MyBatis를 활용한 통계/분석 쿼리
6. **Thymeleaf 레이아웃**: 관리자 대시보드 UI 구축
7. **통합 테스트**: 전체 시스템 동작 검증

## 활용 방법

### 1. 기술 선택 기준
- **JPA 적합한 경우**: 단순 CRUD, 객체 중심 설계, 빠른 개발
- **MyBatis 적합한 경우**: 복잡한 조인 쿼리, 성능 최적화, SQL 제어

### 2. 성능 최적화 전략
- **N+1 문제 해결**: Fetch Join, Entity Graph, Batch Size 조정
- **2차 캐시 활용**: JPA 2차 캐시와 MyBatis 캐시 전략
- **읽기 전용 최적화**: @Transactional(readOnly = true) 활용

이 모듈은 실무에서 자주 사용하는 JPA + MyBatis 하이브리드 아키텍처와 현업 수준의 관리자 대시보드 구축 방법을 종합적으로 학습할 수 있는 실무형 예제입니다.