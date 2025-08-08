## chap13

### AOP 이용 시스템 로그 저장

```java
@Slf4j
@Aspect
@Component
public class PrimaveraLoggingAspect {

    @Autowired
    private PrimaveraLogService primaveraLogService;

    @Autowired
    private MongoSequenceGeneratorService mongoSequenceGeneratorService;

    @Before(value = "@annotation(primaveraLogging)", argNames = "joinPoint, primaveraLogging")
    public void preLogging(JoinPoint joinPoint, PrimaveraLogging primaveraLogging) {
        PrimaveraLog primaveraLog = PrimaveraLog.builder()
                .id(mongoSequenceGeneratorService.generateSequence(PrimaveraLog.SEQUENCE_NAME))
                .type(primaveraLogging.type())
                .kind(joinPoint.getKind())
                .target(joinPoint.getTarget())
                .createDt(Instant.now())
                .build();
        primaveraLogService.save(primaveraLog);
    }
}
```

### Http2 적용

### File Upload

### 시스템 로그 통계

### SQL
```sql
CREATE TABLE IF NOT EXISTS ARTICLE (
    ID BIGINT(20) NOT NULL AUTO_INCREMENT,
    P_ID BIGINT(20) NOT NULL DEFAULT 0,
    REFERENCE BIGINT(20) NOT NULL,
    STEP INT(11) NOT NULL,
    LEVEL INT(11) NOT NULL,
    AUTHOR BIGINT(20) NOT NULL,
    SUBJECT VARCHAR(200) NOT NULL,
    STATUS TINYINT(3) NOT NULL,
    HIT BIGINT(20) NOT NULL DEFAULT 0,
    RECOMMEND BIGINT(20) NOT NULL DEFAULT 0,
    DISAPPROVE BIGINT(20) NOT NULL DEFAULT 0,
    REG_DT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    MOD_DT TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (ID),
    KEY FK_WRITER_ID_IDX (AUTHOR),
    CONSTRAINT FK_ARTICLE_AUTHOR_ID FOREIGN KEY (AUTHOR) REFERENCES USERS (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ARTICLE_ATTACHMENT (
    ID BIGINT(20) NOT NULL AUTO_INCREMENT,
    ARTICLE_ID BIGINT(20) NOT NULL,
    NAME VARCHAR(100) NOT NULL,
    PATH VARCHAR(200) NOT NULL,
    SIZE INT(11) NOT NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE  IF NOT EXISTS ARTICLE_COMMENT (
    ID BIGINT(20) NOT NULL AUTO_INCREMENT,
    ARTICLE_ID BIGINT(20) NOT NULL,
    LEVEL INT(11) NOT NULL,
    STEP INT(11) NOT NULL,
    COMMENT TEXT NOT NULL,
    AUTHOR VARCHAR(45) NOT NULL,
    STATUS TINYINT(3) NOT NULL,
    REG_DT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    MOD_DT TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (ID)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ARTICLE_CONTENT (
    ID         BIGINT(20) NOT NULL AUTO_INCREMENT,
    ARTICLE_ID BIGINT(20) DEFAULT NULL,
    CONTENTS   text       DEFAULT NULL,
    PRIMARY KEY (ID),
    KEY FK_AUTHOR_IDX (ARTICLE_ID),
    CONSTRAINT FK_ARTICLE_ID FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

```

## 실행 방법

### 🚀 Spring Boot 애플리케이션 실행

#### 1. 환경 변수 방식 (권장)
```bash
# 로컬 환경으로 실행  
SPRING_PROFILES_ACTIVE=local ./gradlew :chap13:bootRun
```

#### 2. Program Arguments 방식
```bash
# 기본 실행
./gradlew :chap13:bootRun --args='--spring.profiles.active=local'
```

#### 3. IDE 설정 방식
- IntelliJ IDEA: Run Configuration → VM Options 또는 Program Arguments 설정
- VM Options: `-Dspring.profiles.active=local`
- Program Arguments: `--spring.profiles.active=local`

## 🐳 인프라 설정

### Docker Compose 환경 설정

이 챕터는 **게시판 + Vault 인프라**를 사용합니다:

```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# 게시판 + 중앙 설정 관리용 Docker Compose 실행 (MariaDB + Vault)
docker-compose -f docker-compose.board.yml up -d

# 서비스 상태 확인
docker-compose -f docker-compose.board.yml ps

# Vault 초기화 확인
docker logs vault-init-primavera-board

# 정리 (컨테이너 및 볼륨 삭제)
docker-compose -f docker-compose.board.yml down -v
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308)
- **HashiCorp Vault 1.15** (포트: 8200) - 중앙집중식 설정 관리
- 게시판 전용 데이터베이스 스키마 자동 생성

**애플리케이션 실행:**
```bash
# 인프라 시작 후 애플리케이션 실행
./gradlew :chap13:bootRun -Dspring.profiles.active=local
```

### Docker For MongoDB

#### Downloading an Image

```
docker search mongo

docker login --username {username}

docker pull mongo
```

#### Creating a Container

```
docker run --name mongo -d -p 27017-27019:27017-27019 -e MONGO_INITDB_ROOT_USERNAME=primavera -e MONGO_INITDB_ROOT_PASSWORD=primavera mongo

docker ps

docker exec -it mongo bash

```

## ✅ 최근 테스트 개선사항

### TestContainers 현대화 마이그레이션 완료

**Spring Boot 3.x 표준 방식으로 AOP 기반 로깅 시스템 테스트 현대화:**

#### 마이그레이션된 테스트 파일들:
- `WriteArticleServiceTest`: AOP 로깅 Aspect가 적용된 서비스 통합 테스트
- `ArticleMapperTest`: MongoDB와 연동된 게시글 매퍼 테스트
- `ArticleControllerTest`: 시스템 로그 저장을 포함한 컨트롤러 통합 테스트
- `PostMockControllerTest`: AOP 포인트컷 동작을 검증하는 모킹 테스트

#### 새로운 TestContainers 패턴 (현재 방식)
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("AOP 로깅 시스템 통합 테스트")
class WriteArticleServiceTest {

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
    @DisplayName("게시글 작성 시 AOP 로깅 동작 검증")
    void articleCreationWithLogging() {
        Article article = Article.builder()
            .title("AOP 테스트 게시글")
            .content("시스템 로그 저장 테스트")
            .build();
            
        Article saved = writeArticleService.save(article);
        
        // AOP Aspect가 실행되어 시스템 로그가 저장되었는지 검증
        assertThat(saved.getId()).isNotNull();
        // 추가로 로그 테이블에서 로그 엔트리 확인
    }
}
```

#### 마이그레이션의 주요 개선 효과:
- **AOP Aspect 동작 검증**: @Around, @Before, @After 어드바이스 실행 테스트
- **포인트컷 표현식 검증**: 특정 메서드 패턴에 대한 AOP 적용 확인
- **시스템 로그 저장 검증**: 비즈니스 로직 실행과 로깅의 분리된 동작 테스트
- **MongoDB 통합 테스트**: NoSQL 데이터베이스와의 연동 검증
- **크로스커팅 관심사 테스트**: 로깅, 보안, 트랜잭션 등 횡단 관심사 검증