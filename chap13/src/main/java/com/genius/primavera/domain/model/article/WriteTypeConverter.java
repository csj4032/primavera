package com.genius.primavera.domain.model.article;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WriteTypeConverter implements Converter<String, WriteType> {

    @Override
    public WriteType convert(String source) {
        return WriteType.of(source);
    }
}
