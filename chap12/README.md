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

### 시스템 로그 통계

### Docker For MongoDB

#### Downloading an Image

```
docker search mongo

docker login --username csj4032

docker pull mongo
```

#### Creating a Container

```
docker run --name mongo -d -p 27017-27019:27017-27019 -e MONGO_INITDB_ROOT_USERNAME=primavera -e MONGO_INITDB_ROOT_PASSWORD=primavera mongo

docker ps

docker exec -it mongo bash

```