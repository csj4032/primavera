## chap12

### 계층구조
* 인접 목록 트리 조회 (안티 패턴)
* 경로 열거
* 중첩 집합
* 클로저 테이블

### 계층형 게시판 (글타래) 테이블

```sql
CREATE TABLE IF NOT EXISTS ARTICLE
(
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
  CONSTRAINT FK_ARTICLE_AUTHOR_ID FOREIGN KEY (AUTHOR) REFERENCES USER (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ARTICLE_CONTENT (
    ID         BIGINT(20) NOT NULL AUTO_INCREMENT,
    ARTICLE_ID BIGINT(20) DEFAULT NULL,
    CONTENTS   text       DEFAULT NULL,
    PRIMARY KEY (ID),
    KEY FK_AUTHOR_IDX (ARTICLE_ID),
    CONSTRAINT FK_ARTICLE_ID FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE  IF NOT EXISTS ARTICLE_COMMENT 
(
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  ARTICLE_ID bigint(20) NOT NULL,
  LEVEL int(11) NOT NULL,
  STEP int(11) NOT NULL,
  COMMENT text NOT NULL,
  AUTHOR varchar(45) NOT NULL,
  STATUS tinyint(3) NOT NULL,
  REG_DT timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  MOD_DT timestamp NULL DEFAULT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
```

### 계층 관계 아티클 조회

```java
    @Mapper
    @Repository
    public interface ArticleMapper {
    
        @Insert("INSERT INTO ARTICLE (P_ID, REFERENCE, STEP, LEVEL, SUBJECT, AUTHOR, STATUS) VALUES (#{pId}, #{reference}, #{step}, #{level}, #{subject}, #{author.id}, #{status, typeHandler=ArticleStatusTypeHandler})")
        @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
        int save(Article article);
    
        @Results(id="ARTICLE_WITH_USER",
            value = {
                @Result(property = "id", column = "ID"),
                @Result(property = "pId", column = "P_ID"),
                @Result(property = "parent", javaType = Article.class, column = "P_ID", one = @One(select = "findByArticleId", fetchType = FetchType.DEFAULT)),
                @Result(property = "children", javaType = Article[].class, column = "ID", many = @Many(select = "findByArticleIdForChildren", fetchType = FetchType.DEFAULT)),
                @Result(property = "reference", column = "REFERENCE"),
                @Result(property = "step", column = "STEP"),
                @Result(property = "level", column = "LEVEL"),
                @Result(property = "author.id", column = "AUTHOR"),
                @Result(property = "author.email", column = "EMAIL"),
                @Result(property = "author.nickname", column = "EMAIL"),
                @Result(property = "subject", column = "SUBJECT"),
                @Result(property = "status", typeHandler = ArticleStatusTypeHandler.class, column = "STATUS"),
                @Result(property = "regDt", column = "REG_DT"),
                @Result(property = "modDt", column = "MOD_DT")
    
        })
        @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.AUTHOR, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.AUTHOR = B.ID WHERE A.P_ID = 0")
        List<Article> findAll();
    
        @ResultMap(value = "ARTICLE_WITH_USER")
        @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.AUTHOR, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.AUTHOR = B.ID WHERE A.ID = #{id}")
        Article findByArticleId(long id);
    
        @Results(value = {
                @Result(property = "id", column = "ID"),
                @Result(property = "pId", column = "P_ID"),
                @Result(property = "reference", column = "REFERENCE"),
                @Result(property = "step", column = "STEP"),
                @Result(property = "level", column = "LEVEL"),
                @Result(property = "writer.id", column = "AUTHOR"),
                @Result(property = "writer.email", column = "EMAIL"),
                @Result(property = "writer.nickname", column = "EMAIL"),
                @Result(property = "subject", column = "SUBJECT"),
                @Result(property = "status", typeHandler = ArticleStatusTypeHandler.class, column = "STATUS"),
                @Result(property = "regDt", column = "REG_DT"),
                @Result(property = "modDt", column = "MOD_DT")
    
        })
        @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.AUTHOR, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.AUTHOR = B.ID WHERE A.P_ID = #{id}")
        Article findByArticleIdForChildren(long id);
    }
```

### Springboot Environment

```java
public class PrimaveraApplication {

	private static final String APPLICATION = "spring.config.location=classpath:/application-${spring.profiles.active:default}.yml,classpath:/social.yml";

	public static void main(String[] args) {
		new SpringApplicationBuilder(PrimaveraApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.properties(APPLICATION)
				.build()
				.run(args);
	}
}
```

## 실행 방법

### 🚀 Spring Boot 애플리케이션 실행

#### 1. 환경 변수 방식 (권장)
```bash
# 로컬 환경으로 실행  
SPRING_PROFILES_ACTIVE=local ./gradlew :chap12:bootRun
```

#### 2. Program Arguments 방식
```bash
# 기본 실행
./gradlew :chap12:bootRun --args='--spring.profiles.active=local'
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
./gradlew :chap12:bootRun -Dspring.profiles.active=local
```

## ✅ 최근 테스트 개선사항

### TestContainers 현대화 마이그레이션 완료

**Spring Boot 3.x 표준 방식으로 게시글 관리 시스템 테스트 현대화:**

#### 마이그레이션된 테스트 파일들:
- `WriteArticleServiceTest`: 게시글 작성 서비스 비즈니스 로직 통합 테스트
- `ArticleMapperTest`: MyBatis 기반 게시글 데이터 접근 계층 테스트

#### 새로운 TestContainers 패턴 (현재 방식)
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("게시글 작성 서비스 통합 테스트")
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
    @DisplayName("게시글 작성 및 저장 검증")
    void createAndSaveArticle() {
        Article article = Article.builder()
            .title("테스트 게시글")
            .content("게시글 내용 테스트")
            .author("테스트 작성자")
            .build();
            
        Article saved = writeArticleService.save(article);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("테스트 게시글");
    }
}
```

#### 마이그레이션의 주요 개선 효과:
- **서비스 계층 통합 검증**: 게시글 작성/수정/삭제 비즈니스 로직 테스트
- **MyBatis 매퍼 테스트**: 동적 SQL 쿼리 및 결과 매핑 검증
- **트랜잭션 처리 검증**: @Transactional 어노테이션 기반 트랜잭션 경계 테스트
- **데이터 무결성 검증**: 게시글 데이터 저장/조회 일관성 확인

### MockitoExtension
* https://mincong.io/2020/04/19/mockito-junit5/