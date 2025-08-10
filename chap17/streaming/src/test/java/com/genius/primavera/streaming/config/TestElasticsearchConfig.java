package com.genius.primavera.streaming.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@TestConfiguration
public class TestElasticsearchConfig {

    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(Arrays.asList(
            new LongToInstantConverter(),
            new StringToInstantConverter(),
            new InstantToStringConverter()
        ));
    }

    static class LongToInstantConverter implements Converter<Long, Instant> {
        @Override
        public Instant convert(Long source) {
            return Instant.ofEpochMilli(source);
        }
    }

    static class StringToInstantConverter implements Converter<String, Instant> {
        @Override
        public Instant convert(String source) {
            try {
                return Instant.parse(source);
            } catch (Exception e) {
                try {
                    return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(source));
                } catch (Exception ex) {
                    return Instant.ofEpochMilli(Long.parseLong(source));
                }
            }
        }
    }

    static class InstantToStringConverter implements Converter<Instant, String> {
        @Override
        public String convert(Instant source) {
            return DateTimeFormatter.ISO_INSTANT.format(source);
        }
    }
}