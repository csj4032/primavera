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
@DisplayName("ProductDocumentProcessor translated_text_2 test")
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
                .name("test translated_text_3")
                .email("test@seller.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("translated_text_4")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build();

        testProduct = Product.builder()
                .id(100L)
                .name("MacBook Pro 16translated_text_2 M3 Pro")
                .description("Appletranslated_text_1 translated_text_2 MacBook Pro 16translated_text_2 translated_text_3 M3 Pro translated_text_3 translated_text_6.")
                .price(3200000)
                .status(ProductStatus.ACTIVE)
                .seller(testSeller)
                .category(testCategory)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Producttranslated_text_1 ProductDocumenttranslated_text_1 translated_text_2 translated_text_4")
    void shouldProcessProductToDocumentSuccessfully() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result, "processing translated_text_7 nulltranslated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getId(), result.getProductId(), "translated_text_2 IDtranslated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getName(), result.getName(), "translated_text_2translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getDescription(), result.getDescription(), "translated_text_2 translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getPrice(), result.getPrice(), "translated_text_1translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testProduct.getStatus().name(), result.getStatus(), "translated_text_2translated_text_1 translated_text_4 translated_text_2");
        
        log.info("processing ProductDocument: {}", result);
    }

    @Test
    @DisplayName("translated_text_3 informationtranslated_text_1 translated_text_4 translated_text_4")
    void shouldMapSellerInfoCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getSeller(), "translated_text_3 informationtranslated_text_1 translated_text_4 translated_text_2");
        assertEquals(testSeller.getId(), result.getSeller().getId(), "translated_text_3 IDtranslated_text_1 translated_text_4 translated_text_2");
        assertEquals(testSeller.getName(), result.getSeller().getName(), "translated_text_3translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testSeller.getEmail(), result.getSeller().getEmail(), "translated_text_3 translated_text_1translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testSeller.getRating(), result.getSeller().getRating(), "translated_text_3 translated_text_1 translated_text_4 translated_text_2");
        
        log.info("translated_text_3 information: {}", result.getSeller());
    }

    @Test
    @DisplayName("translated_text_4 informationtranslated_text_1 translated_text_4 translated_text_4")
    void shouldMapCategoryInfoCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getCategory(), "translated_text_4 informationtranslated_text_1 translated_text_4 translated_text_2");
        assertEquals(testCategory.getId(), result.getCategory().getId(), "translated_text_4 IDtranslated_text_1 translated_text_4 translated_text_2");
        assertEquals(testCategory.getName(), result.getCategory().getName(), "translated_text_4translated_text_1 translated_text_4 translated_text_2");
        assertEquals(testCategory.getLevel(), result.getCategory().getLevel(), "translated_text_4 translated_text_1 translated_text_4 translated_text_2");
        assertEquals("translated_text_4 > translated_text_3 > " + testCategory.getName(), 
                result.getCategory().getFullPath(), "translated_text_4 translated_text_2 translated_text_1translated_text_1 translated_text_4 translated_text_11 translated_text_2");
        
        log.info("translated_text_4 information: {}", result.getCategory());
    }

    @Test
    @DisplayName("translated_text_2 translated_text_3translated_text_1 translated_text_2 translated_text_4")
    void shouldExtractSearchKeywordsFromProductName() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getSearchKeywords(), "translated_text_2 translated_text_3translated_text_1 translated_text_4 translated_text_2");
        assertFalse(result.getSearchKeywords().isEmpty(), "translated_text_2 translated_text_3translated_text_1 translated_text_4 translated_text_3 translated_text_2");
        
        assertTrue(result.getSearchKeywords().contains("MacBook"), "MacBook translated_text_3translated_text_1 translated_text_5 translated_text_2");
        assertTrue(result.getSearchKeywords().contains("Pro"), "Pro translated_text_3translated_text_1 translated_text_5 translated_text_2");
        
        log.info("translated_text_2 translated_text_3: {}", result.getSearchKeywords());
    }

    @Test
    @DisplayName("translated_text_1 translated_text_2translated_text_1 translated_text_4 translated_text_4")
    void shouldDeterminePriceRangeCorrectly() throws Exception {
        testProduct = Product.builder()
                .id(1L)
                .name("translated_text_1 translated_text_2")
                .description("translated_text_1 translated_text_2")
                .price(300000)
                .status(ProductStatus.ACTIVE)
                .seller(testSeller)
                .category(testCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ProductDocument lowPriceResult = processor.process(testProduct);
        assertEquals("LOW", lowPriceResult.getPriceRange(), "30translated_text_3 LOW translated_text_1 translated_text_2");

        testProduct.setPrice(700000);
        ProductDocument mediumPriceResult = processor.process(testProduct);
        assertEquals("MEDIUM", mediumPriceResult.getPriceRange(), "70translated_text_3 MEDIUM translated_text_1 translated_text_2");

        testProduct.setPrice(1200000);
        ProductDocument highPriceResult = processor.process(testProduct);
        assertEquals("HIGH", highPriceResult.getPriceRange(), "120translated_text_3 HIGH translated_text_1 translated_text_2");
        
        log.info("translated_text_1 translated_text_2 - LOW: {}, MEDIUM: {}, HIGH: {}", 
                lowPriceResult.getPriceRange(), 
                mediumPriceResult.getPriceRange(), 
                highPriceResult.getPriceRange());
    }

    @Test
    @DisplayName("translated_text_2 informationtranslated_text_1 translated_text_4 translated_text_4")
    void shouldConvertDateTimeCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getIndexedAt(), "translated_text_2 translated_text_2translated_text_1 translated_text_5 translated_text_2");
        assertNotNull(result.getLastModified(), "translated_text_3 modification translated_text_2translated_text_1 translated_text_5 translated_text_2");
        
        log.info("translated_text_2 translated_text_2: {}, translated_text_3 modification: {}", 
                result.getIndexedAt(), result.getLastModified());
    }

    @Test
    @DisplayName("null translated_text_2 translated_text_2 translated_text_1 translated_text_5")
    void shouldThrowExceptionForNullProduct() {
        assertThrows(Exception.class, () -> processor.process(null), 
                "null translated_text_2 translated_text_2 translated_text_1 translated_text_4 translated_text_2");
    }

    @Test
    @DisplayName("translated_text_3 translated_text_2translated_text_1 translated_text_2 processing translated_text_1 exists")
    void shouldProcessDifferentProductStatuses() throws Exception {
        for (ProductStatus status : ProductStatus.values()) {
            testProduct.setStatus(status);
            ProductDocument result = processor.process(testProduct);
            
            assertEquals(status.name(), result.getStatus(), 
                    "translated_text_2 translated_text_2translated_text_1 translated_text_4 translated_text_5 translated_text_2: " + status);
            
            log.info("translated_text_2 translated_text_2 processing: {} -> {}", status, result.getStatus());
        }
    }

    @Test
    @DisplayName("translated_text_2 translated_text_2translated_text_1 translated_text_4 processing")
    void shouldProcessKoreanProductName() throws Exception {
        testProduct.setName("translated_text_2 translated_text_3 S24 translated_text_3 translated_text_4");
        
        ProductDocument result = processor.process(testProduct);
        
        assertEquals("translated_text_2 translated_text_3 S24 translated_text_3 translated_text_4", result.getName(), 
                "translated_text_2 translated_text_2translated_text_1 translated_text_4 processing translated_text_2");
        
        assertFalse(result.getSearchKeywords().isEmpty(), 
                "translated_text_2 translated_text_2 translated_text_3translated_text_1 translated_text_5 translated_text_2");
        
        log.info("translated_text_2 translated_text_2 processing result: {}", result.getName());
        log.info("translated_text_2 translated_text_3: {}", result.getSearchKeywords());
    }
}