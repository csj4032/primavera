package com.genius.primavera.batch.writer;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import com.genius.primavera.common.dto.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchItemWriter implements ItemWriter<ProductDocument> {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "product_catalog_v1";

    @Override
    public void write(Chunk<? extends ProductDocument> chunk) throws Exception {
        BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

        for (ProductDocument doc : chunk.getItems()) {
            bulkRequestBuilder.operations(op -> op
                    .index(idx -> idx
                            .index(INDEX_NAME)
                            .id(String.valueOf(doc.getProductId()))
                            .document(doc)
                    )
            );
        }

        elasticsearchClient.bulk(bulkRequestBuilder.build());
        log.info("{} documents indexed.", chunk.getItems().size());
    }
}
