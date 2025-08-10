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
@DisplayName("ProductDocumentProcessor test")
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
                .name("test connection")
                .email("test@seller.com")
                .rating(4.5)
                .createdAt(LocalDateTime.now())
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("file")
                .level(1)
                .createdAt(LocalDateTime.now())
                .build();

        testProduct = Product.builder()
                .id(100L)
                .name("MacBook Pro 16test M3 Pro")
                .description("Appleshould test MacBook Pro 16test connection M3 Pro connection with.")
                .price(3200000)
                .status(ProductStatus.ACTIVE)
                .seller(testSeller)
                .category(testCategory)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Productshould ProductDocumentshould test file")
    void shouldProcessProductToDocumentSuccessfully() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result, "processing logging nullshould file test");
        assertEquals(testProduct.getId(), result.getProductId(), "test IDshould file test");
        assertEquals(testProduct.getName(), result.getName(), "testshould file test");
        assertEquals(testProduct.getDescription(), result.getDescription(), "test should file test");
        assertEquals(testProduct.getPrice(), result.getPrice(), "shouldshould file test");
        assertEquals(testProduct.getStatus().name(), result.getStatus(), "testshould file test");
        
        log.info("processing ProductDocument: {}", result);
    }

    @Test
    @DisplayName("connection informationshould file")
    void shouldMapSellerInfoCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getSeller(), "connection informationshould file test");
        assertEquals(testSeller.getId(), result.getSeller().getId(), "connection IDshould file test");
        assertEquals(testSeller.getName(), result.getSeller().getName(), "connectionshould file test");
        assertEquals(testSeller.getEmail(), result.getSeller().getEmail(), "connection shouldshould file test");
        assertEquals(testSeller.getRating(), result.getSeller().getRating(), "connection should file test");
        
        log.info("connection information: {}", result.getSeller());
    }

    @Test
    @DisplayName("file informationshould file")
    void shouldMapCategoryInfoCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getCategory(), "file informationshould file test");
        assertEquals(testCategory.getId(), result.getCategory().getId(), "file IDshould file test");
        assertEquals(testCategory.getName(), result.getCategory().getName(), "fileshould file test");
        assertEquals(testCategory.getLevel(), result.getCategory().getLevel(), "file should file test");
        assertEquals("file > connection > " + testCategory.getName(), 
                result.getCategory().getFullPath(), "file test shouldshould file processing test");
        
        log.info("file information: {}", result.getCategory());
    }

    @Test
    @DisplayName("test connectionshould test file")
    void shouldExtractSearchKeywordsFromProductName() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getSearchKeywords(), "test connectionshould file test");
        assertFalse(result.getSearchKeywords().isEmpty(), "test connectionshould file connection test");
        
        assertTrue(result.getSearchKeywords().contains("MacBook"), "MacBook connectionshould processing test");
        assertTrue(result.getSearchKeywords().contains("Pro"), "Pro connectionshould processing test");
        
        log.info("test connection: {}", result.getSearchKeywords());
    }

    @Test
    @DisplayName("should testshould file")
    void shouldDeterminePriceRangeCorrectly() throws Exception {
        testProduct = Product.builder()
                .id(1L)
                .name("should test")
                .description("should test")
                .price(300000)
                .status(ProductStatus.ACTIVE)
                .seller(testSeller)
                .category(testCategory)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ProductDocument lowPriceResult = processor.process(testProduct);
        assertEquals("LOW", lowPriceResult.getPriceRange(), "30connection LOW should test");

        testProduct.setPrice(700000);
        ProductDocument mediumPriceResult = processor.process(testProduct);
        assertEquals("MEDIUM", mediumPriceResult.getPriceRange(), "70connection MEDIUM should test");

        testProduct.setPrice(1200000);
        ProductDocument highPriceResult = processor.process(testProduct);
        assertEquals("HIGH", highPriceResult.getPriceRange(), "120connection HIGH should test");
        
        log.info("should test - LOW: {}, MEDIUM: {}, HIGH: {}", 
                lowPriceResult.getPriceRange(), 
                mediumPriceResult.getPriceRange(), 
                highPriceResult.getPriceRange());
    }

    @Test
    @DisplayName("test informationshould file")
    void shouldConvertDateTimeCorrectly() throws Exception {
        ProductDocument result = processor.process(testProduct);

        assertNotNull(result.getIndexedAt(), "testshould processing test");
        assertNotNull(result.getLastModified(), "connection modification testshould processing test");
        
        log.info("test: {}, connection modification: {}", 
                result.getIndexedAt(), result.getLastModified());
    }

    @Test
    @DisplayName("null test should Endpoint")
    void shouldThrowExceptionForNullProduct() {
        assertThrows(Exception.class, () -> processor.process(null), 
                "null test should file test");
    }

    @Test
    @DisplayName("connection testshould test processing should exists")
    void shouldProcessDifferentProductStatuses() throws Exception {
        for (ProductStatus status : ProductStatus.values()) {
            testProduct.setStatus(status);
            ProductDocument result = processor.process(testProduct);
            
            assertEquals(status.name(), result.getStatus(), 
                    "testshould file processing test: " + status);
            
            log.info("test processing: {} -> {}", status, result.getStatus());
        }
    }

    @Test
    @DisplayName("testshould file processing")
    void shouldProcessKoreanProductName() throws Exception {
        testProduct.setName("test connection S24 connection file");
        
        ProductDocument result = processor.process(testProduct);
        
        assertEquals("test connection S24 connection file", result.getName(), 
                "testshould file processing test");
        
        assertFalse(result.getSearchKeywords().isEmpty(), 
                "test connectionshould processing test");
        
        log.info("test processing result: {}", result.getName());
        log.info("test connection: {}", result.getSearchKeywords());
    }
}