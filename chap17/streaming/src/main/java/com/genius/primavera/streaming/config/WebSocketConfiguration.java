package com.genius.primavera.streaming.config;

import com.genius.primavera.common.dto.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Slf4j
@Configuration
public class WebSocketConfiguration {

    @Bean
    public Sinks.Many<ProductDocument> productSink() {
        Sinks.Many<ProductDocument> sink = Sinks.many().multicast().onBackpressureBuffer();
        log.info("Product sink created for WebSocket streaming");
        return sink;
    }
}