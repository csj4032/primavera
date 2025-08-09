# Chapter 18 Configuration - Spring Cloud Config Server

Spring Boot 교육용 프로젝트 Primavera의 Chapter 18 Configuration 모듈입니다. Spring Cloud Config Server를 사용하여 마이크로서비스 환경에서의 중앙화된 설정 관리를 학습합니다.

## 🎯 학습 목표

- **중앙화된 설정 관리**: 여러 마이크로서비스의 설정을 중앙에서 관리
- **Spring Cloud Config**: 분산 설정 서버 구축 및 운영
- **환경별 설정**: 개발, 테스트, 운영 환경별 설정 분리
- **동적 설정 갱신**: 애플리케이션 재시작 없는 설정 변경
- **보안 설정**: 민감한 설정 정보의 암호화 및 보안

## 📁 프로젝트 구조

```
chap18/configuration/
├── src/main/java/com/genius/primavera/
│   └── ConfigurationApplication.java    # Config Server 메인 클래스
├── src/main/resources/
│   └── application.yml                  # Config Server 설정
└── build.gradle                         # Spring Cloud Config 의존성
```

## 🏗 아키텍처 특성

### 1. Spring Cloud Config Server
```java
@SpringBootApplication
@EnableConfigServer  // Config Server 활성화
public class ConfigurationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigurationApplication.class, args);
    }
}
```

### 2. 마이크로서비스 설정 중앙화
```yaml
# application.yml
server:
  port: 8888  # Config Server 기본 포트

spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          search-paths: configs/{application}
          default-label: main
        health:
          repositories:
            config-repo:
              label: main
              name: account,product,order,front
```

### 3. 환경별 설정 구조
```
config-repository/
├── account/
│   ├── account.yml              # 공통 설정
│   ├── account-local.yml        # 로컬 환경
│   ├── account-dev.yml          # 개발 환경
│   └── account-prod.yml         # 운영 환경
├── product/
│   ├── product.yml
│   ├── product-local.yml
│   └── product-prod.yml
├── order/
│   ├── order.yml
│   └── order-prod.yml
└── front/
    ├── front.yml
    └── front-prod.yml
```

## 🎯 핵심 기능

### 1. 마이크로서비스별 설정 관리
```yaml
# configs/account/account.yml
spring:
  application:
    name: account-service
  
  datasource:
    url: ${DB_URL:jdbc:mariadb://localhost:3308/primavera}
    username: ${DB_USERNAME:primavera}
    password: ${DB_PASSWORD:primavera}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: 8081

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 2. 환경별 설정 오버라이드
```yaml
# configs/account/account-prod.yml
spring:
  datasource:
    url: ${PROD_DB_URL}
    username: ${PROD_DB_USERNAME}
    password: ${PROD_DB_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    
logging:
  level:
    com.genius.primavera: INFO
    org.springframework: WARN
```

### 3. 공통 설정 상속
```yaml
# configs/common/common.yml
spring:
  profiles:
    active: ${PROFILE:local}
    
  jpa:
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
        
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    
management:
  endpoint:
    health:
      show-details: always
```

### 4. 설정 암호화
```yaml
# 암호화된 설정 예시
spring:
  datasource:
    password: '{cipher}AQA5UUIwNd...'  # 암호화된 패스워드
```

## 🛠 기술 스택

### 핵심 기술
- **Java**: 21
- **Spring Boot**: 3.3.6
- **Spring Cloud Config**: 중앙화된 설정 관리
- **Spring Cloud**: 2023.0.3

### 설정 저장소
- **Git Repository**: 설정 파일 버전 관리
- **Local File System**: 로컬 개발 환경
- **Vault Integration**: 민감 정보 보안 관리

## 🚀 실행 방법

### 1. Config Server 실행
```bash
# Config Server 시작
./gradlew :chap18:configuration:bootRun

# 특정 포트로 실행
./gradlew :chap18:configuration:bootRun -Dserver.port=8888
```

### 2. 설정 조회 API
```bash
# 특정 애플리케이션의 설정 조회
curl http://localhost:8888/account/local

# 운영 환경 설정 조회
curl http://localhost:8888/account/prod

# JSON 형식으로 설정 조회
curl http://localhost:8888/account/local/main

# 특정 설정 파일 조회
curl http://localhost:8888/account-local.yml
```

### 3. 설정 갱신 확인
```bash
# Config Server 헬스 체크
curl http://localhost:8888/actuator/health

# 설정 저장소 상태 확인
curl http://localhost:8888/actuator/configprops
```

## 📋 마이크로서비스 연동

### 1. 클라이언트 설정
```yaml
# 각 마이크로서비스의 bootstrap.yml
spring:
  application:
    name: account-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 6
        max-interval: 2000
      discovery:
        enabled: false
```

### 2. 동적 설정 갱신
```java
@RestController
@RefreshScope  // 설정 갱신 지원
public class ConfigTestController {
    
    @Value("${app.message:default}")
    private String message;
    
    @GetMapping("/config")
    public ResponseEntity<String> getConfig() {
        return ResponseEntity.ok(message);
    }
}
```

### 3. 설정 갱신 호출
```bash
# 특정 서비스의 설정 갱신
curl -X POST http://localhost:8081/actuator/refresh

# Config Server 설정 갱신
curl -X POST http://localhost:8888/actuator/bus-refresh
```

## 🔒 보안 설정

### 1. 설정 암호화
```bash
# 암호화 키 설정
export ENCRYPT_KEY=mySecretKey

# 패스워드 암호화
curl -X POST http://localhost:8888/encrypt -d "mySecretPassword"
```

### 2. 보안 설정 예시
```yaml
# application.yml
encrypt:
  key: ${ENCRYPT_KEY:defaultkey}

spring:
  cloud:
    config:
      server:
        encrypt:
          enabled: true
        git:
          uri: ${CONFIG_REPO_URL}
          username: ${GIT_USERNAME}
          password: '{cipher}AQA5UUIwNd...'
```

## 📋 테스트 실행

### Config Server 테스트
```bash
# 설정 서버 테스트
./gradlew :chap18:configuration:test

# 통합 테스트
./gradlew :chap18:configuration:integrationTest
```

### 설정 검증 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldReturnAccountConfig() {
        ResponseEntity<String> response = 
            restTemplate.getForEntity("/account/local", String.class);
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("account-service");
    }
}
```

## 🎓 핵심 학습 포인트

### 1. 12-Factor App 원칙
- **설정의 외부화**: 애플리케이션과 설정 분리
- **환경 패리티**: 개발/스테이징/운영 환경 일관성
- **포트 바인딩**: 환경 변수를 통한 포트 설정

### 2. 설정 우선순위
```
1. Command line arguments
2. Java system properties
3. OS environment variables
4. Config Server properties
5. application.yml in classpath
6. Default values
```

### 3. 설정 프로파일 전략
```yaml
# 프로파일별 설정 로드
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
    include:
      - common
      - database-${spring.profiles.active}
      - security-${spring.profiles.active}
```

### 4. 설정 검증
```java
@ConfigurationProperties("app")
@Validated
public class AppProperties {
    
    @NotBlank
    private String name;
    
    @Min(1)
    @Max(65535)
    private int port;
    
    // getters and setters
}
```

## 📚 주요 애너테이션

### Spring Cloud Config
- `@EnableConfigServer`: Config Server 활성화
- `@RefreshScope`: 설정 갱신 지원
- `@ConfigurationProperties`: 타입 안전한 설정 바인딩

### 설정 관리
- `@Value`: 단일 설정 값 주입
- `@ConditionalOnProperty`: 조건부 빈 생성

## 🔧 운영 모니터링

### 1. Config Server 메트릭
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,configprops
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. 설정 변경 추적
```java
@Component
public class ConfigChangeListener {
    
    @EventListener
    public void onRefresh(RefreshRemoteApplicationEvent event) {
        log.info("Configuration refreshed for: {}", event.getDestinationService());
    }
}
```

## 🔄 다음 단계

1. **chap18:account** - Config Client 구현 및 동적 설정 활용
2. **chap18:product** - 마이크로서비스에서의 설정 관리 패턴
3. **서비스 디스커버리** - Eureka와의 통합
4. **API Gateway** - 라우팅 설정 중앙화

## 📖 관련 문서

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [12-Factor App](https://12factor.net/)
- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Cloud Bus](https://spring.io/projects/spring-cloud-bus)