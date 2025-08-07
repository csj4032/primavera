package com.genius.primavera.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.genius.primavera.batch.repository.CategoryRepository;
import com.genius.primavera.batch.repository.ProductRepository;
import com.genius.primavera.batch.repository.SellerRepository;
import com.genius.primavera.batch.service.ProductIndexingService;
import com.genius.primavera.common.domain.Category;
import com.genius.primavera.common.domain.Product;
import com.genius.primavera.common.domain.ProductStatus;
import com.genius.primavera.common.domain.Seller;
import com.genius.primavera.batch.elasticsearch.ProductDocument;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Simple Elasticsearch Batch 테스트")
@SpringBootTest(classes = {com.genius.primavera.ProductBatchApplication.class})
@EnableTestContainers(value = {
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primavera"),
    @EnableTestContainers.TestContainer(type = ContainerType.ELASTICSEARCH, name = "elasticsearch")
})
public class SimpleElasticsearchBatchTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductIndexingService productIndexingService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String PRODUCTS_INDEX = "products";

    @BeforeEach
    void setUp() throws IOException {
        // 데이터베이스 초기화
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        sellerRepository.deleteAll();
        
        // 기존 인덱스 삭제
        boolean exists = elasticsearchClient.indices().exists(
            ExistsRequest.of(e -> e.index(PRODUCTS_INDEX))
        ).value();

        if (exists) {
            elasticsearchClient.indices().delete(d -> d.index(PRODUCTS_INDEX));
            log.info("기존 인덱스 삭제 완료");
        }
    }

    @Test
    @Order(1)
    @DisplayName("데이터베이스에 데이터를 저장하고 Elasticsearch에 인덱싱할 수 있다")
    void shouldSaveDataAndIndexToElasticsearch() throws Exception {
        // 1. 카테고리 생성
        Category category = categoryRepository.save(Category.builder()
                .name("노트북")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 2. 판매자 생성
        Seller seller = sellerRepository.save(Seller.builder()
                .name("테크 스토어")
                .email("tech@store.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 3. 상품 생성
        Product product = productRepository.save(Product.builder()
                .name("MacBook Pro 16인치")
                .description("Apple M3 Pro 칩셋 탑재 최신 맥북 프로")
                .price(3500000)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .seller(seller)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        log.info("데이터베이스에 저장 완료:");
        log.info("- 카테고리: {}", category.getName());
        log.info("- 판매자: {}", seller.getName());
        log.info("- 상품: {} (ID: {})", product.getName(), product.getId());

        // 4. Elasticsearch 인덱스 생성
        productIndexingService.createProductsIndexIfNotExists();
        
        // 5. 상품을 Elasticsearch에 인덱싱
        productIndexingService.indexProduct(product);
        
        // 6. 인덱싱 완료 대기
        Thread.sleep(1000);
        
        // 7. 검색 확인
        SearchResponse<ProductDocument> searchResponse = productIndexingService.searchProducts("MacBook");
        
        assertNotNull(searchResponse, "검색 응답이 null이 아니어야 한다");
        assertTrue(searchResponse.hits().total().value() > 0, "MacBook 검색 결과가 있어야 한다");
        
        ProductDocument doc = searchResponse.hits().hits().get(0).source();
        assertNotNull(doc, "문서가 존재해야 한다");
        assertEquals(product.getName(), doc.getName(), "상품명이 일치해야 한다");
        assertEquals(product.getPrice(), doc.getPrice(), "가격이 일치해야 한다");
        assertEquals(seller.getName(), doc.getSellerName(), "판매자명이 일치해야 한다");
        assertEquals(category.getName(), doc.getCategoryName(), "카테고리명이 일치해야 한다");
        
        log.info("Elasticsearch 인덱싱 및 검색 성공");
        log.info("검색된 상품: {}", doc.getName());
    }

    @Test
    @Order(2)
    @DisplayName("여러 상품을 데이터베이스에 저장하고 일괄 인덱싱할 수 있다")
    void shouldBatchIndexMultipleProducts() throws Exception {
        // 1. 카테고리 생성 (고유한 이름 사용)
        Category laptop = categoryRepository.save(Category.builder()
                .name("노트북-" + System.currentTimeMillis())
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        Category phone = categoryRepository.save(Category.builder()
                .name("스마트폰-" + System.currentTimeMillis())
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 2. 판매자 생성 (고유한 이메일 사용)
        Seller techStore = sellerRepository.save(Seller.builder()
                .name("테크 스토어-" + System.currentTimeMillis())
                .email("tech" + System.currentTimeMillis() + "@store.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build());
        
        Seller digitalMall = sellerRepository.save(Seller.builder()
                .name("디지털 몰-" + System.currentTimeMillis())
                .email("digital" + System.currentTimeMillis() + "@mall.com")
                .rating(4.2)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 3. 여러 상품 생성
        Product macbook = productRepository.save(Product.builder()
                .name("MacBook Pro 16인치")
                .description("Apple M3 Pro 칩셋 탑재")
                .price(3500000)
                .status(ProductStatus.ACTIVE)
                .category(laptop)
                .seller(techStore)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product dell = productRepository.save(Product.builder()
                .name("Dell XPS 15")
                .description("인텔 13세대 프로세서 탑재")
                .price(2500000)
                .status(ProductStatus.ACTIVE)
                .category(laptop)
                .seller(digitalMall)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product iphone = productRepository.save(Product.builder()
                .name("iPhone 15 Pro Max")
                .description("최신 A17 Pro 칩셋 탑재")
                .price(1900000)
                .status(ProductStatus.ACTIVE)
                .category(phone)
                .seller(techStore)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        
        Product galaxy = productRepository.save(Product.builder()
                .name("Galaxy S24 Ultra")
                .description("스냅드래곤 8 Gen 3 탑재")
                .price(1800000)
                .status(ProductStatus.ACTIVE)
                .category(phone)
                .seller(digitalMall)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        log.info("데이터베이스에 {}개 상품 저장 완료", 4);

        // 4. Elasticsearch 인덱스 생성
        productIndexingService.createProductsIndexIfNotExists();
        
        // 5. 모든 상품을 Elasticsearch에 인덱싱
        productIndexingService.indexProduct(macbook);
        productIndexingService.indexProduct(dell);
        productIndexingService.indexProduct(iphone);
        productIndexingService.indexProduct(galaxy);
        
        // 6. 인덱싱 완료 대기
        Thread.sleep(2000);
        
        // 7. 카테고리별 검색 확인
        SearchResponse<ProductDocument> laptopSearch = productIndexingService.searchProducts("노트북");
        assertTrue(laptopSearch.hits().total().value() >= 2, "노트북 카테고리 상품이 2개 이상 검색되어야 한다");
        log.info("노트북 검색 결과: {}개", laptopSearch.hits().total().value());
        
        SearchResponse<ProductDocument> phoneSearch = productIndexingService.searchProducts("스마트폰");
        assertTrue(phoneSearch.hits().total().value() >= 2, "스마트폰 카테고리 상품이 2개 이상 검색되어야 한다");
        log.info("스마트폰 검색 결과: {}개", phoneSearch.hits().total().value());
        
        // 8. 판매자별 검색 확인 (첫 번째 판매자 이름 동적으로 사용)
        SearchResponse<ProductDocument> techStoreSearch = productIndexingService.searchProducts("테크");
        assertTrue(techStoreSearch.hits().total().value() >= 2, "테크 스토어 판매 상품이 2개 이상 검색되어야 한다");
        log.info("테크 스토어 검색 결과: {}개", techStoreSearch.hits().total().value());
        
        // 9. 전체 문서 수 확인
        CountResponse countResponse = elasticsearchClient.count(c -> c.index(PRODUCTS_INDEX));
        assertEquals(4, countResponse.count(), "총 4개의 문서가 인덱싱되어야 한다");
        log.info("전체 인덱싱된 문서 수: {}", countResponse.count());
    }

    @Test
    @Order(3)
    @DisplayName("데이터베이스에서 모든 상품을 조회하여 인덱싱할 수 있다")
    @Transactional
    void shouldIndexAllProductsFromDatabase() throws Exception {
        // 1. 테스트 데이터 준비
        long timestamp = System.currentTimeMillis();
        Category category = categoryRepository.save(Category.builder()
                .name("전자제품-" + timestamp)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build());
        
        Seller seller = sellerRepository.save(Seller.builder()
                .name("종합 전자-" + timestamp)
                .email("general" + timestamp + "@electronics.com")
                .rating(4.3)
                .createdAt(LocalDateTime.now())
                .build());
        
        // 여러 상품 생성
        for (int i = 1; i <= 5; i++) {
            productRepository.save(Product.builder()
                    .name("전자제품 " + timestamp + "-" + i)
                    .description("고품질 전자제품 #" + i)
                    .price(100000 * i)
                    .status(ProductStatus.ACTIVE)
                    .category(category)
                    .seller(seller)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        
        log.info("데이터베이스에 5개 상품 저장 완료");
        
        // 2. 데이터베이스에서 모든 상품 조회 (연관 엔티티 fetch join으로 가져오기)
        Iterable<Product> allProducts = productRepository.findAll();
        
        // 3. Elasticsearch 인덱스 생성
        productIndexingService.createProductsIndexIfNotExists();
        
        // 4. 모든 상품 인덱싱 (트랜잭션 내에서 연관 관계 접근 가능)
        int count = 0;
        for (Product product : allProducts) {
            // 연관 관계를 미리 로드하여 LazyInitializationException 방지
            product.getCategory().getName(); // 강제로 로드
            product.getSeller().getName(); // 강제로 로드
            productIndexingService.indexProduct(product);
            count++;
        }
        
        log.info("{}개 상품 인덱싱 완료", count);
        
        // 5. 인덱싱 완료 대기
        Thread.sleep(2000);
        
        // 6. 인덱싱 확인
        CountResponse countResponse = elasticsearchClient.count(c -> c.index(PRODUCTS_INDEX));
        assertEquals(5, countResponse.count(), "5개의 문서가 인덱싱되어야 한다");
        log.info("Elasticsearch 문서 수: {}", countResponse.count());
        
        // 7. 데이터베이스와 Elasticsearch 일관성 확인
        long dbCount = productRepository.count();
        assertEquals(dbCount, countResponse.count(), "데이터베이스와 Elasticsearch 문서 수가 일치해야 한다");
        log.info("데이터 일관성 확인 - DB: {}, ES: {}", dbCount, countResponse.count());
    }
}