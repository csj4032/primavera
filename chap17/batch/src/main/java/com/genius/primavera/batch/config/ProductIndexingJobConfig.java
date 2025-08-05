package com.genius.primavera.batch.config;

import com.genius.primavera.batch.processor.ProductDocumentProcessor;
import com.genius.primavera.batch.writer.ElasticsearchItemWriter;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ProductIndexingJobConfig {

    private final ProductDocumentProcessor productDocumentProcessor;
    private final ElasticsearchItemWriter elasticsearchItemWriter;
    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job productIndexingJob(JobRepository jobRepository, Step indexingStep) {
        return new JobBuilder("productIndexingJob", jobRepository)
                .start(indexingStep)
                .build();
    }

    @Bean
    public Step indexingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("indexingStep", jobRepository)
                .<Product, ProductDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(productItemReader())
                .processor(productDocumentProcessor)
                .writer(elasticsearchItemWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Product> productItemReader() {
        // N+1 문제를 피하기 위해 JOIN FETCH 사용
        String jpqlQuery = "SELECT p FROM Product p " +
                "JOIN FETCH p.seller " +
                "JOIN FETCH p.category";

        return new JpaPagingItemReaderBuilder<Product>()
                .name("productItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(jpqlQuery)
                .build();
    }
}