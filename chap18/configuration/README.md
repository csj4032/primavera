# Configuration Service - Centralized Config Management

## 📋 Overview

Configuration Service는 Primavera 마이크로서비스 아키텍처에서 중앙집중식 설정 관리를 담당하는 핵심 인프라 서비스입니다. Spring Cloud Config Server를 기반으로 구축되어 모든 마이크로서비스의 설정을 Git 저장소에서 중앙집중식으로 관리하고 배포합니다.

## 🏗️ 아키텍처 특성

### Core Technologies
- **Spring Boot 3.3.6**: 최신 스프링 부트 프레임워크
- **Spring Cloud Config Server**: 중앙집중식 설정 서버
- **Git Integration**: Git 저장소 기반 설정 버전 관리
- **Dynamic Configuration**: 런타임 설정 갱신 지원

### Configuration as Code Pattern
```java
@Slf4j
@EnableConfigServer
@SpringBootApplication
public class ConfigurationApplication {
    
    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        log.info("Configuration Server Ready!");
    }
}
```

## 🚀 주요 기능

### 1. 중앙집중식 설정 관리
- **Git 기반 저장소**: 모든 설정을 Git에서 버전 관리
- **환경별 설정**: dev/test/prod 환경별 설정 분리
- **실시간 갱신**: `/refresh` 엔드포인트를 통한 동적 설정 갱신
- **암호화 지원**: 민감한 정보 암호화 저장

### 2. 마이크로서비스 설정 배포
- **자동 설정 배포**: 각 서비스별 설정 자동 제공
- **프로파일 기반 설정**: Spring Profiles를 통한 환경별 설정
- **서비스 디스커버리**: 서비스별 개별화된 설정 관리

### 3. Git Repository 구조
```
config-repo/
├── account/
│   ├── account.yml
│   ├── account-dev.yml
│   ├── account-test.yml
│   └── account-prod.yml
├── front/
│   ├── front.yml
│   ├── front-dev.yml
│   ├── front-test.yml
│   └── front-prod.yml
├── order/
│   └── ...
└── product/
    └── ...
```

## 🔧 설정 및 구성

### Config Server 설정
```yaml
server:
  port: 8888

spring:
  cloud:
    config:
      server:
        encrypt:
          enabled: false                # 암호화 비활성화 (개발용)
        git:
          uri: git@github.com:csj4032/config-repo.git
          search-paths: account, front, order, product
```

### Git Repository 설정
- **Repository URL**: `git@github.com:csj4032/config-repo.git`
- **Search Paths**: `account, front, order, product`
- **SSH Key**: Git 저장소 접근을 위한 SSH 키 설정
- **Webhook**: Git 변경 시 자동 갱신 (선택사항)

## 📊 설정 배포 프로세스

### 1. 설정 파일 구조
```yaml
# account.yml (공통 설정)
spring:
  redis:
    host: localhost
    port: 6379

# account-dev.yml (개발 환경)
spring:
  redis:
    host: dev-redis-server
    port: 6379

# account-prod.yml (운영 환경)
spring:
  redis:
    host: prod-redis-server
    port: 6379
    password: ${REDIS_PASSWORD}
```

### 2. 클라이언트 설정 요청 Flow
```
1. Microservice 시작
2. Config Server에 설정 요청 (bootstrap.yml)
3. Git Repository에서 설정 파일 조회
4. 환경별 설정 병합
5. 마이크로서비스에 설정 전달
6. 애플리케이션 컨텍스트 초기화
```

### 3. 동적 설정 갱신
```bash
# Git Repository 설정 변경 후
curl -X POST http://localhost:8080/actuator/refresh

# 또는 Spring Cloud Bus 사용 (전체 서비스 갱신)
curl -X POST http://localhost:8888/actuator/bus-refresh
```

## 🌐 API 엔드포인트

### 설정 조회 API
```http
# 기본 설정 조회
GET /{application}/{profile}
GET /account/default

# 라벨 지정 설정 조회  
GET /{application}/{profile}/{label}
GET /account/dev/master

# 속성별 설정 조회
GET /{application}-{profile}.yml
GET /account-prod.yml

# 암호화된 설정 복호화
POST /encrypt
POST /decrypt
```

### 관리 엔드포인트
```http
# 설정 서버 상태 확인
GET /actuator/health

# Git Repository 갱신
POST /actuator/refresh

# 환경 정보 조회
GET /actuator/env
```

## 🏃‍♂️ 실행 방법

### 1. Git Repository 준비
```bash
# Config Repository 생성
git clone git@github.com:csj4032/config-repo.git
cd config-repo

# 서비스별 설정 디렉토리 생성
mkdir -p account front order product

# 설정 파일 생성
echo "server.port: 8081" > account/account.yml
echo "server.port: 8080" > front/front.yml
echo "server.port: 8082" > order/order.yml
echo "server.port: 8083" > product/product.yml

# Git 커밋
git add .
git commit -m "Initial configuration"
git push origin main
```

### 2. SSH Key 설정 (Git 접근용)
```bash
# SSH Key 생성
ssh-keygen -t rsa -b 4096 -C "config-server@primavera.com"

# 공개키를 Git Repository에 등록
cat ~/.ssh/id_rsa.pub

# Config Server에서 SSH Key 사용 설정
export GIT_SSH_COMMAND="ssh -i ~/.ssh/id_rsa"
```

### 3. Configuration Service 시작
```bash
# Configuration 서비스 실행
./gradlew :chap18:configuration:bootRun

# 또는 JAR 실행
java -jar configuration/build/libs/configuration.jar
```

### 4. 설정 서버 동작 확인
```bash
# Config Server 상태 확인
curl http://localhost:8888/actuator/health

# Account 서비스 설정 조회
curl http://localhost:8888/account/default

# 환경별 설정 조회
curl http://localhost:8888/account/dev
curl http://localhost:8888/account/prod
```

## 🔗 클라이언트 연동

### Bootstrap Configuration
각 마이크로서비스는 `bootstrap.yml`에서 Config Server 연결 설정:

```yaml
# bootstrap.yml (각 마이크로서비스)
spring:
  application:
    name: account  # 설정 파일명과 일치
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 6
```

### 클라이언트 의존성
```gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-config'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

## 📈 모니터링 및 관리

### 로깅 전략
```yaml
logging:
  level:
    org.springframework.cloud.config: DEBUG
    org.springframework.web: INFO
```

### 주요 로그 포인트
- Git Repository 접근 로그
- 설정 파일 로드 상태
- 클라이언트 연결 정보
- 암호화/복호화 작업
- 설정 갱신 이벤트

### Health Check
```json
{
  "status": "UP",
  "components": {
    "configServer": {
      "status": "UP",
      "details": {
        "repository": "git@github.com:csj4032/config-repo.git",
        "label": "master"
      }
    }
  }
}
```

## 🛡️ 보안 고려사항

### 1. Git Repository 보안
- **SSH Key 관리**: 안전한 SSH 키 저장 및 관리
- **Repository 접근 제어**: Git 저장소 접근 권한 관리
- **Webhook 보안**: Git Webhook 인증 설정

### 2. 설정 데이터 암호화
```yaml
# 민감한 정보 암호화 예제
spring:
  datasource:
    password: '{cipher}AQA...'  # 암호화된 패스워드
```

```bash
# 암호화/복호화 명령
curl -X POST http://localhost:8888/encrypt -d "secret_password"
curl -X POST http://localhost:8888/decrypt -d "{cipher}AQA..."
```

### 3. 네트워크 보안
- **HTTPS 사용**: 운영 환경에서 HTTPS 필수
- **방화벽 설정**: Config Server 포트 접근 제한
- **VPN 연결**: 내부 네트워크 전용 설정

## 🧪 테스트 전략

### Config Server 테스트
```java
@SpringBootTest
class ConfigurationApplicationTests {
    
    @Test
    void contextLoads() {
        // Config Server 컨텍스트 로드 테스트
    }
    
    @Test
    void shouldServeConfiguration() {
        // 설정 파일 서빙 테스트
        RestTemplate restTemplate = new RestTemplate();
        String config = restTemplate.getForObject(
            "http://localhost:8888/account/default", 
            String.class
        );
        assertThat(config).isNotNull();
    }
}
```

### 통합 테스트
- **Git Repository Mock**: 테스트용 로컬 Git 저장소
- **Environment Test**: 환경별 설정 로드 테스트
- **Refresh Test**: 동적 설정 갱신 테스트

## 🚀 고급 기능

### 1. Spring Cloud Bus 통합
```gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-bus-amqp'
}
```

### 2. Vault 통합
```yaml
spring:
  cloud:
    config:
      server:
        vault:
          host: localhost
          port: 8200
          scheme: http
```

### 3. 다중 저장소 지원
```yaml
spring:
  cloud:
    config:
      server:
        git:
          repos:
            account:
              uri: git@github.com:csj4032/account-config.git
            front:
              uri: git@github.com:csj4032/front-config.git
```

## 📚 학습 포인트

이 Configuration Service는 다음과 같은 마이크로서비스 설정 관리 패턴들을 학습할 수 있습니다:

1. **중앙집중식 설정 관리**: 모든 서비스 설정의 중앙 관리
2. **환경별 설정 분리**: dev/test/prod 환경별 설정 관리
3. **버전 관리**: Git을 통한 설정 변경 이력 관리
4. **동적 설정 갱신**: 서비스 재시작 없이 설정 변경 적용
5. **보안**: 민감한 설정 정보의 암호화 및 안전한 관리

Configuration Service는 마이크로서비스 아키텍처에서 필수적인 설정 관리 패턴을 제공하며, 운영 환경에서의 효율적인 설정 관리 방법을 학습할 수 있는 핵심 서비스입니다.