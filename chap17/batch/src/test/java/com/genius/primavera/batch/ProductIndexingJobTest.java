package com.genius.primavera.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.genius.primavera.batch.repository.ProductRepository;
import com.genius.primavera.common.domain.Product;
import com.genius.primavera.common.dto.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBatchTest
@TestPropertySource(properties = {
        "batch.async.enabled=false"
})
@DisplayName("Product Indexing Job 테스트")
class ProductIndexingJobTest extends AbstractIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils() {
            return new JobLauncherTestUtils();
        }
    }

    @Test
    @Order(1)
    @DisplayName("데이터베이스에서 상품 데이터를 조회할 수 있다")
    void shouldFindProductsInDatabase() {
        List<Product> products = productRepository.findAll();
        assertFalse(products.isEmpty(), "상품 데이터가 존재해야 한다");
        log.info("Found {} products in database", products.size());
        
        products.forEach(product -> {
            assertNotNull(product.getId());
            assertNotNull(product.getName());
            assertNotNull(product.getSeller());
            assertNotNull(product.getCategory());
            log.info("Product: {} - {} (₩{})", product.getId(), product.getName(), product.getPrice());
        });
    }

    @Test
    @Order(2) 
    @DisplayName("Product Indexing Job이 성공적으로 실행된다")
    void shouldRunProductIndexingJobSuccessfully() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("executionTime", LocalDateTime.now().toString())
                .addString("jobType", "TEST")
                .toJobParameters();

        var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode(), 
                "Job이 성공적으로 완료되어야 한다");
        
        log.info("Job execution status: {}", jobExecution.getStatus());
        log.info("Job exit status: {}", jobExecution.getExitStatus().getExitCode());
        
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            log.info("Step: {}, Read count: {}, Write count: {}", 
                    stepExecution.getStepName(), 
                    stepExecution.getReadCount(), 
                    stepExecution.getWriteCount());
        });
    }

    @Test
    @Order(3)
    @DisplayName("Elasticsearch에 상품 문서가 색인되었는지 확인한다")
    void shouldIndexProductsInElasticsearch() throws IOException, InterruptedException {
        Thread.sleep(2000);
        
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("product_catalog_v1")
                .query(q -> q.matchAll(m -> m))
                .size(100)
        );

        SearchResponse<ProductDocument> searchResponse = 
                elasticsearchClient.search(searchRequest, ProductDocument.class);

        assertTrue(searchResponse.hits().total().value() > 0, 
                "Elasticsearch에 최소 1개 이상의 문서가 색인되어야 한다");
        
        log.info("Elasticsearch에서 찾은 문서 수: {}", searchResponse.hits().total().value());
        
        searchResponse.hits().hits().forEach(hit -> {
            ProductDocument doc = hit.source();
            assertNotNull(doc);
            assertNotNull(doc.getProductId());
            assertNotNull(doc.getName());
            assertNotNull(doc.getPrice());
            assertNotNull(doc.getStatus());
            assertNotNull(doc.getIndexedAt());
            
            log.info("Indexed document: {} - {} (₩{})", 
                    doc.getProductId(), doc.getName(), doc.getPrice());
        });
    }

    @Test
    @Order(4)
    @DisplayName("색인된 상품을 검색할 수 있다")
    void shouldSearchIndexedProducts() throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("product_catalog_v1")
                .query(q -> q.match(m -> m
                        .field("name")
                        .query("Test")
                ))
                .size(10)
        );

        SearchResponse<ProductDocument> searchResponse = 
                elasticsearchClient.search(searchRequest, ProductDocument.class);

        assertTrue(searchResponse.hits().total().value() > 0, 
                "Test가 포함된 상품을 찾을 수 있어야 한다");
        
        log.info("검색 결과: {} 건", searchResponse.hits().total().value());
        
        searchResponse.hits().hits().forEach(hit -> {
            ProductDocument doc = hit.source();
            assertTrue(doc.getName().contains("Test"), 
                    "검색 결과에 'Test'가 포함되어야 한다: " + doc.getName());
            log.info("검색된 상품: {}", doc.getName());
        });
    }

    @Test
    @Order(5)
    @DisplayName("가격 범위로 상품을 검색할 수 있다")
    void shouldSearchProductsByPriceRange() throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("product_catalog_v1")
                .query(q -> q.range(r -> r
                        .field("price")
                        .gte(co.elastic.clients.json.JsonData.of(500000))
                        .lte(co.elastic.clients.json.JsonData.of(900000))
                ))
                .size(10)
        );

        SearchResponse<ProductDocument> searchResponse = 
                elasticsearchClient.search(searchRequest, ProductDocument.class);

        log.info("가격 범위 검색 결과: {} 건", searchResponse.hits().total().value());
        
        searchResponse.hits().hits().forEach(hit -> {
            ProductDocument doc = hit.source();
            assertTrue(doc.getPrice() >= 500000 && doc.getPrice() <= 900000, 
                    "가격이 범위 내에 있어야 한다: " + doc.getPrice());
            log.info("가격 범위 검색 상품: {} - ₩{}", doc.getName(), doc.getPrice());
        });
    }
}