# Chapter 18 Front - API Gateway 및 서비스 오케스트레이션

Spring Boot 교육용 프로젝트 Primavera의 Chapter 18 Front 모듈입니다. API Gateway 패턴과 서비스 오케스트레이션을 통한 마이크로서비스 통합, 로드 밸런싱, 그리고 장애 격리를 학습합니다.

## 🎯 학습 목표

- **API Gateway 패턴**: 마이크로서비스의 단일 진입점 구현
- **서비스 오케스트레이션**: 여러 서비스 조합을 통한 비즈니스 로직 구현
- **부하 분산**: 서비스 인스턴스 간 로드 밸런싱
- **장애 격리**: Circuit Breaker를 통한 시스템 안정성 확보
- **응답 집계**: 여러 서비스의 데이터를 통합한 API 제공

## 📁 프로젝트 구조

```
chap18/front/
├── src/main/java/com/genius/primavera/
│   ├── FrontApplication.java            # 메인 애플리케이션
│   ├── FrontController.java             # API Gateway 컨트롤러
│   ├── FrontHandler.java                # WebFlux 핸들러
│   ├── FrontRouter.java                 # 함수형 라우팅
│   ├── FrontService.java                # 서비스 인터페이스
│   ├── FrontServiceImpl.java            # 서비스 구현체
│   ├── Config.java                      # 동적 설정 관리
│   ├── Info.java                        # 서비스 정보
│   ├── LoadTesting.java                 # 부하 테스트 유틸
│   ├── FrontOrder.java                  # 통합 주문 모델
│   ├── OrderAndProduct.java             # 주문-상품 매핑 모델
│   ├── User.java                        # 사용자 모델
│   ├── Order.java                       # 주문 모델
│   ├── Product.java                     # 상품 모델
│   └── Category.java                    # 카테고리 모델
├── src/main/resources/
│   └── application.yaml                # 애플리케이션 설정
└── build.gradle                        # WebFlux + Cloud Config 의존성
```

## 🏗 아키텍처 특성

### 1. API Gateway 패턴
```java
@RefreshScope
@SpringBootApplication
@EnableConfigurationProperties({Config.class})
public class FrontApplication {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .connectTimeout(Duration.ofMillis(1000))
            .readTimeout(Duration.ofMillis(3000))
            .errorHandler(new CustomErrorHandler())
            .interceptors(new LoggingInterceptor())
            .build();
    }
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }
}
```

### 2. 함수형 라우팅 (WebFlux)
```java
@Configuration
public class FrontRouter {
    
    @Bean
    public RouterFunction<ServerResponse> routes(FrontHandler handler) {
        return RouterFunctions
            .route(GET("/api/users/{userId}/orders"), handler::getUserOrders)
            .andRoute(GET("/api/users/{userId}/dashboard"), handler::getUserDashboard)
            .andRoute(GET("/api/aggregated/{userId}"), handler::getAggregatedData)
            .andRoute(GET("/api/health"), handler::healthCheck)
            .filter(this::loggingFilter);
    }
    
    private Mono<ServerResponse> loggingFilter(ServerRequest request, 
                                              HandlerFunction<ServerResponse> next) {
        log.info("Processing request: {} {}", request.method(), request.path());
        return next.handle(request)
            .doOnSuccess(response -> log.info("Response status: {}", response.statusCode()));
    }
}
```

### 3. 서비스 오케스트레이션
```java
@Component
public class FrontHandler {
    
    private final FrontService frontService;
    private final WebClient webClient;
    
    public Mono<ServerResponse> getAggregatedData(ServerRequest request) {
        String userId = request.pathVariable("userId");
        
        return Mono.zip(
            getUserFromAccountService(userId),
            getOrdersFromOrderService(userId),
            getProductsFromProductService()
        )
        .map(tuple -> FrontOrder.builder()
            .user(tuple.getT1())
            .orders(tuple.getT2())
            .products(tuple.getT3())
            .build())
        .flatMap(frontOrder -> ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(frontOrder))
        .onErrorResume(this::handleError);
    }
}
```

## 🎯 핵심 기능

### 1. 마이크로서비스 통합
```java
@Service
public class FrontServiceImpl implements FrontService {
    
    @Value("${services.account.url:http://localhost:8081}")
    private String accountServiceUrl;
    
    @Value("${services.order.url:http://localhost:8082}")
    private String orderServiceUrl;
    
    @Value("${services.product.url:http://localhost:8083}")
    private String productServiceUrl;
    
    @Override
    public Mono<FrontOrder> findAllOrders(String userId) {
        return Mono.zip(
            getUser(userId),
            getOrders(userId),
            getProducts()
        ).map(tuple -> FrontOrder.builder()
            .user(tuple.getT1())
            .orders(tuple.getT2())
            .products(tuple.getT3())
            .orderAndProduct(buildOrderAndProduct(tuple.getT2(), tuple.getT3()))
            .build());
    }
    
    private Mono<User> getUser(String userId) {
        return webClient.get()
            .uri(accountServiceUrl + "/users/{userId}", userId)
            .retrieve()
            .bodyToMono(User.class)
            .timeout(Duration.ofSeconds(5))
            .onErrorReturn(User.builder().id(Long.valueOf(userId)).name("Unknown").build());
    }
}
```

### 2. Circuit Breaker 및 Fallback
```java
@Component
public class ResilientFrontService {
    
    @CircuitBreaker(name = "account-service", fallbackMethod = "fallbackUser")
    @TimeLimiter(name = "account-service")
    @Retry(name = "account-service")
    public CompletableFuture<User> getUserWithResilience(String userId) {
        return CompletableFuture.supplyAsync(() -> 
            restTemplate.getForObject(
                accountServiceUrl + "/users/{userId}", 
                User.class, userId)
        );
    }
    
    public CompletableFuture<User> fallbackUser(String userId, Exception ex) {
        log.warn("Account service unavailable, returning fallback user for: {}", userId);
        return CompletableFuture.completedFuture(
            User.builder()
                .id(Long.valueOf(userId))
                .name("Fallback User")
                .build()
        );
    }
}
```

### 3. 응답 집계 및 데이터 변환
```java
@RestController
public class FrontController {
    
    @GetMapping("/api/users/{userId}/dashboard")
    public Mono<ResponseEntity<DashboardResponse>> getDashboard(@PathVariable String userId) {
        return frontService.findAllOrders(userId)
            .map(frontOrder -> {
                DashboardResponse dashboard = DashboardResponse.builder()
                    .userInfo(frontOrder.getUser())
                    .totalOrders(frontOrder.getOrders().size())
                    .recentOrders(getRecentOrders(frontOrder.getOrders(), 5))
                    .favoriteProducts(getFavoriteProducts(frontOrder))
                    .orderSummary(calculateOrderSummary(frontOrder.getOrders()))
                    .build();
                    
                return ResponseEntity.ok(dashboard);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(DashboardResponse.empty()));
    }
    
    private OrderSummary calculateOrderSummary(List<Order> orders) {
        return OrderSummary.builder()
            .totalAmount(orders.stream()
                .mapToLong(Order::getAmount)
                .sum())
            .averageAmount(orders.stream()
                .mapToLong(Order::getAmount)
                .average()
                .orElse(0.0))
            .orderCount(orders.size())
            .build();
    }
}
```

### 4. 동적 설정 관리
```java
@Component
@ConfigurationProperties("front")
@RefreshScope
public class Config {
    private Map<String, ServiceConfig> services = new HashMap<>();
    private Integer defaultTimeout = 5000;
    private Integer maxRetries = 3;
    private Boolean circuitBreakerEnabled = true;
    
    @Data
    public static class ServiceConfig {
        private String url;
        private Integer timeout;
        private Integer retries;
        private Boolean enabled = true;
    }
    
    public String getServiceUrl(String serviceName) {
        ServiceConfig config = services.get(serviceName);
        return config != null ? config.getUrl() : "http://localhost:8080";
    }
}
```

### 5. 부하 테스트 및 모니터링
```java
@Component
public class LoadTesting {
    
    private final FrontService frontService;
    private final MeterRegistry meterRegistry;
    
    @EventListener(ApplicationReadyEvent.class)
    public void startLoadTesting() {
        if (isLoadTestingEnabled()) {
            scheduleLoadTest();
        }
    }
    
    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    public void performLoadTest() {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        CompletableFuture.allOf(
            IntStream.range(1, 11)
                .mapToObj(i -> frontService.findAllOrders(String.valueOf(i))
                    .toFuture())
                .toArray(CompletableFuture[]::new)
        )
        .thenRun(() -> sample.stop(Timer.builder("load.test.duration")
            .register(meterRegistry)))
        .exceptionally(throwable -> {
            meterRegistry.counter("load.test.errors").increment();
            log.error("Load test failed", throwable);
            return null;
        });
    }
}
```

## 🛠 기술 스택

### 핵심 기술
- **Java**: 21
- **Spring Boot**: 3.3.6
- **Spring WebFlux**: 리액티브 웹 프레임워크
- **Spring Cloud Config**: 중앙화된 설정 관리

### 통신 및 클라이언트
- **WebClient**: 리액티브 HTTP 클라이언트
- **RestTemplate**: 동기 HTTP 클라이언트
- **Spring Cloud Gateway**: API 게이트웨이 기능

### 장애 복구
- **Resilience4j**: Circuit Breaker, Retry, Rate Limiter
- **Spring Retry**: 재시도 메커니즘

## 🚀 실행 방법

### 1. 전체 마이크로서비스 시작
```bash
# 1. Config Server 시작 (필수)
./gradlew :chap18:configuration:bootRun &

# 2. 백엔드 서비스들 병렬 시작
./gradlew :chap18:account:bootRun &
./gradlew :chap18:product:bootRun &
./gradlew :chap18:order:bootRun &

# 3. 모든 서비스가 시작될 때까지 대기 (약 30초)
sleep 30

# 4. Front Service (API Gateway) 시작
./gradlew :chap18:front:bootRun
```

### 2. Docker Compose로 전체 시스템 시작
```bash
# 인프라 서비스 시작 (MariaDB, Kafka, Redis)
./docker-manager.sh start-all chap18

# 모든 마이크로서비스 시작
docker-compose -f docker-compose-chap18.yml up -d
```

### 3. API 테스트
```bash
# API Gateway 상태 확인
curl http://localhost:8080/actuator/health

# 통합 사용자 정보 조회
curl http://localhost:8080/api/users/1/dashboard

# 사용자 주문 전체 정보
curl http://localhost:8080/api/users/1/orders

# 집계된 데이터 조회
curl http://localhost:8080/api/aggregated/1

# 설정 동적 갱신
curl -X POST http://localhost:8080/actuator/refresh
```

## 📋 테스트 실행

### API Gateway 테스트
```bash
# 전체 테스트 실행
./gradlew :chap18:front:test

# 통합 테스트
./gradlew :chap18:front:test --tests "*IntegrationTest"

# Circuit Breaker 테스트
./gradlew :chap18:front:test --tests "*CircuitBreakerTest"

# 부하 테스트
./gradlew :chap18:front:test --tests "*LoadTest"
```

### 테스트 예시
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.profiles.active=test")
class FrontServiceIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private WebClient webClient;
    
    @Test
    void shouldReturnAggregatedDataWhenAllServicesAvailable() {
        // Given
        mockServiceResponses();
        
        // When & Then
        webTestClient.get()
            .uri("/api/users/1/dashboard")
            .exchange()
            .expectStatus().isOk()
            .expectBody(DashboardResponse.class)
            .value(response -> {
                assertThat(response.getUserInfo()).isNotNull();
                assertThat(response.getTotalOrders()).isGreaterThan(0);
                assertThat(response.getOrderSummary()).isNotNull();
            });
    }
    
    @Test
    void shouldReturnFallbackWhenServiceUnavailable() {
        // Given
        mockServiceFailures();
        
        // When & Then
        webTestClient.get()
            .uri("/api/users/1/dashboard")
            .exchange()
            .expectStatus().isOk()
            .expectBody(DashboardResponse.class)
            .value(response -> {
                assertThat(response.getUserInfo().getName()).isEqualTo("Fallback User");
            });
    }
}
```

## 🎓 핵심 학습 포인트

### 1. API Gateway 패턴
```java
// 단일 진입점을 통한 마이크로서비스 액세스
@GetMapping("/api/**")
public Mono<ResponseEntity<Object>> proxyToBackend(ServerHttpRequest request) {
    String serviceName = extractServiceName(request.getPath());
    String targetUrl = serviceDiscovery.getServiceUrl(serviceName);
    
    return webClient.method(request.getMethod())
        .uri(targetUrl + request.getPath())
        .headers(headers -> headers.addAll(request.getHeaders()))
        .exchange()
        .flatMap(this::handleResponse);
}
```

### 2. 서비스 오케스트레이션
```java
// 여러 서비스를 조합한 비즈니스 로직
public Mono<OrderProcessingResult> processCompleteOrder(CreateOrderRequest request) {
    return validateUser(request.getUserId())
        .flatMap(user -> checkProductAvailability(request.getItems()))
        .flatMap(products -> reserveInventory(request.getItems()))
        .flatMap(reservation -> processPayment(request.getPayment()))
        .flatMap(payment -> createOrder(request))
        .flatMap(order -> sendConfirmationEmail(order))
        .map(OrderProcessingResult::success)
        .onErrorResume(this::handleOrderProcessingError);
}
```

### 3. Circuit Breaker 패턴
```java
// 장애 전파 방지 및 빠른 실패
@CircuitBreaker(name = "order-service")
public Mono<List<Order>> getOrdersWithCircuitBreaker(String userId) {
    return webClient.get()
        .uri("/orders?userId=" + userId)
        .retrieve()
        .bodyToFlux(Order.class)
        .collectList()
        .timeout(Duration.ofSeconds(3))
        .doOnError(error -> log.error("Order service call failed", error));
}
```

### 4. 응답 집계 패턴
```java
// 여러 서비스의 데이터를 통합
public Mono<AggregatedResponse> aggregateUserData(String userId) {
    return Mono.zip(
        userService.getUser(userId),
        orderService.getOrders(userId),
        recommendationService.getRecommendations(userId),
        loyaltyService.getPoints(userId)
    ).map(tuple -> AggregatedResponse.builder()
        .user(tuple.getT1())
        .orders(tuple.getT2())
        .recommendations(tuple.getT3())
        .loyaltyPoints(tuple.getT4())
        .build());
}
```

## 📚 주요 애너테이션

### API Gateway 관련
- `@RestController`: REST API 엔드포인트
- `@GetMapping`: HTTP GET 매핑
- `@PathVariable`: 경로 변수 바인딩

### 리액티브 관련
- `@Bean RouterFunction`: 함수형 라우팅
- `WebClient`: 리액티브 HTTP 클라이언트

### 장애 복구 관련
- `@CircuitBreaker`: Circuit Breaker 패턴
- `@Retry`: 재시도 메커니즘
- `@TimeLimiter`: 타임아웃 제한

### 설정 관리
- `@RefreshScope`: 동적 설정 갱신
- `@ConfigurationProperties`: 설정 바인딩

## 🔧 운영 및 모니터링

### 1. 메트릭 및 모니터링
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,refresh,circuitbreakers
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

### 2. 분산 추적
```java
@Bean
public Tracer tracer() {
    return Tracing.newBuilder()
        .localServiceName("front-service")
        .sampler(Sampler.create(1.0f)) // 100% 샘플링
        .build()
        .tracer();
}
```

### 3. 로드 밸런싱
```java
@Component
public class LoadBalancer {
    private final List<String> instances;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public String getNextInstance() {
        int index = currentIndex.getAndIncrement() % instances.size();
        return instances.get(index);
    }
}
```

## 🔄 다음 단계

1. **서비스 메시**: Istio를 활용한 고급 마이크로서비스 관리
2. **분산 추적**: Zipkin/Jaeger를 통한 요청 추적 및 성능 분석
3. **보안 강화**: OAuth2/JWT 기반 인증 및 권한 부여
4. **컨테이너 오케스트레이션**: Kubernetes에서의 마이크로서비스 배포

## 📖 관련 문서

- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Service Orchestration vs Choreography](https://microservices.io/patterns/data/saga.html)