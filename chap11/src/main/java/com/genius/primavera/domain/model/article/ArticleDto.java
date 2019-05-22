package com.genius.primavera.domain.model.article;

import lombok.Getter;
import lombok.Setter;

public class ArticleDto {

    @Getter
    @Setter
    public static class WriteRequestArticle {
        private long pId;
        private long reference;
        private ArticleStatus status = ArticleStatus.PUBLIC;
        private String subject;
        private long author;
        private String text;
    }
}
