package com.genius.primavera.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProductBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductBatchApplication.class, args);
    }
}