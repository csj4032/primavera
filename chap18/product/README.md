# Chapter 18 Product - 상품 관리 마이크로서비스

Spring Boot 교육용 프로젝트 Primavera의 Chapter 18 Product 모듈입니다. Spring AOP와 커스텀 애너테이션을 활용한 고급 캐싱 시스템, 재고 관리, 그리고 서비스 간 통신을 학습합니다.

## 🎯 학습 목표

- **고급 캐싱 시스템**: AOP와 커스텀 애너테이션을 활용한 캐싱 전략
- **재고 관리**: 분산 환경에서의 재고 동시성 제어
- **이벤트 기반 아키텍처**: Kafka를 통한 비동기 이벤트 처리
- **서비스 간 통신**: 마이크로서비스 간 REST 통신 패턴
- **성능 최적화**: 캐싱과 배치 처리를 통한 성능 향상

## 📁 프로젝트 구조

```
chap18/product/
├── src/main/java/com/genius/primavera/
│   ├── ProductApplication.java          # 메인 애플리케이션
│   ├── Product.java                     # 상품 엔티티
│   ├── ProductController.java           # REST 컨트롤러
│   ├── ProductService.java              # 서비스 인터페이스
│   ├── ProductServiceImpl.java          # 서비스 구현체
│   ├── ProductRepository.java           # JPA 리포지터리
│   ├── cache/                          # 캐싱 시스템
│   │   ├── CacheAspect.java               # 캐시 AOP
│   │   ├── CacheGet.java                  # 캐시 애너테이션
│   │   ├── CacheKey.java                  # 캐시 키 애너테이션
│   │   ├── CacheKeyGenerator.java         # 캐시 키 생성기
│   │   └── CacheKeyPrefixType.java        # 캐시 키 타입
│   ├── config/                         # 설정 클래스
│   │   └── KafkaConfig.java              # Kafka 설정
│   ├── dto/                           # 데이터 전송 객체
│   │   └── InventoryReservationResult.java # 재고 예약 결과
│   ├── event/                         # 이벤트 관련
│   │   ├── InventoryEventConsumer.java    # 재고 이벤트 소비자
│   │   ├── InventoryEventPublisher.java   # 재고 이벤트 발행자
│   │   ├── InventoryReservedEvent.java    # 재고 예약 이벤트
│   │   ├── InventoryInsufficientEvent.java # 재고 부족 이벤트
│   │   ├── OrderCreatedEvent.java         # 주문 생성 이벤트
│   │   └── OrderItemEvent.java            # 주문 아이템 이벤트
│   └── saleed/                        # 할인 정책 시스템
│       ├── SaleCommand.java              # 할인 명령
│       ├── SaleRoleTable.java            # 할인 규칙 테이블
│       ├── SaleRoleType.java             # 할인 타입
│       └── role/                        # 할인 규칙 구현
│           ├── AmountSaleRole.java        # 금액 기반 할인
│           ├── EventSaleRole.java         # 이벤트 할인
│           ├── LegalSaleRole.java         # 법정 할인
│           ├── Saleable.java             # 할인 인터페이스
│           └── StockSaleRole.java         # 재고 기반 할인
├── src/main/resources/
│   └── application.yaml                # 애플리케이션 설정
└── build.gradle                        # WebFlux + JPA 의존성
```

## 🏗 아키텍처 특성

### 1. 고급 캐싱 시스템
```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheGet {
    CacheKeyPrefixType keyPrefixType() default CacheKeyPrefixType.PRODUCT;
    int timeoutSeconds() default 300;
    String description() default "";
}

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheKey {
    long order() default 0;
    String prefix() default "";
}
```

### 2. AOP 기반 캐시 처리
```java
@Aspect
@Component
@Slf4j
public class CacheAspect {
    
    private static final String KEY_DELIMITER = "::";
    
    @Around("@annotation(cacheGet)")
    public Object cacheGet(ProceedingJoinPoint joinPoint, CacheGet cacheGet) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CacheKeyGenerator cacheGenerator = cacheGet.keyPrefixType().getCacheGenerator();
        
        // 기본 키 생성 (클래스명::메서드명)
        String base = getBase(joinPoint.getTarget(), method.getName());
        
        // 파라미터 기반 서픽스 생성
        String suffix = getSuffix(method.getParameterAnnotations(), joinPoint.getArgs());
        
        if (!suffix.isEmpty()) {
            String key = cacheGenerator.generator(join(KEY_DELIMITER, base, suffix));
            // 마지막 파라미터를 캐시 키로 설정
            joinPoint.getArgs()[joinPoint.getArgs().length - 1] = key;
            return joinPoint.proceed(joinPoint.getArgs());
        }
        
        return joinPoint.proceed();
    }
}
```

### 3. 캐시 키 생성 전략
```java
public enum CacheKeyPrefixType {
    PRODUCT(new ProductCacheKeyGenerator()),
    CATEGORY(new CategoryCacheKeyGenerator()),
    USER_PRODUCT(new UserProductCacheKeyGenerator());
    
    private final CacheKeyGenerator cacheGenerator;
    
    CacheKeyPrefixType(CacheKeyGenerator cacheGenerator) {
        this.cacheGenerator = cacheGenerator;
    }
    
    public CacheKeyGenerator getCacheGenerator() {
        return cacheGenerator;
    }
}
```

## 🎯 핵심 기능

### 1. 상품 관리
```java
@Entity
@Table(name = "PRODUCTS")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private Integer stock;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 2. 캐시 적용 서비스
```java
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    
    @CacheGet(keyPrefixType = CacheKeyPrefixType.PRODUCT, timeoutSeconds = 600)
    public Product findById(@CacheKey(order = 1) Long productId, 
                           @CacheKey(order = 2) String version,
                           String cacheKey) {
        log.info("Cache miss for key: {}, fetching from database", cacheKey);
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    }
    
    @CacheGet(keyPrefixType = CacheKeyPrefixType.CATEGORY, timeoutSeconds = 300)
    public List<Product> findByCategory(@CacheKey(order = 1) String category,
                                       @CacheKey(order = 2) Integer limit,
                                       String cacheKey) {
        log.info("Cache miss for category: {}, fetching from database", category);
        return productRepository.findByCategoryWithLimit(category, limit);
    }
}
```

### 3. 재고 관리 및 이벤트 처리
```java
@Service
@Transactional
public class InventoryService {
    
    public InventoryReservationResult reserveInventory(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
            
        if (product.getStock() >= quantity) {
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
            
            // 재고 예약 이벤트 발행
            InventoryReservedEvent event = InventoryReservedEvent.builder()
                .productId(productId)
                .reservedQuantity(quantity)
                .remainingStock(product.getStock())
                .build();
                
            inventoryEventPublisher.publishInventoryReserved(event);
            
            return InventoryReservationResult.success(productId, quantity);
        } else {
            // 재고 부족 이벤트 발행
            InventoryInsufficientEvent event = InventoryInsufficientEvent.builder()
                .productId(productId)
                .requestedQuantity(quantity)
                .availableStock(product.getStock())
                .build();
                
            inventoryEventPublisher.publishInventoryInsufficient(event);
            
            return InventoryReservationResult.failure(productId, "Insufficient inventory");
        }
    }
}
```

### 4. 이벤트 기반 통신
```java
@Component
public class InventoryEventConsumer {
    
    @KafkaListener(topics = "order-created", groupId = "product-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Processing order created event: {}", event);
        
        for (OrderItemEvent item : event.getItems()) {
            try {
                InventoryReservationResult result = inventoryService
                    .reserveInventory(item.getProductId(), item.getQuantity());
                    
                if (!result.isSuccess()) {
                    log.warn("Failed to reserve inventory for product {}: {}", 
                        item.getProductId(), result.getReason());
                }
            } catch (Exception e) {
                log.error("Error processing inventory reservation", e);
            }
        }
    }
}
```

### 5. 할인 정책 시스템
```java
@Component
public class AmountSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        Product product = productService.findById(order.getProductId());
        return product.getPrice().compareTo(BigDecimal.valueOf(50000)) >= 0; // 5만원 이상
    }
}

@Component
public class StockSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        Product product = productService.findById(order.getProductId());
        return product.getStock() > 10; // 재고 10개 초과
    }
}
```

## 🛠 기술 스택

### 핵심 기술
- **Java**: 21
- **Spring Boot**: 3.3.6
- **Spring WebFlux**: 리액티브 웹 프레임워크
- **Spring Data JPA**: 데이터 액세스
- **Spring AOP**: 관점 지향 프로그래밍

### 메시징 및 이벤트
- **Apache Kafka**: 분산 스트리밍 플랫폼
- **Spring Kafka**: Kafka 통합
- **이벤트 소싱**: 도메인 이벤트 패턴

### 캐싱 및 성능
- **커스텀 캐시 프레임워크**: AOP 기반 캐싱
- **메트릭 수집**: 캐시 성능 모니터링

## 🚀 실행 방법

### 1. 의존 서비스 시작
```bash
# Config Server 시작
./gradlew :chap18:configuration:bootRun

# Kafka 서버 시작
./docker-manager.sh start chap18

# MariaDB 시작
docker run -d --name mariadb-product \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -p 3308:3306 mariadb:11.4.7
```

### 2. Product 서비스 시작
```bash
# Product 마이크로서비스 시작
./gradlew :chap18:product:bootRun

# 특정 프로파일로 실행
./gradlew :chap18:product:bootRun -Dspring.profiles.active=local
```

### 3. API 테스트
```bash
# 상품 조회 (캐시 적용)
curl "http://localhost:8083/products/1?version=v1"

# 카테고리별 상품 조회
curl "http://localhost:8083/products/category/electronics?limit=10"

# 재고 예약
curl -X POST http://localhost:8083/products/1/reserve \
  -H "Content-Type: application/json" \
  -d '{"quantity": 5}'

# 상품 할인 계산
curl -X POST http://localhost:8083/products/1/discount \
  -H "Content-Type: application/json" \
  -d '{"discountTypes": ["AMOUNT", "STOCK"]}'
```

## 📋 테스트 실행

### 캐시 테스트
```bash
# 캐싱 시스템 테스트
./gradlew :chap18:product:test --tests "*CacheTest"

# AOP 동작 테스트
./gradlew :chap18:product:test --tests "*AspectTest"

# 성능 벤치마크 테스트
./gradlew :chap18:product:test --tests "*PerformanceTest"
```

### 통합 테스트 예시
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.profiles.active=test")
class ProductServiceCacheIntegrationTest {
    
    @Autowired
    private ProductService productService;
    
    @MockBean
    private ProductRepository productRepository;
    
    @Test
    void shouldCacheProductOnFirstCallAndHitCacheOnSecond() {
        // Given
        Product product = Product.builder()
            .id(1L)
            .name("Test Product")
            .price(BigDecimal.valueOf(10000))
            .build();
            
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When - 첫 번째 호출 (캐시 미스)
        Product firstResult = productService.findById(1L, "v1", null);
        
        // When - 두 번째 호출 (캐시 히트)
        Product secondResult = productService.findById(1L, "v1", null);
        
        // Then
        assertThat(firstResult).isEqualTo(secondResult);
        verify(productRepository, times(1)).findById(1L); // DB는 한 번만 호출
    }
}
```

## 🎓 핵심 학습 포인트

### 1. AOP 패턴
```java
// 횡단 관심사(Cross-cutting Concerns) 분리
@Around("@annotation(cacheGet)")
public Object cacheGet(ProceedingJoinPoint joinPoint, CacheGet cacheGet) throws Throwable {
    // 캐시 로직은 비즈니스 로직과 분리되어 처리
    String cacheKey = generateCacheKey(joinPoint);
    Object cachedResult = getCachedResult(cacheKey);
    
    if (cachedResult != null) {
        return cachedResult;
    }
    
    Object result = joinPoint.proceed();
    cacheResult(cacheKey, result);
    return result;
}
```

### 2. 이벤트 기반 아키텍처
```java
// 비동기 이벤트 처리를 통한 서비스 간 느슨한 결합
@EventListener
@Async
public void handleOrderCreated(OrderCreatedEvent event) {
    // 주문 생성 이벤트에 반응하여 재고 예약
    reserveInventoryForOrder(event);
}
```

### 3. 성능 최적화 패턴
```java
// 캐시 키 생성 최적화
public class ProductCacheKeyGenerator implements CacheKeyGenerator {
    @Override
    public String generator(String rawKey) {
        // 시간 기반 버케팅으로 캐시 무효화 주기 조절
        long timeBucket = System.currentTimeMillis() / 1000 / 300 * 300; // 5분 단위
        return "PRODUCT::" + rawKey + "::" + timeBucket;
    }
}
```

### 4. 동시성 제어
```java
@Transactional
@Lock(LockModeType.PESSIMISTIC_WRITE)
public InventoryReservationResult reserveInventory(Long productId, Integer quantity) {
    // 비관적 락을 통한 재고 동시성 제어
    Product product = productRepository.findByIdForUpdate(productId);
    // ... 재고 처리 로직
}
```

## 📚 주요 애너테이션

### AOP 관련
- `@Aspect`: AspectJ 애스펙트 정의
- `@Around`: 메서드 실행 전후 처리
- `@Pointcut`: 조인 포인트 선택

### 캐시 관련
- `@CacheGet`: 커스텀 캐시 적용 애너테이션
- `@CacheKey`: 캐시 키 생성을 위한 파라미터 표시

### 이벤트 관련
- `@EventListener`: 스프링 이벤트 리스너
- `@KafkaListener`: Kafka 메시지 소비자

## 🔧 성능 모니터링

### 1. 캐시 메트릭
```java
@Component
public class CacheMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordCacheHit(String cacheType) {
        meterRegistry.counter("cache.hit", "type", cacheType).increment();
    }
    
    public void recordCacheMiss(String cacheType) {
        meterRegistry.counter("cache.miss", "type", cacheType).increment();
    }
}
```

### 2. 애플리케이션 메트릭
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

## 🔄 다음 단계

1. **chap18:order** - 주문 처리 및 분산 트랜잭션
2. **chap18:front** - API Gateway 및 서비스 오케스트레이션
3. **분산 추적** - Zipkin/Jaeger를 활용한 트레이싱
4. **서비스 메시** - Istio를 통한 마이크로서비스 관리

## 📖 관련 문서

- [Spring AOP Documentation](https://docs.spring.io/spring-framework/reference/core/aop.html)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Microservices Caching Patterns](https://microservices.io/patterns/data/cache-aside.html)
- [Event-Driven Architecture](https://microservices.io/patterns/data/event-sourcing.html)