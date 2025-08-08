## chap08

### ApplicationConfiguration
* ApplicationConfiguration.java 어플리케이션 설정

### Filter
* org.springframework.boot.web.servlet.filter 패키지
```
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] .s.b.w.s.f.OrderedHiddenHttpMethodFilter : Filter 'hiddenHttpMethodFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] o.s.b.w.s.f.OrderedRequestContextFilter  : Filter 'requestContextFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] s.b.w.s.f.OrderedCharacterEncodingFilter : Filter 'characterEncodingFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] o.s.b.w.s.f.OrderedFormContentFilter     : Filter 'formContentFilter' configured for use
2019-04-16 19:14:36.001 DEBUG 48150 --- [  restartedMain] io.undertow                              : starting undertow server io.undertow.Undertow@b3f2d5b
``` 

### lucy-xss-filter
* [참고](https://github.com/naver/lucy-xss-filter)
```
@Bean
public FilterRegistrationBean<XssEscapeServletFilter> filterRegistrationBean() {
    FilterRegistrationBean<XssEscapeServletFilter> filterRegistration = new FilterRegistrationBean<>();
    filterRegistration.setFilter(new XssEscapeServletFilter());
    filterRegistration.setOrder(1);
    filterRegistration.addUrlPatterns("/*");
    return filterRegistration;
}
```

### Undertow
```
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-web') {
        exclude module: "spring-boot-starter-tomcat"
    }
    compile('org.springframework.boot:spring-boot-starter-undertow')
}
```

## 실행 방법

### 🚀 Spring Boot 애플리케이션 실행

#### 1. 환경 변수 방식 (권장)
```bash
# 로컬 환경으로 실행  
SPRING_PROFILES_ACTIVE=local ./gradlew :chap08:bootRun
```

#### 2. Program Arguments 방식
```bash
# 기본 실행
./gradlew :chap08:bootRun --args='--spring.profiles.active=local'
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
./gradlew :chap08:bootRun -Dspring.profiles.active=local
```

## ✅ 최근 테스트 개선사항

### TestContainers 현대화 마이그레이션 완료

**Spring Boot 3.x 표준 방식으로 필터 체인 테스트 현대화:**

#### 마이그레이션된 테스트 파일들:
- `PrimaveraFilterTest`: Lucy XSS 필터와 커스텀 필터 체인 통합 테스트

#### 새로운 TestContainers 패턴 (현재 방식)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Primavera 필터 체인 테스트")
class PrimaveraFilterTest {

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
    @DisplayName("XSS 필터를 통한 스크립트 공격 방어")
    void xssFilterDefense() {
        String maliciousInput = "<script>alert('XSS')</script>";
        // XSS 필터가 스크립트 태그를 무력화하는지 검증
    }
}
```

#### 마이그레이션의 주요 개선 효과:
- **XSS 보안 필터 검증**: Lucy 필터의 스크립트 태그 무력화 테스트
- **필터 체인 순서 검증**: 커스텀 필터와 보안 필터의 실행 순서 확인
- **Chain of Responsibility 패턴**: 필터 체인의 책임 연쇄 패턴 구현 검증
- **웹 보안 통합 테스트**: 실제 HTTP 요청을 통한 보안 검증

### ETC
* Chain of Responsibility Pattern 참고