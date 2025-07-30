package com.genius.primavera.domain.converter;

import com.genius.primavera.domain.model.article.ArticleStatus;

public class ArticleStatusAttributeConverter extends EnumAttributeConverter<ArticleStatus, Integer> {

    @Override
    protected Class<ArticleStatus> enumClass() {
        return ArticleStatus.class;
    }
}
