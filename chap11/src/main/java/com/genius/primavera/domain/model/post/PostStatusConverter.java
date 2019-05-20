package com.genius.primavera.domain.model.post;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PostStatusConverter implements Converter<String, PostStatus> {

    @Override
    public PostStatus convert(String source) {
        return PostStatus.of(Integer.valueOf(source));
    }
}