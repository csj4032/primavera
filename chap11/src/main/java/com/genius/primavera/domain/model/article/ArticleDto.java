package com.genius.primavera.domain.model.article;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

public class ArticleDto {

    @Getter
    @Setter
    public static class WriteArticle {
        private long pId;
        private ArticleStatus status = ArticleStatus.PUBLIC;
        @NotEmpty
        private String subject;
        private long author;
        private String contents;
    }

    @Getter
    @Setter
    public static class ListArticle {
        private long id;
        private int level;
        private String subject;
        private String authorName;
        private int hit;
        private int recommend;
        private int disapprove;
        private Instant regDt;
        private Instant modDt;

        public String getSubject() {
            return IntStream.range(1, this.level).mapToObj(e -> "RE : ").collect(Collectors.joining()) + subject;
        }
    }

    @Getter
    @Setter
    public static class DetailArticle {
        private long id;
        private long reference;
        private int step;
        private int level;
        private String subject;
        private String authorName;
        private int hit;
        private int recommend;
        private int disapprove;
        private String contents;
        private Instant regDt;
        private Instant modDt;
    }
}
