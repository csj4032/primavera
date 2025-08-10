# Primavera Streaming Service - Debezium CDC Implementation

이 문서는 Primavera 스트리밍 서비스에서 Debezium CDC (Change Data Capture) 기능을 구현하고 테스트 인프라를 구축하면서 수행된 모든 변경 사항을 정리합니다.

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [주요 기능](#주요-기능)  
3. [기술 스택](#기술-스택)
4. [아키텍처](#아키텍처)
5. [구현된 주요 컴포넌트](#구현된-주요-컴포넌트)
6. [테스트 전략](#테스트-전략)
7. [TestContainers 인프라 구축](#testcontainers-인프라-구축)
8. [보안 고려사항](#보안-고려사항)
9. [트러블슈팅](#트러블슈팅)
10. [변경 이력](#변경-이력)
11. [사용법](#사용법)
12. [기존 학습 목표](#기존-학습-목표)

## 🚀 프로젝트 개요

Primavera Streaming Service는 실시간 상품 검색 및 CDC 기반 데이터 동기화를 제공하는 마이크로서비스입니다. MariaDB의 바이너리 로그를 활용하여 데이터 변경사항을 실시간으로 감지하고, Elasticsearch에 자동 동기화하는 시스템을 구현했습니다.

## ✨ 주요 기능

- **실시간 CDC (Change Data Capture)**: MariaDB → Elasticsearch 자동 동기화
- **상품 검색 API**: Elasticsearch 기반 고성능 검색
- **WebSocket 실시간 스트리밍**: 상품 변경사항 실시간 푸시
- **TestContainers 기반 통합 테스트**: 실제 환경과 동일한 테스트 환경

## 📁 프로젝트 구조

```
chap17/streaming/
├── src/main/java/com/genius/primavera/streaming/
│   ├── ProductStreamingApplication.java  # 메인 애플리케이션 (리액티브)
│   ├── config/                          # 설정 클래스
│   │   ├── ElasticsearchConfiguration.java  # Elasticsearch 리액티브 설정
│   │   └── WebSocketConfiguration.java     # WebSocket 설정
│   ├── controller/                      # 리액티브 컨트롤러
│   │   └── ProductStreamController.java    # 상품 스트림 API
│   ├── handler/                         # 이벤트 핸들러
│   │   ├── DebeziumCdcEventHandler.java   # CDC 이벤트 처리
│   │   └── WebSocketHandler.java         # WebSocket 핸들러
│   └── service/                        # 리액티브 서비스
│       └── ProductSearchService.java      # 상품 검색 서비스
├── src/main/resources/
│   ├── application.yml              # 메인 설정 (리액티브 설정)
│   ├── application-local.yml        # 로컬 환경 설정
│   └── logback-spring.xml          # 로깅 설정
└── build.gradle                    # WebFlux, WebSocket, R2DBC 설정
```

## 🏗 아키텍처 특성

### 1. 리액티브 웹 스택
```java
@RestController
@RequestMapping("/api/products")
public class ProductStreamController {
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductDocument> streamProducts() {
        return productSearchService
            .searchAllProducts()
            .delayElements(Duration.ofSeconds(1))  // 스트리밍 시뮬레이션
            .doOnNext(product -> log.info("Streaming product: {}", product.getName()));
    }
    
    @GetMapping("/search")
    public Mono<ResponseEntity<List<ProductDocument>>> searchProducts(
            @RequestParam String query) {
        return productSearchService
            .searchProducts(query)
            .collectList()
            .map(ResponseEntity::ok);
    }
}
```

### 2. WebSocket 실시간 통신
```java
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket connection established: {}", session.getId());
    }
    
    public void broadcastProductUpdate(ProductDocument product) {
        String message = objectMapper.writeValueAsString(product);
        
        sessions.parallelStream()
            .filter(WebSocketSession::isOpen)
            .forEach(session -> {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("Failed to send message to session {}", session.getId(), e);
                }
            });
    }
}
```

### 3. R2DBC 리액티브 데이터 액세스
```gradle
dependencies {
    implementation "org.springframework.boot:spring-boot-starter-data-r2dbc"
    implementation "org.mariadb:r2dbc-mariadb:${r2dbcMariadbVersion}"
    implementation "io.r2dbc:r2dbc-pool:${r2dbcPoolVersion}"
}
```

### 4. 멀티모듈 의존성
```gradle
dependencies {
    implementation project(':chap17:common')  // 공통 도메인 모델 재사용
    implementation "org.springframework.boot:spring-boot-starter-webflux"
    implementation "org.springframework.boot:spring-boot-starter-websocket"
}
```

## 🎯 핵심 기능

### 1. 리액티브 상품 검색
```java
@Service
public class ProductSearchService {
    
    public Flux<ProductDocument> searchAllProducts() {
        return Flux.fromIterable(elasticsearchTemplate
            .search(Query.findAll(), ProductDocument.class)
            .getSearchHits()
        ).map(SearchHit::getContent);
    }
    
    public Flux<ProductDocument> searchProducts(String query) {
        Criteria criteria = new Criteria("name").contains(query)
            .or(new Criteria("description").contains(query));
            
        return Flux.fromIterable(elasticsearchTemplate
            .search(Query.query(criteria), ProductDocument.class)
            .getSearchHits()
        ).map(SearchHit::getContent);
    }
}
```

### 2. CDC 이벤트 실시간 처리
```java
@Component
public class DebeziumCdcEventHandler {
    
    @EventListener
    public void handleProductChange(ChangeEvent<String, String> event) {
        try {
            ProductDocument product = parseProductFromEvent(event);
            
            // WebSocket으로 실시간 알림
            webSocketHandler.broadcastProductUpdate(product);
            
            // 검색 인덱스 업데이트
            updateSearchIndex(product)
                .subscribe(
                    result -> log.info("Index updated for product: {}", product.getId()),
                    error -> log.error("Failed to update index", error)
                );
                
        } catch (Exception e) {
            log.error("Failed to handle CDC event", e);
        }
    }
    
    private Mono<Void> updateSearchIndex(ProductDocument product) {
        return Mono.fromCallable(() -> {
            elasticsearchTemplate.save(product);
            return null;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
```

### 3. Server-Sent Events (SSE)
```java
@GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<ProductDocument>> streamProductEvents() {
    return Flux.interval(Duration.ofSeconds(5))
        .flatMap(tick -> productSearchService.getLatestProducts())
        .map(product -> ServerSentEvent.<ProductDocument>builder()
            .id(String.valueOf(product.getId()))
            .event("product-update")
            .data(product)
            .build()
        );
}
```

### 4. 백프레셰 처리
```java
@GetMapping("/backpressure-demo")
public Flux<String> backpressureDemo() {
    return Flux.range(1, 1000000)
        .map(i -> "Item " + i)
        .onBackpressureBuffer(1000)  // 백프레셰 버퍼 설정
        .delayElements(Duration.ofMillis(1))
        .doOnRequest(demand -> log.info("Requested: {}", demand));
}
```

## 🛠 기술 스택

### 핵심 기술
- **Java**: 21
- **Spring Boot**: 3.3.6
- **Spring WebFlux**: 리액티브 웹 프레임워크
- **Project Reactor**: 리액티브 스트림 구현체
- **WebSocket**: 실시간 양방향 통신

### 데이터 액세스
- **R2DBC**: 리액티브 데이터베이스 클라이언트
- **MariaDB R2DBC**: MariaDB 리액티브 드라이버
- **Connection Pool**: R2DBC 연결 풀

### 테스트
- **Reactor Test**: 리액티브 스트림 테스트
- **TestContainers**: 통합 테스트 환경

## 🚀 실행 방법

### 1. 스트리밍 애플리케이션 실행
```bash
# 리액티브 애플리케이션 실행
./gradlew :chap17:streaming:bootRun

# 로컬 환경으로 실행
./gradlew :chap17:streaming:bootRun -Dspring.profiles.active=local
```

### 2. 스트리밍 API 테스트
```bash
# Server-Sent Events 스트림 구독
curl -N http://localhost:8080/api/products/stream

# WebSocket 연결 테스트 (wscat 사용)
wscat -c ws://localhost:8080/ws/products

# 실시간 검색
curl "http://localhost:8080/api/products/search?query=laptop"
```

### 3. 백프레셔 테스트
```bash
# 백프레셔 데모 실행
curl -N http://localhost:8080/api/products/backpressure-demo
```

## 📋 테스트 실행

### 리액티브 테스트
```bash
# 전체 테스트 실행
./gradlew :chap17:streaming:test

# 리액티브 서비스 테스트
./gradlew :chap17:streaming:test --tests "*ProductSearchServiceTest"

# WebSocket 테스트
./gradlew :chap17:streaming:test --tests "*WebSocketTest"
```

### 성능 테스트
```bash
# 부하 테스트 (Apache Bench 사용)
ab -n 1000 -c 10 http://localhost:8080/api/products/search?query=test

# 스트림 성능 테스트
./gradlew :chap17:streaming:test --tests "*PerformanceTest"
```

## 🎓 핵심 학습 포인트

### 1. 리액티브 스트림 패턴
```java
// 체인 방식의 리액티브 파이프라인
public Flux<ProductDocument> processProductStream() {
    return productSearchService.searchAllProducts()
        .filter(product -> product.getPrice().compareTo(BigDecimal.ZERO) > 0)
        .map(this::enrichProductData)
        .onErrorContinue((throwable, o) -> 
            log.error("Error processing product: {}", o, throwable))
        .subscribeOn(Schedulers.parallel());
}
```

### 2. 백프레셔 처리 전략
- **onBackpressureBuffer**: 버퍼링을 통한 처리
- **onBackpressureDrop**: 초과 데이터 드롭
- **onBackpressureLatest**: 최신 데이터만 유지

### 3. 에러 처리 패턴
```java
public Mono<ProductDocument> getProductSafely(Long id) {
    return productSearchService.getProduct(id)
        .onErrorResume(NotFoundException.class, 
            ex -> Mono.just(ProductDocument.empty()))
        .timeout(Duration.ofSeconds(5))
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)));
}
```

### 4. 스케줄러 활용
- **Schedulers.parallel()**: CPU 집약적 작업
- **Schedulers.boundedElastic()**: I/O 집약적 작업
- **Schedulers.single()**: 순차적 작업

## 📚 주요 애너테이션

### WebFlux 관련
- `@RestController`: 리액티브 컨트롤러
- `@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)`: SSE 엔드포인트

### WebSocket 관련
- `@EnableWebSocket`: WebSocket 지원 활성화
- `@OnOpen`, `@OnMessage`, `@OnClose`: WebSocket 이벤트 핸들러

### 리액티브 테스트
- `@WebFluxTest`: 리액티브 웹 계층 테스트
- `StepVerifier`: 리액티브 스트림 검증

## 🔧 성능 모니터링

### 1. 리액티브 메트릭
```java
@Configuration
public class ReactiveMetricsConfig {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

@Timed("product.search")
public Flux<ProductDocument> searchProducts(String query) {
    // 메트릭이 자동으로 수집됨
}
```

### 2. 백프레셔 모니터링
```yaml
management:
  endpoints:
    web:
      exposure:
        include: metrics
  metrics:
    distribution:
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

## 🔄 다음 단계

1. **chap18** - 마이크로서비스에서의 리액티브 아키텍처
2. **분산 스트리밍** - Apache Kafka와의 연동
3. **실시간 분석** - 스트림 처리를 통한 실시간 데이터 분석

## 🛠 기술 스택

### Core Technologies
- **Spring Boot** 3.3.6
- **Spring WebFlux** (리액티브 웹)
- **Debezium** 3.0.8 (CDC 엔진)
- **Elasticsearch** 8.13.4 (검색 엔진)
- **MariaDB** 11.4.7 (주 데이터베이스)

### Testing
- **TestContainers** 1.21.3 (통합 테스트)
- **JUnit 5** (단위 테스트)
- **Mockito** (모킹 프레임워크)
- **Awaitility** (비동기 테스트)

## 🏗 아키텍처

```
MariaDB (Binary Log) → Debezium CDC Engine → Elasticsearch
                            ↓
                      WebSocket Handler → Real-time Updates
                            ↓
                      Search API (WebFlux)
```

## 🔧 구현된 주요 컴포넌트

### 1. DebeziumCdcEventHandler
CDC 이벤트를 처리하는 핵심 컴포넌트입니다.

**주요 기능:**
- MariaDB 바이너리 로그 실시간 모니터링
- CREATE, UPDATE, DELETE 이벤트 감지
- ProductDocument 자동 생성 및 Elasticsearch 인덱싱
- 가격 범위 자동 분류 (LOW, MEDIUM, HIGH)

**설정 옵션:**
```yaml
debezium:
  enabled: true
  database:
    hostname: ${testcontainer.runtime.mariadb.host:localhost}
    port: ${testcontainer.runtime.mariadb.port:3306}
    user: root
    password: root
    name: primavera
```

### 2. ProductSearchService
Elasticsearch와의 모든 상호작용을 담당하는 서비스입니다.

**제공 메서드:**
- `searchProducts()`: 복합 검색 (키워드, 카테고리, 가격 범위)
- `indexProduct()`: 단일 상품 인덱싱
- `deleteProduct()`: 상품 삭제
- `bulkIndex()`: 대량 인덱싱
- `getIndexHealth()`: Elasticsearch 클러스터 상태 확인

### 3. ProductWebSocketHandler
실시간 상품 업데이트를 WebSocket으로 스트리밍합니다.

**특징:**
- 리액티브 스트리밍 (Flux 기반)
- 자동 연결 관리 및 오류 복구
- JSON 직렬화를 통한 클라이언트 통신

## 🧪 테스트 전략

### 3계층 테스트 접근법

#### 1. 단위 테스트 (DebeziumCdcEventHandlerUnitTest)
- Mockito 기반 격리된 테스트
- CDC 이벤트 처리 로직 검증
- 가격 범위 분류 로직 테스트

```java
@Test
@DisplayName("CREATE 이벤트 처리 테스트")
void shouldHandleCreateEvent() throws Exception {
    // Given
    Struct afterStruct = new Struct(afterSchema)
            .put("id", 1L)
            .put("name", "Test Product")
            .put("price", 50000);

    // When
    RecordChangeEvent<SourceRecord> changeEvent = createChangeEvent("c", afterStruct, null);
    invokeHandleChangeEvent(changeEvent);

    // Then
    verify(productSearchService).indexProduct(any(ProductDocument.class));
}
```

#### 2. 통합 테스트 (DebeziumCdcEventHandlerIntegrationTest)
- 실제 MariaDB 컨테이너 사용
- 데이터베이스 변경 → CDC 이벤트 → Elasticsearch 인덱싱 전체 흐름 검증
- TestContainers를 통한 격리된 테스트 환경

```java
@SpringBootTest
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "mariadb"),
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")
})
public class DebeziumCdcEventHandlerIntegrationTest {
    
    @Test
    @DisplayName("상품 생성 시 CDC 이벤트가 처리되는지 확인")
    void shouldHandleProductCreateEvent() {
        // Given
        String insertSql = "INSERT INTO PRODUCTS (name, price) VALUES ('Test Product', 200000)";
        
        // When
        jdbcTemplate.update(insertSql);
        
        // Then
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> verify(productSearchService, atLeastOnce())
                .indexProduct(any(ProductDocument.class)));
    }
}
```

#### 3. Elasticsearch 템플릿 테스트 (ProductElasticsearchTemplateIntegrationTest)
- Spring Data Elasticsearch 기반 테스트
- 인덱스 생성, 검색, 집계 기능 검증

## 🐳 TestContainers 인프라 구축

### 개선된 TestContainers 설정

#### 1. MariaDB 컨테이너 설정
```yaml
testcontainers:
  containers:
    mariadb:
      type: MARIADB
      mariadb:
        image: "mariadb:11.4.7"
        database: "primavera"
        username: "primavera"
        password: "primavera"
        initScript: "classpath:sql/init.sql"
        command:
          - "--server-id=1"
          - "--log-bin=mysql-bin"
          - "--binlog-format=ROW"
          - "--binlog-row-image=FULL"
```

#### 2. Elasticsearch 컨테이너 설정
```yaml
    elasticsearch:
      type: ELASTICSEARCH
      elasticsearch:
        image: "docker.elastic.co/elasticsearch/elasticsearch:8.13.4"
        environment:
          discovery.type: "single-node"
          xpack.security.enabled: "false"
```

### TestContainers 기능 개선사항

#### MariaDBContainerCreator 개선
- **initScript 지원 추가**: YAML에서 초기화 스크립트 지정 가능
- **커맨드 설정 지원**: 바이너리 로그 활성화 등 DB 옵션 설정

```java
// initScript 적용
if (mariaDbSpec.getInitScript() != null && !mariaDbSpec.getInitScript().isEmpty()) {
    container.withInitScript(mariaDbSpec.getInitScript());
}

// 커맨드 옵션 적용
if (mariaDbSpec.getCommand() != null && !mariaDbSpec.getCommand().isEmpty()) {
    container.withCommand(mariaDbSpec.getCommand().toArray(new String[0]));
}
```

#### ElasticsearchContainerCreator 개선
- **Elasticsearch 8.x 호환성**: 더 이상 사용되지 않는 설정 제거
- **보안 설정 최적화**: 테스트 환경에 적합한 보안 설정

**제거된 deprecated 설정:**
```java
// 더 이상 사용하지 않음
"xpack.license.enabled" // 완전 제거
"xpack.monitoring.enabled" // xpack.monitoring.templates.enabled로 변경
"transport.tcp.port" // transport.port로 변경
```

#### MySQL 컨테이너에도 동일한 개선사항 적용
MariaDB와 동일한 커맨드 설정 기능을 MySQL 컨테이너에도 추가했습니다.

## 🔐 보안 고려사항

### CDC 사용자 권한 관리

**교육 목적 vs 프로덕션 보안**

현재 구현에서는 학습 효과를 위해 `root` 사용자를 사용하고 있지만, 프로덕션 환경에서는 별도의 CDC 전용 사용자를 생성해야 합니다.

#### 프로덕션 권한 설정 예제
```sql
-- CDC 전용 사용자 생성
CREATE USER 'debezium'@'%' IDENTIFIED BY 'strong_password';

-- 필요한 최소 권한 부여
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT 
ON *.* TO 'debezium'@'%';

FLUSH PRIVILEGES;
```

#### 권한 설명
- **SELECT**: 스냅샷 생성 및 데이터 읽기
- **RELOAD**: 테이블 락 관리
- **SHOW DATABASES**: 데이터베이스 목록 조회
- **REPLICATION SLAVE**: 바이너리 로그 읽기
- **REPLICATION CLIENT**: 복제 상태 확인

### SSL 설정
테스트 환경에서는 SSL을 비활성화하지만, 프로덕션에서는 반드시 SSL을 활성화해야 합니다.

```properties
# 테스트 환경
database.ssl.mode=disabled

# 프로덕션 환경
database.ssl.mode=required
database.ssl.ca=/path/to/ca.pem
```

## 🔧 트러블슈팅

### 해결된 주요 문제들

#### 1. initScript 로딩 실패
**문제**: YAML 설정에 initScript를 지정했지만 null로 반환됨
**원인**: TestContainers 설정 구조와 YAML 구조 불일치
**해결책**: YAML 구조 수정 및 MariaDBContainerCreator에 initScript 지원 추가

#### 2. MariaDB 시작 실패 - Binary Log 변수 오류
**문제**: `Variable 'log_bin' is a read only variable` 오류 발생
**원인**: init.sql에서 런타임에 설정할 수 없는 변수 설정 시도
**해결책**: 바이너리 로그 설정을 컨테이너 시작 옵션으로 이동

#### 3. Elasticsearch 8.x 호환성 문제
**문제**: Deprecated 설정으로 인한 컨테이너 시작 실패
**원인**: Elasticsearch 7.x에서 8.x로 업그레이드되면서 설정 변경
**해결책**: 설정 업데이트 및 deprecated 항목 제거

#### 4. Debezium MariaDB 커넥터 누락
**문제**: `ClassNotFoundException: io.debezium.connector.mysql.MySqlConnector`
**원인**: MySQL 커넥터 대신 MariaDB 커넥터 사용 필요
**해결책**: 의존성 추가 및 커넥터 클래스 변경

```gradle
implementation "io.debezium:debezium-connector-mariadb:${debeziumVersion}"
```

#### 5. SSL 모드 오류
**문제**: `Wrong argument value 'preferred' for SslMode`
**원인**: MariaDB 커넥터에서 지원하지 않는 SSL 모드
**해결책**: SSL 비활성화 설정 추가

```properties
database.ssl.mode=disabled
```

### 성능 최적화

#### Debezium 설정 최적화
```properties
# 스냅샷 모드: 기존 데이터를 읽지 않고 변경사항만 추적
snapshot.mode=never

# 오프셋 플러시 간격: 1초마다 진행 상황 저장
offset.flush.interval.ms=1000

# 테이블 필터링: 필요한 테이블만 모니터링
table.include.list=primavera.PRODUCTS
```

#### Elasticsearch 설정 최적화
```java
// 검색 쿼리 최적화
SearchRequest searchRequest = SearchRequest.of(s -> s
    .index(INDEX_NAME)
    .query(finalQuery)
    .size(size)
    .trackTotalHits(t -> t.enabled(true)) // 정확한 개수 추적
);
```

## 📝 변경 이력

### 주요 변경사항

#### Core Implementation
1. **DebeziumCdcEventHandler 구현** (chap17/streaming/src/main/java/com/genius/primavera/streaming/handler/)
   - MariaDB Connector 사용으로 변경
   - 실시간 CDC 이벤트 처리
   - ProductDocument 자동 생성 및 인덱싱
   - 가격 범위 자동 분류 로직

2. **ProductSearchService 완성** (chap17/streaming/src/main/java/com/genius/primavera/streaming/service/)
   - Elasticsearch 8.x 호환성 확보
   - 리액티브 스트리밍 적용
   - 복합 검색 기능 구현

3. **WebSocket 실시간 스트리밍** (chap17/streaming/src/main/java/com/genius/primavera/streaming/)
   - ProductWebSocketHandler 구현
   - 실시간 상품 변경사항 푸시

#### Test Infrastructure
4. **통합 테스트 구축** (chap17/streaming/src/test/)
   - DebeziumCdcEventHandlerIntegrationTest
   - DebeziumCdcEventHandlerUnitTest
   - ProductElasticsearchTemplateIntegrationTest

5. **TestContainers 개선** (appendix/spring-boot-starter-test-containers/)
   - MariaDBContainerCreator: initScript 및 command 지원
   - MySQLContainerCreator: 동일한 기능 추가
   - ElasticsearchContainerCreator: Elasticsearch 8.x 호환성

#### Configuration & Setup
6. **테스트 설정 파일** (chap17/streaming/src/test/resources/)
   - application-test.yml: TestContainers 설정
   - sql/init.sql: 테스트 데이터베이스 스키마

7. **의존성 추가** (chap17/streaming/build.gradle)
   - Debezium MariaDB Connector
   - TestContainers 통합

#### Cleanup
8. **사용하지 않는 코드 정리**
   - TestMariaDBContainer.java 제거 (TestContainers로 대체)
   - WebSocketHandler.java 제거 (ProductWebSocketHandler 사용)
   - WebSocketConfiguration.java 제거 (중복된 설정)

### 파일별 상세 변경사항

#### DebeziumCdcEventHandler.java
```java
// MySQL에서 MariaDB 커넥터로 변경
props.setProperty("connector.class", "io.debezium.connector.mariadb.MariaDbConnector");

// SSL 비활성화
props.setProperty("database.ssl.mode", "disabled");

// null 체크 강화
if (indexResult != null) {
    indexResult.doOnSuccess(...).subscribe();
} else {
    log.warn("ProductSearchService.indexProduct() returned null");
}
```

#### TestContainers Configuration
```yaml
# 이전: 하드코딩된 설정
# 이후: YAML 기반 유연한 설정
testcontainers:
  containers:
    mariadb:
      command:
        - "--server-id=1"
        - "--log-bin=mysql-bin"
        - "--binlog-format=ROW"
```

#### Elasticsearch Configuration
```java
// 제거된 deprecated 설정들
- "xpack.license.enabled"
- "xpack.monitoring.enabled" → "xpack.monitoring.templates.enabled"
- "transport.tcp.port" → "transport.port"
```

## 🚀 사용법

### 개발 환경 실행

#### 1. Docker 인프라 시작
```bash
# MariaDB와 Elasticsearch 컨테이너 시작
docker-compose up -d
```

#### 2. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
SPRING_PROFILES_ACTIVE=local ./gradlew :chap17:streaming:bootRun
```

#### 3. CDC 활성화를 위한 설정
```bash
# Vault 토큰과 함께 실행 (설정이 Vault에 저장된 경우)
VAULT_TOKEN=primavera-vault-token SPRING_PROFILES_ACTIVE=local ./gradlew :chap17:streaming:bootRun
```

### 테스트 실행

#### 단위 테스트
```bash
./gradlew :chap17:streaming:test --tests "*UnitTest"
```

#### 통합 테스트
```bash
./gradlew :chap17:streaming:test --tests "*IntegrationTest"
```

#### 전체 테스트
```bash
./gradlew :chap17:streaming:test
```

### API 사용 예제

#### 상품 검색
```bash
curl -X GET "http://localhost:8080/api/products/search?query=laptop&minPrice=500000&maxPrice=2000000&size=10"
```

#### Elasticsearch 상태 확인
```bash
curl -X GET "http://localhost:8080/api/products/health"
```

#### WebSocket 연결
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/products');
ws.onmessage = function(event) {
    const product = JSON.parse(event.data);
    console.log('Product updated:', product);
};
```

### CDC 테스트

#### 상품 생성
```sql
INSERT INTO PRODUCTS (name, description, price, category) 
VALUES ('New Laptop', 'Gaming Laptop', 1500000, 'Electronics');
```

#### 상품 수정
```sql
UPDATE PRODUCTS SET price = 1200000 WHERE id = 1;
```

#### 상품 삭제
```sql
DELETE FROM PRODUCTS WHERE id = 1;
```

각 데이터베이스 변경 후 Elasticsearch에 자동으로 반영되며, WebSocket을 통해 실시간으로 클라이언트에 전달됩니다.

---

## 📞 지원

이 구현에 대한 질문이나 개선 사항이 있다면 다음을 참고하세요:

- [Debezium 공식 문서](https://debezium.io/documentation/)
- [TestContainers 가이드](https://www.testcontainers.org/)
- [Elasticsearch Java Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/index.html)
- [Spring WebFlux Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)

---

## 🎓 기존 학습 목표

- **리액티브 프로그래밍**: WebFlux와 Reactor를 사용한 비동기 처리
- **WebSocket 통신**: 실시간 양방향 통신 구현
- **Server-Sent Events**: 서버에서 클라이언트로의 실시간 푸시
- **R2DBC**: 리액티브 데이터베이스 액세스
- **CDC 이벤트 스트리밍**: 실시간 데이터 변경 감지 및 전파

## 📖 관련 문서

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [R2DBC Documentation](https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/)
- [WebSocket API Guide](https://spring.io/guides/gs/messaging-stomp-websocket/)

---
*이 문서는 Primavera Spring Boot 교육 프로젝트의 일부이며, 실제 프로덕션 환경 적용 시 보안 및 성능 검토가 필요합니다.*