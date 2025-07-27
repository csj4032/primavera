package com.genius.primavera.domain.converter;

import com.genius.primavera.domain.model.post.PostStatus;

public class PostStatusAttributeConverter extends EnumAttributeConverter<PostStatus, Integer> {

    @Override
    protected Class<PostStatus> enumClass() {
        return PostStatus.class;
    }
}
