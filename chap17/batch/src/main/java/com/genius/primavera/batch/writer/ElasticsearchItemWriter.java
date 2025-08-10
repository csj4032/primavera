package com.genius.primavera.batch.writer;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import com.genius.primavera.common.dto.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchItemWriter implements ItemWriter<ProductDocument> {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "product_catalog_v1";

    @Override
    public void write(Chunk<? extends ProductDocument> chunk) throws Exception {
        var bulkRequest = buildBulkRequest(chunk.getItems());
        executeBulkRequest(bulkRequest, chunk.getItems().size());
    }

    private BulkRequest buildBulkRequest(List<? extends ProductDocument> items) {
        var builder = new BulkRequest.Builder();
        
        items.forEach(doc -> builder.operations(op -> op
                .index(idx -> idx
                        .index(INDEX_NAME)
                        .id(String.valueOf(doc.getProductId()))
                        .document(doc))));
        
        return builder.build();
    }

    private void executeBulkRequest(BulkRequest request, int itemCount) throws Exception {
        var response = elasticsearchClient.bulk(request);
        
        if (response.errors()) {
            var errorCount = (int) response.items().stream()
                    .filter(item -> item.error() != null)
                    .count();
            
            log.warn("Bulk indexing completed with {} errors out of {} documents", errorCount, itemCount);

            response.items().stream()
                    .filter(item -> item.error() != null)
                    .limit(5)
                    .forEach(item -> log.error("Indexing error for document {}: {}", 
                        item.id(), item.error().reason()));
        } else {
            log.info("Successfully indexed {} documents", itemCount);
        }
    }
}
