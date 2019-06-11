package com.genius.primavera.domain.model.article;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

public class CommentDto {

    @Getter
    @Setter
    public static class Detail {
        private long id;
        private String authorImage;
        private String authorName;
        private String comment;
        private Instant regDt;
        private Instant modDt;
    }
}
