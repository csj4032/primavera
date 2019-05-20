## chap11

### 계층형 게시판 테이블

```sql
CREATE TABLE `ARTICLE` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `P_ID` bigint(20) NOT NULL DEFAULT 0,
  `REFERENCE` bigint(20) NOT NULL,
  `STEP` int(11) NOT NULL,
  `LEVEL` int(11) NOT NULL,
  `WRITER_ID` bigint(20) NOT NULL,
  `SUBJECT` varchar(200) NOT NULL,
  `STATUS` tinyint(3) NOT NULL,
  `HIT` bigint(20) NOT NULL DEFAULT 0,
  `LIKE` bigint(20) NOT NULL DEFAULT 0,
  `DISLIKE` bigint(20) NOT NULL DEFAULT 0,
  `REG_DT` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `MOD_DT` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_WRITER_ID_idx` (`WRITER_ID`),
  CONSTRAINT `FK_ARTICLE_WRITER_ID` FOREIGN KEY (`WRITER_ID`) REFERENCES `USER` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
```

### 계층 관계 아티클 조회

```java
    @Mapper
    @Repository
    public interface ArticleMapper {
    
        @Insert("INSERT INTO ARTICLE (P_ID, REFERENCE, STEP, LEVEL, SUBJECT, WRITER_ID, STATUS) VALUES (#{pId}, #{reference}, #{step}, #{level}, #{subject}, #{writer.id}, #{status, typeHandler=ArticleStatusTypeHandler})")
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
                @Result(property = "writer.id", column = "WRITER_ID"),
                @Result(property = "writer.email", column = "EMAIL"),
                @Result(property = "writer.nickname", column = "EMAIL"),
                @Result(property = "subject", column = "SUBJECT"),
                @Result(property = "status", typeHandler = ArticleStatusTypeHandler.class, column = "STATUS"),
                @Result(property = "regDt", column = "REG_DT"),
                @Result(property = "modDt", column = "MOD_DT")
    
        })
        @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.WRITER_ID, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.WRITER_ID = B.ID WHERE A.P_ID = 0")
        List<Article> findAll();
    
        @ResultMap(value = "ARTICLE_WITH_USER")
        @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.WRITER_ID, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.WRITER_ID = B.ID WHERE A.ID = #{id}")
        Article findByArticleId(long id);
    
        @Results(value = {
                @Result(property = "id", column = "ID"),
                @Result(property = "pId", column = "P_ID"),
                @Result(property = "reference", column = "REFERENCE"),
                @Result(property = "step", column = "STEP"),
                @Result(property = "level", column = "LEVEL"),
                @Result(property = "writer.id", column = "WRITER_ID"),
                @Result(property = "writer.email", column = "EMAIL"),
                @Result(property = "writer.nickname", column = "EMAIL"),
                @Result(property = "subject", column = "SUBJECT"),
                @Result(property = "status", typeHandler = ArticleStatusTypeHandler.class, column = "STATUS"),
                @Result(property = "regDt", column = "REG_DT"),
                @Result(property = "modDt", column = "MOD_DT")
    
        })
        @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.WRITER_ID, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.WRITER_ID = B.ID WHERE A.P_ID = #{id}")
        Article findByArticleIdForChildren(long id);
    }
```