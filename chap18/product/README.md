# Product Service - Advanced Caching & AOP

## 📋 Overview

Product Service는 Primavera 마이크로서비스 아키텍처에서 상품 관리를 담당하는 핵심 서비스입니다. Spring Boot와 Spring AOP를 활용한 고급 캐싱 전략, 커스텀 애노테이션 기반 캐시 키 생성, 그리고 할인 정책 엔진을 포함하여 상품 도메인의 복잡한 비즈니스 로직을 효율적으로 처리합니다.

## 🏗️ 아키텍처 특성

### Core Technologies
- **Spring Boot 3.3.6**: 최신 스프링 부트 프레임워크
- **Spring AOP**: 관점 지향 프로그래밍 (Aspect-Oriented Programming)
- **Custom Cache Framework**: 애노테이션 기반 캐싱 시스템
- **Spring Cloud Config**: 중앙집중식 설정 관리
- **Strategy Pattern**: 할인 정책 엔진

### Advanced Caching Architecture
```java
@SpringBootApplication
@EnableAspectJAutoProxy
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
```

## 🚀 주요 기능

### 1. 고급 캐싱 시스템
- **AOP 기반 캐싱**: 애스펙트를 통한 투명한 캐시 적용
- **커스텀 애노테이션**: `@CacheGet`을 통한 선언적 캐싱
- **동적 키 생성**: 메서드 파라미터 기반 캐시 키 자동 생성
- **캐시 키 전략**: 다양한 캐시 키 생성 전략 지원

### 2. 상품 엔티티 모델
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private String category;
    private Integer stock;
    private Instant createdAt;
    private Instant updatedAt;
}
```

### 3. 커스텀 캐시 애노테이션
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

### 4. 캐시 키 생성 전략
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

// 구체적인 키 생성기 구현
public class ProductCacheKeyGenerator implements CacheKeyGenerator {
    @Override
    public String generator(String rawKey) {
        return "PRODUCT::" + rawKey + "::" + System.currentTimeMillis() / 1000 / 300 * 300;
    }
}
```

## 🔧 고급 캐싱 구현

### 1. CacheAspect 구현
```java
@Slf4j
@Aspect
@Component
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
            joinPoint.getArgs()[2] = key;
            return joinPoint.proceed(joinPoint.getArgs());
        }
        
        return joinPoint.proceed();
    }
    
    private String getSuffix(Annotation[][] annotations, Object[] arguments) {
        Map<Long, String> cacheKeyMap = new LinkedHashMap<>();
        
        for (int i = 0; i < annotations.length; i++) {
            CacheKey cacheKey = getCacheKey(annotations[i]);
            if (cacheKey != null) {
                cacheKeyMap.put(cacheKey.order(), arguments[i].toString());
            }
        }
        
        return joiningSuffixes(cacheKeyMap);
    }
    
    private String joiningSuffixes(Map<Long, String> cacheKeyMap) {
        return cacheKeyMap.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(Map.Entry::getValue)
            .collect(Collectors.joining(KEY_DELIMITER));
    }
}
```

### 2. 캐시 적용 서비스
```java
@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    
    @Override
    @CacheGet(keyPrefixType = CacheKeyPrefixType.PRODUCT, timeoutSeconds = 600)
    public Product findById(@CacheKey(order = 1) Long productId, 
                           @CacheKey(order = 2) String version,
                           String cacheKey) {
        log.info("Cache miss for key: {}, fetching from database", cacheKey);
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    }
    
    @Override
    @CacheGet(keyPrefixType = CacheKeyPrefixType.CATEGORY, timeoutSeconds = 300)
    public List<Product> findByCategory(@CacheKey(order = 1) String category,
                                       @CacheKey(order = 2) Integer limit,
                                       String cacheKey) {
        log.info("Cache miss for category: {}, fetching from database", category);
        return productRepository.findByCategoryWithLimit(category, limit);
    }
    
    @Override
    @CacheGet(keyPrefixType = CacheKeyPrefixType.USER_PRODUCT, timeoutSeconds = 180)
    public List<Product> getRecommendations(@CacheKey(order = 1) Long userId,
                                          @CacheKey(order = 2) String algorithm,
                                          String cacheKey) {
        log.info("Cache miss for user recommendations: {}, algorithm: {}", userId, algorithm);
        return recommendationEngine.getRecommendations(userId, algorithm);
    }
}
```

### 3. 캐시 키 생성 결과 예시
```java
// findById(123L, "v1", cacheKey) 호출 시
// 생성되는 캐시 키: "PRODUCT::ProductServiceImpl::findById::123::v1::1640995200"

// findByCategory("electronics", 10, cacheKey) 호출 시  
// 생성되는 캐시 키: "CATEGORY::ProductServiceImpl::findByCategory::electronics::10::1640995200"

// getRecommendations(456L, "collaborative", cacheKey) 호출 시
// 생성되는 캐시 키: "USER_PRODUCT::ProductServiceImpl::getRecommendations::456::collaborative::1640995200"
```

## 🎯 할인 정책 시스템 (Product Domain)

### 할인 규칙 구현 (상품 도메인)
```java
// 금액 기반 할인 (상품별)
@Component
public class AmountSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        Product product = productService.findById(order.getProductId());
        return product.getPrice() >= 50000L; // 5만원 이상 상품
    }
}

// 재고 기반 할인
@Component
public class StockSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        Product product = productService.findById(order.getProductId());
        return product.getStock() > 10; // 재고 10개 초과 시
    }
}

// 이벤트 상품 할인
@Component
public class EventSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        Product product = productService.findById(order.getProductId());
        return "EVENT".equals(product.getCategory()); // 이벤트 카테고리
    }
}

// 신상품 할인
@Component
public class LegalSaleRole implements Saleable {
    @Override
    public boolean isSaleable(Order order) {
        Product product = productService.findById(order.getProductId());
        return ChronoUnit.DAYS.between(product.getCreatedAt(), Instant.now()) <= 30; // 30일 이내 신상품
    }
}
```

### 상품별 할인 계산기
```java
@Service
public class ProductDiscountService {
    
    private final SaleCommand saleCommand;
    private final ProductService productService;
    
    public DiscountResult calculateProductDiscount(Long productId, Set<SaleRoleType> discountTypes) {
        Product product = productService.findById(productId);
        Order mockOrder = Order.builder()
            .productId(productId)
            .amount(product.getPrice())
            .build();
            
        boolean canApplyDiscount = saleCommand.isSaleable(mockOrder, discountTypes);
        
        if (canApplyDiscount) {
            Long discountRate = calculateDiscountRate(discountTypes);
            Long discountedPrice = product.getPrice() * (100 - discountRate) / 100;
            
            return DiscountResult.builder()
                .productId(productId)
                .originalPrice(product.getPrice())
                .discountedPrice(discountedPrice)
                .discountRate(discountRate)
                .discountTypes(discountTypes)
                .applied(true)
                .build();
        }
        
        return DiscountResult.builder()
            .productId(productId)
            .originalPrice(product.getPrice())
            .applied(false)
            .reason("Product does not meet discount criteria")
            .build();
    }
}
```

## 🔧 설정 및 구성

### 애플리케이션 설정
```yaml
spring:
  application:
    name: product
  cloud:
    config:
      uri: http://localhost:8888

server:
  port: 8083
  tomcat:
    threads:
      max: 1

logging:
  level:
    org.springframework: DEBUG
    com.genius.primavera.cache: DEBUG
```

### AOP 설정
```java
@Configuration
@EnableAspectJAutoProxy
public class AopConfiguration {
    
    @Bean
    public CacheAspect cacheAspect() {
        return new CacheAspect();
    }
    
    @Bean
    public CacheKeyGenerator defaultCacheKeyGenerator() {
        return new DefaultCacheKeyGenerator();
    }
}
```

## 🌐 API 엔드포인트

### 상품 관리 API
```http
# 상품 상세 조회 (캐시 적용)
GET /products/{id}?version=v1

# 카테고리별 상품 조회 (캐시 적용)  
GET /products/category/{category}?limit=10

# 사용자 맞춤 추천 (캐시 적용)
GET /products/recommendations/{userId}?algorithm=collaborative

# 상품 할인 계산
POST /products/{productId}/discount
Content-Type: application/json
{
  "discountTypes": ["AMOUNT", "STOCK"]
}

# 신상품 목록
GET /products/new?days=30

# 재고 부족 상품 목록
GET /products/low-stock?threshold=5
```

### 캐시 관리 API
```http
# 캐시 통계 조회
GET /actuator/cache-stats

# 특정 캐시 키 삭제
DELETE /cache/{cacheKey}

# 상품 캐시 전체 삭제
DELETE /cache/products

# 캐시 히트율 조회
GET /cache/hit-ratio
```

## 📊 성능 모니터링

### 캐시 성능 메트릭
```java
@Component
public class CacheMetrics {
    
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);
    
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }
    
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }
    
    public double getHitRatio() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }
    
    @Scheduled(fixedRate = 60000) // 1분마다
    public void logCacheStats() {
        log.info("Cache Stats - Hits: {}, Misses: {}, Hit Ratio: {:.2f}%", 
            cacheHits.get(), cacheMisses.get(), getHitRatio() * 100);
    }
}
```

### 캐시 워밍업
```java
@EventListener(ApplicationReadyEvent.class)
public void warmUpCache() {
    log.info("Starting cache warm-up...");
    
    // 인기 카테고리 사전 로드
    List<String> popularCategories = Arrays.asList(
        "electronics", "books", "clothing", "home"
    );
    
    popularCategories.forEach(category -> {
        try {
            productService.findByCategory(category, 20, null);
            log.debug("Warmed up cache for category: {}", category);
        } catch (Exception e) {
            log.warn("Failed to warm up cache for category: {}", category, e);
        }
    });
    
    log.info("Cache warm-up completed");
}
```

## 🏃‍♂️ 실행 방법

### 1. Config Server 시작
```bash
# Configuration 서비스 먼저 실행
./gradlew :chap18:configuration:bootRun
```

### 2. Product Service 시작
```bash
# Product 서비스 실행
./gradlew :chap18:product:bootRun

# 또는 JAR 직접 실행
java -jar product/build/libs/product.jar
```

### 3. 서비스 동작 확인
```bash
# 서비스 상태 확인
curl http://localhost:8083/actuator/health

# 상품 조회 (첫 번째 호출 - 캐시 미스)
curl "http://localhost:8083/products/1?version=v1"

# 동일한 상품 재조회 (캐시 히트)
curl "http://localhost:8083/products/1?version=v1"

# 카테고리별 상품 조회
curl "http://localhost:8083/products/category/electronics?limit=10"

# 상품 할인 계산
curl -X POST http://localhost:8083/products/1/discount \
  -H "Content-Type: application/json" \
  -d '{"discountTypes":["AMOUNT","STOCK"]}'
```

## 🧪 테스트 전략

### 캐싱 동작 테스트
```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class ProductCacheTest {
    
    @Autowired
    private ProductService productService;
    
    @MockBean
    private ProductRepository productRepository;
    
    @Test
    @Order(1)
    void shouldCacheMissOnFirstCall() {
        // Given
        Product product = Product.builder().id(1L).name("Test Product").build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        // When
        Product result = productService.findById(1L, "v1", null);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1L);
    }
    
    @Test
    @Order(2) 
    void shouldCacheHitOnSecondCall() {
        // When - 동일한 파라미터로 재호출
        Product result = productService.findById(1L, "v1", null);
        
        // Then - 캐시에서 반환되어 Repository 호출 없음
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1L); // 여전히 1번만 호출
    }
}
```

### AOP 동작 테스트
```java
@Test
void shouldGenerateCorrectCacheKey() {
    // Given
    Method method = ProductServiceImpl.class.getMethod("findById", Long.class, String.class, String.class);
    Object[] args = {123L, "v2", null};
    
    // When
    String cacheKey = cacheAspect.generateCacheKey(method, args);
    
    // Then
    assertThat(cacheKey).contains("ProductServiceImpl");
    assertThat(cacheKey).contains("findById");
    assertThat(cacheKey).contains("123");
    assertThat(cacheKey).contains("v2");
}
```

### 할인 정책 테스트
```java
@Test
void shouldCalculateProductDiscountCorrectly() {
    // Given
    Product expensiveProduct = Product.builder()
        .id(1L)
        .price(100000L)  // 10만원
        .stock(15)       // 재고 15개
        .category("ELECTRONICS")
        .build();
    
    when(productService.findById(1L)).thenReturn(expensiveProduct);
    
    Set<SaleRoleType> discountTypes = Set.of(SaleRoleType.AMOUNT, SaleRoleType.STOCK);
    
    // When
    DiscountResult result = productDiscountService.calculateProductDiscount(1L, discountTypes);
    
    // Then
    assertThat(result.isApplied()).isTrue();
    assertThat(result.getOriginalPrice()).isEqualTo(100000L);
    assertThat(result.getDiscountedPrice()).isLessThan(100000L);
}
```

## 📈 성능 벤치마크

### 캐시 효과 측정
```java
@Component
public class CachePerformanceBenchmark {
    
    @EventListener(ApplicationReadyEvent.class)
    public void runBenchmark() {
        // 캐시 없이 1000번 호출
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            productRepository.findById(1L);
        }
        long withoutCache = System.currentTimeMillis() - startTime;
        
        // 캐시 있이 1000번 호출 (첫 번째만 DB 액세스)
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            productService.findById(1L, "v1", null);
        }
        long withCache = System.currentTimeMillis() - startTime;
        
        log.info("Performance Benchmark:");
        log.info("Without Cache: {}ms", withoutCache);
        log.info("With Cache: {}ms", withCache);
        log.info("Performance Improvement: {}x", (double) withoutCache / withCache);
    }
}
```

## 📚 학습 포인트

이 Product Service는 다음과 같은 고급 Spring 패턴들을 학습할 수 있습니다:

1. **AOP (Aspect-Oriented Programming)**: 횡단 관심사의 모듈화
2. **Custom Annotations**: 도메인 특화 애노테이션 설계와 구현
3. **Cache Strategy**: 효율적인 캐싱 전략과 키 생성 알고리즘
4. **Strategy Pattern**: 할인 정책의 유연한 구현과 확장
5. **Performance Optimization**: 캐시를 통한 성능 최적화 기법
6. **Monitoring & Metrics**: 캐시 성능 모니터링과 메트릭 수집

Product Service는 실제 운영 환경에서 중요한 성능 최적화 기법들을 종합적으로 학습할 수 있는 완벽한 예제이며, 특히 AOP와 커스텀 애노테이션을 활용한 고급 캐싱 시스템의 설계와 구현을 실습할 수 있습니다.