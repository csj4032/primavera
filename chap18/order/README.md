# Order Service - Reactive Order Management

## 📋 Overview

Order Service는 Primavera 마이크로서비스 아키텍처에서 주문 관리를 담당하는 핵심 서비스입니다. Spring WebFlux와 R2DBC를 활용한 완전한 반응형(Reactive) 스택으로 구축되어 높은 동시성과 성능을 제공하며, 함수형 라우팅과 할인 정책 엔진을 포함합니다.

## 🏗️ 아키텍처 특성

### Core Technologies
- **Spring Boot 3.3.6**: 최신 스프링 부트 프레임워크
- **Spring WebFlux**: 완전한 반응형 웹 스택
- **R2DBC (MariaDB)**: 반응형 데이터베이스 드라이버
- **RouterFunction**: 함수형 라우팅 패턴
- **Spring Cloud Config**: 중앙집중식 설정 관리

### Functional Reactive Pattern
```java
@SpringBootConfiguration
@EnableAutoConfiguration
public class OrderApplication {
    
    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class)
            .initializers((GenericApplicationContext context) -> {
                context.registerBean(RouterFunction.class, () -> {
                    var orderRepository = context.getBean(OrderRepository.class);
                    var orderService = new OrderServiceImpl(orderRepository);
                    return route()
                        .GET("/users/{userId}/orders", request -> 
                            ok().body(orderService.findByUserId(
                                request.pathVariable("userId")), Order.class))
                        .build();
                });
            })
            .build()
            .run(args);
    }
}
```

## 🚀 주요 기능

### 1. 반응형 주문 관리
- **비동기 주문 처리**: Mono/Flux를 활용한 완전한 비동기 처리
- **R2DBC 연동**: 반응형 데이터베이스 액세스
- **백프레셔 제어**: 시스템 부하 제어 및 안정성 확보
- **함수형 라우팅**: WebFlux RouterFunction 패턴

### 2. 주문 엔티티 모델
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private Long productId;
    private Long amount;
    
    // 생성자 오버로딩 (ID 자동생성용)
    public Order(Long userId, Long productId, Long amount) {
        this.userId = userId;
        this.productId = productId;
        this.amount = amount;
    }
}
```

### 3. 할인 정책 엔진 (Saleed Package)
```java
@Service
public class SaleCommand {
    private final Map<SaleRoleType, Saleable> discountableMap;
    
    public SaleCommand(SaleRoleTable discountRoleTable) {
        this.discountableMap = discountRoleTable.getDiscountableTable();
    }
    
    public boolean isSaleable(final Order order, Set<SaleRoleType> discountRoleTypes) {
        return discountRoleTypes.stream()
            .map(r -> discountableMap.get(r).isSaleable(order))
            .count() == discountRoleTypes.size();
    }
}
```

### 4. 할인 규칙 구현체
```java
// 금액 기반 할인
@Component
public class AmountSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        return order.getAmount() >= 10000L; // 1만원 이상
    }
}

// 재고 기반 할인
@Component  
public class StockSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        return order.getProductId() <= 50L; // 특정 상품만
    }
}

// 이벤트 기반 할인
@Component
public class EventSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        return LocalDateTime.now().getHour() >= 14; // 오후 2시 이후
    }
}

// 법적 제약 검증
@Component
public class LegalSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        return order.getUserId() >= 1L; // 유효한 사용자
    }
}
```

## 🔧 설정 및 구성

### 애플리케이션 설정
```yaml
spring:
  application:
    name: order
  cloud:
    config:
      uri: http://localhost:8888
  r2dbc:
    url: r2dbc:pool:mariadb://localhost:3306/primavera?useLegacyDatetimeCode=false&serverTimezone=Asia/Seoul
    username: primavera
    password: primavera
    pool:
      initial-size: 10
      max-size: 20
      max-idle-time: 300m
      validation-query: SELECT 1

server:
  port: 8082
  tomcat:
    threads:
      max: 1                     # 반응형 단일 스레드
```

### R2DBC 연결풀 설정
```java
@Configuration
public class R2dbcConfiguration {
    
    @Bean
    public ConnectionFactory connectionFactory() {
        MariadbConnectionFactory factory = MariadbConnectionFactory.from(
            MariadbConnectionConfiguration.builder()
                .host("localhost")
                .port(3306)
                .username("primavera")
                .password("primavera")
                .database("primavera")
                .build());
                
        return new PooledConnectionFactory(
            ConnectionPoolConfiguration.builder(factory)
                .initialSize(10)
                .maxSize(20)
                .maxIdleTime(Duration.ofMinutes(30))
                .validationQuery("SELECT 1")
                .build());
    }
}
```

## 📊 반응형 데이터 액세스

### Repository 구현
```java
@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    
    @Query("SELECT * FROM ORDERS WHERE USER_ID = :userId")
    Flux<Order> findByUserId(Long userId);
    
    @Query("SELECT * FROM ORDERS WHERE PRODUCT_ID = :productId")
    Flux<Order> findByProductId(Long productId);
    
    @Query("SELECT * FROM ORDERS WHERE AMOUNT >= :minAmount")
    Flux<Order> findByAmountGreaterThanEqual(Long minAmount);
    
    @Modifying
    @Query("DELETE FROM ORDERS WHERE USER_ID = :userId")
    Mono<Void> deleteByUserId(Long userId);
}
```

### Service 구현
```java
@Service
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    public Flux<Order> findByUserId(String userId) {
        return orderRepository.findByUserId(Long.parseLong(userId))
            .doOnNext(order -> log.debug("Found order: {}", order))
            .onErrorResume(throwable -> {
                log.error("Error finding orders for user: {}", userId, throwable);
                return Flux.empty();
            });
    }
    
    @Override
    public Mono<Order> save(Order order) {
        return orderRepository.save(order)
            .doOnSuccess(saved -> log.info("Order saved: {}", saved))
            .onErrorResume(throwable -> {
                log.error("Error saving order: {}", order, throwable);
                return Mono.empty();
            });
    }
}
```

## 🌐 함수형 라우팅

### RouterFunction 설정
```java
@Bean
public RouterFunction<ServerResponse> orderRoutes(OrderService orderService) {
    return route()
        // 사용자별 주문 조회
        .GET("/users/{userId}/orders", request -> 
            ok().body(orderService.findByUserId(
                request.pathVariable("userId")), Order.class))
        
        // 주문 생성
        .POST("/orders", request -> 
            request.bodyToMono(Order.class)
                .flatMap(orderService::save)
                .flatMap(order -> ok().bodyValue(order)))
        
        // 주문 수정
        .PUT("/orders/{id}", request ->
            Mono.zip(
                Mono.just(request.pathVariable("id")),
                request.bodyToMono(Order.class)
            )
            .flatMap(tuple -> {
                Order order = tuple.getT2();
                order.setId(Long.parseLong(tuple.getT1()));
                return orderService.save(order);
            })
            .flatMap(order -> ok().bodyValue(order)))
        
        // 주문 삭제
        .DELETE("/orders/{id}", request ->
            orderService.deleteById(Long.parseLong(request.pathVariable("id")))
                .then(noContent().build()))
        
        // 할인 가능 여부 확인
        .POST("/orders/{id}/discount-check", request ->
            request.bodyToFlux(SaleRoleType.class)
                .collectSet()
                .flatMap(roleTypes -> 
                    orderService.findById(Long.parseLong(request.pathVariable("id")))
                        .map(order -> saleCommand.isSaleable(order, roleTypes))
                        .flatMap(result -> ok().bodyValue(Map.of("saleable", result))))
        )
        .build();
}
```

## 🎯 할인 정책 시스템

### 할인 규칙 테이블
```java
@Component
public class SaleRoleTable {
    
    private final Map<SaleRoleType, Saleable> discountableTable;
    
    public SaleRoleTable(List<Saleable> saleables) {
        this.discountableTable = Map.of(
            SaleRoleType.AMOUNT, saleables.stream()
                .filter(s -> s instanceof AmountSaleRole)
                .findFirst().orElseThrow(),
            SaleRoleType.STOCK, saleables.stream()
                .filter(s -> s instanceof StockSaleRole)
                .findFirst().orElseThrow(),
            SaleRoleType.EVENT, saleables.stream()
                .filter(s -> s instanceof EventSaleRole)
                .findFirst().orElseThrow(),
            SaleRoleType.LEGAL, saleables.stream()
                .filter(s -> s instanceof LegalSaleRole)
                .findFirst().orElseThrow()
        );
    }
    
    public Map<SaleRoleType, Saleable> getDiscountableTable() {
        return discountableTable;
    }
}
```

### 할인 정책 열거형
```java
public enum SaleRoleType {
    AMOUNT,   // 금액 기반 할인
    STOCK,    // 재고 기반 할인  
    EVENT,    // 이벤트 기반 할인
    LEGAL     // 법적 제약 검증
}
```

### 할인 적용 예제
```java
@RestController
public class OrderDiscountController {
    
    @PostMapping("/orders/{orderId}/apply-discount")
    public Mono<ResponseEntity<DiscountResult>> applyDiscount(
            @PathVariable Long orderId,
            @RequestBody Set<SaleRoleType> discountTypes) {
        
        return orderService.findById(orderId)
            .map(order -> {
                boolean canApplyDiscount = saleCommand.isSaleable(order, discountTypes);
                if (canApplyDiscount) {
                    Long discountedAmount = calculateDiscount(order, discountTypes);
                    return DiscountResult.builder()
                        .orderId(orderId)
                        .originalAmount(order.getAmount())
                        .discountedAmount(discountedAmount)
                        .discountTypes(discountTypes)
                        .applied(true)
                        .build();
                } else {
                    return DiscountResult.builder()
                        .orderId(orderId)
                        .originalAmount(order.getAmount())
                        .applied(false)
                        .reason("Discount conditions not met")
                        .build();
                }
            })
            .map(result -> ResponseEntity.ok(result))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
```

## 🗄️ 데이터베이스 스키마

### 주문 테이블 구조
```sql
CREATE TABLE `ORDERS` (
    `ID` bigint(20) NOT NULL AUTO_INCREMENT,
    `USER_ID` bigint(20) NOT NULL,
    `PRODUCT_ID` bigint(20) NOT NULL,  
    `AMOUNT` bigint(20) NOT NULL,
    PRIMARY KEY (`ID`),
    INDEX `idx_user_id` (`USER_ID`),
    INDEX `idx_product_id` (`PRODUCT_ID`),
    INDEX `idx_amount` (`AMOUNT`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
```

### 데이터 초기화
```java
@EventListener(ApplicationReadyEvent.class)
public void init(ApplicationReadyEvent applicationReadyEvent) {
    log.debug("OrderApplication Start... {}", applicationReadyEvent);
    var orderRepository = applicationReadyEvent.getApplicationContext()
        .getBean(OrderRepository.class);
    
    // 기존 데이터 정리
    orderRepository.deleteAll().subscribe();
    
    // 테스트 데이터 생성 (사용자 1-100, 상품 1-100)
    LongStream.rangeClosed(1, 100).forEach(userId -> {
        orderRepository.saveAll(
            LongStream.rangeClosed(1, 100)
                .mapToObj(productId -> new Order(userId, productId, 100L))
                .collect(Collectors.toList())
        ).subscribe();
    });
}
```

## 🏃‍♂️ 실행 방법

### 1. 데이터베이스 준비
```bash
# MariaDB 실행 (Docker)
docker run -d --name mariadb-order \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3306:3306 mariadb:11.4.7

# 주문 테이블 생성
mysql -h localhost -u primavera -p primavera < init-db.sql
```

### 2. Config Server 시작
```bash
# Configuration 서비스 먼저 실행
./gradlew :chap18:configuration:bootRun
```

### 3. Order Service 시작
```bash
# Order 서비스 실행
./gradlew :chap18:order:bootRun

# 또는 JAR 직접 실행
java -jar order/build/libs/order.jar
```

### 4. 서비스 동작 확인
```bash
# 서비스 상태 확인
curl http://localhost:8082/actuator/health

# 사용자 주문 조회
curl http://localhost:8082/users/1/orders

# 주문 생성
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"amount":15000}'

# 할인 가능 여부 확인
curl -X POST http://localhost:8082/orders/1/discount-check \
  -H "Content-Type: application/json" \
  -d '["AMOUNT","EVENT"]'
```

## 📈 성능 최적화

### 1. R2DBC 연결풀 튜닝
```yaml
spring:
  r2dbc:
    pool:
      initial-size: 10          # 초기 연결 수
      max-size: 20              # 최대 연결 수
      max-idle-time: 300m       # 유휴 연결 유지 시간
      max-acquire-time: 30s     # 연결 획득 최대 대기 시간
      validation-query: SELECT 1 # 연결 유효성 검증 쿼리
```

### 2. 백프레셔 제어
```java
@Service
public class OrderStreamService {
    
    public Flux<Order> getOrdersWithBackpressure(String userId) {
        return orderRepository.findByUserId(Long.parseLong(userId))
            .limitRate(100)                    // 백프레셔 제어
            .buffer(10)                        // 배치 처리
            .flatMap(Flux::fromIterable)       // 평면화
            .publishOn(Schedulers.parallel())  // 병렬 처리
            .doOnNext(order -> log.debug("Processing order: {}", order))
            .onBackpressureBuffer(1000);       // 버퍼 크기 제한
    }
}
```

### 3. 인덱스 최적화
```sql
-- 복합 인덱스 생성
CREATE INDEX idx_user_product ON ORDERS (USER_ID, PRODUCT_ID);
CREATE INDEX idx_amount_user ON ORDERS (AMOUNT, USER_ID);

-- 쿼리 성능 분석
EXPLAIN SELECT * FROM ORDERS WHERE USER_ID = 1 AND AMOUNT >= 10000;
```

## 🧪 테스트 전략

### 반응형 서비스 테스트
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    @Test
    void shouldReturnOrdersForUser() {
        // Given
        Order order1 = new Order(1L, 1L, 1L, 1000L);
        Order order2 = new Order(2L, 1L, 2L, 2000L);
        when(orderRepository.findByUserId(1L))
            .thenReturn(Flux.just(order1, order2));
        
        // When
        Flux<Order> result = orderService.findByUserId("1");
        
        // Then
        StepVerifier.create(result)
            .expectNext(order1)
            .expectNext(order2)
            .verifyComplete();
    }
}
```

### 할인 정책 테스트
```java
@Test
void shouldApplyDiscountWhenConditionsMet() {
    // Given
    Order order = new Order(1L, 1L, 25L, 15000L); // 1.5만원 주문
    Set<SaleRoleType> discountTypes = Set.of(
        SaleRoleType.AMOUNT,  // 1만원 이상 (만족)
        SaleRoleType.STOCK    // 상품 25번 (만족)
    );
    
    // When
    boolean result = saleCommand.isSaleable(order, discountTypes);
    
    // Then
    assertThat(result).isTrue();
}

@Test
void shouldNotApplyDiscountWhenConditionsNotMet() {
    // Given
    Order order = new Order(1L, 1L, 75L, 5000L); // 5천원 주문
    Set<SaleRoleType> discountTypes = Set.of(
        SaleRoleType.AMOUNT,  // 1만원 이상 (불만족)
        SaleRoleType.STOCK    // 상품 75번 (불만족)
    );
    
    // When  
    boolean result = saleCommand.isSaleable(order, discountTypes);
    
    // Then
    assertThat(result).isFalse();
}
```

### WebFlux 통합 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class OrderIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    @Order(1)
    void shouldCreateOrder() {
        Order order = new Order(1L, 1L, 10000L);
        
        webTestClient.post()
            .uri("/orders")
            .bodyValue(order)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Order.class)
            .value(created -> {
                assertThat(created.getId()).isNotNull();
                assertThat(created.getUserId()).isEqualTo(1L);
            });
    }
    
    @Test
    @Order(2)
    void shouldFindOrdersByUserId() {
        webTestClient.get()
            .uri("/users/1/orders")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Order.class)
            .hasSize(100);  // 초기화 시 생성된 주문 수
    }
}
```

## 📚 학습 포인트

이 Order Service는 다음과 같은 현대적인 반응형 프로그래밍 패턴들을 학습할 수 있습니다:

1. **Reactive Programming**: WebFlux + R2DBC 완전한 반응형 스택
2. **Functional Routing**: RouterFunction을 통한 함수형 라우팅
3. **Strategy Pattern**: 할인 정책의 유연한 구현과 확장
4. **Domain-Driven Design**: 비즈니스 로직의 도메인 모델링
5. **Reactive Streams**: 백프레셔 제어와 비동기 데이터 처리
6. **Testing Strategy**: 반응형 애플리케이션의 체계적 테스트

Order Service는 반응형 프로그래밍의 핵심 개념들을 실제 비즈니스 로직과 결합하여 학습할 수 있는 완벽한 예제이며, 특히 할인 정책 엔진을 통해 전략 패턴과 도메인 주도 설계의 실용적 적용을 배울 수 있습니다.