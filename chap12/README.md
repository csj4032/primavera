## chap11

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

### MockitoExtension
* https://mincong.io/2020/04/19/mockito-junit5/