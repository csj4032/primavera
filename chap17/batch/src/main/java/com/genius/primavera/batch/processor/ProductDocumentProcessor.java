package com.genius.primavera.batch.processor;
import com.genius.primavera.common.dto.ProductDocument;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Arrays;

@Component
public class ProductDocumentProcessor implements ItemProcessor<Product, ProductDocument> {

    @Override
    public ProductDocument process(Product product) throws Exception {
        var sellerDoc = new ProductDocument.SellerInfo(
                product.getSeller().getId(),
                product.getSeller().getName(),
                product.getSeller().getEmail(),
                product.getSeller().getRating()
        );

        var categoryDoc = new ProductDocument.CategoryInfo(
                product.getCategory().getId(),
                product.getCategory().getName(),
                "전자제품 > 컴퓨터 > " + product.getCategory().getName(),
                product.getCategory().getLevel()
        );

        return ProductDocument.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus().name())
                .seller(sellerDoc)
                .category(categoryDoc)
                .searchKeywords(Arrays.asList(product.getName().split(" ")))
                .priceRange(determinePriceRange(product.getPrice()))
                .indexedAt(product.getCreatedAt().atOffset(ZoneOffset.UTC).toInstant())
                .lastModified(product.getUpdatedAt().atOffset(ZoneOffset.UTC).toInstant())
                .build();
    }

    private String determinePriceRange(int price) {
        if (price < 500000) return "LOW";
        if (price < 1000000) return "MEDIUM";
        return "HIGH";
    }
}