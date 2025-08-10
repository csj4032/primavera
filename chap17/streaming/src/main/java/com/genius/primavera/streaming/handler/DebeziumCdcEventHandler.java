package com.genius.primavera.streaming.handler;

import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.streaming.service.ProductSearchService;
import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumCdcEventHandler {

    private final ProductSearchService productSearchService;
    private final Sinks.Many<ProductDocument> productEventSink = Sinks.many().multicast().onBackpressureBuffer();
    
    private DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;
    private ExecutorService executor;

    @Value("${debezium.enabled:false}")
    private boolean enabled;

    @Value("${debezium.database.hostname:localhost}")
    private String dbHost;

    @Value("${debezium.database.port:3306}")
    private String dbPort;

    @Value("${debezium.database.user:primavera}")
    private String dbUser;

    @Value("${debezium.database.password:primavera}")
    private String dbPassword;

    @Value("${debezium.database.name:primavera}")
    private String dbName;

    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("Debezium CDC is disabled for streaming service");
            return;
        }

        Properties props = new Properties();
        props.setProperty("name", "product-streaming-cdc");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", "/tmp/streaming-offsets.dat");
        props.setProperty("offset.flush.interval.ms", "30000");
        
        props.setProperty("database.hostname", dbHost);
        props.setProperty("database.port", dbPort);
        props.setProperty("database.user", dbUser);
        props.setProperty("database.password", dbPassword);
        props.setProperty("database.dbname", dbName);
        props.setProperty("database.server.id", "85745");
        props.setProperty("database.server.name", "primavera-streaming");
        props.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
        props.setProperty("database.history.file.filename", "/tmp/streaming-dbhistory.dat");
        
        props.setProperty("table.include.list", "primavera.PRODUCTS");
        props.setProperty("include.schema.changes", "false");
        props.setProperty("snapshot.mode", "initial");

        Configuration config = Configuration.from(props);

        debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(config.asProperties())
                .notifying(this::handleChangeEvent)
                .build();

        executor = Executors.newSingleThreadExecutor();
        executor.execute(debeziumEngine);
        
        log.info("Debezium CDC engine started for streaming service");
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> recordChangeEvent) {
        SourceRecord sourceRecord = recordChangeEvent.record();
        
        if (sourceRecord.value() == null) {
            return;
        }

        Struct sourceRecordValue = (Struct) sourceRecord.value();
        
        if (sourceRecordValue != null) {
            String operation = sourceRecordValue.getString("op");
            
            Struct after = sourceRecordValue.getStruct("after");
            Struct before = sourceRecordValue.getStruct("before");
            
            log.debug("CDC Event - Operation: {}", operation);
            
            switch (operation) {
                case "c":
                case "u":
                case "r":
                    if (after != null) {
                        ProductDocument product = convertToProductDocument(after);
                        productSearchService.indexProduct(product)
                                .doOnSuccess(v -> {
                                    productEventSink.tryEmitNext(product);
                                    log.info("Product {} indexed from CDC event", product.getProductId());
                                })
                                .doOnError(error -> log.error("Failed to index product from CDC", error))
                                .subscribe();
                    }
                    break;
                case "d":
                    if (before != null) {
                        Long productId = before.getInt64("id");
                        productSearchService.deleteProduct(productId)
                                .doOnSuccess(v -> log.info("Product {} deleted from index", productId))
                                .doOnError(error -> log.error("Failed to delete product from index", error))
                                .subscribe();
                    }
                    break;
            }
        }
    }

    private ProductDocument convertToProductDocument(Struct data) {
        Long productId = data.getInt64("id");
        String name = data.getString("name");
        String description = data.getString("description");
        Integer price = data.getInt32("price");
        String status = data.getString("status");
        
        return ProductDocument.builder()
                .productId(productId)
                .name(name)
                .description(description)
                .price(price)
                .status(status)
                .searchKeywords(Arrays.asList(name.split(" ")))
                .priceRange(determinePriceRange(price))
                .indexedAt(Instant.now())
                .lastModified(Instant.now())
                .build();
    }

    private String determinePriceRange(Integer price) {
        if (price < 500000) return "LOW";
        if (price < 1000000) return "MEDIUM";
        return "HIGH";
    }

    @PreDestroy
    public void stop() {
        if (debeziumEngine != null) {
            try {
                debeziumEngine.close();
                log.info("Debezium streaming engine stopped");
            } catch (IOException e) {
                log.error("Error stopping Debezium engine", e);
            }
        }
        
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
