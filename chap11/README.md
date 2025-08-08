## chap11

### Test
* https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
* MockMvc
* WithUserDetails
* MockBean

### Thymeleaf
* https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html

### Pagination
* 페이지는 전체 아이템 수, 현재 페이지, 페이지 당 아이템, 페이지 노출 수를 이용

#### Paged
* 페이징을 위한 정보와 아이템을 관리

#### PageRequest
* 페이징을 위한 기본 정보를 관리

#### PageRequest
* 페이징을 위한 기본 정보를 관리

### wysihtml5 에디터 적용

## 실행 방법

### 🚀 Spring Boot 애플리케이션 실행

#### 1. 환경 변수 방식 (권장)
```bash
# 로컬 환경으로 실행  
SPRING_PROFILES_ACTIVE=local ./gradlew :chap11:bootRun
```

#### 2. Program Arguments 방식
```bash
# 기본 실행
./gradlew :chap11:bootRun --args='--spring.profiles.active=local'
```

#### 3. IDE 설정 방식
- IntelliJ IDEA: Run Configuration → VM Options 또는 Program Arguments 설정
- VM Options: `-Dspring.profiles.active=local`
- Program Arguments: `--spring.profiles.active=local`

## 🐳 인프라 설정

### Docker Compose 환경 설정

이 챕터는 **MyBatis + 보안 인프라**를 사용합니다:

```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# MyBatis + 보안 학습용 Docker Compose 실행 (MariaDB)
docker-compose -f docker-compose.mybatis.yml up -d

# 서비스 상태 확인
docker-compose -f docker-compose.mybatis.yml ps

# 정리 (컨테이너 및 볼륨 삭제)
docker-compose -f docker-compose.mybatis.yml down -v
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308)
- MyBatis 전용 데이터베이스 스키마 자동 생성

**애플리케이션 실행:**
```bash
# 인프라 시작 후 애플리케이션 실행
./gradlew :chap11:bootRun -Dspring.profiles.active=local
```

## ✅ 최근 테스트 개선사항

### TestContainers 현대화 마이그레이션 완료

**Spring Boot 3.x 표준 방식으로 OAuth2 소셜 로그인 테스트 현대화:**

#### 마이그레이션된 테스트 파일들:
- `PostControllerTest`: OAuth2 인증 기반 포스트 관리 통합 테스트
- `PostMockControllerTest`: OAuth2 인증 모킹을 활용한 컨트롤러 단위 테스트

#### 새로운 TestContainers 패턴 (현재 방식)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")  
@DisplayName("OAuth2 소셜 로그인 포스트 컨트롤러 테스트")
class PostControllerTest {

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
    @WithMockUser(roles = "USER")
    @DisplayName("인증된 사용자의 포스트 작성")
    void authenticatedUserCreatePost() {
        Post post = Post.builder()
            .title("OAuth2 테스트 포스트")
            .content("소셜 로그인으로 작성된 포스트")
            .build();
            
        ResponseEntity<Post> response = restTemplate.postForEntity("/posts", post, Post.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

#### 마이그레이션의 주요 개선 효과:
- **OAuth2 인증 플로우 검증**: 카카오/구글 소셜 로그인 통합 테스트
- **인증 컨텍스트 테스트**: Spring Security OAuth2 인증 정보 검증
- **소셜 사용자 프로필 매핑**: 외부 제공자 사용자 정보 연동 테스트
- **권한 기반 접근 제어**: OAuth2 스코프별 접근 권한 검증
- **토큰 기반 인증**: JWT/OAuth2 토큰 처리 및 검증