# Front Service - API Gateway & Orchestration

## 📋 Overview

Front Service는 Primavera 마이크로서비스 아키텍처에서 API Gateway 역할과 서비스 오케스트레이션을 담당하는 프론트엔드 서비스입니다. Spring WebFlux와 RestTemplate을 활용하여 여러 마이크로서비스를 조합하고 통합된 API를 제공합니다.

## 🏗️ 아키텍처 특성

### Core Technologies
- **Spring Boot 3.3.6**: 최신 스프링 부트 프레임워크
- **Spring WebFlux**: 비동기 반응형 웹 프레임워크
- **Spring Cloud Config**: 중앙집중식 설정 관리
- **RestTemplate**: HTTP 클라이언트 통신
- **@RefreshScope**: 동적 설정 갱신

### Service Orchestration Pattern
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
            .build();
    }
}
```

## 🚀 주요 기능

### 1. API Gateway 역할
- **서비스 라우팅**: 클라이언트 요청을 적절한 마이크로서비스로 라우팅
- **요청 집계**: 여러 서비스의 응답을 조합하여 통합된 결과 제공
- **로드 밸런싱**: 다수의 서비스 인스턴스 간 부하 분산
- **서킷 브레이커**: 장애 서비스 격리 및 안정성 확보

### 2. 서비스 오케스트레이션
```java
@Service
public class FrontServiceImpl implements FrontService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public FrontOrder findAllOrders(String userId) {
        // 1. Account Service에서 사용자 정보 조회
        User user = restTemplate.getForObject(
            "http://localhost:8081/users/{userId}", 
            User.class, userId);
        
        // 2. Order Service에서 주문 정보 조회
        Order[] orders = restTemplate.getForObject(
            "http://localhost:8082/users/{userId}/orders", 
            Order[].class, userId);
        
        // 3. Product Service에서 상품 정보 조회
        Product[] products = restTemplate.getForObject(
            "http://localhost:8083/products", 
            Product[].class);
        
        // 4. 데이터 조합 및 반환
        return FrontOrder.builder()
            .user(user)
            .orders(Arrays.asList(orders))
            .products(Arrays.asList(products))
            .build();
    }
}
```

### 3. 데이터 모델 통합
```java
@Data
@Builder
public class FrontOrder {
    private User user;
    private List<Order> orders;
    private List<Product> products;
    private OrderAndProduct orderAndProduct;
}

@Data
@Builder  
public class OrderAndProduct {
    private Long orderId;
    private Long productId;
    private String productName;
    private Long amount;
    private Category category;
}
```

## 🔧 설정 및 구성

### 애플리케이션 설정
```yaml
spring:
  application:
    name: front
  cloud:
    config:
      uri: http://localhost:8888  # Config Server 연결

server:
  port: 8080                     # Gateway 표준 포트
  tomcat:
    threads:
      max: 1                     # WebFlux 단일 스레드

management:
  endpoints:
    web:
      exposure:
        include: refresh         # 동적 설정 갱신
```

### RestTemplate 설정
```java
@Configuration
public class WebClientConfiguration {
    
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
            .connectTimeout(Duration.ofMillis(1000))
            .readTimeout(Duration.ofMillis(3000))
            .errorHandler(new CustomErrorHandler())
            .interceptors(new LoggingInterceptor());
    }
}
```

### 동적 설정 관리
```java
@Component
@ConfigurationProperties("front")
@RefreshScope
public class Config {
    private String accountServiceUrl;
    private String orderServiceUrl;
    private String productServiceUrl;
    private Integer timeoutMs;
    
    // getters and setters
}
```

## 📊 서비스 통합 패턴

### 1. Fan-Out Pattern (병렬 호출)
```java
@Service
public class ParallelFrontService {
    
    public CompletableFuture<FrontOrder> findAllOrdersAsync(String userId) {
        CompletableFuture<User> userFuture = CompletableFuture
            .supplyAsync(() -> getUserFromAccountService(userId));
            
        CompletableFuture<List<Order>> ordersFuture = CompletableFuture
            .supplyAsync(() -> getOrdersFromOrderService(userId));
            
        CompletableFuture<List<Product>> productsFuture = CompletableFuture
            .supplyAsync(() -> getProductsFromProductService());
        
        return CompletableFuture.allOf(userFuture, ordersFuture, productsFuture)
            .thenApply(v -> FrontOrder.builder()
                .user(userFuture.join())
                .orders(ordersFuture.join())
                .products(productsFuture.join())
                .build());
    }
}
```

### 2. Circuit Breaker Pattern
```java
@Component
public class ResilientFrontService {
    
    @CircuitBreaker(name = "account-service", fallbackMethod = "fallbackUser")
    public User getUser(String userId) {
        return restTemplate.getForObject(
            "http://localhost:8081/users/{userId}", 
            User.class, userId);
    }
    
    public User fallbackUser(String userId, Exception ex) {
        return User.builder()
            .id(Long.parseLong(userId))
            .name("Unknown User")
            .build();
    }
}
```

### 3. Response Aggregation Pattern
```java
@RestController
public class FrontController {
    
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardData> getDashboard(@PathVariable String userId) {
        // 여러 서비스 호출 및 데이터 집계
        DashboardData dashboard = DashboardData.builder()
            .userInfo(accountService.getUser(userId))
            .recentOrders(orderService.getRecentOrders(userId, 5))
            .recommendedProducts(productService.getRecommendations(userId))
            .orderSummary(analyticsService.getOrderSummary(userId))
            .build();
            
        return ResponseEntity.ok(dashboard);
    }
}
```

## 🌐 API 엔드포인트

### 통합 API
```http
# 사용자 대시보드 (통합 정보)
GET /dashboard/{userId}

# 사용자 주문 전체 정보 (User + Orders + Products)
GET /users/{userId}/orders/full

# 주문-상품 매핑 정보
GET /users/{userId}/order-products

# 상품별 주문 통계
GET /products/{productId}/order-stats

# 카테고리별 주문 현황
GET /categories/{categoryId}/orders
```

### Gateway API (프록시)
```http
# Account Service 프록시
GET /api/accounts/{userId}
POST /api/accounts
PUT /api/accounts/{userId}

# Order Service 프록시  
GET /api/orders/{userId}
POST /api/orders

# Product Service 프록시
GET /api/products
GET /api/products/{productId}
```

### 관리 API
```http
# 설정 갱신
POST /actuator/refresh

# Health Check
GET /actuator/health

# 서비스 상태 확인
GET /actuator/info
```

## 🏃‍♂️ 실행 방법

### 1. 의존 서비스 시작
```bash
# 1. Configuration Service 시작
./gradlew :chap18:configuration:bootRun

# 2. Account Service 시작  
./gradlew :chap18:account:bootRun

# 3. Order Service 시작
./gradlew :chap18:order:bootRun

# 4. Product Service 시작
./gradlew :chap18:product:bootRun
```

### 2. Front Service 시작
```bash
# Front Service 실행
./gradlew :chap18:front:bootRun

# 또는 JAR 실행
java -jar front/build/libs/front.jar
```

### 3. 서비스 동작 확인
```bash
# Front Service 상태 확인
curl http://localhost:8080/actuator/health

# 통합 API 테스트
curl http://localhost:8080/users/1/orders/full

# 대시보드 API 테스트
curl http://localhost:8080/dashboard/1
```

## 🔗 서비스 통합 및 통신

### 마이크로서비스 연동
```java
@Component
public class ServiceClient {
    
    @Value("${services.account.url:http://localhost:8081}")
    private String accountServiceUrl;
    
    @Value("${services.order.url:http://localhost:8082}")  
    private String orderServiceUrl;
    
    @Value("${services.product.url:http://localhost:8083}")
    private String productServiceUrl;
    
    public User getUser(String userId) {
        return restTemplate.getForObject(
            accountServiceUrl + "/users/{userId}", 
            User.class, userId);
    }
    
    public List<Order> getOrders(String userId) {
        Order[] orders = restTemplate.getForObject(
            orderServiceUrl + "/users/{userId}/orders", 
            Order[].class, userId);
        return Arrays.asList(orders);
    }
    
    public List<Product> getProducts() {
        Product[] products = restTemplate.getForObject(
            productServiceUrl + "/products", 
            Product[].class);
        return Arrays.asList(products);
    }
}
```

### 에러 처리 및 Fallback
```java
@Service
public class ResilientFrontService {
    
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public FrontOrder getFrontOrderWithRetry(String userId) {
        try {
            return frontService.findAllOrders(userId);
        } catch (Exception e) {
            log.warn("Service call failed, retrying...", e);
            throw e;
        }
    }
    
    @Recover
    public FrontOrder recover(Exception ex, String userId) {
        log.error("All retry attempts failed for user: {}", userId, ex);
        return FrontOrder.builder()
            .user(getDefaultUser(userId))
            .orders(Collections.emptyList())
            .products(Collections.emptyList())
            .build();
    }
}
```

## 📈 성능 최적화

### 1. 비동기 처리
```java
@Service
public class AsyncFrontService {
    
    @Async
    public CompletableFuture<User> getUserAsync(String userId) {
        return CompletableFuture.completedFuture(
            restTemplate.getForObject(
                "http://localhost:8081/users/{userId}", 
                User.class, userId));
    }
    
    @Async
    public CompletableFuture<List<Order>> getOrdersAsync(String userId) {
        Order[] orders = restTemplate.getForObject(
            "http://localhost:8082/users/{userId}/orders", 
            Order[].class, userId);
        return CompletableFuture.completedFuture(Arrays.asList(orders));
    }
}
```

### 2. 캐싱 전략
```java
@Service
public class CachedFrontService {
    
    @Cacheable(value = "products", unless = "#result.size() == 0")
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }
    
    @Cacheable(value = "userOrders", key = "#userId")
    public FrontOrder getUserOrders(String userId) {
        return frontService.findAllOrders(userId);
    }
    
    @CacheEvict(value = "userOrders", key = "#userId")
    public void evictUserOrdersCache(String userId) {
        // 캐시 무효화
    }
}
```

### 3. Connection Pool 최적화
```java
@Configuration
public class HttpClientConfiguration {
    
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(3000);
        
        PoolingHttpClientConnectionManager connectionManager = 
            new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);
        
        HttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .build();
            
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }
}
```

## 🛡️ 보안 및 인증

### 1. JWT Token 전파
```java
@Component
public class TokenPropagationInterceptor implements ClientHttpRequestInterceptor {
    
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        String token = getCurrentUserToken();
        if (token != null) {
            request.getHeaders().add("Authorization", "Bearer " + token);
        }
        
        return execution.execute(request, body);
    }
}
```

### 2. API Rate Limiting
```java
@RestController
public class RateLimitedController {
    
    @RateLimiter(name = "front-api", fallbackMethod = "fallbackResponse")
    @GetMapping("/users/{userId}/orders/full")
    public ResponseEntity<FrontOrder> getUserOrdersFull(@PathVariable String userId) {
        return ResponseEntity.ok(frontService.findAllOrders(userId));
    }
    
    public ResponseEntity<String> fallbackResponse(String userId, Exception ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body("API rate limit exceeded. Please try again later.");
    }
}
```

## 🧪 테스트 전략

### 통합 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FrontServiceIntegrationTest {
    
    @MockBean
    private RestTemplate restTemplate;
    
    @Test
    void shouldReturnFrontOrderWhenAllServicesAvailable() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(User.class), anyString()))
            .thenReturn(mockUser());
        when(restTemplate.getForObject(anyString(), eq(Order[].class), anyString()))
            .thenReturn(mockOrders());
        when(restTemplate.getForObject(anyString(), eq(Product[].class)))
            .thenReturn(mockProducts());
        
        // When
        FrontOrder result = frontService.findAllOrders("1");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getOrders()).isNotEmpty();
        assertThat(result.getProducts()).isNotEmpty();
    }
}
```

### 계약 테스트 (Contract Testing)
```java
@Test
void shouldCallAccountServiceWithCorrectContract() {
    // Account Service API 계약 검증
    wireMockServer.stubFor(get(urlEqualTo("/users/1"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                    "id": 1,
                    "name": "test_user",
                    "createDate": "2024-01-15T10:30:00Z"
                }
                """)));
}
```

## 📚 학습 포인트

이 Front Service는 다음과 같은 마이크로서비스 아키텍처 패턴들을 학습할 수 있습니다:

1. **API Gateway Pattern**: 단일 진입점을 통한 마이크로서비스 액세스
2. **Service Orchestration**: 여러 서비스의 조합을 통한 비즈니스 로직 구현
3. **Circuit Breaker Pattern**: 장애 전파 방지 및 시스템 안정성 확보
4. **Response Aggregation**: 다수 서비스 응답의 통합 및 변환
5. **Asynchronous Communication**: 비동기 통신을 통한 성능 최적화
6. **Dynamic Configuration**: 런타임 설정 변경 및 적용

Front Service는 마이크로서비스 아키텍처에서 서비스 간 통합과 오케스트레이션의 핵심 역할을 담당하며, 실제 운영 환경에서의 API Gateway 패턴을 학습할 수 있는 실용적인 예제입니다.