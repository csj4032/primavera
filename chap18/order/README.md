# Chapter 18 Order - 주문 관리 마이크로서비스

Spring Boot 교육용 프로젝트 Primavera의 Chapter 18 Order 모듈입니다. 리액티브 주문 처리, 이벤트 기반 아키텍처, 그리고 분산 트랜잭션을 통한 복잡한 비즈니스 로직 처리를 학습합니다.

## 🎯 학습 목표

- **이벤트 기반 아키텍처**: Kafka를 통한 비동기 이벤트 처리
- **분산 트랜잭션**: Saga 패턴을 활용한 데이터 일관성 관리
- **복잡한 비즈니스 로직**: 주문 처리 워크플로우 구현
- **서비스 간 통신**: 마이크로서비스 간 협력 패턴
- **할인 정책 엔진**: 전략 패턴을 활용한 유연한 비즈니스 규칙

## 📁 프로젝트 구조

```
chap18/order/
├── src/main/java/com/genius/primavera/
│   ├── OrderApplication.java            # 메인 애플리케이션
│   ├── Order.java                       # 주문 엔티티
│   ├── OrderController.java             # REST 컨트롤러
│   ├── OrderRepository.java             # JPA 리포지터리
│   ├── OrderService.java                # 서비스 인터페이스
│   ├── OrderServiceImpl.java            # 서비스 구현체
│   ├── config/                          # 설정 클래스
│   │   └── KafkaConfig.java              # Kafka 설정
│   ├── dto/                            # 데이터 전송 객체
│   │   ├── CreateOrderRequest.java       # 주문 생성 요청
│   │   └── CreateOrderItemRequest.java   # 주문 항목 요청
│   ├── event/                          # 이벤트 관련
│   │   ├── InventoryEventConsumer.java   # 재고 이벤트 소비자
│   │   ├── OrderEventPublisher.java      # 주문 이벤트 발행자
│   │   ├── OrderCreatedEvent.java        # 주문 생성 이벤트
│   │   ├── OrderCancelledEvent.java      # 주문 취소 이벤트
│   │   ├── InventoryReservedEvent.java   # 재고 예약 이벤트
│   │   ├── InventoryInsufficientEvent.java # 재고 부족 이벤트
│   │   └── OrderItemEvent.java           # 주문 항목 이벤트
│   └── saleed/                         # 할인 정책 시스템
│       ├── SaleCommand.java             # 할인 명령
│       ├── SaleRoleTable.java           # 할인 규칙 테이블
│       ├── SaleRoleType.java            # 할인 타입
│       └── role/                       # 할인 규칙 구현
│           ├── AmountSaleRole.java       # 금액 기반 할인
│           ├── EventSaleRole.java        # 이벤트 할인
│           ├── LegalSaleRole.java        # 법정 할인
│           ├── Saleable.java            # 할인 인터페이스
│           └── StockSaleRole.java        # 재고 기반 할인
├── src/main/resources/
│   └── application.yaml                # 애플리케이션 설정
└── build.gradle                        # WebFlux + JPA 의존성
```

## 🏗 아키텍처 특성

### 1. 이벤트 기반 주문 처리
```java
@Component
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @EventListener
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing order created event: {}", event);
        kafkaTemplate.send("order-created", event);
    }
    
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .userId(order.getUserId())
            .items(order.getItems().stream()
                .map(item -> OrderItemEvent.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build())
                .collect(Collectors.toList()))
            .totalAmount(order.getTotalAmount())
            .createdAt(order.getCreatedAt())
            .build();
            
        applicationEventPublisher.publishEvent(event);
    }
}
```

### 2. Saga 패턴 구현
```java
@Service
@Transactional
public class OrderSagaOrchestrator {
    
    public Mono<OrderResult> processOrder(CreateOrderRequest request) {
        return validateOrder(request)
            .flatMap(this::reserveInventory)
            .flatMap(this::processPayment)
            .flatMap(this::confirmOrder)
            .onErrorResume(this::compensate);
    }
    
    private Mono<OrderResult> compensate(Throwable error) {
        log.error("Order processing failed, starting compensation", error);
        
        return releaseInventory()
            .then(refundPayment())
            .then(cancelOrder())
            .then(Mono.just(OrderResult.failed(error.getMessage())));
    }
}
```

### 3. 복잡한 비즈니스 규칙
```java
@Service
public class OrderValidationService {
    
    public Mono<ValidationResult> validateOrder(CreateOrderRequest request) {
        return Mono.zip(
            validateUser(request.getUserId()),
            validateProducts(request.getItems()),
            validateInventory(request.getItems()),
            validateBusinessRules(request)
        ).map(tuple -> {
            List<String> errors = new ArrayList<>();
            if (!tuple.getT1().isValid()) errors.addAll(tuple.getT1().getErrors());
            if (!tuple.getT2().isValid()) errors.addAll(tuple.getT2().getErrors());
            if (!tuple.getT3().isValid()) errors.addAll(tuple.getT3().getErrors());
            if (!tuple.getT4().isValid()) errors.addAll(tuple.getT4().getErrors());
            
            return errors.isEmpty() 
                ? ValidationResult.success()
                : ValidationResult.failure(errors);
        });
    }
}
```

## 🎯 핵심 기능

### 1. 주문 엔티티
```java
@Entity
@Table(name = "ORDERS")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

@Entity
@Table(name = "ORDER_ITEMS")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private BigDecimal price;
}
```

### 2. 주문 처리 워크플로우
```java
@Service
public class OrderServiceImpl implements OrderService {
    
    @Transactional
    public Mono<Order> createOrder(CreateOrderRequest request) {
        return validateOrderRequest(request)
            .flatMap(this::buildOrder)
            .flatMap(this::saveOrder)
            .doOnSuccess(this::publishOrderCreatedEvent)
            .doOnError(error -> log.error("Failed to create order", error));
    }
    
    private Mono<Order> buildOrder(CreateOrderRequest request) {
        return Mono.fromCallable(() -> {
            Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .build();
                
            List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> OrderItem.builder()
                    .order(order)
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .build())
                .collect(Collectors.toList());
                
            order.setItems(items);
            order.setTotalAmount(calculateTotalAmount(items));
            
            return order;
        });
    }
}
```

### 3. 이벤트 기반 재고 관리
```java
@Component
public class InventoryEventConsumer {
    
    @KafkaListener(topics = "inventory-reserved", groupId = "order-service")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for order: {}", event.getOrderId());
        
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.INVENTORY_RESERVED)
            .doOnSuccess(order -> {
                // 결제 처리 이벤트 발행
                PaymentRequestEvent paymentEvent = PaymentRequestEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .amount(order.getTotalAmount())
                    .build();
                orderEventPublisher.publishPaymentRequested(paymentEvent);
            })
            .subscribe();
    }
    
    @KafkaListener(topics = "inventory-insufficient", groupId = "order-service")
    public void handleInventoryInsufficient(InventoryInsufficientEvent event) {
        log.warn("Insufficient inventory for order: {}", event.getOrderId());
        
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED)
            .doOnSuccess(order -> {
                OrderCancelledEvent cancelEvent = OrderCancelledEvent.builder()
                    .orderId(order.getId())
                    .reason("Insufficient inventory")
                    .cancelledAt(LocalDateTime.now())
                    .build();
                orderEventPublisher.publishOrderCancelled(cancelEvent);
            })
            .subscribe();
    }
}
```

### 4. 할인 정책 적용
```java
@Component
public class OrderDiscountService {
    
    private final SaleCommand saleCommand;
    
    public Mono<DiscountResult> applyDiscount(Long orderId, Set<SaleRoleType> discountTypes) {
        return orderRepository.findById(orderId)
            .map(order -> {
                boolean canApplyDiscount = saleCommand.isSaleable(order, discountTypes);
                
                if (canApplyDiscount) {
                    BigDecimal discountRate = calculateDiscountRate(discountTypes);
                    BigDecimal discountedAmount = order.getTotalAmount()
                        .multiply(BigDecimal.ONE.subtract(discountRate));
                        
                    order.setTotalAmount(discountedAmount);
                    
                    return DiscountResult.builder()
                        .orderId(orderId)
                        .originalAmount(order.getTotalAmount())
                        .discountedAmount(discountedAmount)
                        .discountRate(discountRate)
                        .discountTypes(discountTypes)
                        .applied(true)
                        .build();
                } else {
                    return DiscountResult.builder()
                        .orderId(orderId)
                        .applied(false)
                        .reason("Order does not meet discount criteria")
                        .build();
                }
            });
    }
}
```

### 5. 분산 트랜잭션 관리
```java
@Component
public class OrderTransactionManager {
    
    @SagaOrchestrationStart
    public void processOrder(OrderCreatedEvent event) {
        SagaTransaction transaction = SagaTransaction.builder()
            .correlationId(event.getOrderId().toString())
            .build();
            
        // Step 1: 재고 예약
        transaction.addStep(
            () -> inventoryService.reserveInventory(event.getItems()),
            () -> inventoryService.releaseInventory(event.getItems())
        );
        
        // Step 2: 결제 처리
        transaction.addStep(
            () -> paymentService.processPayment(event.getUserId(), event.getTotalAmount()),
            () -> paymentService.refundPayment(event.getUserId(), event.getTotalAmount())
        );
        
        // Step 3: 주문 확정
        transaction.addStep(
            () -> confirmOrder(event.getOrderId()),
            () -> cancelOrder(event.getOrderId())
        );
        
        sagaManager.execute(transaction);
    }
}
```

## 🛠 기술 스택

### 핵심 기술
- **Java**: 21
- **Spring Boot**: 3.3.6
- **Spring WebFlux**: 리액티브 웹 프레임워크
- **Spring Data JPA**: 데이터 액세스

### 이벤트 및 메시징
- **Apache Kafka**: 분산 스트리밍 플랫폼
- **Spring Kafka**: Kafka 통합
- **이벤트 소싱**: 도메인 이벤트 패턴

### 분산 시스템
- **Saga 패턴**: 분산 트랜잭션 관리
- **CQRS**: 명령과 조회 분리

## 🚀 실행 방법

### 1. 의존 서비스 시작
```bash
# Config Server 시작
./gradlew :chap18:configuration:bootRun

# Kafka 및 Zookeeper 시작
./docker-manager.sh start chap18

# MariaDB 시작
docker run -d --name mariadb-order \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -p 3308:3306 mariadb:11.4.7

# Product 및 Account 서비스 시작 (의존성)
./gradlew :chap18:account:bootRun &
./gradlew :chap18:product:bootRun &
```

### 2. Order 서비스 시작
```bash
# Order 마이크로서비스 시작
./gradlew :chap18:order:bootRun

# 특정 프로파일로 실행
./gradlew :chap18:order:bootRun -Dspring.profiles.active=local
```

### 3. API 테스트
```bash
# 주문 생성
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 10000
      },
      {
        "productId": 2,
        "quantity": 1,
        "price": 15000
      }
    ]
  }'

# 주문 조회
curl http://localhost:8082/orders/1

# 사용자별 주문 목록
curl http://localhost:8082/users/1/orders

# 주문 할인 적용
curl -X POST http://localhost:8082/orders/1/discount \
  -H "Content-Type: application/json" \
  -d '{"discountTypes": ["AMOUNT", "EVENT"]}'

# 주문 상태 변경
curl -X PUT http://localhost:8082/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

## 📋 테스트 실행

### 이벤트 기반 테스트
```bash
# 전체 테스트 실행
./gradlew :chap18:order:test

# 이벤트 처리 테스트
./gradlew :chap18:order:test --tests "*EventTest"

# Saga 트랜잭션 테스트
./gradlew :chap18:order:test --tests "*SagaTest"

# 통합 테스트
./gradlew :chap18:order:test --tests "*IntegrationTest"
```

### 테스트 예시
```java
@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=test")
@EmbeddedKafka(partitions = 1, topics = {"order-created", "inventory-reserved"})
class OrderEventIntegrationTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Test
    void shouldPublishOrderCreatedEventWhenOrderIsCreated() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .userId(1L)
            .items(List.of(
                CreateOrderItemRequest.builder()
                    .productId(1L)
                    .quantity(2)
                    .price(BigDecimal.valueOf(10000))
                    .build()
            ))
            .build();
            
        // When
        StepVerifier.create(orderService.createOrder(request))
            .assertNext(order -> {
                assertThat(order.getId()).isNotNull();
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            })
            .verifyComplete();
            
        // Then
        // Kafka 이벤트 발행 검증
        verify(kafkaTemplate, timeout(5000))
            .send(eq("order-created"), any(OrderCreatedEvent.class));
    }
}
```

## 🎓 핵심 학습 포인트

### 1. 이벤트 기반 아키텍처
```java
// 이벤트 발행과 구독을 통한 느슨한 결합
@EventListener
@Async
public void handleOrderCreated(OrderCreatedEvent event) {
    // 비동기로 재고 예약 요청
    reserveInventoryAsync(event);
    
    // 비동기로 결제 준비
    preparePaymentAsync(event);
}
```

### 2. Saga 패턴
```java
// 분산 트랜잭션의 단계적 처리 및 보상
public class OrderProcessingSaga {
    private final List<SagaStep> steps = Arrays.asList(
        new InventoryReservationStep(),
        new PaymentProcessingStep(),
        new OrderConfirmationStep()
    );
    
    public void execute() {
        try {
            steps.forEach(SagaStep::execute);
        } catch (Exception e) {
            compensate();
        }
    }
}
```

### 3. 도메인 이벤트
```java
// 도메인 객체에서 직접 이벤트 발행
@Entity
public class Order {
    @DomainEvents
    Collection<Object> domainEvents() {
        return Arrays.asList(
            new OrderCreatedEvent(this),
            new PaymentRequestedEvent(this)
        );
    }
}
```

### 4. 최종 일관성 (Eventual Consistency)
```java
// 즉시 일관성 대신 최종 일관성 보장
@RetryableTopic(
    attempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
@KafkaListener(topics = "inventory-events")
public void handleInventoryEvent(InventoryEvent event) {
    // 재시도 로직과 함께 최종 일관성 보장
    processInventoryChange(event);
}
```

## 📚 주요 애너테이션

### 이벤트 관련
- `@EventListener`: 스프링 이벤트 리스너
- `@KafkaListener`: Kafka 메시지 소비자
- `@DomainEvents`: JPA 도메인 이벤트

### 트랜잭션 관련
- `@Transactional`: 트랜잭션 경계 설정
- `@Retryable`: 재시도 로직
- `@Async`: 비동기 처리

### Saga 관련
- `@SagaStart`: Saga 시작점
- `@SagaCompensation`: 보상 트랜잭션

## 🔧 운영 및 모니터링

### 1. 이벤트 추적
```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### 2. Kafka 모니터링
```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            kafkaAdmin.describeCluster();
            return Health.up()
                .withDetail("kafka", "Available")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("kafka", "Unavailable")
                .withException(e)
                .build();
        }
    }
}
```

## 🔄 다음 단계

1. **chap18:front** - API Gateway 및 서비스 오케스트레이션
2. **분산 추적** - Zipkin을 통한 요청 추적
3. **서킷 브레이커** - Resilience4j를 활용한 장애 격리
4. **이벤트 소싱** - 완전한 이벤트 기반 데이터 저장

## 📖 관련 문서

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)
- [Domain Events](https://martinfowler.com/eaaDev/DomainEvent.html)