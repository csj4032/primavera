## chap12

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
    CONSTRAINT FK_ARTICLE_AUTHOR_ID FOREIGN KEY (AUTHOR) REFERENCES USER (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
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