package com.genius.primavera.domain.model.article;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.Setter;

public class ArticleDto {

    @Getter
    @Setter
    public static class WriteRequestArticle {
        private long pId;
        private long reference;
        private int step;
        private int level;
        private ArticleStatus status = ArticleStatus.PUBLIC;
        private String subject;
        private long author;
        private String text;
    }

    @Getter
    @Setter
    public static class ListResponseArticle {
        private long id;
        private int level;
        private String subject;
        private String authorName;
        private int hit;
        private int like;
        private Instant regDt;
        private Instant modDt;

        public String getSubject() {
            return IntStream.range(1, this.level).mapToObj(e -> "RE :").collect(Collectors.joining()) + subject;
        }
    }
}
