package com.genius.primavera.batch;

import com.genius.primavera.batch.processor.ProductDocumentProcessor;
import com.genius.primavera.common.domain.Category;
import com.genius.primavera.common.domain.Product;
import com.genius.primavera.common.domain.ProductStatus;
import com.genius.primavera.common.domain.Seller;
import com.genius.primavera.common.dto.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductDocumentProcessor 단위 테스트")
class ProductDocumentProcessorTest {

    private ProductDocumentProcessor processor;
    
    private Product testProduct;
    private Seller testSeller;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        processor = new ProductDocumentProcessor();

        testSeller = Seller.builder()
                .id(1L)
                .name("테스트 판매자")
                .email("test@seller.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("전자제품")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build();

        testProduct = Product.builder()
                .id(100L)
                .name("MacBook Pro 16인치 M3 Pro")
                .description("Apple의 최신 MacBook Pro 16인치 모델로 M3 Pro 칩셋을 탑재했습니다.")
                .price(3200000)
                .status(ProductStatus.ACTIVE)
                .seller(testSeller)
                .category(testCategory)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Product를 ProductDocument로 정상 변환한다")
    void shouldProcessProductToDocumentSuccessfully() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result, "처리 결과가 null이 아니어야 한다");
        assertEquals(testProduct.getId(), result.getProductId(), "상품 ID가 일치해야 한다");
        assertEquals(testProduct.getName(), result.getName(), "상품명이 일치해야 한다");
        assertEquals(testProduct.getDescription(), result.getDescription(), "상품 설명이 일치해야 한다");
        assertEquals(testProduct.getPrice(), result.getPrice(), "가격이 일치해야 한다");
        assertEquals(testProduct.getStatus().name(), result.getStatus(), "상태가 일치해야 한다");
        
        log.info("처리된 ProductDocument: {}", result);
    }

    @Test
    @DisplayName("판매자 정보가 정확하게 매핑된다")
    void shouldMapSellerInfoCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getSeller(), "판매자 정보가 존재해야 한다");
        assertEquals(testSeller.getId(), result.getSeller().getId(), "판매자 ID가 일치해야 한다");
        assertEquals(testSeller.getName(), result.getSeller().getName(), "판매자명이 일치해야 한다");
        assertEquals(testSeller.getEmail(), result.getSeller().getEmail(), "판매자 이메일이 일치해야 한다");
        assertEquals(testSeller.getRating(), result.getSeller().getRating(), "판매자 평점이 일치해야 한다");
        
        log.info("판매자 정보: {}", result.getSeller());
    }

    @Test
    @DisplayName("카테고리 정보가 정확하게 매핑된다")
    void shouldMapCategoryInfoCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getCategory(), "카테고리 정보가 존재해야 한다");
        assertEquals(testCategory.getId(), result.getCategory().getId(), "카테고리 ID가 일치해야 한다");
        assertEquals(testCategory.getName(), result.getCategory().getName(), "카테고리명이 일치해야 한다");
        assertEquals(testCategory.getLevel(), result.getCategory().getLevel(), "카테고리 레벨이 일치해야 한다");
        assertEquals("전자제품 > 컴퓨터 > " + testCategory.getName(), 
                result.getCategory().getFullPath(), "카테고리 전체 경로가 올바르게 생성되어야 한다");
        
        log.info("카테고리 정보: {}", result.getCategory());
    }

    @Test
    @DisplayName("검색 키워드가 상품명에서 추출된다")
    void shouldExtractSearchKeywordsFromProductName() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getSearchKeywords(), "검색 키워드가 존재해야 한다");
        assertFalse(result.getSearchKeywords().isEmpty(), "검색 키워드가 비어있지 않아야 한다");
        
        assertTrue(result.getSearchKeywords().contains("MacBook"), "MacBook 키워드가 포함되어야 한다");
        assertTrue(result.getSearchKeywords().contains("Pro"), "Pro 키워드가 포함되어야 한다");
        
        log.info("검색 키워드: {}", result.getSearchKeywords());
    }

    @Test
    @DisplayName("가격 범위가 올바르게 결정된다")
    void shouldDeterminePriceRangeCorrectly() throws Exception {
        testProduct = Product.builder()
                .id(1L)
                .name("저가 상품")
                .description("저가 상품")
                .price(300000)
                .status(ProductStatus.ACTIVE)
                .seller(testSeller)
                .category(testCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ProductDocument lowPriceResult = processor.process(testProduct);
        assertEquals("LOW", lowPriceResult.getPriceRange(), "30만원은 LOW 가격대여야 한다");

        testProduct.setPrice(700000);
        ProductDocument mediumPriceResult = processor.process(testProduct);
        assertEquals("MEDIUM", mediumPriceResult.getPriceRange(), "70만원은 MEDIUM 가격대여야 한다");

        testProduct.setPrice(1200000);
        ProductDocument highPriceResult = processor.process(testProduct);
        assertEquals("HIGH", highPriceResult.getPriceRange(), "120만원은 HIGH 가격대여야 한다");
        
        log.info("가격 범위 - LOW: {}, MEDIUM: {}, HIGH: {}", 
                lowPriceResult.getPriceRange(), 
                mediumPriceResult.getPriceRange(), 
                highPriceResult.getPriceRange());
    }

    @Test
    @DisplayName("날짜 정보가 올바르게 변환된다")
    void shouldConvertDateTimeCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getIndexedAt(), "색인 시간이 설정되어야 한다");
        assertNotNull(result.getLastModified(), "마지막 수정 시간이 설정되어야 한다");
        
        log.info("색인 시간: {}, 마지막 수정: {}", 
                result.getIndexedAt(), result.getLastModified());
    }

    @Test
    @DisplayName("null 상품에 대해 예외를 발생시킨다")
    void shouldThrowExceptionForNullProduct() {
        assertThrows(Exception.class, () -> processor.process(null), 
                "null 상품에 대해 예외가 발생해야 한다");
    }

    @Test
    @DisplayName("다양한 상태의 상품을 처리할 수 있다")
    void shouldProcessDifferentProductStatuses() throws Exception {
        for (ProductStatus status : ProductStatus.values()) {
            testProduct.setStatus(status);
            ProductDocument result = processor.process(testProduct);
            
            assertEquals(status.name(), result.getStatus(), 
                    "상품 상태가 올바르게 매핑되어야 한다: " + status);
            
            log.info("상품 상태 처리: {} -> {}", status, result.getStatus());
        }
    }

    @Test
    @DisplayName("한글 상품명이 올바르게 처리된다")
    void shouldProcessKoreanProductName() throws Exception {
        testProduct.setName("삼성 갤럭시 S24 울트라 스마트폰");
        
        ProductDocument result = processor.process(testProduct);
        
        assertEquals("삼성 갤럭시 S24 울트라 스마트폰", result.getName(), 
                "한글 상품명이 올바르게 처리되어야 한다");
        
        assertFalse(result.getSearchKeywords().isEmpty(), 
                "한글 상품명에서도 키워드가 추출되어야 한다");
        
        log.info("한글 상품명 처리 결과: {}", result.getName());
        log.info("한글 키워드: {}", result.getSearchKeywords());
    }
}