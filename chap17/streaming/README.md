# Chapter 17 Streaming - 리액티브 스트리밍 및 실시간 통신

Spring Boot 교육용 프로젝트 Primavera의 Chapter 17 스트리밍 모듈입니다. WebFlux를 사용한 리액티브 프로그래밍과 WebSocket을 통한 실시간 양방향 통신을 학습합니다.

## 🎯 학습 목표

- **리액티브 프로그래밍**: WebFlux와 Reactor를 사용한 비동기 처리
- **WebSocket 통신**: 실시간 양방향 통신 구현
- **Server-Sent Events**: 서버에서 클라이언트로의 실시간 푸시
- **R2DBC**: 리액티브 데이터베이스 액세스
- **CDC 이벤트 스트리밍**: 실시간 데이터 변경 감지 및 전파

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

## 📖 관련 문서

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [R2DBC Documentation](https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/)
- [WebSocket API Guide](https://spring.io/guides/gs/messaging-stomp-websocket/)