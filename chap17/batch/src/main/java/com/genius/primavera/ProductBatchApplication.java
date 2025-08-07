package com.genius.primavera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.genius.primavera.batch.repository"})
@EntityScan(basePackages = {"com.genius.primavera.common.domain"})
@EnableJpaAuditing
public class ProductBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductBatchApplication.class, args);
    }
}